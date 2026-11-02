package com.innerdimension.mixin;
import com.innerdimension.effect.ModStatusEffects;
import com.innerdimension.pregnancy.MentalPregnancySystem;
import com.nonid.effect.NonStatusEffects;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Route NoN effect consumers (including movement) to the single mental effect. */
@Mixin(class_1309.class)
public abstract class MentalPregnancyEffectMixin {
    @Inject(method="method_6112",at=@At("HEAD"),cancellable=true)
    private void inner$effect(class_6880<class_1291> effect,CallbackInfoReturnable<class_1293> cir){
        if(effect.comp_349()==NonStatusEffects.PREGNANT && (Object)this instanceof class_3222 player && MentalPregnancySystem.isMental(player))
            cir.setReturnValue(player.method_6112(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY)));
    }
    @Inject(method="method_6059",at=@At("HEAD"),cancellable=true)
    private void inner$hasEffect(class_6880<class_1291> effect,CallbackInfoReturnable<Boolean> cir){
        if(effect.comp_349()==NonStatusEffects.PREGNANT && (Object)this instanceof class_3222 player && MentalPregnancySystem.isMental(player))
            cir.setReturnValue(player.method_6059(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY)));
    }
    @Inject(method="method_6016",at=@At("HEAD"),cancellable=true)
    private void inner$removeEffect(class_6880<class_1291> effect,CallbackInfoReturnable<Boolean> cir){
        if(effect.comp_349()==NonStatusEffects.PREGNANT && (Object)this instanceof class_3222 player && MentalPregnancySystem.isMental(player))
            cir.setReturnValue(player.method_6016(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY)));
    }
    @ModifyVariable(method="method_6092(Lnet/minecraft/class_1293;)Z",at=@At("HEAD"),argsOnly=true)
    private class_1293 inner$singleEffect(class_1293 effect){
        if(effect.method_5579().comp_349()==NonStatusEffects.PREGNANT && (Object)this instanceof class_3222 player && MentalPregnancySystem.isMental(player))
            return new class_1293(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY),effect.method_5584(),effect.method_5578(),false,true,true);
        return effect;
    }
}
