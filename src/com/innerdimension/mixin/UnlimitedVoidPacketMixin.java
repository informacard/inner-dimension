package com.innerdimension.mixin;
import com.innerdimension.domain.runtime.UnlimitedVoidControl;
import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_3244.class)
public abstract class UnlimitedVoidPacketMixin {
    @Shadow public class_3222 field_14140;
    // Vanilla schedules the packet onto the server thread before this semantic boundary.
    @Inject(method={"method_12063","method_12066","method_12046","method_12065","method_12062","method_12076","method_12070","method_12045","method_12078"},at=@At(value="INVOKE",target="Lnet/minecraft/class_2600;method_11073(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_3218;)V",shift=At.Shift.AFTER),cancellable=true)
    private void inner$informationActions(CallbackInfo ci){if(UnlimitedVoidControl.locked(field_14140)||com.innerdimension.voiddomain.RitualControl.locked(field_14140))ci.cancel();}
}
