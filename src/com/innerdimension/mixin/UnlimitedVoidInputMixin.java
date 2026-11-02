package com.innerdimension.mixin;
import com.innerdimension.client.UnlimitedVoidClient;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(class_310.class)
public abstract class UnlimitedVoidInputMixin {
    @Inject(method="method_1536",at=@At("HEAD"),cancellable=true)
    private void inner$informationAttack(CallbackInfoReturnable<Boolean> ci){if(UnlimitedVoidClient.active()||com.innerdimension.client.RitualInput.active())ci.setReturnValue(false);}
    @Inject(method={"method_1590","method_1583"},at=@At("HEAD"),cancellable=true)
    private void inner$informationUse(CallbackInfo ci){if(UnlimitedVoidClient.active()||com.innerdimension.client.RitualInput.active())ci.cancel();}
}
