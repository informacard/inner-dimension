package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D11 minecraft:ocelot (yellow).
 *
 * <p>Swift step: the owner has +30% movement speed as a multiplicative attribute lease, no
 * sneaking required. It is its own modifier id, so it simply adds to D07's temporary +15%
 * instead of being folded into a potion, and revoking it touches nothing else.</p>
 */
public final class D11 implements DomainAbility {
    public static final D11 INSTANCE = new D11();
    private D11() {}
    @Override public String id() { return "minecraft:ocelot"; }
    @Override public String code() { return "D11"; }

    private static final double SPEED_BONUS = 0.30;

    @Override
    public void onTick(DomainSession session,long now) {
        D24D46Support.leaseSet(session,"d11_objects",com.innerdimension.domain.runtime.DomainObjects.inside(session,"D11"),entity->{AttributeLeaseManager.apply(entity, class_5134.field_23719, "d11_speed", SPEED_BONUS,
                class_1322.class_1323.field_6331, true, session.sessionId, code());},entity->{AttributeLeaseManager.apply(entity, class_5134.field_23719, "d11_speed", SPEED_BONUS,
                class_1322.class_1323.field_6331, false, session.sessionId, code());});
    }

    @Override
    public void onEnd(DomainSession session) {
        // The runtime revokes every modifier this session owns.
    }
}
