package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1309.class)
public abstract class VoidEmbodimentEffectMixin {
 @Inject(method="method_6111",at=@At("HEAD"),cancellable=true)
 private void protectDirect(class_6880<class_1291> effect,CallbackInfoReturnable<class_1293> ci){if(effect.comp_349()==com.innerdimension.effect.ModStatusEffects.VOID_EMBODIMENT&&(Object)this instanceof class_3222 p&&com.innerdimension.voiddomain.VoidEmbodiment.active(p))ci.setReturnValue(null);}
 @Inject(method="method_6012",at=@At("RETURN"))
 private void restoreAfterClear(CallbackInfoReturnable<Boolean> ci){if((Object)this instanceof class_3222 p&&p.method_5805()&&com.innerdimension.voiddomain.VoidEmbodiment.active(p))p.method_6092(new class_1293(com.innerdimension.voiddomain.VoidEmbodiment.effect(),-1,0,false,true,true));}
 @Inject(method="method_6016",at=@At("HEAD"),cancellable=true)
 private void protect(class_6880<class_1291> effect,CallbackInfoReturnable<Boolean> ci){if(effect.comp_349()==com.innerdimension.effect.ModStatusEffects.VOID_EMBODIMENT&&(Object)this instanceof class_3222 p&&com.innerdimension.voiddomain.VoidEmbodiment.active(p))ci.setReturnValue(false);}
}
