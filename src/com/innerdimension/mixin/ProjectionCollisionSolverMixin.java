package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionCollisionSeed;
import com.innerdimension.projection.ProjectionEnvironment;
import com.innerdimension.projection.ProjectionMovementDiagnostics;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shared static solver, including callers that cancel Entity's outer movement method.
 * Apply after Lithium's priority-1000 overwrite so direct calls to its replacement receive
 * projection shapes too. Its redirected normal path retains ProjectionEntityMoveMixin's seed.
 */
@Mixin(value = class_1297.class, priority = 900)
public abstract class ProjectionCollisionSolverMixin {
    @ModifyVariable(method = "method_20736", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static List<class_265> inner$solverShapes(List<class_265> original,
            class_1297 entity, class_243 movement, class_238 body, class_1937 world,
            List<class_265> entityShapes) {
        class_238 swept = body.method_18804(movement);
        List<class_265> projected = new ArrayList<>(ProjectionEnvironment.collisionShapes(world, swept));
        if (entity != null) projected.addAll(ProjectionEnvironment.lavaSurfaceShapes(entity, swept));
        ProjectionMovementDiagnostics.solverQuery(entity, world, body, movement, projected.size());
        return ProjectionCollisionSeed.refresh(original, projected);
    }

    @Inject(method = "method_20736", at = @At("RETURN"))
    private static void inner$solverResult(class_1297 entity, class_243 movement, class_238 body,
            class_1937 world, List<class_265> entityShapes, CallbackInfoReturnable<class_243> cir) {
        ProjectionMovementDiagnostics.solverResult(entity, movement, cir.getReturnValue());
    }
}
