package com.innerdimension.domain.ability;

import com.innerdimension.domain.DomainModes;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.domain.world.DomainTeleportGuard;
import net.minecraft.*;
import java.util.*;

/** One harmless slime per object and session, with a shared physical movement lock. */
public final class D77 implements DomainAbility {
    public static final D77 INSTANCE=new D77();
    private static final String SHELL="inner_dimension:slime_cage";
    private record Cage(class_1621 slime,class_243 anchor){}
    private static final Map<class_1309,Map<UUID,Cage>> LOCKS=new WeakHashMap<>();
    public String id(){return DomainModes.SLIME_LARGE;}
    public String code(){return "D77";}
    public static boolean shell(class_1297 e){return e.method_5752().contains(SHELL);}
    public static boolean locked(class_1297 e){
        if(e.method_73183().method_8608())return false;
        var cages=LOCKS.get(e);if(cages==null)return false;
        return cages.keySet().stream().anyMatch(id->{var s=DomainSessionIndex.bySession(id);return s!=null&&!s.ended()&&s.phaseActive()&&DomainObjects.matches(s,"D77",e);});
    }
    public void onTick(DomainSession s,long now){
        if(!(s.world instanceof class_3218 world))return;
        List<class_1309> current=DomainObjects.inside(s,code()).stream().filter(e->!shell(e)).toList();
        List<class_1309> previous=s.state("d77_objects",List.of());
        for(var e:previous)if(!current.contains(e))release(e,s.sessionId);
        for(var e:current){
            Map<UUID,Cage> cages=LOCKS.computeIfAbsent(e,k->new HashMap<>());
            Cage cage=cages.get(s.sessionId);
            if(cage==null){
                class_243 anchor=cages.isEmpty()?e.method_33571():cages.values().iterator().next().anchor();
                var created=D47D69Support.create(world,"minecraft:slime",anchor.field_1352,anchor.field_1351,anchor.field_1350);
                if(!(created instanceof class_1621 slime))continue;
                slime.method_5977(true);slime.method_5875(true);slime.method_5648(true);
                slime.field_5960=true;
                slime.method_5780(SHELL);slime.method_5780(DomainSummons.SESSION+s.sessionId);
                slime.method_5780(DomainSummons.OWNER+s.ownerId);
                resize(slime,e);
                if(!world.method_8649(slime))continue;
                cage=new Cage(slime,anchor);cages.put(s.sessionId,cage);
            }
            if(cage.slime().method_31481()){
                release(e,s.sessionId);continue;
            }
            class_243 anchor=cage.anchor();
            if(e.method_33571().method_1025(anchor)>0.0001){
                // Client-authoritative player movement needs a server position correction.
                DomainTeleportGuard.push(DomainTeleportGuard.Reason.SYSTEM);
                try{e.method_48105(world,anchor.field_1352,anchor.field_1351,anchor.field_1350,Set.of(),e.method_36454(),e.method_36455(),false);}
                finally{DomainTeleportGuard.pop();}
            }
            e.method_18799(class_243.field_1353);e.field_6017=0;e.field_6037=true;
            resize(cage.slime(),e);
        }
        s.putState("d77_objects",List.copyOf(current));
    }
    private static void resize(class_1621 slime,class_1309 target){
        class_238 box=target.method_5829();
        double width=Math.max(box.field_1320-box.field_1323,box.field_1324-box.field_1321);
        double edge=Math.max(width,box.field_1325-box.field_1322)+0.25;
        // Use the actual slime dimensions (including scale attributes), not a guessed constant.
        double unit=Math.min(slime.method_17681(),slime.method_17682())/Math.max(1,slime.method_7152());
        int size=Math.max(1,(int)Math.ceil(edge/Math.max(0.001,unit)));
        if(size!=slime.method_7152())slime.method_7161(size,true);
        // Vanilla caps integer size. Scale the largest shell further for oversized modded mobs.
        if(Math.min(slime.method_17681(),slime.method_17682())<edge){
            var scale=slime.method_5996(D47D69Support.attribute("minecraft:scale"));
            if(scale!=null)scale.method_6192(scale.method_6194()*edge/Math.min(slime.method_17681(),slime.method_17682()));
        }
        double height=slime.method_17682();
        slime.method_5814((box.field_1323+box.field_1320)*0.5,(box.field_1322+box.field_1325-height)*0.5,(box.field_1321+box.field_1324)*0.5);
        slime.method_18799(class_243.field_1353);
    }
    private static void release(class_1309 e,UUID session){
        var cages=LOCKS.get(e);if(cages==null)return;var cage=cages.remove(session);
        if(cage!=null)cage.slime().method_31472();if(cages.isEmpty())LOCKS.remove(e);
    }
    public void onEnd(DomainSession s){for(var e:new ArrayList<>(LOCKS.keySet()))release(e,s.sessionId);}
}
