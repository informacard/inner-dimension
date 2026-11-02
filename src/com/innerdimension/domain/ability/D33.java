package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D33 minecraft:bogged (green) — "沼毒".
 *
 * <p>Every enemy inside the domain holds Poison I, resolved by vanilla's own poison
 * mechanics (the effect cannot kill, and immune entities simply ignore it — no substitute
 * damage is invented). D37's Poison II wins on amplifier while both are present, and when
 * D37's contribution is withdrawn the lease falls back to level I instead of clearing.</p>
 */
public final class D33 implements DomainAbility {
    public static final D33 INSTANCE = new D33();
    private D33() {}
    @Override public String id() { return "minecraft:bogged"; }
    @Override public String code() { return "D33"; }

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 enemy : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D33")) {
            EffectLeaseManager.apply(enemy, "minecraft:poison", "D33", s.sessionId, 60, 0, now);
        }
    }
}
