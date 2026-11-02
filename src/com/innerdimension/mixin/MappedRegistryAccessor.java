package com.innerdimension.mixin;

import net.minecraft.class_2370;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_2370.class)
public interface MappedRegistryAccessor {
    @Mutable
    @Accessor("field_36463")
    void inner$setFrozen(boolean frozen);

    @Accessor("field_36463")
    boolean inner$isFrozen();
}
