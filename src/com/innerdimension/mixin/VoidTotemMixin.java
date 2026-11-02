package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1309.class)
public abstract class VoidTotemMixin {
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="method_6095",at=@At(value="INVOKE",target="Lnet/minecraft/class_1282;method_48789(Lnet/minecraft/class_6862;)Z"))
    private boolean inner$voidAllowsTotem(class_1282 source,class_6862<class_8110> tag,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original){
        return !com.innerdimension.voiddomain.VoidContamination.isDamage(source)&&original.call(source,tag);
    }
    @Inject(method="method_6095",at=@At("HEAD"),cancellable=true)
    private void inner$onlyTotem(class_1282 source,CallbackInfoReturnable<Boolean> ci){
        if(!com.innerdimension.voiddomain.VoidContamination.isDamage(source))return;
        var living=(class_1309)(Object)this;
        var totem=class_7923.field_41178.method_63535(class_2960.method_12829("minecraft:totem_of_undying"));
        boolean held=false;for(var hand:class_1268.values())if(living.method_5998(hand).method_7909()==totem)held=true;
        if(!held)ci.setReturnValue(false);
    }
    @Inject(method="method_6095",at=@At("RETURN"))
    private void inner$usedTotem(class_1282 source,CallbackInfoReturnable<Boolean> ci){
        if(com.innerdimension.voiddomain.VoidContamination.isDamage(source)&&ci.getReturnValue())com.innerdimension.voiddomain.VoidContamination.noteTotem();
    }
}
