package com.innerdimension.mixin;

import com.innerdimension.domain.world.DomainTeleportGuard;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2709;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * D62 spatial disturbance. Verified intermediary target:
 * {@code class_1297.method_48105(class_3218,double,double,double,Set,float,float,boolean)}
 * — the single authoritative teleport entry point (ender pearls, chorus fruit, commands
 * and this mod's own relocation all funnel through it).
 *
 * <p>{@code class_1297.method_5814(double,double,double)} (refreshPositionAndAngles) is
 * deliberately <em>not</em> intercepted: vanilla physics calls it for ordinary per-tick
 * movement, so cancelling it would freeze every entity. The blueprint classifies such raw
 * third-party position writes as explicitly out of scope.</p>
 *
 * <p>An ender pearl is consumed by the thrower before the teleport resolves, so refusing
 * the teleport never refunds it.</p>
 */
@Mixin(class_1297.class)
public abstract class DomainTeleportMixin {
    @Inject(method = "method_48105", at = @At("HEAD"), cancellable = true)
    private void innerDimension$d62Teleport(class_3218 world, double x, double y, double z,
                                            Set<class_2709> relative, float yaw, float pitch,
                                            boolean resetCamera, CallbackInfoReturnable<Boolean> cir) {
        if (DomainTeleportGuard.blocked((class_1297) (Object) this, world, x, y, z)) cir.setReturnValue(false);
    }
}
