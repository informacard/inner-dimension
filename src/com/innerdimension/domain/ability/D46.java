package com.innerdimension.domain.ability;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;
/** The nearest moving enemy is tracked by a vertical sonic strike emitted from the ceiling shell. */
public final class D46 implements DomainAbility {
    public static final D46 INSTANCE = new D46();
    private D46() {}
    @Override public String id() { return "minecraft:warden"; }
    @Override public String code() { return "D46"; }
    private static final long STRIKE_INTERVAL = 20;
    private static final float DAMAGE = 4f;
    @Override public void onTick(DomainSession session, long now) {
        if (!(session.world instanceof class_3218 world)) return;
        Map<UUID, class_243> positions = positions(session);
        Set<UUID> present = new HashSet<>();
        class_3222 owner = session.owner();
        class_1309 nearest = owner == null ? null : com.innerdimension.domain.runtime.DomainObjects.nearest(session,"D46", owner.method_73189());
        for (class_1309 enemy : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D46")) {
            UUID id = enemy.method_5667();
            present.add(id);
            class_243 current = enemy.method_73189();
            class_243 previous = positions.put(id, current);
            if (enemy != nearest || !moved(previous, current) || !session.dueSelf("d46_sonic", STRIKE_INTERVAL, now)) continue;
            session.armSelf("d46_sonic", now);
            class_243 head = enemy.method_33571();
            class_243 source = ceilingSource(session.maxY(), head);
            // Sample downward at the target's current X/Z, so every new strike tracks it vertically.
            int count = Math.max(1, (int) Math.ceil(Math.max(0, source.field_1351 - head.field_1351)));
            for (int i = 0; i <= count; i++) {
                double y = source.field_1351 + (head.field_1351 - source.field_1351) * i / count;
                world.method_65096(class_2398.field_38908, source.field_1352, y, source.field_1350,
                        1, 0, 0, 0, 0);
            }
            world.method_43128(null, source.field_1352, source.field_1351, source.field_1350,
                    class_3417.field_38830, class_3419.field_15251, 2f, 1f);
            DomainDamagePipeline.hurt(enemy, session.owner(), "minecraft:sonic_boom", DAMAGE);
        }
        positions.keySet().retainAll(present);
    }
    /** Actual displacement detects walking, crouching, swimming, jumps and external pushes. */
    public static boolean moved(class_243 previous, class_243 current) {
        return previous != null && current.method_1020(previous).method_1027() > 1.0E-8;
    }
    public static class_243 ceilingSource(double ceiling, class_243 targetHead) {
        return new class_243(targetHead.field_1352, ceiling, targetHead.field_1350);
    }
    @SuppressWarnings("unchecked")
    private static Map<UUID, class_243> positions(DomainSession session) {
        Map<UUID, class_243> positions = session.state("d46_positions", null);
        if (positions == null) {
            positions = new HashMap<>();
            session.putState("d46_positions", positions);
        }
        return positions;
    }
    @Override public void onEnd(DomainSession session) { session.putState("d46_positions", null); }
}
