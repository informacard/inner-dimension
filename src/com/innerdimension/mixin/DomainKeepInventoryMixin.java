package com.innerdimension.mixin;

import com.innerdimension.domain.ability.D16;
import net.minecraft.class_12279;
import net.minecraft.class_1657;
import net.minecraft.class_1928;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Makes only D16's current death use vanilla's existing keep-inventory branch. */
@Mixin(class_1657.class)
public abstract class DomainKeepInventoryMixin {
    @Redirect(method = "method_16078", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/class_1928;method_76185(Lnet/minecraft/class_12279;)Ljava/lang/Object;"))
    private Object innerDimension$d16VanillaKeepInventory(class_1928 rules, class_12279<?> rule) {
        Object vanilla = rules.method_76185(rule);
        if (Boolean.TRUE.equals(vanilla)) return vanilla;
        return (Object) this instanceof class_3222 player && D16.keepInventoryForDeath(player)
                ? Boolean.TRUE : vanilla;
    }
}
