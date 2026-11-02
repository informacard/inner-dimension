package com.innerdimension.mixin;
import net.minecraft.class_2611;
import net.minecraft.class_5561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(class_2611.class)
public interface ProjectionEnderChestAccessor {
    @Accessor("field_27217") class_5561 inner$openers();
}
