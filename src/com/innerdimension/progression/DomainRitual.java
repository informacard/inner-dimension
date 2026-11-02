package com.innerdimension.progression;

import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.security.DollSecurityManager;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.util.*;

/** Samples a swept landing before fall distance is reset by vanilla. */
public final class DomainRitual {
    private record Sample(class_3218 world, double y, double fall) { }
    private record Attempt(class_3222 player, EnderPearlDildoEntity port, boolean domain) { }
    private record Materials(int obsidian, int eye) {
        boolean both() { return obsidian >= 0 && eye >= 0; }
        boolean any() { return obsidian >= 0 || eye >= 0; }
    }
    private static final Map<UUID, Sample> BEFORE = new HashMap<>();
    private static final Map<UUID, Attempt> PENDING = new HashMap<>();
    private static final Map<UUID, Long> LAST_HINT = new HashMap<>();
    public static void clear() { BEFORE.clear(); PENDING.clear(); LAST_HINT.clear(); PistonProgress.clear(); }
    private static void hint(class_3222 p, String key) {
        long now = com.innerdimension.core.ServerClock.now();
        if (now - LAST_HINT.getOrDefault(p.method_5667(), -100L) < 20) return;
        LAST_HINT.put(p.method_5667(), now);
        p.method_7353(class_2561.method_43471(key), true);
    }
    public static void movement(class_3222 p) {
        BEFORE.put(p.method_5667(), new Sample(p.method_51469(), p.method_23318(), p.field_6017));
    }
    public static void before(MinecraftServer server) {
        BEFORE.clear();
        for (var p : server.method_3760().method_14571())
            BEFORE.put(p.method_5667(), new Sample(p.method_51469(), p.method_23318(), p.field_6017));
    }
    public static boolean landing(class_3222 p, double distance) {
        return landing(p, distance, "END_TICK");
    }
    public static void packetComplete(class_3222 p) {
        var sample=BEFORE.get(p.method_5667());
        if(sample!=null && sample.world()==p.method_51469() && sample.y()>p.method_23318())
            landing(p,Math.max(p.field_6017,sample.fall()+sample.y()-p.method_23318()),"MOVEMENT_PACKET");
    }
    public static boolean supported(EnderPearlDildoEntity port) {
        var pos=port.method_24515().method_10074();
        var shape=port.method_73183().method_8320(pos).method_26220(port.method_73183(),pos);
        return !shape.method_1110() && Math.abs(pos.method_10264()+shape.method_1107().field_1325-port.method_23318())<=.15;
    }
    public static boolean landing(class_3222 p, double distance, String source) {
        if (PENDING.containsKey(p.method_5667())) {
            return true;
        }
        if (!p.method_5805() || p.method_7325() || DynamicDimensionManager.isInnerWorld(p.method_51469().method_27983())) {
            return false;
        }
        var before = BEFORE.get(p.method_5667());
        double y = p.method_23318();
        double previous = before != null && before.world() == p.method_51469() ? before.y() : y;
        var box = p.method_5829();
        var search = new class_238(box.field_1323, y - 1.1, box.field_1321, box.field_1320, Math.max(previous, y) + .1, box.field_1324);
        for (var port : p.method_51469().method_8390(EnderPearlDildoEntity.class, search, DomainRitual::supported)) {
            double top = port.method_23318() + .9;
            // This decorative entity does not stop a player at its visual top. Test the actual
            // supported landing, allowing feet at its base, after the movement packet is accepted.
            if (y > top + .15 || y < port.method_23318() - .15
                    || (!p.method_24828() && !source.equals("FALL_DAMAGE"))
                    || (previous <= y && !source.equals("FALL_DAMAGE"))) continue;
            var gate = DollSecurityManager.forInteraction(port, p.method_5667());
            if (gate.blocked()) {
                DollSecurityManager.sendFeedback(p, gate);
                return false;
            }
            double landingDistance = RitualConditions.landingDistance(distance, y, port.method_23318());
            boolean high = RitualConditions.touches(landingDistance);
            // Grant touch before choosing the destination, so the first high fall can unlock
            // both touch and the core. Touch itself requires neither materials nor gaze.
            if (high) InnerAdvancements.grant(p, "touch");
            Materials materials = materials(port);
            boolean gaze = InnerAdvancements.done(p, "gaze_abyss");
            var destination = RitualConditions.destination(landingDistance, gaze, materials.obsidian() >= 0, materials.eye() >= 0);
            if (destination == RitualConditions.Destination.NONE) {
                if (landingDistance >= 3) hint(p, !gaze ? "inner_dimension.ritual.need_gaze"
                        : landingDistance < 14.0 - 1.0e-4 ? "inner_dimension.ritual.need_height"
                        : "inner_dimension.ritual.need_materials");
                return false;
            }
            boolean domain = destination == RitualConditions.Destination.DOMAIN;
            PENDING.put(p.method_5667(), new Attempt(p, port, domain));
            return true;
        }
        return false;
    }
    private static class_1792 item(String id) { return class_7923.field_41178.method_63535(class_2960.method_12829(id)); }
    private static int slot(EnderPearlDildoEntity port, class_1792 item) {
        var inv = port.hopperInventory();
        for (int i = 0; i < inv.method_5439(); i++) if (inv.method_5438(i).method_7909() == item && !inv.method_5438(i).method_7960()) return i;
        return -1;
    }
    private static Materials materials(EnderPearlDildoEntity port) {
        return new Materials(slot(port, item("minecraft:obsidian")), slot(port, item("minecraft:ender_eye")));
    }
    public static void after(MinecraftServer server) {
        for (var p : server.method_3760().method_14571()) {
            var s = BEFORE.get(p.method_5667());
            if (s != null && s.world() == p.method_51469() && s.y() > p.method_23318())
                landing(p, Math.max(p.field_6017, s.fall() + s.y() - p.method_23318()));
        }
        for (var a : List.copyOf(PENDING.values())) {
            var p = a.player(); var port = a.port();
            if (!p.method_5805() || port.method_31481() || p.method_51469() != port.method_73183()) {
                continue;
            }
            Materials materials = materials(port);
            if (a.domain() ? !materials.both() : !materials.any()) continue;
            if (DollSecurityManager.forInteraction(port, p.method_5667()).blocked()
                    || !InnerAdvancements.done(p, "gaze_abyss")) continue;
            // This API preserves the personal return point and validates the entry safety gate.
            if (DynamicDimensionManager.enterRitual(p, a.domain() ? 32.0 : 24.0)) {
                if(!a.domain())com.innerdimension.sound.ModSounds.personal(p,"abyss_passage",.8f);
                if (materials.obsidian() >= 0) port.hopperInventory().method_5438(materials.obsidian()).method_7934(1);
                if (materials.eye() >= 0) port.hopperInventory().method_5438(materials.eye()).method_7934(1);
                port.hopperInventory().method_5431();
                p.field_6017 = 0;
                p.method_18799(class_243.field_1353);
                if(a.domain()) com.innerdimension.progression.DomainFeatures.introduce(p);
                else {
                    InnerAdvancements.grant(p,"lost_passage");
                    p.method_7353(class_2561.method_43471(materials.both()
                            ? "inner_dimension.ritual.passage_height" : "inner_dimension.ritual.passage_materials"), true);
                }
            }
        }
        PENDING.clear();
        LAST_HINT.keySet().removeIf(id -> server.method_3760().method_14602(id) == null);
        PistonProgress.expire();
    }
}
