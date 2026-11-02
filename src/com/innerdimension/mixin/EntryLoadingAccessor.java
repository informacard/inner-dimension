package com.innerdimension.mixin;

import net.minecraft.class_634;
import net.minecraft.class_11653;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_634.class)
public interface EntryLoadingAccessor {
    @Accessor("field_61723") class_11653 inner$loadingTracker();
}
