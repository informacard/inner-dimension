package com.innerdimension.projection;
import net.minecraft.*;
import java.util.*;
public record DomainWheelStatePayload(boolean open,List<String> names) implements class_8710 {
    public static final class_8710.class_9154<DomainWheelStatePayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","domain_wheel_state"));
    public static final class_9139<class_9129,DomainWheelStatePayload> CODEC=class_9139.method_56437((b,p)->{b.writeBoolean(p.open);b.writeInt(p.names.size());for(String n:p.names)b.method_10814(n);},b->{boolean open=b.readBoolean();int n=b.readInt();if(n<0||n>7)throw new IllegalArgumentException("Invalid slots");List<String> names=new ArrayList<>();for(int i=0;i<n;i++)names.add(b.method_19772());return new DomainWheelStatePayload(open,List.copyOf(names));});
    public class_8710.class_9154<DomainWheelStatePayload> method_56479(){return ID;}
}
