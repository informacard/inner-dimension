package com.innerdimension.boss;

import com.innerdimension.chunk.LoadedChunks;
import com.innerdimension.core.ServerClock;
import com.innerdimension.projection.ProjectionAnimation;
import com.innerdimension.domain.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.domain.combat.DomainDamageTypes;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.util.*;

final class BossEncounter {
    final UUID id;
    final int kind; // dragon, wither, warden
    final String worldId;
    class_3218 world;
    class_1308 entity;
    class_243 center,spawn,appearance;
    double radius,vertical;
    String city;
    long cast,activeAt=-1,deadline,warningUntil=-1,burnoutUntil,disabledUntil,nextPulse,missingSince;
    int nodes,spreadCount;
    boolean pending,removed,registered,awake,crystalBaselineReady,immunityApplied,halfPhasePending;
    final Set<DomainContest.Entry> countered=Collections.newSetFromMap(new IdentityHashMap<>());
    final Set<DomainContest.Entry> disabledBy=Collections.newSetFromMap(new IdentityHashMap<>());
    final Map<DomainContest.Entry,Integer> wardenWait=new IdentityHashMap<>();
    final Map<Object,BossContestRules.Edge> edges=new LinkedHashMap<>();
    final Map<UUID,Integer> exposure=new HashMap<>();
    final Set<UUID> crystals=new HashSet<>();
    final UUID lease=UUID.randomUUID();
    final Set<class_1309> exposed=new HashSet<>();
    WitherDomainEffects wither;
    class_1309 forcedTarget;
    BossEncounter(class_1308 e,class_3218 w){
        id=e.method_5667();entity=e;world=w;worldId=w.method_27983().method_29177().toString();
        kind=e instanceof class_1510?0:e instanceof class_1528?1:2;
        center=spawn=appearance=e.method_73189();radius=kind==0?BossDomainConfig.get().dragonRadius:kind==1?BossDomainConfig.get().witherRadius:64;vertical=kind==2?32:radius;
        // Migration consumes thresholds already passed; never replays earlier damage nodes.
        if(kind==0){if(e.method_6032()/e.method_6063()<=.7)nodes|=4;if(e.method_6032()/e.method_6063()<=.35)nodes|=8;}
        if(kind==1){if(e.method_6032()/e.method_6063()<=.5)nodes|=2;}
    }
    BossEncounter(UUID id,int kind,String worldId){this.id=id;this.kind=kind;this.worldId=worldId;}
    boolean warden(){return kind==2;}
    boolean active(){return !removed&&registered&&activeAt>=0&&ServerClock.now()>=activeAt;}
    boolean warning(){return !removed&&registered&&warningUntil>ServerClock.now();}
    double strength(){var c=BossDomainConfig.get();return kind==0?c.dragonStrength:kind==1?c.witherStrength:c.wardenStrength;}
    double simpleRate(){var c=BossDomainConfig.get();return kind==0?c.dragonSimpleRate:kind==1?c.witherSimpleRate:c.wardenSimpleRate;}
    void rememberPosition(){if(entity!=null){spawn=entity.method_73189();if(kind==1)center=spawn;}}
    double currentRadius(boolean full){
        if(full||warden())return radius;
        if(!active())return 0;
        int expand=kind==0?BossDomainConfig.get().dragonExpandTicks:BossDomainConfig.get().witherExpandTicks;
        return radius*Math.min(1,Math.max(0,(ServerClock.now()-activeAt+1)/(double)expand));
    }
    boolean contains(class_243 p,boolean full){
        if(center==null)return false;double r=currentRadius(full),dx=p.field_1352-center.field_1352,dz=p.field_1350-center.field_1350;
        double low=kind==0?world.method_31607():center.field_1351-vertical;
        double high=kind==0?world.method_31607()+world.method_31605():center.field_1351+vertical;
        return dx*dx+dz*dz<=r*r&&p.field_1351>=low&&p.field_1351<=high;
    }
    boolean intersects(class_238 box,boolean full){
        double r=currentRadius(full);if(center==null||world==null)return false;
        double dx=Math.max(box.field_1323-center.field_1352,Math.max(0,center.field_1352-box.field_1320));
        double dz=Math.max(box.field_1321-center.field_1350,Math.max(0,center.field_1350-box.field_1324));
        double low=kind==0?world.method_31607():center.field_1351-vertical,high=kind==0?world.method_31607()+world.method_31605():center.field_1351+vertical;
        return dx*dx+dz*dz<=r*r&&box.field_1325>=low&&box.field_1322<=high;
    }
    void before(long now,MinecraftServer server){
        if(removed)return;
        if(warden())refreshDisable(now);
        if(world==null){for(var w:server.method_3738())if(w.method_27983().method_29177().toString().equals(worldId)){world=w;break;}if(world==null)return;}
        if(entity!=null&&(!entity.method_5805()||entity.method_31481()||entity.method_73183()!=world||world.method_73284(id)!=entity)){remove();return;}
        if(entity==null){
            if(warden()&&spawn!=null&&LoadedChunks.contains(world,class_2338.method_49638(spawn))){
                var found=world.method_73284(id);if(found instanceof class_7260 w){entity=w;missingSince=0;}
                else if(missingSince==0)missingSince=now;else if(now-missingSince>=40)remove();
            }
            return;
        }
        rememberPosition();
        if(kind==0){if(!registered)dragonCenter();if(!crystalBaselineReady)captureCrystals();}
        if(warden()){
            if(!registered){
                BossCityGeometry.locate(this);
                for(var b:BossDomains.BOSSES.values())if(b!=this&&b.warden()&&!b.removed&&b.registered&&worldId.equals(b.worldId)&&city.equals(b.city))return;
                begin(now);return;
            }
            if(entity.method_41328(class_4050.field_38099)){awake=false;return;}
            if(entity.method_41328(class_4050.field_38100)){remove();return;}
            if(active()&&!awake){awake=true;nextPulse=now+BossDomainConfig.get().wardenPulseTicks;}
            if(active()&&now==disabledUntil)nextPulse=now+BossDomainConfig.get().wardenPulseTicks;
        }else{
            if(registered&&now>=deadline){end();return;}
            if(entity instanceof class_1528 spawning&&spawning.method_6884()>0)return;
            boolean controlled=UnlimitedVoidControl.locked(entity)||entity.method_5987();
            boolean counter=false,coveredByPlayer=false,halfPhase=false;
            countered.removeIf(e->!e.live());
            for(var e:BossDomains.entries())if(!e.simple&&BossDomains.valid(e)&&e.shell.method_73183()==world){
                boolean covers=BossDomains.willCover(e,entity);
                if(entity instanceof class_1510 dragon)for(var part:dragon.method_5690())if(BossDomains.willCover(e,part))covers=true;
                coveredByPlayer|=covers;
                if(covers&&!countered.contains(e)){counter=true;countered.add(e);}

            }
            boolean nodeRequest=false;
            if(kind==0){
                double hp=entity.method_6032()/entity.method_6063();
                if(hp<=.7&&(nodes&4)==0){nodes|=4;pending=true;BossDomains.dirty=true;}
                if(hp<=.35&&(nodes&8)==0){nodes|=8;pending=true;BossDomains.dirty=true;}
                var dragon=(class_1510)entity;
                boolean perched=dragon.method_6831().method_6864().method_6848();
                nodeRequest=pending&&perched;
            }else{
                var boss=(class_1528)entity;
                if(boss.method_6884()>0)return; // never stack with the vanilla spawn explosion
                if((nodes&1)==0){nodes|=1;pending=true;BossDomains.dirty=true;burnoutUntil=Math.max(burnoutUntil,now+1);}
                noteHalfHealth();halfPhase=halfPhasePending;
                nodeRequest=pending;
            }
            boolean players=server.method_3760().method_14571().stream().anyMatch(p->BossDomains.context(this,p));
            if(halfPhase&&players){
                begin(now,coveredByPlayer);
                org.slf4j.LoggerFactory.getLogger("inner_dimension.boss").info("Wither half-health phase: boss={} cast={} health={} max={} immuneAt={}",id,cast,entity.method_6032(),entity.method_6063(),activeAt);
                return;
            }
            if(!registered&&players&&(counter||now>=burnoutUntil&&!controlled&&nodeRequest))begin(now,counter);
            else if(warning()&&counter){warningUntil=activeAt=now;deadline=now+(kind==0?BossDomainConfig.get().dragonTicks:BossDomainConfig.get().witherTicks);BossDomains.dirty=true;}
        }
    }
    void noteHalfHealth(){
        if(kind==1&&entity!=null&&BossCombatRules.halfPhaseDue(nodes,entity.method_6032(),entity.method_6063())){
            nodes|=2;pending=true;halfPhasePending=true;BossDomains.dirty=true;
        }
    }
    void begin(long now){begin(now,false);}
    void begin(long now,boolean counter){
        if(kind==0&&!dragonCenter())return;
        if(registered)cleanup();
        BossDomains.dirty=true;cast++;edges.clear();exposure.clear();pending=false;halfPhasePending=false;registered=true;
        warningUntil=now+(counter?0:20);activeAt=warningUntil;
        deadline=activeAt+(kind==0?BossDomainConfig.get().dragonTicks:kind==1?BossDomainConfig.get().witherTicks:Long.MAX_VALUE/4);
        nextPulse=activeAt+BossDomainConfig.get().wardenPulseTicks;
        if(kind==1)wither=new WitherDomainEffects(this);
        org.slf4j.LoggerFactory.getLogger("inner_dimension.boss").info("Boss domain begin: boss={} kind={} cast={} counter={} activeAt={}",id,kind,cast,counter,activeAt);
        for(var p:world.method_8503().method_3760().method_14571())if(BossDomains.context(this,p)){
            if(!counter&&net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(p,com.innerdimension.network.DomainHudPayload.ID)){
                var name=class_2561.method_43471("inner_dimension.boss.name."+kind).getString();
                var palette=com.innerdimension.domain.DomainTextPalette.boss(kind);
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new com.innerdimension.network.DomainHudPayload(false,name,palette[0],palette[1],palette[0],333,667,"",0,0,0,333,667,10,10,-1));
            }
            com.innerdimension.sound.ModSounds.personal(p,"domain_clash",.7f);
        }
    }
    boolean dragonCenter(){
        var dragon=(class_1510)entity;var fight=dragon.method_6829();
        var portal=fight==null?null:fight.method_12530().comp_1545().orElse(null);
        if(portal==null){if(ServerClock.now()%200==0)org.slf4j.LoggerFactory.getLogger("inner_dimension.boss").warn("Dragon {} domain waits for a known exit portal",id);return false;}
        // The center column is checked directly, never a building-sensitive heightmap.
        class_2338 top=null;
        for(int y=portal.method_10264();y<=portal.method_10264()+8;y++){
            var pos=new class_2338(portal.method_10263(),y,portal.method_10260());
            if(!LoadedChunks.contains(world,pos))return false;
            if(world.method_8320(pos).method_26204()==class_2246.field_9987)top=pos;
        }
        if(top==null)return false;
        center=new class_243(top.method_10263()+.5,top.method_10264()+1,top.method_10260()+.5);return true;
    }
    void captureCrystals(){
        if(!(entity instanceof class_1510))return;
        for(var spike:class_3310.method_14506(world))if(!LoadedChunks.contains(world,new class_2338(spike.method_13966(),spike.method_13964(),spike.method_13967())))return;
        crystals.clear();crystalBaselineReady=true;BossDomains.dirty=true;
        for(var spike:class_3310.method_14506(world))for(var crystal:world.method_18467(class_1511.class,spike.method_13968())){
            // Adopt only the pillar-top baseline UUID. Later player placements never join it.
            if(Math.abs(crystal.method_23317()-(spike.method_13966()+.5))<.1 && Math.abs(crystal.method_23321()-(spike.method_13967()+.5))<.1 && Math.abs(crystal.method_23318()-(spike.method_13964()+1))<.1)crystals.add(crystal.method_5667());
        }
        if(crystals.isEmpty())nodes|=3;
    }
    void crystal(class_1511 crystal,class_1282 source){
        if(!crystals.remove(crystal.method_5667()))return;BossDomains.dirty=true;
        boolean player=source!=null&&source.method_5529() instanceof class_1657;
        if(player){if((nodes&1)==0){nodes|=1;pending=true;BossDomains.dirty=true;}if(crystals.isEmpty()&&(nodes&2)==0){nodes|=2;pending=true;BossDomains.dirty=true;}}
        else if(crystals.isEmpty())nodes|=2;
    }
    void disable(long now,Collection<DomainContest.Entry> winners){
        disabledBy.addAll(winners);
        BossDomains.dirty=true;
        disabledUntil=Math.max(disabledUntil,now+BossDomainConfig.get().wardenDisabledTicks);
        nextPulse=disabledUntil+BossDomainConfig.get().wardenPulseTicks;
    }
    void refreshDisable(long now){
        boolean wasHeld=!disabledBy.isEmpty();
        // Keep exact cast references through the complete retraction, even after contest
        // membership ends. Slot changes and later casts must not take over this hold.
        disabledBy.removeIf(e->e.shell.method_31481()||e.shell.phase()==ProjectionAnimation.RETRACTING&&e.shell.elapsed(0)>=e.shell.closeTicks());
        disabledUntil=BossContestRules.disabledDeadline(now,disabledUntil,BossDomainConfig.get().wardenDisabledTicks,wasHeld||!disabledBy.isEmpty());
        if(wasHeld){nextPulse=disabledUntil+BossDomainConfig.get().wardenPulseTicks;if(disabledBy.isEmpty())BossDomains.dirty=true;}
    }
    void effects(long now,MinecraftServer server){
        if(entity==null)return;
        for(var target:List.copyOf(exposed))if(!target.method_5805()||target.method_73183()!=world||!contains(target.method_73189(),false)||BossDomains.protectedBy(this,target)||disabledUntil>now){EffectLeaseManager.revoke(target,EffectLeaseManager.effect("minecraft:glowing"),lease,now);exposed.remove(target);}
        if(now%5==0)boundary();
        if(kind==0){
            Set<UUID> current=new HashSet<>();
            for(var p:server.method_3760().method_14571())if(p.method_73183()==world&&p.method_5805()&&!p.method_7325()&&contains(p.method_73189(),false)&&!BossDomains.protectedBy(this,p)){
                current.add(p.method_5667());int count=exposure.merge(p.method_5667(),1,Integer::sum);
                if(count>=BossDomainConfig.get().dragonExposureTicks){
                    exposure.put(p.method_5667(),0);
                    if(!BossDomains.protectedBy(this,p)){
                        p.method_64397(world,DomainDamageTypes.source(world,"inner_dimension:boss_dragon_breath",entity,entity),BossDomainConfig.get().dragonDamage);
                        double dx=center.field_1352-p.method_23317(),dz=center.field_1350-p.method_23321();
                        if(dx*dx+dz*dz<1e-9){dx=1;dz=0;}
                        p.method_6005(BossDomainConfig.get().dragonKnockback,dx,dz);
                        double resist=Math.max(0,1-p.method_45325(class_5134.field_23718));
                        p.method_5762(0,BossDomainConfig.get().dragonUpward*resist,0);p.field_6037=true;
                    }
                }
            }
            exposure.keySet().retainAll(current);
        }else if(kind==1)wither.tick(now);
        else {
            if(disabledUntil>now)withdrawForced();
            if(!awake||disabledUntil>now)return;
            if(now>=nextPulse){nextPulse=now+BossDomainConfig.get().wardenPulseTicks;pulse(now);}
            forceTarget();
            if(spreadCount<3&&now>=activeAt+spreadCount*40L){spreadCount++;BossDomains.dirty=true;BossEnvironment.spread(this);}
        }
    }
    void pulse(long now){
        var w=(class_7260)entity;w.method_5783(class_3417.field_38830,1f,1f);
        for(var raw:world.method_8335(entity,new class_238(center.field_1352-radius,center.field_1351-vertical,center.field_1350-radius,center.field_1352+radius,center.field_1351+vertical,center.field_1350+radius))){
            if(!(raw instanceof class_1309 target)||com.innerdimension.projection.ProjectionEntityPolicy.ignores(raw)||!raw.method_5805()||raw.method_7325()||!contains(raw.method_73189(),false)||BossDomains.protectedBy(this,raw))continue;
            EffectLeaseManager.apply(target,"minecraft:glowing","boss_exposure",lease,20,0,now);exposed.add(target);
            boolean charging=w.method_18868().method_18896(class_4140.field_38858)||w.method_18868().method_18896(class_4140.field_38857);
            if(charging&&w.method_18868().method_18904(class_4140.field_22355).orElse(null)==target)continue;
            var delta=target.method_33571().method_1020(w.method_33571());int steps=Math.min(256,(int)Math.ceil(delta.method_1033()));
            for(int i=0;i<steps;i++){var point=w.method_33571().method_1019(delta.method_1021(i/(double)Math.max(1,steps)));world.method_65096(class_2398.field_38908,point.field_1352,point.field_1351,point.field_1350,1,0,0,0,0);}
            if(!BossDomains.protectedBy(this,target))target.method_64397(world,DomainDamageTypes.source(world,"inner_dimension:boss_sonic_boom",w,w),BossDomainConfig.get().wardenDamage);
        }
    }
    void withdrawForced(){
        if(forcedTarget!=null&&entity instanceof class_7260 w&&w.method_18868().method_18904(class_4140.field_22355).orElse(null)==forcedTarget)w.method_18868().method_18875(class_4140.field_22355);
        forcedTarget=null;
    }
    void forceTarget(){
        var w=(class_7260)entity;
        // Withdraw only a memory still containing our exact target, never vanilla targets.
        if(forcedTarget!=null&&(BossDomains.protectedBy(this,forcedTarget)||!forcedTarget.method_5805()||forcedTarget.method_73183()!=world)){
            if(w.method_18868().method_18904(class_4140.field_22355).orElse(null)==forcedTarget)w.method_18868().method_18875(class_4140.field_22355);
            forcedTarget=null;
        }
        var target=world.method_8503().method_3760().method_14571().stream().filter(p->p.method_73183()==world&&p.method_5805()&&!p.method_7325()&&p.method_5858(w)<=36&&!BossDomains.protectedBy(this,p))
            .min(Comparator.<class_3222>comparingDouble(p->p.method_5858(w)).thenComparing(p->p.method_5667())).orElse(null);
        if(target!=null&&w.method_18868().method_18904(class_4140.field_22355).orElse(null)!=target){w.method_43274(target);forcedTarget=target;}
    }
    void boundary(){
        double r=currentRadius(false);var particle=kind==0?class_2398.field_11207:kind==1?class_2398.field_11251:class_2398.field_38002;
        for(int i=0;i<48;i++){double a=i*Math.PI/24;world.method_65096(particle,center.field_1352+Math.cos(a)*r,center.field_1351+.2,center.field_1350+Math.sin(a)*r,1,0,.1,0,0);}
    }
    void cleanup(){disabledBy.clear();immunityApplied=false;org.slf4j.LoggerFactory.getLogger("inner_dimension.boss").info("Boss domain cleanup: boss={} cast={}",id,cast);withdrawForced();if(wither!=null){wither.clear();wither=null;}exposure.clear();edges.clear();wardenWait.clear();exposed.clear();EffectLeaseManager.revokeSession(lease,ServerClock.now());forcedTarget=null;}
    void end(){if(!registered)return;BossDomains.dirty=true;cleanup();registered=false;activeAt=-1;warningUntil=-1;burnoutUntil=ServerClock.now()+(kind==0?BossDomainConfig.get().dragonBurnout:BossDomainConfig.get().witherBurnout);}
    void remove(){BossDomains.dirty=true;cleanup();registered=false;activeAt=-1;removed=true;}
}
