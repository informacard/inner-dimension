package com.innerdimension.mixin;
import com.innerdimension.domain.ability.D41;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Supplies a temporary extra equipment slot without replacing chest armor. */
@Mixin(class_1309.class)
public abstract class DomainGlideEligibilityMixin {
    @Inject(method = "method_6118", at = @At("RETURN"), cancellable = true)
    private void innerDimension$domainWings(class_1304 slot, CallbackInfoReturnable<class_1799> cir) {
        if (slot == class_1304.field_48824 && cir.getReturnValue().method_7960()
                && D41.allowsVanillaGlide((class_1309) (Object) this))
            cir.setReturnValue(D41.wings());
    }
}
