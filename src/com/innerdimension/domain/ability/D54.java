package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D54 minecraft:piglin_brute (orange) — 蛮兵 / brute.
 *
 * <p>The owner carries Strength I and +0.25 knockback resistance (0.25 of a full point, added
 * as a value modifier) while the expansion is stable. Both are leases: the strength effect is
 * merged at the highest same-effect amplifier, so D55's pet strength never stacks a second
 * potion, and the knockback-resistance modifier is added on top of whatever D25 contributes
 * and is clamped by vanilla at 100%.</p>
 */
public final class D54 implements DomainAbility {
    public static final D54 INSTANCE = new D54();
    private D54() {}
    @Override public String id() { return "minecraft:piglin_brute"; }
    @Override public String code() { return "D54"; }
    @Override public int priority() { return 100; }

    private static final int REFRESH = 60;
    private static final double KNOCKBACK_RESISTANCE = 0.25;

    @Override
    public void onTick(DomainSession s, long now) {
        D24D46Support.leaseSet(s,"d54_selected",com.innerdimension.domain.runtime.DomainObjects.inside(s,"D54"),e->{},e->{var attribute=D47D69Support.attribute("minecraft:knockback_resistance");if(attribute!=null)AttributeLeaseManager.apply(e,attribute,"d54_knockback_resistance",KNOCKBACK_RESISTANCE,class_1322.class_1323.field_6328,false,s.sessionId,"D54");EffectLeaseManager.revoke(e,EffectLeaseManager.effect("minecraft:strength"),s.sessionId,"D54",now);});
        for(class_1309 owner:com.innerdimension.domain.runtime.DomainObjects.inside(s,"D54")){
        EffectLeaseManager.apply(owner, "minecraft:strength", "D54", s.sessionId, REFRESH, 0, now);
        class_6880<class_1320> resistance = D47D69Support.attribute("minecraft:knockback_resistance");
        if (resistance != null) AttributeLeaseManager.apply(owner, resistance, "d54_knockback_resistance",
                KNOCKBACK_RESISTANCE, class_1322.class_1323.field_6328, true, s.sessionId, "D54");
        }
    }
}
