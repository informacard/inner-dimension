package com.innerdimension.domain.ability;

import com.innerdimension.domain.compat.NoNDomainBridge;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D29 minecraft:husk (yellow) — "饥渴".
 *
 * <p>Every enemy inside the domain holds Slowness I, and entities that carry NoN data also
 * accumulate libido at the same rate as D02 (max/1200 per tick, submitted only in whole API
 * units with the fractional remainder kept in the session).</p>
 *
 * <p>Combination: D02 and D29 must never double the same target's growth, so D29 skips the
 * libido branch for any target already covered by a session that carries D02. Slowness is a
 * leased effect, so D32's Slowness II wins by amplifier without stacking durations.</p>
 */
public final class D29 implements DomainAbility {
    public static final D29 INSTANCE = new D29();
    private D29() {}
    @Override public String id() { return "minecraft:husk"; }
    @Override public String code() { return "D29"; }

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 enemy : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D29")) {
            EffectLeaseManager.apply(enemy, "minecraft:slowness", "D29", s.sessionId, 60, 0, now);
            growLibido(s, enemy, now);
        }
    }

    private static void growLibido(DomainSession s, class_1309 enemy, long now) {
        if (!NoNDomainBridge.hasLibido(enemy)) return;
        var d02Sessions = DomainSessionIndex.coveringWith(enemy, "D02");
        if (!d02Sessions.isEmpty()) {
            D02.accrueLibido(d02Sessions.get(0), enemy, now);
            return;
        }
        int max = NoNDomainBridge.maxLibido(enemy);
        if (max <= 0) return;
        String key = "d29_libido:" + enemy.method_5667();
        double accumulated = s.accumulate(key, max / 1200.0);
        int whole = (int) Math.floor(accumulated);
        if (whole <= 0) return;
        s.putState(key, accumulated - whole);
        NoNDomainBridge.addLibido(enemy, whole);
    }
}
