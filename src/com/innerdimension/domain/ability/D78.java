package com.innerdimension.domain.ability;

import com.innerdimension.domain.DomainModes;
import com.innerdimension.domain.runtime.*;
import com.nonid.GenderHolder;
import net.minecraft.*;
import java.util.*;

/** Shared gender lease; persistent original-state tags also repair interrupted sessions. */
public final class D78 implements DomainAbility {
    public static final D78 INSTANCE=new D78();
    private static final String ORIGINAL="inner_dimension:slime_gender:";
    private static final Map<class_1309,Set<UUID>> LEASES=new WeakHashMap<>();
    public static void releaseAll(class_1309 e){LEASES.remove(e);restoreOrphan(e);}
    public String id(){return DomainModes.SLIME_SMALL;}
    public String code(){return "D78";}
    public void onTick(DomainSession s,long now){
        List<class_1309> current=DomainObjects.inside(s,code()).stream()
                .filter(e->e instanceof GenderHolder&&!D77.shell(e)).toList();
        List<class_1309> previous=s.state("d78_objects",List.of());
        for(var e:previous)if(!current.contains(e))release(e,s.sessionId);
        for(var e:current){
            acquire(e,s.sessionId);
        }
        s.putState("d78_objects",List.copyOf(current));
    }
    private static void acquire(class_1309 e,UUID session){
        if(!LEASES.containsKey(e)){
            restoreOrphan(e);
            int tags=(e.method_5752().contains("gender.male")?1:0)|(e.method_5752().contains("gender.female")?2:0);
            e.method_5780(ORIGINAL+((GenderHolder)e).getGenderMask()+":"+tags);
        }
        LEASES.computeIfAbsent(e,k->new HashSet<>()).add(session);
        set(e,GenderHolder.GENDER_BOTH,3);
    }
    private static void set(class_1309 e,int mask,int tags){
        if(!(e instanceof GenderHolder holder))return;
        holder.setGenderMask(mask);
        if((tags&1)!=0)e.method_5780("gender.male");else e.method_5738("gender.male");
        if((tags&2)!=0)e.method_5780("gender.female");else e.method_5738("gender.female");
    }
    private static void release(class_1309 e,UUID session){
        var sources=LEASES.get(e);if(sources==null)return;
        sources.remove(session);if(!sources.isEmpty())return;
        LEASES.remove(e);restoreOrphan(e);
    }
    public void onEnd(DomainSession s){for(var e:new ArrayList<>(LEASES.keySet()))release(e,s.sessionId);}
    public static void restoreOrphan(class_1309 e){
        if(e.method_73183().method_8608()||LEASES.containsKey(e))return;
        for(String tag:new ArrayList<>(e.method_5752()))if(tag.startsWith(ORIGINAL)){
            String[] values=tag.substring(ORIGINAL.length()).split(":");
            if(values.length==2)try{set(e,Integer.parseInt(values[0]),Integer.parseInt(values[1]));}
            catch(NumberFormatException ignored){}
            e.method_5738(tag);
        }
    }
}
