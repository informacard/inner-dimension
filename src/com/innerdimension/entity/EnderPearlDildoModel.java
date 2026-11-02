package com.innerdimension.entity;

import com.innerdimension.InnerDimension;
import net.minecraft.class_2960;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class EnderPearlDildoModel extends GeoModel<EnderPearlDildoEntity> {
    private final class_2960 modelId;

    public EnderPearlDildoModel(String path) {
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
    public class_2960 getAnimationResource(EnderPearlDildoEntity animatable) {
        return InnerDimension.id("geckolib/animations/ender_pearl_dildo.animation.json");
    }
}
