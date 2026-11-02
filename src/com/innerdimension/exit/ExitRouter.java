package com.innerdimension.exit;

import com.innerdimension.binding.DollBindingManager;
import com.innerdimension.chunk.ChunkProbes;
import com.innerdimension.core.ServerClock;
import com.innerdimension.core.Tuning;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.entity.SensoryLinkDollEntity;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.security.EntrySafetyGuard;
import com.innerdimension.security.SecurityState;
import com.innerdimension.state.InnerState;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The one place that decides where a player, an item or a mob leaves an inner dimension.
 *
 * <p>Every exit path goes through this class - the pearl-surface sweep, an object crossing the room
 * boundary, the funnel and the pending retry pass - so the rules cannot drift apart between call
 * sites. The resolver only ever <em>decides</em>: it does not spawn, remove or deduct anything, and
 * it never deletes a strong binding except through the central absence sampling, which is the only
 * evidence strong enough to call a port permanently gone.</p>
 *
 * <p>Resolution order (implementation plan section 0.10): the strong port first (any waiting state
 * returns immediately and never falls through), then - only after the strong port was provably
 * removed and cleaned up - re-read the set, then "connections but no strong port" (weak ports never
 * output), then the object's own entry source, and finally the room owner's own return point.</p>
 *
 * <p>One deliberate exception sits in the weak-port step: an object swept off the pearl surface in
 * the slipstream of the owner who just left travels <em>with</em> that player, so it lands on the
 * owner's return point rather than being held back. That exception is keyed to the surface trigger
 * alone - the funnel and the boundary ejection keep waiting, so a weak port still never becomes an
 * automatic logistics exit.</p>
 */
public final class ExitRouter {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");

    /** Objects waiting to leave, with the tick they started waiting and their last warning. */
    private static final class WaitRecord {
        private final long since;
        private long lastWarning = Long.MIN_VALUE;

        private WaitRecord(long since) {
            this.since = since;
        }
    }

    private static final Map<UUID, WaitRecord> WAITING = new HashMap<>();
    private record FunnelWait(ExitResolution.Status status, long tick) {}
    private static final Map<String, FunnelWait> FUNNEL_WAITING = new HashMap<>();
    private record PlayerExit(class_3218 origin, long deadline, Runnable afterLeave) {}
    private static final Map<UUID, PlayerExit> PLAYER_EXITS = new HashMap<>();

    public static boolean playerExitPending(class_3222 player) {
        return PLAYER_EXITS.containsKey(player.method_5667());
    }

    /** The pearl sweep must happen only after its player actually leaves. */
    public static void afterPendingPlayerExit(class_3222 player, Runnable action) {
        PLAYER_EXITS.computeIfPresent(player.method_5667(), (id, request) ->
                new PlayerExit(request.origin(), request.deadline(), action));
    }

    private static void retryPlayers() {
        MinecraftServer server = com.innerdimension.core.ServerHolder.server();
        if (server == null) return;
        for (var entry : new ArrayList<>(PLAYER_EXITS.entrySet())) {
            class_3222 player = server.method_3760().method_14602(entry.getKey());
            PlayerExit request = entry.getValue();
            if (player == null || !player.method_5805() || player.method_51469() != request.origin()
                    || ServerClock.now() >= request.deadline()) {
                if (player != null && player.method_51469() == request.origin()
                        && ServerClock.now() >= request.deadline()) {
                    player.method_7353(net.minecraft.class_2561.method_43471("inner_dimension.binding.exit_unavailable"), true);
                }
                PLAYER_EXITS.remove(entry.getKey());
                if (player != null) com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
                continue;
            }
            if (leavePlayer(player)) {
                PLAYER_EXITS.remove(entry.getKey());
            } else if (!playerExitPending(player)) {
                PLAYER_EXITS.remove(entry.getKey());
                com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
            }
        }
    }

    /** Prepare every chunk touched by the collision search, including a wide entity's box. */
    private static boolean landingChunksReady(Spot spot, class_1297 entity) {
        return landingChunksReady(spot, entity, false);
    }

    private static boolean landingChunksReady(Spot spot, class_1297 entity, boolean prefetch) {
        class_238 box = entity.method_5829();
        double radius = Tuning.NONPLAYER_SEARCH_RADIUS + 1;
        int minX = ((int)Math.floor(spot.x - radius + box.field_1323 - entity.method_23317())) >> 4;
        int maxX = ((int)Math.floor(spot.x + radius + box.field_1320 - entity.method_23317())) >> 4;
        int minZ = ((int)Math.floor(spot.z - radius + box.field_1321 - entity.method_23321())) >> 4;
        int maxZ = ((int)Math.floor(spot.z + radius + box.field_1324 - entity.method_23321())) >> 4;
        boolean ready = true;
        for (int cx = minX; cx <= maxX; cx++) for (int cz = minZ; cz <= maxZ; cz++) {
            if (prefetch) com.innerdimension.chunk.TemporaryChunkLoads.holdChunk(spot.world, cx, cz);
            else ChunkProbes.ensureChunk(spot.world, cx, cz);
            ready &= spot.world.method_14178().method_21730(cx, cz) != null;
        }
        return ready;
    }

    private static void waitForPlayerChunks(class_3222 player, class_3218 origin) {
        PLAYER_EXITS.putIfAbsent(player.method_5667(), new PlayerExit(origin,
                ServerClock.now() + 600, null));
    }

    private ExitRouter() {
    }

    /** Advances the shared clocks; called once per server tick, before the retry pass. */
    public static void tick() {
        ChunkProbes.tick();
        retryPlayers();
        FUNNEL_WAITING.entrySet().removeIf(entry -> ServerClock.now() - entry.getValue().tick() > 1200);
    }

    /** Clears the runtime bookkeeping when a server starts. Persistent requests are untouched. */
    public static void reset() {
        WAITING.clear();
        FUNNEL_WAITING.clear();
        PLAYER_EXITS.clear();
        ChunkProbes.reset();
    }

    /** Objects currently waiting for an output port, for diagnostics. */
    public static Set<UUID> waitingObjects() {
        return Set.copyOf(WAITING.keySet());
    }

    // ------------------------------------------------------------------ players

    /** Prefetch only the resolved landing search footprint, without moving the player. */
    public static void prefetchReturn(class_3222 player) {
        if (!DynamicDimensionManager.isInnerWorld(player.method_51469().method_27983())) return;
        Spot spot = locateReturn(player);
        if (spot != null) landingChunksReady(spot, player, true);
    }

    /** Whether the player can leave right now. */
    public static boolean canLeave(class_3222 player) {
        return locateReturn(player) != null;
    }

    /**
     * Sends a player back to the exact spot they stood on before entering.
     *
     * <p>Transported visitors prefer the current strong exit. If it cannot be used, the room owner's
     * current exterior position or saved exterior return point is the final fallback.</p>
     */
    public static boolean leavePlayer(class_3222 player) {
        if (player == null) {
            return false;
        }
        class_3218 current = player.method_51469();
        if (current == null || !DynamicDimensionManager.isInnerWorld(current.method_27983())) {
            return false;
        }
        MinecraftServer server = current.method_8503();
        if (server == null) {
            return false;
        }
        Spot spot = locateReturn(player);
        if (spot == null) {
            PLAYER_EXITS.remove(player.method_5667());
            LOGGER.warn("[InnerDimension] No usable return point for {}; keeping them in the room",
                    player.method_5667());
            return false;
        }
        if (!landingChunksReady(spot, player)) {
            waitForPlayerChunks(player, current);
            return false;
        }
        if (!com.innerdimension.transport.SafeEntityTeleport.prepare(player)) {
            PLAYER_EXITS.remove(player.method_5667());
            return false;
        }
        class_243 landing = findSafeSpot(spot.world, player, spot.x, spot.y, spot.z);
        if (landing == null) {
            Spot fallback = ownerSpot(server, InnerState.get(server.method_30002()),
                    DynamicDimensionManager.ownerOfWorld(current.method_27983()));
            if (fallback != null) {
                spot = fallback;
                if (!landingChunksReady(spot, player)) {
                    waitForPlayerChunks(player, current);
                    return false;
                }
                landing = findSafeSpot(spot.world, player, spot.x, spot.y, spot.z);
            }
        }
        if (landing == null) {
            PLAYER_EXITS.remove(player.method_5667());
            LOGGER.warn("[InnerDimension] The return point of {} is blocked; nothing safe found",
                    player.method_5667());
            return false;
        }
        boolean moved = com.innerdimension.transport.SafeEntityTeleport.move(player, spot.world,
                landing.field_1352, landing.field_1351, landing.field_1350, Set.of(), spot.yaw, spot.pitch, false);
        if (moved) {
            InnerState.get(server.method_30002()).clearEntrySource(player.method_5667());
            EntrySafetyGuard.noteSuccessfulLeave(player.method_5667());
            PlayerExit request = PLAYER_EXITS.remove(player.method_5667());
            if (request != null && request.afterLeave() != null) {
                try { request.afterLeave().run(); }
                catch (Throwable failure) { LOGGER.error("[InnerDimension] Deferred surface sweep failed", failure); }
            }
        } else {
            PLAYER_EXITS.remove(player.method_5667());
        }
        return moved;
    }

    private record Spot(class_3218 world, double x, double y, double z, float yaw, float pitch) {
    }

    /**
     * Resolves the visit's strong exit with the room owner's exterior position as a fallback.
     *
     * <p>Saved positions inside rooms are refused to avoid sending the visitor into another room.</p>
     */
    private static Spot locateReturn(class_3222 player) {
        class_3218 current = player.method_51469();
        MinecraftServer server = current == null ? null : current.method_8503();
        if (server == null) {
            return null;
        }
        InnerState state = InnerState.get(server.method_30002());
        UUID owner = DynamicDimensionManager.ownerOfWorld(current.method_27983());
        InnerState.EntrySource source = state.getEntrySource(player.method_5667());
        // Boat riders and directly leashed players both carry an entry-dildo record.
        // Prefer this visit's strong port, then the owner's exterior position.
        if (source != null && source.entryDildoUuid() != null && owner != null
                && owner.equals(source.roomOwner())) {
            InnerState.BindingSet bindings = state.bindingSetOrNull(owner);
            InnerState.Binding binding = bindings == null ? null
                    : bindings.bindings.get(bindings.strongDildoUuid);
            if (binding != null) {
                ExitResolution resolution = throughStrongBinding(server, state, bindings, binding);
                if (resolution != null && resolution.ready()) {
                    return new Spot(resolution.world(), resolution.x(), resolution.y(), resolution.z(),
                            player.method_36454(), player.method_36455());
                }
            }
        }
        return ownerSpot(server, state, owner == null ? player.method_5667() : owner);
    }

    /** Online owners outside use their current position; owners in any room use their saved exterior. */
    private static Spot ownerSpot(MinecraftServer server, InnerState state, UUID owner) {
        if (owner == null) return null;
        class_3222 online = server.method_3760().method_14602(owner);
        if (online != null && !DynamicDimensionManager.isInnerWorld(online.method_51469().method_27983())) {
            class_3218 target = online.method_51469();
            ChunkProbes.ensureChunk(target, ((int) Math.floor(online.method_23317())) >> 4,
                    ((int) Math.floor(online.method_23321())) >> 4);
            return new Spot(target, online.method_23317(), online.method_23318(), online.method_23321(),
                    online.method_36454(), online.method_36455());
        }
        InnerState.ReturnPoint point = state.getPlayerReturn(owner);
        if (point == null) {
            return null;
        }
        class_5321<class_1937> key = class_5321.method_29179(class_7924.field_41223, point.dimId);
        if (DynamicDimensionManager.isInnerWorld(key)) {
            return null;
        }
        class_3218 target = server.method_3847(key);
        if (target == null) {
            return null;
        }
        ChunkProbes.ensureChunk(target,
                ((int) Math.floor(point.x)) >> 4, ((int) Math.floor(point.z)) >> 4);
        return new Spot(target, point.x, point.y, point.z, point.yaw, point.pitch);
    }

    // -------------------------------------------------------------- non-players

    /**
     * Sends one non-player entity out of the room, or leaves it exactly where it is.
     *
     * @param trigger why the object is being sent; stored with the request when it has to wait
     * @return {@code true} when the entity really left
     */
    public static boolean sendEntity(class_3218 innerWorld, class_1297 entity, InnerState.ExitTrigger trigger) {
        if (entity instanceof SensoryLinkDollEntity || entity instanceof EnderPearlDildoEntity) return false;
        if (entity instanceof class_3222 player) return leavePlayer(player);
        if (innerWorld == null || entity == null || entity.method_31481()) {
            return false;
        }
        MinecraftServer server = innerWorld.method_8503();
        if (server == null) {
            return false;
        }
        UUID uuid = entity.method_5667();
        ExitResolution resolution = resolve(innerWorld, null, entity, trigger);
        if (!resolution.ready()) {
            noteWaiting(uuid, resolution);
            registerPending(server, innerWorld, uuid, trigger);
            return false;
        }
        if (!com.innerdimension.transport.SafeEntityTeleport.prepare(entity)) {
            registerPending(server, innerWorld, uuid, trigger);
            return false;
        }
        class_243 landing = findSafeSpot(resolution.world(), entity,
                resolution.x(), resolution.y(), resolution.z());
        if (landing == null) {
            noteWaiting(uuid, ExitResolution.waiting(ExitResolution.Status.WAIT_BLOCKED, "blocked landing"));
            registerPending(server, innerWorld, uuid, trigger);
            return false;
        }
        boolean moved = com.innerdimension.transport.SafeEntityTeleport.move(entity, resolution.world(), landing.field_1352, landing.field_1351,
                landing.field_1350, Set.of(), entity.method_36454(), entity.method_36455(), false);
        if (!moved) {
            noteWaiting(uuid, ExitResolution.waiting(ExitResolution.Status.WAIT_BLOCKED, "teleport refused"));
            registerPending(server, innerWorld, uuid, trigger);
            return false;
        }
        InnerState state = InnerState.get(server.method_30002());
        // The teleport can build a fresh instance, so the source marker is cleared on whatever entity
        // is really on the other side now - by UUID, which survives the move.
        class_1297 arrived = resolution.world().method_73284(uuid);
        class_1297 carrier = arrived != null ? arrived : entity;
        if (carrier instanceof class_1542 dropped) {
            ModExitComponents.clear(dropped.method_6983());
        }
        state.clearEntrySource(uuid);
        state.clearPendingExit(uuid);
        WAITING.remove(uuid);
        completeOutputProbe(resolution);
        LOGGER.info("[InnerDimension] Sent {} out to {} at ({}, {}, {})",
                uuid, resolution.world().method_27983().method_29177(),
                (int) Math.floor(landing.field_1352), (int) Math.floor(landing.field_1351),
                (int) Math.floor(landing.field_1350));
        return true;
    }

    /** Convenience for the boundary check, which has no more specific trigger. */
    public static boolean sendEntity(class_3218 innerWorld, class_1297 entity) {
        return sendEntity(innerWorld, entity, InnerState.ExitTrigger.BOUNDARY);
    }

    /**
     * Sends one item of {@code stack} out as a drop.
     *
     * <p>Used by the funnel: the source stack is never modified here, and the caller lowers it only
     * after this returned {@code true}.</p>
     */
    public static boolean outputOneItem(class_3218 innerWorld, class_1799 stack) {
        return outputOneItem(innerWorld, stack, null);
    }

    public static boolean outputOneItem(class_3218 innerWorld, class_1799 stack, class_2338 hopperPos) {
        if (innerWorld == null || stack == null || stack.method_7960()) {
            return false;
        }
        ExitResolution resolution = resolve(innerWorld, stack, null);
        if (!resolution.ready()) {
            noteFunnelWaiting(innerWorld, hopperPos, resolution);
            return false;
        }
        class_1799 payload = stack.method_46651(1);
        ModExitComponents.clear(payload);
        class_1542 item = new class_1542(resolution.world(), resolution.x(), resolution.y(),
                resolution.z(), payload);
        class_243 landing = findSafeSpot(resolution.world(), item,
                resolution.x(), resolution.y(), resolution.z());
        if (landing == null) {
            noteFunnelWaiting(innerWorld, hopperPos,
                    ExitResolution.waiting(ExitResolution.Status.WAIT_BLOCKED, "no safe item landing"));
            return false;
        }
        item.method_5814(landing.field_1352, landing.field_1351, landing.field_1350);
        item.method_18800(0.0D, 0.0D, 0.0D);
        // The caller deducts its own inventory only when the spawn really happened.
        boolean spawned = resolution.world().method_8649(item);
        if (spawned) {
            if (hopperPos != null) FUNNEL_WAITING.remove(innerWorld.method_27983() + ":" + hopperPos);
            completeOutputProbe(resolution);
            LOGGER.info("[InnerDimension] Funnel output to {} at ({}, {}, {})",
                    resolution.world().method_27983().method_29177(),
                    (int) Math.floor(landing.field_1352), (int) Math.floor(landing.field_1351),
                    (int) Math.floor(landing.field_1350));
        } else {
            noteFunnelWaiting(innerWorld, hopperPos,
                    ExitResolution.waiting(ExitResolution.Status.WAIT_BLOCKED, "target world refused item spawn"));
        }
        return spawned;
    }

    private static void noteFunnelWaiting(class_3218 world, class_2338 pos, ExitResolution resolution) {
        if (pos == null) return;
        String key = world.method_27983() + ":" + pos;
        long now = ServerClock.now();
        FunnelWait previous = FUNNEL_WAITING.get(key);
        if (previous == null || (previous.status() != resolution.status() && now - previous.tick() >= 20)
                || now - previous.tick() >= 200) {
            FUNNEL_WAITING.put(key, new FunnelWait(resolution.status(), now));
            LOGGER.info("[InnerDimension] Funnel at {} in {} waits: {} ({})", pos,
                    world.method_27983().method_29177(), resolution.status(), resolution.reason());
        }
    }

    /**
     * The whole exit rule set, without performing any transfer.
     *
     * <p>Used by the callers that do not know why an object is leaving and must not take the
     * surface-only route: the funnel's item output. It resolves as if no trigger were known.</p>
     *
     * @param stack  the item being sent out, or {@code null} when an entity is being sent
     * @param entity the entity being sent out, or {@code null} for an item
     */
    public static ExitResolution resolve(class_3218 innerWorld, class_1799 stack, class_1297 entity) {
        return resolve(innerWorld, stack, entity, null);
    }

    /**
     * The whole exit rule set, without performing any transfer.
     *
     * @param stack   the item being sent out, or {@code null} when an entity is being sent
     * @param entity  the entity being sent out, or {@code null} for an item
     * @param trigger why the object is leaving, or {@code null} for the callers that have no reason
     *                to give. Only {@link InnerState.ExitTrigger#SURFACE} opens the weak-port
     *                fallback to the room owner's return point; everything else, {@code null}
     *                included, keeps the "weak ports cannot output" wait.
     */
    private static ExitResolution resolve(class_3218 innerWorld, class_1799 stack, class_1297 entity,
                                          InnerState.ExitTrigger trigger) {
        MinecraftServer server = innerWorld.method_8503();
        if (server == null) {
            return ExitResolution.waiting(ExitResolution.Status.WAIT_NO_SOURCE, "no server");
        }
        UUID owner = DynamicDimensionManager.ownerOfWorld(innerWorld.method_27983());
        if (owner == null) {
            return ExitResolution.waiting(ExitResolution.Status.WAIT_NO_SOURCE, "not an inner dimension");
        }
        InnerState state = InnerState.get(server.method_30002());

        // --- 1/2. the strong port, or a strong port that was provably cleaned up ---
        InnerState.BindingSet set = state.bindingSetOrNull(owner);
        if (set != null && set.strongDildoUuid != null) {
            InnerState.Binding strong = set.bindings.get(set.strongDildoUuid);
            if (strong != null) {
                ExitResolution throughStrong = throughStrongBinding(server, state, set, strong);
                if (throughStrong != null) {
                    return throughStrong;
                }
                // Proven absent and cleaned up: re-read the set before deciding anything else.
                set = state.bindingSetOrNull(owner);
            }
        }

        // --- 3. connections without a strong port ---
        // A weak port is not an output port: by default the object waits. The one exception is the
        // surface sweep that follows an owner who has already left - those objects are travelling
        // with that player, so they are sent to the same return point instead of being held back.
        // The funnel, a boundary ejection and the retry of either keep waiting exactly as before.
        boolean weakOnly = set != null && !set.bindings.isEmpty()
                && set.strongDildoUuid == null;
        if (weakOnly) {
            if (trigger == InnerState.ExitTrigger.SURFACE) {
                ExitResolution throughReturn = throughOwnerReturn(server, state, owner);
                if (throughReturn != null) {
                    return throughReturn;
                }
                return ExitResolution.waiting(ExitResolution.Status.WAIT_NO_SOURCE,
                        "no usable owner return point for surface exit");
            }
            return ExitResolution.waiting(ExitResolution.Status.WAIT_NO_STRONG,
                    "no strong output port (weak ports cannot output)");
        }

        // --- 4. the object's own entry source ---
        UUID objectUuid = entity == null ? null : entity.method_5667();
        class_1799 carrierStack = stack;
        if (carrierStack == null && entity instanceof class_1542 dropped) {
            // A dropped item carries its source on the stack it holds, not on an entity side table.
            carrierStack = dropped.method_6983();
        }
        InnerState.EntrySource source = carrierStack != null
                ? ModExitComponents.sourceOf(carrierStack)
                : (objectUuid == null ? null : state.getEntrySource(objectUuid));
        boolean sourceUsable = false;
        if (source != null) {
            if (source.roomOwner() == null || !source.roomOwner().equals(owner)) {
                // A record that belongs to another room must not route this object through a
                // stranger's entrance; the room owner's own return point is considered instead.
                LOGGER.debug("[InnerDimension] Ignoring a source that belongs to another room");
            } else if (!source.valid()) {
                LOGGER.debug("[InnerDimension] Ignoring an incomplete source record");
            } else {
                sourceUsable = true;
                ExitResolution throughSource = throughEntrySource(server, state, source);
                if (throughSource != null) {
                    return throughSource;
                }
            }
        }

        // --- 5. the room owner's own return point (newest first, the legacy record second) ---
        ExitResolution throughReturn = throughOwnerReturn(server, state, owner);
        if (throughReturn != null) {
            return throughReturn;
        }
        return ExitResolution.waiting(ExitResolution.Status.WAIT_NO_SOURCE,
                "no binding, no usable source and no return point");
    }

    /**
     * The room owner's own return point: where that player stood before entering this dimension.
     *
     * <p>An online owner in the exterior supplies their current position. Otherwise the owner's
     * last saved exterior position is used, including while the owner is in another inner room.</p>
     *
     * @return the ready resolution, or {@code null} when this owner has no usable return point
     */
    private static ExitResolution throughOwnerReturn(MinecraftServer server, InnerState state, UUID owner) {
        Spot spot = ownerSpot(server, state, owner);
        if (spot == null) return null;
        return ExitResolution.ready(spot.world, spot.x, spot.y, spot.z,
                null, null, state.bindingRevision(owner), 0);
    }

    /**
     * Output through the dimension's one strong port.
     *
     * @return the resolution, or {@code null} when the connection was proven gone and removed
     */
    private static ExitResolution throughStrongBinding(MinecraftServer server, InnerState state,
                                                       InnerState.BindingSet set,
                                                       InnerState.Binding strong) {
        int revision = set.bindingRevision;
        // A lock suspends the port: the record stays, nothing falls back, nothing is deleted.
        InnerState.SecurityLock lock = state.lockOfDoll(strong.dollUuid);
        if (lock == null) {
            lock = state.lockOfDildo(strong.dildoUuid);
        }
        if (lock != null) {
            return ExitResolution.waiting(ExitResolution.Status.WAIT_LOCKED, "the output port is locked");
        }
        if (!strong.authorized) {
            // An old connection nobody has claimed yet: wait for its owner instead of transferring.
            return ExitResolution.waiting(ExitResolution.Status.WAIT_VALIDATION,
                    "the output port has not been authorized yet");
        }
        class_5321<class_1937> key = class_5321.method_29179(class_7924.field_41223, strong.dimId);
        if (DynamicDimensionManager.isInnerWorld(key)) {
            // Nested rooms have no defined output yet; the object waits rather than looping.
            return ExitResolution.waiting(ExitResolution.Status.WAIT_VALIDATION,
                    "nested inner dimensions have no defined output");
        }
        class_3218 world = server.method_3847(key);
        if (world == null) {
            // "The world is not here right now" is not "the port is gone": keep the record.
            return ExitResolution.waiting(ExitResolution.Status.WAIT_MISSING_WORLD,
                    "the recorded output world does not exist right now");
        }
        class_2338 dollPos = new class_2338((int) Math.floor(strong.dollX),
                (int) Math.floor(strong.dollY), (int) Math.floor(strong.dollZ));
        // A FULL chunk can still have unloaded/non-ticking entities. Probe even when its
        // blocks are present, so output does not depend on a player returning outside.
        ChunkProbes.ensure(world, dollPos);
        if (!ChunkProbes.chunkLoaded(world, dollPos)) {
            // A destination only needs its chunk loaded; requiring the entity section to be ready as
            // well would keep an otherwise fine port waiting forever.
            ChunkProbes.ensure(world, dollPos);
            return ExitResolution.waiting(ExitResolution.Status.WAIT_LOAD,
                    "the output chunk " + (dollPos.method_10263() >> 4) + "," + (dollPos.method_10260() >> 4)
                            + " in " + strong.dimId + " is not loaded yet");
        }
        class_1297 doll = world.method_73284(strong.dollUuid);
        if (doll instanceof SensoryLinkDollEntity connected) {
            UUID dollOwner = DollBindingManager.ownerOf(connected);
            if (dollOwner == null) {
                return ExitResolution.waiting(ExitResolution.Status.WAIT_VALIDATION,
                        "the output doll's profile is not resolved");
            }
            if (!dollOwner.equals(strong.ownerUuid)) {
                LOGGER.warn("[InnerDimension] The doll {} no longer belongs to {}; dropping the connection",
                        strong.dollUuid, strong.ownerUuid);
                state.removeBinding(strong.ownerUuid, strong.dildoUuid);
                DollBindingManager.noteEntitySeen(strong.dildoUuid);
                return null;
            }
            DollBindingManager.noteEntitySeen(strong.dildoUuid);
            if (!connected.isSettled()) {
                return ExitResolution.waiting(ExitResolution.Status.WAIT_MOVING,
                        "the output doll is still moving: " + connected.settledDebug());
            }
            return ExitResolution.ready(world, connected.method_23317(), connected.method_23318(),
                    connected.method_23321(), strong.dildoUuid, strong.dollUuid, revision, 0);
        }
        if (doll != null) {
            LOGGER.warn("[InnerDimension] The doll {} of the strong output of {} is gone (type mismatch)",
                    strong.dollUuid, strong.ownerUuid);
            state.removeBinding(strong.ownerUuid, strong.dildoUuid);
            DollBindingManager.noteEntitySeen(strong.dildoUuid);
            return null;
        }
        if (DollBindingManager.observeAbsent(strong.dildoUuid, dollPos,
                ChunkProbes.entityChunkReady(world, dollPos))
                != DollBindingManager.AbsenceObservation.CONFIRMED_ABSENT) {
            state.replaceBinding(strong.withVerified(false));
            return ExitResolution.waiting(ExitResolution.Status.WAIT_VALIDATION,
                    "verifying the output doll");
        }
        LOGGER.warn("[InnerDimension] The doll of the strong output of {} is gone; dropping the connection",
                strong.ownerUuid);
        state.removeBinding(strong.ownerUuid, strong.dildoUuid);
        DollBindingManager.noteEntitySeen(strong.dildoUuid);
        return null;
    }

    /**
     * Output through the dildo the object came in through.
     *
     * @return the resolution, or {@code null} when this source cannot be used and the caller should
     *         continue with the next rule
     */
    private static ExitResolution throughEntrySource(MinecraftServer server, InnerState state,
                                                     InnerState.EntrySource source) {
        if (source.entryDildoUuid() != null && state.lockOfDildo(source.entryDildoUuid()) != null) {
            return ExitResolution.waiting(ExitResolution.Status.WAIT_LOCKED, "the entry port is locked");
        }
        class_5321<class_1937> key = class_5321.method_29179(class_7924.field_41223, source.dimId());
        if (DynamicDimensionManager.isInnerWorld(key)) {
            return ExitResolution.waiting(ExitResolution.Status.WAIT_VALIDATION,
                    "the entry source is itself an inner dimension");
        }
        class_3218 world = server.method_3847(key);
        if (world == null) {
            return ExitResolution.waiting(ExitResolution.Status.WAIT_MISSING_WORLD,
                    "the entry world is not available right now");
        }
        class_2338 recorded = new class_2338((int) Math.floor(source.dildoX()),
                (int) Math.floor(source.dildoY()), (int) Math.floor(source.dildoZ()));
        ChunkProbes.ensure(world, recorded);
        if (source.entryDildoUuid() != null) {
            if (!ChunkProbes.chunkLoaded(world, recorded)) {
                ChunkProbes.ensure(world, recorded);
                return ExitResolution.waiting(ExitResolution.Status.WAIT_LOAD,
                        "the entry chunk " + (recorded.method_10263() >> 4) + ","
                                + (recorded.method_10260() >> 4) + " in " + source.dimId()
                                + " is not loaded yet");
            }
            class_1297 dildo = world.method_73284(source.entryDildoUuid());
            if (dildo instanceof EnderPearlDildoEntity present) {
                // The entry device still exists, so its own security state decides.
                DollSecurityManager.Gate gate =
                        DollSecurityManager.forPort(present, source.roomOwner());
                if (gate.state() == SecurityState.LOCKED) {
                    return ExitResolution.waiting(ExitResolution.Status.WAIT_LOCKED,
                            "the entry device is locked");
                }
                if (gate.blocked()) {
                    return ExitResolution.waiting(ExitResolution.Status.DENY_AUTH,
                            "the entry device refuses this owner");
                }
                return ExitResolution.ready(world, present.method_23317(), present.method_23318(),
                        present.method_23321(), present.method_5667(), null,
                        state.bindingRevision(source.roomOwner()), 0);
            }
        }
        // The entry device is provably gone (its chunk is ready and it is not there): its last known
        // position is the next best answer, and the position the object came from is the last resort.
        ChunkProbes.ensure(world, recorded);
        return ExitResolution.ready(world, source.dildoX(), source.dildoY(), source.dildoZ(),
                null, null, state.bindingRevision(source.roomOwner()), 0);
    }

    // ----------------------------------------------------------- pending exits

    /** Remembers a waiting object so the retry pass can find it again. */
    private static void registerPending(MinecraftServer server, class_3218 innerWorld, UUID uuid,
                                        InnerState.ExitTrigger trigger) {
        if (uuid == null || trigger == null || server == null) {
            return;
        }
        UUID owner = DynamicDimensionManager.ownerOfWorld(innerWorld.method_27983());
        if (owner == null) {
            return;
        }
        InnerState.get(server.method_30002()).setPendingExit(uuid,
                new InnerState.PendingExit(owner, trigger));
    }

    /**
     * Retries the objects that are still waiting, every {@link Tuning#EXIT_RETRY_TICKS} ticks.
     *
     * <p>Only objects in a room that is loaded right now are looked at: waiting for an object in an
     * unloaded chunk must not keep that chunk loaded. A request is cleared when the object left, died
     * or belongs to a different room, and kept otherwise.</p>
     */
    public static void processPending(MinecraftServer server) {
        if (server == null || ServerClock.now() % Tuning.EXIT_RETRY_TICKS != 0) {
            return;
        }
        InnerState state = InnerState.get(server.method_30002());
        Map<UUID, InnerState.PendingExit> pending = state.pendingExits();
        if (pending.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, InnerState.PendingExit> entry : new ArrayList<>(pending.entrySet())) {
            UUID uuid = entry.getKey();
            InnerState.PendingExit request = entry.getValue();
            class_3218 room = server.method_3847(DynamicDimensionManager.worldKey(request.roomOwner()));
            if (room == null) {
                // The room is gone, so this request can never be fulfilled.
                state.clearPendingExit(uuid);
                WAITING.remove(uuid);
                continue;
            }
            class_1297 entity = room.method_73284(uuid);
            if (entity == null) {
                // Not loaded right now: keep the request rather than forgetting the object.
                continue;
            }
            if (entity.method_31481()) {
                state.clearPendingExit(uuid);
                WAITING.remove(uuid);
                continue;
            }
            try {
                sendEntity(room, entity, request.trigger());
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Pending output retry failed for {}", uuid, throwable);
            }
        }
    }

    // ----------------------------------------------------------------- helpers

    /** A committed transfer ends this probe; the next transaction may start a fresh budget. */
    private static void completeOutputProbe(ExitResolution resolution) {
        ChunkProbes.complete(resolution.world(), new class_2338((int) Math.floor(resolution.x()),
                (int) Math.floor(resolution.y()), (int) Math.floor(resolution.z())));
    }

    /**
     * A place near the requested point where the entity's own collision box fits.
     *
     * <p>The search order is fixed by the plan: height first, then a growing horizontal ring, closest
     * first. Nothing is guessed from a single block being air - the entity's complete box has to be
     * accepted by the world, which also rules out the build-height limits and the world border.</p>
     *
     * @return the position to transfer to, or {@code null} when the neighbourhood is full
     */
    public static class_243 findSafeSpot(class_3218 world, class_1297 entity,
                                         double baseX, double baseY, double baseZ) {
        class_238 box = entity.method_5829();
        double fromX = entity.method_23317();
        double fromY = entity.method_23318();
        double fromZ = entity.method_23321();
        int worldBottom = world.method_31607();
        for (int dy = 0; dy <= Tuning.NONPLAYER_SEARCH_UP; dy++) {
            for (int radius = 0; radius <= Tuning.NONPLAYER_SEARCH_RADIUS; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        double x = baseX + dx;
                        double y = baseY + dy;
                        double z = baseZ + dz;
                        if (y < worldBottom) {
                            continue; // below the world floor
                        }
                        class_238 moved = box.method_989(x - fromX, y - fromY, z - fromZ);
                        if (world.method_8587(entity, moved)) {
                            return new class_243(x, y, z);
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Logs a waiting object once, then at a throttled rate while the same reason persists. */
    private static void noteWaiting(UUID uuid, ExitResolution resolution) {
        if (uuid == null) {
            return;
        }
        long now = ServerClock.now();
        WaitRecord record = WAITING.computeIfAbsent(uuid, key -> {
            LOGGER.info("[InnerDimension] Object {} waits for an output port: {} ({})",
                    key, resolution.status(), resolution.reason());
            return new WaitRecord(now);
        });
        if (now - record.since >= Tuning.WAIT_WARN_TICKS
                && now - record.lastWarning >= Tuning.REPEAT_WARN_TICKS) {
            record.lastWarning = now;
            LOGGER.warn("[InnerDimension] Object {} has been waiting {} ticks: {} ({})",
                    uuid, now - record.since, resolution.status(), resolution.reason());
        }
    }

    /** Diagnostics line for the status command. */
    public static net.minecraft.class_2561 describe(class_3218 innerWorld) {
        MinecraftServer server = innerWorld.method_8503();
        UUID owner = DynamicDimensionManager.ownerOfWorld(innerWorld.method_27983());
        if (server == null || owner == null) {
            return net.minecraft.class_2561.method_43471("inner_dimension.command.not_inside");
        }
        InnerState state = InnerState.get(server.method_30002());
        InnerState.BindingSet set = state.bindingSetOrNull(owner);
        int connections = set == null ? 0 : set.bindings.size();
        UUID strong = set == null ? null : set.strongDildoUuid;
        return net.minecraft.class_2561.method_43469("inner_dimension.command.status",
                connections, strong == null ? net.minecraft.class_2561.method_43471("inner_dimension.command.none") : strong.toString(),
                state.bindingRevision(owner), WAITING.size(), state.pendingExits().size(),
                state.locks().size(), ChunkProbes.activeProbeCount());
    }

    /** Forces a fresh chunk position object for a position. */
    public static class_1923 chunkOf(double x, double z) {
        return new class_1923(((int) Math.floor(x)) >> 4, ((int) Math.floor(z)) >> 4);
    }
}
