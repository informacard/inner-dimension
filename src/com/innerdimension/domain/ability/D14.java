package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D14 minecraft:armadillo (pink).
 *
 * <p>Curled defence: after 6 ticks of sneaking without moving or leaving its support the
 * owner is armed and every incoming hit is multiplied by 0.4. Moving, jumping or standing
 * up disarms immediately; attacking in place is allowed, and external knockback still
 * applies for the hit that caused it (disarm happens on the following tick).</p>
 *
 * <p>The 0.4 multiplier lives in {@code postArmor}, i.e. after vanilla armour, resistance
 * and absorption — never multiplied into the raw pre-armour amount.</p>
 */
public final class D14 implements DomainAbility, DamageHook {
    public static final D14 INSTANCE = new D14();
    private D14() {}
    @Override public String id() { return "minecraft:armadillo"; }
    @Override public String code() { return "D14"; }

    /** Late in the post-armor chain, so it scales the damage that will really be taken. */
    @Override public int priority() { return 90; }

    private static final long STILL_TICKS = 6;
    private static final float REDUCTION = 0.4f;
    private static final double SUPPORT_DEPTH = 0.25;
    private static final String FLAG_ARMED = "d14_armed";
    private static final String STATE_STILL_SINCE = "d14_still_since";

    @Override
    public float postArmor(DomainSession session, class_1309 victim, class_1282 source, float amount) {
        if (amount <= 0) return amount;
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D14",victim)) return amount;
        return session.flag(FLAG_ARMED+":"+victim.method_5667()) ? amount * REDUCTION : amount;
    }

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        for(class_1309 owner:com.innerdimension.domain.runtime.DomainObjects.inside(session,"D14")){
        String suffix=":"+owner.method_5667();
        boolean supported = owner.method_24828()
                || !Double.isNaN(DomainGeometry.supportDistance(session.world, owner.method_5829(), SUPPORT_DEPTH));
        boolean moving = !D01D23Support.still(owner) || !supported || owner.method_18798().field_1351 > 0.01;

        if (!owner.method_5715() || moving) {
            // Standing up, walking or jumping disarms at once and restarts the 6 ticks.
            session.flag(FLAG_ARMED+suffix, false);
            session.putState(STATE_STILL_SINCE+suffix, now);
            continue;
        }
        long since = session.state(STATE_STILL_SINCE+suffix, now);
        if (now - since >= STILL_TICKS && !session.flag(FLAG_ARMED+suffix)) {
            // Re-check before arming: the 6 ticks must have been still from the start.
            session.flag(FLAG_ARMED+suffix, true);
        } else if (!session.flag(FLAG_ARMED+suffix)) {
            session.putState(STATE_STILL_SINCE+suffix, since);
        }
        }
    }

    /** True while the 60% reduction is active, for the status HUD. */
    public static boolean armed(DomainSession session) {
        return session.flag(FLAG_ARMED+":"+session.ownerId);
    }

    @Override
    public void onEnd(DomainSession session) {
        session.flag(FLAG_ARMED, false);
    }
}
