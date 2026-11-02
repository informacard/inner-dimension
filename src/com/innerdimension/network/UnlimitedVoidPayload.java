package com.innerdimension.network;
import net.minecraft.*;
public record UnlimitedVoidPayload(int ticks) implements class_8710 {
    public static final class_8710.class_9154<UnlimitedVoidPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","unlimited_void"));
    public static final class_9139<class_9129,UnlimitedVoidPayload> CODEC=class_9139.method_56437((b,p)->b.writeInt(p.ticks),b->new UnlimitedVoidPayload(b.readInt()));
    public class_8710.class_9154<UnlimitedVoidPayload> method_56479(){return ID;}
}
