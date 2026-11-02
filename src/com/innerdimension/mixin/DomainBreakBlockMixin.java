package com.innerdimension.mixin;
import net.minecraft.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1937.class) public abstract class DomainBreakBlockMixin {
 @Inject(method="method_30093",at=@At("HEAD"),cancellable=true) private void inner$locked(class_2338 pos,boolean drop,class_1297 entity,int depth,CallbackInfoReturnable<Boolean> ci){if(com.innerdimension.domain.ability.D64.locked((class_1937)(Object)this,pos))ci.setReturnValue(false);}
}
