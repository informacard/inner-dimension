package com.innerdimension.progression;
import com.innerdimension.core.ServerClock;
import net.minecraft.class_1297;
import java.util.*;
public final class PistonProgress {
    private static final Map<UUID, Long> MOVED = new HashMap<>();
    public static void record(class_1297 entity) { MOVED.put(entity.method_5667(), ServerClock.now()); }
    public static boolean recent(class_1297 entity) { return ServerClock.now() - MOVED.getOrDefault(entity.method_5667(), -100L) <= 8; }
    public static void clear() { MOVED.clear(); }
    public static void expire() { MOVED.values().removeIf(t -> ServerClock.now() - t > 8); }
}
