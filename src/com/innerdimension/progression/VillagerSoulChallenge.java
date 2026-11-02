package com.innerdimension.progression;

import com.innerdimension.core.ServerClock;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.projection.*;
import com.innerdimension.domain.*;
import com.innerdimension.transport.SafeEntityTeleport;
import com.innerdimension.chunk.TemporaryChunkLoads;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.util.*;

/** Villager ritual and the independent lifetime of its posthumous domain. */
public final class VillagerSoulChallenge {
    public static final UUID COMMON_OWNER=UUID.nameUUIDFromBytes("inner_dimension:villager_common".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private static final org.slf4j.Logger LOGGER=org.slf4j.LoggerFactory.getLogger("inner_dimension.villager");
    private record Attempt(class_1646 villager,EnderPearlDildoEntity port){}
    private static final Map<UUID,Attempt> PENDING=new LinkedHashMap<>();
    private static final Map<UUID,Fight> FIGHTS=new LinkedHashMap<>();
    private static final class Fight {
        final class_3218 world;
        final UUID villager;
        final RoomProjectionEntity shell,interior;
        final boolean previousNoAi;
        final Set<UUID> players=new HashSet<>(),jumpPlayers=new HashSet<>();
        class_3218 source;
        RoomProjectionContraption snapshot;
        DomainContest.Entry contest;
        UUID golem;
        boolean summoned,closed,rewarded;
        long emptySince=-1;
        Fight(class_1646 actor,RoomProjectionEntity shell,RoomProjectionEntity interior,boolean noAi){
            world=(class_3218)actor.method_73183();villager=actor.method_5667();this.shell=shell;this.interior=interior;previousNoAi=noAi;
        }
        void breakDomain(){
            if(closed)return;closed=true;shell.retract();interior.retract();
            com.innerdimension.domain.runtime.AttributeLeaseManager.revokeSession(villager);
            com.innerdimension.domain.runtime.EffectLeaseManager.revokeSession(villager,com.innerdimension.domain.runtime.DomainRuntime.now());
            var actor=world.method_73284(villager);
            if(actor instanceof class_1646 mob)mob.method_5977(previousNoAi);
            var guardian=golem==null?null:world.method_73284(golem);if(guardian!=null)guardian.method_31472();
        }
        void loseDomain(){
            shell.breakApart();interior.breakApart();breakDomain();var actor=world.method_73284(villager);
            if(actor instanceof class_1646 mob&&mob.method_5805()){
                mob.method_6092(new class_1293(effect("minecraft:weakness"),300,0));
                mob.method_64397(world,com.innerdimension.domain.combat.DomainDamageTypes.source(world,"minecraft:generic",null,null),5);
            }
        }
    }
    private VillagerSoulChallenge(){}
    public static void startWorld(MinecraftServer server){DynamicDimensionManager.getOrCreate(server,COMMON_OWNER);}
    /** Called after MCA's copyPastaIntensifies has copied NoAI to the spawned replacement. */
    public static void replaced(class_1646 previous,class_1646 replacement){
        if(!(replacement.method_73183() instanceof class_3218 world)||previous.method_73183()!=world)return;
        UUID oldId=previous.method_5667(),newId=replacement.method_5667();
        if(oldId.equals(newId))return;
        Attempt pending=PENDING.remove(oldId);
        if(pending!=null)PENDING.put(newId,new Attempt(replacement,pending.port()));
        var saved=VillagerRitualState.get(world.method_8503());
        if(saved.replaceVillager(oldId,newId)){
            replacement.method_5977(true);
            replacement.field_6017=0;replacement.method_18799(class_243.field_1353);
            LOGGER.info("Villager ritual transferred after MCA replacement: previous={} replacement={}",oldId,newId);
        }
    }
    private static int slot(EnderPearlDildoEntity port,String id){
        var item=class_7923.field_41178.method_63535(class_2960.method_12829(id));
        for(int i=0;i<port.hopperInventory().method_5439();i++)if(port.hopperInventory().method_5438(i).method_7909()==item&&!port.hopperInventory().method_5438(i).method_7960())return i;
        return -1;
    }
    public static boolean landing(class_1646 villager,double distance){
        if(!(villager.method_73183() instanceof class_3218 world)||!villager.method_5805()||DynamicDimensionManager.isInnerWorld(world.method_27983())||FIGHTS.containsKey(villager.method_5667()))return false;
        if(PENDING.containsKey(villager.method_5667()))return true;
        var box=villager.method_5829();
        for(var port:world.method_8390(EnderPearlDildoEntity.class,box.method_1014(1.1),p->p.isTethered()&&DomainRitual.supported(p))){
            double y=villager.method_23318();
            if(y<port.method_23318()-.15||y>port.method_23318()+1.05||!box.method_994(port.method_5829().method_1009(0,1.1,0)))continue;
            if(RitualConditions.landingDistance(distance,y,port.method_23318())<22-1e-4||slot(port,"minecraft:obsidian")<0||slot(port,"minecraft:ender_eye")<0)continue;
            PENDING.put(villager.method_5667(),new Attempt(villager,port));return true;
        }
        return false;
    }
    public static void tick(MinecraftServer server){
        var saved=VillagerRitualState.get(server);
        for(Attempt attempt:List.copyOf(PENDING.values())){
            var actor=attempt.villager();var port=attempt.port();
            if(!actor.method_5805()||port.method_31481()||actor.method_73183()!=port.method_73183())continue;
            int obsidian=slot(port,"minecraft:obsidian"),eye=slot(port,"minecraft:ender_eye");if(obsidian<0||eye<0)continue;
            var visit=new VillagerRitualState.Visit(actor.method_5667(),actor.method_73183().method_27983().method_29177().toString(),port.method_5667(),port.method_23317()+1.5,port.method_23318(),port.method_23321(),server.method_30002().method_75260()+200,actor.method_5987(),-1);
            var common=DynamicDimensionManager.getOrCreate(server,COMMON_OWNER);
            if(!SafeEntityTeleport.move(actor,common,8.5,33,8.5,Set.of(),actor.method_36454(),actor.method_36455(),false))continue;
            var arrived=common.method_73284(visit.villager());
            if(arrived instanceof class_1646 mob){mob.method_5977(true);mob.field_6017=0;mob.method_18799(class_243.field_1353);}
            saved.put(visit);
            port.hopperInventory().method_5438(obsidian).method_7934(1);port.hopperInventory().method_5438(eye).method_7934(1);port.hopperInventory().method_5431();
        }
        PENDING.clear();
        if(!saved.visits.isEmpty()){
            var common=DynamicDimensionManager.getOrCreate(server,COMMON_OWNER);TemporaryChunkLoads.holdChunk(common,0,0);common.method_8497(0,0);
            for(var visit:List.copyOf(saved.visits.values())){
                long now=server.method_30002().method_75260();
                var actor=common.method_73284(visit.villager());
                // Chunk terrain can be available before its saved entities. Keep the return
                // record until the actor loads (or MCA transfers it), never erase it on a miss.
                if(!(actor instanceof class_1646 mob)){
                    if(ServerClock.now()%200==0)LOGGER.warn("Villager ritual waits for entity load: villager={} world={}",visit.villager(),common.method_27983().method_29177());
                    continue;
                }
                var key=class_5321.method_29179(class_7924.field_41223,class_2960.method_12829(visit.world()));var origin=server.method_3847(key);
                if(origin==null)continue;
                TemporaryChunkLoads.holdChunk(origin,((int)Math.floor(visit.x()))>>4,((int)Math.floor(visit.z()))>>4);origin.method_8497(((int)Math.floor(visit.x()))>>4,((int)Math.floor(visit.z()))>>4);
                var port=origin.method_73284(visit.port());double x=port==null?visit.x():port.method_23317()+1.5,y=port==null?visit.y():port.method_23318(),z=port==null?visit.z():port.method_23321();
                if(visit.returnAt()<0 && now<visit.due()){
                    boolean approached=server.method_3760().method_14571().stream().anyMatch(p->p.method_73183()==origin&&p.method_5805()&&!p.method_7325()&&p.method_73189().method_1025(new class_243(x,y,z))<=100);
                    if(approached)saved.put(new VillagerRitualState.Visit(visit.villager(),visit.world(),visit.port(),visit.x(),visit.y(),visit.z(),visit.due(),visit.noAi(),now+10));
                    continue;
                }
                if(visit.returnAt()>=0 && now<visit.returnAt())continue;
                ProjectionSafetySearch.Point returnPoint;
                try {
                    if(!SafeEntityTeleport.prepare(mob))continue;
                    returnPoint=ProjectionSafety.returnLanding(origin,mob,x,y,z);
                } catch(RuntimeException failure) {
                    if(ServerClock.now()%20==0)LOGGER.warn("Villager waits for safe return: villager={} preferred={},{},{}",visit.villager(),x,y,z,failure);
                    continue;
                }
                if(!SafeEntityTeleport.move(mob,origin,returnPoint.x(),returnPoint.y(),returnPoint.z(),Set.of(),mob.method_36454(),mob.method_36455(),false))continue;
                LOGGER.info("Villager returned safely: villager={} preferred={},{},{} landing={}",visit.villager(),x,y,z,returnPoint);
                var returned=origin.method_73284(visit.villager());saved.remove(visit.villager());
                if(returned instanceof class_1646 villager){
                    villager.field_6017=0;villager.method_18799(class_243.field_1353);villager.method_5977(visit.noAi());
                    boolean nearby=server.method_3760().method_14571().stream().anyMatch(p->p.method_73183()==origin&&p.method_5805()&&!p.method_7325()&&p.method_73189().method_1025(new class_243(x,y,z))<=100);
                    if(nearby)try{expand(villager,visit.noAi(),common);}catch(RuntimeException failure){villager.method_5977(visit.noAi());LOGGER.error("Villager domain expansion failed",failure);}
                }
            }
        }
        for(Fight f:List.copyOf(FIGHTS.values()))tickFight(f,server);
    }
    public static void awakenedAttack(class_1646 actor){
        if(FIGHTS.containsKey(actor.method_5667()))return;
        var common=DynamicDimensionManager.getOrCreate(((class_3218)actor.method_73183()).method_8503(),COMMON_OWNER);
        com.innerdimension.dimension.RoomShellGenerator.ensureShell(common,COMMON_OWNER);
        expand(actor,actor.method_5987(),common);
    }
    private static void expand(class_1646 actor,boolean noAi,class_3218 common){
        var snapshot=RoomProjectionContraption.snapshot(common);
        var feet=actor.method_73189();var shell=new RoomProjectionEntity(RoomProjectionManager.ENTITY_TYPE,actor.method_73183());var interior=new RoomProjectionEntity(RoomProjectionManager.ENTITY_TYPE,actor.method_73183());
        Fight fight=new Fight(actor,shell,interior,noAi);
        fight.source=common;fight.snapshot=snapshot;
        try{
            shell.initialize(snapshot.part(true),feet,true,actor.method_5667());interior.initialize(snapshot.part(false),feet,false,actor.method_5667());
            shell.presentation("？？？",0x713DA6);interior.presentation("？？？",0x713DA6);
            DomainContest.announce(shell);
            if(!SafeEntityTeleport.prepare(actor))throw new IllegalStateException("Villager caster preparation waits");
            var desired=ProjectionSafety.expansionLanding(snapshot,common,shell,actor);
            var moves=ProjectionSafety.prepareExpansion(shell,interior,actor,desired);
            ProjectionSafety.apply(moves);RoomProjectionManager.relocated(moves);ExpansionPositionLease.hold(moves,shell,interior);
            if(!fight.world.method_8649(shell)||!fight.world.method_8649(interior))throw new IllegalStateException("Villager domain spawn rejected");
            actor.method_5977(true);FIGHTS.put(actor.method_5667(),fight);
            fight.contest=DomainContest.add(actor.method_5667(),shell,interior,5,false,fight::loseDomain);
            DomainContest.pairOpen(fight.contest,null,false);
        }catch(RuntimeException failure){shell.method_31472();interior.method_31472();throw failure;}
    }
    private static void tickFight(Fight f,MinecraftServer server){
        if(f.closed){if(f.shell.elapsed(0)>=f.shell.closeTicks()){f.shell.method_31472();f.interior.method_31472();FIGHTS.remove(f.villager);}return;}
        TemporaryChunkLoads.holdChunk(f.world,((int)Math.floor(f.shell.method_23317()))>>4,((int)Math.floor(f.shell.method_23321()))>>4);
        if(f.shell.method_31481()||f.interior.method_31481()){f.breakDomain();return;}
        ProjectionMirror.tickStatic(f.shell);ProjectionMirror.tickStatic(f.interior);
        var actor=f.world.method_73284(f.villager);
        if(!f.summoned&&(actor==null||!actor.method_5805())){f.breakDomain();return;}
        boolean shellReady=f.shell.phase()==ProjectionAnimation.EXPANDING&&f.shell.elapsed(0)>=Math.max(20,f.shell.shellTicks());
        boolean interiorReady=f.shell.phase()==ProjectionAnimation.INTERIOR&&f.shell.elapsed(0)>=f.shell.interiorTicks();
        if(shellReady||interiorReady){
            try{
                var moves=ProjectionSafety.prepareStage(f.shell,f.interior,shellReady);ProjectionSafety.applyStage(moves,f.shell,f.interior);
                if(shellReady){f.shell.beginInterior();f.interior.beginInterior();}else{f.shell.stabilize();f.interior.stabilize();}
            }catch(RuntimeException failure){if(ServerClock.now()%20==0)LOGGER.warn("Villager projection waiting for safe migration",failure);return;}
        }
        if(!f.contest.ready()){syncJump(f,List.of());return;}
        List<class_3222> inside=server.method_3760().method_14571().stream().filter(p->p.method_73183()==f.world&&p.method_5805()&&!p.method_7325()&&f.contest.covers(p)).toList();
        syncJump(f,inside);
        if(inside.isEmpty()){
            if(f.emptySince<0)f.emptySince=ServerClock.now();
            // Safety migration may temporarily put players outside while the room settles.
            // Never collapse on the first stable tick before a guardian can be summoned.
            if(ServerClock.now()-f.emptySince>=200){LOGGER.info("Villager domain closed: no player inside for 10 seconds, villager={} summoned={} bounds={}",f.villager,f.summoned,f.contest.bounds());f.breakDomain();}
            return;
        }
        f.emptySince=-1;
        if(!f.summoned){
            var target=inside.getFirst();var type=class_7923.field_41177.method_63535(class_2960.method_12829("minecraft:iron_golem"));
            var golem=type.method_5883(f.world,class_3730.field_16460);
            if(!(golem instanceof class_1439 guardian)){LOGGER.warn("Villager domain guardian creation failed: {}",golem);f.breakDomain();return;}
            guardian.method_5814(target.method_23317(),target.method_5829().field_1325+.2,target.method_23321());
            try {
                var landing=ProjectionSafety.occupantLanding(f.snapshot,f.source,f.interior,guardian);
                guardian.method_5814(landing.x(),landing.y(),landing.z());
            } catch(RuntimeException failure) {
                if(ServerClock.now()%20==0)LOGGER.warn("Villager guardian waits for safe supported spawn",failure);
                return;
            }
            guardian.method_5980(target);guardian.method_5971();
            if(!f.world.method_8649(guardian)){LOGGER.warn("Villager domain guardian spawn rejected: {}",f.villager);f.breakDomain();return;}
            f.golem=guardian.method_5667();f.summoned=true;
            LOGGER.info("Villager domain guardian spawned: villager={} guardian={} target={}",f.villager,f.golem,target.method_5667());
            // Only a successful expansion sacrifices the villager. No ordinary death awards a core.
            if(actor instanceof class_1309 living) {
                // This is the ritual sacrifice, not an attack that inherited damage immunity can cancel.
                living.method_6033(0);
                living.method_6078(com.innerdimension.domain.combat.DomainDamageTypes.source(f.world,"minecraft:generic_kill",null,null));
            }
        }
        var guardian=f.world.method_73284(f.golem);
        if(!(guardian instanceof class_1309 golem)||!golem.method_5805()){
            // Death event grants the reward; unloading/removal never does.
            f.breakDomain();return;
        }
        for(var player:inside){
            if(f.players.add(player.method_5667())&&!SimpleDomain.active(player)){
                com.innerdimension.domain.runtime.EffectLeaseManager.apply(player,"minecraft:weakness","VILLAGER",f.villager,300,2,com.innerdimension.domain.runtime.DomainRuntime.now());
                com.innerdimension.domain.runtime.EffectLeaseManager.apply(player,"minecraft:slowness","VILLAGER",f.villager,300,2,com.innerdimension.domain.runtime.DomainRuntime.now());
            }
        }
        if(guardian instanceof class_1308 mob)mob.method_5980(inside.stream().min(Comparator.comparingDouble(p->p.method_73189().method_1025(mob.method_73189()))).orElseThrow());
    }
    private static void syncJump(Fight f,List<class_3222> inside){
        Set<UUID> affected=new HashSet<>();
        for(var player:inside)if(!SimpleDomain.active(player)){
            // Zero-valued transient marker syncs to client prediction without changing other bonuses.
            com.innerdimension.domain.runtime.AttributeLeaseManager.apply(player,class_5134.field_23728,"villager_jump",0,class_1322.class_1323.field_6331,true,f.villager,"VILLAGER");
            affected.add(player.method_5667());
        }
        for(UUID previous:f.jumpPlayers)if(!affected.contains(previous)){
            var player=f.world.method_8503().method_3760().method_14602(previous);
            if(player!=null)com.innerdimension.domain.runtime.AttributeLeaseManager.apply(player,class_5134.field_23728,"villager_jump",0,class_1322.class_1323.field_6331,false,f.villager,"VILLAGER");
        }
        f.jumpPlayers.clear();f.jumpPlayers.addAll(affected);
    }
    private static class_6880<class_1291> effect(String id){return class_7923.field_41174.method_10223(class_2960.method_12829(id)).orElseThrow();}
    public static boolean blocksPlacement(class_1297 player){
        return FIGHTS.values().stream().anyMatch(f->!f.closed&&f.contest.ready()&&f.contest.covers(player));
    }
    public static void died(class_1309 entity,class_1282 source){
        for(Fight f:List.copyOf(FIGHTS.values()))if(!f.closed&&f.summoned&&!f.rewarded&&entity.method_5667().equals(f.golem)){
            var attacker=source==null?null:source.method_5529();
            // A player or its tame summon must actually win the challenge while somebody survives.
            boolean playerKill=attacker instanceof class_1657||attacker instanceof class_1321 tame&&tame.method_35057() instanceof class_1657;
            boolean survivors=f.world.method_8503().method_3760().method_14571().stream().anyMatch(p->p.method_5805()&&!p.method_7325()&&f.contest.covers(p));
            if(playerKill&&survivors){
                for(var survivor:f.world.method_8503().method_3760().method_14571())if(survivor.method_5805()&&!survivor.method_7325()&&f.contest.covers(survivor))com.innerdimension.progression.InnerAdvancements.grant(survivor,"disciple_slayer");
                f.rewarded=true;f.world.method_8649(new class_1542(f.world,entity.method_23317(),entity.method_23318(),entity.method_23321(),new class_1799(com.innerdimension.item.ModItems.VILLAGER_SOUL_CORE)));
            }
            f.shell.breakApart();f.interior.breakApart();f.breakDomain();
        }
    }
    public static void clear(){for(Fight f:List.copyOf(FIGHTS.values())){f.breakDomain();f.shell.method_31472();f.interior.method_31472();}FIGHTS.clear();PENDING.clear();}
}
