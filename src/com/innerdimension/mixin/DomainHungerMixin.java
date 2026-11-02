package com.innerdimension.mixin;

import com.innerdimension.domain.world.DomainFoodGuard;
import net.minecraft.class_1702;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * D17 mule endurance, storage half. Verified intermediary targets on
 * {@code class_1702} (HungerManager): {@code method_7588(class_3222)} (update),
 * {@code method_7586()} (getFoodLevel), {@code method_7589()} (getSaturationLevel),
 * {@code method_7580(int)} (setFoodLevel) and {@code method_7581(float)}
 * (setSaturationLevel) — all confirmed against the intermediary jar bytecode.
 *
 * <p>The guard is deliberately narrow: it snapshots at the head of the tick and only
 * writes back when a decrease actually happened, so eating, saturation-rich food, natural
 * regeneration and every positive food change pass through unchanged. It never rewrites
 * the values on a tick where nothing was lost.</p>
 */
@Mixin(class_1702.class)
public abstract class DomainHungerMixin {
    @Unique private int inner$d17Food;
    @Unique private float inner$d17Saturation;
    @Unique private boolean inner$d17Guarded;

    @Inject(method = "method_7588", at = @At("HEAD"))
    private void innerDimension$d17Snapshot(class_3222 player, CallbackInfo ci) {
        inner$d17Guarded = DomainFoodGuard.protectedFromHunger(player);
        if (!inner$d17Guarded) return;
        class_1702 self = (class_1702) (Object) this;
        inner$d17Food = self.method_7586();
        inner$d17Saturation = self.method_7589();
    }

    @Inject(method = "method_7588", at = @At("RETURN"))
    private void innerDimension$d17Restore(class_3222 player, CallbackInfo ci) {
        if (!inner$d17Guarded) return;
        inner$d17Guarded = false;
        class_1702 self = (class_1702) (Object) this;
        if (self.method_7586() < inner$d17Food) self.method_7580(inner$d17Food);
        if (self.method_7589() < inner$d17Saturation) self.method_7581(inner$d17Saturation);
    }
}
