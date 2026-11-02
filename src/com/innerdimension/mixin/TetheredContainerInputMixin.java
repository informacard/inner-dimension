package com.innerdimension.mixin;

import com.innerdimension.client.TetheredContainerMovementLock;
import net.minecraft.class_241;
import net.minecraft.class_743;
import net.minecraft.class_744;
import net.minecraft.class_10185;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_743.class, priority = 2000)
public abstract class TetheredContainerInputMixin extends class_744 {

    @Inject(method = "method_3129", at = @At("RETURN"))
    private void innerDimension$blockMovementWhileContainerIsOpen(CallbackInfo ci) {
        if (TetheredContainerMovementLock.isActive() || com.innerdimension.client.ExtractionClient.active()) {
            field_54155 = class_10185.field_54098;
            field_55868 = class_241.field_1340;
        }
    }
}
