package com.innerdimension.mixin;
import com.innerdimension.domain.ability.D77;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(class_1297.class)
public abstract class SlimeCageEntityMixin {
    @Inject(method="method_5784",at=@At("HEAD"),cancellable=true)
    private void inner$slimeLock(class_1313 type,class_243 movement,CallbackInfo ci){
        if(D77.locked((class_1297)(Object)this))ci.cancel();
    }
    @Inject(method="method_5697",at=@At("HEAD"),cancellable=true)
    private void inner$harmlessShell(class_1297 other,CallbackInfo ci){
        if(D77.shell((class_1297)(Object)this)||D77.shell(other))ci.cancel();
    }
}
