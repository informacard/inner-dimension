package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.class_1308;
import net.minecraft.class_1409;
import net.minecraft.class_1408;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Invalidates only ground-navigation paths whose nearby projection set changed. */
@Mixin(class_1408.class)
public abstract class ProjectionMobNavigationMixin {
    /** The move target must use the same projected floor as the path evaluator. */
    @Inject(method="method_38065",at=@At("RETURN"),cancellable=true)
    private void inner$projectedMoveHeight(class_243 point,CallbackInfoReturnable<Double> cir) {
        ProjectionNavigationAccessor access=(ProjectionNavigationAccessor)this;
        class_1308 mob=access.inner$getMob();
        double half=mob.method_17681()*.5;
        class_238 footprint=new class_238(point.field_1352-half,point.field_1351-1.126,
            point.field_1350-half,point.field_1352+half,point.field_1351+.001,point.field_1350+half);
        double support=ProjectionEnvironment.supportHeight(access.inner$getWorld(),footprint,
            point.field_1351-1.126,point.field_1351+.001);
        if(!Double.isNaN(support))cir.setReturnValue(Math.max(cir.getReturnValue(),support));
    }

    @Unique private long inner$projectionSignature = Long.MIN_VALUE;

    @Inject(method = "method_6360", at = @At("HEAD"))
    private void inner$refreshProjectionPath(CallbackInfo ci) {
        if (!((Object)this instanceof class_1409)) return;
        ProjectionNavigationAccessor access = (ProjectionNavigationAccessor)this;
        class_1308 mob = access.inner$getMob();
        class_238 region = mob.method_5829().method_1014(48);
        long signature = ProjectionEnvironment.signature(access.inner$getWorld(), region);
        if (inner$projectionSignature != Long.MIN_VALUE && signature != inner$projectionSignature) {
            class_1408 navigation = (class_1408)(Object)this;
            boolean active = !navigation.method_6357();
            // method_61441 only changes the search budget; it leaves the old path intact.
            // Stop first so the throttled recalculation cannot keep following stale nodes.
            navigation.method_6340();
            if (active) {
                mob.method_5962().method_6239(mob.method_23317(), mob.method_23318(), mob.method_23321(), 0);
                mob.method_5930(0);
                mob.method_6100(false);
                navigation.method_6356();
            }
        }
        inner$projectionSignature = signature;
    }
}
