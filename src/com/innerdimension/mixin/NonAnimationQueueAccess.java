package com.innerdimension.mixin;

import java.util.UUID;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Uses the same actor-wide queue cleanup as NoN's own world-change handler. */
@Mixin(targets = "com.nonid.internal.animation.server.ServerAnimationController", remap = false)
public interface NonAnimationQueueAccess {
    @Invoker(value = "clearQueuesContainingActor", remap = false)
    static void inner$clearQueuesContainingActor(class_3218 world, UUID actor) {
        throw new AssertionError("Mixin invoker not applied");
    }
}
