package com.innerdimension.mixin;

import com.innerdimension.domain.world.DomainFoodGuard;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * D17 mule endurance, exhaustion entry. Verified intermediary target:
 * {@code class_1657.method_7322(float)} (addExhaustion), which delegates to
 * {@code class_1702.method_7583(float)} (HungerManager.addExhaustion).
 *
 * <p>Exhaustion is the only natural driver of food-level and saturation loss, and it is
 * also what the Hunger status effect and ordinary sprinting/jumping feed. Refusing it at
 * this entry point (never by rewriting the stored values every tick) satisfies the
 * blueprint's "natural healing still uses the existing hunger condition but must not
 * deduct" rule. Direct food/saturation decreases are covered separately by
 * {@link DomainHungerMixin}.</p>
 */
@Mixin(class_1657.class)
public abstract class DomainFoodMixin {
    @Inject(method = "method_7322", at = @At("HEAD"), cancellable = true)
    private void innerDimension$d17Exhaustion(float amount, CallbackInfo ci) {
        if (amount > 0 && DomainFoodGuard.protectedFromHunger((class_1657) (Object) this)) ci.cancel();
    }
}
