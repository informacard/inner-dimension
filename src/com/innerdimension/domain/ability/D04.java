package com.innerdimension.domain.ability;

import com.innerdimension.domain.compat.NoNDomainBridge;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D04 minecraft:chicken (neutral).
 *
 * <p>Woundless ground: while any covering session engraves D04, no damage lands inside the
 * domain at all — vanilla types and this mod's own autonomous damage alike. The owner's
 * melee is consumed without an animation. Right-clicking a living actor starts a matched
 * NoN animation, with a per-owner 40 tick cooldown.</p>
 */
public final class D04 implements DomainAbility, DamageHook, AttackHook {
    public static final D04 INSTANCE = new D04();
    private D04() {}
    @Override public String id() { return "minecraft:chicken"; }
    @Override public String code() { return "D04"; }

    /** First in the pre-damage chain: D04 -> D50 drowning -> D40 -> element immunities. */
    @Override public int priority() { return 0; }

    private static final long ANIMATION_COOLDOWN = 40;
    private static final String ANIMATION_TIMER = "d04_animation";

    /**
     * TRUE cancels the damage. Nothing inside a D04 domain is allowed to be hurt, which is
     * exactly why the pipeline must run this hook before every other decision.
     */
    @Override
    public Boolean preDamage(DomainSession session, class_1309 victim, class_1282 source, float amount) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D04",victim))return null;
        return Boolean.TRUE;
    }

    @Override
    public Boolean onOwnerMelee(DomainSession session, class_3222 attacker, class_1309 target) {
        return com.innerdimension.domain.runtime.DomainObjects.matches(session,"D04",target)?Boolean.TRUE:null;
    }

    /** Decorations retain their ordinary right-click interaction. */
    public static boolean animationTarget(class_1297 target) {
        if (!(target instanceof class_1657 || target instanceof class_1308)) return false;
        if (target instanceof class_1531
                || target instanceof com.innerdimension.entity.EnderPearlDildoEntity
                || target instanceof com.innerdimension.entity.SensoryLinkDollEntity) return false;
        String path = class_7923.field_41177.method_10221(target.method_5864()).method_12832();
        return !path.contains("dildo") && !path.contains("plushie") && !path.contains("doll")
                && !path.contains("mannequin") && !path.contains("armor_stand");
    }

    public boolean onOwnerUse(DomainSession session, class_3222 attacker, class_1297 target) {
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D04",target) || !animationTarget(target) || !session.contains(attacker) || !session.contains(target)) return false;
        // The pipeline's own combat clock; identical to the tick value onTick receives.
        long now = com.innerdimension.domain.combat.DomainDamagePipeline.now();
        if (session.due(ANIMATION_TIMER, attacker.method_5667(), ANIMATION_COOLDOWN, now)) {
            session.arm(ANIMATION_TIMER, attacker.method_5667(), now);
            startAnimation(session, attacker, (class_1309) target);
        }
        return true;
    }

    private void startAnimation(DomainSession session, class_3222 attacker, class_1309 target) {
        if (!(attacker.method_73183() instanceof class_3218 world)) return;
        // Never restart an animation while either participant is already pending/active.
        if (NoNDomainBridge.animationBusy(world, attacker.method_5667())) return;
        if (NoNDomainBridge.animationBusy(world, target.method_5667())) return;
        List<class_1297> actors = new ArrayList<>(2);
        actors.add(attacker);
        actors.add(target);
        NoNDomainBridge.startMatched(world, actors);
    }

    @Override
    public void onEnd(DomainSession session) {
        // The per-attacker animation cooldown is session-scoped and dies with the session.
    }
}
