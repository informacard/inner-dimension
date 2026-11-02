package com.innerdimension.projection;

import net.minecraft.class_2960;
import net.minecraft.class_8710;
import net.minecraft.class_9129;
import net.minecraft.class_9139;

/** Separate input so the normal enclosed expansion remains available on R. */
public record ToggleOpenProjectionPayload() implements class_8710 {
    public static final ToggleOpenProjectionPayload INSTANCE = new ToggleOpenProjectionPayload();
    public static final class_8710.class_9154<ToggleOpenProjectionPayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "toggle_open_projection"));
    public static final class_9139<class_9129, ToggleOpenProjectionPayload> CODEC =
            class_9139.method_56437((buffer, payload) -> { }, buffer -> INSTANCE);
    @Override public class_8710.class_9154<ToggleOpenProjectionPayload> method_56479() { return ID; }
}
