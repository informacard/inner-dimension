package com.innerdimension.mixin;

import com.innerdimension.domain.world.DomainTeleportGuard;
import com.innerdimension.projection.ProjectionBlocks;
import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep vanilla face selection and teleport attempts, including projected support/collision. */
@Mixin(class_1606.class)
public abstract class ProjectionShulkerEntityMixin {
    @Shadow protected abstract class_2350 method_24351(class_2338 pos);

    @Inject(method = "method_7127", at = @At("HEAD"), cancellable = true)
    private void inner$preventTeleport(CallbackInfoReturnable<Boolean> cir) {
        class_1297 self = (class_1297)(Object)this;
        if (DomainTeleportGuard.blocked(self, self.method_23317(), self.method_23318(), self.method_23321()))
            cir.setReturnValue(false);
    }

    @Redirect(method = "method_7127", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1606;method_24351(Lnet/minecraft/class_2338;)Lnet/minecraft/class_2350;"))
    private class_2350 inner$destination(class_1606 self, class_2338 pos) {
        if (DomainTeleportGuard.blocked(self, pos.method_10263() + 0.5, pos.method_10264(), pos.method_10260() + 0.5))
            return null;
        return method_24351(pos);
    }

    @Redirect(method = "method_33351", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1937;method_8320(Lnet/minecraft/class_2338;)Lnet/minecraft/class_2680;"))
    private class_2680 inner$occupied(class_1937 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "method_24350", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1937;method_24368(Lnet/minecraft/class_2338;Lnet/minecraft/class_1297;Lnet/minecraft/class_2350;)Z"))
    private boolean inner$attach(class_1937 world, class_2338 pos, class_1297 entity, class_2350 face) {
        class_2680 projected = ProjectionBlocks.projected(world, pos);
        return projected == null ? world.method_24368(pos, entity, face)
                : projected.method_26169(world, pos, entity, face);
    }

    @Redirect(method = {"method_24350", "method_7127"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1937;method_8587(Lnet/minecraft/class_1297;Lnet/minecraft/class_238;)Z"))
    private boolean inner$clear(class_1937 world, class_1297 entity, class_238 box) {
        return world.method_8587(entity, box) && !ProjectionEnvironment.projectedCollision(world, box);
    }
}
