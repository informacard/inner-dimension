package com.innerdimension.mixin;

import com.innerdimension.transport.SafeEntityTeleport;
import net.minecraft.class_3222;
import net.minecraft.class_5454;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3222.class)
public abstract class PlayerAnimationTeleportMixin {
    @Unique private int inner$diagnosedTeleports;

    @WrapMethod(method = "method_61275")
    private class_3222 inner$timeTeleport(class_5454 transition, Operation<class_3222> original) {
        class_3222 player = (class_3222) (Object) this;
        var from = player.method_51469().method_27983();
        var to = transition.comp_2820().method_27983();
        boolean measure = inner$diagnosedTeleports < 4 && !from.equals(to)
                && (com.innerdimension.dimension.DynamicDimensionManager.isInnerWorld(from)
                || com.innerdimension.dimension.DynamicDimensionManager.isInnerWorld(to));
        if (!measure) return original.call(transition);
        int sequence = ++inner$diagnosedTeleports;
        long start = System.nanoTime();
        try { return original.call(transition); }
        finally {
            org.slf4j.LoggerFactory.getLogger("inner_dimension.teleport.diagnostics").info(
                    "[InnerTeleportDiag] SERVER_MOVE seq={} player={} from={} to={} elapsed_ms={} wall_ms={}",
                    sequence, player.method_5667(), from.method_29177(), to.method_29177(),
                    Math.round((System.nanoTime() - start) / 100_000.0) / 10.0, System.currentTimeMillis());
        }
    }

    // ServerPlayer overrides Entity's transition and short-distance teleport methods.
    @Inject(method = "method_61275", at = @At("HEAD"), cancellable = true)
    private void inner$stopBeforeTeleport(class_5454 transition, CallbackInfoReturnable<class_3222> callback) {
        if (!SafeEntityTeleport.prepare((class_3222) (Object) this)) callback.setReturnValue(null);
    }

    @Inject(method = "method_5859", at = @At("HEAD"), cancellable = true)
    private void inner$stopBeforeRequestTeleport(double x, double y, double z, CallbackInfo callback) {
        if (!SafeEntityTeleport.prepare((class_3222) (Object) this)) callback.cancel();
    }
}
