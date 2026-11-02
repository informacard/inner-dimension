package com.innerdimension.mixin;

import com.innerdimension.client.ProjectionRenderPosition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Change only the section lookup after the normal distance/frustum checks passed. */
@Mixin(class_761.class)
public abstract class ProjectionRenderSectionMixin {
    @WrapOperation(method = "method_72917", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1297;method_24515()Lnet/minecraft/class_2338;"))
    private class_2338 inner$projectionSection(class_1297 entity, Operation<class_2338> original) {
        return ProjectionRenderPosition.sectionPosition(entity, original.call(entity));
    }
}
