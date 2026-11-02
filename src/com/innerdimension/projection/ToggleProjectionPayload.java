package com.innerdimension.projection;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

/** The sender's identity and position always come from the server connection. */
public record ToggleProjectionPayload() implements class_8710 {
    public static final ToggleProjectionPayload INSTANCE = new ToggleProjectionPayload();
    public static final class_8710.class_9154<ToggleProjectionPayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "toggle_projection"));
    public static final class_9139<class_9129, ToggleProjectionPayload> CODEC =
            class_9139.method_56431(INSTANCE);
    @Override public class_8710.class_9154<ToggleProjectionPayload> method_56479() { return ID; }
}
