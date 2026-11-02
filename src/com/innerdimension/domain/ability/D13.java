package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D13 minecraft:polar_bear (cyan).
 *
 * <p>Thickness: the owner's maximum health is +8 (4 hearts) through a session-scoped
 * attribute lease. Nothing is healed when the bonus appears. When the expansion ends the
 * runtime's {@code AttributeLeaseManager.revokeSession} removes exactly this modifier and
 * clamps the current health down to the new maximum, which is not a damage event — there is
 * deliberately no {@code heal} or {@code hurt} call here.</p>
 */
public final class D13 implements DomainAbility {
    public static final D13 INSTANCE = new D13();
    private D13() {}
    @Override public String id() { return "minecraft:polar_bear"; }
    @Override public String code() { return "D13"; }

    private static final double HEALTH_BONUS = 8.0;

    @Override
    public void onTick(DomainSession session,long now) {
        D24D46Support.leaseSet(session,"d13_objects",com.innerdimension.domain.runtime.DomainObjects.inside(session,"D13"),entity->{AttributeLeaseManager.apply(entity, class_5134.field_23716, "d13_max_health", HEALTH_BONUS,
                class_1322.class_1323.field_6328, true, session.sessionId, code());},entity->{AttributeLeaseManager.apply(entity, class_5134.field_23716, "d13_max_health", HEALTH_BONUS,
                class_1322.class_1323.field_6328, false, session.sessionId, code());});
    }

    @Override
    public void onEnd(DomainSession session) {
        // Health is clamped by AttributeLeaseManager.revokeSession; no heal() and no hurt().
    }
}
