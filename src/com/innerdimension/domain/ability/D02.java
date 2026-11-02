package com.innerdimension.domain.ability;

import com.innerdimension.domain.compat.NoNDomainBridge;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D02 minecraft:pig (pink).
 *
 * <p>Restless scent: every entity inside that has NoN data gains an extra 1/60 of its
 * libido cap per second (cap/1200 per tick). The fractional part is kept per entity in the
 * session and only whole units are submitted. No vanilla status effect is involved.</p>
 *
 * <p>The ledger is deliberately shared with the husk's identical branch: {@link
 * #accrueLibido} refuses a second submission for the same entity on the same tick, so
 * D02 + D29 still add exactly one identical rate. D29 must call this method rather than
 * keeping its own counter.</p>
 */
public final class D02 implements DomainAbility {
    public static final D02 INSTANCE = new D02();
    private D02() {}
    @Override public String id() { return "minecraft:pig"; }
    @Override public String code() { return "D02"; }

    private static final String REMAINDER = "d02_remainder";
    private static final String LAST_TICK = "d02_last_tick";
    private static final double PER_TICK_DIVISOR = 1200.0;

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        Set<UUID> eligible = new HashSet<>();
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D02")) {
            if (!NoNDomainBridge.hasLibido(entity)) continue;
            eligible.add(entity.method_5667());
            accrueLibido(session, entity, now);
        }
        // A target that left loses its fractional remainder instead of banking it.
        D01D23Support.uuidMap(session, REMAINDER).keySet().retainAll(eligible);
        D01D23Support.uuidMap(session, LAST_TICK).keySet().retainAll(eligible);
    }

    /**
     * One tick of the shared libido accrual. Returns the amount actually handed to NoN, or
     * 0 when nothing whole accumulated or this entity was already served this tick by the
     * other ability with the same rate.
     */
    public static int accrueLibido(DomainSession session, class_1309 entity, long now) {
        if (session == null || entity == null) return 0;
        if (!NoNDomainBridge.hasLibido(entity)) return 0;
        UUID id = entity.method_5667();
        Map<UUID, Long> last = D01D23Support.uuidMap(session, LAST_TICK);
        Long previous = last.get(id);
        if (previous != null && previous == now) return 0;
        last.put(id, now);
        int cap = NoNDomainBridge.maxLibido(entity);
        if (cap <= 0) return 0;
        Map<UUID, Double> remainder = D01D23Support.uuidMap(session, REMAINDER);
        double accumulated = remainder.getOrDefault(id, 0.0) + cap / PER_TICK_DIVISOR;
        int whole = (int) Math.floor(accumulated);
        remainder.put(id, accumulated - whole);
        if (whole <= 0) return 0;
        // NoN clamps at the cap itself; our contribution can never exceed it.
        return NoNDomainBridge.addLibido(entity, whole);
    }

    @Override
    public void onEnd(DomainSession session) {
        // Nothing to roll back: the values already added stay, only the remainder dies.
        session.putState(REMAINDER, null);
        session.putState(LAST_TICK, null);
    }
}
