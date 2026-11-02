package com.innerdimension.projection;

import net.minecraft.*;
import java.util.*;

/** Fragmented transport; all fragments are applied together on the client thread. */
public record ProjectionUpdatePayload(int entityId, UUID instance, long base, long next, boolean full,
                                     int part, int parts, List<Entry> entries) implements class_8710 {
    public record Entry(class_2338 pos, int stateId, class_2487 data) { }
    public static final int FRAGMENT_SIZE = ProjectionBatch.FRAGMENT_SIZE, MAX_PARTS = ProjectionBatch.MAX_PARTS;
    public static final class_8710.class_9154<ProjectionUpdatePayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "projection_update"));
    public static final class_9139<class_9129, ProjectionUpdatePayload> CODEC = class_9139.method_56437((buf,p) -> {
        buf.writeInt(p.entityId); buf.method_10797(p.instance); buf.writeLong(p.base); buf.writeLong(p.next);
        buf.writeBoolean(p.full); buf.writeInt(p.part); buf.writeInt(p.parts); buf.writeInt(p.entries.size());
        for (Entry e : p.entries) { buf.method_10807(e.pos); buf.writeInt(e.stateId); buf.method_10794(e.data); }
    }, buf -> {
        int id = buf.readInt(); UUID uuid = buf.method_10790(); long base = buf.readLong(), next = buf.readLong();
        boolean full = buf.readBoolean(); int part = buf.readInt(), parts = buf.readInt(), size = buf.readInt();
        if (parts < 1 || parts > MAX_PARTS || part < 0 || part >= parts || size < 0 || size > FRAGMENT_SIZE)
            throw new IllegalArgumentException("Invalid projection batch size");
        List<Entry> entries = new ArrayList<>(size);
        for (int i=0;i<size;i++) entries.add(new Entry(buf.method_10811(),buf.readInt(),buf.method_10798()));
        return new ProjectionUpdatePayload(id,uuid,base,next,full,part,parts,List.copyOf(entries));
    });
    @Override public class_8710.class_9154<ProjectionUpdatePayload> method_56479() { return ID; }
}
