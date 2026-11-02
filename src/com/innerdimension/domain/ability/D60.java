package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.projectile.DomainProjectileState;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D60 minecraft:llama (yellow) — 反吐 / counter-spit.
 *
 * <p>When an ally really loses health to a hostile entity and the mixed real+projection ray
 * between them is unobstructed, the victim spits back: one vanilla {@code minecraft:llama_spit}
 * projectile is created and launched at the attacker, and the victim's own 40 tick timer is
 * armed. Walls therefore block the reaction instead of letting damage pass through them. All
 * domain damage and reflected hits are ignored, so the reaction can never re-trigger itself or
 * D55/D63.</p>
 *
 * <p>The projectile's on-hit payload (2 damage of {@code inner_dimension:domain_spit} plus
 * Slowness I for 60 ticks) is the projectile teammate's integration point: this class binds the
 * projectile to the session with {@link DomainProjectileState} and records the intended target,
 * which that hit handler consumes. {@link #shouldSpit} is the standalone eligibility check the
 * integration can call before spawning if it owns the spawn instead.</p>
 */
public final class D60 implements DomainAbility, DamageHook {
    public static final D60 INSTANCE = new D60();
    private D60() {}
    @Override public String id() { return "minecraft:llama"; }
    @Override public String code() { return "D60"; }
    @Override public int priority() { return 100; }

    private static final String TIMER_KEY = "d60";
    private static final long COOLDOWN = 40;
    private static final String SPIT_TYPE = "minecraft:llama_spit";
    /** Aim speed of the reaction projectile; the payload is applied by the projectile handler. */
    private static final double SPIT_SPEED = 1.5;

    @Override
    public void afterDamage(DomainSession session, class_1309 victim, class_1282 source,
                            float baseAmount, float taken, boolean blocked) {
        if (taken <= 0 || source == null) return;
        // Reflected and domain-generated damage must never re-enter the reaction chain.
        if (DomainDamageTypes.isReflected(source) || DomainDamageTypes.isDomainDamage(source)) return;
        class_1297 raw = source.method_5529();
        if (!(raw instanceof class_1309 attacker)) return;
        long now = DomainDamagePipeline.now();
        if (!shouldSpit(session, victim, raw, now)) return;
        session.arm(TIMER_KEY, victim.method_5667(), now);
        spawnSpit(session, victim, attacker);
    }

    /**
     * Eligibility only: ally victim, hostile damage source entity, unobstructed mixed ray and
     * the victim's own 40 tick timer is due. Does not arm the timer.
     */
    public static boolean shouldSpit(DomainSession session, class_1309 victim, class_1297 attacker, long now) {
        if (session == null || victim == null || attacker == null) return false;
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D60",victim)) return false;
        if (!(attacker instanceof class_1309)) return false;
        if (attacker == victim) return false;
        if (!session.due(TIMER_KEY, victim.method_5667(), COOLDOWN, now)) return false;
        return DomainGeometry.raycast(session.world, session, victim.method_33571(),
                attacker.method_33571(), false, false) == null;
    }

    private static void spawnSpit(DomainSession session, class_1309 victim, class_1309 attacker) {
        class_243 origin = victim.method_33571();
        class_1297 created = D47D69Support.create(session.world, SPIT_TYPE,
                origin.field_1352, origin.field_1351 - 0.1, origin.field_1350);
        if (!(created instanceof class_1676 spit)) return;
        spit.method_7432(victim); // ownership transfer: the victim fired it
        class_243 direction = DomainGeometry.direction(victim.method_33571(), attacker.method_33571());
        spit.method_18799(direction.method_1021(SPIT_SPEED));
        if (!(session.world instanceof class_3218 server)) return;
        server.method_8649(spit);
        DomainProjectileState.State state = DomainProjectileState.bind(spit, session.sessionId,
                session.ownerId, "D60", DomainDamagePipeline.now());
        state.target = attacker.method_5667();
    }
}
