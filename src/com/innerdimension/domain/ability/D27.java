package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/** Copper oxidation and restoration share one exclusive per-expansion mode. */
public final class D27 implements DomainAbility {
    public static final D27 INSTANCE = new D27();
    private D27() {}
    @Override public String id() { return "minecraft:copper_golem"; }
    @Override public String code() { return "D27"; }

    @Override
    public void onTick(DomainSession s, long now) {
        D24D46Support.copperTick(s, now, com.innerdimension.domain.DomainModes.reverse(s,"D27")?-1:1);
    }
}
