package com.innerdimension.mixin;

import com.innerdimension.centrifuge.LiquidProvenanceLedger;
import com.nonid.LiquidHolder;
import net.minecraft.class_11368;
import net.minecraft.class_11372;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public abstract class LiquidProvenancePersistenceMixin {
    @Inject(method = "method_5647", at = @At("TAIL"))
    private void innerDimension$writeSources(class_11372 view, CallbackInfo ci) {
        if ((Object)this instanceof LiquidHolder) {
            String encoded = LiquidProvenanceLedger.encode(this);
            if (!encoded.isEmpty()) view.method_71469("InnerDimensionLiquidSources", encoded);
        }
    }

    @Inject(method = "method_5651", at = @At("TAIL"))
    private void innerDimension$readSources(class_11368 view, CallbackInfo ci) {
        if ((Object)this instanceof LiquidHolder) {
            LiquidProvenanceLedger.decode(this, view.method_71441("InnerDimensionLiquidSources").orElse(""));
        }
    }
}
