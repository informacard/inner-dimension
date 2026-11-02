package com.innerdimension.mixin;
import com.innerdimension.item.ModItems;
import com.innerdimension.progression.InnerAdvancements;
import net.minecraft.*;
import nonplayerplushie.SensoryLinkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(class_1714.class)
public abstract class DomainCoreCraftGateMixin {
    @Redirect(method="method_17399",at=@At(value="INVOKE",target="Lnet/minecraft/class_3955;method_8116(Lnet/minecraft/class_9695;Lnet/minecraft/class_7225$class_7874;)Lnet/minecraft/class_1799;"))
    private static class_1799 inner$gate(class_3955 recipe,class_9695 input,class_7225.class_7874 lookup,
        class_1703 handler,class_3218 world,class_1657 player,class_8566 inventory,class_1731 result,class_8786<class_3955> entry) {
        class_1799 out=recipe.method_8116((class_9694)input,lookup);
        if(out.method_7909()==ModItems.DOMAIN_CORE && (!(player instanceof class_3222 p)
            || !InnerAdvancements.done(p,"cursed_core")
            || !player.method_5667().equals(SensoryLinkData.ownerUuid(SensoryLinkData.read(out))))) return class_1799.field_8037;
        return out;
    }
}
