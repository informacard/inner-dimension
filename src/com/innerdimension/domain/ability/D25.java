package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * D25 minecraft:iron_golem (neutral) — "铁壁".
 *
 * <p>Every explicit friendly entity inside the domain holds +8 armor and +0.30 knockback
 * resistance as two of our own attribute modifiers. The lease is refreshed every tick while
 * the entity qualifies and is withdrawn the moment it stops qualifying or the expansion
 * ends, so an ordinary visitor is never affected and foreign modifiers are never touched.</p>
 */
public final class D25 implements DomainAbility {
    public static final D25 INSTANCE = new D25();
    private D25() {}
    @Override public String id() { return "minecraft:iron_golem"; }
    @Override public String code() { return "D25"; }

    @Override
    public void onTick(DomainSession s, long now) {
        D24D46Support.leaseSet(s, "d25_active", com.innerdimension.domain.runtime.DomainObjects.inside(s,"D25"),
                entity -> {
                    modifier(entity, class_5134.field_23724, "d25_armor", 8.0, true, s);
                    modifier(entity, class_5134.field_23718, "d25_knockback_resistance", 0.30, true, s);
                },
                entity -> {
                    modifier(entity, class_5134.field_23724, "d25_armor", 8.0, false, s);
                    modifier(entity, class_5134.field_23718, "d25_knockback_resistance", 0.30, false, s);
                });
    }

    private static void modifier(class_1309 entity, class_6880<class_1320> attribute, String id,
                                 double amount, boolean enabled, DomainSession s) {
        AttributeLeaseManager.apply(entity, attribute, id, amount,
                class_1322.class_1323.field_6328, enabled, s.sessionId, "D25");
    }
}
