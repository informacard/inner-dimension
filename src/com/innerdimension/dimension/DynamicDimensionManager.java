package com.innerdimension.dimension;

import com.google.common.collect.ImmutableList;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.binding.DollBindingManager;
import com.innerdimension.chunk.TemporaryChunkLoads;
import com.innerdimension.config.InnerConfig;
import com.innerdimension.core.ServerClock;
import com.innerdimension.core.ServerHolder;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.exit.ExitRouter;
import com.innerdimension.mixin.MappedRegistryAccessor;
import com.innerdimension.mixin.MinecraftServerAccessor;
import com.innerdimension.mixin.ServerWorldAccessor;
import com.innerdimension.pearl.PearlSurfaceTransport;
import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.security.EntrySafetyGuard;
import com.innerdimension.state.InnerState;
import com.mojang.serialization.Lifecycle;
import net.minecraft.class_1937;
import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2370;
import net.minecraft.class_2378;
import net.minecraft.class_2680;
import net.minecraft.class_27;
import net.minecraft.class_2791;
import net.minecraft.class_2806;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_5219;
import net.minecraft.class_5268;
import net.minecraft.class_5321;
import net.minecraft.class_5363;
import net.minecraft.class_7924;
import net.minecraft.class_9248;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Creates and tracks per-owner "inner" dimensions at runtime.
 *
 * <p>Dimension id rule: {@code inner_dimension:inner_<owner uuid without dashes, lowercase>}.
 * The dimension options are copied from the datapack template
 * {@code inner_dimension:inner_template}, registered into the live dimension registry
 * after a temporary unfreeze, and a {@link class_3218} is constructed and inserted into
 * the server's worlds map exactly like vanilla does in
 * {@code MinecraftServer.createLevels} (see MinecraftServer.java L444).</p>
 */
public class DynamicDimensionManager {
    public static final String NAMESPACE = "inner_dimension";
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");
    private static final String PREFIX = "inner_";
    private static final class_2960 TEMPLATE_ID = class_2960.method_60655(NAMESPACE, "inner_template");

    /**
     * True while the server is inside a tick, and therefore inside (or nested in) the
     * {@code MinecraftServer.method_3813} loop over the world map.
     *
     * <p>Creating an inner room inserts a world into that very map ({@code field_4589}), which a
     * LinkedHashMap iterator turns into a {@link java.util.ConcurrentModificationException} when
     * the loop advances -- the server crashes with "Exception in server tick loop". Anything that
     * could create a room while this is set is therefore queued instead and run from
     * {@link #runDeferredTasks()} at END_SERVER_TICK, which Fabric fires at the tail of
     * {@code MinecraftServer.method_3748}, i.e. after the world loop has finished.</p>
     */
    private static boolean inServerTick;

    /** Work that must not touch the server's world map from inside a tick. */
    private static final Queue<Runnable> DEFERRED_TASKS = new ConcurrentLinkedQueue<>();

    /** Two seconds is far longer than a complete vanilla piston extension/retraction stroke. */
    private static final int FEED_CHUNK_GRACE_TICKS = 40;

    /**
     * Landing point used by player and mob teleports. The requested feet position is centered
     * half a block above y=0.
     */
    public static final int LANDING_X = 8;
    public static final double LANDING_Y = 0.5D;
    public static final int LANDING_Z = 8;

    private DynamicDimensionManager() {
    }

    /** Marks the start of a server tick. Registered on START_SERVER_TICK. */
    public static void beginServerTick() {
        inServerTick = true;
    }

    /**
     * Runs queued room-creating work. Called first thing in {@link #tickBoundary}, before that
     * method iterates the world map itself.
     */
    public static void runDeferredTasks() {
        inServerTick = false;
        Runnable task;
        while ((task = DEFERRED_TASKS.poll()) != null) {
            try {
                task.run();
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Deferred world task failed", throwable);
            }
        }
    }

    public static String dimensionName(UUID owner) {
        return PREFIX + owner.toString().replace("-", "").toLowerCase(Locale.ROOT);
    }

    public static class_5321<class_1937> worldKey(UUID owner) {
        return class_5321.method_29179(class_7924.field_41223,
                class_2960.method_60655(NAMESPACE, dimensionName(owner)));
    }

    private static class_5321<class_5363> dimensionOptionsKey(UUID owner) {
        return class_5321.method_29179(class_7924.field_41224,
                class_2960.method_60655(NAMESPACE, dimensionName(owner)));
    }

    public static boolean isInnerWorld(class_5321<class_1937> key) {
        if (key == null) {
            return false;
        }
        class_2960 id = key.method_29177();
        return NAMESPACE.equals(id.method_12836()) && id.method_12832().startsWith(PREFIX);
    }

    public static UUID ownerOfWorld(class_5321<class_1937> key) {
        if (key == null) {
            return null;
        }
        class_2960 id = key.method_29177();
        if (!NAMESPACE.equals(id.method_12836())) {
            return null;
        }
        String path = id.method_12832();
        if (!path.startsWith(PREFIX)) {
            return null;
        }
        String hex = path.substring(PREFIX.length());
        if (hex.length() != 32) {
            return null;
        }
        try {
            String dashed = hex.substring(0, 8) + '-'
                    + hex.substring(8, 12) + '-'
                    + hex.substring(12, 16) + '-'
                    + hex.substring(16, 20) + '-'
                    + hex.substring(20, 32);
            return UUID.fromString(dashed);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the existing inner world of the owner or creates it. When this method
     * returns the chunk at chunk coords (0, 0) is fully generated and a 3x3 safety
     * platform under the landing point (8, 2, 8) is in place, so the player can be
     * teleported immediately.
     */
    public static class_3218 getOrCreate(MinecraftServer server, UUID owner) {
        class_5321<class_1937> key = worldKey(owner);
        Map<class_5321<class_1937>, class_3218> worlds =
                ((MinecraftServerAccessor) server).inner$getWorlds();
        class_3218 existing = worlds.get(key);
        if (existing != null) {
            RoomShellGenerator.generateIfNeeded(existing, owner);
            return existing;
        }

        // --- 1. obtain the live dimension registry ---
        // MinecraftServer.method_46221() is the public getter for private field_25132
        // (class_7780<class_7659>), verified by bytecode.
        class_2378<class_5363> dimensionRegistry =
                server.method_46221().method_45926().method_30530(class_7924.field_41224);
        @SuppressWarnings("unchecked")
        class_2370<class_5363> mappedRegistry =
                (class_2370<class_5363>) (class_2370<?>) dimensionRegistry;

        // --- 2. copy dimension options from the template ---
        class_5321<class_5363> templateKey =
                class_5321.method_29179(class_7924.field_41224, TEMPLATE_ID);
        class_5363 template = dimensionRegistry.method_31189(templateKey)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing inner dimension template: " + NAMESPACE + ":inner_template"));
        class_5363 options = new class_5363(template.comp_1012(), template.comp_1013());

        // --- 3. temporarily unfreeze, register, refreeze ---
        class_5321<class_5363> optionsKey = dimensionOptionsKey(owner);
        MappedRegistryAccessor registryAccessor = (MappedRegistryAccessor) mappedRegistry;
        boolean wasFrozen = registryAccessor.inner$isFrozen();
        registryAccessor.inner$setFrozen(false);
        try {
            mappedRegistry.method_10272(optionsKey, options,
                    new class_9248(Optional.empty(), Lifecycle.stable()));
        } finally {
            registryAccessor.inner$setFrozen(wasFrozen);
        }

        // --- 4. construct the ServerWorld exactly like vanilla (MinecraftServer L444) ---
        MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor) server;
        class_3218 overworld = server.method_30002();
        class_5219 saveProperties = serverAccessor.inner$getSaveProperties();
        class_5268 overworldLevelData =
                ((ServerWorldAccessor) overworld).inner$getLevelData();
        class_27 derivedLevelData = new class_27(saveProperties, overworldLevelData);

        class_3218 innerWorld = new class_3218(
                server,
                serverAccessor.inner$getExecutor(),
                serverAccessor.inner$getSession(),
                derivedLevelData,
                key,
                options,
                false,
                0L,
                ImmutableList.of(),
                false,
                overworld.method_52168());
        worlds.put(key, innerWorld);

        // The old keepLoaded policy owned all nine shell tickets. Retire the eight
        // peripheral tickets; both usable interiors are in chunk (0, 0).
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                if (cx != 0 || cz != 0) innerWorld.method_17988(cx, cz, false);
            }
        }
        innerWorld.method_17988(0, 0, InnerConfig.get().keepLoaded);
        boolean shellNeedsBuild = InnerState.get(overworld).getLayout(owner) != RoomShellGenerator.LAYOUT_VERSION;
        if (shellNeedsBuild) {
            // --- 5/6. prepare all shell chunks only for construction/migration ---
            // The room is 18 blocks wide and centred on the origin, so it straddles chunk
            // boundaries. Writing blocks into a chunk that is not loaded is silently dropped,
            // which used to leave holes in the shell.
            int edge = RoomGeometry.INTERIOR_SIZE;
            int minChunk = Math.floorDiv(-1, 16);
            int maxChunk = Math.floorDiv(edge, 16);
            for (int chunkX = minChunk; chunkX <= maxChunk; chunkX++) {
                for (int chunkZ = minChunk; chunkZ <= maxChunk; chunkZ++) {
                    TemporaryChunkLoads.holdChunk(innerWorld, chunkX, chunkZ);
                    class_2791 chunk = innerWorld.method_14178()
                            .method_12121(chunkX, chunkZ, class_2806.field_12803, true);
                    if (chunk == null) {
                        throw new IllegalStateException("Synchronous chunk generation failed for "
                                + key.method_29177() + " chunk " + chunkX + "," + chunkZ);
                    }
                }
            }

            // --- 7. guarantee a safe landing regardless of template worldgen ---
            if (!InnerState.get(overworld).isShellGenerated(owner)) placeLandingSafety(innerWorld);

            // --- 8. build the room shell once ---
            RoomShellGenerator.generateIfNeeded(innerWorld, owner);
        }

        return innerWorld;
    }

    /**
     * Places a 3x3 pearl_face platform at y=-1 centered on the landing point and clears
     * y=0..2 above it. This is idempotent and fully consistent with the shell that
     * {@link RoomShellGenerator} builds later (it overwrites y=-1 with the same blocks).
     */
    private static void placeLandingSafety(class_3218 innerWorld) {
        class_2680 pearlState = ModBlocks.PEARL_FACE.method_9564();
        // block state id 0 is always minecraft:air
        class_2680 airState = class_2248.method_9531(0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = LANDING_X + dx;
                int z = LANDING_Z + dz;
                // flag 3 = notify neighbours; the same value RoomShellGenerator uses.
                innerWorld.method_8652(new class_2338(x, -1, z), pearlState, 3);
                for (int y = 0; y <= 3; y++) {
                    innerWorld.method_8652(new class_2338(x, y, z), airState, 3);
                }
            }
        }
    }

    /**
     * Called after the server has started. Rebuilds the inner dimension/world for every
     * room owner recorded in {@link InnerState}. Old peripheral force-load tickets are retired by
     * getOrCreate; the worlds map entry (the live ServerWorld) is recreated here.
     * Idempotent.
     *
     * <p>Order matters: the pure runtime caches are reset first, then the rooms are restored, and the
     * entity tracking that happens while the worlds load is therefore not thrown away afterwards.</p>
     */
    public static void onServerStarted(MinecraftServer server) {
        ServerHolder.set(server);
        // Static state survives an integrated-server restart in the same client process.
        ServerClock.reset();
        TemporaryChunkLoads.reset();
        ExitRouter.reset();
        DollSecurityManager.reset();
        EntrySafetyGuard.reset();
        DollBindingManager.reset();
        PearlSurfaceTransport.reset();
        com.innerdimension.item.EnderPearlPlugItem.clearWearTicks();
        FEED_QUEUE.clear();
        InnerState state = InnerState.get(server.method_30002());
        for (UUID owner : new ArrayList<>(state.roomOwners())) {
            try {
                getOrCreate(server, owner);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to restore the inner dimension of {}", owner, throwable);
            }
        }
        DollSecurityManager.diagnoseMissingBelts();
    }

    /**
     * Called while the server is stopping.
     *
     * <p>Only runtime references are dropped. The temporary tickets are not removed (they are the
     * shared vanilla type and cannot outlive the server anyway), and persistent records - bindings,
     * locks, entry sources, pending requests and player return points - are deliberately left to be
     * saved as they are.</p>
     */
    public static void onServerStopping() {
        TemporaryChunkLoads.forgetAll();
        FEED_QUEUE.clear();
        ExitRouter.reset();
        ServerHolder.clear();
        DollBindingManager.reset();
        DollSecurityManager.reset();
        EntrySafetyGuard.reset();
        PearlSurfaceTransport.reset();
        com.innerdimension.item.EnderPearlPlugItem.clearWearTicks();
    }

    /**
     * The fixed end-of-tick order of implementation plan section 0.17.
     *
     * <p>Bindings are revalidated first, so an output that is about to run already knows whether its
     * port is still valid; then the queued input requests (whose world creation must not happen while
     * the server iterates its world map); then the waiting output requests; then the boundary check;
     * and finally the probe bookkeeping.</p>
     */
    public static void tickEnd(MinecraftServer server) {
        if (server == null) {
            return;
        }
        // Room creation queued from inside this tick's world loop adds worlds to the map the loops
        // below iterate, so it happens before anything else.
        runDeferredTasks();
        DollBindingManager.tick();
        runFeedQueue();
        ExitRouter.processPending(server);
        tickBoundary(server);
        PearlSurfaceTransport.tick();
        ExitRouter.tick();
    }

    /**
     * Sneak + right-click a dildo: remember where the player came from and send them to
     * their own inner room, creating the dimension / shell on first use.
     *
     * @return {@code true} when the player was actually moved.
     */
    public static boolean enter(class_3222 player) {
        return enter(player, player);
    }

    /** Repositions a player who is already inside an inner room to its standard landing point. */
    public static boolean returnToLanding(class_3222 player) {
        if (player == null || !isInnerWorld(player.method_51469().method_27983())) {
            return false;
        }
        if(!com.innerdimension.transport.SafeEntityTeleport.prepare(player))return false;
        var landing=new com.innerdimension.projection.ProjectionSafetySearch.Point(LANDING_X,LANDING_Y,LANDING_Z);
        if(com.innerdimension.voiddomain.InfectionPillars.present(player.method_51469())){
            landing=InnerRoomLanding.find(player.method_51469(),player,landing,false);if(landing==null)return false;
        }
        return com.innerdimension.transport.SafeEntityTeleport.move(player, player.method_51469(),
                landing.x(),landing.y(),landing.z(),
                Set.of(), player.method_36454(), player.method_36455(), false);
    }

    /**
     * Sneak + right-click a dildo. {@code anchor} is the entity the player interacted with;
     * its position becomes the spot the player (and anything dropped inside the room)
     * returns to, so the player comes back to the dildo rather than to wherever they stood.
     */
    public static boolean enter(class_3222 player, class_1297 anchor) {
        return player != null && enterAs(player, player.method_5667(), anchor);
    }

    /**
     * Like {@link #enter(class_3222)} but sends the player into the dimension owned by
     * {@code owner} instead of their own. Used by {@code /innerdim enter <player>}.
     */
    public static boolean enterAs(class_3222 player, UUID owner) {
        return enterAs(player, owner, player);
    }

    public static boolean enterAs(class_3222 player, UUID owner, class_1297 anchor) {
        if (inServerTick) {
            // Called from inside the world tick (an entity tick, say): creating the room would add a
            // world to the map MinecraftServer is iterating right now. Run it at the end of the tick.
            DEFERRED_TASKS.add(() -> enterAs(player, owner, anchor));
            return true;
        }
        try {
            return enterAs0(player, owner, anchor);
        } catch (Throwable throwable) {
            LOGGER.error("[InnerDimension] Failed to enter the inner dimension", throwable);
            return false;
        }
    }

    /**
     * The actual entry of a player into a room.
     *
     * <p>Implementation plan section 0.8: the snapshot is taken from the player's own world and
     * position before anything is created, an already-present player is never moved again and never
     * has their record overwritten, and a player standing in another inner dimension is refused
     * rather than force-nested. The record is only committed on a successful teleport, so a failed
     * entry leaves the previous return point alone.</p>
     *
     * <p>A safety gate runs before the player moves: an entry through a device that has meanwhile
     * been locked or refused does not happen at all.</p>
     */
    private static boolean enterAs0(class_3222 player, UUID owner, class_1297 anchor) {
        return enterAs0(player, owner, anchor, LANDING_Y);
    }

    public static boolean enterRitual(class_3222 player, double landingY) {
        if (inServerTick) throw new IllegalStateException("Ritual must run after the world tick");
        return enterAs0(player, player.method_5667(), player, landingY);
    }

    private static boolean enterAs0(class_3222 player, UUID owner, class_1297 anchor, double landingY) {
        if (player == null || owner == null) {
            return false;
        }
        class_3218 origin = player.method_51469();
        if (isInnerWorld(origin.method_27983())) {
            // Nested entry is not defined in this round; the player keeps their current record.
            LOGGER.warn("[InnerDimension] Refusing to enter an inner dimension from inside another one ({})",
                    player.method_5667());
            return false;
        }
        MinecraftServer server = origin.method_8503();
        if (server == null) {
            return false;
        }
        if (!EntrySafetyGuard.allowPlayerEntry(player, owner)) {
            return false;
        }
        // The gate runs on the device that is being used to enter, if there is one.
        if (anchor instanceof EnderPearlDildoEntity dildo) {
            DollSecurityManager.Gate gate = DollSecurityManager.forInteraction(dildo, player.method_5667());
            if (gate.blocked()) {
                DollSecurityManager.sendFeedback(player, gate);
                return false;
            }
        }
        class_3218 overworld = server.method_30002();
        InnerState state = InnerState.get(overworld);

        if (!com.innerdimension.transport.SafeEntityTeleport.prepare(player)) return false;
        // --- snapshot, after animation restoration and before anything moves ---
        String sourceDim = origin.method_27983().method_29177().toString();
        double sourceX = player.method_23317();
        double sourceY = player.method_23318();
        double sourceZ = player.method_23321();
        float sourceYaw = player.method_36454();
        float sourcePitch = player.method_36455();

        state.addRoomOwner(owner);
        class_3218 inner = getOrCreate(server, owner);
        RoomShellGenerator.ensureShell(inner, owner);

        // Already standing in the target room: nothing to move, and the record is not touched.
        if (player.method_51469() == inner) {
            return true;
        }

        // The tether variant drags whatever it holds along: led mobs and its container. Every
        // object it moves is tagged with this dildo as its entry, so it can find its way back out.
        if (anchor instanceof EnderPearlDildoEntity dildo) {
            dildo.moveContentsInto(origin, inner, owner, true);
        }

                if(anchor instanceof EnderPearlDildoEntity)com.innerdimension.sound.ModSounds.personal(player,"lubricated_entry",.8f);
        boolean firstDomainEntry=landingY==32.0&&!com.innerdimension.progression.DomainState.get(inner).introduced.contains(owner);
        var landing=new com.innerdimension.projection.ProjectionSafetySearch.Point(firstDomainEntry?.5:LANDING_X,firstDomainEntry?32.5:landingY,firstDomainEntry?.5:LANDING_Z);
        if(com.innerdimension.voiddomain.InfectionPillars.present(inner)){
            landing=InnerRoomLanding.find(inner,player,landing,landingY==32.0);if(landing==null)return false;
        }
        boolean moved = com.innerdimension.transport.SafeEntityTeleport.move(player, inner,
                landing.x(),landing.y(),landing.z(),
                Set.of(), sourceYaw, sourcePitch, false);
        if (!moved) {
            LOGGER.warn("[InnerDimension] The teleport of {} into their room was refused", player.method_5667());
            return false;
        }
        // Commit only after the teleport really happened.
        state.clearEntrySource(player.method_5667());
        if (owner.equals(player.method_5667()) && landingY == LANDING_Y)
            com.innerdimension.progression.InnerAdvancements.grant(player, "deepest");
        state.setPlayerReturn(player.method_5667(), origin.method_27983().method_29177(),
                sourceX, sourceY, sourceZ, sourceYaw, sourcePitch);
        LOGGER.debug("[InnerDimension] {} entered {} from {} at ({}, {}, {})",
                player.method_5667(), owner, sourceDim, sourceX, sourceY, sourceZ);
        return true;
    }

    /**
     * Sends a player inside an inner room back out. Does nothing (returns {@code false}) when the
     * player is not inside an inner room or has no stored return point.
     *
     * <p>Kept as the single entry point used by the command, the floor block and the boundary
     * check; the routing itself lives in {@link ExitRouter}, together with the rule that only this
     * player's own return record may be read.</p>
     */
    public static boolean leave(class_3222 player) {
        return ExitRouter.leavePlayer(player);
    }

    /** Side length of the hollow interior; the room itself is {@code interior + 2}. */
    private static int interiorSize() {
        return RoomGeometry.INTERIOR_SIZE;
    }

    /** Players, items and everything else are ejected once they leave the interior box. */
    private static boolean outsideRoom(double x, double y, double z) {
        int inner = interiorSize();
        // A rider's position can sit below the hull/floor. Only a real fall two blocks
        // below the interior counts as leaving; walls and ceiling also allow small overlap.
        double eps = 0.5D;
        return x < -eps || x >= inner + eps
                || z < -eps || z >= inner + eps
                || y < -2.0D || y >= RoomGeometry.DOMAIN_CEILING + 1.0D;
    }

    /** Called every server tick: anything that escapes the room box is sent back out. */
    public static void tickBoundary(MinecraftServer server) {
        if (server == null) {
            return;
        }
        // First: room creation queued from inside this tick's world loop. It adds worlds to the
        // map this method is about to iterate, so it has to happen before the loops below start.
        runDeferredTasks();
        for (class_3222 player : server.method_3760().method_14571()) {
            class_3218 world = player.method_51469();
            if (world == null || !isInnerWorld(world.method_27983())) {
                continue;
            }
            if(com.innerdimension.voiddomain.VoidRitual.running(player))continue;
            dropThroughAbyssPassageFloor(player, world);
            if (!outsideRoom(player.method_23317(), player.method_23318(), player.method_23321())) {
                continue;
            }
            try {
                leave(player);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to eject {} from their inner dimension",
                        player.method_5667(), throwable);
            }
        }

        for (class_3218 world : server.method_3738()) {
            if (!isInnerWorld(world.method_27983())) {
                continue;
            }
            try {
                ejectLooseEntities(server, world);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to eject loose entities from {}",
                        world.method_27983(), throwable);
            }
        }
    }

    /** Only the passage floor opens downward; domain floors and abyss walls stay solid. */
    private static void dropThroughAbyssPassageFloor(class_3222 player, class_3218 world) {
        double x = player.method_23317(), y = player.method_23318(), z = player.method_23321();
        if (!player.method_24828() || player.method_5854() != null
                || x < 0 || x >= RoomGeometry.INTERIOR_SIZE
                || z < 0 || z >= RoomGeometry.INTERIOR_SIZE
                || Math.abs(y - (RoomGeometry.INNER_CEILING + 1.0)) > 0.05) return;
        class_2338 floor = new class_2338((int)Math.floor(x), RoomGeometry.INNER_CEILING,
                (int)Math.floor(z));
        if (world.method_8320(floor).method_26204() != ModBlocks.ABYSS_BLOCK) return;
        // Clear the entire body below the one-block floor so collision cannot push it back up.
        double height = player.method_5829().field_1325 - player.method_5829().field_1322;
        var landing=new com.innerdimension.projection.ProjectionSafetySearch.Point(x,RoomGeometry.INNER_CEILING-height-.05,z);
        if(com.innerdimension.voiddomain.InfectionPillars.present(world)){
            if(!com.innerdimension.transport.SafeEntityTeleport.prepare(player))return;
            landing=InnerRoomLanding.find(world,player,landing,false);if(landing==null)return;
        }
        if (com.innerdimension.transport.SafeEntityTeleport.move(player, world, landing.x(),landing.y(),landing.z(), Set.of(),
                player.method_36454(), player.method_36455(), false)) {
            com.innerdimension.sound.ModSounds.personal(player,"abyss_passage",.8f);
            player.method_18799(new net.minecraft.class_243(0, -0.08, 0));
            player.field_6017 = 0;
        }
    }

    /**
     * Keeps the feed mechanism ticking after its only nearby player is teleported away.
     *
     * <p>A 3x3 area is used because the doll and the piston base can straddle a chunk edge, and the
     * loads are short-lived tickets rather than {@code setChunkForced}: they expire on their own
     * after {@link TemporaryChunkLoads#TICKET_TICKS} ticks, are never written to the save, and
     * cannot collide with an administrator's {@code /forceload}. Asking again - a second input -
     * simply resets that budget, which is the "repeated input extends the same batch" behaviour.</p>
     */
    private static void keepFeedMechanismLoaded(class_3218 world, class_2338 cell) {
        int centerX = Math.floorDiv(cell.method_10263(), 16);
        int centerZ = Math.floorDiv(cell.method_10260(), 16);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                TemporaryChunkLoads.holdChunk(world, centerX + dx, centerZ + dz);
            }
        }
    }

    /**
     * Anything that is not a player and left the room box is sent out through the one exit rule
     * set: the dimension's strong port when it has one, otherwise the object's own entry dildo.
     *
     * <p>The previous behaviour borrowed the first present player's return point, which meant an
     * item could end up at a stranger's doorstep, and a weak-only dimension still produced output.
     * Objects that cannot leave yet are simply left where they are and retried - they are never
     * deleted, and the exit resolver carries their own source record.</p>
     */
    private static void ejectLooseEntities(MinecraftServer server, class_3218 world) {
        for (class_1297 entity : world.method_27909()) {
            if (entity instanceof class_3222
                    || entity instanceof com.innerdimension.entity.SensoryLinkDollEntity
                    || entity instanceof EnderPearlDildoEntity) {
                continue; // players are handled separately, through their own return record
            }
            if (!outsideRoom(entity.method_23317(), entity.method_23318(), entity.method_23321())) {
                continue;
            }
            try {
                ExitRouter.sendEntity(world, entity);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to send {} out of {}",
                        entity.method_5667(), world.method_27983(), throwable);
            }
        }
    }

    /** Clears the generated-shell marker of the player's own room so it is rebuilt. */
    public static void resetOwnRoom(class_3222 player) {
        MinecraftServer server = player.method_51469().method_8503();
        if (server == null) {
            return;
        }
        InnerState.get(server.method_30002()).resetLayout(player.method_5667());
    }

    /**
     * Destroys the room, evicts every occupant, and wipes all data and disk files.
     * Called by bridge integration when an AI companion is permanently deleted.
     */
    public static void deleteRoom(MinecraftServer server, UUID owner) {
        if (server == null || owner == null) {
            return;
        }
        class_5321<class_1937> key = worldKey(owner);
        class_3218 inner = ((MinecraftServerAccessor) server).inner$getWorlds().get(key);
        if (inner != null) {
            ArrayList<class_3222> occupants = new ArrayList<>(inner.method_18456());
            for (class_3222 p : occupants) ExitRouter.leavePlayer(p);

            class_3218 overworld = server.method_30002();
            int spawnY = overworld.method_8624(class_2902.class_2903.field_13195, 0, 0);
            for (class_3222 p : occupants) {
                if (p.method_51469() != inner) continue;
                boolean forced = com.innerdimension.transport.SafeEntityTeleport.forceRoomEvacuation(
                        p, overworld, 0.5D, spawnY, 0.5D, p.method_36454(), p.method_36455());
                if (!forced || p.method_51469() == inner) {
                    LOGGER.error("[InnerDimension] Refusing to delete {}: player {} could not be evacuated",
                            owner, p.method_5667());
                    return;
                }
            }
            // Never trust only the teleport return value: the live world must actually be empty.
            if (!inner.method_18456().isEmpty()) {
                LOGGER.error("[InnerDimension] Refusing to delete {}: {} player(s) remain in the world",
                        owner, inner.method_18456().size());
                return;
            }
            try {
                inner.close();
            } catch (Exception e) {
                LOGGER.warn("[InnerDimension] Error closing inner world of {}: {}", owner, e.toString());
            }
            ((MinecraftServerAccessor) server).inner$getWorlds().remove(key);
        }
        InnerState state = InnerState.get(server.method_30002());
        state.removeRoomData(owner);
        net.minecraft.class_32.class_5143 session = ((MinecraftServerAccessor) server).inner$getSession();
        if (session != null) {
            try {
                java.nio.file.Path dimDir = session.method_27424(key);
                if (dimDir != null) {
                    java.nio.file.Files.walk(dimDir)
                            .sorted(java.util.Comparator.reverseOrder())
                            .forEach(path -> {
                                try { java.nio.file.Files.deleteIfExists(path); } catch (Exception ignored) {}
                            });
                    LOGGER.info("[InnerDimension] Deleted room directory for {}", owner);
                }
            } catch (java.nio.file.NoSuchFileException ignored) {
                // directory was never created — nothing to delete
            } catch (Exception e) {
                LOGGER.warn("[InnerDimension] Failed to delete room directory for {}: {}", owner, e.toString());
            }
        }
    }

    /** Why a stick and its doll ended up in the same cell. */
    public enum FeedCause {
        /** The doll moved onto the stick: contents go in and the owner follows. */
        DOLL_MOVED,
        /** The stick was pushed onto the doll: only contents move, the owner does not follow. */
        DILDO_MOVED,
        /** They were already together (a reload): nothing is triggered. */
        NO_MOVEMENT
    }

    /**
     * A queued input request.
     *
     * <p>It stores identifiers rather than a live entity reference: by the time the end of the tick
     * runs, the stick may have been unloaded or removed, and a captured object would then be used
     * after its removal. The binding revision is stored so a request that was overtaken by a lock or
     * a re-binding is discarded instead of replaying.</p>
     */
    public record FeedRequest(UUID dildoUuid, UUID owner, class_2960 worldId, FeedCause cause,
                              int bindingRevision) {
    }

    private static final Queue<FeedRequest> FEED_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * Queues an input event for the end of this tick.
     *
     * <p>Security spec section 7: the queue gate is checked here as well as at execution, so a locked
     * or mismatched device never even enters the queue.</p>
     */
    public static void queueFeed(EnderPearlDildoEntity dildo, UUID owner, FeedCause cause) {
        if (dildo == null || owner == null || cause == FeedCause.NO_MOVEMENT) {
            return;
        }
        if (!(dildo.method_73183() instanceof class_3218 origin) || origin.method_8503() == null) {
            return;
        }
        DollSecurityManager.Gate gate = DollSecurityManager.forPort(dildo, owner);
        if (gate.blocked()) {
            LOGGER.debug("[InnerDimension] Not queueing an input for stick {}: {}",
                    dildo.method_5667(), gate.state());
            return;
        }
        InnerState state = InnerState.get(origin.method_8503().method_30002());
        FEED_QUEUE.add(new FeedRequest(dildo.method_5667(), owner,
                origin.method_27983().method_29177(), cause,
                state.bindingRevision(owner)));
    }

    /** Runs the queued input requests at the end of the tick, after the world loop has finished. */
    private static void runFeedQueue() {
        FeedRequest request;
        while ((request = FEED_QUEUE.poll()) != null) {
            try {
                executeFeed(request);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Input request for {} failed", request.dildoUuid(), throwable);
            }
        }
    }

    /**
     * Executes one input request after re-verifying the actual objects.
     *
     * <p>The stick, its connection, its authorization and its lock are all re-read here, so a request
     * that was queued before a lock or a re-binding is dropped rather than applied to a changed
     * world.</p>
     */
    private static void executeFeed(FeedRequest request) {
        MinecraftServer server = ServerHolder.server();
        if (server == null) {
            return;
        }
        class_5321<class_1937> key = class_5321.method_29179(class_7924.field_41223, request.worldId());
        class_3218 origin = server.method_3847(key);
        if (origin == null) {
            return;
        }
        class_1297 found = origin.method_73284(request.dildoUuid());
        if (!(found instanceof EnderPearlDildoEntity dildo) || dildo.method_31481()) {
            // The device is gone: the request dies with it rather than being repaired into something.
            return;
        }
        InnerState state = InnerState.get(server.method_30002());
        if (state.bindingRevision(request.owner()) != request.bindingRevision()
                && state.bindingOfDildo(request.dildoUuid()) == null) {
            LOGGER.debug("[InnerDimension] Dropping a stale input request for stick {}",
                    request.dildoUuid());
            return;
        }
        DollSecurityManager.Gate gate = DollSecurityManager.forPort(dildo, request.owner());
        if (gate.blocked()) {
            LOGGER.debug("[InnerDimension] Input request for stick {} was refused at execution: {}",
                    request.dildoUuid(), gate.state());
            return;
        }
        boolean teleportPlayer = request.cause() == FeedCause.DOLL_MOVED;
        class_3222 player = teleportPlayer
                ? server.method_3760().method_14602(request.owner()) : null;
        if (teleportPlayer && player == null) {
            LOGGER.warn("[InnerDimension] {}'s sensory link doll was fed into a stick, but they are offline",
                    request.owner());
            return;
        }
        if (teleportPlayer && (!EntrySafetyGuard.allowPlayerEntry(player, request.owner())
                || !EntrySafetyGuard.allowDildoPlayerFeed(request.dildoUuid(), player))) {
            LOGGER.debug("[InnerDimension] Input request for stick {} was stopped by the entry loop guard",
                    request.dildoUuid());
            return;
        }
        if (teleportPlayer) {
            // Once the owner leaves, this may be the last loaded chunk around the machine.
            keepFeedMechanismLoaded(origin, dildo.method_24515());
        }
        state.addRoomOwner(request.owner());
        class_3218 room = getOrCreate(server, request.owner());
        RoomShellGenerator.ensureShell(room, request.owner());
        
        com.innerdimension.sound.ModSounds.at(origin,dildo.method_24515(),"lubricated_entry",.8f);
        dildo.moveContentsInto(origin, room, request.owner(), teleportPlayer);

        if (!teleportPlayer) {
            return;
        }
        // Already standing in that very room: the contents are in, nothing to move. Teleporting here
        // would drag the player off wherever they were back to the landing pad (and show another
        // loading screen) every time a doll was pushed around while they were inside.
        if (player.method_51469() == room) {
            return;
        }
        if (!com.innerdimension.transport.SafeEntityTeleport.prepare(player)) return;
        class_3218 playerOrigin = player.method_51469();
        // The snapshot is taken from the player's own world and position, never from the device.
        double returnX = player.method_23317(), returnY = player.method_23318(), returnZ = player.method_23321();
        float returnYaw = player.method_36454(), returnPitch = player.method_36455();
        if (isInnerWorld(playerOrigin.method_27983())) {
            LOGGER.warn("[InnerDimension] Refusing to store a return point inside an inner room for {}",
                    player.method_5667());
        }
        var landing=new com.innerdimension.projection.ProjectionSafetySearch.Point(LANDING_X,LANDING_Y,LANDING_Z);
        if(com.innerdimension.voiddomain.InfectionPillars.present(room)){
            landing=InnerRoomLanding.find(room,player,landing,false);if(landing==null)return;
        }
        if (com.innerdimension.transport.SafeEntityTeleport.move(player, room,
                landing.x(),landing.y(),landing.z(), Set.of(), returnYaw, returnPitch, false)) {
            com.innerdimension.progression.InnerAdvancements.grant(player, "deepest");
            state.clearEntrySource(player.method_5667());
            if (!isInnerWorld(playerOrigin.method_27983())) {
                state.setPlayerReturn(player.method_5667(), playerOrigin.method_27983().method_29177(),
                        returnX, returnY, returnZ, returnYaw, returnPitch);
            }
        }
    }
}
