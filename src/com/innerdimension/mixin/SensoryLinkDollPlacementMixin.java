package com.innerdimension.mixin;

import com.innerdimension.binding.DollBindingManager;
import net.minecraft.*;
import nonplayerplushie.SensoryLinkDollBlock;
import nonplayerplushie.SensoryLinkDollBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Retain the server-side placer before the periodic binding sweep sees this block. */
@Mixin(value = SensoryLinkDollBlock.class, remap = false)
public abstract class SensoryLinkDollPlacementMixin {
    @Inject(method = "method_9567", at = @At("TAIL"))
    private void inner$recordPlacer(class_1937 world, class_2338 pos, class_2680 state,
                                   class_1309 placer, class_1799 stack, CallbackInfo ci) {
        if (!(world instanceof class_3218) || !(placer instanceof class_3222 player)) return;
        if (!(world.method_8321(pos) instanceof SensoryLinkDollBlockEntity doll)) return;
        class_2487 data = doll.data();
        if (data == null) return;
        data.method_10582(DollBindingManager.BLOCK_PLACER_KEY, player.method_5667().toString());
        doll.setData(data);
    }
}
