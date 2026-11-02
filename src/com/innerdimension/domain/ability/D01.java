package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D01 minecraft:axolotl (pink).
 *
 * <p>Aquatic: the owner keeps Water Breathing I while it is inside the fixed interior.
 * The effect is a session-scoped lease, so any Water Breathing the player already had
 * (or gains from another mod) survives both the lease and its revocation, and no swim
 * speed modifier is applied at all.</p>
 */
public final class D01 implements DomainAbility {
    public static final D01 INSTANCE = new D01();
    private D01() {}
    @Override public String id() { return "minecraft:axolotl"; }
    @Override public String code() { return "D01"; }

    /** 60 ticks is re-applied every tick; the lease keeps the real duration merged. */
    private static final int REFRESH = 60;

    @Override
    public void onTick(DomainSession session,long now) {
        D24D46Support.leaseSet(session,"d01_objects",com.innerdimension.domain.runtime.DomainObjects.inside(session,"D01"),entity->{EffectLeaseManager.apply(entity, "minecraft:water_breathing", code(),
                    session.sessionId, REFRESH, 0, now);},entity->{EffectLeaseManager.revoke(entity,EffectLeaseManager.effect("minecraft:water_breathing"),session.sessionId,"D01",now);});
    }

    @Override
    public void onEnd(DomainSession session) {
        // DomainRuntime.EffectLeaseManager.revokeSession does the rest.
        class_3222 owner = session.owner();
        if (owner != null) {
            EffectLeaseManager.revoke(owner, EffectLeaseManager.effect("minecraft:water_breathing"),
                    session.sessionId, code(), com.innerdimension.domain.combat.DomainDamagePipeline.now());
        }
    }
}
