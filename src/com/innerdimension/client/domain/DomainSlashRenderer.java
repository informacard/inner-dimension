package com.innerdimension.client.domain;

import com.innerdimension.network.DomainSlashVisualPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.*;

/** Depth-tested oriented blade trails with a bright core; no effect on game physics. */
public final class DomainSlashRenderer {
    private record TimedStroke(DomainSlashVisualPayload.Stroke stroke, long born) {}
    private static final List<TimedStroke> ACTIVE = new ArrayList<>();
    private static class_638 lastWorld;
    private DomainSlashRenderer() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(DomainSlashVisualPayload.ID,
                (p, c) -> c.client().execute(() -> accept(p)));
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> c.execute(DomainSlashRenderer::clear));
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.field_1687 != lastWorld) clear();
            lastWorld = c.field_1687;
            if (lastWorld != null) ACTIVE.removeIf(s -> lastWorld.method_75260() - s.born() >= s.stroke().duration());
        });
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(DomainSlashRenderer::render);
    }

    private static void accept(DomainSlashVisualPayload payload) {
        var world = class_310.method_1551().field_1687;
        if (world == null || !world.method_27983().method_29177().toString().equals(payload.dimension())) return;
        if (lastWorld != world) clear();
        lastWorld = world;
        for (var stroke : payload.strokes()) ACTIVE.add(new TimedStroke(stroke, world.method_75260()));
        if (ACTIVE.size() > 2048) ACTIVE.subList(0, ACTIVE.size() - 2048).clear();
    }

    private static void clear() { ACTIVE.clear(); lastWorld = null; }

    private static void render(WorldRenderContext context) {
        var client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1773 == null || ACTIVE.isEmpty() || context.commandQueue() == null) return;
        var camera = client.field_1773.method_19418().method_71156();
        var matrices = context.matrices() != null ? context.matrices() : new class_4587();
        for (var timed : List.copyOf(ACTIVE)) {
            var stroke = timed.stroke();
            double age = client.field_1687.method_75260() - timed.born();
            float alpha = (float) Math.max(0, 1 - age / stroke.duration());
            if (alpha <= 0) continue;
            class_243 from = stroke.from().method_1020(camera), to = stroke.to().method_1020(camera);
            context.commandQueue().method_73483(matrices, DomainOutlineRenderer.lineLayer(), (pose, out) -> {
                edge(out, pose, from, to, stroke.color(), alpha, 6);
                edge(out, pose, from, to, 0xFFF5EE, alpha, 2);
            });
        }
    }

    private static void edge(class_4588 out, class_4587.class_4665 pose, class_243 from, class_243 to,
                             int color, float alpha, float width) {
        class_243 delta = to.method_1020(from);
        double length = delta.method_1033();
        if (length < 1e-6) return;
        class_243 normal = delta.method_1021(1 / length);
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255, a = (int) (alpha * 255);
        out.method_56824(pose, (float) from.field_1352, (float) from.field_1351, (float) from.field_1350)
                .method_1336(r, g, b, a).method_60831(pose, (float) normal.field_1352, (float) normal.field_1351, (float) normal.field_1350).method_75298(width);
        out.method_56824(pose, (float) to.field_1352, (float) to.field_1351, (float) to.field_1350)
                .method_1336(r, g, b, a).method_60831(pose, (float) normal.field_1352, (float) normal.field_1351, (float) normal.field_1350).method_75298(width);
    }
}
