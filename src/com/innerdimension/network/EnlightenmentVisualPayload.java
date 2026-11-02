package com.innerdimension.network;
import net.minecraft.*;
public record EnlightenmentVisualPayload(int variant,boolean preview) implements class_8710 {
    public static final class_8710.class_9154<EnlightenmentVisualPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","enlightenment_visual"));
    public static final class_9139<class_9129,EnlightenmentVisualPayload> CODEC=class_9139.method_56437((buf,p)->{buf.writeByte(p.variant);buf.writeBoolean(p.preview);},buf->new EnlightenmentVisualPayload(buf.readUnsignedByte(),buf.readBoolean()));
    public class_8710.class_9154<EnlightenmentVisualPayload> method_56479(){return ID;}
}
