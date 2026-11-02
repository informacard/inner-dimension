package com.innerdimension.mixin;

import com.innerdimension.domain.ai.DomainAiPolicy;
import net.minecraft.class_1308;
import net.minecraft.class_1408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * D47 gaze-freeze, path half. Verified intermediary target:
 * {@code class_1408.method_6340()} (PathNavigation.tick).
 *
 * <p>Cancelling the navigation tick stops the AI from committing new paths or new
 * {@code MoveControl} targets. The mob keeps its current physical velocity, gravity,
 * D67 levitation and any external knockback — this is a refusal of the <em>intent</em>,
 * never {@code setNoAi(true)} and never a path teleport. The companion
 * {@link DomainMoveControlMixin} freezes the already-committed wanted position, and
 * {@code method_61441()} (stop) is left to the mob's own AI so it can resume cleanly.</p>
 */
@Mixin(class_1408.class)
public abstract class DomainNavigationMixin {
    @Inject(method = "method_6340", at = @At("HEAD"), cancellable = true)
    private void innerDimension$d47NavigationTick(CallbackInfo ci) {
        class_1308 mob = ((ProjectionNavigationAccessor) (Object) this).inner$getMob();
        if (mob != null && DomainAiPolicy.denyMove(mob)) ci.cancel();
    }
}
