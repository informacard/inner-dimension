package com.innerdimension.mixin;

import net.minecraft.class_1308;
import net.minecraft.class_1408;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1408.class)
public interface ProjectionNavigationAccessor {
    @Accessor("field_6684") class_1308 inner$getMob();
    @Accessor("field_6677") class_1937 inner$getWorld();
}
