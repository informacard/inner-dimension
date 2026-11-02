package com.innerdimension.binding;

import com.innerdimension.chunk.ChunkProbes;
import com.innerdimension.core.ServerClock;
import com.innerdimension.core.Tuning;
import com.innerdimension.entity.SensoryLinkDollEntity;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.security.SecurityState;
import com.innerdimension.state.InnerState;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Lifecycle;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_7225;
import net.minecraft.class_9296;
import nonplayerplushie.SensoryLinkData;
import nonplayerplushie.SensoryLinkDollBlock;
import nonplayerplushie.SensoryLinkDollBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the life cycle of dildo-to-doll connections.
 *
 * <p>A connection is established by looking at the single block cell <em>above</em> a dildo, and a
 * doll only counts when its own feet cell is that cell - never merely because an expanded collision
 * box overlaps it. The dimension owner is the identity the doll itself carries, which the placement
 * path took from non_player_plushie's sensory link data, so the doll entity's UUID, its display name
 * and the acting player's UUID can never decide ownership.</p>
 *
 * <p>Detection is the entry point for establishing or restoring a connection and is deliberately
 * <em>not</em> an unbinding check: a doll moving down into the dildo's own cell, or being retracted
 * by a piston, leaves the cell above on its own, and that must not be read as "the user removed the
 * doll". Cleaning a connection up requires the central absence sampling of
 * {@link #observeAbsent}.</p>
 *
 * <p>Every path here is gated by {@link DollSecurityManager}: a locked, mismatched, pending or
 * unclaimed port is left exactly as it is. Refusal is a state, not a missing connection, so it can
 * never degrade into "unbound" and open the ordinary fallbacks.</p>
 */
public final class DollBindingManager {
    public static final String BLOCK_PLACER_KEY = "inner_dimension_placer_uuid";
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");

    /** Dildos that still need a sweep, with the tick of their last sweep. */
    private static final Map<EnderPearlDildoEntity, Long> TRACKED = new ConcurrentHashMap<>();

    /** One absence observation record per connection, shared by every caller. */
    private static final class AbsenceRecord {
        private class_2338 sampledChunk;
        private Long readySince;
        private Long lastSample;
        private int misses;

        private void reset() {
            sampledChunk = null;
            readySince = null;
            lastSample = null;
            misses = 0;
        }
    }

    private static final Map<UUID, AbsenceRecord> ABSENCE = new ConcurrentHashMap<>();

    /** Owners whose legacy migration has not picked its default strong port yet. */
    private static final Map<UUID, Long> MIGRATIONS = new ConcurrentHashMap<>();

    private DollBindingManager() {
    }

    /** The answer of the absence sampling. */
    public enum AbsenceObservation {
        /** Not enough evidence yet: keep the connection and look again later. */
        WAIT_ENTITY,
        /** The doll is provably gone: the caller may clean the connection up. */
        CONFIRMED_ABSENT
    }

    // ------------------------------------------------------------- registry

    /** Registers a dildo for periodic detection. Idempotent. */
    public static void track(EnderPearlDildoEntity dildo) {
        if (dildo == null || dildo.method_73183() == null || dildo.method_73183().method_8608()) {
            return;
        }
        TRACKED.putIfAbsent(dildo, ServerClock.now());
    }

    /** Drops a dildo from the registry and removes the connection it held. */
    public static void untrack(EnderPearlDildoEntity dildo) {
        if (dildo == null) {
            return;
        }
        TRACKED.remove(dildo);
        clearConnectionsOfDildo(dildo);
    }

    /**
     * Restores the pure runtime caches when a server starts.
     *
     * <p>Called before the worlds and entities are restored, so nothing that was just tracked can be
     * thrown away again: the tracking happens while the entities load, which is after this.</p>
     */
    public static void reset() {
        TRACKED.clear();
        ABSENCE.clear();
        MIGRATIONS.clear();
    }

    /** Asks for an immediate re-detection of this dildo, used right after a piston moved it. */
    public static void markMoved(EnderPearlDildoEntity dildo) {
        if (dildo != null && TRACKED.containsKey(dildo)) {
            TRACKED.put(dildo, ServerClock.now() - Tuning.BINDING_RESCAN_TICKS);
        }
    }

    /** Ticks the sweeps and the migration windows. */
    public static void tick() {
        long now = ServerClock.now();
        Iterator<Map.Entry<EnderPearlDildoEntity, Long>> iterator = TRACKED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<EnderPearlDildoEntity, Long> entry = iterator.next();
            EnderPearlDildoEntity dildo = entry.getKey();
            class_1937 world = dildo.method_73183();
            if (world == null || dildo.method_31481()) {
                iterator.remove();
                continue;
            }
            if (!(world instanceof class_3218 serverWorld)) {
                continue;
            }
            if (now - entry.getValue() < Tuning.BINDING_RESCAN_TICKS) {
                continue;
            }
            entry.setValue(now);
            try {
                sweep(serverWorld, dildo);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Binding sweep failed for dildo {}",
                        dildo.method_5667(), throwable);
            }
        }
        runMigrations();
    }

    // ------------------------------------------------------- absence sampling

    /**
     * Samples whether a connection's doll is really missing.
     *
     * <p>{@code entityReady} has to come from an entity-level chunk check: only then does a missing
     * entity mean anything. Any number of calls in one tick advance the record at most once, two
     * evidences need {@link Tuning#ABSENCE_SAMPLE_INTERVAL} ticks between them, and the first sample
     * is only taken {@link Tuning#ENTITY_LOAD_GRACE_TICKS} ticks after the chunk reported its
     * entities loaded.</p>
     *
     * @param dollPos last known position of the doll; moving it restarts the sampling
     */
    public static AbsenceObservation observeAbsent(UUID dildoUuid, class_2338 dollPos, boolean entityReady) {
        if (dildoUuid == null) {
            return AbsenceObservation.WAIT_ENTITY;
        }
        AbsenceRecord record = ABSENCE.computeIfAbsent(dildoUuid, key -> new AbsenceRecord());
        long now = ServerClock.now();
        if (!entityReady) {
            record.reset();
            return AbsenceObservation.WAIT_ENTITY;
        }
        class_2338 anchor = dollPos == null ? null
                : new class_2338(dollPos.method_10263() >> 4, 0, dollPos.method_10260() >> 4);
        if (record.sampledChunk != null && !record.sampledChunk.equals(anchor)) {
            // The connection moved: earlier evidence was about another chunk.
            record.reset();
        }
        record.sampledChunk = anchor;
        if (record.readySince == null) {
            record.readySince = now;
            return AbsenceObservation.WAIT_ENTITY;
        }
        if (now - record.readySince < Tuning.ENTITY_LOAD_GRACE_TICKS) {
            return AbsenceObservation.WAIT_ENTITY;
        }
        if (record.lastSample != null && now - record.lastSample < Tuning.ABSENCE_SAMPLE_INTERVAL) {
            return record.misses >= Tuning.ABSENCE_CONFIRMATIONS
                    ? AbsenceObservation.CONFIRMED_ABSENT
                    : AbsenceObservation.WAIT_ENTITY;
        }
        record.lastSample = now;
        record.misses++;
        return record.misses >= Tuning.ABSENCE_CONFIRMATIONS
                ? AbsenceObservation.CONFIRMED_ABSENT
                : AbsenceObservation.WAIT_ENTITY;
    }

    /** Forgets the absence evidence of a connection, called whenever its doll was seen again. */
    public static void noteEntitySeen(UUID dildoUuid) {
        if (dildoUuid != null) {
            ABSENCE.remove(dildoUuid);
        }
    }

    // ----------------------------------------------------------------- sweeps

    /**
     * One detection pass for a dildo: look at the cell above, convert a placed sensory link doll
     * block if allowed, then establish or refresh the connection.
     */
    public static void sweep(class_3218 world, EnderPearlDildoEntity dildo) {
        InnerState state = InnerState.get(world.method_8503().method_30002());
        UUID dildoUuid = dildo.method_5667();
        InnerState.Binding existing = state.bindingOfDildo(dildoUuid);

        class_2338 above = dildo.method_24515().method_10084();
        List<SensoryLinkDollEntity> candidates = List.of();
        if (ChunkProbes.chunkLoaded(world, above)) {
            // Looking for a candidate only needs the chunk; the stricter entity-level readiness is
            // used where a *missing* doll has to be believable (verifyExisting), not here.
            candidates = dollsInCell(world, above, dildo);
            if (candidates.isEmpty()) {
                maybeConvertPlacedDoll(world, above, dildo);
                candidates = dollsInCell(world, above, dildo);
            }
        }

        // An existing connection that still matches one of the candidates wins outright, so a
        // second doll standing in the cell can never steal the binding.
        if (existing != null) {
            for (SensoryLinkDollEntity candidate : candidates) {
                UUID candidateOwner = ownerOf(candidate);
                if (candidate.method_5667().equals(existing.dollUuid)
                        && candidateOwner != null
                        && candidateOwner.equals(existing.ownerUuid)) {
                    noteEntitySeen(dildoUuid);
                    dildo.setBoundDoll(candidate.method_5667());
                    state.replaceBinding(withPositions(existing, world, dildo, candidate, true));
                    return;
                }
            }
            // Nothing usable above: that is NOT an unbind. A doll going down into the stick's own
            // cell or being retracted by a piston leaves the cell above all the time.
            if (!candidates.isEmpty()) {
                LOGGER.warn("[InnerDimension] Another doll stands above dildo {}; keeping the existing "
                        + "connection to {}", dildoUuid, existing.dollUuid);
            }
            verifyExisting(world, state, existing, dildo);
            return;
        }

        // No usable existing connection: only a unique, fully identified candidate may bind, and
        // only when the security gate allows this stick to connect to that owner at all.
        List<SensoryLinkDollEntity> valid = new ArrayList<>();
        for (SensoryLinkDollEntity candidate : candidates) {
            UUID candidateOwner = ownerOf(candidate);
            if (candidateOwner == null) {
                continue;
            }
            // A doll's owner placement remains valid even when somebody replaces the stick.
            if (state.lockOfDoll(candidate.method_5667()) != null) continue;
            DollSecurityManager.Gate gate = candidate.isOwnerPlaced()
                    ? DollSecurityManager.forDirectOwnerClaim(dildo, candidateOwner)
                    : DollSecurityManager.forPort(dildo, candidateOwner);
            if (gate.state() == SecurityState.OWNER_MISMATCH) {
                // Remember the refusal so a rescan cannot quietly turn it into a transfer.
                dildo.rejectOwner(candidateOwner);
                LOGGER.warn("[InnerDimension] Dildo {} was placed by {} but the doll above belongs to "
                                + "{}; refusing to bind", dildoUuid, dildo.getPlacerUuid(), candidateOwner);
                continue;
            }
            if (gate.blocked()) {
                LOGGER.debug("[InnerDimension] Dildo {} waits for verification ({}): {}",
                        dildoUuid, gate.state(), gate.messageKey());
                continue;
            }
            valid.add(candidate);
        }
        if (valid.isEmpty()) {
            return;
        }
        if (valid.size() > 1) {
            LOGGER.warn("[InnerDimension] {} dolls stand above dildo {}; refusing to pick one",
                    valid.size(), dildoUuid);
            return;
        }

        SensoryLinkDollEntity doll = valid.get(0);
        UUID owner = ownerOf(doll);
        if (doll.isOwnerPlaced()) dildo.authorize(owner);
        // A successfully authorized legacy connection also belongs to this doll from now on.
        doll.markOwnerPlaced();
        class_2960 dimId = world.method_27983().method_29177();
        InnerState.BindingSet set = state.bindingSet(owner);
        // The first connection of a dimension is its default strong port - except while an old save
        // is still being migrated, where the pointer waits for the deterministic pass.
        boolean deferStrong = dildo.isLegacyDildo() && set.strongDildoUuid == null;
        InnerState.Binding binding = new InnerState.Binding(dildoUuid, doll.method_5667(), owner, dimId,
                dildo.method_23317(), dildo.method_23318(), dildo.method_23321(),
                doll.method_23317(), doll.method_23318(), doll.method_23321(), true, true, 0);
        state.registerBinding(binding, !deferStrong);
        if (deferStrong) {
            MIGRATIONS.putIfAbsent(owner, ServerClock.now() + Tuning.LEGACY_COLLECTION_TICKS);
            LOGGER.info("[InnerDimension] Registered legacy dildo {} for {}; the default strong port "
                    + "is picked when the migration window closes", dildoUuid, owner);
        } else {
            dildo.markMigrated();
        }
        dildo.setBoundDoll(doll.method_5667());
        doll.setBoundDildo(dildoUuid, owner);
        noteEntitySeen(dildoUuid);
        LOGGER.info("[InnerDimension] Dildo {} bound to {}'s doll, dimension owner {}",
                dildoUuid, doll.method_5667(), owner);
    }

    /** A copy of a connection with refreshed dildo and doll positions. */
    private static InnerState.Binding withPositions(InnerState.Binding binding, class_3218 world,
                                                    EnderPearlDildoEntity dildo, SensoryLinkDollEntity doll,
                                                    boolean verified) {
        return new InnerState.Binding(binding.dildoUuid, binding.dollUuid, binding.ownerUuid,
                world.method_27983().method_29177(),
                dildo.method_23317(), dildo.method_23318(), dildo.method_23321(),
                doll.method_23317(), doll.method_23318(), doll.method_23321(),
                verified, binding.authorized, binding.bindingRevision);
    }

    /**
     * Re-checks the doll of an existing connection through its own recorded position.
     *
     * <p>This is the only path that may remove a connection because "the entity is not there", and it
     * insists on the entity-level chunk check plus two spaced-out evidences. Everything else keeps
     * the record with {@code verified == false}, which is what an unloaded chunk must look like.</p>
     */
    private static void verifyExisting(class_3218 world, InnerState state,
                                       InnerState.Binding binding, EnderPearlDildoEntity dildo) {
        class_2338 dollPos = new class_2338((int) Math.floor(binding.dollX),
                (int) Math.floor(binding.dollY), (int) Math.floor(binding.dollZ));
        boolean ready = ChunkProbes.entityChunkReady(world, dollPos);
        if (!ready) {
            ChunkProbes.ensure(world, dollPos);
            state.replaceBinding(binding.withVerified(false));
            observeAbsent(binding.dildoUuid, dollPos, false);
            return;
        }
        class_1297 doll = world.method_73284(binding.dollUuid);
        if (doll instanceof SensoryLinkDollEntity present) {
            UUID owner = ownerOf(present);
            if (owner == null) {
                // An unresolved profile is "waiting", never "mismatched".
                state.replaceBinding(binding.withVerified(false));
                observeAbsent(binding.dildoUuid, dollPos, false);
                return;
            }
            if (!owner.equals(binding.ownerUuid)) {
                LOGGER.warn("[InnerDimension] Doll {} above dildo {} belongs to {}, not {}; dropping "
                        + "the connection", binding.dollUuid, binding.dildoUuid, owner, binding.ownerUuid);
                noteEntitySeen(binding.dildoUuid);
                dropBinding(state, binding, dildo);
                return;
            }
            noteEntitySeen(binding.dildoUuid);
            state.replaceBinding(binding.withVerified(true));
            return;
        }
        if (doll != null) {
            LOGGER.warn("[InnerDimension] Doll {} above dildo {} is now a {}; dropping the connection",
                    binding.dollUuid, binding.dildoUuid, doll.getClass().getSimpleName());
            noteEntitySeen(binding.dildoUuid);
            dropBinding(state, binding, dildo);
            return;
        }
        state.replaceBinding(binding.withVerified(false));
        if (observeAbsent(binding.dildoUuid, dollPos, true) == AbsenceObservation.CONFIRMED_ABSENT) {
            LOGGER.warn("[InnerDimension] Doll {} of dildo {} is gone; dropping the connection",
                    binding.dollUuid, binding.dildoUuid);
            noteEntitySeen(binding.dildoUuid);
            dropBinding(state, binding, dildo);
        }
    }

    /**
     * The dolls whose own feet cell is exactly {@code cell}.
     *
     * <p>The query box is the single block cell and the result is filtered by the doll's own position,
     * so a doll standing one cell higher - the idle "placed on top of the stick" state - is never
     * mistaken for one inside it.</p>
     */
    private static List<SensoryLinkDollEntity> dollsInCell(class_3218 world, class_2338 cell,
                                                       EnderPearlDildoEntity dildo) {
        List<SensoryLinkDollEntity> result = new ArrayList<>();
        for (class_1297 entity : world.method_8333(dildo, new class_238(cell), candidate -> true)) {
            if (entity instanceof SensoryLinkDollEntity doll && doll.method_24515().equals(cell)) {
                result.add(doll);
            }
        }
        return result;
    }

    /**
     * The dimension owner of a doll.
     *
     * <p>The identity recorded when the doll was created wins: it comes from the plushie mod's
     * resolver (or from the sensory link data of a converted block) and exists even for a doll that
     * carries no skin profile. Only a doll written by an older build, which never stored it, falls
     * back to its profile.</p>
     */
    public static UUID ownerOf(SensoryLinkDollEntity doll) {
        if (doll == null) {
            return null;
        }
        UUID recorded = doll.getOwnerUuid();
        if (recorded != null) {
            return recorded;
        }
        class_9296 profile = doll.getProfile();
        if (profile == null) {
            return null;
        }
        GameProfile gameProfile = profile.method_73313();
        return gameProfile == null ? null : gameProfile.id();
    }

    /**
     * True when a doll may be placed in {@code cell}: the cell must not already hold one.
     *
     * <p>Placing a second doll into an occupied cell would leave two candidates for one stick, which
     * is exactly the {@code CONFLICT} case the sweep refuses to resolve by guessing.</p>
     */
    public static boolean canPlaceDollAt(class_3218 world, class_2338 cell, EnderPearlDildoEntity dildo) {
        if (!ChunkProbes.chunkLoaded(world, cell)) {
            return false;
        }
        return dollsInCell(world, cell, dildo).isEmpty();
    }

    // -------------------------------------------------- placed doll conversion

    /**
     * Converts a placed non_player_plushie sensory link doll <em>block</em> standing in the cell
     * above a dildo into this mod's doll entity.
     *
     * <p>Only a real {@code non_player_plushie:sensory_link_doll} block is converted, and only when
     * its own {@code SensoryLinkData} names an owner the stick is already allowed to connect to. The
     * recognition and the identity both come from the plushie mod's public API; no player head, no
     * old marker and no custom name is consulted, so a decorative player head and a head an older
     * plushie build marked are both left exactly where they are.</p>
     *
     * <p>The identity is read first and nothing is removed before that. The whole step is a
     * transaction - the original block state and the full block entity data are kept, the entity is
     * spawned and its boolean result checked, the block is removed without drops and the removal is
     * verified; any failure puts the original block back exactly as it was, so the conversion can
     * never eat the doll.</p>
     *
     * <p>Rotation is preserved: the block stores a {@code rotation} property counting sixteenths of
     * a turn.</p>
     */
    private static void maybeConvertPlacedDoll(class_3218 world, class_2338 cell,
                                               EnderPearlDildoEntity dildo) {
        class_2680 state = world.method_8320(cell);
        if (!(state.method_26204() instanceof SensoryLinkDollBlock)) {
            return;
        }
        if (!(world.method_8321(cell) instanceof SensoryLinkDollBlockEntity placed)) {
            return;
        }
        // placed.data() hands out a copy and is null for a block placed without any data.
        class_2487 data = placed.data();
        UUID placedOwner = SensoryLinkData.ownerUuid(data);
        if (placedOwner == null || !SensoryLinkData.valid(data)) {
            LOGGER.debug("[InnerDimension] The sensory link doll at {} carries no usable owner; "
                    + "keeping it", cell);
            return;
        }
        // Identity and authorization are checked before anything is touched.
        String placerRaw = data.method_68564(BLOCK_PLACER_KEY, "");
        UUID placer = null;
        if (!placerRaw.isEmpty()) {
            try { placer = UUID.fromString(placerRaw); }
            catch (IllegalArgumentException ignored) { }
        }
        boolean manualPlacement = !placerRaw.isEmpty();
        DollSecurityManager.Gate gate = manualPlacement
                ? DollSecurityManager.forPlacedDoll(dildo, placedOwner, placer)
                : DollSecurityManager.forPort(dildo, placedOwner);
        if (gate.blocked()) {
            if (gate.state() == SecurityState.OWNER_MISMATCH) {
                dildo.rejectOwner(placedOwner);
            }
            LOGGER.debug("[InnerDimension] Not converting the placed doll at {}: {}", cell, gate.state());
            return;
        }

        // --- 1. everything needed to restore the original object ---
        class_7225.class_7874 registries = world.method_8503().method_46221().method_45926();
        class_2487 savedData = placed.method_38242(registries);
        float yaw = dollYaw(state);

        // --- 2. build the entity, still unbound ---
        SensoryLinkDollEntity doll = new SensoryLinkDollEntity(
                com.innerdimension.entity.ModEntities.SENSORY_LINK_DOLL, world);
        doll.method_5814(cell.method_10263() + 0.5D, cell.method_10264(), cell.method_10260() + 0.5D);
        doll.method_36456(yaw);
        doll.method_36457(0.0F);
        doll.setDollData(data);
        doll.setOwnerUuid(placedOwner);
        doll.setOwnerName(SensoryLinkData.ownerName(data));

        // --- 3. spawn and check the result ---
        if (!world.method_8649(doll)) {
            LOGGER.warn("[InnerDimension] Could not spawn the doll entity at {}; block kept", cell);
            doll.method_31472();
            return;
        }

        // --- 4. remove the block without drops and verify it really went ---
        boolean removed = world.method_8650(cell, false);
        if (!removed || world.method_8320(cell).method_26204() instanceof SensoryLinkDollBlock) {
            LOGGER.error("[InnerDimension] Could not remove the placed sensory link doll at {}; "
                    + "restoring it", cell);
            doll.method_31472();
            restorePlacedDoll(world, cell, state, savedData, registries);
            return;
        }
        if (manualPlacement) {
            // Commit authorization only after conversion succeeded, never on a failed spawn/removal.
            doll.markOwnerPlaced();
            dildo.authorize(placedOwner);
            dildo.markMigrated();
        }
        LOGGER.info("[InnerDimension] Converted the placed sensory link doll at {} into an entity", cell);
    }

    /** Puts a saved doll block and its data back, used when a conversion step failed. */
    private static void restorePlacedDoll(class_3218 world, class_2338 cell, class_2680 state,
                                          class_2487 savedData, class_7225.class_7874 registries) {
        world.method_8652(cell, state, 3);
        class_2586 restored = class_2586.method_11005(cell, state, savedData, registries);
        if (restored != null) {
            world.method_8438(restored);
        }
    }

    /**
     * The yaw a placed doll block was facing.
     *
     * <p>The block stores its facing as a {@code rotation} property counting sixteenths of a turn,
     * and the conversion has to keep it. The property is looked up by name rather than by the
     * plushie mod's own value holder, which keeps the lookup valid even if the block is reworked.</p>
     */
    private static float dollYaw(class_2680 state) {
        for (class_2769<?> property : state.method_28501()) {
            if (!"rotation".equals(property.method_11899())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            class_2769<Integer> rotation = (class_2769<Integer>) property;
            return state.method_11654(rotation) * 22.5F;
        }
        return 0.0F;
    }

    // ---------------------------------------------------------------- cleanup

    /**
     * Removes every connection that belongs to this dildo, including the strong pointer when it
     * pointed here.
     *
     * <p>A locked pair is explicitly protected: the spec says a lock survives normal destruction
     * paths, so the connection is kept and the caller is told that the object was not cleaned.</p>
     */
    public static void clearConnectionsOfDildo(EnderPearlDildoEntity dildo) {
        class_1937 world = dildo.method_73183();
        if (world == null || world.method_8608()) {
            return;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        UUID dildoUuid = dildo.method_5667();
        if (state.lockOfDildo(dildoUuid) != null) {
            LOGGER.warn("[InnerDimension] Refusing to drop the locked connection of dildo {}", dildoUuid);
            return;
        }
        for (Map.Entry<UUID, InnerState.BindingSet> entry : state.allBindings().entrySet()) {
            if (entry.getValue().bindings.containsKey(dildoUuid)) {
                state.removeBinding(entry.getKey(), dildoUuid);
                dildo.clearBindingIdentity();
                return;
            }
        }
    }

    /** Removes the connections that referenced this doll, called when a doll is destroyed. */
    public static void clearConnectionsOfDoll(SensoryLinkDollEntity doll) {
        class_1937 world = doll.method_73183();
        if (world == null || world.method_8608()) {
            return;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        UUID dollUuid = doll.method_5667();
        if (state.lockOfDoll(dollUuid) != null) {
            LOGGER.warn("[InnerDimension] Refusing to drop the locked connections of doll {}", dollUuid);
            return;
        }
        for (Map.Entry<UUID, InnerState.BindingSet> entry : state.allBindings().entrySet()) {
            for (InnerState.Binding binding : new ArrayList<>(entry.getValue().bindings.values())) {
                if (binding.dollUuid.equals(dollUuid)) {
                    LOGGER.info("[InnerDimension] Doll {} was removed; dropping its connections", dollUuid);
                    state.removeBinding(entry.getKey(), binding.dildoUuid);
                    class_1297 port = world.method_73284(binding.dildoUuid);
                    if (port instanceof EnderPearlDildoEntity dildo) dildo.clearBindingIdentity();
                }
            }
        }
    }

    // -------------------------------------------------------------- promotion

    /**
     * Shift + right-click on a dildo that has a usable connection: make it the one strong output
     * port of its dimension owner, or claim an unverified legacy port.
     *
     * <p>Only the connection's own owner may do this, a locked pair may not be re-pointed, and the
     * connection has to survive validation first.</p>
     *
     * @return the outcome of the attempt
     */
    public static Promotion promoteStrong(class_3218 world, EnderPearlDildoEntity dildo, UUID actorUuid) {
        if (world == null || dildo == null) {
            return Promotion.DENIED;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        UUID dildoUuid = dildo.method_5667();
        InnerState.Binding binding = state.bindingOfDildo(dildoUuid);
        if (binding == null) {
            return Promotion.NOT_CONNECTED;
        }
        if (state.lockOfDoll(binding.dollUuid) != null || state.lockOfDildo(dildoUuid) != null) {
            return Promotion.LOCKED;
        }
        if (actorUuid == null || !actorUuid.equals(binding.ownerUuid)) {
            return Promotion.DENIED;
        }
        // Validate before trusting the record: the dildo may have been moved after the sweep.
        sweep(world, dildo);
        binding = state.bindingOfDildo(dildoUuid);
        if (binding == null) {
            return Promotion.NOT_CONNECTED;
        }
        if (!binding.authorized) {
            // The owner is standing here and proving the connection by hand: this is the fixed claim
            // path for an old save, and the only way an unverified port becomes usable again.
            state.replaceBinding(new InnerState.Binding(binding.dildoUuid, binding.dollUuid,
                    binding.ownerUuid, binding.dimId, binding.x, binding.y, binding.z,
                    binding.dollX, binding.dollY, binding.dollZ, binding.verified, true,
                    binding.bindingRevision));
            dildo.authorize(actorUuid);
            dildo.markMigrated();
            state.promoteStrong(binding.ownerUuid, dildoUuid);
            LOGGER.info("[InnerDimension] {} claimed the legacy connection of dildo {}", actorUuid, dildoUuid);
            return Promotion.CLAIMED;
        }
        return state.promoteStrong(binding.ownerUuid, dildoUuid)
                ? Promotion.PROMOTED
                : Promotion.ALREADY_STRONG;
    }

    /** The outcome of a promotion attempt. */
    public enum Promotion {
        /** The stick became the strong port. */
        PROMOTED,
        /** It already was the strong port. */
        ALREADY_STRONG,
        /** An unverified legacy connection was claimed and promoted. */
        CLAIMED,
        /** The pair carries a lock. */
        LOCKED,
        /** The actor is not the connection's owner, or the request was invalid. */
        DENIED,
        /** No connection to promote. */
        NOT_CONNECTED
    }

    /** The dimension owner this dildo currently resolves to, or {@code null}. */
    public static UUID ownerOfDildo(EnderPearlDildoEntity dildo) {
        class_1937 world = dildo.method_73183();
        if (world == null || world.method_8608()) {
            return null;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        InnerState.Binding binding = state.bindingOfDildo(dildo.method_5667());
        return binding == null ? null : binding.ownerUuid;
    }

    /** Removes the authoritative record and the matching live entity's recovery caches together. */
    private static void dropBinding(InnerState state, InnerState.Binding binding,
                                    EnderPearlDildoEntity dildo) {
        state.removeBinding(binding.ownerUuid, binding.dildoUuid);
        if (dildo != null && binding.dildoUuid.equals(dildo.method_5667())) {
            dildo.clearBindingIdentity();
        }
    }

    /** True when the dildo has a connection that has not been invalidated. */
    public static boolean hasUsableConnection(EnderPearlDildoEntity dildo) {
        class_1937 world = dildo.method_73183();
        if (world == null || world.method_8608()) {
            return false;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        InnerState.Binding binding = state.bindingOfDildo(dildo.method_5667());
        return binding != null && binding.verified;
    }

    /** Called by the dildo after it is placed or loaded, so detection starts promptly. */
    public static void onDildoPlaced(EnderPearlDildoEntity dildo) {
        track(dildo);
        class_1937 world = dildo.method_73183();
        if (world instanceof class_3218 serverWorld) {
            try {
                sweep(serverWorld, dildo);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Initial binding sweep failed", throwable);
            }
        }
    }

    // ---------------------------------------------------------------- migration

    /**
     * Finishes the migration of an old save.
     *
     * <p>Legacy dildos are discovered while their chunks load, and chunk load order must not decide
     * which one becomes the strong output port. Their connections are registered without a strong
     * pointer, and once the collection window closed the pointer goes to the candidate with the
     * smallest {@code uuid.toString()} - a deterministic choice that does not depend on load order.
     * A candidate that arrives after the deadline stays weak, even if its UUID is smaller.</p>
     */
    private static void runMigrations() {
        if (MIGRATIONS.isEmpty()) {
            return;
        }
        long now = ServerClock.now();
        Iterator<Map.Entry<UUID, Long>> iterator = MIGRATIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() > now) {
                continue;
            }
            UUID owner = entry.getKey();
            iterator.remove();
            EnderPearlDildoEntity any = trackedDildoOf(owner);
            if (any == null) {
                continue;
            }
            if (!(any.method_73183() instanceof class_3218 serverWorld)) {
                continue;
            }
            InnerState state = InnerState.get(serverWorld.method_8503().method_30002());
            List<UUID> sorted = state.sortedDildoUuids(owner);
            if (sorted.isEmpty()) {
                // Nothing legal was collected: the window closes without claiming hasEverBound, so a
                // later legal candidate may open a new window.
                LOGGER.info("[InnerDimension] The migration window of {} closed without a candidate", owner);
                continue;
            }
            sorted.sort(java.util.Comparator.comparing(UUID::toString));
            UUID chosen = sorted.get(0);
            state.promoteStrong(owner, chosen);
            LOGGER.info("[InnerDimension] Migrated the strong output port of {} to dildo {} "
                    + "(lowest of {} collected candidates)", owner, chosen, sorted.size());
        }
    }

    /** Any tracked dildo whose connection belongs to {@code owner}, used to reach its world. */
    private static EnderPearlDildoEntity trackedDildoOf(UUID owner) {
        for (EnderPearlDildoEntity dildo : TRACKED.keySet()) {
            if (owner.equals(ownerOfDildo(dildo))) {
                return dildo;
            }
        }
        return null;
    }
}
