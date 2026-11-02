package com.innerdimension.network;
import net.minecraft.*;
/** Explicit expansion announcement and coloured contest names, independent of entity tracking. */
public record DomainHudPayload(boolean contest,String first,int left,int center,int right,int splitLeft,int splitRight,
                               String second,int otherLeft,int otherCenter,int otherRight,int otherSplitLeft,int otherSplitRight,int titleTicks,int nameTicks,int heart) implements class_8710 {
    public DomainHudPayload(boolean contest,String first,int left,int center,int right,int splitLeft,int splitRight,
                            String second,int otherLeft,int otherCenter,int otherRight,int otherSplitLeft,int otherSplitRight) {
        this(contest,first,left,center,right,splitLeft,splitRight,second,otherLeft,otherCenter,otherRight,otherSplitLeft,otherSplitRight,15,5,0);
    }
    public static final class_8710.class_9154<DomainHudPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","domain_hud"));
    public static final class_9139<class_9129,DomainHudPayload> CODEC=class_9139.method_56437((b,p)->{
        b.writeBoolean(p.contest);b.method_10814(p.first);b.writeInt(p.left);b.writeInt(p.center);b.writeInt(p.right);b.writeInt(p.splitLeft);b.writeInt(p.splitRight);
        b.method_10814(p.second);b.writeInt(p.otherLeft);b.writeInt(p.otherCenter);b.writeInt(p.otherRight);b.writeInt(p.otherSplitLeft);b.writeInt(p.otherSplitRight);b.writeInt(p.titleTicks);b.writeInt(p.nameTicks);b.writeInt(p.heart);
    },b->new DomainHudPayload(b.readBoolean(),b.method_19772(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.method_19772(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt()));
    @Override public class_8710.class_9154<DomainHudPayload> method_56479(){return ID;}
}
