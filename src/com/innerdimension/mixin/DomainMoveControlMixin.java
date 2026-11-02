package com.innerdimension.mixin;

import com.innerdimension.domain.ai.DomainAiPolicy;
import net.minecraft.class_1308;
import net.minecraft.class_1335;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * D47 gaze-freeze, movement half. Verified intermediary target:
 * {@code class_1335.method_6240()} (MoveControl.tick), reading {@code class_1335.field_6371}
 * (the controlled mob).
 *
 * <p>PathNavigation.tick only <em>submits</em> a wanted position; MoveControl.tick is what
 * actually steers the body towards it. Cancelling it here stops active AI locomotion while
 * leaving the entity's velocity, gravity, levitation and external knockback completely
 * alone, which is exactly what the blueprint requires for D47 (and keeps D08's drive-off
 * intent alive while frozen).</p>
 */
@Mixin(class_1335.class)
public abstract class DomainMoveControlMixin {
    @Shadow @Final protected class_1308 field_6371;

    @Inject(method = "method_6240", at = @At("HEAD"), cancellable = true)
    private void innerDimension$d47MoveTick(CallbackInfo ci) {
        if (field_6371 != null && DomainAiPolicy.denyMove(field_6371)) ci.cancel();
    }
}
