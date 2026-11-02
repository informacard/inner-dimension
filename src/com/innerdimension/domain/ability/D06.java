package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainAbilityInput;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D06 minecraft:goat (yellow).
 *
 * <p>Short dash: the client records a double tap of one movement key while sprinting and
 * submits the direction; the server re-validates owner, STABLE, direction, sprint and the
 * 2 second cooldown. The actual movement is at most 3 blocks over 4 ticks, swept every
 * tick against real + projected collision and clipped to the domain interior, so it can
 * never pass a wall, a projected block or the boundary, and it deals no damage.</p>
 *
 * <p>Direction encoding used by the input payload: 0 = forward, 1 = backward, 2 = left,
 * 3 = right (relative to the owner's yaw), -1 = nothing pending.</p>
 */
public final class D06 implements DomainAbility {
    public static final D06 INSTANCE = new D06();
    private D06() {}
    @Override public String id() { return "minecraft:goat"; }
    @Override public String code() { return "D06"; }

    private static final long COOLDOWN = 40;
    private static final int DASH_TICKS = 4;
    private static final double DASH_DISTANCE = 3.0;
    private static final double EPSILON = 1.0E-4;

    private static final String TIMER = "d06_dash_cooldown";
    private static final String STATE_DIRECTION = "d06_direction";
    private static final String STATE_TICKS = "d06_ticks_left";
    private static final String FLAG_DASHING = "d06_dashing";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        class_3222 owner = session.owner();
        if (owner == null) { session.flag(FLAG_DASHING, false); return; }
        if (!session.flag(FLAG_DASHING)) begin(session, owner, now);
        if (session.flag(FLAG_DASHING)) step(session, owner);
    }

    private void begin(DomainSession session, class_3222 owner, long now) {
        // The payload has already enforced the 250 ms double-tap window; the server only
        // re-checks eligibility, sprint state and its own cooldown.
        int direction = DomainAbilityInput.consumeDashDirection(session, now);
        if (direction < 0 || direction > 3) return;
        if (!owner.method_5624()) return;                       // must still be sprinting
        if (!session.dueSelf(TIMER, COOLDOWN, now)) return;     // still cooling down
        if (!session.contains(owner)) return;                   // must be in the interior
        session.armSelf(TIMER, now);
        session.putState(STATE_DIRECTION, direction);
        session.putState(STATE_TICKS, DASH_TICKS);
        session.flag(FLAG_DASHING, true);
    }

    private void step(DomainSession session, class_3222 owner) {
        if (!owner.method_5805() || !session.contains(owner)) {
            session.flag(FLAG_DASHING, false);
            return;
        }
        double step = DASH_DISTANCE / DASH_TICKS;
        class_243 heading = heading(owner, session.state(STATE_DIRECTION, 0));
        class_243 requested = new class_243(heading.field_1352 * step, 0, heading.field_1350 * step);
        class_243 clipped = DomainGeometry.clipToSession(session, owner, requested);
        class_243 resolved = DomainGeometry.sweep(owner, clipped);
        double wanted = Math.hypot(clipped.field_1352, clipped.field_1350);
        double moved = Math.hypot(resolved.field_1352, resolved.field_1350);
        if (moved > EPSILON) {
            owner.method_18800(resolved.field_1352, owner.method_18798().field_1351, resolved.field_1350);
            // method_18800 is vanilla setVelocity(x,y,z); this flag makes the server send
            // the ordinary velocity correction instead of inventing a parallel movement packet.
            owner.field_6037 = true;
        }
        int left = session.state(STATE_TICKS, DASH_TICKS) - 1;
        // Collision or boundary stops the dash immediately; a full sweep keeps going.
        if (moved < wanted - EPSILON || left <= 0) session.flag(FLAG_DASHING, false);
        else session.putState(STATE_TICKS, left);
    }

    private static class_243 heading(class_1297 owner, int direction) {
        double radians = Math.toRadians(owner.method_36454());
        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);
        double rightX = -forwardZ;
        double rightZ = forwardX;
        return switch (direction) {
            case 1 -> new class_243(-forwardX, 0, -forwardZ);
            case 2 -> new class_243(-rightX, 0, -rightZ);
            case 3 -> new class_243(rightX, 0, rightZ);
            default -> new class_243(forwardX, 0, forwardZ);
        };
    }

    /** Remaining cooldown, for the client HUD translation key. */
    public static long remainingCooldown(DomainSession session, long now) {
        return session.remainingSelf(TIMER, COOLDOWN, now);
    }

    @Override
    public void onEnd(DomainSession session) {
        session.flag(FLAG_DASHING, false);
        session.putState(STATE_DIRECTION, null);
    }
}
