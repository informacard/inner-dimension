package com.innerdimension.mixin;

import com.innerdimension.domain.combat.DomainAttackPipeline;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainHealingPipeline;
import com.innerdimension.domain.combat.DomainKnockbackPipeline;
import com.innerdimension.domain.projectile.DomainProjectileState;
import com.innerdimension.domain.runtime.AttributeLeaseManager;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The narrow bridge between vanilla's living-entity health path and this mod's pipelines.
 * Nothing here decides anything: every branch forwards a semantic event and lets the
 * combat services combine the covering sessions.
 *
 * <p>Verified intermediary targets:
 * {@code class_1309.method_64397(ServerWorld,DamageSource,float)},
 * {@code class_1309.method_6074(ServerWorld,DamageSource,float)} (actuallyHurt),
 * {@code class_1309.method_6025(float)} (heal),
 * {@code class_1309.method_6005(double,double,double)} (knockback),
 * {@code class_1297.method_5650(RemovalReason)} (remove).</p>
 */
@Mixin(class_1309.class)
public abstract class DomainLivingEntityMixin {
    @Inject(method="method_5773",at=@At("HEAD")) private void inner$restoreName(CallbackInfo ci){com.innerdimension.domain.ability.D74.restoreOrphanName((net.minecraft.class_1309)(Object)this);com.innerdimension.domain.ability.D78.restoreOrphan((net.minecraft.class_1309)(Object)this);}
    @ModifyVariable(method = "method_6091", at = @At("HEAD"), argsOnly = true)
    private net.minecraft.class_243 inner$gazeMovement(net.minecraft.class_243 input) {
        if ((Object)this instanceof net.minecraft.class_1308 mob
                && com.innerdimension.domain.ai.DomainAiPolicy.denyMove(mob)) return net.minecraft.class_243.field_1353;
        return input;
    }
    /** D10 ambush bonus, merged into the same damage request as the melee hit. */
    @ModifyVariable(method = "method_64397", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float innerDimension$domainDamage(float amount, class_3218 world, class_1282 source) {
        return amount + DomainAttackPipeline.consumeBonus((class_1309) (Object) this, source);
    }

    /**
     * D14 x0.4 (postArmor) — {@code method_6074} is the actual-hurt boundary, so armor and
     * resistance have already been applied by the caller.
     */
    @ModifyVariable(method = "method_6074", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float innerDimension$domainFinalDamage(float amount, class_3218 world, class_1282 source) {
        return DomainDamagePipeline.finalAmount((class_1309) (Object) this, source, amount);
    }

    /** D34/D59 healing reduction. */
    @ModifyVariable(method = "method_6025", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float innerDimension$domainHealing(float amount) {
        return DomainHealingPipeline.modify((class_1309) (Object) this, amount);
    }

    @Inject(method = "method_6025", at = @At("RETURN"))
    private void innerDimension$domainHealed(float amount, CallbackInfo ci) {
        if (amount > 0) DomainHealingPipeline.after((class_1309) (Object) this, amount);
    }

    /**
     * D56 multiplier / D48 upward conversion. The handler captures the full argument list
     * of {@code method_6005}, so the horizontal components are read from the same call
     * without any ThreadLocal hand-off or injection ordering assumption.
     */
    @ModifyVariable(method = "method_6005", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double innerDimension$domainKnockback(double strength, double x, double z) {
        return DomainKnockbackPipeline.strengthFactor((class_1309) (Object) this, strength, x, z);
    }

    @Inject(method = "method_6005", at = @At("RETURN"))
    private void innerDimension$domainKnockbackApplied(CallbackInfo ci) {
        DomainKnockbackPipeline.afterKnockback((class_1309) (Object) this);
    }

    /** A removed entity must not keep this mod's attribute leases or projectile state. */
    @Inject(method = "method_5650", at = @At("HEAD"))
    private void innerDimension$domainForget(class_1297.class_5529 reason, CallbackInfo ci) {
        if ((Object) this instanceof class_1309 living) {
            AttributeLeaseManager.revokeEntity(living);
            DomainProjectileState.forget(living);
        }
    }
}
