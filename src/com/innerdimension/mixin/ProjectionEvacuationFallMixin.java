package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionEvacuation;
import net.minecraft.class_1309;
import net.minecraft.class_1282;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1309.class)
public abstract class ProjectionEvacuationFallMixin {
    @Inject(method="method_5747",at=@At("HEAD"),cancellable=true)
    private void inner$evacuationLanding(double distance,float multiplier,class_1282 source,CallbackInfoReturnable<Boolean> ci) {
        if (ProjectionEvacuation.consumeFall((class_1309)(Object)this)) ci.setReturnValue(false);
    }
}
