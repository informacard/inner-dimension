package com.innerdimension.domain;

import com.innerdimension.core.ServerClock;
import com.innerdimension.projection.*;
import com.innerdimension.boss.BossDomains;
import com.innerdimension.progression.InnerAdvancements;
import net.minecraft.server.MinecraftServer;
import net.minecraft.class_238;
import java.util.*;

/** Per-contact verdicts, one shared resource account per cast, simultaneous outcome commit. */
final class MultiDomainContest {
    record Key(DomainContest.Entry first,DomainContest.Entry second) {}
    static final class Edge {
        final int mode; // 1 open/open, 2 open cast inside an older closed domain, 3 outer pressure
        final double average,cost,firstStrength,secondStrength;
        final long finish;
        final boolean prison;
        boolean decided;
        Edge(int mode,double average,double cost,double firstStrength,double secondStrength,long finish,boolean prison){
            this.mode=mode;this.average=average;this.cost=cost;this.firstStrength=firstStrength;this.secondStrength=secondStrength;this.finish=finish;this.prison=prison;
        }
    }
    private static final Map<Key,Edge> EDGES=new LinkedHashMap<>();
    private static final Map<DomainContest.Entry,MultiDomainBudget> ACCOUNTS=new IdentityHashMap<>();
    private static final Set<UUID> DEBITED=new HashSet<>();
    private static final Map<DomainContest.Entry,Double> HUD_RATES=new IdentityHashMap<>();
    private static final Set<DomainContest.Entry> PAINTED=Collections.newSetFromMap(new IdentityHashMap<>());
    private static List<Key> paintedEdges=List.of();
    private static long debitTick=-1;
    private MultiDomainContest() {}
    static boolean opponents(UUID a,UUID b){return EDGES.entrySet().stream().anyMatch(i->(i.getValue().mode==2?live(i.getKey()):contact(i.getKey()))&&(i.getKey().first.owner.equals(a)&&i.getKey().second.owner.equals(b)||i.getKey().first.owner.equals(b)&&i.getKey().second.owner.equals(a)));}
    private static double deadline(Key k,Edge e){return e.mode==2?Math.max(0,e.finish-ServerClock.now()):Math.min(remainingTicks(k.first),remainingTicks(k.second));}
    static DomainContest.Entry opponent(DomainContest.Entry e){
        Key best=null;double soonest=Double.POSITIVE_INFINITY;
        for(var item:EDGES.entrySet()){var k=item.getKey();if((k.first==e||k.second==e)&&(item.getValue().mode==2?live(k):contact(k))&&!item.getValue().decided){double time=deadline(k,item.getValue());if(time<soonest){soonest=time;best=k;}}}
        return best==null?null:best.first==e?best.second:best.first;
    }
    static int clashTicks(DomainContest.Entry e){var other=opponent(e);if(other==null)return 0;for(var item:EDGES.entrySet()){var k=item.getKey();if(k.first==e&&k.second==other||k.first==other&&k.second==e)return (int)Math.ceil(deadline(k,item.getValue()));}return 0;}
    static double remainingTicks(DomainContest.Entry e){return DomainContest.remaining(e)/HUD_RATES.getOrDefault(e,1d);}
    static boolean protectedFrom(UUID attacker,UUID defender){return EDGES.entrySet().stream().anyMatch(i->i.getValue().mode==3&&i.getKey().first.owner.equals(attacker)&&i.getKey().second.owner.equals(defender)&&contact(i.getKey()));}
    static boolean debited(UUID owner){return debitTick==ServerClock.now()&&DEBITED.contains(owner);}
    private static boolean live(Key k){return k.first.live()&&k.second.live()&&k.first.shell.method_73183()==k.second.shell.method_73183();}
    private static boolean contact(Key k){return live(k)&&k.first.geometryContains(k.second.bounds());}
    static boolean prisonAllows(UUID owner,class_238 box){
        for(var item:EDGES.entrySet())if(item.getValue().prison){var k=item.getKey();if(k.first.owner.equals(owner)&&live(k)&&!k.second.bounds().method_1011(1).method_994(box))return false;}
        return true;
    }
    static boolean prisonAllowsPoint(UUID owner,double x,double y,double z){
        for(var item:EDGES.entrySet())if(item.getValue().prison){var k=item.getKey();if(k.first.owner.equals(owner)&&live(k)&&!k.second.bounds().method_1011(1).method_1008(x,y,z))return false;}
        return true;
    }
    static void add(DomainContest.Entry a,DomainContest.Entry b){
        if(a==b||a.simple||b.simple||!a.open()&&!b.open()||BossDomains.cooperate(a,b))return;
        var first=a.open()?(b.open()&&b.sequence<a.sequence?b:a):b;
        var second=first==a?b:a;var key=new Key(first,second);
        if(!contact(key)||EDGES.containsKey(key))return;
        boolean internal=false,response=false;
        if(!second.open()&&first.sequence>second.sequence){
            var caster=((net.minecraft.class_3218)first.shell.method_73183()).method_73284(first.owner);
            internal=caster!=null&&second.bounds().method_1011(1).method_994(caster.method_5829());
            response=ServerClock.now()-second.began<DomainContestRules.REACTION_TICKS;
        }
        double left=first.strength,right=second.strength,cost=0;int mode=second.open()?1:internal?2:3;
        if(mode==2){if(RoomProjectionManager.reversed(second.owner))right=DomainContestRules.penalized(right);if(!response)left=DomainContestRules.penalized(left);}
        if(mode==3&&!RoomProjectionManager.reversed(second.owner))right=DomainContestRules.penalized(right);
        double avg=DomainContestRules.average(left,right);
        if(mode==3&&!RoomProjectionManager.reversed(second.owner))cost=avg*20+60;
        EDGES.put(key,new Edge(mode,avg,cost,left,right,ServerClock.now()+DomainContestRules.clashTicks(left,right),mode==2&&!response));
        SimpleDomain.forceEnemy(first.owner,second.owner);SimpleDomain.forceEnemy(second.owner,first.owner);
        for(var e:List.of(first,second)){
            var p=e.shell.method_73183().method_8503().method_3760().method_14602(e.owner);
            if(p!=null&&InnerAdvancements.done(p,"mental_seeding"))InnerAdvancements.grant(p,"domain_contest");
        }
        DomainContest.grantShinjuku(first,second);
        com.innerdimension.sound.ModSounds.at(first.shell.method_73183(),first.shell.method_24515(),"domain_clash",1f);
    }
    static void tick(MinecraftServer server){
        DEBITED.clear();HUD_RATES.clear();debitTick=ServerClock.now();
        EDGES.keySet().removeIf(k->!live(k));ACCOUNTS.keySet().removeIf(e->!e.live());
        var entries=DomainContest.entries();
        for(var first:entries)if(first.open()&&first.live())for(var second:entries)if(!second.simple&&second.live())add(first,second);
        var rates=new LinkedHashMap<DomainContest.Entry,Double>();var dues=new IdentityHashMap<DomainContest.Entry,Double>();
        var failed=new LinkedHashSet<DomainContest.Entry>();
        for(var item:EDGES.entrySet()){
            var k=item.getKey();var e=item.getValue();
            if(!live(k)||e.decided)continue;
            if(e.mode==2){
                // Once trapped, leaving the contact volume cannot cancel the pending verdict.
                rates.merge(k.first,1d,Math::max);rates.merge(k.second,1d,Math::max);
                if(e.finish<=ServerClock.now()){
                    e.decided=true;int verdict=DomainContestRules.result(e.firstStrength,e.secondStrength,com.innerdimension.config.InnerConfig.get().domainClashDrawThreshold);
                    if(verdict<=0)failed.add(k.first);if(verdict>=0)failed.add(k.second);
                }
            }else if(contact(k)){
                rates.merge(k.first,e.average,Math::max);rates.merge(k.second,e.mode==1?e.average:1,Math::max);
                if(e.mode==3)dues.merge(k.second,e.cost,Math::max);
            }
        }
        for(var item:rates.entrySet()){
            var e=item.getKey();var budget=ACCOUNTS.computeIfAbsent(e,k->new MultiDomainBudget());
            double baseRate=BossDomains.debited(e.owner)?BossDomains.debitRate(e.owner):(DebugDomain.has(e.owner)?DebugDomain.currentRate(e.owner):0);
            double rate=Math.max(1,item.getValue());HUD_RATES.put(e,Math.max(rate,baseRate));
            var debit=budget.debit(DomainContest.remaining(e),rate,dues.getOrDefault(e,0d),baseRate,BossDomains.paid(e));
            DomainContest.remaining(e,debit.remaining());BossDomains.recordPaid(e,budget.paid());DEBITED.add(e.owner);
            if(debit.remaining()<=0)failed.add(e);
        }
        BossDomains.collectFailures(server,failed);
        var closedWinners=DomainContest.collectClosed(failed,ServerClock.now());
        for(var e:failed)DomainContest.breakForBoss(e,server);
        DomainContest.finishClosed(closedWinners,server);
        BossDomains.finishTick(server);
        // One deterministic terrain pass over all surviving edges, rebuilding overrides as a group.
        if(ServerClock.now()%5==0||!failed.isEmpty())repaint();
    }
    private static void repaint(){
        var current=EDGES.entrySet().stream().filter(i->contact(i.getKey())&&!i.getValue().decided&&i.getValue().mode!=3).map(Map.Entry::getKey).toList();
        // Rebuild only when contact membership changes; stable waves reuse per-pair cell caches.
        if(!current.equals(paintedEdges)){
            for(var e:PAINTED)if(e.live())OpenDomainOverlap.restore(e);PAINTED.clear();paintedEdges=current;
        }
        for(var k:current){
            if(EDGES.get(k).mode==1)OpenDomainOverlap.mix(k.first,k.second);else OpenDomainOverlap.mix(k.second,k.first);
            PAINTED.add(k.first);PAINTED.add(k.second);
        }
    }
    static void withdraw(DomainContest.Entry e){EDGES.keySet().removeIf(k->k.first==e||k.second==e);ACCOUNTS.remove(e);repaint();}
    static void clear(){EDGES.clear();ACCOUNTS.clear();DEBITED.clear();HUD_RATES.clear();PAINTED.clear();paintedEdges=List.of();debitTick=-1;}
}