package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainAttackPipeline;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * D10 minecraft:fox (orange).
 *
 * <p>Ambush: while the owner sneaks, stands still (horizontal and vertical movement below
 * the stillness threshold) and has not really been hurt for 2 seconds, it charges for
 * 1 second and then becomes READY. READY survives 2 seconds after standing up. The next
 * melee that is not cancelled merges an extra {@code max(baseDamage * 2, 12)} into the very
 * same damage request — never a second {@code hurt} call — and then cools down for
 * 4 seconds. Taking real damage cancels CHARGING/READY.</p>
 *
 * <p>The state names are exposed through {@link #stateOf} so the client HUD can show the
 * prepared/ready/cooldown text (the payload itself is owned by the client worker).</p>
 */
public final class D10 implements DomainAbility, AttackHook, DamageHook {
    public static final D10 INSTANCE = new D10();
    private D10() {}
    @Override public String id() { return "minecraft:fox"; }
    @Override public String code() { return "D10"; }

    /** After the D04 animation replacement: D04 terminates, D10 adds damage. */
    @Override public int priority() { return 20; }

    public enum Phase { IDLE, CHARGING, READY, COOLDOWN }

    private static final long CHARGE_TICKS = 20;      // 1 second
    private static final long READY_GRACE = 40;       // 2 seconds after standing up
    private static final long COOLDOWN_TICKS = 80;    // 4 seconds
    private static final long CALM_TICKS = 40;        // 2 seconds since the last real hurt
    private static final float BONUS_MULTIPLIER = 2.0f;
    private static final float BONUS_FLOOR = 12.0f;

    private static final String STATE_PHASE = "d10_phase";
    private static final String STATE_SINCE = "d10_since";
    private static final String STATE_READY_UNTIL = "d10_ready_until";
    private static final String STATE_COOLDOWN_UNTIL = "d10_cooldown_until";

    private static final Logger LOGGER = LoggerFactory.getLogger("InnerDimension");
    private static final String LOG_CHARGING = "d10_log_charging";
    private static final String LOG_READY = "d10_log_ready";
    private static final String LOG_BLOCKED = "d10_log_blocked";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        class_3222 owner = session.owner();
        if (owner == null) return;

        Phase phase = phase(session);
        long lastHurt = DomainDamagePipeline.lastHurt(session.ownerId);

        switch (phase) {
            case IDLE -> {
                boolean sneaking = owner.method_5715();
                boolean still = D01D23Support.still(owner);
                boolean calm = now - lastHurt >= CALM_TICKS;
                if (sneaking && still && calm) {
                    session.putState(STATE_PHASE, Phase.CHARGING.name());
                    session.putState(STATE_SINCE, now);
                    if (session.once(LOG_CHARGING))
                        LOGGER.info("D10 fox: charging (sneaking, still, calm)");
                } else if (sneaking && session.once(LOG_BLOCKED)) {
                    // Answers "why is there no ambush hint" without logging every tick.
                    LOGGER.info("D10 fox: sneaking but not charging: still={} calm={} sinceHurt={}",
                            still, calm, now - lastHurt);
                }
            }
            case CHARGING -> {
                if (!owner.method_5715() || !D01D23Support.still(owner)) {
                    session.putState(STATE_PHASE, Phase.IDLE.name());
                } else if (now - session.state(STATE_SINCE, now) >= CHARGE_TICKS) {
                    session.putState(STATE_PHASE, Phase.READY.name());
                    session.putState(STATE_READY_UNTIL, now + READY_GRACE);
                    if (session.once(LOG_READY)) LOGGER.info("D10 fox: ready");
                }
            }
            case READY -> {
                // Standing up keeps READY alive for its full grace period.
                if (owner.method_5715()) session.putState(STATE_READY_UNTIL, now + READY_GRACE);
                else if (now > session.state(STATE_READY_UNTIL, 0L))
                    session.putState(STATE_PHASE, Phase.IDLE.name());
            }
            case COOLDOWN -> {
                if (now >= session.state(STATE_COOLDOWN_UNTIL, 0L))
                    session.putState(STATE_PHASE, Phase.IDLE.name());
            }
        }
    }

    @Override
    public float bonusDamage(DomainSession session, class_3222 attacker, class_1309 target, float baseDamage) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D10",target))return 0;
        if (phase(session) != Phase.READY) return 0;
        long now = DomainDamagePipeline.now();
        // Merged into the pending request; the invulnerability window is never involved.
        session.putState(STATE_PHASE, Phase.COOLDOWN.name());
        session.putState(STATE_COOLDOWN_UNTIL, now + COOLDOWN_TICKS);
        float base = baseDamage > 0 ? baseDamage : DomainAttackPipeline.attackDamage(attacker);
        return Math.max(base * BONUS_MULTIPLIER, BONUS_FLOOR);
    }

    @Override
    public void ownerHurt(DomainSession session, class_1282 source, float taken) {
        if (taken <= 0) return;
        Phase phase = phase(session);
        if (phase == Phase.CHARGING || phase == Phase.READY) {
            session.putState(STATE_PHASE, Phase.IDLE.name());
            session.putState(STATE_READY_UNTIL, 0L);
        }
    }

    private static Phase phase(DomainSession session) {
        String raw = session.state(STATE_PHASE, Phase.IDLE.name());
        try {
            return Phase.valueOf(raw);
        } catch (IllegalArgumentException broken) {
            return Phase.IDLE;
        }
    }

    /** Current ambush phase, for the client status translation key. */
    public static String stateOf(DomainSession session) {
        return phase(session).name();
    }

    @Override
    public void onEnd(DomainSession session) {
        session.putState(STATE_PHASE, Phase.IDLE.name());
        session.putState(STATE_READY_UNTIL, 0L);
        session.putState(STATE_COOLDOWN_UNTIL, 0L);
    }
}
