package com.innerdimension.mixin;

import net.minecraft.class_775;
import net.minecraft.class_776;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_776.class)
public interface ProjectionBlockRenderDispatcherAccessor {
    @Accessor("field_4167") class_775 inner$fluidRenderer();
}
