package com.innerdimension.mixin;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_312.class)
public abstract class DomainCoreScrollMixin {
    @Inject(method="method_1598",at=@At("HEAD"),cancellable=true)
    private void inner$binding(long window,double horizontal,double vertical,CallbackInfo ci){if(com.innerdimension.client.DomainWheelInput.scroll(vertical))ci.cancel();}
}
