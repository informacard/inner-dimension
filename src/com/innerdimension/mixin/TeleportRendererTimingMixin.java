package com.innerdimension.mixin;

import com.innerdimension.client.TeleportDiagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_638;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_761.class)
public abstract class TeleportRendererTimingMixin {
    @WrapMethod(method = "method_3279")
    private void inner$timeRendererReload(Operation<Void> original) {
        long start = TeleportDiagnostics.begin();
        try { original.call(); }
        finally { TeleportDiagnostics.span("renderer.reload", start); }
    }

    @WrapMethod(method = "method_3244")
    private void inner$timeRendererWorld(class_638 world, Operation<Void> original) {
        long start = TeleportDiagnostics.begin();
        try { original.call(world); }
        finally { TeleportDiagnostics.span("renderer.set_world", start); }
    }
}
