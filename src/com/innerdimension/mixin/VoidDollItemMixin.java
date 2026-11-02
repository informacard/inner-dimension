package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1542.class)
public abstract class VoidDollItemMixin {
    @Inject(method="method_5773",at=@At("HEAD"))
    private void inner$fallingDoll(CallbackInfo ci){
        var item=(class_1542)(Object)this;
        if(item.method_6983().method_7909()!=nonplayerplushie.SensoryLinkItems.SENSORY_LINK_DOLL)return;
        var data=nonplayerplushie.SensoryLinkData.read(item.method_6983());
        if(nonplayerplushie.SensoryLinkData.valid(data))com.innerdimension.voiddomain.VoidContamination.dollFalling(item,nonplayerplushie.SensoryLinkData.ownerUuid(data));
    }
}
