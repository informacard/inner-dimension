package com.innerdimension.network;
import net.minecraft.*;
public record ExtractionPayload(boolean active,double x,double y,double z) implements class_8710 {
 public static final class_8710.class_9154<ExtractionPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","extraction"));
 public static final class_9139<class_9129,ExtractionPayload> CODEC=class_9139.method_56437((b,p)->{b.writeBoolean(p.active());b.writeDouble(p.x());b.writeDouble(p.y());b.writeDouble(p.z());},b->new ExtractionPayload(b.readBoolean(),b.readDouble(),b.readDouble(),b.readDouble()));
 public class_8710.class_9154<ExtractionPayload> method_56479(){return ID;}
}
