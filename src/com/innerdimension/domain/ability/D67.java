package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D67 minecraft:shulker (purple) — 失重囚笼 / weightless cage.
 *
 * <p>Ordinary enemies inside the interior carry a Levitation I lease, and every tick the top of
 * the entity is capped against the mixed real+projection ceiling: while less than 0.5 blocks of
 * headroom remain, only the upward velocity component is truncated, so gravity, horizontal
 * knockback and suffocation behaviour are untouched. The levitation effect itself is refreshed
 * through the lease manager, so an external instance survives.</p>
 *
 * <p>Bosses never float. They receive a -30% movement-speed attribute lease under a modifier id
 * shared with D47, so vanilla's one modifier slot naturally arbitrates the pair at -30%.</p>
 */
public final class D67 implements DomainAbility {
    public static final D67 INSTANCE = new D67();
    private D67() {}
    @Override public String id() { return "minecraft:shulker"; }
    @Override public String code() { return "D67"; }
    @Override public int priority() { return 100; }

    private static final String SLOW_KEY = "d67_slowed";
    private static final double MIN_HEADROOM = 0.5;
    private static final double CEILING_PROBE = 4.0;
    private static final int LEVITATION_REFRESH = 40;

    @Override
    public void onTick(DomainSession s, long now) {
        Set<UUID> slowed = new HashSet<>();
        class_6880<class_1320> speed = D47D69Support.attribute("minecraft:movement_speed");
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D67")) {
            DomainRelations.Relation relation = DomainRelations.of(s, entity);
            if (relation == DomainRelations.Relation.BOSS) {
                if (speed != null) {
                    AttributeLeaseManager.apply(entity, speed, D47.BOSS_SLOW_MODIFIER, -0.30,
                            class_1322.class_1323.field_6331, true, s.sessionId, "D67");
                    slowed.add(entity.method_5667());
                }
                continue;
            }
            
            EffectLeaseManager.apply(entity, "minecraft:levitation", "D67", s.sessionId,
                    LEVITATION_REFRESH, 0, now);
            capBelowCeiling(s, entity);
        }
        Object previous = s.state(SLOW_KEY, null);
        if (speed == null || !(previous instanceof Set<?> set)) {
            if (speed != null) s.putState(SLOW_KEY, slowed);
            return;
        }
        for (Object value : set) {
            if (!(value instanceof UUID uuid) || slowed.contains(uuid)) continue;
            class_1309 entity = D47D69Support.living(s.world, uuid);
            if (entity != null) AttributeLeaseManager.apply(entity, speed, D47.BOSS_SLOW_MODIFIER, 0,
                    class_1322.class_1323.field_6331, false, s.sessionId, "D67");
        }
        s.putState(SLOW_KEY, slowed);
    }

    /** Only the upward component is truncated; nothing is pushed into a block. */
    private static void capBelowCeiling(DomainSession s, class_1309 entity) {
        class_238 box = entity.method_5829();
        double ceiling = DomainGeometry.ceilingBottom(s.world, box, CEILING_PROBE);
        if (Double.isNaN(ceiling)) return;
        if (ceiling - box.field_1325 >= MIN_HEADROOM) return;
        class_243 velocity = entity.method_18798();
        if (velocity.field_1351 > 0) entity.method_18799(new class_243(velocity.field_1352, 0, velocity.field_1350));
    }

    @Override
    public void onEnd(DomainSession s) {
        class_6880<class_1320> speed = D47D69Support.attribute("minecraft:movement_speed");
        Object previous = s.state(SLOW_KEY, null);
        if (speed == null || !(previous instanceof Set<?> set)) return;
        for (Object value : set) {
            if (!(value instanceof UUID uuid)) continue;
            class_1309 entity = D47D69Support.living(s.world, uuid);
            if (entity != null) AttributeLeaseManager.apply(entity, speed, D47.BOSS_SLOW_MODIFIER, 0,
                    class_1322.class_1323.field_6331, false, s.sessionId, "D67");
        }
    }
}
