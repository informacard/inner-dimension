package com.innerdimension.domain.combat;

import com.innerdimension.domain.ability.DomainAbilities;
import com.innerdimension.domain.ability.HealHook;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

/**
 * Healing is never turned into damage (D34/D59). Positive heals are scaled by the
 * strongest reduction among the covering sessions, and the real healed amount is
 * recorded so D12/D55 can tell healing from a no-op.
 */
public final class DomainHealingPipeline {
    private DomainHealingPipeline() {}

    public static float modify(class_1309 entity, float amount) {
        if (entity == null || amount <= 0) return amount;
        // Ability-driven healing must also respect enemy healing reduction, including
        // overlapping domains where one owner treats the target as friendly.
        float multiplier = com.innerdimension.boss.BossDomains.healingMultiplier(entity);
        for (DomainSession session : DomainSessionIndex.covering(entity))
            for (HealHook hook : DomainAbilities.hooks(session, HealHook.class)) {
                try {
                    multiplier = Math.min(multiplier, Math.max(0, hook.healingMultiplier(session, entity)));
                } catch (RuntimeException ignored) {
                    // A broken hook must never amplify healing.
                }
            }
        return amount * multiplier;
    }

    public static void after(class_1309 entity, float healed) {
        if (entity == null || healed <= 0) return;
        DomainDamagePipeline.markHeal(entity);
        for (DomainSession session : DomainSessionIndex.covering(entity))
            for (HealHook hook : DomainAbilities.hooks(session, HealHook.class)) {
                try {
                    hook.afterHealing(session, entity, healed);
                } catch (RuntimeException ignored) {
                    // Best effort.
                }
            }
    }

    /** Ability-driven heals use the same reduction pipeline as other healing. */
    public static void domainHeal(class_1309 entity, float amount) {
        if (entity == null || amount <= 0) return;
        entity.method_6025(amount);
    }
}
