package com.innerdimension.domain;

import com.innerdimension.core.ServerClock;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.domain.ability.DebugDomainAbility;
import com.innerdimension.progression.VillagerSoulChallenge;
import com.innerdimension.projection.*;
import com.innerdimension.transport.SafeEntityTeleport;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.util.*;

/** Transient NPC-owned domains borrowing the common room. Never engraves or creates an NPC room. */
public final class DebugDomain {
    public enum Type { CLOSED, OPEN, SIMPLE }
    private record Request(UUID player,long at,Type type,int mask) {}
    private static final Map<UUID,Request> PENDING=new LinkedHashMap<>();
    private static final Map<UUID,Expansion> ACTIVE=new LinkedHashMap<>();
    private static final class Expansion {
        final class_1309 actor;
        final class_3218 world,source;
        final RoomProjectionEntity shell,interior;
        final Type type;
        final boolean noAi;
        final DomainSession session;
        DomainContest.Entry entry;
        double remaining,rate=1;
        boolean closed,backlash,movementReleased;
        Expansion(class_1309 actor,class_3218 source,RoomProjectionEntity shell,RoomProjectionEntity interior,Type type,int mask) {
            this.actor=actor;this.source=source;world=(class_3218)actor.method_73183();this.shell=shell;this.interior=interior;this.type=type;
            noAi=actor instanceof class_1308 mob&&mob.method_5987();
            remaining=type==Type.OPEN?OpenDomainBinding.duration(0):DomainLifetime.MAX_TICKS;
            session=new DomainSession(actor.method_5667(),world,shell,interior,type==Type.SIMPLE?List.of(DomainContest.SIMPLE):List.of());
            session.putState("debug_mask",mask);session.capture(ServerClock.now());
        }
        void restoreAi(){if(actor instanceof class_1308 mob)mob.method_5977(noAi);}
        void close(boolean broken) {
            if(broken&&!backlash){backlash=true;shell.breakApart();interior.breakApart();if(actor.method_5805()){actor.method_6092(new class_1293(class_7923.field_41174.method_10223(class_2960.method_12829("minecraft:weakness")).orElseThrow(),300,0));actor.method_64397(world,com.innerdimension.domain.combat.DomainDamageTypes.source(world,"minecraft:generic",null,null),5);}}
            if(closed)return;closed=true;session.markEnded();SimpleDomain.end(actor.method_5667());restoreAi();ExpansionPositionLease.releaseCaster(shell);
            shell.retract();interior.retract();
        }
    }
    private DebugDomain() {}
    public static LiteralArgumentBuilder<class_2168> command() {
        var root=class_2170.method_9247("domain").then(class_2170.method_9247("stop").executes(ctx->{clear();ctx.getSource().method_9226(()->class_2561.method_43470("已清理调试领域和等待任务。"),false);return 1;}));
        for(var type:Type.values())root.then(class_2170.method_9247(type.name().toLowerCase(Locale.ROOT))
            .executes(ctx->schedule(ctx.getSource().method_9207(),type,"111111"))
            .then(class_2170.method_9244("objects",StringArgumentType.word())
                .suggests((ctx,builder)->{for(var example:List.of("111111","000000","100000","010000","001000","000100","000010","000001"))builder.suggest(example);return builder.buildFuture();})
                .executes(ctx->schedule(ctx.getSource().method_9207(),type,StringArgumentType.getString(ctx,"objects")))));
        return root;
    }
    private static int schedule(class_3222 player,Type type,String digits) {
        int mask;try{mask=DebugDomainRules.parseMask(digits);}catch(IllegalArgumentException invalid){player.method_7353(class_2561.method_43470(invalid.getMessage()),false);return 0;}
        PENDING.put(player.method_5667(),new Request(player.method_5667(),ServerClock.now()+DebugDomainRules.DELAY_TICKS,type,mask));
        player.method_7353(class_2561.method_43470("2 秒后由当时离你最近的生物展开 "+type.name().toLowerCase(Locale.ROOT)+" 领域；对象 "+digits),false);return 1;
    }
    public static boolean has(UUID owner){return ACTIVE.containsKey(owner);}
    public static class_3218 source(UUID owner){var e=ACTIVE.get(owner);return e==null?null:e.source;}
    public static double remaining(UUID owner){var e=ACTIVE.get(owner);return e==null?0:e.remaining;}
    public static void remaining(UUID owner,double value){var e=ACTIVE.get(owner);if(e!=null)e.remaining=Math.max(0,value);}
    public static double currentRate(UUID owner){var e=ACTIVE.get(owner);return e==null?0:e.rate;}
    public static void rate(UUID owner,double value){var e=ACTIVE.get(owner);if(e!=null)e.rate=value;}
    public static void attacked(class_1297 attacker,class_1309 target){var e=ACTIVE.get(attacker.method_5667());if(e!=null&&!e.closed&&e.session.contains(target))e.session.activeObjects.add(target.method_5667());}
    public static void relocated(List<ProjectionSafety.Move> moves) {
        for(var move:moves){var e=ACTIVE.get(move.entity().method_5667());if(e==null||e.type!=Type.SIMPLE)continue;
            double dx=move.to().x()-move.from().x(),dy=move.to().y()-move.from().y(),dz=move.to().z()-move.from().z();
            for(var half:List.of(e.shell,e.interior))half.method_5814(half.method_23317()+dx,half.method_23318()+dy,half.method_23321()+dz);
            e.session.capture(ServerClock.now());
        }
    }
    public static void tick(MinecraftServer server) {
        for(var request:List.copyOf(PENDING.values()))if(ServerClock.now()>=request.at()) {
            PENDING.remove(request.player());var player=server.method_3760().method_14602(request.player());if(player==null||!player.method_5805())continue;
            class_1309 nearest=null;double distance=Double.POSITIVE_INFINITY;
            for(var entity:player.method_51469().method_27909())if(entity instanceof class_1309 living&&!(living instanceof class_1657)&&living.method_5805()&&!living.method_7325()&&!ProjectionEntityPolicy.ignores(living)) {
                double d=living.method_73189().method_1025(player.method_73189());if(d<distance){nearest=living;distance=d;}
            }
            if(nearest==null){player.method_7353(class_2561.method_43470("未找到当前维度已加载的生物。"),false);continue;}
            var old=DomainContest.entry(nearest.method_5667());if(old!=null&&old.live()){player.method_7353(class_2561.method_43470("最近的生物已有领域，请等待其结束。"),false);continue;}
            try{expand(nearest,request.type(),request.mask(),server);player.method_7353(class_2561.method_43470("调试领域已展开：").method_10852(nearest.method_5477()),false);}
            catch(RuntimeException failure){org.slf4j.LoggerFactory.getLogger("inner_dimension.debug").error("Debug NPC domain expansion failed",failure);player.method_7353(class_2561.method_43470("调试领域展开失败，详情见日志。"),false);}
        }
        for(var e:List.copyOf(ACTIVE.values()))tick(e);
    }
    private static void expand(class_1309 actor,Type type,int mask,MinecraftServer server) {
        var common=DynamicDimensionManager.getOrCreate(server,VillagerSoulChallenge.COMMON_OWNER);var world=(class_3218)actor.method_73183();
        if(!SafeEntityTeleport.prepare(actor))throw new IllegalStateException("Debug caster preparation failed");
        var feet=new class_243(actor.method_23317(),actor.method_5829().field_1322,actor.method_23321());
        if(type==Type.OPEN)feet=new class_243(Math.rint(feet.field_1352),Math.rint(feet.field_1351),Math.rint(feet.field_1350));
        var enemy=DomainContest.threat(actor);
        boolean response=DomainContest.canRespondDebug(enemy);
        var snapshot=type==Type.SIMPLE?RoomProjectionContraption.simpleSnapshot():type==Type.OPEN?RoomProjectionContraption.openSnapshot(common,world,feet,FloorPatternSettings.DEFAULT,OpenDomainBinding.radius(0)):RoomProjectionContraption.snapshot(common);
        boolean counter=type==Type.CLOSED&&response;
        if(counter){var spec=((RoomProjectionContraption)enemy.shell.getContraption()).sourceSpec();feet=enemy.shell.getAnchorVec().method_1031(spec.anchorX(),spec.anchorY()-.01,spec.anchorZ());}
        if(type==Type.OPEN&&enemy!=null&&!enemy.open()){DomainContest.paintClosed(snapshot,feet,enemy,common,FloorPatternSettings.DEFAULT);if(!response)DomainContest.clip(snapshot,feet,enemy);}
        var shell=new RoomProjectionEntity(RoomProjectionManager.ENTITY_TYPE,world);var interior=new RoomProjectionEntity(RoomProjectionManager.ENTITY_TYPE,world);
        Expansion e=null;
        try {
            shell.initialize(type==Type.CLOSED?snapshot.part(true):snapshot.emptyPart(),feet,true,actor.method_5667());
            interior.initialize(type==Type.CLOSED?snapshot.part(false):snapshot,feet,false,actor.method_5667());
            shell.presentation(type==Type.SIMPLE?"简易领域":"",0xffffff);interior.presentation(type==Type.SIMPLE?"简易领域":"",0xffffff);
            if(type!=Type.CLOSED){shell.beginInterior();interior.beginInterior();}
            if(type==Type.SIMPLE){shell.stabilize();interior.stabilize();}
            if(type!=Type.SIMPLE)interior.mirror(new ProjectionMirror(common,shell,interior));
            e=new Expansion(actor,common,shell,interior,type,mask);
            if(type!=Type.SIMPLE){DomainContest.announce(shell);var desired=counter?null:type==Type.OPEN?ProjectionSafety.openExpansionLanding(snapshot,shell,actor):ProjectionSafety.expansionLanding(snapshot,common,shell,actor);var moves=ProjectionSafety.prepareExpansion(shell,interior,actor,desired);ProjectionSafety.apply(moves);RoomProjectionManager.relocated(moves);ExpansionPositionLease.hold(moves,shell,interior);}
            if(!world.method_8649(shell)||!world.method_8649(interior))throw new IllegalStateException("Debug domain spawn rejected");
            if(actor instanceof class_1308 mob)mob.method_5977(true);
            ACTIVE.put(actor.method_5667(),e);var current=e;
            e.entry=DomainContest.add(actor.method_5667(),shell,interior,5,type==Type.SIMPLE,()->current.close(true));
            if(counter)DomainContest.pair(enemy,e.entry);else if(type!=Type.SIMPLE)DomainContest.pairOpen(e.entry,enemy,response);
            if(type==Type.SIMPLE)SimpleDomain.start(actor,enemy==null?0:enemy.strength,()->current.close(false));
        } catch(RuntimeException failure) {if(e!=null){e.close(false);ACTIVE.remove(actor.method_5667(),e);}shell.method_31472();interior.method_31472();throw failure;}
    }
    private static void tick(Expansion e) {
        if(e.closed){if(e.shell.elapsed(0)>=e.shell.closeTicks()){e.shell.method_31472();e.interior.method_31472();ACTIVE.remove(e.actor.method_5667(),e);}return;}
        if(!e.actor.method_5805()||e.actor.method_31481()||e.actor.method_73183()!=e.world||e.shell.method_31481()||e.interior.method_31481()){e.close(false);return;}
        if(e.type!=Type.SIMPLE){e.remaining-=e.rate;if(e.remaining<=0&&!DomainContest.suppressed(e.actor.method_5667())){e.close(false);return;}}
        if(e.interior.mirror()!=null)e.interior.mirror().tick();else{ProjectionMirror.tickStatic(e.shell);ProjectionMirror.tickStatic(e.interior);}
        if(e.type==Type.OPEN&&!e.movementReleased&&OpenDomainRules.movementReady(e.shell.phase(),e.shell.elapsed(0)))try {var moves=ProjectionSafety.prepareStage(e.shell,e.interior,false);ProjectionSafety.applyStage(moves,e.shell,e.interior);e.interior.enableOpenCollision();ExpansionPositionLease.releaseCaster(e.shell);e.restoreAi();e.movementReleased=true;}catch(RuntimeException retry){return;}
        boolean shellReady=e.shell.phase()==ProjectionAnimation.EXPANDING&&e.shell.elapsed(0)>=e.shell.shellTicks();
        boolean interiorReady=e.shell.phase()==ProjectionAnimation.INTERIOR&&e.shell.elapsed(0)>=e.shell.interiorTicks();
        if(shellReady||interiorReady)try{var moves=ProjectionSafety.prepareStage(e.shell,e.interior,shellReady);ProjectionSafety.applyStage(moves,e.shell,e.interior);if(shellReady){e.shell.beginInterior();e.interior.beginInterior();}else{e.shell.stabilize();e.interior.stabilize();e.restoreAi();}}catch(RuntimeException retry){return;}
        if(e.type==Type.SIMPLE&&!e.entry.covers(e.actor)){e.close(false);return;}
        if((e.type==Type.OPEN||e.entry.ready())&&!DomainContest.suppressed(e.actor.method_5667()))DebugDomainAbility.INSTANCE.onTick(e.session,ServerClock.now());
    }
    public static void clear(){PENDING.clear();for(var e:List.copyOf(ACTIVE.values())){DomainContest.withdraw(e.actor.method_5667());e.close(false);e.shell.method_31472();e.interior.method_31472();}ACTIVE.clear();}
}
