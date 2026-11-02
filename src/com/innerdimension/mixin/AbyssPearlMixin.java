package com.innerdimension.mixin;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.progression.InnerAdvancements;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1684.class)
public abstract class AbyssPearlMixin {
    @Inject(method="method_7488", at=@At("HEAD"), cancellable=true)
    private void inner$abyss(class_239 hit, CallbackInfo ci) {
        var pearl = (class_1684)(Object)this;
        if (hit instanceof class_3965 block && pearl.method_73183().method_8320(block.method_17777()).method_26204() == ModBlocks.ABYSS_BLOCK
                && pearl.method_24921() instanceof class_3222 player) {
            InnerAdvancements.grant(player, "gaze_abyss");
            pearl.method_31472();
            ci.cancel();
        }
    }
}
