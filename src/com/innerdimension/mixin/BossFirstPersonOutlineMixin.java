package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Hide the camera player's own glowing silhouette without changing server exposure. */
@Mixin(class_1297.class)
public abstract class BossFirstPersonOutlineMixin {
    @Inject(method="method_5851",at=@At("HEAD"),cancellable=true)
    private void inner$hideFirstPersonOutline(CallbackInfoReturnable<Boolean> ci){
        var client=class_310.method_1551();
        if((Object)this==client.field_1724&&client.field_1690.method_31044().method_31034())ci.setReturnValue(false);
    }
}
