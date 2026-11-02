package com.innerdimension.mixin;
import com.innerdimension.domain.ability.D48;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1309.class)
public abstract class DomainWindJumpMixin {
    @Inject(method = "method_6106()F", at = @At("RETURN"), cancellable = true)
    private void innerDimension$villagerJump(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Float> cir) {
        if(!((Object)this instanceof net.minecraft.class_1657 player))return;
        var jump=player.method_5996(net.minecraft.class_5134.field_23728);
        if(jump==null||!jump.method_6196(com.innerdimension.domain.runtime.AttributeLeaseManager.id("villager_jump")))return;
        var gravity=net.minecraft.class_7923.field_41190.method_10223(net.minecraft.class_2960.method_12829("minecraft:gravity")).orElseThrow();
        cir.setReturnValue(com.innerdimension.progression.VillagerJumpRules.quarter(cir.getReturnValue(),player.method_45325(gravity)));
    }
    @Inject(method = "method_6043", at = @At("RETURN"))
    private void innerDimension$windJump(CallbackInfo ci) {
        D48.afterJump((class_1309) (Object) this);
    }
}
