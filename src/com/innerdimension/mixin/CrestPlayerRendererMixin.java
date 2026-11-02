package com.innerdimension.mixin;

import com.innerdimension.client.CrestPlayerRenderState;
import net.minecraft.class_1007;
import net.minecraft.class_10055;
import net.minecraft.class_11890;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1007.class)
public abstract class CrestPlayerRendererMixin {
    @Inject(method="method_62604",at=@At("TAIL"))
    private void inner$copyCrestOwner(class_11890 player, class_10055 state, float tickDelta, CallbackInfo ci) {
        ((CrestPlayerRenderState)state).inner$crestPlayer(player.method_5667());
    }
}
