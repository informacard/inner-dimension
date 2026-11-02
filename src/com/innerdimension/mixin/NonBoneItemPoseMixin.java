package com.innerdimension.mixin;

import com.innerdimension.client.NonBoneItemPose;
import com.nonid.internal.animation.client.render.gecko.layer.BoneItemGeoLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/** Fix the observer registered after Iris has already rendered this pass. */
@Mixin(value = BoneItemGeoLayer.class, remap = false)
public abstract class NonBoneItemPoseMixin {
    @Redirect(method = "submitRenderTask", at = @At(value = "INVOKE",
            target = "Lsoftware/bernie/geckolib/renderer/base/RenderPassInfo;addBoneUpdater(Lsoftware/bernie/geckolib/renderer/base/RenderPassInfo$BoneUpdater;)V"))
    private void inner$captureExistingPose(RenderPassInfo<GeoRenderState> pass,
                                           RenderPassInfo.BoneUpdater<GeoRenderState> observer) {
        NonBoneItemPose.capture(pass, observer);
    }
}
