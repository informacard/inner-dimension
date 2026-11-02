package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.class_7;
import net.minecraft.class_14;
import net.minecraft.class_1308;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds projection geometry only to vanilla's ordinary ground-node evaluator. */
@Mixin(class_14.class)
public abstract class ProjectionLandPathNodeMakerMixin {
    @Inject(method = "method_31932", at = @At("RETURN"), cancellable = true)
    private void inner$projectedNodeType(int x, int y, int z, CallbackInfoReturnable<class_7> cir) {
        class_1308 mob = ((ProjectionNodeMakerAccessor)this).inner$getMob();
        switch (ProjectionEnvironment.cell(mob, x, y, z)) {
            case BLOCKED -> cir.setReturnValue(class_7.field_22);
            case WALKABLE -> { if (cir.getReturnValue() == class_7.field_7) cir.setReturnValue(class_7.field_12); }
            case WATER -> { if (cir.getReturnValue() == class_7.field_7) cir.setReturnValue(class_7.field_18); }
            case LAVA -> { if (cir.getReturnValue() == class_7.field_7) cir.setReturnValue(class_7.field_14); }
            default -> { }
        }
    }

    @Inject(method = "method_37003", at = @At("RETURN"), cancellable = true)
    private void inner$exactProjectedSupport(class_2338 pos, CallbackInfoReturnable<Double> cir) {
        class_1308 mob = ((ProjectionNodeMakerAccessor)this).inner$getMob();
        double half = mob.method_17681() * 0.5;
        class_238 footprint = new class_238(pos.method_10263() + 0.5 - half, pos.method_10264() - 1.126,
                pos.method_10260() + 0.5 - half, pos.method_10263() + 0.5 + half,
                pos.method_10264() + 0.001, pos.method_10260() + 0.5 + half);
        double projected = ProjectionEnvironment.supportHeight(mob.method_73183(), footprint,
                pos.method_10264() - 1.126, pos.method_10264() + 0.001);
        if (!Double.isNaN(projected)) cir.setReturnValue(Math.max(cir.getReturnValue(), projected));
    }

    @Inject(method = "method_29304", at = @At("HEAD"), cancellable = true)
    private void inner$projectedSweep(class_238 box, CallbackInfoReturnable<Boolean> cir) {
        class_1308 mob = ((ProjectionNodeMakerAccessor)this).inner$getMob();
        if (ProjectionEnvironment.projectedCollision(mob.method_73183(), box)) cir.setReturnValue(true);
    }
}
