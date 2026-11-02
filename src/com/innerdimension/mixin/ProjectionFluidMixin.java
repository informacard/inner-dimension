package com.innerdimension.mixin;

import com.innerdimension.config.InnerConfig;
import com.innerdimension.projection.ProjectionEnvironment;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3486;
import net.minecraft.class_3611;
import net.minecraft.class_6862;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Merges the current projected FluidState into vanilla's body and eye fluid refresh. */
@Mixin(class_1297.class)
public abstract class ProjectionFluidMixin {
    @Shadow protected Object2DoubleMap<class_6862<class_3611>> field_5964;
    @Shadow protected boolean field_6000;
    @Shadow protected boolean field_5957;
    @Shadow @Final private Set<class_6862<class_3611>> field_25599;

    @Inject(method = "method_67633", at = @At("HEAD"), cancellable = true)
    private void inner$preventLavaIgnition(CallbackInfo ci) {
        if (com.innerdimension.domain.ability.D52.protectedFromLava((class_1297)(Object)this)) ci.cancel();
    }

    @Inject(method = "method_5713", at = @At("TAIL"))
    private void inner$refreshProjectedWater(CallbackInfo ci) {
        class_1297 self = (class_1297)(Object)this;
        ProjectionEnvironment.FluidContact contact = ProjectionEnvironment.fluid(self);
        if (contact.kind() != ProjectionEnvironment.FluidKind.WATER || self.method_5765()) return;
        field_5957 = true;
        field_5964.put(class_3486.field_15517, Math.max(field_5964.getDouble(class_3486.field_15517), contact.depth()));
        self.field_6017 = 0;
        self.method_5646();
    }

    @Inject(method = "method_5876", at = @At("RETURN"), cancellable = true)
    private void inner$projectedFluidRefresh(CallbackInfoReturnable<Boolean> cir) {
        if(field_5957) cir.setReturnValue(true);
    }

    @Inject(method = "method_5692", at = @At("RETURN"), cancellable = true)
    private void inner$mergeProjectedFluid(class_6862<class_3611> tag, double push,
                                            CallbackInfoReturnable<Boolean> cir) {
        class_1297 self = (class_1297)(Object)this;
        boolean realFluid=cir.getReturnValue();
        ProjectionEnvironment.FluidContact contact = ProjectionEnvironment.fluid(self);
        boolean matches = contact.kind() == ProjectionEnvironment.FluidKind.WATER && tag == class_3486.field_15517
                || contact.kind() == ProjectionEnvironment.FluidKind.LAVA && tag == class_3486.field_15518;
        if (!matches) return;
        field_5964.put(tag, Math.max(field_5964.getDouble(tag), contact.depth()));
        class_243 flow=contact.flow();
        if (!realFluid && self.method_5675() && flow.method_1033()>0) {
            if (!(self instanceof net.minecraft.class_1657)) flow=flow.method_1029();
            flow=flow.method_1021(push);
            class_243 velocity=self.method_18798();
            if (Math.abs(velocity.field_1352)<.003 && Math.abs(velocity.field_1350)<.003
                    && flow.method_1033()<.0045000000000000005) flow=flow.method_1029().method_1021(.0045000000000000005);
            self.method_18799(velocity.method_1019(flow));
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "method_5630", at = @At("TAIL"))
    private void inner$mergeProjectedEyeFluid(CallbackInfo ci) {
        class_1297 self = (class_1297)(Object)this;
        if (ProjectionEnvironment.eyesIn(self, class_3486.field_15517)) {
            field_6000 = true;
            field_25599.add(class_3486.field_15517);
        }
        if (ProjectionEnvironment.eyesIn(self, class_3486.field_15518)) field_25599.add(class_3486.field_15518);
    }

    @Inject(method = "method_5790", at = @At("TAIL"))
    private void inner$startProjectedSwimming(CallbackInfo ci) {
        class_1297 self = (class_1297)(Object)this;
        if (!self.method_5681() && self.method_5624() && self.method_5869() && !self.method_5765()
                && ProjectionEnvironment.eyesIn(self, class_3486.field_15517)) self.method_5796(true);
    }

    @Inject(method = "method_5670", at = @At("TAIL"))
    private void inner$projectedLavaContact(CallbackInfo ci) {
        class_1297 self = (class_1297)(Object)this;
        if (self.method_73183().method_8608() || !InnerConfig.get().projectionLavaDamage) return;
        if (ProjectionEnvironment.fluid(self).kind() != ProjectionEnvironment.FluidKind.LAVA) return;
        self.method_67633();
        self.method_5730();
    }
}
