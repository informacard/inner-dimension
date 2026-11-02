package com.innerdimension.projection;

import net.minecraft.*;
import java.util.*;

/** Per-actor retries and one-landing fall protection never abort another person's expansion. */
public final class ProjectionEvacuation {
    private record Pending(class_1297 actor,RoomProjectionEntity shell,RoomProjectionEntity interior) { }
    private record Protected(class_1297 actor,class_1937 world,long start) { }
    private static final Map<UUID,Pending> PENDING=new HashMap<>();
    private static final Map<UUID,Protected> FALL=new HashMap<>();
    private static long ticks;
    private ProjectionEvacuation() { }
    public static void defer(class_1297 actor,RoomProjectionEntity shell,RoomProjectionEntity interior) {
        if (ProjectionEntityPolicy.ignores(actor)) return;
        PENDING.put(actor.method_5667(),new Pending(actor,shell,interior));
    }
    public static boolean pending(class_1297 actor,RoomProjectionEntity projection) {
        Pending p=PENDING.get(actor.method_5667());
        return p!=null && (p.shell==projection || p.interior==projection);
    }
    public static void protect(class_1297 actor) {
        if (actor instanceof class_1309) FALL.put(actor.method_5667(),new Protected(actor,actor.method_73183(),ticks));
    }
    public static boolean consumeFall(class_1297 actor) {
        Protected p=FALL.get(actor.method_5667());
        if (p==null || p.actor!=actor || p.world!=actor.method_73183()) return false;
        FALL.remove(actor.method_5667());
        actor.field_6017=0;
        return true;
    }
    public static void tick() {
        ticks++;
        FALL.entrySet().removeIf(e -> {
            Protected p=e.getValue();
            return p.actor.method_31747() || !p.actor.method_5805() || p.actor.method_73183()!=p.world
                    || ticks-p.start>2 && p.actor.method_24828();
        });
        PENDING.entrySet().removeIf(e -> {
            Pending p=e.getValue();
            if (p.actor.method_31747() || !p.actor.method_5805() || p.shell.method_31747()
                    || p.actor.method_73183()!=p.shell.method_73183()
                    || p.shell.phase()==ProjectionAnimation.RETRACTING) return true;
            return ticks%10==0 && ProjectionSafety.retryEvacuation(p.shell,p.interior,p.actor);
        });
    }
    public static void clear() { PENDING.clear(); FALL.clear(); ticks=0; }
}
