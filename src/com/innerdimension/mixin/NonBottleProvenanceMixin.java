package com.innerdimension.mixin;

import com.innerdimension.centrifuge.LiquidProvenanceLedger;
import com.nonid.LiquidHolder;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nonid.NonItemSystem", remap = false)
public abstract class NonBottleProvenanceMixin {
    @Inject(method = "createBottleStackForTank", at = @At("RETURN"), remap = false)
    private static void innerDimension$attachSources(LiquidHolder holder, CallbackInfoReturnable<class_1799> cir) {
        LiquidProvenanceLedger.attachBottle(holder, cir.getReturnValue());
    }
}
