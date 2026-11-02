package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D63 minecraft:guardian (orange) — 棘甲 / thorn armour.
 *
 * <p>When an ally really loses health to a melee attacker, {@code min(taken * 0.3, 6)} is
 * reflected back with the dedicated {@code inner_dimension:domain_reflected} category. The
 * damage pipeline excludes that category from the reaction passes, so the reflection can never
 * re-trigger D60, D63 or D55. A 10 tick per-(victim, attacker) timer limits the rate, and a hit
 * that D04 cancelled has {@code taken == 0} and reflects nothing.</p>
 */
public final class D63 implements DomainAbility, DamageHook {
    public static final D63 INSTANCE = new D63();
    private D63() {}
    @Override public String id() { return "minecraft:guardian"; }
    @Override public String code() { return "D63"; }
    @Override public int priority() { return 100; }

    private static final double FRACTION = 0.3;
    private static final float MAX_REFLECT = 6.0f;
    private static final long COOLDOWN = 10;

    @Override
    public void afterDamage(DomainSession session, class_1309 victim, class_1282 source,
                            float baseAmount, float taken, boolean blocked) {
        if (taken <= 0 || source == null) return;
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D63",victim)) return;
        class_1297 raw = source.method_5529();
        if (!(raw instanceof class_1309 attacker) || attacker == victim) return;
        // Melee only: the direct source of the damage is the attacker's own body.
        if (source.method_5526() != attacker) return;
        if (DomainDamageTypes.isDomainDamage(source)) return;
        long now = DomainDamagePipeline.now();
        String key = "d63_" + victim.method_5667();
        if (!session.due(key, attacker.method_5667(), COOLDOWN, now)) return;
        session.arm(key, attacker.method_5667(), now);
        float amount = Math.min(Math.max(0f, taken) * (float) FRACTION, MAX_REFLECT);
        if (amount <= 0) return;
        DomainDamagePipeline.hurt(attacker, session.owner(), DomainDamageTypes.REFLECTED, amount);
    }
}
