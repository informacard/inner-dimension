package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.projectile.DomainProjectileState;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

import java.util.*;

/** Owner-fired projectiles home on enemies unless sneaking. Vanilla resolves impact damage.
 * Arrows retain the existing interior-block nudge; other projectiles keep vanilla block impacts. */
public final class D31 implements DomainAbility {
    public static final D31 INSTANCE = new D31();
    private D31() {}
    @Override public String id() { return "minecraft:skeleton"; }
    @Override public String code() { return "D31"; }

    private static final double DEFAULT_SPEED = 2.0;

    @Override
    public void onTick(DomainSession s, long now) {
        class_3222 owner = s.owner();
        if (owner == null) return;
        List<class_1676> arrows = s.world.method_8390(class_1676.class, s.bounds(6), arrow -> !(arrow instanceof class_1536));
        for (class_1676 arrow : arrows) {
            if (arrow.method_7325() || !arrow.method_5805()) continue;
            class_1297 shooter = arrow.method_24921();
            if (shooter == null || !shooter.method_5667().equals(s.ownerId)) continue;
            DomainProjectileState.State state = DomainProjectileState.peek(arrow);
            boolean bound = state != null && s.sessionId.equals(state.sessionId)
                    && "D31".equals(state.code) && !state.consumed;
            if (owner.method_5715()) {
                if (bound) state.homing = false;
                continue;
            }
            if (!bound) {
                // Arrows this expansion never took over are left completely alone.
                if (!s.contains(arrow)) continue;
                state = DomainProjectileState.bind(arrow, s.sessionId, s.ownerId, "D31", now);
            }
            state.homing = true;
            steer(s, arrow, state, now);
        }
    }

    private static void steer(DomainSession s, class_1676 arrow, DomainProjectileState.State state, long now) {
        class_1309 target = null;
        if (state.target != null) {
            class_1309 current = D24D46Support.livingByUuid(s.world, s.bounds(16), state.target);
            if (current != null && s.contains(current) && com.innerdimension.domain.runtime.DomainObjects.matches(s,"D31",current)) target = current;
        }
        if (target == null) {
            target = com.innerdimension.domain.runtime.DomainObjects.nearest(s,"D31", arrow.method_73189());
            state.target = target == null ? null : target.method_5667();
        }

        class_243 from = arrow.method_73189();
        class_243 velocity = arrow.method_18798();
        String speedKey = "d31_speed:" + arrow.method_5667();
        double speed = s.state(speedKey, Double.valueOf(0.0));
        if (velocity.method_1033() > speed) {
            speed = velocity.method_1033();
            s.putState(speedKey, speed);
        }
        if (speed < 0.1) speed = DEFAULT_SPEED;

        if (target == null) return;

        class_243 direction = DomainGeometry.direction(from, target.method_33571());
        class_243 next = direction.method_1021(speed);
        class_238 swept = arrow.method_5829()
                .method_1009(next.field_1352, next.field_1351, next.field_1350).method_1014(0.3);
        if (swept.method_994(target.method_5829())) {
            // Never overshoot: land inside the target's own box so vanilla resolves the hit.
            double distance = target.method_33571().method_1020(from).method_1033();
            next = direction.method_1021(Math.min(speed, Math.max(0.05, distance)));
        }

        class_243 end = from.method_1019(next);
        if (!s.containsPoint(end.field_1352, end.field_1351, end.field_1350)) {
            arrow.method_31472();
            state.consumed = true;
            return;
        }
        if (DomainGeometry.raycast(s.world, s, from, end, true, true) != null) {
            arrow.method_31472();
            state.consumed = true;
            return;
        }

        // The arrow is stuck inside the domain (vanilla collision stopped it on an ordinary
        // interior block). Step it past that block and re-aim; `setInGround` is protected in
        // this version, so the position nudge is the available pass-through mechanism.
        if (arrow instanceof class_1667 && arrow.method_18798().method_1033() < 0.02) {
            class_243 step = direction.method_1021(0.6);
            double x = from.field_1352 + step.field_1352;
            double y = from.field_1351 + step.field_1351;
            double z = from.field_1350 + step.field_1350;
            if (!s.containsPoint(x, y, z)) {
                arrow.method_31472();
                state.consumed = true;
                return;
            }
            arrow.method_5814(x, y, z);
        }
        arrow.method_18799(next);
        arrow.field_6037 = true;
    }
}
