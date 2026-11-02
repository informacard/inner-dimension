package com.innerdimension.mixin;
import com.innerdimension.progression.InnerAdvancements;
import net.minecraft.*;
import nonplayerplushie.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1734.class)
public abstract class DollCraftAdvancementMixin {
    @Inject(method="method_7667", at=@At("TAIL"))
    private void inner$crafted(class_1657 player, class_1799 output, CallbackInfo ci) {
        if (player instanceof class_3222 p && output.method_7909() == SensoryLinkItems.SENSORY_LINK_DOLL
                && p.method_5667().equals(SensoryLinkData.ownerUuid(SensoryLinkData.read(output))))
            InnerAdvancements.grant(p, "in_control");
    }
}
