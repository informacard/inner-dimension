package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Packet filtering alone cannot stop server-side gravity, pushes or piston movement. */
@Mixin(value=class_1297.class,priority=2000)
public abstract class ExtractionEntityMovementMixin {
 @Inject(method="method_5784",at=@At("HEAD"),cancellable=true)
 private void inner$holdExtraction(class_1313 type,class_243 delta,CallbackInfo ci){
  if((Object)this instanceof class_3222 player&&com.innerdimension.item.SemenExtraction.active(player)){
   player.method_18799(class_243.field_1353);ci.cancel();
  }
 }
}
