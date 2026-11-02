package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D15 minecraft:horse (orange).
 *
 * <p>Rear hoof: every tick the owner looks for enemies in the rear half-circle,
 * within 4.5 blocks and reachable by a mixed real + projected ray. Each target has its own
 * 1.5 second cooldown. When the 6 point hoof damage really lands (not cancelled by D04 or an
 * immunity) the target also gets 0.8 blocks/tick of horizontal velocity away from the
 * owner, clipped to the domain boundary. This knockback never enters the player knockback
 * pipeline, so D56/D48 do not touch it.</p>
 */
public final class D15 implements DomainAbility {
    public static final D15 INSTANCE = new D15();
    private D15() {}
    @Override public String id() { return "minecraft:horse"; }
    @Override public String code() { return "D15"; }

    public static final double RANGE = 4.5;
    public static final float DAMAGE = 6.0f;
    private static final double PUSH = 0.8;
    public static final long COOLDOWN = 30;
    private static final String TIMER = "d15_hoof";

    public static boolean rearTarget(class_243 facing, class_243 offset) {
        double distance = Math.hypot(offset.field_1352, offset.field_1350);
        return distance > 1.0E-6 && distance <= RANGE
                && facing.field_1352 * offset.field_1352 + facing.field_1350 * offset.field_1350 <= 0;
    }

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        class_3222 owner = session.owner();
        if (owner == null) return;
        // Horizontal facing must still work when the owner looks straight up or down.
        double yaw = Math.toRadians(owner.method_36454());
        class_243 flatLook = new class_243(-Math.sin(yaw), 0, Math.cos(yaw));

        for (class_1309 target : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D15")) {
            if (target.method_5667().equals(session.ownerId)) continue;
            if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D15",target)) continue;
            if (target.method_5829().field_1322 > owner.method_5829().field_1325 + .5
                    || target.method_5829().field_1325 < owner.method_5829().field_1322 - .5) continue;
            class_243 toTarget = new class_243(target.method_23317() - owner.method_23317(), 0,
                    target.method_23321() - owner.method_23321());
            if (!rearTarget(flatLook, toTarget)) continue;
            if (!DomainGeometry.lineOfSight(session.world, session, owner.method_33571(), target.method_33571()))
                continue;
            if (!session.due(TIMER, target.method_5667(), COOLDOWN, now)) continue;

            boolean landed = DomainDamagePipeline.hurtFromOwner(target, owner, DomainDamageTypes.HOOF, DAMAGE);
            // The cooldown is consumed by the attempt so a cancelled hit waits a full interval.
            session.arm(TIMER, target.method_5667(), now);
            if (!landed) continue;                   // D04 or an immunity: no knockback either

            class_243 push = DomainGeometry.horizontalAway(owner, target, PUSH);
            push = DomainGeometry.clampInside(session, target, push);
            target.method_18799(target.method_18798().method_1019(push));
            target.field_6037 = true;
        }
    }

    @Override
    public void onEnd(DomainSession session) {
        // Session-scoped cooldowns only.
    }
}
