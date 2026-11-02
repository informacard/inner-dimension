package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionProjectiles;
import net.minecraft.class_1937;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Include exact projected block shapes in vanilla's eight third-person camera probes. */
@Mixin(class_4184.class)
public abstract class ProjectionCameraCollisionMixin {
    @Redirect(method = "method_19318", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1937;method_17742(Lnet/minecraft/class_3959;)Lnet/minecraft/class_3965;"))
    private class_3965 inner$cameraBlocks(class_1937 world, class_3959 ray) {
        return ProjectionProjectiles.hit(world, ray, world.method_17742(ray), true);
    }
}
