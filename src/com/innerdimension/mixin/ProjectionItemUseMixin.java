package com.innerdimension.mixin;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_310.class)
public abstract class ProjectionItemUseMixin {
 @org.spongepowered.asm.mixin.Shadow private int field_1752;
 @Inject(method="method_1583",at=@At("HEAD"),cancellable=true)
 private void inner$projectionItemUse(CallbackInfo ci){if(field_1752<=0&&com.innerdimension.client.ProjectionPicking.use((class_310)(Object)this)){field_1752=4;ci.cancel();}}
}
