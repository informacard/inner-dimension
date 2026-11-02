package com.innerdimension.mixin;
import net.minecraft.class_2627;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(class_2627.class)
public interface ProjectionShulkerAccessor {
    @Accessor("field_12056") void inner$progress(float value);
    @Accessor("field_12055") void inner$previousProgress(float value);
}
