package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainHealingPipeline;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainRelations;
import net.minecraft.*;
import java.util.*;

/**
 * D09 minecraft:wolf (blue).
 *
 * <p>Hunting pack: the enemy the owner really hit in melee is marked for 8 seconds (a new
 * hit replaces the old mark), friendly pets prefer that target, deal 25% more final damage
 * against it, and every pet recovers 1 health per 4 seconds of eligibility — independent
 * of whether anything is marked.</p>
 */
public final class D09 implements DomainAbility, AttackHook, DamageHook, AiHook {
    public static final D09 INSTANCE = new D09();
    private D09() {}
    @Override public String id() { return "minecraft:wolf"; }
    @Override public String code() { return "D09"; }

    private static final long MARK_DURATION = 160;
    private static final float PET_DAMAGE_BONUS = 1.25f;
    private static final long HEAL_INTERVAL = 80;
    public static final long SUMMON_INTERVAL = 100;
    public static final int MAX_SUMMONS = 6;
    private static final String STATE_MARK = "d09_mark";
    private static final String STATE_MARK_UNTIL = "d09_mark_until";
    private static final String TIMER_HEAL = "d09_pet_heal";

    /* --------------------------------------------------------------- mark */

    @Override
    public void afterMelee(DomainSession session, class_3222 attacker, class_1309 target, float taken) {
        if (target == null || taken <= 0 || !com.innerdimension.domain.runtime.DomainObjects.matches(session,"D09",target)) return;
        session.putState(STATE_MARK, target.method_5667());
        session.putState(STATE_MARK_UNTIL,
                com.innerdimension.domain.combat.DomainDamagePipeline.now() + MARK_DURATION);
    }

    /** The currently valid D09 mark, or null. D55 must consult this before its own fallback. */
    public static UUID markedTarget(DomainSession session, long now) {
        if (session == null) return null;
        UUID marked = session.state(STATE_MARK, null);
        long until = session.state(STATE_MARK_UNTIL, 0L);
        return marked != null && now < until ? marked : null;
    }

    /** The marked living entity inside this session, or null. */
    public static class_1309 markedEntity(DomainSession session, long now) {
        UUID marked = markedTarget(session, now);
        if (marked == null) return null;
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D09"))
            if (entity.method_5667().equals(marked) && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D09",entity)) return entity;
        return null;
    }

    /* ------------------------------------------------------------- combat */

    @Override
    public float postArmor(DomainSession session, class_1309 victim, class_1282 source, float amount) {
        if (amount <= 0 || source == null) return amount;
        UUID marked = markedTarget(session, com.innerdimension.domain.combat.DomainDamagePipeline.now());
        if (marked == null || !victim.method_5667().equals(marked)) return amount;
        class_1297 attacker = source.method_5529();
        if (!(attacker instanceof class_1309 living) || !D01D23Support.isPet(session, living)) return amount;
        return amount * PET_DAMAGE_BONUS;
    }

    /* ----------------------------------------------------------------- AI */

    @Override public void onStable(DomainSession s){DomainSummons.spawn(s,code(),"minecraft:wolf");DomainSummons.spawn(s,code(),"minecraft:wolf");s.armSelf("d09_summon",com.innerdimension.domain.runtime.DomainRuntime.now());}
    @Override public void afterDamage(DomainSession s,class_1309 victim,class_1282 source,float base,float taken,boolean blocked){if(taken>0&&source!=null&&DomainRelations.isFriendly(s,victim)&&source.method_5529() instanceof class_1309 attacker&&!DomainRelations.isFriendly(s,attacker)){s.putState("d09_guard",attacker.method_5667());s.putState("d09_guard_until",com.innerdimension.domain.combat.DomainDamagePipeline.now()+160);}}
    @Override
    public List<class_1309> preferredTargets(DomainSession session, class_1308 mob) {
        if (!D01D23Support.isPet(session, mob)) return List.of();
        class_1309 marked = markedEntity(session, com.innerdimension.domain.combat.DomainDamagePipeline.now());
        if(marked!=null)return List.of(marked);long now=com.innerdimension.domain.combat.DomainDamagePipeline.now();if(now<session.state("d09_guard_until",0L)){var attacker=D47D69Support.living(session.world,session.state("d09_guard",(UUID)null));if(attacker!=null&&session.contains(attacker)&&!DomainRelations.isFriendly(session,attacker))return List.of(attacker);}return List.of();
    }

    /* --------------------------------------------------------------- heal */

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        if(session.dueSelf("d09_summon",SUMMON_INTERVAL,now)){session.armSelf("d09_summon",now);if(DomainSummons.list(session,code()).size()<MAX_SUMMONS)DomainSummons.spawn(session,code(),"minecraft:wolf");}
        Set<UUID> eligible = new HashSet<>();
        for (class_1309 entity : session.livingInside()) {
            if (!D01D23Support.isPet(session, entity)) continue;
            eligible.add(entity.method_5667());
            com.innerdimension.domain.runtime.EffectLeaseManager.apply(entity,"minecraft:resistance",code(),session.sessionId,60,0,now);
            if (D01D23Support.dueAfterEligibility(session, TIMER_HEAL, entity.method_5667(), HEAL_INTERVAL, now))
                DomainHealingPipeline.domainHeal(entity, 1.0f);
        }
        D01D23Support.retainEligible(session, TIMER_HEAL, eligible);
    }

    @Override
    public void onEnd(DomainSession session) {
        DomainSummons.end(session,code());
        session.putState(STATE_MARK, null);
        session.putState(STATE_MARK_UNTIL, 0L);
    }
}
