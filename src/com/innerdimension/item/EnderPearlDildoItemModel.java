package com.innerdimension.item;

import com.innerdimension.InnerDimension;
import net.minecraft.class_2960;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/** GeckoLib model used when the dildo is held / shown in the inventory. */
public class EnderPearlDildoItemModel extends GeoModel<EnderPearlDildoItem> {
    private final class_2960 modelId;

    public EnderPearlDildoItemModel(String path) {
        this.modelId = InnerDimension.id(path);
    }

    @Override
    public class_2960 getModelResource(GeoRenderState renderState) {
        return this.modelId;
    }

    @Override
    public class_2960 getTextureResource(GeoRenderState renderState) {
        return InnerDimension.id("textures/entity/ender_pearl_dildo.png");
    }

    @Override
    public class_2960 getAnimationResource(EnderPearlDildoItem animatable) {
        return InnerDimension.id("geckolib/animations/ender_pearl_dildo.animation.json");
    }
}
