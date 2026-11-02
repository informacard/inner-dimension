package com.innerdimension.network;
import net.minecraft.*;
public record BaptismFinishedPayload() implements class_8710 {
 public static final BaptismFinishedPayload INSTANCE=new BaptismFinishedPayload();
 public static final class_8710.class_9154<BaptismFinishedPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","baptism_finished"));
 public static final class_9139<class_9129,BaptismFinishedPayload> CODEC=class_9139.method_56431(INSTANCE);
 public class_8710.class_9154<BaptismFinishedPayload> method_56479(){return ID;}
}
