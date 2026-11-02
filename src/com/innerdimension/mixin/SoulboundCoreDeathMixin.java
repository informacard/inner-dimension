package com.innerdimension.mixin;
import com.innerdimension.item.SoulboundCores;
import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1657.class)
public abstract class SoulboundCoreDeathMixin {
 @Unique private java.util.List<SoulboundCores.Saved> inner$soulbound=java.util.List.of();
 @Inject(method="method_16078",at=@At("HEAD"))
 private void inner$protectCore(class_3218 world,CallbackInfo ci){if((Object)this instanceof class_3222 p)inner$soulbound=SoulboundCores.take(p.method_31548());}
 @Inject(method="method_16078",at=@At("RETURN"))
 private void inner$restoreCore(class_3218 world,CallbackInfo ci){if((Object)this instanceof class_3222 p){SoulboundCores.restore(p.method_31548(),inner$soulbound);inner$soulbound=java.util.List.of();}}
}
