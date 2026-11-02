package com.innerdimension.mixin;

import net.minecraft.class_1058;
import net.minecraft.class_775;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_775.class)
public interface ProjectionFluidRendererAccessor {
    @Accessor("field_64568") class_1058 inner$lavaStill();
    @Accessor("field_64569") class_1058 inner$lavaFlowing();
    @Accessor("field_64570") class_1058 inner$waterStill();
    @Accessor("field_64571") class_1058 inner$waterFlowing();
    @Accessor("field_4164") class_1058 inner$waterOverlay();
}
