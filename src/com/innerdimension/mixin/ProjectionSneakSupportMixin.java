package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Include projected floors in vanilla's sneak edge checks on both client and server. */
@Mixin(class_1657.class)
public abstract class ProjectionSneakSupportMixin {
    @Inject(method = "method_59818", at = @At("RETURN"), cancellable = true)
    private void inner$projectedSupport(double x, double z, double depth,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        class_1657 player = (class_1657)(Object)this;
        class_238 body = player.method_5829();
        // Match vanilla's footprint and downward probe, preserving protection at real edges.
        class_238 probe = new class_238(body.field_1323 + 1.0E-7 + x,
                body.field_1322 - depth - 1.0E-7, body.field_1321 + 1.0E-7 + z,
                body.field_1320 - 1.0E-7 + x, body.field_1322,
                body.field_1324 - 1.0E-7 + z);
        if (ProjectionEnvironment.projectedCollision(player.method_73183(), probe)
                || !ProjectionEnvironment.lavaSurfaceShapes(player, probe).isEmpty())
            cir.setReturnValue(false);
    }
}
