package com.innerdimension.mixin;
import com.nonid.NeedsOfNature;
import com.innerdimension.pregnancy.MentalPregnancySystem;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=NeedsOfNature.class,remap=false)
public class MentalPregnancyCompletionMixin {
    @Inject(method="tickPregnancyForPlayer",at=@At("HEAD"),cancellable=true)
    private static void inner$complete(class_3218 world,class_3222 player,long now,CallbackInfo ci){if(player!=null && MentalPregnancySystem.interceptTick(player,now))ci.cancel();}
}
