package com.innerdimension.network;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.*;

/** Batched short-lived slash strokes. This payload never decides hits or damage. */
public record DomainSlashVisualPayload(String dimension, List<Stroke> strokes) implements class_8710 {
    public record Stroke(class_243 from, class_243 to, int color, int duration) {}
    public static final int MAX_STROKES = 128;
    public static final class_8710.class_9154<DomainSlashVisualPayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "slash_visual"));
    public static final class_9139<class_9129, DomainSlashVisualPayload> CODEC = class_9139.method_56437(
            (b, p) -> {
                b.method_10814(p.dimension());
                b.writeInt(p.strokes().size());
                for (Stroke s : p.strokes()) {
                    b.writeDouble(s.from().field_1352); b.writeDouble(s.from().field_1351); b.writeDouble(s.from().field_1350);
                    b.writeDouble(s.to().field_1352); b.writeDouble(s.to().field_1351); b.writeDouble(s.to().field_1350);
                    b.writeInt(s.color()); b.writeInt(s.duration());
                }
            }, b -> {
                String dimension = b.method_19772();
                int count = b.readInt();
                if (count < 0 || count > MAX_STROKES) throw new IllegalArgumentException("Invalid slash count");
                List<Stroke> strokes = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    class_243 from = new class_243(b.readDouble(), b.readDouble(), b.readDouble());
                    class_243 to = new class_243(b.readDouble(), b.readDouble(), b.readDouble());
                    int color = b.readInt(), duration = b.readInt();
                    if (finite(from) && finite(to) && duration > 0 && duration <= 10)
                        strokes.add(new Stroke(from, to, color, duration));
                }
                return new DomainSlashVisualPayload(dimension, List.copyOf(strokes));
            });

    private static boolean finite(class_243 v) {
        return Double.isFinite(v.field_1352) && Double.isFinite(v.field_1351) && Double.isFinite(v.field_1350);
    }

    @Override public class_8710.class_9154<DomainSlashVisualPayload> method_56479() { return ID; }

    public static void send(class_3218 world, List<Stroke> strokes) {
        if (strokes.isEmpty()) return;
        for (class_3222 player : PlayerLookup.world(world)) {
            if (!ServerPlayNetworking.canSend(player, ID)) continue;
            List<Stroke> nearby = strokes.stream()
                    .filter(s -> player.method_73189().method_1025(s.from()) <= 128 * 128)
                    .limit(MAX_STROKES).toList();
            if (!nearby.isEmpty()) ServerPlayNetworking.send(player,
                    new DomainSlashVisualPayload(world.method_27983().method_29177().toString(), nearby));
        }
    }
}
