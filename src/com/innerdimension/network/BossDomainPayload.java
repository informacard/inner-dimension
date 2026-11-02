package com.innerdimension.network;
import net.minecraft.*;
import java.util.*;
/** A complete bounded snapshot. Empty list clears obsolete casts and exposure immediately. */
public record BossDomainPayload(long tick,List<View> views) implements class_8710 {
    public record View(UUID boss,long cast,int kind,boolean warning,int actual,double virtual,double player,int exposure,int disabled,boolean inside){
        public View(UUID boss,long cast,int kind,boolean warning,int actual,double virtual,double player,int exposure,int disabled){this(boss,cast,kind,warning,actual,virtual,player,exposure,disabled,false);}
    }
    public static final class_8710.class_9154<BossDomainPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","boss_domains"));
    public static final class_9139<class_9129,BossDomainPayload> CODEC=class_9139.method_56437((b,p)->{
        b.writeLong(p.tick);b.writeInt(p.views.size());for(var v:p.views){b.writeLong(v.boss.getMostSignificantBits());b.writeLong(v.boss.getLeastSignificantBits());b.writeLong(v.cast);b.writeInt(v.kind);b.writeBoolean(v.warning);b.writeInt(v.actual);b.writeDouble(v.virtual);b.writeDouble(v.player);b.writeInt(v.exposure);b.writeInt(v.disabled);b.writeBoolean(v.inside);}
    },b->{long tick=b.readLong();int n=b.readInt();if(n<0||n>8)throw new IllegalArgumentException("Invalid Boss snapshot size");List<View> views=new ArrayList<>();for(int i=0;i<n;i++)views.add(new View(new UUID(b.readLong(),b.readLong()),b.readLong(),b.readInt(),b.readBoolean(),b.readInt(),b.readDouble(),b.readDouble(),b.readInt(),b.readInt(),b.readBoolean()));return new BossDomainPayload(tick,List.copyOf(views));});
    public class_8710.class_9154<BossDomainPayload> method_56479(){return ID;}
}
