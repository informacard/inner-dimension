package com.innerdimension.mixin;

import com.innerdimension.domain.ai.DomainAiPolicy;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Final server-side mob melee permission gate (verified class_1308.method_6121). */
@Mixin(class_1308.class)
public abstract class DomainMobAttackMixin {
    @Inject(method = "method_6121", at = @At("HEAD"), cancellable = true)
    private void innerDimension$denyMobMelee(class_3218 world, class_1297 target,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof class_1309 living
                && DomainAiPolicy.denyAttack((class_1308) (Object) this, living)) {
            cir.setReturnValue(false);
        }
    }
}
