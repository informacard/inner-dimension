package com.innerdimension.mixin;

import com.innerdimension.projection.RoomProjectionManager;
import net.minecraft.class_2828;
import net.minecraft.class_3244;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep client movement from undoing the caster's landing during the reveal. */
@Mixin(class_3244.class)
public abstract class ProjectionPlayerMovementMixin {
    @Inject(method="method_12063",at=@At("HEAD"),cancellable=true)
    private void inner$holdCaster(class_2828 packet,CallbackInfo ci) {
        class_3244 handler=(class_3244)(Object)this;
        // Off-thread invocations must continue through vanilla's main-thread dispatch.
        if (handler.field_14140.method_51469().method_8503().method_18854()
                && (com.innerdimension.domain.ability.D77.locked(handler.field_14140)||RoomProjectionManager.holdMovement(handler.field_14140,packet))) ci.cancel();
    }
}
