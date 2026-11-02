package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * D45 minecraft:ravager (neutral) — "震地".
 *
 * <p>Every enemy standing on a surface — real or projected, measured with
 * {@code DomainGeometry.supportDistance} and never with {@code onGround} — with its feet no
 * more than 0.25 blocks above that surface starts a 40 tick timer; when it expires the enemy
 * takes 2 points of {@code inner_dimension:domain_ground_shock}.</p>
 *
 * <p>Jumping, floating (D67's levitation included) or losing the support surface clears both
 * the timer and the qualification, so the next landing always waits a fresh full interval and
 * no blocks are ever broken.</p>
 */
public final class D45 implements DomainAbility {
    public static final D45 INSTANCE = new D45();
    private D45() {}
    @Override public String id() { return "minecraft:ravager"; }
    @Override public String code() { return "D45"; }

    @Override
    public void onTick(DomainSession s, long now) {
        class_3222 owner = s.owner();
        for (class_1309 enemy : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D45")) {
            double distance = DomainGeometry.supportDistance(s.world, enemy.method_5829(), 0.45);
            boolean grounded = !Double.isNaN(distance) && distance <= 0.25;
            if (!grounded) {
                D24D46Support.forgetQualification(s, "d45", enemy.method_5667());
                continue;
            }
            if (!D24D46Support.periodReady(s, "d45", enemy.method_5667(), 40, now)) continue;
            if (DomainDamagePipeline.hurt(enemy, owner, DomainDamageTypes.GROUND_SHOCK, 2f)) {
                class_243 velocity = enemy.method_18798();
                double resistance = enemy.method_45325(class_5134.field_23718);
                enemy.method_18799(velocity.method_1031(0, 0.55 * Math.max(0, 1 - resistance), 0));
                enemy.field_6037 = true;
            }
        }
    }
}
