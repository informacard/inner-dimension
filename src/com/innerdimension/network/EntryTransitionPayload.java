package com.innerdimension.network;

import net.minecraft.*;

/** Sent before NoN's natural stop/reset packets, only to the entering actor. */
public record EntryTransitionPayload(boolean closed, boolean complete, int impact) implements class_8710 {
    public EntryTransitionPayload(boolean closed, boolean complete) { this(closed, complete, 0); }
    public static final class_8710.class_9154<EntryTransitionPayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "entry_transition"));
    public static final class_9139<class_9129, EntryTransitionPayload> CODEC =
            class_9139.method_56437((buf, payload) -> { buf.writeBoolean(payload.closed()); buf.writeBoolean(payload.complete()); buf.writeByte(payload.impact()); },
                    buf -> new EntryTransitionPayload(buf.readBoolean(), buf.readBoolean(), buf.readUnsignedByte()));
    public static void send(net.minecraft.class_3222 player, boolean closed, boolean complete) {
        if (net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player, ID))
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new EntryTransitionPayload(closed, complete));
    }
    @Override public class_8710.class_9154<EntryTransitionPayload> method_56479() { return ID; }
}
