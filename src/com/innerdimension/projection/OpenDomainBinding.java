package com.innerdimension.projection;

import com.mojang.serialization.Codec;
import com.innerdimension.state.InnerState;
import net.minecraft.*;
import java.util.*;

/** Saved binding steps; each expansion captures its own immutable limits. */
public final class OpenDomainBinding extends class_18 {
    private final Map<UUID,Integer> steps=new HashMap<>();
    public OpenDomainBinding(){}
    private OpenDomainBinding(Map<UUID,Integer> values){values.forEach((id,n)->steps.put(id,n));}
    public static final Codec<OpenDomainBinding> CODEC=Codec.unboundedMap(InnerState.UUID_CODEC,Codec.INT).xmap(OpenDomainBinding::new,s->s.steps);
    public static final class_10741<OpenDomainBinding> TYPE=new class_10741<>("inner_dimension_open_bindings",OpenDomainBinding::new,CODEC,class_4284.field_19212);
    public static OpenDomainBinding get(class_3218 world){return world.method_8503().method_30002().method_17983().method_17924(TYPE);}
    public static int radius(int steps){return (int)Math.max(1,Math.min(Integer.MAX_VALUE,rawRadius(steps)));}
    public static int duration(int steps){return (int)Math.max(20,Math.min(Integer.MAX_VALUE,rawDuration(steps)));}
    private static long rawRadius(int steps){return (long)com.innerdimension.config.InnerConfig.get().openDomainInitialRadius+4L*steps;}
    private static long rawDuration(int steps){return ((long)com.innerdimension.config.InnerConfig.get().openDomainInitialDurationSeconds-12L*steps)*20L;}
    public int radius(UUID owner){return radius(steps.getOrDefault(owner,0));}
    public int duration(UUID owner){return duration(steps.getOrDefault(owner,0));}
    public boolean adjust(UUID owner,int direction){long candidate=(long)steps.getOrDefault(owner,0)+Integer.signum(direction);if(candidate<Integer.MIN_VALUE||candidate>Integer.MAX_VALUE)return false;int next=(int)candidate;if(rawRadius(next)<=0||rawRadius(next)>Integer.MAX_VALUE||rawDuration(next)<=0||rawDuration(next)>Integer.MAX_VALUE)return false;steps.put(owner,next);method_80();return true;}
    public static int burnout(long elapsed,int maximum,boolean forced){return (int)Math.max(60,(forced?maximum:Math.min(maximum,Math.max(0,elapsed)))/8);}
}
