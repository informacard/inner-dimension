package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D53 minecraft:piglin (yellow) — 黄金停战 / golden truce.
 *
 * <p>Any living entity with a golden armour piece in an equipment slot is truce-eligible:
 * ordinary hostile AI may neither select it as a target nor resolve a melee hit on it. As soon
 * as such an entity itself lands an attack it loses the truce for 100 ticks — recorded with a
 * per-target session timer, so swapping armour on and off cannot reset it early.</p>
 *
 * <p>Players are never controlled: a player attacker is exempt from {@code denyOwnAttack}, so
 * the ability only ever re-points or refuses mob AI. Permanent mob hatred memory is untouched,
 * and the mob's current target is dropped by {@code DomainAiPolicy}, not by us.</p>
 */
public final class D53 implements DomainAbility, AttackHook, AiHook, DamageHook {
    public static final D53 INSTANCE = new D53();
    private D53() {}
    @Override public String id() { return "minecraft:piglin"; }
    @Override public String code() { return "D53"; }
    @Override public int priority() { return 100; }

    private static final String LOST_KEY = "truce_lost";
    private static final long LOST_TICKS = 100;

    /** TRUE while the entity has not attacked within the truce window. */
    private static boolean truceActive(DomainSession session, class_1309 entity, long now) {
        return session.due(LOST_KEY, entity.method_5667(), LOST_TICKS, now);
    }

    @Override
    public Boolean denyTarget(DomainSession session, class_1308 mob, class_1309 target) {
        if (mob == null || target == null || !com.innerdimension.domain.runtime.DomainObjects.matches(session,"D53",target)) return null;
        if (!D47D69Support.goldenArmour(target)) return null;
        if (!truceActive(session, target, DomainDamagePipeline.now())) return null;
        return Boolean.TRUE;
    }

    @Override
    public boolean denyOwnAttack(DomainSession session, class_1309 attacker, class_1309 target) {
        if (attacker == null || target == null || !com.innerdimension.domain.runtime.DomainObjects.matches(session,"D53",target)) return false;
        if (attacker instanceof class_1657) return false; // never control players
        if (!D47D69Support.goldenArmour(target)) return false;
        return truceActive(session, target, DomainDamagePipeline.now());
    }

    @Override
    public void onTick(DomainSession s, long now) {
        // The truce holder loses protection by attacking, from wherever it attacked; the
        // after-damage record only sees hits whose victim was itself inside a domain.
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D53")) {
            if (!D47D69Support.goldenArmour(entity)) continue;
            long lastAttack = DomainDamagePipeline.lastAttack(entity.method_5667());
            if (lastAttack <= now - 1 || lastAttack > now) continue;
            s.arm(LOST_KEY, entity.method_5667(), now);
        }
    }

    @Override
    public void afterDamage(DomainSession session, class_1309 victim, class_1282 source,
                            float baseAmount, float taken, boolean blocked) {
        if (taken <= 0 || source == null) return;
        // The truce holder loses its protection by attacking, not by being attacked.
        class_1297 raw = source.method_5529();
        if (!(raw instanceof class_1309 attacker) || attacker == victim) return;
        if (!D47D69Support.goldenArmour(attacker)) return;
        session.arm(LOST_KEY, attacker.method_5667(), DomainDamagePipeline.now());
    }
}
