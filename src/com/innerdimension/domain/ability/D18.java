package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D18 minecraft:camel (yellow).
 *
 * <p>Tall frame: the owner's entity interaction range is +1.5 blocks through a
 * session-scoped attribute lease. Only the entity range changes — block interaction range
 * is untouched, and the attack ray is still stopped by real and projected solid blocks.</p>
 */
public final class D18 implements DomainAbility {
    public static final D18 INSTANCE = new D18();
    private D18() {}
    @Override public String id() { return "minecraft:camel"; }
    @Override public String code() { return "D18"; }

    private static final double REACH_BONUS = 1.5;

    @Override
    public void onTick(DomainSession session,long now) {
        D24D46Support.leaseSet(session,"d18_objects",com.innerdimension.domain.runtime.DomainObjects.inside(session,"D18"),entity->{AttributeLeaseManager.apply(entity, class_5134.field_47759, "d18_entity_reach", REACH_BONUS,
                class_1322.class_1323.field_6328, true, session.sessionId, code());},entity->{AttributeLeaseManager.apply(entity, class_5134.field_47759, "d18_entity_reach", REACH_BONUS,
                class_1322.class_1323.field_6328, false, session.sessionId, code());});
    }

    @Override
    public void onEnd(DomainSession session) {
        // The runtime revokes the modifier.
    }
}
