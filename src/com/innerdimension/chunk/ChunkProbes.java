package com.innerdimension.chunk;

import com.innerdimension.core.ServerClock;
import com.innerdimension.core.Tuning;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_3218;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Groups "please load this chunk so I can look at it" requests into one budgeted probe per chunk.
 *
 * <p>Implementation plan section 0.11. A probe is not a permanent hold and not a per-call renewal:
 * the first request for a chunk takes one 40-tick ticket, every other caller in that window shares
 * it, and when the budget runs out the mod stops asking for 200 ticks rather than keeping the chunk
 * alive forever. Waiting for an entity therefore cannot turn into an unbounded load, and a request
 * may wait indefinitely (the object stays where it is) without costing anything during the cooldown.</p>
 *
 * <p>"Releasing" here only ever means dropping this mod's own bookkeeping entry. The ticket type is
 * the shared vanilla ender-pearl type, so explicitly removing it would cancel other people's
 * teleport loads - the plan forbids that, and the ticket expires by itself.</p>
 */
public final class ChunkProbes {
    private record Key(class_3218 world, int x, int z) {
    }

    /** Chunk -> tick its current probe started. */
    private static final Map<Key, Long> ACTIVE = new HashMap<>();
    /** Chunk -> tick before which no new probe may start. */
    private static final Map<Key, Long> COOLDOWN = new HashMap<>();

    private ChunkProbes() {
    }

    /**
     * Makes sure a probe is running for the chunk of {@code pos}.
     *
     * @return {@code true} when a probe is active right now, i.e. the caller may look at the chunk
     */
    public static boolean ensure(class_3218 world, class_2338 pos) {
        if (world == null || pos == null) {
            return false;
        }
        return ensureChunk(world, pos.method_10263() >> 4, pos.method_10260() >> 4);
    }

    /** Same as {@link #ensure}, for callers that already know the chunk coordinates. */
    public static boolean ensureChunk(class_3218 world, int chunkX, int chunkZ) {
        if (world == null) {
            return false;
        }
        Key key = new Key(world, chunkX, chunkZ);
        long now = ServerClock.now();
        Long startedAt = ACTIVE.get(key);
        if (startedAt != null) {
            if (now - startedAt < Tuning.PROBE_TICKS) {
                return true; // shared with whoever asked first
            }
            // Budget spent: no renewal, just remember when the next round may start.
            ACTIVE.remove(key);
            COOLDOWN.put(key, now + Tuning.PROBE_COOLDOWN_TICKS);
            return false;
        }
        Long blockedUntil = COOLDOWN.get(key);
        if (blockedUntil != null) {
            if (blockedUntil > now) {
                return false;
            }
            COOLDOWN.remove(key);
        }
        TemporaryChunkLoads.holdChunk(world, chunkX, chunkZ);
        ACTIVE.put(key, now);
        return true;
    }

    /** Ends a successfully committed probe without removing the shared vanilla ticket. */
    public static void complete(class_3218 world, class_2338 pos) {
        if (world == null || pos == null) return;
        Key key = new Key(world, pos.method_10263() >> 4, pos.method_10260() >> 4);
        ACTIVE.remove(key);
        COOLDOWN.remove(key);
        // Never remove the shared vanilla ticket; it expires naturally.
    }

    /** Advances the budgets; called once per server tick. */
    public static void tick() {
        long now = ServerClock.now();
        Iterator<Map.Entry<Key, Long>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Long> entry = iterator.next();
            if (now - entry.getValue() >= Tuning.PROBE_TICKS) {
                COOLDOWN.put(entry.getKey(), now + Tuning.PROBE_COOLDOWN_TICKS);
                iterator.remove();
            }
        }
        COOLDOWN.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    /**
     * True when the chunk that contains {@code pos} is loaded.
     *
     * <p>Important: {@code WorldView.method_22347(BlockPos)} is <em>isAir</em>, not "is this chunk
     * loaded". {@code ServerWorld.method_67506} checks random ticking and, by default, requires a
     * nearby player; it is unsuitable for a forced chunk in an empty dimension. Use the
     * server chunk manager's non-loading full chunk lookup instead.</p>
     */
    public static boolean chunkLoaded(class_3218 world, class_2338 pos) {
        return LoadedChunks.contains(world, pos);
    }

    /**
     * True when the chunk is loaded far enough that its entities must already be present.
     *
     * <p>Stricter than {@link #chunkLoaded}: this is the check that makes "the entity is not here"
     * usable as evidence, by also requiring the entity section of the chunk to be ready. A false
     * answer only delays evidence, so it is safe to be conservative here.</p>
     */
    public static boolean entityChunkReady(class_3218 world, class_2338 pos) {
        if (!chunkLoaded(world, pos)) {
            return false;
        }
        return world.method_66588(new class_1923(pos.method_10263() >> 4, pos.method_10260() >> 4));
    }

    /** Forgets every budget; used when a server starts, so nothing survives a session. */
    public static void reset() {
        ACTIVE.clear();
        COOLDOWN.clear();
    }

    /** Number of chunks with an active probe, for diagnostics. */
    public static int activeProbeCount() {
        return ACTIVE.size();
    }
}
