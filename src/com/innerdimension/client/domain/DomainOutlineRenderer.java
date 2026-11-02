package com.innerdimension.client.domain;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * D19 treasure sense + D22 echo location: through-wall box outlines owned entirely by this
 * client, drawn between the entity pass and the translucent terrain pass.
 *
 * <p>Rules taken from the blueprint:</p>
 * <ul>
 *   <li>the entity pass is local and team-independent - it never reads a team colour, never
 *       touches the global glowing flag, and includes invisible entities, because the whole
 *       point of echo location is to reveal what is not visible;</li>
 *   <li>containers are drawn as a box only. Inventory NBT never reaches the client, so it can
 *       never be rendered.</li>
 * </ul>
 *
 * <p>Both sets live in {@link DomainClientState} and are replaced by every incoming snapshot,
 * so an ability that ends (or an owner that leaves the world) stops drawing on the next
 * payload / the next tick.</p>
 */
public final class DomainOutlineRenderer {
    private static final float CONTAINER_GROW = 0.002f;
    private static final double ENTITY_GROW = 0.1;

    private static final int ENTITY_COLOR = 0x49E8FF;
    private static final int CONTAINER_COLOR = 0xFFC83C;

    /** Lazily built custom "lines" layer; see {@link #lineLayer()}. */
    private static volatile class_1921 lines;
    private static volatile boolean lineLayerFailed;

    private DomainOutlineRenderer() {}

    public static void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(DomainOutlineRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        class_310 client = class_310.method_1551();
        class_638 world = client.field_1687;
        if (world == null || client.field_1724 == null || client.field_1773 == null) return;
        List<Integer> entityIds = DomainClientState.outlinedEntityIds();
        List<class_2338> containers = DomainClientState.outlinedContainers();
        if (entityIds.isEmpty() && containers.isEmpty()) return;
        class_11659 queue = context.commandQueue();
        class_1921 layer = lineLayer();
        if (queue == null || layer == null) return;
        // Vanilla hands the world pass a fresh, identity pose stack (WorldRenderer builds it per
        // render), so outlines are emitted in camera-relative coordinates.
        class_4587 matrices = context.matrices() != null ? context.matrices() : new class_4587();
        class_243 camera = client.field_1773.method_19418().method_71156();
        double cx = camera.field_1352;
        double cy = camera.field_1351;
        double cz = camera.field_1350;

        for (int id : entityIds) {
            class_1297 entity = world.method_8469(id);
            if (entity == null || entity.method_7325()) continue;
            class_238 box = relative(entity.method_5829().method_1014(ENTITY_GROW), cx, cy, cz);
            queue.method_73483(matrices, layer, (pose, vertices) ->
                    drawBox(vertices, box, ENTITY_COLOR, pose));
        }
        for (class_2338 pos : containers) {
            class_238 box = relative(new class_238(pos).method_1014(CONTAINER_GROW), cx, cy, cz);
            queue.method_73483(matrices, layer, (pose, vertices) ->
                    drawBox(vertices, box, CONTAINER_COLOR, pose));
        }
    }

    private static class_238 relative(class_238 box, double cx, double cy, double cz) {
        return new class_238(box.field_1323 - cx, box.field_1322 - cy, box.field_1321 - cz,
                box.field_1320 - cx, box.field_1325 - cy, box.field_1324 - cz);
    }

    private static void drawBox(class_4588 out, class_238 box, int rgb) {
        drawBox(out, box, rgb, new class_4587().method_23760());
    }

    private static void drawBox(class_4588 out, class_238 box, int rgb, class_4587.class_4665 pose) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float a = 1.0f;
        float x0 = (float) box.field_1323;
        float y0 = (float) box.field_1322;
        float z0 = (float) box.field_1321;
        float x1 = (float) box.field_1320;
        float y1 = (float) box.field_1325;
        float z1 = (float) box.field_1324;
        // Bottom face.
        edge(out, pose, x0, y0, z0, x1, y0, z0, r, g, b, a);
        edge(out, pose, x1, y0, z0, x1, y0, z1, r, g, b, a);
        edge(out, pose, x1, y0, z1, x0, y0, z1, r, g, b, a);
        edge(out, pose, x0, y0, z1, x0, y0, z0, r, g, b, a);
        // Top face.
        edge(out, pose, x0, y1, z0, x1, y1, z0, r, g, b, a);
        edge(out, pose, x1, y1, z0, x1, y1, z1, r, g, b, a);
        edge(out, pose, x1, y1, z1, x0, y1, z1, r, g, b, a);
        edge(out, pose, x0, y1, z1, x0, y1, z0, r, g, b, a);
        // Verticals.
        edge(out, pose, x0, y0, z0, x0, y1, z0, r, g, b, a);
        edge(out, pose, x1, y0, z0, x1, y1, z0, r, g, b, a);
        edge(out, pose, x1, y0, z1, x1, y1, z1, r, g, b, a);
        edge(out, pose, x0, y0, z1, x0, y1, z1, r, g, b, a);
    }

    private static void edge(class_4588 out, class_4587.class_4665 pose, float x1, float y1, float z1, float x2, float y2, float z2,
                             int r, int g, int b, float a) {
        int alpha = (int) (a * 255.0f);
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 1.0E-6f) return;
        dx /= length;
        dy /= length;
        dz /= length;
        // The 1.21.11 line shader requires both the segment direction and per-vertex width.
        out.method_56824(pose, x1, y1, z1).method_1336(r, g, b, alpha)
                .method_60831(pose, dx, dy, dz).method_75298(2.0f);
        out.method_56824(pose, x2, y2, z2).method_1336(r, g, b, alpha)
                .method_60831(pose, dx, dy, dz).method_75298(2.0f);
    }

    /**
     * The through-wall "lines" layer.
     *
     * <p>1.21.11 only exposes depth-tested line layers ({@code class_12249.field_64042} "lines"),
     * and the only factory that turns a custom no-depth pipeline into a {@code RenderLayer}
     * ({@code class_1921.method_75940}) is package-private in the intermediary jar. Fabric's
     * transitive access widener declares it {@code transitive-accessible}, so it is invokable at
     * runtime; it is reached reflectively here and the public, depth-tested layer is used as a
     * fallback so the outlines still appear (occluded by walls) instead of disappearing.</p>
     */
    static class_1921 lineLayer() {
        class_1921 current = lines;
        if (current != null || lineLayerFailed) return current != null ? current : class_12249.field_64042;
        synchronized (DomainOutlineRenderer.class) {
            if (lines == null && !lineLayerFailed) {
                try {
                    lines = buildNoDepthLines();
                } catch (Throwable failure) {
                    lineLayerFailed = true;
                    org.slf4j.LoggerFactory.getLogger("inner_dimension.domain.outline")
                            .warn("Domain outline layer unavailable, falling back to depth-tested lines", failure);
                }
            }
        }
        return lines != null ? lines : class_12249.field_64042;
    }

    private static class_1921 buildNoDepthLines() throws ReflectiveOperationException {
        RenderPipeline pipeline = buildPipeline();
        class_12247 setup = class_12247.method_75927(pipeline).method_75930(class_12245.field_63976)
                .method_75931(class_12246.field_63980).method_75938();
        Method factory = class_1921.class.getDeclaredMethod("method_75940", String.class, class_12247.class);
        factory.setAccessible(true);
        return (class_1921) factory.invoke(null, "inner_dimension:domain_outline", setup);
    }

    private static RenderPipeline buildPipeline() throws ReflectiveOperationException {
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(class_2960.method_60655("inner_dimension", "pipeline/domain_outline"))
                .withVertexShader("core/rendertype_lines")
                .withFragmentShader("core/rendertype_lines")
                .withUniform("DynamicTransforms", class_10789.field_60031)
                .withUniform("Projection", class_10789.field_60031)
                .withUniform("Fog", class_10789.field_60031)
                .withUniform("Globals", class_10789.field_60031)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withVertexFormat(class_290.field_63455, VertexFormat.class_5596.field_27377)
                .build();
        // Iris replaces world shaders by pipeline identity, not by shader names.
        try {
            Class<?> iris = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines");
            iris.getMethod("copyPipeline", RenderPipeline.class, RenderPipeline.class)
                    .invoke(null, class_10799.field_56833, pipeline);
        } catch (ClassNotFoundException absent) {
            // Iris is optional; vanilla compiles the line shader directly.
        }
        return pipeline;
    }
}
