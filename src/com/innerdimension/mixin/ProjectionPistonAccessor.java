package com.innerdimension.mixin;
import net.minecraft.class_2669;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(class_2669.class)
public interface ProjectionPistonAccessor {
    @Accessor("field_12207") void inner$progress(float progress);
    @Accessor("field_12206") void inner$previousProgress(float progress);
}
