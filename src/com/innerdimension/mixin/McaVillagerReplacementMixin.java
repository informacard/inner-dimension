package com.innerdimension.mixin;

import com.innerdimension.progression.VillagerSoulChallenge;
import net.minecraft.class_1314;
import net.minecraft.class_1646;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional MCA 7.7.36 compatibility, without linking MCA classes when it is absent. */
@Pseudo
@Mixin(targets="net.conczin.mca.server.SpawnQueue",remap=false)
public abstract class McaVillagerReplacementMixin {
    @Inject(method="copyPastaIntensifies(Lnet/minecraft/class_1314;Lnet/minecraft/class_1314;)V",
            at=@At("TAIL"),remap=false,require=1)
    private void inner$transferRitual(class_1314 replacement,class_1314 previous,CallbackInfo ci){
        if(previous instanceof class_1646 oldVillager&&replacement instanceof class_1646 newVillager)
            VillagerSoulChallenge.replaced(oldVillager,newVillager);
    }
}
