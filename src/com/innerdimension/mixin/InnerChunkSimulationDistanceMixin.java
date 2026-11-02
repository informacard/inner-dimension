package com.innerdimension.mixin;

import com.innerdimension.dimension.DynamicDimensionManager;
import net.minecraft.class_3215;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Both usable rooms are in (0,0); one neighbouring chunk is enough to simulate. */
@Mixin(class_3215.class)
public abstract class InnerChunkSimulationDistanceMixin {
    @Shadow @Final private class_3218 field_13945;

    @ModifyArg(method = {"<init>", "method_38634"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_3204;method_38629(I)V"), index = 0)
    private int inner$smallRoomSimulation(int requested) {
        return DynamicDimensionManager.isInnerWorld(field_13945.method_27983()) ? 1 : requested;
    }
}
