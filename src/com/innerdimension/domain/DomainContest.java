package com.innerdimension.domain;

import com.innerdimension.core.ServerClock;
import com.innerdimension.projection.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.network.DomainContestPayload;
import com.innerdimension.progression.InnerAdvancements;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.*;

/** Server-authoritative reaction windows and paired contests, also used by NPC challenges. */
public final class DomainContest {
    public static final String SIMPLE = "inner_dimension:self";
    public static final class Entry {
        public final long sequence=++castSequence;
        public final UUID owner;
        public final RoomProjectionEntity shell, interior;
        public final double strength;
        public final long began = ServerClock.now();
        public final boolean simple;
        final Runnable breakDomain;
        Entry partner;
        long finish = -1;
        boolean lost;
        boolean resolved;
        boolean follower;
        public double effectiveStrength;
        double npcRemaining=DomainLifetime.MAX_TICKS;
        Entry(UUID owner, RoomProjectionEntity shell, RoomProjectionEntity interior, double strength,
              boolean simple, Runnable breakDomain) {
            this.owner=owner; this.shell=shell; this.interior=interior; this.strength=strength;
            this.simple=simple; this.breakDomain=breakDomain;
            this.effectiveStrength=strength;
        }
        public boolean live() { return !lost && !shell.method_31481() && shell.phase()!=ProjectionAnimation.RETRACTING; }
        public class_238 bounds() { return shell.method_5829(); }
        public boolean open() { return !simple && ((RoomProjectionContraption)interior.getContraption()).sourceSpec().shell()==null; }
        public boolean covers(class_1297 entity) {
            return live() && entity.method_73183()==shell.method_73183() && geometryContains(entity.method_5829());
        }
        public boolean geometryContains(class_238 box) {
            if(!MultiDomainContest.prisonAllows(owner,box))return false;
            if(!open())return bounds().method_994(box);
            var spec=((RoomProjectionContraption)interior.getContraption()).sourceSpec();
            var center=interior.getAnchorVec().method_1031(spec.anchorX(),spec.anchorY(),spec.anchorZ());
            double x=center.field_1352,y=center.field_1351,z=center.field_1350;
            double radius=(spec.source().maxX()+1-spec.source().minX())*.5*interior.progress(0);
            return OpenDomainRules.intersects(box.field_1323-x,box.field_1322-y,box.field_1321-z,box.field_1320-x,box.field_1325-y,box.field_1324-z,radius);
        }
        public boolean ready() { return live() && shell.phase()==ProjectionAnimation.STABLE && !suppressed(owner); }
        public boolean partnered() { return partner!=null||MultiDomainContest.opponent(this)!=null; }
        public boolean follower() { return follower; }
    }
    private static long castSequence;
    private static final Map<UUID,Entry> ENTRIES=new LinkedHashMap<>();
    private DomainContest() {}
    public static boolean opponents(UUID first,UUID second){var own=ENTRIES.get(first);return own!=null&&own.partner!=null&&own.partner.owner.equals(second)||MultiDomainContest.opponents(first,second);}
    public static java.util.List<Entry> entries(){return java.util.List.copyOf(ENTRIES.values());}
    public static void breakForBoss(Entry e,MinecraftServer server){if(e.live())lose(e,server,null);}
    public static Entry entry(UUID owner) { return ENTRIES.get(owner); }
    public static void withdraw(UUID owner) { var e=ENTRIES.remove(owner);if(e!=null){MultiDomainContest.withdraw(e);if(e.partner!=null&&e.partner.partner==e&&e.partner.live())restore(e.partner);} }
    public static double forecastTicks(UUID owner){var e=entry(owner);return e==null?RoomProjectionManager.remaining(owner):MultiDomainContest.remainingTicks(e);}
    public static boolean debited(UUID owner){return MultiDomainContest.debited(owner);}
    public static Entry add(UUID owner, RoomProjectionEntity shell, RoomProjectionEntity interior,
                            double strength, boolean simple, Runnable broken) {
        withdraw(owner);
        Entry e=new Entry(owner,shell,interior,strength,simple,broken); ENTRIES.put(owner,e); return e;
    }
    public static Entry threat(class_1297 player) {
        for(Entry e:ENTRIES.values()) if(!e.simple && !e.open() && !e.owner.equals(player.method_5667()) && e.covers(player) && (!(player instanceof class_3222 p)||!com.innerdimension.boss.BossDomains.cooperate(p,e))) return e;
        for(Entry e:ENTRIES.values()) if(e.open() && !e.owner.equals(player.method_5667()) && e.covers(player) && (!(player instanceof class_3222 p)||!com.innerdimension.boss.BossDomains.cooperate(p,e))) return e;
        return null;
    }
    public static com.innerdimension.network.DomainHudPayload hud(boolean contest,RoomProjectionEntity first,RoomProjectionEntity second) {
        return new com.innerdimension.network.DomainHudPayload(contest,first.domainName(),first.nameColor(0),first.nameColor(1),first.nameColor(2),first.nameSplit(0),first.nameSplit(1),
            second==null?"":second.domainName(),second==null?0xffffff:second.nameColor(0),second==null?0xffffff:second.nameColor(1),second==null?0xffffff:second.nameColor(2),second==null?333:second.nameSplit(0),second==null?667:second.nameSplit(1),15,5,first.heart());
    }
    /** Called once before expansion relocation; uses the completed snapshot's full bounds. */
    public static void announce(RoomProjectionEntity shell) {
        if(com.innerdimension.boss.BossDomains.announceAlly(shell))return;
        var volume=((RoomProjectionContraption)shell.getContraption()).sourceSpec().source().bounds()
            .method_997(shell.getAnchorVec()).method_1009(0,2,0);
        for(var player:((class_3218)shell.method_73183()).method_8503().method_3760().method_14571()) {
            if(player.method_73183()!=shell.method_73183() || player.method_5667().equals(shell.owner())
                || !player.method_5805() || player.method_7325() || !volume.method_994(player.method_5829())
                || !InnerAdvancements.done(player,"disciple_slayer"))continue;
            if(ServerPlayNetworking.canSend(player,com.innerdimension.network.DomainHudPayload.ID)) {
                ServerPlayNetworking.send(player,hud(false,shell,null));
                org.slf4j.LoggerFactory.getLogger("inner_dimension.contest").info("Domain expansion announced: owner={} player={} bounds={}",shell.owner(),player.method_5667(),volume);
            }
        }
    }
    public static boolean canRespond(class_3222 player,Entry e) {
        return canRespondDebug(e)&&InnerAdvancements.done(player,"mental_seeding");
    }
    public static boolean canRespondDebug(Entry e) {
        return e!=null && !e.simple && !e.open() && (e.partner==null || !e.partner.open()) && !e.resolved && e.live()
            && ServerClock.now()-e.began<DomainContestRules.REACTION_TICKS;
    }
    public static boolean suppressed(UUID owner) {
        Entry e=entry(owner);
        return e!=null && !e.simple && !e.open() && e.live() && MultiDomainContest.opponent(e)==null && !e.resolved
            && (e.partner!=null || ServerClock.now()-e.began<DomainContestRules.REACTION_TICKS);
    }
    public static void pair(Entry first,Entry second) {
        if(first==second || !first.live() || !second.live() || first.simple || second.simple || com.innerdimension.boss.BossDomains.cooperate(first,second))return;
        if(first.open()||second.open()){MultiDomainContest.add(first,second);return;}
        if(!first.open()&&!second.open()) {
            var group=DomainContestRules.closedGroup(first,second,e->e.partner!=null&&!e.partner.open()&&e.partner.live()?e.partner:null);
            if(group.size()>=3) {
                // Detach the whole group before callbacks withdraw individual entries.
                for(var e:group){e.partner=null;e.resolved=true;}
                var server=first.shell.method_73183().method_8503();
                for(var e:group)lose(e,server,null);
                return;
            }
        }
        if(first.partner!=null||second.partner!=null)return;
        if(!first.open()&&!second.open())for(var entry:List.of(first,second))if(RoomProjectionManager.reversed(entry.owner))entry.effectiveStrength=DomainContestRules.penalized(entry.effectiveStrength);
        first.partner=second; second.partner=first;
        grantShinjuku(first,second);
        grantShinjuku(second,first);
        for(var entry:List.of(first,second)){var player=entry.shell.method_73183().method_8503().method_3760().method_14602(entry.owner);if(player!=null&&InnerAdvancements.done(player,"mental_seeding"))InnerAdvancements.grant(player,"domain_contest");}
        second.follower=true;
        long end=ServerClock.now()+DomainContestRules.clashTicks(first.effectiveStrength,second.effectiveStrength);
        first.finish=end; second.finish=end;
        com.innerdimension.sound.ModSounds.at(first.shell.method_73183(),first.shell.method_24515(),"domain_clash",1f);
        // Vanilla movement solves the union of BOTH rooms and the real world.
        second.interior.contestGhost(false);
        org.slf4j.LoggerFactory.getLogger("inner_dimension.contest").info("Domain contest paired: first={} second={} secondInteriorGhost={}",first.owner,second.owner,second.interior.contestGhost());
    }
    static void grantShinjuku(Entry open,Entry closed) {
        if(!open.open()||closed.open()
            ||!RoomProjectionManager.activeAbilities(closed.owner).contains(UnlimitedVoidRules.ID)
            ||!RoomProjectionManager.activeAbilities(open.owner).contains(DomainModes.SHRINE))return;
        var p=closed.shell.method_73183().method_8503().method_3760().method_14602(closed.owner);
        if(p!=null)InnerAdvancements.grant(p,"shinjuku_showdown");
    }
    public static void pairOpen(Entry added,Entry enemy,boolean response) {
        if(enemy!=null)MultiDomainContest.add(added,enemy);
        for(var candidate:List.copyOf(ENTRIES.values()))if(candidate!=added)MultiDomainContest.add(added,candidate);
    }
    public static boolean protectedFromOpen(UUID attacker,class_1297 target) {return MultiDomainContest.protectedFrom(attacker,target.method_5667());}
    public static boolean allowsBlock(UUID owner,class_2338 pos) {
        var e=entry(owner);if(e==null)return true;
        var point=e.interior.getAnchorVec().method_1031(pos.method_10263()+.5,pos.method_10264()+.5,pos.method_10260()+.5);
        return MultiDomainContest.prisonAllowsPoint(owner,point.field_1352,point.field_1351,point.field_1350);
    }
    public static boolean allowsPoint(UUID owner,double x,double y,double z) {return MultiDomainContest.prisonAllowsPoint(owner,x,y,z);}
    public static void clip(RoomProjectionContraption snapshot,class_243 feet,Entry closed) {
        var spec=snapshot.sourceSpec();var anchor=feet.method_1031(-spec.anchorX(),-spec.anchorY(),-spec.anchorZ());
        var bounds=closed.bounds().method_1011(1);
        snapshot.getBlocks().keySet().removeIf(pos->!bounds.method_1008(anchor.field_1352+pos.method_10263()+.5,anchor.field_1351+pos.method_10264()+.5,anchor.field_1350+pos.method_10260()+.5));
    }
    public static void paintClosed(RoomProjectionContraption snapshot,class_243 feet,Entry closed,class_3218 source,FloorPatternSettings settings) {
        class_2680[][] tile=new class_2680[16][16];
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)tile[x][z]=source.method_8320(new class_2338(x,com.innerdimension.dimension.RoomGeometry.DOMAIN_FLOOR+1,z));
        var sampler=new FloorPatternSampler(tile,settings);var spec=snapshot.sourceSpec();
        var anchor=feet.method_1031(-spec.anchorX(),-spec.anchorY(),-spec.anchorZ());
        var other=(RoomProjectionContraption)closed.interior.getContraption();
        List<ProjectionUpdatePayload.Entry> changes=new ArrayList<>();
        for(var cell:other.getBlocks().entrySet()) {
            if(!cell.getValue().comp_1342().method_26212(other.snapshotView(),cell.getKey()))continue;
            var point=closed.interior.getAnchorVec().method_1031(cell.getKey().method_10263()+.5,cell.getKey().method_10264()+.5,cell.getKey().method_10260()+.5);
            var pos=new class_2338((int)Math.floor(point.field_1352-anchor.field_1352),(int)Math.floor(point.field_1351-anchor.field_1351),(int)Math.floor(point.field_1350-anchor.field_1350));
            if(!spec.source().contains(pos)||!snapshot.validInterior(pos)&&!OpenDomainRules.contains(pos.method_10263()+.5-spec.anchorX(),pos.method_10264()+.5-spec.anchorY(),pos.method_10260()+.5-spec.anchorZ(),(spec.source().maxX()+1-spec.source().minX())*.5))continue;
            changes.add(new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(FloorPatternSampler.floorBlock(sampler.at(pos.method_10263(),pos.method_10260()))),null));
        }
        snapshot.applyBatch(changes,false,snapshot.version()+1);
    }
    private static void restore(Entry winner) {
        OpenDomainOverlap.restore(winner);
        RoomProjectionManager.lifetimeRate(winner.owner,1);
        winner.partner=null;winner.resolved=true;winner.effectiveStrength=winner.strength;
    }
    static double remaining(Entry e) { return RoomProjectionManager.active(e.owner)==null&&!DebugDomain.has(e.owner)?e.npcRemaining:RoomProjectionManager.remaining(e.owner); }
    static void remaining(Entry e,double value) { if(RoomProjectionManager.active(e.owner)==null&&!DebugDomain.has(e.owner))e.npcRemaining=Math.max(0,value);else RoomProjectionManager.remaining(e.owner,value); }
    public static double strength(class_3222 player,List<String> loadout) {
        return DomainStrength.of(loadout,com.innerdimension.progression.DomainState.get(player.method_51469()).capacity(player.method_5667()),
            Math.max(0,com.innerdimension.domain.compat.NoNDomainBridge.currentEnergy(player)),
            com.innerdimension.domain.compat.NoNDomainBridge.fertilityAmplifier(player)>=0).total();
    }
    static List<Entry> collectClosed(Set<Entry> failed,long now) {
        var winners=new ArrayList<Entry>();var processed=Collections.newSetFromMap(new IdentityHashMap<Entry,Boolean>());
        for(var e:ENTRIES.values()) {
            var other=e.partner;
            if(!e.live()||other==null||!other.live()||e.resolved||e.finish>now||!processed.add(e))continue;
            processed.add(other);e.partner=null;other.partner=null;e.resolved=other.resolved=true;
            int verdict=DomainContestRules.result(e.effectiveStrength,other.effectiveStrength,com.innerdimension.config.InnerConfig.get().domainClashDrawThreshold);
            if(verdict<=0)failed.add(e);if(verdict>=0)failed.add(other);
            if(verdict!=0)winners.add(verdict>0?e:other);
        }
        return winners;
    }
    static void finishClosed(List<Entry> winners,MinecraftServer server) {
        for(var winner:winners)if(winner.live()) {
            restore(winner);winner.interior.contestGhost(false);
            if(winner.follower&&winner.shell.phase()==ProjectionAnimation.STABLE) {
                try{var moves=ProjectionSafety.prepareStage(winner.shell,winner.interior,false);ProjectionSafety.applyStage(moves,winner.shell,winner.interior);}
                catch(RuntimeException failure){org.slf4j.LoggerFactory.getLogger("inner_dimension.contest").warn("Winning interior waits for safe relocation",failure);}
            }
            var player=server.method_3760().method_14602(winner.owner);
            if(player!=null)player.method_7353(class_2561.method_43471("inner_dimension.contest.won"),true);
        }
    }
    public static void tick(MinecraftServer server) {
        long now=ServerClock.now();
        MultiDomainContest.tick(server);
        for(Entry e:List.copyOf(ENTRIES.values())) {
            if(!e.live()) {
                if(e.partner!=null && e.partner.live()) { var winner=e.partner;restore(winner);winner.interior.contestGhost(false); }
                ENTRIES.remove(e.owner,e); continue;
            }
            if(e.partner!=null&&!e.partner.live()){restore(e);e.interior.contestGhost(false);}
            if(e.partner==null) {
                if(now-e.began>=DomainContestRules.REACTION_TICKS) e.resolved=true;
                continue;
            }
        }
        for(var player:server.method_3760().method_14571()) {
            Entry enemy=threat(player);
            boolean warning=enemy!=null && InnerAdvancements.done(player,"disciple_slayer");
            int remaining=warning&&!enemy.open()?(int)Math.max(0,DomainContestRules.REACTION_TICKS-(now-enemy.began)):0;
            var simple=SimpleDomain.get(player.method_5667());
            int time=simple==null?0:(int)Math.ceil(simple.remaining);
            Entry own=entry(player.method_5667());
            Entry display=own==null?null:own.partner;
            int clash=display==null?0:(int)Math.max(0,own.finish-now);
            int multiple=own==null?0:MultiDomainContest.clashTicks(own);
            if(multiple>0&&(clash==0||multiple<clash)){clash=multiple;display=MultiDomainContest.opponent(own);}
            if(clash>0 && display!=null && InnerAdvancements.done(player,"disciple_slayer") && ServerPlayNetworking.canSend(player,com.innerdimension.network.DomainHudPayload.ID))
                ServerPlayNetworking.send(player,hud(true,own.shell,display.shell));
            if(ServerPlayNetworking.canSend(player,DomainContestPayload.ID)) ServerPlayNetworking.send(player,
                new DomainContestPayload(warning?enemy.owner:null,remaining,time,clash));
        }
    }
    private static void lose(Entry e,MinecraftServer server,UUID winner) {
        if(e.lost)return;
        e.lost=true; DomainRuntime.end(e.owner);e.breakDomain.run();e.shell.breakApart();e.interior.breakApart();
        var player=server.method_3760().method_14602(e.owner);
        if(player!=null && player.method_5805()) {
            var effect=class_7923.field_41174.method_10223(class_2960.method_12829("minecraft:weakness")).orElseThrow();
            player.method_6092(new class_1293(effect,300,0));
            player.field_6008=0;
            player.method_64397(player.method_51469(),com.innerdimension.domain.combat.DomainDamageTypes.source(player.method_51469(),"minecraft:generic",null,null),5);
            player.method_7353(class_2561.method_43471("inner_dimension.contest.lost"),true);
        }
        if(winner!=null && entry(winner)!=null)SimpleDomain.forceEnemy(winner,e.owner);
    }
    public static void clear() { MultiDomainContest.clear();DebugDomain.clear();ENTRIES.clear();castSequence=0; SimpleDomain.clear(); }
}
