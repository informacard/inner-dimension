package com.innerdimension.client;

import com.innerdimension.projection.RoomProjectionContraption;
import com.innerdimension.projection.RoomProjectionEntity;
import com.innerdimension.projection.RoomProjectionManager;
import com.innerdimension.projection.builtin.ClientContraption;
import com.innerdimension.projection.builtin.ContraptionEntityRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.*;

/** Exercise the cold virtual-world and mesh path with a tiny, undisplayed snapshot.
 * All work stays on the client thread; each render layer gets a separate tick.
 * Never captures a real room or changes the world, and releases every temporary buffer.
 */
public final class ProjectionWarmup {
    private static int delay = -1, layer;
    private static ClientContraption preview;
    private ProjectionWarmup() {}
    private static void reset() {
        if (preview != null) preview.invalidateStructure();
        preview = null;
        delay = -1;
        layer = 0;
    }
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            reset();
            delay = 100;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(ProjectionWarmup::reset));
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(
            new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                public class_2960 getFabricId() { return com.innerdimension.InnerDimension.id("projection_warmup"); }
                public void method_14491(class_3300 manager) {
                    reset();
                    if (class_310.method_1551().field_1687 != null) delay = 100;
                }
            });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (delay < 0 || client.field_1687 == null || client.field_1724 == null) return;
            if (delay-- > 0) return;
            delay = 0;
            try {
                if (preview == null) {
                    var snapshot = new RoomProjectionContraption();
                    snapshot.bounds = new class_238(0, 0, 0, 3, 1, 1);
                    class_2680[] states = {com.innerdimension.block.ModBlocks.PEARL_FACE.method_9564(),
                            class_2246.field_10540.method_9564(), class_2246.field_10124.method_9564()};
                    for (int i = 0; i < states.length; i++) {
                        var pos = new class_2338(i, 0, 0);
                        snapshot.getBlocks().put(pos, new class_3499.class_3501(pos, states[i], null));
                    }
                    var entity = new RoomProjectionEntity(RoomProjectionManager.ENTITY_TYPE, client.field_1687);
                    entity.setContraption(snapshot);
                    preview = new ClientContraption(snapshot);
                    return;
                }
                var layers = class_11515.values();
                if (layer < layers.length) {
                    ContraptionEntityRenderer.getBuffer(preview.getContraption(), preview,
                            preview.getRenderLevel(), layers[layer++]);
                } else reset();
            } catch (RuntimeException error) {
                reset();
                org.slf4j.LoggerFactory.getLogger("inner_dimension.projection").warn("Projection warmup skipped", error);
            }
        });
    }
}