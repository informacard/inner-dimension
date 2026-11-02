package com.innerdimension.domain.ai;

import com.innerdimension.domain.ability.AiHook;
import com.innerdimension.domain.ability.AttackHook;
import com.innerdimension.domain.ability.DomainAbilities;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

import java.util.*;

/**
 * The only place that answers "may this mob target / move / attack". Abilities never set
 * the NoAI flag and never blindly overwrite a pathfinder target; they register an intent
 * here and the navigation and target-selector mixins consult it.
 */
public final class DomainAiPolicy {
    private DomainAiPolicy() {}

    /** TRUE when any covering session forbids this mob from choosing the target. */
    public static boolean denyTarget(class_1308 mob, class_1309 target) {
        if (mob == null || target == null) return false;
        for (DomainSession session : DomainSessionIndex.covering(mob)) {
            if (target.method_5667().equals(session.ownerId)) {
                // Nothing in a mob's own domain may single out the owner unless it is an enemy of it.
                if (DomainRelations.isFriendly(session, mob)) return true;
            }
            for (AiHook hook : DomainAbilities.hooks(session, AiHook.class)) {
                try {
                    if (Boolean.TRUE.equals(hook.denyTarget(session, mob, target))) return true;
                } catch (RuntimeException ignored) {
                    // An AI hook failure must not freeze the mob.
                }
            }
        }
        for (DomainSession session : DomainSessionIndex.covering(target)) {
            if (mob.method_5667().equals(session.ownerId)) continue;
            for (AiHook hook : DomainAbilities.hooks(session, AiHook.class)) {
                try {
                    if (Boolean.TRUE.equals(hook.denyTarget(session, mob, target))) return true;
                } catch (RuntimeException ignored) {
                    // As above.
                }
            }
        }
        return false;
    }

    /** TRUE when the mob is forbidden from committing active movement this tick (D47). */
    public static boolean denyMove(class_1308 mob) {
        if (mob == null) return false;
        for (DomainSession session : DomainSessionIndex.covering(mob))
            for (AiHook hook : DomainAbilities.hooks(session, AiHook.class)) {
                try {
                    if (hook.denyMove(session, mob)) return true;
                } catch (RuntimeException ignored) {
                    // As above.
                }
            }
        return false;
    }

    /** TRUE when this entity's own melee must be refused (D53 truce, owner safety). */
    public static boolean denyAttack(class_1309 attacker, class_1309 target) {
        if (attacker == null || target == null) return false;
        for (DomainSession session : DomainSessionIndex.covering(attacker)) {
            for (AttackHook hook : DomainAbilities.hooks(session, AttackHook.class)) {
                try {
                    if (hook.denyOwnAttack(session, attacker, target)) return true;
                } catch (RuntimeException ignored) {
                    // As above.
                }
            }
            if (target.method_5667().equals(session.ownerId) && DomainRelations.isFriendly(session, attacker))
                return true;
        }
        return false;
    }

    /**
     * Preferred targets a session pushes this mob towards, in order (D09 mark, D55 latest
     * attacker). Only used by abilities that own that mob's behaviour.
     */
    public static List<class_1309> preferred(class_1308 mob) {
        if (mob == null || DomainSessionIndex.covering(mob).isEmpty()) return List.of();
        List<class_1309> out = new ArrayList<>();
        for (DomainSession session : DomainSessionIndex.covering(mob))
            for (AiHook hook : DomainAbilities.hooks(session, AiHook.class)) {
                try {
                    for (class_1309 candidate : hook.preferredTargets(session, mob))
                        if (candidate != null && candidate.method_5805() && !out.contains(candidate)) out.add(candidate);
                } catch (RuntimeException ignored) {
                    // As above.
                }
            }
        return out;
    }

    /** Applies the best available preferred target without fighting the mob's own AI. */
    public static boolean pushPreferredTarget(class_1308 mob) {
        List<class_1309> preferred = preferred(mob);
        if (preferred.isEmpty()) return false;
        for (class_1309 candidate : preferred) {
            if (candidate != mob && !denyTarget(mob, candidate)) {
                mob.method_5980(candidate);
                // Brain-driven mobs read ATTACK_TARGET rather than MobEntity.target.
                mob.method_18868().method_18878(class_4140.field_22355, candidate);
                return true;
            }
        }
        return false;
    }
}
