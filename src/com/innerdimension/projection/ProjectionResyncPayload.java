package com.innerdimension.projection;
import net.minecraft.*;
import java.util.UUID;
public record ProjectionResyncPayload(int entityId, UUID instance) implements class_8710 {
    public static final class_8710.class_9154<ProjectionResyncPayload> ID = new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","projection_resync"));
    public static final class_9139<class_9129,ProjectionResyncPayload> CODEC=class_9139.method_56437(
        (buf,p) -> { buf.writeInt(p.entityId); buf.method_10797(p.instance); },
        buf -> new ProjectionResyncPayload(buf.readInt(),buf.method_10790()));
    @Override public class_8710.class_9154<ProjectionResyncPayload> method_56479(){return ID;}
}
