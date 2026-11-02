package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D58 wither_skeleton (purple) — 凋刃 / withered blade.
 *
 * <p>After the owner's melee attack really lands (and after D04's cancellation has already
 * decided the hit happens at all), the victim receives a Wither I lease for 160 ticks. A
 * refresh does not add a separate instant wither tick: no extra damage call is made here. The
 * lease merges at the highest amplifier, so D58 + D59 apply one Wither I, never two.</p>
 */
public final class D58 implements DomainAbility, AttackHook {
    public static final D58 INSTANCE = new D58();
    private D58() {}
    @Override public String id() { return "minecraft:wither_skeleton"; }
    @Override public String code() { return "D58"; }
    @Override public int priority() { return 100; }

    private static final int WITHER_TICKS = 160;

    @Override
    public void afterMelee(DomainSession session, class_3222 attacker, class_1309 target, float taken) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D58",target))return;
        if (target == null || taken <= 0) return;
        EffectLeaseManager.apply(target, "minecraft:wither", "D58", session.sessionId,
                WITHER_TICKS, 0, DomainDamagePipeline.now());
    }
}
