package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D32 minecraft:stray (blue) — "霜缚".
 *
 * <p>Every enemy inside the domain holds Slowness II. Stacking is handled by the lease
 * manager, which keeps the highest amplifier across overlapping sessions, so D29 + D32 give
 * exactly Slowness II with one duration instead of two competing instances.</p>
 */
public final class D32 implements DomainAbility {
    public static final D32 INSTANCE = new D32();
    private D32() {}
    @Override public String id() { return "minecraft:stray"; }
    @Override public String code() { return "D32"; }

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 enemy : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D32")) {
            EffectLeaseManager.apply(enemy, "minecraft:slowness", "D32", s.sessionId, 60, 1, now);
        }
    }
}
