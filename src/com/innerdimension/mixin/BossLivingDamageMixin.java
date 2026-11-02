package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import com.innerdimension.boss.WitherDomainEffects;
@Mixin(class_1309.class)
public abstract class BossLivingDamageMixin {
    @Inject(method="method_64397",at=@At("HEAD"),cancellable=true)
    private void inner$bossProtection(class_3218 w,class_1282 source,float amount,CallbackInfoReturnable<Boolean> ci){if(!WitherDomainEffects.allowDamage((class_1309)(Object)this,source))ci.setReturnValue(false);}
    @Inject(method="method_64397",at=@At("RETURN"))
    private void inner$bossHit(class_3218 w,class_1282 source,float amount,CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValueZ()){WitherDomainEffects.hit((class_1309)(Object)this,source);com.innerdimension.boss.BossDomains.damaged((class_1309)(Object)this);}}
    @Inject(method="method_16080",at=@At("HEAD"),cancellable=true)
    private void inner$noBossSummonLoot(class_3218 w,class_1282 source,CallbackInfo ci){if(WitherDomainEffects.marked((class_1297)(Object)this))ci.cancel();}
}
