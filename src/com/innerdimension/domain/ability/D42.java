package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D42 minecraft:witch (purple) — "应急药理".
 *
 * <p>Three independent branches on the session owner, each a leased effect with its own
 * condition and refresh:</p>
 * <ul>
 *   <li>burning ({@code isOnFire}) -&gt; Fire Resistance I, 60 ticks, refreshed while burning;</li>
 *   <li>air shortage -&gt; Water Breathing I, 60 ticks. The danger test is vanilla's own
 *       threshold, {@code airSupply <= -20} (the value {@code shouldTakeDrowningDamage()}
 *       uses), extended with the mixed real+projection eye-in-water check so projected water
 *       counts;</li>
 *   <li>health at or below 30 % of the maximum -&gt; Regeneration I for 100 ticks, on its own
 *       200 tick session timer.</li>
 * </ul>
 *
 * <p>The branches never exclude each other, the healing branch is unaffected by D04, and every
 * effect is a lease so an external instance is preserved on withdrawal.</p>
 */
public final class D42 implements DomainAbility {
    public static final D42 INSTANCE = new D42();
    private D42() {}
    @Override public String id() { return "minecraft:witch"; }
    @Override public String code() { return "D42"; }

    private static final int VANILLA_DROWNING_THRESHOLD = -20;

    @Override
    public void onTick(DomainSession s, long now) {
        for(class_1309 owner:com.innerdimension.domain.runtime.DomainObjects.inside(s,"D42")){

        if (owner.method_5809()) {
            EffectLeaseManager.apply(owner, "minecraft:fire_resistance", "D42", s.sessionId, 60, 0, now);
        }

        int air = owner.method_5669();
        boolean drowning = air <= VANILLA_DROWNING_THRESHOLD;
        boolean submerged = DomainGeometry.eyesInWater(owner) && air < owner.method_5748() / 2;
        if (drowning || submerged) {
            EffectLeaseManager.apply(owner, "minecraft:water_breathing", "D42", s.sessionId, 60, 0, now);
        }

        float max = owner.method_6063();
        if (max > 0 && owner.method_6032() <= max * 0.30f && s.due("d42_regen",owner.method_5667(),200,now)) {
            s.arm("d42_regen",owner.method_5667(),now);
            EffectLeaseManager.apply(owner, "minecraft:regeneration", "D42", s.sessionId, 100, 0, now);
        }
        }
    }
}
