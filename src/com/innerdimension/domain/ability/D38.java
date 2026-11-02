package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D38 minecraft:silverfish (neutral) — "啮石".
 *
 * <p>The owner holds Haste II for as long as the expansion is stable. Nothing here bypasses
 * tool tier requirements, protection permissions or the read-only projection rule — Haste
 * only shortens the mining time vanilla already allows.</p>
 */
public final class D38 implements DomainAbility {
    public static final D38 INSTANCE = new D38();
    private D38() {}
    @Override public String id() { return "minecraft:silverfish"; }
    @Override public String code() { return "D38"; }

    @Override
    public void onTick(DomainSession s,long now) {
        D24D46Support.leaseSet(s,"d38_objects",com.innerdimension.domain.runtime.DomainObjects.inside(s,"D38"),entity->{EffectLeaseManager.apply(entity, "minecraft:haste", "D38", s.sessionId, 60, 1, now);},entity->{EffectLeaseManager.revoke(entity,EffectLeaseManager.effect("minecraft:haste"),s.sessionId,"D38",now);});
    }

}
