package com.innerdimension.security;

import com.innerdimension.binding.DollBindingManager;
import com.innerdimension.core.ServerClock;
import com.innerdimension.entity.SensoryLinkDollEntity;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.state.InnerState;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The one place that answers "may this dildo/doll pair transfer anything right now?".
 *
 * <p>Security spec sections 2, 4 and 7. The answer is a structured {@link Gate}, never a boolean
 * and never {@code null}: a locked port, a mismatch, a legacy port waiting to be claimed and a
 * genuinely unbound dildo are four different things, and only the last one may take the ordinary
 * fallbacks (entering your own dimension, output through the entry source, the room owner's return
 * point).</p>
 *
 * <p>Who is who, fixed by the spec: {@code actorUuid} is the server-side interacting player,
 * {@code dollOwnerUuid} always comes from the doll's head profile, {@code placerUuid} is fixed when
 * the dildo item is placed and is never rewritten by a rescan, a piston, a reload or a promotion,
 * and {@code authorizedOwnerUuid} is only ever set by a successful interaction of the doll's own
 * owner. Names, display text, skins and anything a client sends take no part in this.</p>
 */
public final class DollSecurityManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");

    /** The two belt items of the NoN default pack that may lock a doll. */
    public static final class_2960 BELT_A = class_2960.method_60655("needsofnature", "protector_a");
    public static final class_2960 BELT_AV = class_2960.method_60655("needsofnature", "protector_av");

    /** Feedback message keys, resolved through the language files - never shown as literal keys. */
    public static final String MSG_LOCKED = "inner_dimension.security.locked";
    public static final String MSG_NOT_YOURS = "inner_dimension.security.not_yours";
    public static final String MSG_PENDING = "inner_dimension.security.pending";
    public static final String MSG_LOCK_DONE = "inner_dimension.security.lock_done";
    public static final String MSG_UNLOCK_DONE = "inner_dimension.security.unlock_done";
    public static final String MSG_LOCK_FAILED = "inner_dimension.security.lock_failed";
    public static final String MSG_CLAIM_DONE = "inner_dimension.security.claim_done";

    /** Guards "at most one lock state change per player per server tick". */
    private static final Map<UUID, Long> LAST_LOCK_CHANGE = new HashMap<>();
    /**
     * Guards "one click cannot lock and unlock in the same action", keyed by the doll.
     *
     * <p>The two interaction hooks of a single right-click do not always arrive in the same server
     * tick, so a per-tick guard alone was not enough: with a single belt the first hook locked the
     * doll, the hand became empty, and the second hook - arriving a tick later - saw an empty-handed
     * click on a locked doll and unlocked it again. To the player that looked like "locking only
     * worked once". A short guard on the doll itself closes that window.</p>
     */
    private static final Map<UUID, Long> LAST_DOLL_CHANGE = new HashMap<>();
    /** Separation required between two state changes of the same doll. */
    private static final int DOLL_CHANGE_GUARD_TICKS = 10;
    private static boolean beltMissingDiagnosed;

    private DollSecurityManager() {
    }

    /** The verdict of a security check. */
    public record Gate(SecurityState state, String messageKey) {
        public boolean allowed() {
            return state.allowsTransfer();
        }

        public boolean blocked() {
            return state.blocksPort();
        }
    }

    private static Gate gate(SecurityState state, String messageKey) {
        return new Gate(state, messageKey);
    }

    /** Called on server start, so no per-session guard survives into the next session. */
    public static void reset() {
        LAST_LOCK_CHANGE.clear();
        LAST_DOLL_CHANGE.clear();
    }

    // ------------------------------------------------------------------ belts

    /**
     * True when the stack is one of the two accepted belts.
     *
     * <p>The registered id is compared, not the translated name: renaming a random item to
     * "A Chastity Belt" must not lock anything, and an unknown id is reported once instead of being
     * silently replaced by another item.</p>
     */
    public static boolean isAcceptedBelt(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_2378<net.minecraft.class_1792> registry = class_7923.field_41178;
        class_2960 id = registry.method_10221(stack.method_7909());
        if (id == null) {
            return false;
        }
        if (BELT_A.equals(id) || BELT_AV.equals(id)) {
            return true;
        }
        if (id.method_12836().equals(BELT_A.method_12836())
                && id.method_12832().startsWith("protector_")) {
            // A sibling belt (for example protector_v) is deliberately not accepted.
            return false;
        }
        return false;
    }

    /** Reports once when neither belt id can be resolved, so a missing pack does not spam. */
    public static void diagnoseMissingBelts() {
        if (beltMissingDiagnosed) {
            return;
        }
        beltMissingDiagnosed = true;
        boolean a = class_7923.field_41178.method_10223(BELT_A).isPresent();
        boolean av = class_7923.field_41178.method_10223(BELT_AV).isPresent();
        if (!a || !av) {
            LOGGER.warn("[InnerDimension] Chastity belt ids not both present ({}={}, {}={}); "
                            + "locking is unavailable for the missing one",
                    BELT_A, a, BELT_AV, av);
        }
    }

    // ------------------------------------------------------------------ state

    /** The security state of a dildo against a candidate doll owner. */
    public static Gate forPort(EnderPearlDildoEntity dildo, UUID candidateOwner) {
        if (dildo == null) {
            return gate(SecurityState.UNBOUND, null);
        }
        InnerState state = stateOf(dildo);
        if (state != null) {
            // A lock is held on the actual objects, so it is found through either UUID.
            UUID boundDoll = dildo.getBoundDoll();
            if ((boundDoll != null && state.lockOfDoll(boundDoll) != null)
                    || state.lockOfDildo(dildo.method_5667()) != null) {
                return gate(SecurityState.LOCKED, MSG_LOCKED);
            }
        }
        UUID authorized = dildo.getAuthorizedOwner();
        UUID placer = dildo.getPlacerUuid();
        UUID rejected = dildo.getRejectedOwner();

        if (candidateOwner == null) {
            if (dildo.getBoundDoll() != null || authorized != null) {
                // A connection exists but its profile cannot be resolved right now: wait, because
                // "unknown" is not "unbound" and must not open the fallbacks.
                return gate(SecurityState.AUTH_PENDING, MSG_PENDING);
            }
            return gate(SecurityState.UNBOUND, null);
        }
        if (candidateOwner.equals(rejected)) {
            return gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
        }
        if (authorized != null) {
            return candidateOwner.equals(authorized)
                    ? gate(SecurityState.AUTHORIZED, null)
                    : gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
        }
        if (placer != null) {
            return candidateOwner.equals(placer)
                    ? gate(SecurityState.AUTHORIZED, null)
                    : gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
        }
        // No placer evidence at all: an old port that already has a connection waits for its owner
        // to claim it; a dildo that never connected anything stays plainly unbound.
        if (dildo.getBoundDoll() != null || dildo.isLegacyDildo()) {
            return gate(SecurityState.LEGACY_UNVERIFIED, MSG_PENDING);
        }
        return gate(SecurityState.UNBOUND, null);
    }

    /**
     * The gate in front of every dimension branch of an interaction.
     *
     * <p>Only {@code UNBOUND} may continue into "enter my own dimension". A locked, mismatched,
     * pending or unclaimed port consumes the interaction and says why - it never falls back.</p>
     */
    public static Gate forInteraction(EnderPearlDildoEntity dildo, UUID actorUuid) {
        if (dildo == null) {
            return gate(SecurityState.UNBOUND, null);
        }
        InnerState state = stateOf(dildo);
        UUID boundDoll = dildo.getBoundDoll();
        if (state != null && ((boundDoll != null && state.lockOfDoll(boundDoll) != null)
                || state.lockOfDildo(dildo.method_5667()) != null)) {
            return gate(SecurityState.LOCKED, MSG_LOCKED);
        }
        UUID owner = boundOwnerOf(dildo);
        if (owner == null) {
            // With no doll connection, every player may use the ordinary personal-room entry,
            // including legacy devices whose original placer was never recorded.
            return gate(SecurityState.UNBOUND, null);
        }
        if (actorUuid != null && actorUuid.equals(owner)) {
            return gate(SecurityState.AUTHORIZED, null);
        }
        return gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
    }

    /**
     * Gate for a server-authenticated owner placing their own doll by hand.
     *
     * <p>The immutable placer UUID is useful evidence for unattended/automatic binding, but it
     * must not make first-time authorization impossible in multiplayer. A live player holding a
     * doll whose embedded UUID is their own may claim a genuinely unbound port. Existing bindings,
     * authorizations and locks remain protected.</p>
     */
    public static Gate forDirectOwnerClaim(EnderPearlDildoEntity dildo, UUID ownerUuid) {
        if (dildo == null || ownerUuid == null) return gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
        InnerState state = stateOf(dildo);
        UUID boundDoll = dildo.getBoundDoll();
        if (state != null && ((boundDoll != null && state.lockOfDoll(boundDoll) != null)
                || state.lockOfDildo(dildo.method_5667()) != null)) {
            return gate(SecurityState.LOCKED, MSG_LOCKED);
        }
        UUID boundOwner = boundOwnerOf(dildo);
        if (boundOwner == null) return gate(SecurityState.UNBOUND, null);
        return ownerUuid.equals(boundOwner)
                ? gate(SecurityState.AUTHORIZED, null)
                : gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
    }

    /** A manually placed block uses its actual placer, just like right-click placement. */
    public static Gate forPlacedDoll(EnderPearlDildoEntity dildo, UUID ownerUuid, UUID placerUuid) {
        if (ownerUuid == null || !ownerUuid.equals(placerUuid)) {
            return gate(SecurityState.OWNER_MISMATCH, MSG_NOT_YOURS);
        }
        return forDirectOwnerClaim(dildo, ownerUuid);
    }

    /** The dimension owner this dildo is connected to, resolved from the live doll or its record. */
    public static UUID boundOwnerOf(EnderPearlDildoEntity dildo) {
        InnerState state = stateOf(dildo);
        if (state != null) {
            InnerState.Binding binding = state.bindingOfDildo(dildo.method_5667());
            // Runtime state is authoritative. Entity fields are persisted recovery caches and must
            // never resurrect ownership after the binding was deliberately removed.
            return binding == null ? null : binding.ownerUuid;
        }
        if (dildo.getAuthorizedOwner() != null) {
            return dildo.getAuthorizedOwner();
        }
        return dildo.getPlushieOwner();
    }

    /** True when the actual pair carries a lock, whichever UUID the caller knows. */
    public static boolean isLocked(EnderPearlDildoEntity dildo, UUID dollUuid) {
        InnerState state = stateOf(dildo);
        if (state == null) {
            return false;
        }
        if (dollUuid != null && state.lockOfDoll(dollUuid) != null) {
            return true;
        }
        UUID bound = dildo == null ? null : dildo.getBoundDoll();
        if (bound != null && state.lockOfDoll(bound) != null) {
            return true;
        }
        return dildo != null && state.lockOfDildo(dildo.method_5667()) != null;
    }

    /** The lock governing a doll, if any. */
    public static InnerState.SecurityLock lockOf(UUID dollUuid, UUID dildoUuid) {
        if (dollUuid == null) {
            return null;
        }
        InnerState state = stateFor(dollUuid, dildoUuid);
        return state == null ? null : state.lockOfDoll(dollUuid);
    }

    private static InnerState stateOf(EnderPearlDildoEntity dildo) {
        class_1937 world = dildo.method_73183();
        if (!(world instanceof class_3218 serverWorld) || serverWorld.method_8503() == null) {
            return null;
        }
        return InnerState.get(serverWorld.method_8503().method_30002());
    }

    private static InnerState stateFor(UUID dollUuid, UUID dildoUuid) {
        // Both UUIDs are resolved through the running server's overworld data.
        net.minecraft.server.MinecraftServer server = com.innerdimension.core.ServerHolder.server();
        return server == null ? null : InnerState.get(server.method_30002());
    }

    // ------------------------------------------------------------ interactions

    /**
     * The outcome of a lock or unlock attempt.
     *
     * <p>A plain boolean hid every reason: "the doll is already locked", "you are not the owner" and
     * "you were holding something" all looked identical, both in chat and in the log. The states are
     * separate so the player is told what happened and the log can be used to diagnose a report.</p>
     */
    public enum LockOutcome {
        LOCKED,
        UNLOCKED,
        NOT_A_BELT,
        NOT_OWNER,
        NOT_BOUND_HERE,
        MOVING,
        ALREADY_LOCKED,
        NO_RECORD,
        HANDS_NOT_EMPTY,
        NO_RETURNABLE_BELT,
        DUPLICATE_THIS_TICK,
        MISSING_DILDO
    }

    /**
     * Locks the pair the player is right-clicking with a belt in the main hand.
     *
     * <p>Security spec section 5: every condition is re-checked here on the server, the belt is
     * copied with {@code count = 1} into the lock record, exactly one item leaves the main hand, and
     * any step that does not commit leaves both the item and the state as they were.</p>
     */
    public static LockOutcome tryLock(EnderPearlDildoEntity dildo, SensoryLinkDollEntity doll, class_3222 actor) {
        if (dildo == null || doll == null || actor == null) {
            return LockOutcome.MISSING_DILDO;
        }
        if (actor.method_7325()) {
            return LockOutcome.NOT_OWNER; // spectators never change state
        }
        if (dollChangeTooSoon(doll.method_5667())) {
            // The other hook of the same click, or a duplicated packet: not a new action.
            return LockOutcome.DUPLICATE_THIS_TICK;
        }
        class_1799 mainHand = actor.method_5998(class_1268.field_5808);
        if (!isAcceptedBelt(mainHand)) {
            return LockOutcome.NOT_A_BELT;
        }
        if (!claimLockSlot(actor)) {
            return LockOutcome.DUPLICATE_THIS_TICK;
        }
        UUID dollOwner = DollBindingManager.ownerOf(doll);
        if (dollOwner == null || !dollOwner.equals(actor.method_5667())) {
            return LockOutcome.NOT_OWNER;
        }
        UUID boundOwner = boundOwnerOf(dildo);
        if (boundOwner == null || !boundOwner.equals(dollOwner)) {
            return LockOutcome.NOT_BOUND_HERE;
        }
        if (!doll.method_24515().equals(dildo.method_24515().method_10084())) {
            return LockOutcome.NOT_BOUND_HERE;
        }
        if (!doll.isSettled()) {
            return LockOutcome.MOVING;
        }
        if (!dildo.method_5667().equals(doll.getBoundDildo())
                || !dollOwner.equals(doll.getBoundOwner())) {
            return LockOutcome.NOT_BOUND_HERE;
        }
        if (!(dildo.method_73183() instanceof class_3218 world) || world.method_8503() == null) {
            return LockOutcome.MISSING_DILDO;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        if (state.lockOfDoll(doll.method_5667()) != null) {
            return LockOutcome.ALREADY_LOCKED; // one belt per doll
        }
        if (dildo.method_31481() || doll.method_31481()) {
            return LockOutcome.MISSING_DILDO;
        }

        // --- commit: build the record from a one-item copy, consume one, publish the lock ---
        class_1799 belt = mainHand.method_46651(1);
        InnerState.SecurityLock lock = new InnerState.SecurityLock(
                UUID.randomUUID().toString(), doll.method_5667(), dildo.method_5667(),
                dollOwner, belt, 1);
        if (!state.addLock(lock)) {
            return LockOutcome.ALREADY_LOCKED;
        }
        int remaining = mainHand.method_7947() - 1;
        actor.method_6122(class_1268.field_5808,
                remaining <= 0 ? class_1799.field_8037 : mainHand.method_46651(remaining));
        doll.setLockId(lock.lockId());
        doll.setLockedDisplay(true);
        // Requests already queued for this port are void, and the current contact is suppressed so
        // lifting the lock later cannot replay it.
        dildo.suppressContactUntilSeparated();
        markDollChanged(doll.method_5667());
        releaseLockSlot(actor);
        LOGGER.info("[InnerDimension] Doll {} locked with {} (dildo {})",
                doll.method_5667(), lock.lockId(), dildo.method_5667());
        return LockOutcome.LOCKED;
    }

    /**
     * Hands the original belt back and clears the lock.
     *
     * <p>Security spec section 6: both hands empty, actor is the lock owner, the record still holds a
     * returnable belt and the main hand is verified empty again at commit time. A failure keeps the
     * lock - a consumed record without a returned belt would destroy an item.</p>
     */
    public static LockOutcome tryUnlock(SensoryLinkDollEntity doll, class_3222 actor, InnerState state) {
        if (doll == null || actor == null || state == null || actor.method_7325()) {
            return LockOutcome.NOT_OWNER;
        }
        if (dollChangeTooSoon(doll.method_5667())) {
            // A click that just locked must not unlock again through its second hook.
            return LockOutcome.DUPLICATE_THIS_TICK;
        }
        class_1799 mainHand = actor.method_5998(class_1268.field_5808);
        class_1799 offHand = actor.method_5998(class_1268.field_5810);
        if (!mainHand.method_7960() || !offHand.method_7960()) {
            return LockOutcome.HANDS_NOT_EMPTY;
        }
        InnerState.SecurityLock lock = state.lockOfDoll(doll.method_5667());
        if (lock == null) {
            return LockOutcome.NO_RECORD;
        }
        if (!actor.method_5667().equals(lock.ownerUuid())) {
            return LockOutcome.NOT_OWNER;
        }
        UUID dollOwner = DollBindingManager.ownerOf(doll);
        if (dollOwner == null || !dollOwner.equals(lock.ownerUuid())) {
            return LockOutcome.NOT_OWNER;
        }
        if (doll.getLockId() != null && !doll.getLockId().equals(lock.lockId())) {
            return LockOutcome.NO_RECORD; // the entity and the authoritative record disagree
        }
        if (!lock.hasReturnableBelt()) {
            // A damaged record must not be "fixed" by inventing a new belt.
            LOGGER.error("[InnerDimension] The lock {} of doll {} holds no returnable belt; keeping the lock",
                    lock.lockId(), doll.method_5667());
            return LockOutcome.NO_RETURNABLE_BELT;
        }
        if (!claimLockSlot(actor)) {
            return LockOutcome.DUPLICATE_THIS_TICK;
        }
        class_1799 currentMain = actor.method_5998(class_1268.field_5808);
        if (!currentMain.method_7960()) {
            return LockOutcome.HANDS_NOT_EMPTY; // re-check right before committing
        }
        // --- commit ---
        actor.method_6122(class_1268.field_5808, lock.beltStack().method_46651(1));
        state.removeLock(doll.method_5667());
        doll.setLockId(null);
        doll.setLockedDisplay(false);
        markDollChanged(doll.method_5667());
        releaseLockSlot(actor);
        LOGGER.info("[InnerDimension] Doll {} unlocked; the belt was returned", doll.method_5667());
        return LockOutcome.UNLOCKED;
    }

    /**
     * Clears the display lock state of a doll that has no authoritative record, used on load so a
     * stale local flag cannot keep a port "locked" forever.
     */
    public static void refreshDisplayState(SensoryLinkDollEntity doll, InnerState state) {
        if (doll == null || state == null) {
            return;
        }
        InnerState.SecurityLock lock = state.lockOfDoll(doll.method_5667());
        if (lock == null) {
            if (doll.getLockId() != null) {
                LOGGER.warn("[InnerDimension] Doll {} references a missing lock {}; clearing the local reference",
                        doll.method_5667(), doll.getLockId());
            }
            doll.setLockId(null);
            doll.setLockedDisplay(false);
            return;
        }
        doll.setLockId(lock.lockId());
        doll.setLockedDisplay(true);
    }

    /** True when this player already changed a lock state during the current tick. */
    private static boolean claimLockSlot(class_3222 actor) {
        UUID id = actor.method_5667();
        Long last = LAST_LOCK_CHANGE.get(id);
        if (last != null && last == ServerClock.now()) {
            return false;
        }
        return true;
    }

    private static void releaseLockSlot(class_3222 actor) {
        LAST_LOCK_CHANGE.put(actor.method_5667(), ServerClock.now());
    }

    /** True when this doll had a lock state change too recently for another one to be a new action. */
    private static boolean dollChangeTooSoon(UUID dollUuid) {
        Long last = LAST_DOLL_CHANGE.get(dollUuid);
        return last != null && ServerClock.now() - last < DOLL_CHANGE_GUARD_TICKS;
    }

    private static void markDollChanged(UUID dollUuid) {
        LAST_DOLL_CHANGE.put(dollUuid, ServerClock.now());
    }

    /** Clears the per-session guards when a server starts. */
    public static void resetGuards() {
        LAST_LOCK_CHANGE.clear();
        LAST_DOLL_CHANGE.clear();
    }

    /** The message text for a gate, or {@code null} when nothing should be said. */
    public static class_2561 feedback(Gate gate) {
        return gate.messageKey() == null ? null : class_2561.method_43471(gate.messageKey());
    }

    /** Convenience: sends a gate's feedback to a player, at most one message per interaction. */
    public static void sendFeedback(class_3222 player, Gate gate) {
        if (player == null || gate == null) {
            return;
        }
        class_2561 text = feedback(gate);
        if (text != null) {
            player.method_7353(text, true);
        }
    }

    /** Sends a plain translation key as real translated text. */
    public static void send(class_3222 player, String translationKey) {
        if (player != null && translationKey != null) {
            player.method_7353(class_2561.method_43471(translationKey), true);
        }
    }
}
