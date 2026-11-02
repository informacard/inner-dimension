package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionEnvironment;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1313;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds projection shapes to vanilla's one movement solve. This deliberately feeds the same list
 * used by vertical clipping, horizontal clipping and step-up, preventing Create's OBB response
 * from independently marking a wall-side contact as ground.
 */
@Mixin(class_1297.class)
public abstract class ProjectionEntityMoveMixin {
    // Lithium redirects the initial solve away from method_59920, but consumes this list.
    @ModifyVariable(method = "method_17835", at = @At("STORE"), ordinal = 0)
    private List<class_265> inner$lithiumShapes(List<class_265> vanilla, class_243 movement) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("lithium")) return vanilla;
        class_1297 entity = (class_1297)(Object)this;
        class_238 box = entity.method_5829().method_18804(movement);
        List<class_265> projected = new ArrayList<>(ProjectionEnvironment.collisionShapes(entity.method_73183(), box));
        projected.addAll(ProjectionEnvironment.lavaSurfaceShapes(entity, box));
        com.innerdimension.projection.ProjectionMovementDiagnostics.query(entity, box, projected.size());
        return projected.isEmpty() ? vanilla : new com.innerdimension.projection.ProjectionCollisionSeed(vanilla, projected);
    }
    @Inject(method = "method_59920", at = @At("RETURN"), cancellable = true)
    private static void inner$projectionCollisionShapes(class_1297 entity, class_1937 world,
            List<class_265> entityShapes, class_238 sweptBox,
            CallbackInfoReturnable<List<class_265>> cir) {
        List<class_265> projected = new ArrayList<>(ProjectionEnvironment.collisionShapes(world, sweptBox));
        projected.addAll(ProjectionEnvironment.lavaSurfaceShapes(entity, sweptBox));
        com.innerdimension.projection.ProjectionMovementDiagnostics.query(entity, sweptBox, projected.size());
        if (projected.isEmpty() && !(entityShapes instanceof com.innerdimension.projection.ProjectionCollisionSeed)) return;
        List<class_265> vanilla = cir.getReturnValue();
        List<class_265> merged = new ArrayList<>(vanilla.size() + projected.size());
        merged.addAll(vanilla);
        if (entityShapes instanceof com.innerdimension.projection.ProjectionCollisionSeed seed)
            merged.removeAll(seed.projected());
        merged.addAll(projected);
        cir.setReturnValue(merged);
    }

    @Inject(method = "method_17835", at = @At("HEAD"))
    private void inner$beginSolve(class_243 movement, CallbackInfoReturnable<class_243> cir) {
        com.innerdimension.projection.ProjectionMovementDiagnostics.begin((class_1297)(Object)this, movement);
    }

    @Inject(method = "method_17835", at = @At("RETURN"))
    private void inner$endSolve(class_243 movement, CallbackInfoReturnable<class_243> cir) {
        com.innerdimension.projection.ProjectionMovementDiagnostics.end((class_1297)(Object)this, movement, cir.getReturnValue());
    }

    @Inject(method = "method_5784", at = @At("RETURN"))
    private void inner$afterMove(class_1313 type, class_243 movement, CallbackInfo ci) {
        com.innerdimension.projection.ProjectionMovementDiagnostics.after((class_1297)(Object)this);
    }
}
