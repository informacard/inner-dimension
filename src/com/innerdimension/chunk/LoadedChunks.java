package com.innerdimension.chunk;

import net.minecraft.class_2338;
import net.minecraft.class_3218;

/** Non-loading full chunk lookup, independent of players and random-tick policy. */
public final class LoadedChunks {
    private LoadedChunks() {}

    public static boolean contains(class_3218 world, class_2338 pos) {
        return world != null && pos != null
                && world.method_14178().method_21730(pos.method_10263() >> 4, pos.method_10260() >> 4) != null;
    }
}
