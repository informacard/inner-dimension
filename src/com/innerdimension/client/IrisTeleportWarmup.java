package com.innerdimension.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

/** One cold shader initialization per active pack, on the render/client thread. */
public final class IrisTeleportWarmup {
    private static int delay = -1;
    private IrisTeleportWarmup() {}
    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) return;
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> delay = 100);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> delay = -1);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.field_1687 == null || client.field_1724 == null || client.field_1755 != null) return;
            if (delay < 0 || delay-- > 0) return;
            delay = -1;
            IrisBridge.warm();
        });
    }

    // Keep optional Iris classes out of the entrypoint's linkage when Iris is absent.
    private static final class IrisBridge {
        private static Object attemptedPack;
        static void warm() {
            var pack = net.irisshaders.iris.Iris.getCurrentPack().orElse(null);
            var manager = net.irisshaders.iris.Iris.getPipelineManager();
            if (pack == null || pack == attemptedPack
                    || !(manager.getPipelineNullable() instanceof net.irisshaders.iris.pipeline.IrisRenderingPipeline)) return;
            attemptedPack = pack;
            var current = net.irisshaders.iris.Iris.getCurrentDimension();
            var target = net.irisshaders.iris.shaderpack.DimensionId.END;
            if (current.equals(target)) return;
            var log = org.slf4j.LoggerFactory.getLogger("inner_dimension.shader_warmup");
            long start = System.nanoTime();
            try {
                // Use Iris's normal destruction path, including texture reset and Sodium revision.
                // At no point are two live dimension pipelines retained.
                manager.destroyPipeline();
                manager.preparePipeline(target);
                log.info("[InnerShaderWarmup] Target {} initialized in {} ms", target,
                        (System.nanoTime() - start) / 1_000_000L);
            } catch (RuntimeException failure) {
                log.warn("[InnerShaderWarmup] Preload failed", failure);
            } finally {
                manager.destroyPipeline();
                manager.preparePipeline(current);
                log.info("[InnerShaderWarmup] Finished and restored {} in {} ms", current,
                        (System.nanoTime() - start) / 1_000_000L);
            }
        }
    }
}
