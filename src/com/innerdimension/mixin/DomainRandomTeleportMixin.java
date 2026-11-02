package com.innerdimension.mixin;

import com.innerdimension.domain.world.DomainTeleportGuard;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Endermen and chorus fruit use LivingEntity.randomTeleport, not Entity.teleport. */
@Mixin(class_1309.class)
public abstract class DomainRandomTeleportMixin {
    @Inject(method = "method_6082", at = @At("HEAD"), cancellable = true)
    private void inner$randomTeleport(double x, double y, double z, boolean particles,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (DomainTeleportGuard.blocked((class_1297)(Object)this, x, y, z))
            cir.setReturnValue(false);
    }
}
