package com.innerdimension.mixin;
import java.util.UUID;import net.minecraft.class_1542;import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(class_1542.class) public interface DomainItemOwnerAccessor {
 @Accessor("field_41893") UUID inner$pickupOwner();
}
