package com.innerdimension.mixin;

import net.minecraft.class_8;
import net.minecraft.class_1308;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_8.class)
public interface ProjectionNodeMakerAccessor {
    @Accessor("field_33") class_1308 inner$getMob();
}
