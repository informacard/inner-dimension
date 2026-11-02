package com.innerdimension.domain.runtime;

import com.innerdimension.domain.UnlimitedVoidRules;
import com.innerdimension.domain.compat.NoNDomainBridge;
import com.innerdimension.progression.*;
import com.innerdimension.network.UnlimitedVoidPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.*;
import java.util.*;

/** Cross-session exposure ledger. No persisted AI flag is ever changed. */
public final class UnlimitedVoidControl {
    private static final class Episode {
        final class_1309 entity; final class_1937 world; final class_243 anchor;
        final long start; final int cap; final double multiplier; double debt;
        Episode(class_1309 e,long now){
            entity=e;world=e.method_73183();anchor=e.method_73189();start=now;
            multiplier=e instanceof class_3222 p?UnlimitedVoidRules.multiplier(DomainState.get(p.method_51469()).capacity(p.method_5667()),Math.max(0,NoNDomainBridge.currentEnergy(p)),NoNDomainBridge.maxLibido(p)):1;
            cap=(int)Math.ceil((e instanceof class_3222 p?(InnerAdvancements.done(p,"enlightenment")?120:240):300)*multiplier);
            e.method_6075();
        }
    }
    private static final Map<UUID,Episode> EPISODES=new HashMap<>();
    private static final Map<UUID,Long> IMMUNE=new HashMap<>();
    private static final Set<class_1309> EXPOSED=new HashSet<>();
    private static final Set<UUID> SPENT=new HashSet<>();
    private UnlimitedVoidControl(){}
    public static void expose(class_1309 e){if(e.method_5805()&&!e.method_7325())EXPOSED.add(e);}
    public static boolean locked(class_1297 e){return !e.method_73183().method_8608()&&EPISODES.containsKey(e.method_5667());}
    public static void tick(long now){
        Set<UUID> exposedIds=new HashSet<>();for(var e:EXPOSED)exposedIds.add(e.method_5667());
        SPENT.retainAll(exposedIds);
        IMMUNE.entrySet().removeIf(e->e.getValue()<=now);
        for(var e:EXPOSED){var id=e.method_5667();if(!SPENT.contains(id)&&!IMMUNE.containsKey(id))EPISODES.computeIfAbsent(id,k->new Episode(e,now));}
        for(var it=EPISODES.entrySet().iterator();it.hasNext();){
            var entry=it.next();var r=entry.getValue();var e=r.entity;boolean exposed=EXPOSED.contains(e);
            r.debt=UnlimitedVoidRules.debt(r.debt,exposed,r.multiplier);
            if(com.innerdimension.boss.BossDomains.domainImmune(e)||!e.method_5805()||e.method_7325()||e.method_73183()!=r.world||now-r.start>=r.cap||r.debt<=0){
                it.remove();SPENT.add(entry.getKey());
                if(e instanceof class_3222 p){IMMUNE.put(entry.getKey(),now+UnlimitedVoidRules.IMMUNITY);send(p,0);}continue;
            }
            if(e.method_73189().method_1025(r.anchor)>.0001){
                com.innerdimension.domain.world.DomainTeleportGuard.push(com.innerdimension.domain.world.DomainTeleportGuard.Reason.SYSTEM);
                try{e.method_48105((class_3218)r.world,r.anchor.field_1352,r.anchor.field_1351,r.anchor.field_1350,Set.of(),e.method_36454(),e.method_36455(),false);}
                finally{com.innerdimension.domain.world.DomainTeleportGuard.pop();}
            }
            e.method_18799(class_243.field_1353);e.field_6017=0;e.field_6037=true;
            if(e instanceof class_1308 mob)mob.method_5942().method_6340();
            if(e instanceof class_3222 p)send(p,(int)Math.min(r.cap-(now-r.start),Math.ceil(r.debt)));
        }
        EXPOSED.clear();
    }
    private static void send(class_3222 p,int ticks){if(ServerPlayNetworking.canSend(p,UnlimitedVoidPayload.ID))ServerPlayNetworking.send(p,new UnlimitedVoidPayload(ticks));}
    public static void disconnected(UUID id){var r=EPISODES.remove(id);if(r!=null)IMMUNE.put(id,DomainRuntime.now()+UnlimitedVoidRules.IMMUNITY);EXPOSED.removeIf(e->e.method_5667().equals(id));}
    public static void clear(){for(var r:EPISODES.values())if(r.entity instanceof class_3222 p)send(p,0);EPISODES.clear();IMMUNE.clear();EXPOSED.clear();SPENT.clear();}
}
