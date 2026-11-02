package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1308.class)
public abstract class UnlimitedVoidMobMixin {
    @Inject(method="method_5987",at=@At("HEAD"),cancellable=true)
    private void inner$informationNoAi(CallbackInfoReturnable<Boolean> ci){
        if(com.innerdimension.domain.runtime.UnlimitedVoidControl.locked((class_1297)(Object)this))ci.setReturnValue(true);
    }
}
