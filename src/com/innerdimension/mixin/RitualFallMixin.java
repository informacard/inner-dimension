package com.innerdimension.mixin;
import com.innerdimension.progression.DomainRitual;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1309.class)
public abstract class RitualFallMixin {
    @Inject(method="method_5747", at=@At("HEAD"), cancellable=true)
    private void inner$ritual(double distance, float multiplier, class_1282 source, CallbackInfoReturnable<Boolean> ci) {
        if ((Object)this instanceof class_3222 p && com.innerdimension.voiddomain.VoidEmbodiment.landing(p,distance)) {ci.setReturnValue(false);return;}
        if ((Object)this instanceof class_3222 player && DomainRitual.landing(player, distance, "FALL_DAMAGE")) ci.setReturnValue(false);
        else if ((Object)this instanceof class_1646 villager && com.innerdimension.progression.VillagerSoulChallenge.landing(villager,distance)) ci.setReturnValue(false);
    }
}
