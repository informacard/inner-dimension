package com.innerdimension.projection;
import java.util.UUID;
public final class RoomProjectionManager {
    public static int age=-1,burnout=0;
    public static int domainAge(UUID owner) { return age; }
    public static int burnoutRemaining(UUID owner) { return burnout; }
}
