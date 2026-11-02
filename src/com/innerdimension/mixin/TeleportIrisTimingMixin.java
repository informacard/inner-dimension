package com.innerdimension.mixin;

import com.innerdimension.client.TeleportDiagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.PipelineManager", remap = false)
public abstract class TeleportIrisTimingMixin {
    @WrapMethod(method = "preparePipeline", remap = false)
    private WorldRenderingPipeline inner$timePipeline(NamespacedId dimension,
                                                       Operation<WorldRenderingPipeline> original) {
        long start = TeleportDiagnostics.begin();
        try { return original.call(dimension); }
        finally {
            if (start != 0) TeleportDiagnostics.span("iris.prepare_pipeline." + dimension, start);
        }
    }

    @WrapMethod(method = "destroyPipeline", remap = false)
    private void inner$timePipelineDestroy(Operation<Void> original) {
        long start = TeleportDiagnostics.begin();
        try { original.call(); }
        finally { TeleportDiagnostics.span("iris.destroy_pipeline", start); }
    }
}
