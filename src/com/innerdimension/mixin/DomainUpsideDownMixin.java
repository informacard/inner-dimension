package com.innerdimension.mixin;
import net.minecraft.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Reuse the vanilla Dinnerbone render transform, including the player renderer. */
@Mixin({class_922.class,class_1007.class}) public abstract class DomainUpsideDownMixin {
 @Inject(method="method_38563",at=@At("HEAD"),cancellable=true) private void inner$inverted(class_1309 entity,CallbackInfoReturnable<Boolean> ci){var effect=com.innerdimension.domain.runtime.EffectLeaseManager.effect("inner_dimension:upside_down");if(effect!=null&&entity.method_6059(effect))ci.setReturnValue(true);}
}
