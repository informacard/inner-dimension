package com.innerdimension.mixin;

import com.innerdimension.client.domain.DomainClientState;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The ranged slash replaces melee/mining while the active domain grants its input. */
@Mixin(class_310.class)
public abstract class DomainSlashInputMixin {
    @Inject(method = "method_1536", at = @At("HEAD"), cancellable = true)
    private void inner$slashAttack(CallbackInfoReturnable<Boolean> ci) {
        if (DomainClientState.d44Available()) ci.setReturnValue(false);
        else if(com.innerdimension.client.ProjectionPicking.attack((class_310)(Object)this))ci.setReturnValue(true);
    }
    @Inject(method = "method_1590", at = @At("HEAD"), cancellable = true)
    private void inner$slashMining(boolean breaking, CallbackInfo ci) {
        if (DomainClientState.d44Available()) ci.cancel();
    }
}
