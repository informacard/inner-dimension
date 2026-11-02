package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import com.innerdimension.domain.world.DomainWorldActions;
import net.minecraft.*;


import java.util.*;

/**
 * Shared plumbing for the D24-D46 handlers. Package-private on purpose: it is not part of
 * the ability-layer API surface and no other package may depend on it.
 *
 * <p>Nothing here keeps state of its own. Timers and counters live in the
 * {@link DomainSession}, collected values live in the session state map.</p>
 */
final class D24D46Support {
    private D24D46Support() {}

    /** Enemies (including bosses) currently inside the session interior. */
    static List<class_1309> enemies(DomainSession session) {
        List<class_1309> out = new ArrayList<>(4);
        for (class_1309 entity : session.livingInside()) {
            if (entity == null || entity.method_7325() || !entity.method_5805()) continue;
            if (DomainRelations.isEnemy(session, entity)) out.add(entity);
        }
        return out;
    }

    /** The owner plus every explicit ally inside the session interior. */
    static List<class_1309> friendlies(DomainSession session) {
        List<class_1309> out = new ArrayList<>(4);
        for (class_1309 entity : session.livingInside()) {
            if (entity == null || entity.method_7325() || !entity.method_5805()) continue;
            if (DomainRelations.isFriendly(session, entity)) out.add(entity);
        }
        return out;
    }

    /**
     * Qualification-gated per-target period.
     *
     * <p>The first sighting only arms the timer and returns false, so a target that just
     * entered the domain never takes a hit on its very first tick. Every later attempt
     * restarts the full interval, whether the damage lands or is cancelled downstream —
     * that is what makes "wait a whole interval after a cancellation" work.</p>
     */
    static boolean periodReady(DomainSession session, String key, UUID target, long interval, long now) {
        String seen = "seen:" + key + ":" + target;
        if (!session.flag(seen)) {
            session.flag(seen, true);
            session.arm(key, target, now);
            return false;
        }
        if (session.remaining(key, target, interval, now) > 0) return false;
        session.arm(key, target, now);
        return true;
    }

    /** Forgets a target's qualification so the next sighting restarts the interval. */
    static void forgetQualification(DomainSession session, String key, UUID target) {
        session.flag("seen:" + key + ":" + target, false);
        session.clearTimer(key, target);
    }

    /** Resolves a living entity by uuid inside the given box, or null. */
    static class_1309 livingByUuid(class_1937 world, class_238 box, UUID id) {
        if (world == null || box == null || id == null) return null;
        for (class_1309 entity : world.method_8390(class_1309.class, box, e -> e.method_5667().equals(id))) return entity;
        return null;
    }

    /** Nearest enemy of the session to the given point, or null. */
    static class_1309 nearestEnemy(DomainSession session, class_243 from) {
        class_1309 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (class_1309 entity : enemies(session)) {
            double distance = entity.method_33571().method_1020(from).method_1033();
            if (distance < bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    /**
     * Applies a per-entity lease to exactly the entities that qualify right now, and
     * withdraws it from the ones that stopped qualifying. The previous set lives in the
     * session, never in a static map keyed by owner.
     */
    @SuppressWarnings("unchecked")
    static void leaseSet(DomainSession session, String key, List<class_1309> current,
                         java.util.function.Consumer<class_1309> grant,
                         java.util.function.Consumer<class_1309> revoke) {
        Set<UUID> ids = new HashSet<>();
        for (class_1309 entity : current) {
            ids.add(entity.method_5667());
            grant.accept(entity);
        }
        Set<UUID> previous = session.state(key, (Set<UUID>) null);
        if (previous != null) {
            for (UUID id : previous) {
                if (ids.contains(id)) continue;
                class_1309 gone = livingByUuid(session.world, session.bounds(64), id);
                if (gone != null) revoke.accept(gone);
            }
        }
        session.putState(key, ids);
    }

    /* ------------------------------------------------------------------ copper */

    /**
     * One tick of the shared D27/D44 copper scanner: at most 64 real blocks per tick, then
     * a re-scan every 20 ticks for newly placed copper. The rotating cursor namespace is
     * shared between both abilities ("copper"), and a block covered by a session that wants
     * the opposite direction is recorded as a conflict and left alone by both sides.
     *
     * @param direction +1 oxidises (D27), -1 de-oxidises (D44)
     */
    @SuppressWarnings("unchecked")
    static void copperTick(DomainSession session, long now, int direction) {
        String opposite = direction > 0 ? "D44" : "D27";
        List<class_2338> candidates = session.state("copper_candidates", (List<class_2338>) null);
        if (candidates == null) {
            candidates = copperCandidates(session, direction);
            session.putState("copper_candidates", candidates);
            session.armSelf("copper_rescan", now);
            session.flag("copper_drained", false);
        } else if (session.flag("copper_drained")) {
            if (!session.dueSelf("copper_rescan", 20, now)) return;
            session.armSelf("copper_rescan", now);
            candidates = copperCandidates(session, direction);
            session.putState("copper_candidates", candidates);
            session.flag("copper_drained", false);
        }
        if (candidates.isEmpty()) {
            session.flag("copper_drained", true);
            return;
        }
        List<class_2338> slice = DomainWorldActions.slice(session, "copper", candidates, 64);
        for (class_2338 pos : slice) {
            class_2680 state = session.world.method_8320(pos);
            class_2680 next = DomainWorldActions.shiftOxidation(state, direction);
            if (next == null) continue;
            boolean conflict = false;
            for (DomainSession other : DomainSessionIndex.coveringBlock(session.world, pos)) {
                if (other == session) continue;
                if (other.hasCode("D27") && (com.innerdimension.domain.DomainModes.reverse(other,"D27")?-1:1)!=direction) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) {
                session.flag("copper_conflict:" + pos.method_10063(), true);
                continue;
            }
            DomainWorldActions.setBlock(session, pos, next, 3);
        }
        if (candidates.size() <= 64) session.flag("copper_drained", true);
    }

    private static List<class_2338> copperCandidates(DomainSession session, int direction) {
        List<class_2338> out = new ArrayList<>();
        for (class_2338 pos : session.blocksIn()) {
            class_2680 state = session.world.method_8320(pos);
            if (state.method_26215()) continue;
            if (DomainWorldActions.shiftOxidation(state, direction) == null) continue;
            out.add(pos);
        }
        return out;
    }

}

