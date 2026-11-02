package com.innerdimension.mixin;
import com.innerdimension.pregnancy.MentalPregnancySystem;
import com.nonid.NeedsOfNature;
import com.nonid.api.NonPlayerApi;
import com.nonid.api.NonGender;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=NeedsOfNature.class,remap=false)
public abstract class MentalPregnancyGenderMixin {
    @Redirect(method={"tickPregnancyForPlayer","tryStartPendingPregnancyHatch","queuePendingPregnancyHatch","markPregnancyPending","beginPregnancy(Lnet/minecraft/class_3222;Lnet/minecraft/class_2960;JLcom/nonid/NeedsOfNature$PregnancyVariantData;I)Z"},at=@At(value="INVOKE",target="Lcom/nonid/NonGenderSystem;isOnlyMale(Lnet/minecraft/class_1297;)Z"))
    private static boolean inner$mentalAnyGender(class_1297 player){
        return !(player instanceof class_3222 server && MentalPregnancySystem.isMental(server)) && (player instanceof class_1657 entity && NonPlayerApi.getGender(entity)==NonGender.MALE);
    }
}
