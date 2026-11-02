package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_3244.class)
public abstract class ExtractionMovementMixin {
 @Shadow public class_3222 field_14140;
 // Keep the held tool and bottle in their slots until the transaction ends: movement,
 // dropping/swapping, hotbar selection, inventory clicks and creative inventory writes.
 @Inject(method={"method_12063","method_12066","method_12056","method_12076","method_12070"},at=@At(value="INVOKE",target="Lnet/minecraft/class_2600;method_11073(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_3218;)V",shift=At.Shift.AFTER),cancellable=true)
 private void inner$extractMovement(CallbackInfo ci){if(com.innerdimension.item.SemenExtraction.active(field_14140))ci.cancel();}
}
