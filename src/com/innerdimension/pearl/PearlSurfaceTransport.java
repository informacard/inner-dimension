package com.innerdimension.pearl;

import com.innerdimension.block.ModBlocks;
import com.innerdimension.core.ServerClock;
import com.innerdimension.core.Tuning;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.entity.SensoryLinkDollEntity;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.exit.ExitResolution;
import com.innerdimension.exit.ExitRouter;
import com.innerdimension.state.InnerState;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2614;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Everything that happens at the pearl surface of a room: the sneak-right-click sweep and the funnel
 * that stands on the floor.
 *
 * <p>The floor block's own use hook and the Fabric {@code UseBlockCallback} both land here, so one
 * click - empty hand, held item, main hand or off hand - runs one transaction. The first answer of a
 * click is cached for a few ticks and reused, so a click that failed cannot come back as a success
 * through the other hook.</p>
 */
public final class PearlSurfaceTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");

    /** The cached first answer of a click, so the second hook cannot change it. */
    private record ClickResult(long tick, boolean success) {
    }

    private static final Map<UUID, ClickResult> HANDLED = new HashMap<>();
    private record EyeExit(class_3218 origin, long due, List<class_1297> candidates) {}
    private static final Map<UUID, EyeExit> EYE_EXITS = new HashMap<>();

    private PearlSurfaceTransport() {
    }

    /** Expires the click cache; called once per server tick. */
    public static void tick() {
        long now = ServerClock.now();
        HANDLED.entrySet().removeIf(entry -> now - entry.getValue().tick() > Tuning.SURFACE_DEDUPE_TICKS);
        for (var entry : new ArrayList<>(EYE_EXITS.entrySet())) {
            var request = entry.getValue();
            if (now < request.due()) continue;
            EYE_EXITS.remove(entry.getKey());
            var player = request.origin().method_8503().method_3760().method_14602(entry.getKey());
            if (player == null || !player.method_5805() || player.method_51469() != request.origin()) continue;
            com.innerdimension.network.EntryTransitionPayload.send(player, true, false);
            finishSurfaceExit(player, request.origin(), request.candidates(), now);
        }
    }

    /** Clears the cache when a server starts. */
    public static void reset() {
        HANDLED.clear();
        EYE_EXITS.clear();
    }

    /**
     * Sneak + right-click on a pearl surface.
     *
     * <p>The player's own leave is attempted first: a click that cannot send the player out must not
     * shuffle animals and items around. The candidates are snapshotted before the player moves, so the
     * sweep works on this room and never on the world the player is about to arrive in.</p>
     *
     * @return {@code true} when the interaction was consumed
     */
    public static boolean leaveFromSurface(class_3222 player, class_1937 world) {
        if (player == null || world == null || world.method_8608()) {
            return false;
        }
        if (!player.method_5715()) {
            return false;
        }
        if (!(world instanceof class_3218 serverWorld)
                || !DynamicDimensionManager.isInnerWorld(serverWorld.method_27983())) {
            return false;
        }
        UUID playerId = player.method_5667();
        long now = ServerClock.now();
        ClickResult cached = HANDLED.get(playerId);
        if (cached != null && now - cached.tick() <= Tuning.SURFACE_DEDUPE_TICKS) {
            // The other hook of the same click: the first answer stands.
            return cached.success();
        }
        // One click already owns the continuation. Do not capture/sweep the same entities twice.
        if (ExitRouter.playerExitPending(player) || EYE_EXITS.containsKey(playerId)) return true;
        // Snapshot before the player moves, so the sweep cannot accidentally work on the world the
        // player is about to arrive in.
        List<class_1297> candidates = snapshotSurface(serverWorld);

        if (!ExitRouter.canLeave(player)) {
            HANDLED.put(playerId, new ClickResult(now, false));
            player.method_7353(class_2561.method_43471("inner_dimension.binding.exit_unavailable"), true);
            return false;
        }
        com.innerdimension.network.EntryTransitionPayload.send(player, false, false);
        ExitRouter.prefetchReturn(player);
        EYE_EXITS.put(playerId, new EyeExit(serverWorld, now + 4, candidates));
        HANDLED.put(playerId, new ClickResult(now, true));
        return true;
    }

    private static boolean finishSurfaceExit(class_3222 player, class_3218 serverWorld,
                                              List<class_1297> candidates, long now) {
        UUID playerId = player.method_5667();

        if (!ExitRouter.canLeave(player) || !ExitRouter.leavePlayer(player)) {
            if (ExitRouter.playerExitPending(player)) {
                ExitRouter.afterPendingPlayerExit(player, () -> {
                    com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
                    sendCandidates(serverWorld, candidates);
                });
                HANDLED.put(playerId, new ClickResult(now, true));
                return true;
            }
            com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
            HANDLED.put(playerId, new ClickResult(now, false));
            player.method_7353(class_2561.method_43471("inner_dimension.binding.exit_unavailable"), true);
            return false;
        }
        HANDLED.put(playerId, new ClickResult(now, true));
        com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
        try {
            sendCandidates(serverWorld, candidates);
        } catch (Throwable throwable) {
            LOGGER.error("[InnerDimension] The pearl surface sweep failed", throwable);
        }
        return true;
    }

    /**
     * Every player, mob and dropped item resting on a pearl surface of this dimension.
     *
     * <p>Only loaded entities are looked at: the sweep never force-loads a dimension to find something
     * to send out, and it covers the whole room rather than just the clicked block.</p>
     */
    public static List<class_1297> snapshotSurface(class_3218 world) {
        List<class_1297> candidates = new ArrayList<>();
        for (class_1297 entity : world.method_27909()) {
            if (entity.method_5854() != null) {
                // A passenger is moved as part of its ride tree below, never on its own.
                continue;
            }
            if (isSurfaceCandidate(world, entity)) {
                candidates.add(entity);
            }
        }
        return candidates;
    }

    /** Sends the snapshotted roots, each with its whole ride tree. */
    private static void sendCandidates(class_3218 world, List<class_1297> candidates) {
        for (class_1297 entity : candidates) {
            if (entity.method_31481() || entity.method_73183() != world) {
                continue;
            }
            try {
                sendTree(world, entity);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to send {} off the pearl surface",
                        entity.method_5667(), throwable);
            }
        }
    }

    /**
     * Sends a ride tree as one unit.
     *
     * <p>The whole tree is collected first. Players use their visit's player-exit rule;
     * devices are excluded even when somebody has mounted them with another mod.</p>
     */
    private static void sendTree(class_3218 world, class_1297 root) {
        List<class_1297> tree = new ArrayList<>();
        collectTree(root, tree);
        boolean anyLeft = false;
        for (class_1297 member : tree) {
            if (member instanceof SensoryLinkDollEntity || member instanceof EnderPearlDildoEntity
                    || member.method_73183() != world) continue;
            boolean ok = ExitRouter.sendEntity(world, member, InnerState.ExitTrigger.SURFACE);
            if (!ok) {
                if (anyLeft) {
                    // Half a ride has left. This is deliberately not called a transaction: a
                    // cross-dimension teleport builds new instances, so the original bodies cannot
                    // always be restored. What is guaranteed is that no second copy is created and the
                    // case is logged for manual diagnosis.
                    LOGGER.error("[InnerDimension] The ride tree of {} could only be sent partially; "
                            + "the remaining members stay in the room", root.method_5667());
                }
                return;
            }
            anyLeft = true;
        }
    }

    /** Every vehicle and passenger of a tree, root first. */
    private static void collectTree(class_1297 entity, List<class_1297> out) {
        out.add(entity);
        for (class_1297 passenger : entity.method_5685()) {
            collectTree(passenger, out);
        }
    }

    /**
     * True for a mob or a dropped item whose feet rest on the surface of a pearl_face block.
     *
     * <p>The test uses the bottom of the collision box against the top of the block below, with the
     * fixed tolerance, and requires real X/Z overlap on both axes with that surface block. A funnel
     * standing on the floor lifts its cargo one block higher, so standing on a funnel is not a
     * candidate, and neither is anything floating above the surface.</p>
     */
    public static boolean isSurfaceCandidate(class_3218 world, class_1297 entity) {
        if (entity instanceof SensoryLinkDollEntity || entity instanceof EnderPearlDildoEntity) {
            return false; // this mod's own actors are never swept
        }
        boolean item = entity instanceof class_1542;
        boolean mob = entity instanceof class_1308 || entity instanceof class_3222;
        if (!item && !mob) {
            // Projectiles, armour stands, boats and other decoration are out of scope.
            return false;
        }
        return touchesPearlSurface(world, entity);
    }

    /** True when the entity's collision box rests on the top face of a pearl_face block. */
    public static boolean touchesPearlSurface(class_3218 world, class_1297 entity) {
        class_238 box = entity.method_5829();
        double bottom = box.field_1322;
        int blockY = (int) Math.floor(bottom + Tuning.SURFACE_VERTICAL_EPSILON) - 1;
        double gap = Math.abs(bottom - (blockY + 1.0D));
        if (gap > Tuning.SURFACE_VERTICAL_EPSILON) {
            return false;
        }
        // Resting has to be supported: the vanilla on-ground flag, or a gap small enough that the
        // entity is sitting on the surface right now.
        if (!entity.method_24828() && gap > Tuning.SURFACE_VERTICAL_EPSILON / 2.0D) {
            return false;
        }
        double minX = box.field_1323;
        double maxX = box.field_1320;
        double minZ = box.field_1321;
        double maxZ = box.field_1324;
        int minBlockX = (int) Math.floor(minX);
        int maxBlockX = (int) Math.floor(maxX);
        int minBlockZ = (int) Math.floor(minZ);
        int maxBlockZ = (int) Math.floor(maxZ);
        for (int x = minBlockX; x <= maxBlockX; x++) {
            double overlapX = Math.min(maxX, x + 1.0D) - Math.max(minX, x);
            if (overlapX <= Tuning.SURFACE_OVERLAP_EPSILON) {
                continue;
            }
            for (int z = minBlockZ; z <= maxBlockZ; z++) {
                double overlapZ = Math.min(maxZ, z + 1.0D) - Math.max(minZ, z);
                if (overlapZ <= Tuning.SURFACE_OVERLAP_EPSILON) {
                    continue;
                }
                if (world.method_8320(new class_2338(x, blockY, z)).method_26204() == ModBlocks.PEARL_FACE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The funnel hook: a vanilla hopper standing directly on a pearl_face block inside an inner
     * dimension inserts one item into a chest adjacent to its room's strong dildo.
     *
     * <p>Called from the head of the hopper's own push step, which is reached only when the hopper is
     * unlocked, off cooldown and not empty - so a redstone-locked hopper never produces output and its
     * inventory cannot change. The return value is the "something moved" answer the caller uses to set
     * the vanilla 8-tick cooldown; reporting {@code false} therefore only means "try again next tick",
     * with the inventory untouched.</p>
     *
     * @return true when exactly one item left through the exit
     */
    public static boolean outputOneItemFrom(class_3218 world, class_2614 hopper) {
        return com.innerdimension.transport.DildoHopperTransport.outputToChests(world, hopper);
    }
}
