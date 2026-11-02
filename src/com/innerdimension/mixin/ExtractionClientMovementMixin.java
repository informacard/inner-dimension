package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Stop local movement before physics/auto-jump and camera interpolation can observe it. */
@Mixin(value=class_746.class,priority=2000)
public abstract class ExtractionClientMovementMixin {
 @Inject(method="method_5784",at=@At("HEAD"),cancellable=true)
 private void inner$holdExtraction(class_1313 type,class_243 delta,CallbackInfo ci){
  if(com.innerdimension.client.ExtractionClient.active()){
   ((class_746)(Object)this).method_18799(class_243.field_1353);ci.cancel();
  }
 }
}
