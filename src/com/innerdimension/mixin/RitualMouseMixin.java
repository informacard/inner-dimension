package com.innerdimension.mixin;
import com.innerdimension.client.RitualInput;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_312.class)
public abstract class RitualMouseMixin {
    @Inject(method="method_1601",at=@At("HEAD"),cancellable=true)
    private void inner$ritualButton(long window,class_11910 button,int action,CallbackInfo ci){if(action!=0&&RitualInput.active()&&class_310.method_1551().field_1755==null)ci.cancel();}
    @Inject(method="method_1598",at=@At("HEAD"),cancellable=true)
    private void inner$ritualScroll(CallbackInfo ci){if(RitualInput.active()&&class_310.method_1551().field_1755==null)ci.cancel();}
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="method_1606",at=@At(value="INVOKE",target="Lnet/minecraft/class_746;method_5872(DD)V"))
    private void inner$ritualLook(class_746 player,double x,double y,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original){if(!RitualInput.active())original.call(player,x,y);}
}
