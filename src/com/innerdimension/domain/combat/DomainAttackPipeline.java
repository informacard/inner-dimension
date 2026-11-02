package com.innerdimension.domain.combat;

import com.innerdimension.domain.ability.AttackHook;
import com.innerdimension.domain.ability.DomainAbilities;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

import java.util.*;

/**
 * Player melee entry point. Order (blueprint section 5.1): D04 animation replacement
 * terminates the hit; otherwise D10's bonus damage is merged into the very same damage
 * request, then D37/D58 statuses and the D09 mark are applied.
 */
public final class DomainAttackPipeline {
    private record Bonus(UUID attacker, UUID victim, long tick, float amount) {}

    private static final Map<UUID, Bonus> PENDING_BONUS = new HashMap<>();

    private DomainAttackPipeline() {}

    /** Total attack damage of the attacker's current weapon, for D10's floor calculation. */
    public static float attackDamage(class_1309 attacker) {
        class_1324 attribute = attacker.method_5996(class_5134.field_23717);
        return attribute == null ? 1.0f : (float) attribute.method_6201();
    }

    /**
     * Fabric melee callback body. Returning a non-PASS result cancels the vanilla attack.
     */
    public static boolean handleMelee(class_3222 attacker, class_1309 target) {
        DomainDamagePipeline.markAttack(attacker);
        if(com.innerdimension.boss.BossDomains.domainImmune(target))return false;
        DomainSession session = DomainSessionIndex.byOwner(attacker.method_5667());
        if (session == null || !session.stable() || session.world != attacker.method_73183()) return false;
        boolean consumed = false;
        float bonus = 0;
        float base = attackDamage(attacker);
        for (AttackHook hook : DomainAbilities.hooks(session, AttackHook.class)) {
            try {
                Boolean decision = hook.onOwnerMelee(session, attacker, target);
                if (Boolean.TRUE.equals(decision)) consumed = true;
                bonus += hook.bonusDamage(session, attacker, target, base);
            } catch (RuntimeException ignored) {
                // A broken hook must not break unrelated abilities on the same loadout.
            }
        }
        if (!consumed && bonus > 0) {
            PENDING_BONUS.put(target.method_5667(),
                    new Bonus(attacker.method_5667(), target.method_5667(), DomainDamagePipeline.now(), bonus));
        }
        if (!consumed) {
            DomainKnockbackPipeline.arm(attacker, target, attacker.method_5624());
        }
        return consumed;
    }

    /** Called from the damage() hook so the bonus lands inside the same damage request. */
    public static float consumeBonus(class_1309 victim, class_1282 source) {
        Bonus bonus = PENDING_BONUS.get(victim.method_5667());
        if (bonus == null || bonus.tick() != DomainDamagePipeline.now()) return 0;
        class_1297 attacker = source == null ? null : source.method_5529();
        if (attacker == null || !attacker.method_5667().equals(bonus.attacker())) return 0;
        PENDING_BONUS.remove(victim.method_5667());
        return bonus.amount();
    }

    public static void afterMelee(class_1309 victim, class_1282 source, float taken) {
        if(DomainDamagePipeline.autonomous())return;
        if (taken <= 0 || source == null || com.innerdimension.boss.BossDomains.domainImmune(victim)) return;
        class_1297 attacker = source.method_5529();
        if (!(attacker instanceof class_3222 player)) return;
        if (source.method_5526() != attacker) return;
        DomainSession session = DomainSessionIndex.byOwner(player.method_5667());
        if (session == null) return;
        for (AttackHook hook : DomainAbilities.hooks(session, AttackHook.class)) {
            try {
                hook.afterMelee(session, player, victim, taken);
            } catch (RuntimeException ignored) {
                // Best effort.
            }
        }
    }

    /** True when this melee hit came from the given player's own weapon. */
    public static boolean isPlayerMelee(class_1282 source, class_3222 player) {
        return source != null && source.method_5526() == player && source.method_5529() == player;
    }

    /** Arms the knockback intent for the D56/D48 mixin; consumed by the knockback call. */
    public static void armKnockback(class_3222 attacker, class_1309 target, boolean sprinting) {
        DomainKnockbackPipeline.arm(attacker, target, sprinting);
    }

    public static void clear() {
        PENDING_BONUS.clear();
        DomainKnockbackPipeline.clear();
    }
}
