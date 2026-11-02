package com.innerdimension.mixin;

import com.innerdimension.client.EntryTransition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_332;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public abstract class EntryTransitionRenderMixin {
    @Inject(method = "method_3192", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_11228;method_70890(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private void inner$coverEntryReset(class_9779 ticks, boolean tick, CallbackInfo callback,
                                       @Local class_332 graphics) {
        EntryTransition.render(graphics);
    }
}
