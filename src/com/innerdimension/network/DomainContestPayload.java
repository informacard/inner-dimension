package com.innerdimension.network;
import net.minecraft.*;
import java.util.UUID;
public record DomainContestPayload(UUID warningOwner,int reaction,int simple,int clash) implements class_8710 {
    public static final class_8710.class_9154<DomainContestPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","domain_contest"));
    public static final class_9139<class_9129,DomainContestPayload> CODEC=class_9139.method_56437((buf,p)->{
        buf.writeBoolean(p.warningOwner!=null);if(p.warningOwner!=null)buf.method_10797(p.warningOwner);
        buf.writeInt(p.reaction);buf.writeInt(p.simple);buf.writeInt(p.clash);
    },buf->new DomainContestPayload(buf.readBoolean()?buf.method_10790():null,buf.readInt(),buf.readInt(),buf.readInt()));
    @Override public class_8710.class_9154<DomainContestPayload> method_56479(){return ID;}
}
