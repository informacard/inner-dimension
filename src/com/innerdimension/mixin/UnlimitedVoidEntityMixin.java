package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1297.class)
public abstract class UnlimitedVoidEntityMixin {
    @Inject(method="method_5784",at=@At("HEAD"),cancellable=true)
    private void inner$informationMovement(class_1313 type,class_243 movement,CallbackInfo ci){
        if(com.innerdimension.domain.runtime.UnlimitedVoidControl.locked((class_1297)(Object)this))ci.cancel();
    }
}
