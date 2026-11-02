package com.innerdimension.client;

import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

/** NoN's item callback only observes the pose; it does not update any bones. */
public final class NonBoneItemPose {
    private NonBoneItemPose() {}

    public static <R extends GeoRenderState> void capture(RenderPassInfo<R> pass,
                                                         RenderPassInfo.BoneUpdater<R> observer) {
        // Iris can execute the model task before layers are submitted. Registering
        // another updater then is too late. Read the cached pose instead, while it
        // is installed, and let GeckoLib restore the bones when this scope ends.
        pass.renderPosed(() -> observer.run(pass, name -> pass.model().getBone(name)
                .map(bone -> bone.frameSnapshot != null ? bone.frameSnapshot : BoneSnapshot.create(bone))));
    }
}
