package com.innerdimension.boss;

import com.innerdimension.core.ServerClock;
import com.innerdimension.domain.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.projection.*;
import com.innerdimension.chunk.LoadedChunks;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.util.*;

/** Main-thread Boss coordinator. Never uses Entry.partner or impersonates a player session. */
public final class BossDomains {
    static final Map<UUID,BossEncounter> BOSSES=new LinkedHashMap<>();
    private static final Map<DomainContest.Entry,Group> GROUPS=new IdentityHashMap<>();
    private static final Map<DomainContest.Entry,Double> PAID=new IdentityHashMap<>();
    private static final Map<DomainContest.Entry,Set<String>> WARDEN_JUDGED=new IdentityHashMap<>();
    private static final Map<UUID,Long> DEBITED=new HashMap<>();
    private static final Set<Pair> COOPERATIVE=new HashSet<>();
    private static final BossCombatRules.ShrineGate SHRINE=new BossCombatRules.ShrineGate();
    public static boolean allowShrine(class_1309 target,long now){return !com.innerdimension.domain.runtime.DomainRelations.isBoss(target)||SHRINE.claim(target.method_5667(),now);}
    private static long sequence;
    static boolean dirty;
    record Pair(DomainContest.Entry a,DomainContest.Entry b){}
    static final class Group {
        final long sequence=++BossDomains.sequence;
        final class_243 center,original;
        final class_238 full;
        final class_1937 world;
        Group(class_243 center,class_243 original,class_238 full,class_1937 world){this.center=center;this.original=original;this.full=full;this.world=world;}
    }
    private BossDomains(){}
    public static void register(){
        BossDomainConfig.load();
        PayloadTypeRegistry.playS2C().register(com.innerdimension.network.BossDomainPayload.ID,com.innerdimension.network.BossDomainPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTING.register(s->clear());
        ServerLifecycleEvents.SERVER_STARTED.register(s->{if(BossDomainConfig.get().enabled)BossPersistence.load(s);});
        ServerLifecycleEvents.SERVER_STOPPING.register(s->{BossPersistence.save(s);clear();});
        ServerEntityEvents.ENTITY_LOAD.register((entity,world)->{
            if(isBoss(entity)&&BossDomainConfig.get().enabled) {
                var old=BOSSES.get(entity.method_5667());
                if(old!=null&&old.worldId.equals(world.method_27983().method_29177().toString())){old.world=world;old.entity=(class_1308)entity;old.missingSince=0;old.awake=false;}
                else BOSSES.put(entity.method_5667(),new BossEncounter((class_1308)entity,world));
            }
            WitherDomainEffects.loaded(entity);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity,world)->{
            var b=BOSSES.get(entity.method_5667());if(b==null)return;
            b.rememberPosition();
            var reason=entity.method_35049();
            if(!entity.method_5805() || (reason!=class_1297.class_5529.field_27000 && reason!=class_1297.class_5529.field_27001)){b.remove();BOSSES.remove(b.id);}
            else {if(!b.warden())b.end();b.awake=false;b.withdrawForced();b.entity=null;}
        });
    }
    public static boolean isBoss(class_1297 e){return e instanceof class_1510||e instanceof class_1528||e instanceof class_7260||e instanceof class_1508;}
    /** Only domain ability effects are exempted; normal player melee remains vanilla. */
    public static boolean domainImmune(class_1297 target){
        return target instanceof class_1528 wither&&wither.method_5805()&&wither.method_6872();
    }
    public static void damaged(class_1309 target){var b=BOSSES.get(target.method_5667());if(b!=null)b.noteHalfHealth();}
    public static boolean inBattle(class_3222 player){
        for(var b:BOSSES.values())if(BossDomains.context(b,player))return true;
        return false;
    }
    /** Route a caster's notices only to participants sharing its encounter. */
    public static boolean announceAlly(com.innerdimension.projection.RoomProjectionEntity shell){
        var server=shell.method_73183().method_8503();
        var caster=server.method_3760().method_14602(shell.owner());
        if(caster==null||!inBattle(caster))return false;
        for(var player:server.method_3760().method_14571()){
            if(player==caster)continue;
            boolean shared=false;
            for(var b:BOSSES.values())if(context(b,caster)&&context(b,player)){shared=true;break;}
            if(shared)player.method_7353(class_2561.method_43469("inner_dimension.boss.ally_announcement",caster.method_5477(),shell.domainName().isBlank()?class_2561.method_43471("inner_dimension.contest.unnamed"):class_2561.method_43470(shell.domainName())),false);
        }
        return true;
    }
    public static List<DomainContest.Entry> entries(){return DomainContest.entries();}
    static boolean valid(DomainContest.Entry e){
        if(!e.live())return false;
        var p=e.shell.method_73183().method_8503().method_3760().method_14602(e.owner);
        return p!=null&&p.method_5805()&&!p.method_7325()&&p.method_73183()==e.shell.method_73183();
    }
    static boolean effective(DomainContest.Entry e){return valid(e)&&(e.open()||e.shell.phase()==ProjectionAnimation.STABLE||e.shell.phase()==ProjectionAnimation.INTERIOR);}
    static boolean context(BossEncounter b,class_1297 p){
        if(b.removed||(!b.warden()&&b.entity==null)||p==null||p.method_73183()!=b.world||!p.method_5805()||p.method_7325())return false;
        if(b.contains(p.method_73189(),true))return true;
        var e=DomainContest.entry(p.method_5667());return e!=null&&e.live()&&b.intersects(e.bounds(),true);
    }
    public static boolean cooperate(DomainContest.Entry a,DomainContest.Entry b){
        if(a==null||b==null||a==b)return false;
        Pair pair=new Pair(a,b),reverse=new Pair(b,a);
        if(COOPERATIVE.contains(pair)||COOPERATIVE.contains(reverse))return true;
        var s=a.shell.method_73183().method_8503();
        var p=s.method_3760().method_14602(a.owner);var q=s.method_3760().method_14602(b.owner);
        for(var encounter:BOSSES.values())if(context(encounter,p)&&context(encounter,q)){COOPERATIVE.add(pair);return true;}
        return false;
    }
    /** Pre-expansion lookup, including IDLE encounters, before the old threat/pair paths. */
    public static boolean cooperate(class_3222 p,DomainContest.Entry e){
        if(e==null)return false;
        var own=DomainContest.entry(p.method_5667());if(own!=null&&cooperate(own,e))return true;
        var other=p.method_51469().method_8503().method_3760().method_14602(e.owner);
        for(var b:BOSSES.values())if(context(b,p)&&context(b,other))return true;
        return false;
    }
    public static class_243 sharedCenter(class_3222 p,RoomProjectionContraption snapshot,class_243 feet){
        class_238 proposed=full(snapshot,feet);
        return GROUPS.entrySet().stream().filter(x->valid(x.getKey())&&!x.getKey().open()&&cooperate(p,x.getKey()))
            .map(Map.Entry::getValue).distinct().filter(g->g.world==p.method_73183()
                &&g.full.method_1008(feet.field_1352,feet.field_1351,feet.field_1350)
                &&proposed.method_1008(g.original.field_1352,g.original.field_1351,g.original.field_1350))
            .min(Comparator.comparingLong(g->g.sequence)).map(g->g.center).orElse(feet);
    }
    static class_238 full(RoomProjectionContraption snapshot,class_243 feet){var sp=snapshot.sourceSpec();return sp.source().bounds().method_997(feet.method_1031(-sp.anchorX(),-sp.anchorY(),-sp.anchorZ()));}
    public static void added(DomainContest.Entry e,class_243 original){
        if(e.simple||e.open())return;
        var sp=((RoomProjectionContraption)e.shell.getContraption()).sourceSpec();
        var center=e.shell.getAnchorVec().method_1031(sp.anchorX(),sp.anchorY(),sp.anchorZ());
        Group found=GROUPS.entrySet().stream().filter(x->valid(x.getKey())&&cooperate(e,x.getKey()))
            .map(Map.Entry::getValue).filter(g->g.world==e.shell.method_73183()&&g.center.method_1025(center)<.0001)
            .min(Comparator.comparingLong(g->g.sequence)).orElse(null);
        GROUPS.put(e,found==null?new Group(center,original,full((RoomProjectionContraption)e.shell.getContraption(),center),e.shell.method_73183()):found);
    }
    /** Source and cast specific, realtime geometry; no immunity to vanilla Boss damage. */
    static boolean protectedBy(BossEncounter b,class_1297 target){
        if(!b.active()||target.method_73183()!=b.world||!b.contains(target.method_73189(),false))return false;
        for(var e:entries())if(effective(e)&&e.covers(target)&&(e.simple?SimpleDomain.active(target)||SimpleDomain.get(e.owner)!=null:true))return true;
        return false;
    }
    /** Use the completed geometry, independent of animation progress or ability activation. */
    static boolean willCover(DomainContest.Entry e,class_1297 target){
        if(!e.live()||target.method_73183()!=e.shell.method_73183())return false;
        if(!e.open())return e.bounds().method_994(target.method_5829());
        var sp=((RoomProjectionContraption)e.interior.getContraption()).sourceSpec();
        var center=e.interior.getAnchorVec().method_1031(sp.anchorX(),sp.anchorY(),sp.anchorZ());
        double r=(sp.source().maxX()+1-sp.source().minX())*.5;
        var box=target.method_5829();
        return com.innerdimension.projection.OpenDomainRules.intersects(box.field_1323-center.field_1352,box.field_1322-center.field_1351,box.field_1321-center.field_1350,box.field_1320-center.field_1352,box.field_1325-center.field_1351,box.field_1324-center.field_1350,r);
    }
    static boolean overlaps(BossEncounter b,DomainContest.Entry e){
        if(!e.open())return b.intersects(e.bounds(),false);
        var sp=((RoomProjectionContraption)e.interior.getContraption()).sourceSpec();
        var center=e.interior.getAnchorVec().method_1031(sp.anchorX(),sp.anchorY(),sp.anchorZ());
        double r=(sp.source().maxX()+1-sp.source().minX())*.5*e.interior.progress(0);
        double dx=b.center.field_1352-center.field_1352,dz=b.center.field_1350-center.field_1350;
        double low=b.kind==0?b.world.method_31607():b.center.field_1351-b.vertical;
        double high=b.kind==0?b.world.method_31607()+b.world.method_31605():b.center.field_1351+b.vertical;
        double dy=Math.max(low-center.field_1351,Math.max(0,center.field_1351-high));
        double horizontal=Math.max(0,Math.sqrt(dx*dx+dz*dz)-b.currentRadius(false));
        return horizontal*horizontal+dy*dy<=r*r;
    }
    public static double simpleRate(class_1297 target){double rate=1;for(var b:BOSSES.values())if(b.active()&&target.method_73183()==b.world&&b.contains(target.method_73189(),false))rate=Math.max(rate,b.simpleRate());return rate;}
    public static float healingMultiplier(class_1309 target){
        for(var b:BOSSES.values())if(b.kind==1&&b.active()&&b.contains(target.method_73189(),false)&&target.method_73183()==b.world&&!protectedBy(b,target))return BossDomainConfig.get().witherHealingMultiplier;
        return 1;
    }
    private static final Map<UUID,Double> DEBIT_RATES=new HashMap<>();
    public static double debitRate(UUID owner){return debited(owner)?DEBIT_RATES.getOrDefault(owner,1d):0;}
    public static double paid(DomainContest.Entry entry){return PAID.getOrDefault(entry,0d);}
    public static void recordPaid(DomainContest.Entry entry,double paid){PAID.merge(entry,paid,Math::max);}
    public static boolean debited(UUID owner){return DEBITED.getOrDefault(owner,-1L)==ServerClock.now();}
    /** Snapshot all edges, debit each player once, then commit all simultaneous outcomes. */
    public static void tick(MinecraftServer server){
        PENDING_FAILURES.clear();PENDING_WINS.clear();PENDING_WARDEN_WINS.clear();pendingTick=false;
        if(!BossDomainConfig.get().enabled)return;
        long now=ServerClock.now();
        if(now%100==0)SHRINE.expire(com.innerdimension.domain.runtime.DomainRuntime.now());
        GROUPS.keySet().removeIf(e->!valid(e));PAID.keySet().removeIf(e->!valid(e));WARDEN_JUDGED.keySet().removeIf(e->!valid(e));
        COOPERATIVE.removeIf(p->!valid(p.a)||!valid(p.b));DEBITED.clear();DEBIT_RATES.clear();
        for(var b:BOSSES.values().stream().sorted(Comparator.comparing(x->x.id)).toList()){
            b.before(now,server);
            boolean immune=b.entity!=null&&domainImmune(b.entity);
            if(immune&&!b.immunityApplied){
                EffectLeaseManager.shieldEntity(b.entity,com.innerdimension.domain.combat.DomainDamagePipeline.now());
                AttributeLeaseManager.revokeEntity(b.entity);
            }
            b.immunityApplied=immune;
        }
        var rates=new IdentityHashMap<DomainContest.Entry,Double>();
        var dues=new IdentityHashMap<DomainContest.Entry,Double>();
        var failures=Collections.newSetFromMap(new IdentityHashMap<DomainContest.Entry,Boolean>());
        var wins=new LinkedHashSet<BossEncounter>();
        var wardenWins=new IdentityHashMap<BossEncounter,Set<DomainContest.Entry>>();
        for(var b:BOSSES.values()) {
            if(!b.active())continue;
            Map<Object,List<DomainContest.Entry>> groups=new LinkedHashMap<>();
            for(var e:entries())if(!e.simple&&effective(e)&&RoomProjectionManager.remaining(e.owner)>0&&e.shell.method_73183()==b.world&&overlaps(b,e))
                groups.computeIfAbsent(e.open()?e:GROUPS.getOrDefault(e,null)==null?e:GROUPS.get(e),k->new ArrayList<>()).add(e);
            var participants=groups.values().stream().flatMap(List::stream).map(e->e.owner).distinct().toList();
            if(participants.size()>1)for(var id:participants){var player=server.method_3760().method_14602(id);if(player!=null)com.innerdimension.progression.InnerAdvancements.grant(player,"manual/boss_together");}
            for(var item:groups.entrySet()) {
                var members=item.getValue();double p=members.stream().mapToDouble(e->e.strength).max().orElse(0);
                var edge=b.edges.computeIfAbsent(item.getKey(),k->new BossContestRules.Edge(BossDomainConfig.get().virtualTicks,now-b.activeAt));
                var pressure=BossContestRules.pressure(p,b.strength());
                for(var e:members){rates.merge(e,e.open()?pressure.playerRate():1,Math::max);if(!e.open())dues.merge(e,pressure.closedCost(),Math::max);}
                if(b.warden()) {
                    edge.contactTicks++;
                    String cityKey=b.worldId+"/"+b.city;
                    var pending=members.stream().filter(e->!WARDEN_JUDGED.getOrDefault(e,Set.of()).contains(cityKey)).toList();
                    boolean decide=false;
                    for(var e:pending)if(b.wardenWait.merge(e,1,Integer::sum)>=BossContestRules.waitTicks(p,b.strength()))decide=true;
                    if(decide){
                        for(var e:pending)WARDEN_JUDGED.computeIfAbsent(e,k->new HashSet<>()).add(cityKey);
                        edge.decided=true;int result=BossContestRules.verdict(p,b.strength(),com.innerdimension.config.InnerConfig.get().domainClashDrawThreshold);
                        if(result>0){wins.add(b);wardenWins.computeIfAbsent(b,k->Collections.newSetFromMap(new IdentityHashMap<>())).addAll(members);}else if(result<0)failures.addAll(members);
                    }
                } else if(edge.step(pressure.bossRate()))wins.add(b);
            }
            // Entries are retained while casts live even when geometry temporarily separates.
            b.edges.keySet().removeIf(k->k instanceof DomainContest.Entry e?!valid(e):!GROUPS.containsValue(k));
        }
        for(var item:rates.entrySet()) {
            var e=item.getKey();double paid=PAID.getOrDefault(e,0d),due=dues.getOrDefault(e,0d);
            var debit=BossContestRules.debit(RoomProjectionManager.remaining(e.owner),item.getValue(),paid,due);double remaining=debit.remaining();
            PAID.put(e,debit.paid());RoomProjectionManager.remaining(e.owner,remaining);DEBITED.put(e.owner,now);DEBIT_RATES.put(e.owner,Math.max(1,item.getValue()));
            if(remaining<=0)failures.add(e);
        }
        PENDING_FAILURES.addAll(failures);PENDING_WINS.addAll(wins);PENDING_WARDEN_WINS.putAll(wardenWins);pendingTick=true;
    }
    private static final Set<DomainContest.Entry> PENDING_FAILURES=new LinkedHashSet<>();
    private static final Set<BossEncounter> PENDING_WINS=new LinkedHashSet<>();
    private static final Map<BossEncounter,Set<DomainContest.Entry>> PENDING_WARDEN_WINS=new IdentityHashMap<>();
    private static boolean pendingTick;
    public static void collectFailures(MinecraftServer server,Set<DomainContest.Entry> failures){
        for(var e:PENDING_FAILURES){
            var player=server.method_3760().method_14602(e.owner);
            if(player!=null)for(var b:BOSSES.values())if(b.active()&&e.shell.method_73183()==b.world&&overlaps(b,e))
                com.innerdimension.progression.InnerAdvancements.grant(player,"manual/boss_"+new String[]{"dragon","wither","warden"}[b.kind]);
        }
        failures.addAll(PENDING_FAILURES);PENDING_FAILURES.clear();
    }
    public static void finishTick(MinecraftServer server){
        if(!pendingTick)return;pendingTick=false;long now=ServerClock.now();
        var wins=PENDING_WINS;var wardenWins=PENDING_WARDEN_WINS;
        for(var b:wins)if(b.warden())b.disable(now,wardenWins.getOrDefault(b,Set.of()));else b.end();
        for(var b:List.copyOf(BOSSES.values()))if(b.active())b.effects(now,server);
        for(var p:server.method_3760().method_14571())sendHud(p,now);
        BOSSES.values().removeIf(b->b.removed);
        if(dirty||now%100==0){BossPersistence.save(server);dirty=false;}
        PENDING_WINS.clear();PENDING_WARDEN_WINS.clear();
    }
    static void sendHud(class_3222 p,long now){
        if(!ServerPlayNetworking.canSend(p,com.innerdimension.network.BossDomainPayload.ID))return;
        List<com.innerdimension.network.BossDomainPayload.View> views=new ArrayList<>();
        var own=DomainContest.entry(p.method_5667());
        for(var b:BOSSES.values())if((b.active()||b.warning())&&context(b,p)){
            Object key=own==null?null:GROUPS.containsKey(own)?GROUPS.get(own):own;var edge=b.edges.get(key);
            double virtual=edge==null?-1:edge.remaining;
            double rate=own!=null&&own.open()&&b.active()&&effective(own)&&overlaps(b,own)?BossContestRules.pressure(own.strength,b.strength()).playerRate():1;
            views.add(new com.innerdimension.network.BossDomainPayload.View(b.id,b.cast,b.kind,b.warning(),b.warden()?-1:(int)Math.max(0,b.deadline-now),virtual,own==null?-1:Math.min(RoomProjectionManager.remaining(own.owner)/Math.max(1,rate),DomainContest.forecastTicks(own.owner)),b.exposure.getOrDefault(p.method_5667(),0),b.disabledUntil>now?(int)(b.disabledUntil-now):0,b.active()&&b.disabledUntil<=now&&p.method_73183()==b.world&&b.contains(p.method_73189(),false)));
            if(views.size()==8)break;
        }
        ServerPlayNetworking.send(p,new com.innerdimension.network.BossDomainPayload(now,views));
    }
    public static void crystal(class_1510 dragon,class_1511 crystal,class_1282 source){var b=BOSSES.get(dragon.method_5667());if(b!=null)b.crystal(crystal,source);}
    static void clear(){PENDING_FAILURES.clear();PENDING_WINS.clear();PENDING_WARDEN_WINS.clear();pendingTick=false;SHRINE.clear();for(var b:BOSSES.values())b.cleanup();BOSSES.clear();GROUPS.clear();PAID.clear();WARDEN_JUDGED.clear();DEBITED.clear();DEBIT_RATES.clear();COOPERATIVE.clear();sequence=0;}
}
