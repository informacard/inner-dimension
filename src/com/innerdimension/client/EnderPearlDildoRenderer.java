package com.innerdimension.client;

import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.entity.EnderPearlDildoModel;
import net.minecraft.class_5617.class_5618;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EnderPearlDildoRenderer extends GeoEntityRenderer<EnderPearlDildoEntity, DildoRenderState> {
    public EnderPearlDildoRenderer(class_5618 context) {
        this(context, "entity/ender_pearl_dildo");
    }

    public EnderPearlDildoRenderer(class_5618 context, String modelPath) {
        super(context, new EnderPearlDildoModel(modelPath));
    }
}
