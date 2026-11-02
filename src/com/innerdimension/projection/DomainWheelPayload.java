package com.innerdimension.projection;
import net.minecraft.*;
/** action: 0=request wheel, 1=cast slot, 2=adjust binding. */
public record DomainWheelPayload(int action,boolean open,int value) implements class_8710 {
    public static final class_8710.class_9154<DomainWheelPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","domain_wheel"));
    public static final class_9139<class_9129,DomainWheelPayload> CODEC=class_9139.method_56437((b,p)->{b.writeInt(p.action);b.writeBoolean(p.open);b.writeInt(p.value);},b->new DomainWheelPayload(b.readInt(),b.readBoolean(),b.readInt()));
    public class_8710.class_9154<DomainWheelPayload> method_56479(){return ID;}
}
