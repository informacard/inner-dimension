package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** First/third-person fog samples the camera point without changing the world's FluidState. */
@Mixin(class_4184.class)
public abstract class ProjectionCameraFluidMixin {
    @Shadow private class_1937 field_18710;
    @Shadow private class_243 field_18712;

    @Inject(method = "method_19334", at = @At("RETURN"), cancellable = true)
    private void inner$projectedSubmersion(CallbackInfoReturnable<class_5636> cir) {
        if (field_18710 == null || cir.getReturnValue() != class_5636.field_27888) return;
        ProjectionEnvironment.FluidContact contact = ProjectionEnvironment.fluidAt(field_18710, field_18712);
        if (contact.kind() == ProjectionEnvironment.FluidKind.WATER) cir.setReturnValue(class_5636.field_27886);
        else if (contact.kind() == ProjectionEnvironment.FluidKind.LAVA) cir.setReturnValue(class_5636.field_27885);
    }
}
