package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D28 minecraft:zombie (green) — "腐弱".
 *
 * <p>Every enemy inside the domain holds Weakness I. The lease is source-tracked: a higher
 * external level survives, and revoking this expansion restores whatever was there before.
 * Ranged damage is deliberately untouched.</p>
 */
public final class D28 implements DomainAbility {
    public static final D28 INSTANCE = new D28();
    private D28() {}
    @Override public String id() { return "minecraft:zombie"; }
    @Override public String code() { return "D28"; }

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 enemy : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D28")) {
            EffectLeaseManager.apply(enemy, "minecraft:weakness", "D28", s.sessionId, 60, 0, now);
        }
    }
}
