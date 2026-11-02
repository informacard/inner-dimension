package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1140.class)
public abstract class BaptismSoundMixin {
 @Inject(method="method_43222",at=@At("HEAD"),cancellable=true)
 private void inner$baptismGain(float volume,class_3419 category,CallbackInfoReturnable<Float> ci){
  if(com.innerdimension.client.BaptismSilence.active())ci.setReturnValue(0F);
 }
 @Inject(method="method_43222",at=@At("RETURN"),cancellable=true)
 private void inner$informationGain(float volume,class_3419 category,CallbackInfoReturnable<Float> ci){
  if(com.innerdimension.client.UnlimitedVoidClient.active())ci.setReturnValue(ci.getReturnValue()*.18F);
 }
}
