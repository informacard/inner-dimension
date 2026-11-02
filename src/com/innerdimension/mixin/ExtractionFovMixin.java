package com.innerdimension.mixin;
import net.minecraft.*;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(class_757.class)
public abstract class ExtractionFovMixin {
 // Compose with zoom mods instead of cancelling before their return modifiers run.
 @ModifyReturnValue(method="method_3196",at=@At("RETURN"))
 private float inner$extractFov(float original,class_4184 camera,float delta,boolean changing){
  return original*com.innerdimension.client.ExtractionClient.scale(delta);
 }
}
