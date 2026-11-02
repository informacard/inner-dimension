package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionProjectiles;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1941.class)
public interface ProjectionProjectileRaycastMixin {
    @Inject(method = "method_61717", at = @At("RETURN"), cancellable = true)
    private void inner$projectedBlocks(class_3959 ray, CallbackInfoReturnable<class_3965> cir) {
        if ((Object)this instanceof class_1937 world)
            cir.setReturnValue(ProjectionProjectiles.hit(world, ray, cir.getReturnValue()));
    }
}
