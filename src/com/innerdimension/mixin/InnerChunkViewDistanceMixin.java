package com.innerdimension.mixin;

import com.innerdimension.dimension.DynamicDimensionManager;
import net.minecraft.class_3218;
import net.minecraft.class_3898;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Keep vanilla's supported view-distance minimum and its lighting margins. */
@Mixin(class_3898.class)
public abstract class InnerChunkViewDistanceMixin {
    @Shadow @Final private class_3218 field_17214;

    @ModifyVariable(method = "method_17214", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int inner$smallRoomView(int requested) {
        return DynamicDimensionManager.isInnerWorld(field_17214.method_27983()) ? 2 : requested;
    }
}
