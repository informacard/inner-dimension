package com.innerdimension.network;
import net.minecraft.*;
public record RitualControlPayload(boolean locked) implements class_8710 {
    public static final class_8710.class_9154<RitualControlPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","ritual_control"));
    public static final class_9139<class_9129,RitualControlPayload> CODEC=class_9139.method_56437((b,p)->b.writeBoolean(p.locked),b->new RitualControlPayload(b.readBoolean()));
    public class_8710.class_9154<RitualControlPayload> method_56479(){return ID;}
}
