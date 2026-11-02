package com.innerdimension.mixin;
import net.minecraft.*;import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_3225.class) public abstract class DomainPlayerBreakMixin {
 @Shadow protected class_3218 field_14007;
 @Inject(method="method_14266",at=@At("HEAD"),cancellable=true) private void inner$locked(class_2338 pos,CallbackInfoReturnable<Boolean> ci){if(com.innerdimension.domain.ability.D64.locked(field_14007,pos))ci.setReturnValue(false);}
}
