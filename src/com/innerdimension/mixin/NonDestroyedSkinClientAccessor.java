package com.innerdimension.mixin;
import com.nonid.client.NonDestroyedSkinClient;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=NonDestroyedSkinClient.class,remap=false)
public interface NonDestroyedSkinClientAccessor {
    @Invoker("clearCachedTextures") static void inner$clearCachedTextures(UUID id){ throw new AssertionError(); }
}
