package com.innerdimension.network;
import net.minecraft.*;
import java.util.UUID;
public record DomainVisualStatePayload(UUID playerUuid,boolean active,int visualRevision,int domainAge,int burnoutRemaining,int objectFlags) implements class_8710 {
    public DomainVisualStatePayload(UUID playerUuid,boolean active,int visualRevision,int domainAge,int burnoutRemaining){this(playerUuid,active,visualRevision,domainAge,burnoutRemaining,0);}
    public static final class_8710.class_9154<DomainVisualStatePayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","domain_visual_state"));
    public static final class_9139<class_9129,DomainVisualStatePayload> CODEC=class_9139.method_56437(
        (buf,p) -> { buf.method_10797(p.playerUuid()); buf.writeBoolean(p.active()); buf.writeInt(p.visualRevision()); buf.writeInt(p.domainAge()); buf.writeInt(p.burnoutRemaining()); buf.writeInt(p.objectFlags()); },
        buf -> new DomainVisualStatePayload(buf.method_10790(),buf.readBoolean(),buf.readInt(),buf.readInt(),buf.readInt(),buf.readInt()));
    @Override public class_8710.class_9154<DomainVisualStatePayload> method_56479(){ return ID; }
}
