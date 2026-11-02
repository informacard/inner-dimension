package com.innerdimension.mixin;
import net.minecraft.*;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
@Mixin(class_1309.class) public abstract class DomainExtendedEffectMixin {
 @ModifyVariable(method="method_37222",at=@At("HEAD"),argsOnly=true) private class_1293 inner$extend(class_1293 effect){return com.innerdimension.domain.ability.D34.extend((class_1309)(Object)this,effect);}
}
