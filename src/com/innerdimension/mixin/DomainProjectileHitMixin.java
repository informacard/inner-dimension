package com.innerdimension.mixin;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.projectile.DomainProjectileState;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Resolves the custom D60 projectile payload before vanilla projectile hit handling. */
@Mixin(class_1676.class)
public abstract class DomainProjectileHitMixin {
    @Inject(method = "method_7454", at = @At("HEAD"), cancellable = true)
    private void innerDimension$d60Payload(class_3966 hit, CallbackInfo ci) {
        class_1297 projectile = (class_1297) (Object) this;
        DomainProjectileState.State state = DomainProjectileState.peek(projectile);
        if (state == null || state.consumed || !"D60".equals(state.code)) return;
        class_1297 rawTarget = hit.method_17782();
        if (!(rawTarget instanceof class_1309 target)) return;
        if (state.target != null && !state.target.equals(target.method_5667())) return;
        DomainSession session = DomainSessionIndex.bySession(state.sessionId);
        if (session == null || session.ended()) return;
        state.consumed = true;
        if (DomainDamagePipeline.hurt(target, session.owner(), DomainDamageTypes.SPIT, 2.0f))
            EffectLeaseManager.apply(target, "minecraft:slowness", "D60", session.sessionId,
                    60, 0, DomainDamagePipeline.now());
        DomainProjectileState.forget(projectile);
        projectile.method_31472();
        ci.cancel();
    }
}
