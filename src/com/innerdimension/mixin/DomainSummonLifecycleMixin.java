package com.innerdimension.mixin;
import net.minecraft.class_1308;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1308.class) public abstract class DomainSummonLifecycleMixin {
 @Inject(method="method_5773",at=@At("HEAD")) private void inner$orphan(CallbackInfo ci){com.innerdimension.domain.ability.DomainSummons.discardOrphan((class_1308)(Object)this);}
}
