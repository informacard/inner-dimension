package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * D39 minecraft:slime (green) — "弹性地面".
 *
 * <p>All FALL damage inside the domain is cancelled in the pre-damage chain. Landing is
 * detected with the mixed support surface ({@code DomainGeometry.supportTop} over real and
 * projected blocks), never with {@code onGround}: on the falling-to-supported edge, and while
 * the entity is not sneaking, the entity is pushed back up at 0.42 blocks/tick. Each
 * successive bounce in one landing sequence is halved, the sequence resets after 40 ticks of
 * no bouncing, and a ceiling closer than the bounce height stops it.</p>
 */
public final class D39 implements DomainAbility, DamageHook {
    public static final D39 INSTANCE = new D39();
    private D39() {}
    @Override public String id() { return "minecraft:slime"; }
    @Override public String code() { return "D39"; }

    /** Runs after D04/D50/D40 in the pre-damage chain (blueprint section 5.3). */
    @Override public int priority() { return 50; }

    @Override
    public Boolean preDamage(DomainSession s, class_1309 victim, class_1282 source, float amount) {
        // TRUE cancels: DomainDamagePipeline.pre() stops at the first non-null decision and
        // TRUE means "no damage" (see DamageHook.preDamage).
        return com.innerdimension.domain.runtime.DomainObjects.matches(s,"D39",victim)&&DomainDamageTypes.isFall(source) ? Boolean.TRUE : null;
    }

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D39")) {
            if (entity == null || entity.method_7325() || !entity.method_5805()) continue;
            String uuid = entity.method_5667().toString();
            class_243 velocity = entity.method_18798();
            class_238 box = entity.method_5829();
            double top = DomainGeometry.supportTop(s.world, box, 0.35);
            boolean supported = !Double.isNaN(top) && box.field_1322 - top <= 0.06;

            if (supported && velocity.field_1351 <= 0.001) {
                if (!s.flag("d39_falling:" + uuid)) continue;
                s.flag("d39_falling:" + uuid, false);
                if (entity.method_5715()) continue;
                bounce(s, entity, box, velocity, uuid, now);
            } else if (velocity.field_1351 < -0.05) {
                s.flag("d39_falling:" + uuid, true);
            }
        }
    }

    private static void bounce(DomainSession s, class_1309 entity, class_238 box,
                               class_243 velocity, String uuid, long now) {
        Long last = s.state("d39_last:" + uuid, (Long) null);
        int level = s.state("d39_bounce:" + uuid, Integer.valueOf(0));
        if (last == null || now - last > 40) level = 0;
        double wanted = 0.42 * Math.pow(0.5, level);
        double ceiling = DomainGeometry.ceilingBottom(s.world, box, 0.6);
        if (!Double.isNaN(ceiling) && ceiling - box.field_1325 < wanted + 0.05) return;
        if (box.field_1325 + wanted > s.maxY()) return;
        entity.method_18799(new class_243(velocity.field_1352, wanted, velocity.field_1350));
        entity.field_6037 = true;
        s.putState("d39_bounce:" + uuid, Integer.valueOf(level + 1));
        s.putState("d39_last:" + uuid, Long.valueOf(now));
    }
}
