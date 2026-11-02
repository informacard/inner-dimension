package com.innerdimension.boss;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.domain.combat.*;
import com.innerdimension.domain.*;
import com.innerdimension.progression.DomainFeatures;
import net.minecraft.*;
import java.util.*;
import java.lang.reflect.*;
public final class BossCombat245Regression {
    static int checks;
    static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static void set(Object o,Class<?> c,String name,Object value)throws Exception{var f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
    static class ProbeWither extends class_1528 {
        UUID id;float health;class_1937 world;
        ProbeWither(){super(null,null);}
        public UUID method_5667(){return id;}
        public float method_6032(){return health;}
        public double method_45325(class_6880<class_1320> attribute){return 300;}
        public boolean method_5805(){return true;}
        public boolean method_7325(){return false;}
        public Set<String> method_5752(){return Set.of();}
        public class_1937 method_73183(){return world;}
        public class_1299<?> method_5864(){return class_7923.field_41177.method_10223(class_2960.method_60655("minecraft","wither")).orElseThrow().comp_349();}
    }
    public static void main(String[] args)throws Exception{
        class_155.method_36208();class_2966.method_12851();
        UUID target=UUID.randomUUID(),other=UUID.randomUUID();var gate=new BossCombatRules.ShrineGate();
        int hits=0;
        for(int tick=0;tick<120;tick++)for(int caster=0;caster<8;caster++)if(gate.claim(target,tick))hits++;
        check(hits==10,"Eight overlapping casters share ten Boss cuts over six seconds");
        check(gate.claim(other,119),"An unrelated Boss owns an independent window");
        check(!gate.claim(target,119)&&gate.claim(target,120),"Shared window opens at twelve ticks exactly");
        gate.clear();check(gate.claim(target,0),"Server lifecycle reset removes stale cooldowns");
        check(!BossCombatRules.halfPhaseDue(1,151,300),"First phase remains above half");
        check(BossCombatRules.halfPhaseDue(1,150,300),"Exact half health triggers second phase");
        check(!BossCombatRules.halfPhaseDue(3,149,300),"The half-health phase cannot retrigger repeatedly");
        check(BossCombatRules.domainSource(DomainDamageTypes.SHRINE_SLASH,false),"Shrine is domain damage");
        check(BossCombatRules.domainSource("minecraft:mob_attack",true),"Domain summons cannot bypass immunity");
        check(!BossCombatRules.domainSource("minecraft:player_attack",false)&&!BossCombatRules.domainSource("minecraft:arrow",false),"Ordinary weapon sources remain ordinary damage");
        check(!DomainFeatures.shrineAvailable(false,List.of("minecraft:vindicator")),"Vindicator alone does not unlock Shrine");
        check(!DomainFeatures.shrineAvailable(true,List.of()),"Baptism alone does not unlock Shrine");
        check(!DomainFeatures.shrineAvailable(true,List.of(DomainModes.SHRINE)),"A legacy Shrine record cannot bypass the Vindicator prerequisite");
        check(DomainFeatures.shrineAvailable(true,List.of("minecraft:vindicator")),"Both prerequisites unlock Shrine in either acquisition order");
        check(DomainAbilityCatalog.weight(List.of(DomainModes.SHRINE))==8,"Shrine occupies eight capacity");
        check(DomainModes.settings("D76")&&DomainModes.valid("D76",DomainObjects.HOSTILE),"Server accepts selected Shrine targets");
        check(!DomainModes.valid("D76",DomainObjects.SELF),"Server rejects self-targeting Shrine masks");
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var mob=(ProbeWither)unsafe.allocateInstance(ProbeWither.class);mob.id=UUID.randomUUID();mob.health=151;mob.world=(class_3218)unsafe.allocateInstance(class_3218.class);
        var encounter=new BossEncounter(mob.id,1,"minecraft:overworld");encounter.entity=mob;encounter.registered=true;encounter.activeAt=0;encounter.nodes=1;
        BossDomains.BOSSES.put(mob.id,encounter);
        try{
            check(!BossDomains.domainImmune(mob),"Live first-phase coordinator does not grant immunity above half");
            mob.health=150;check(BossDomains.domainImmune(mob),"Live coordinator grants immunity at the threshold before the next tick");
            BossDomains.damaged(mob);check(encounter.halfPhasePending&&(encounter.nodes&2)!=0,"Successful damage immediately records the half-health phase");
            mob.health=240;check(!mob.method_6872()&&!BossDomains.domainImmune(mob),"Healing above half follows the vanilla projectile-immunity predicate exactly");
            mob.health=120;check(mob.method_6872()&&BossDomains.domainImmune(mob),"Re-entering vanilla armored state restores domain immunity");
            var plain=new class_1282(class_6880.method_40223(new class_8110("test",0)));
            check(DomainDamagePipeline.pre(mob,plain,6),"Real damage entry point preserves ordinary damage during domain immunity");
            encounter.registered=false;check(BossDomains.domainImmune(mob),"Domain immunity follows vanilla armor even without an active Boss domain");
            encounter.nodes=1;mob.health=240;
            var session=(DomainSession)unsafe.allocateInstance(DomainSession.class);
            set(session,DomainSession.class,"ownerId",UUID.randomUUID());set(session,DomainSession.class,"codes",Set.of());
            var masks=new HashMap<String,Integer>();set(session,DomainSession.class,"objectMasks",masks);
            masks.put("D76",DomainObjects.HOSTILE);check(DomainObjects.qualifies(session,"D76",mob),"Shrine real target pipeline includes a selected hostile Boss");
            masks.put("D76",DomainObjects.FRIENDLY);check(!DomainObjects.qualifies(session,"D76",mob),"Shrine real target pipeline excludes an unselected hostile Boss");
            masks.put("D76",0);check(!DomainObjects.qualifies(session,"D76",mob),"Empty target mask suppresses Shrine instead of using its legacy all-target shortcut");
        }finally{BossDomains.BOSSES.remove(mob.id);}
        System.out.println("PASS BossCombat245Regression: "+checks+" checks; shared hit budget, real immunity and target entry points, unlock prerequisites");
    }
}
