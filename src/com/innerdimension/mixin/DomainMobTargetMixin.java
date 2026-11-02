package com.innerdimension.mixin;

import com.innerdimension.domain.ai.DomainAiPolicy;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Target-selector gate. Verified intermediary target:
 * {@code class_1308.method_5980(class_1309)} (setTarget).
 *
 * <p>Only the target <em>choice</em> is refused (D08 creeper/phantom drive-off, D53 golden
 * truce, D57 frenzy exclusions). The mob's permanent hate memory, its physics and any
 * external knockback are untouched, and the flag is never set.</p>
 */
@Mixin(class_1308.class)
public abstract class DomainMobTargetMixin {
    @org.spongepowered.asm.mixin.Unique private net.minecraft.class_243 inner$beforeAi;
    @ModifyVariable(method = "method_5980", at = @At("HEAD"), argsOnly = true)
    private class_1309 inner$preferredTarget(class_1309 target) {
        var preferred = DomainAiPolicy.preferred((class_1308)(Object)this);
        return preferred.isEmpty() ? target : preferred.getFirst();
    }
    @Inject(method = {"method_5968", "method_59664"}, at = @At("RETURN"), cancellable = true)
    private void inner$readPreferredTarget(CallbackInfoReturnable<class_1309> cir) {
        var preferred = DomainAiPolicy.preferred((class_1308)(Object)this);
        if (!preferred.isEmpty()) cir.setReturnValue(preferred.getFirst());
    }
    @Inject(method = "method_6023", at = @At("HEAD"))
    private void inner$commitPreferredTarget(CallbackInfo ci) {
        inner$beforeAi = ((class_1308)(Object)this).method_18798();
        DomainAiPolicy.pushPreferredTarget((class_1308)(Object)this);
    }
    @Inject(method = "method_6023", at = @At("RETURN"))
    private void inner$freezeMovementIntent(CallbackInfo ci) {
        class_1308 mob = (class_1308)(Object)this;
        DomainAiPolicy.pushPreferredTarget(mob);
        if (!DomainAiPolicy.denyMove(mob)) return;
        if (inner$beforeAi != null) {
            mob.method_18799(inner$beforeAi);
        }
        mob.field_6212 = 0;
        mob.field_6250 = 0;
        mob.field_6227 = 0;
        mob.method_6100(false);
    }
    @Inject(method = "method_5980", at = @At("HEAD"), cancellable = true)
    private void innerDimension$domainTarget(class_1309 target, CallbackInfo ci) {
        if (target == null) return;
        if (DomainAiPolicy.denyTarget((class_1308) (Object) this, target)) ci.cancel();
    }
}
