package com.innerdimension.mixin;

import fi.dy.masa.litematica.selection.Box;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Litematica recreates an unused one-cell selection at zero in every new dimension. */
@Pseudo
@Mixin(targets = "fi.dy.masa.litematica.render.OverlayRenderer", remap = false)
public abstract class InnerDefaultSelectionMixin {
    @Inject(method = "renderSelectionBox", at = @At("HEAD"), cancellable = true, remap = false)
    private void inner$hideUnusedOrigin(Box box, @Coerce Object type, float red, float green, float blue,
                                       @Coerce Object placement, Matrix4f matrix, CallbackInfo ci) {
        var world = class_310.method_1551().field_1687;
        if (world == null || !com.innerdimension.dimension.DynamicDimensionManager.isInnerWorld(world.method_27983())) return;
        if (placement == null && type instanceof Enum<?> value && value.name().startsWith("AREA_")
                && "Unnamed".equals(box.getName())
                && class_2338.field_10980.equals(box.getPos1())
                && class_2338.field_10980.equals(box.getPos2())) ci.cancel();
    }
}
