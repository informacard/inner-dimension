package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1510.class)
public abstract class BossDragonCrystalMixin {
    @org.spongepowered.asm.mixin.Unique private float inner$healthBefore;
    @org.spongepowered.asm.mixin.Unique private long inner$lastBlockedLog=-100;
    @Inject(method="method_6816",at=@At("HEAD"))
    private void inner$meleeStart(class_3218 world,class_1508 part,class_1282 source,float amount,CallbackInfoReturnable<Boolean> ci){inner$healthBefore=((class_1510)(Object)this).method_6032();}
    @Inject(method="method_6816",at=@At("RETURN"))
    private void inner$meleeResult(class_3218 world,class_1508 part,class_1282 source,float amount,CallbackInfoReturnable<Boolean> ci){
        var dragon=(class_1510)(Object)this;
        if(amount<=0||source==null||!(source.method_5529() instanceof class_1657)||source.method_5526()!=source.method_5529()||dragon.method_6032()<inner$healthBefore)return;
        long now=com.innerdimension.core.ServerClock.now();if(now-inner$lastBlockedLog<40)return;inner$lastBlockedLog=now;
        org.slf4j.LoggerFactory.getLogger("inner_dimension.boss").warn("Dragon melee caused no health loss: dragon={} part={} partId={} player={} amount={} accepted={} phase={} health={} regen={} locked={} sessions={}",dragon.method_5667(),part.field_7006,part.method_5628(),source.method_5529().method_5667(),amount,ci.getReturnValueZ(),dragon.method_6831().method_6864().method_6849().method_6871(),dragon.method_6032(),dragon.field_6008,com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(dragon),com.innerdimension.domain.runtime.DomainSessionIndex.covering(dragon));
    }

    @Inject(method="method_6828",at=@At("HEAD"))
    private void inner$crystal(class_3218 world,class_1511 crystal,class_2338 pos,class_1282 source,CallbackInfo ci){com.innerdimension.boss.BossDomains.crystal((class_1510)(Object)this,crystal,source);}
}
