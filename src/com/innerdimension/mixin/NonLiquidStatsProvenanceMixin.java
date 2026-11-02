package com.innerdimension.mixin;

import com.innerdimension.centrifuge.LiquidProvenanceLedger;
import com.nonid.NonStats;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NonStats.class, remap = false)
public abstract class NonLiquidStatsProvenanceMixin {
    @Inject(method = "addLiquidGained", at = @At("TAIL"), remap = false)
    private static void innerDimension$recordLiquid(class_3222 player, int amount,
                                                     Map<class_2960, Integer> sources, CallbackInfo ci) {
        LiquidProvenanceLedger.record(player, amount, sources);
    }
}
