package com.innerdimension.mixin;

import com.innerdimension.domain.ability.D43;
import net.minecraft.class_1309;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * D43 rapid reload. Verified intermediary target:
 * {@code class_1764.method_7775(class_1799, class_1309)} — the concrete
 * {@code CrossbowItem.getPullTime(ItemStack, LivingEntity)}, which is the bundled
 * (Quick Charge and every other modifier applied) charge duration in ticks:
 * {@code floor(enchantmentModifiedSeconds * 20)}. There is no {@code method_7868} in the
 * 1.21.11 intermediary jar; {@code class_1811} is the abstract ranged-weapon base and only
 * {@code class_1764} carries the final pull time.
 *
 * <p>The result is halved with a floor of 5 ticks, and ammunition is still consumed by the
 * untouched vanilla charge path.</p>
 */
@Mixin(class_1764.class)
public abstract class DomainCrossbowMixin {
    @Inject(method = "method_7775", at = @At("RETURN"), cancellable = true)
    private static void innerDimension$d43ChargeTime(class_1799 stack, class_1309 user,
                                                     CallbackInfoReturnable<Integer> cir) {
        if (!D43.shouldHalveCharge(user)) return;
        int ticks = cir.getReturnValue();
        cir.setReturnValue(Math.max(5, ticks / 2));
    }
}
