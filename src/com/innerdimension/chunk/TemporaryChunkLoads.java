package com.innerdimension.chunk;

import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_3215;
import net.minecraft.class_3218;
import net.minecraft.class_3230;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Short-lived chunk loads, kept strictly separate from persistent force-loading.
 *
 * <p>The previous implementation used {@code ServerWorld#method_17988} (setChunkForced), which is
 * the very call the {@code /forceload} command uses and whose state is written into
 * {@code chunks.dat}. Adding a temporary load that way meant a crash or a {@code /stop} between
 * the add and the release left a <em>permanent</em> force-load behind, and a release could cancel
 * an administrator's own {@code /forceload} on the same chunk.</p>
 *
 * <p>This class instead uses the vanilla ticket the ender-pearl teleport uses: TicketType
 * {@code minecraft:ender_pearl}, a 40-tick budget and no persistence flag, so the TicketManager
 * expires it on its own and nothing survives a restart. Holding the same chunk again simply
 * resets that budget, which is exactly the "repeated input extends the same batch" behaviour the
 * specification asks for.</p>
 *
 * <p>Every call must happen on the server thread: the ticket manager is an unsynchronised
 * {@code SavedData} owned by the server tick.</p>
 */
public final class TemporaryChunkLoads {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");

    /** Same reach vanilla uses for a teleport: level 31, i.e. entity-ticking. */
    private static final int TICKET_RADIUS = 2;
    /** Ticket budget of {@code minecraft:ender_pearl}, in server ticks. */
    public static final long TICKET_TICKS = 40L;

    private record ChunkKey(class_3218 world, int x, int z) {
    }

    /** Chunks this mod holds right now, mapped to the tick their ticket is expected to expire. */
    private static final Map<ChunkKey, Long> HOLDS = new HashMap<>();
    private static long serverTicks;

    private TemporaryChunkLoads() {
    }

    /** Advances the bookkeeping clock. Called once per server tick. */
    public static void tick() {
        serverTicks++;
        HOLDS.entrySet().removeIf(entry -> entry.getValue() <= serverTicks);
    }

    /**
     * Forgets the bookkeeping without releasing anything.
     *
     * <p>Used when a server starts: the static map survives an integrated-server restart in the
     * same JVM, while the tickets themselves belonged to the previous session and are already
     * gone, so releasing them then would be wrong.</p>
     */
    public static void reset() {
        HOLDS.clear();
        serverTicks = 0L;
    }

    /** Holds the chunk that contains {@code pos} for the ticket budget. */
    public static void hold(class_3218 world, class_2338 pos) {
        if (world == null || pos == null) {
            return;
        }
        holdChunk(world, pos.method_10263() >> 4, pos.method_10260() >> 4);
    }

    /**
     * Holds one chunk for the ticket budget, renewing it when it is already held.
     *
     * <p>Re-adding an equal ticket only resets its remaining ticks, so calling this for the same
     * chunk within one legal operation extends the hold without accumulating garbage.</p>
     */
    public static void holdChunk(class_3218 world, int chunkX, int chunkZ) {
        if (world == null) {
            return;
        }
        class_3215 chunkManager = world.method_14178();
        if (chunkManager == null) {
            return;
        }
        chunkManager.method_66009(class_3230.field_54211, new class_1923(chunkX, chunkZ), TICKET_RADIUS);
        HOLDS.put(new ChunkKey(world, chunkX, chunkZ), serverTicks + TICKET_TICKS);
    }

    /**
     * Drops this mod's bookkeeping entry for a chunk. It deliberately does <em>not</em> remove the
     * ticket.
     *
     * <p>The ticket type is the vanilla ender-pearl type and is shared with real pearls and with
     * other mods, so there is no exclusive proof of ownership: calling
     * {@code method_66010(field_54211, ...)} would cancel somebody else's teleport load. The ticket
     * expires on its own after its budget, and a world unload takes it with the world.</p>
     */
    public static void forget(class_3218 world, int chunkX, int chunkZ) {
        HOLDS.remove(new ChunkKey(world, chunkX, chunkZ));
    }

    /**
     * Forgets every bookkeeping entry on server stop, without removing any ticket.
     *
     * <p>Nothing here is persisted: vanilla does not save this ticket type, so it cannot outlive the
     * server. This only clears the mod's own view, and the configured keep-alive chunks and an
     * administrator's {@code /forceload} are never handled by this class at all.</p>
     */
    public static void forgetAll() {
        int forgotten = HOLDS.size();
        HOLDS.clear();
        LOGGER.info("[InnerDimension] Forgot {} temporary chunk-load entries on shutdown "
                + "(tickets are left to expire on their own)", forgotten);
    }

    /** Number of chunks currently held, for the diagnostics command. */
    public static int heldChunkCount() {
        return HOLDS.size();
    }
}
