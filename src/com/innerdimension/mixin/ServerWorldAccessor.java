package com.innerdimension.mixin;

import net.minecraft.class_3218;
import net.minecraft.class_5268;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_3218.class)
public interface ServerWorldAccessor {
    @Accessor("field_24456")
    class_5268 inner$getLevelData();
}
