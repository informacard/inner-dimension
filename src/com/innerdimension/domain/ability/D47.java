package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D47 minecraft:creaking (yellow) — 注视禁行 / gaze lock.
 *
 * <p>While the owner looks at an ordinary enemy inside the fixed interior and the mixed
 * real+projection ray between their eye positions is unobstructed, that enemy's active
 * movement intent is refused. Physics velocity, gravity, levitation, external knockback and
 * in-place attacks are untouched: the navigation mixin owned by the navigation teammate calls
 * {@code DomainAiPolicy.denyMove}, this class only answers the hook. No AI flag is ever set
 * and the mob's target is left alone, so it keeps attacking from where it stands; once the
 * gaze breaks the mixin resumes the mob's original path.</p>
 *
 * <p>Bosses are never pinned: they receive a -30% movement-speed attribute lease instead.</p>
 */
public final class D47 implements DomainAbility, AiHook {
    public static final D47 INSTANCE = new D47();
    private D47() {}
    @Override public String id() { return "minecraft:creaking"; }
    @Override public String code() { return "D47"; }
    @Override public int priority() { return 100; }

    /** Blueprint: 70 degrees total, i.e. 35 degrees either side of the look vector. */
    private static final double HALF_ANGLE_DEGREES = 35.0;
    private static final String GAZE_KEY = "d47_gaze";
    private static final String SLOW_KEY = "d47_slowed";
    static final String BOSS_SLOW_MODIFIER = "boss_domain_slow";

    @Override
    public void onTick(DomainSession s, long now) {
        class_3222 owner = s.owner();
        if (owner != null && s.dueSelf("d47_gaze_scan", 2, now)) {
            s.armSelf("d47_gaze_scan", now);
            Object prior = s.state(GAZE_KEY,null);
            Set<UUID> locked = new HashSet<>();
            for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D47")) {
                if (!(entity instanceof class_1308 mob)) continue;
                if (DomainRelations.isBoss(mob)) continue;
                if (DomainGeometry.viewAngle(owner, mob) > HALF_ANGLE_DEGREES) continue;
                if (DomainGeometry.raycast(s.world, s, owner.method_33571(), mob.method_33571(),
                        false, false) != null) continue;
                locked.add(mob.method_5667());
                if (!(prior instanceof Set<?> old && old.contains(mob.method_5667()))
                        && now - com.innerdimension.domain.combat.DomainDamagePipeline.lastHurt(mob.method_5667()) > 2) {
                    class_243 velocity = mob.method_18798();
                    mob.method_18799(new class_243(0,velocity.field_1351,0));
                }
            }
            s.putState(GAZE_KEY, locked);
        }
        applyBossSlow(s);
    }

    /** Bosses take the speed penalty instead of being pinned; ordinary enemies take neither. */
    private void applyBossSlow(DomainSession s) {
        class_6880<class_1320> speed = D47D69Support.attribute("minecraft:movement_speed");
        if (speed == null) return;
        Set<UUID> slowed = new HashSet<>();
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D47")) {
            if (!(entity instanceof class_1308 mob)) continue;
            if (DomainRelations.of(s, mob) != DomainRelations.Relation.BOSS) continue;
            AttributeLeaseManager.apply(mob, speed, BOSS_SLOW_MODIFIER, -0.30,
                    class_1322.class_1323.field_6331, true, s.sessionId, "D47");
            slowed.add(mob.method_5667());
        }
        Object previous = s.state(SLOW_KEY, null);
        if (previous instanceof Set<?> set)
            for (Object value : set) {
                if (!(value instanceof UUID uuid) || slowed.contains(uuid)) continue;
                class_1309 entity = D47D69Support.living(s.world, uuid);
                if (entity != null) AttributeLeaseManager.apply(entity, speed, BOSS_SLOW_MODIFIER, 0,
                        class_1322.class_1323.field_6331, false, s.sessionId, "D47");
            }
        s.putState(SLOW_KEY, slowed);
    }

    @Override
    public boolean denyMove(DomainSession session, class_1308 mob) {
        if (mob == null) return false;
        if (DomainRelations.isBoss(mob)||!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D47",mob)) return false;
        Object stored = session.state(GAZE_KEY, null);
        return stored instanceof Set<?> set && set.contains(mob.method_5667());
    }

    @Override
    public void onEnd(DomainSession s) {
        class_6880<class_1320> speed = D47D69Support.attribute("minecraft:movement_speed");
        Object previous = s.state(SLOW_KEY, null);
        if (speed == null || !(previous instanceof Set<?> set)) return;
        for (Object value : set) {
            if (!(value instanceof UUID uuid)) continue;
            class_1309 entity = D47D69Support.living(s.world, uuid);
            if (entity != null) AttributeLeaseManager.apply(entity, speed, BOSS_SLOW_MODIFIER, 0,
                    class_1322.class_1323.field_6331, false, s.sessionId, "D47");
        }
    }
}
