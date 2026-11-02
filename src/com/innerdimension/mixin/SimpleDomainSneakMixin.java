package com.innerdimension.mixin;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(class_1297.class)
public abstract class SimpleDomainSneakMixin {
    @ModifyVariable(method="method_5660",at=@At("HEAD"),argsOnly=true)
    private boolean inner$simpleSneak(boolean value){return value||com.innerdimension.domain.SimpleDomain.active((class_1297)(Object)this);}
}
