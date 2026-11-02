package com.innerdimension.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Attenuate only D07/D11's contribution to vanilla movement-speed FOV. */
@Mixin(class_742.class)
public abstract class DomainSpeedFovMixin {
    @ModifyExpressionValue(method = "method_3118", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_742;method_45325(Lnet/minecraft/class_6880;)D"))
    private double inner$gentlerDomainSpeedFov(double speed) {
        var player = (class_742) (Object) this;
        var attribute = player.method_5996(class_5134.field_23719);
        if (attribute == null) return speed;
        for (String path : new String[] {"d07_speed", "d11_speed"}) {
            var modifier = attribute.method_6199(class_2960.method_60655("inner_dimension", path));
            if (modifier == null || modifier.comp_2450() != class_1322.class_1323.field_6331) continue;
            double amount = modifier.comp_2449();
            if (amount > 0) speed *= (1 + amount * .35) / (1 + amount);
        }
        return speed;
    }
}
