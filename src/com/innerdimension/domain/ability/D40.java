package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * D40 minecraft:enderman (purple) — "闪避".
 *
 * <p>At the pre-damage entry — before anything else about the hit resolves — a projectile
 * aimed at the session owner is dodged once per 40 ticks (a session-scoped cooldown, so
 * switching slots or reopening the UI cannot refresh it). The hit is fully cancelled: the
 * damage and every rider effect are dropped, the projectile entity is removed so it cannot
 * stick, remain in flight or apply a second time, and ender particles are played in place.
 * The owner is not moved at all.</p>
 *
 * <p>Only enemy projectiles are dodged, and only against the owner. Because the chain stops
 * at the first non-null decision, a D04 cancellation that runs first consumes the hit without
 * spending D40's cooldown.</p>
 */
public final class D40 implements DomainAbility, DamageHook {
    public static final D40 INSTANCE = new D40();
    private D40() {}
    @Override public String id() { return "minecraft:enderman"; }
    @Override public String code() { return "D40"; }

    /** After D04 and D50, before D51/D52/D39 (blueprint section 5.3). */
    @Override public int priority() { return 40; }
    private static final String TIMER = "d40";
    private static final long COOLDOWN = 40;

    @Override
    public Boolean preDamage(DomainSession s, class_1309 victim, class_1282 source, float amount) {
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(s,"D40",victim)) return null;
        if (source == null || !DomainDamageTypes.isProjectile(source)) return null;
        class_1297 direct = source.method_5526();
        if (direct == null) return null;
        class_1297 attacker = source.method_5529();
        if (attacker == victim) return null;
        long now = DomainDamagePipeline.now();
        if (!s.due(TIMER, victim.method_5667(), COOLDOWN, now)) return null;
        s.arm(TIMER, victim.method_5667(), now);
        particles(s, victim);
        // Removing the projectile is what makes the dodge free of side effects: a cancelled
        // damage request would still let a tipped arrow apply its effect riders and stick.
        // A thrown trident is left in place instead, because discarding it would destroy a
        // loyalty trident that should return to its owner; for it the cancellation alone is
        // the miss.
        if (discardProjectile(direct)) direct.method_31472();
        // TRUE cancels the hit; see DamageHook.preDamage.
        return Boolean.TRUE;
    }

    /** Virtual slashes use the caster as direct source; never discard a living attacker. */
    public static boolean discardProjectile(class_1297 direct) {
        return direct instanceof class_1676 && !(direct instanceof class_1685);
    }

    public static long remainingCooldown(DomainSession session, long now) {
        return session.remaining(TIMER, session.ownerId, COOLDOWN, now);
    }

    private static void particles(DomainSession s, class_1309 victim) {
        if (!(s.world instanceof class_3218 world)) return;
        world.method_65096(class_2398.field_11214,
                victim.method_23317(), victim.method_23318() + victim.method_5829().method_17940() * 0.5,
                victim.method_23321(), 24, 0.4, 0.5, 0.4, 0.05);
    }
}
