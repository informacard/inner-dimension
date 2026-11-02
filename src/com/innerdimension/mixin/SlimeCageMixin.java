package com.innerdimension.mixin;
import com.innerdimension.domain.ability.D77;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(class_1621.class)
public abstract class SlimeCageMixin {
    @Inject(method="method_7155",at=@At("HEAD"),cancellable=true)
    private void inner$noContactDamage(class_1309 target,CallbackInfo ci){if(D77.shell((class_1297)(Object)this))ci.cancel();}
    @Inject(method="method_5697",at=@At("HEAD"),cancellable=true)
    private void inner$noPush(class_1297 target,CallbackInfo ci){if(D77.shell((class_1297)(Object)this)||D77.shell(target))ci.cancel();}
}
