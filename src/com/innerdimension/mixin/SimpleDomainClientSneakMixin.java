package com.innerdimension.mixin;

import com.innerdimension.client.domain.DomainContestClient;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** LocalPlayer reads input directly instead of Entity's tracked sneak flag. */
@Mixin(class_746.class)
public abstract class SimpleDomainClientSneakMixin {
    @Inject(method={"method_5715","method_18276"},at=@At("RETURN"),cancellable=true)
    private void inner$mandatoryCrouch(CallbackInfoReturnable<Boolean> cir) {
        if(DomainContestClient.simple())cir.setReturnValue(true);
    }
}
