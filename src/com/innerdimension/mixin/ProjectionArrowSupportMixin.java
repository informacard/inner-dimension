package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionProjectiles;
import net.minecraft.class_1665;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1665.class)
public abstract class ProjectionArrowSupportMixin {
    @Inject(method = "method_26351", at = @At("RETURN"), cancellable = true)
    private void inner$keepEmbedded(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && ProjectionProjectiles.supportsArrow((class_1665)(Object)this))
            cir.setReturnValue(false);
    }
}
