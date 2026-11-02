package com.innerdimension.mixin;

import com.innerdimension.transport.SafeEntityTeleport;
import net.minecraft.class_1297;
import net.minecraft.class_3218;
import net.minecraft.class_5454;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1297.class)
public abstract class EntityAnimationTeleportMixin {
    @Inject(method = "method_5731", at = @At("HEAD"), cancellable = true)
    private void inner$stopBeforeTeleport(class_5454 transition, CallbackInfoReturnable<class_1297> callback) {
        class_1297 entity = (class_1297) (Object) this;
        if (entity.method_73183() instanceof class_3218 && !SafeEntityTeleport.prepare(entity)) {
            callback.setReturnValue(null);
        }
    }

    @Inject(method = "method_5859", at = @At("HEAD"), cancellable = true)
    private void inner$stopBeforeRequestTeleport(double x, double y, double z, CallbackInfo callback) {
        class_1297 entity = (class_1297) (Object) this;
        if (entity.method_73183() instanceof class_3218 && !SafeEntityTeleport.prepare(entity)) callback.cancel();
    }
}
