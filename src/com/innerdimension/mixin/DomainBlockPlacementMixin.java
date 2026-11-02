package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1747.class)
public abstract class DomainBlockPlacementMixin {
    @Inject(method="method_7712",at=@At("HEAD"),cancellable=true)
    private void inner$domainPlacement(class_1750 context,CallbackInfoReturnable<class_1269> ci){
        var player=context.method_8036();
        if(com.innerdimension.domain.ability.D64.locked(context.method_8045(),context.method_8037())){ci.setReturnValue(class_1269.field_5814);return;}
        if(player!=null&&(com.innerdimension.domain.SimpleDomain.active(player)||com.innerdimension.progression.VillagerSoulChallenge.blocksPlacement(player)))ci.setReturnValue(class_1269.field_5814);
    }
}
