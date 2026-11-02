package com.innerdimension.mixin;

import com.innerdimension.animation.DildoEntryAnimation;
import com.innerdimension.animation.AnimationStopContext;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import java.util.List;
import java.util.ArrayDeque;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nonid.internal.animation.server.ServerAnimationController", remap = false)
public abstract class NonAnimationControllerMixin {
    private static final String ADVANCE = "maybeAdvanceStages(Lnet/minecraft/class_3218;Ljava/util/UUID;Lcom/nonid/internal/animation/server/ServerAnimationController$ActiveInstance;JLjava/util/List;Ljava/util/List;)Z";
    private static final String STOP = "stopInstance(Lnet/minecraft/class_3218;Ljava/util/UUID;ZLcom/nonid/internal/animation/server/ServerAnimationController$StopReason;Ljava/util/Set;)Z";
    // Inject supports coercion; a stack preserves context across nested controller calls.
    @Unique private static final ThreadLocal<ArrayDeque<AnimationStopContext>> inner$frames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = ADVANCE, at = @At("HEAD"), remap = false)
    private static void inner$beginAdvance(class_3218 world, UUID instance, @Coerce Object active,
                                           long now, List<?> advances, List<UUID> removed,
                                           CallbackInfoReturnable<Boolean> callback) {
        inner$frames.get().push(new AnimationStopContext(world, instance, true));
    }

    @Inject(method = STOP, at = @At("HEAD"), remap = false)
    private static void inner$beginStop(class_3218 world, UUID instance, boolean restore,
                                        @Coerce Object reason, Set<UUID> skipped,
                                        CallbackInfoReturnable<Boolean> callback) {
        if (reason instanceof Enum<?> value && value.name().equals("NATURAL_END"))
            DildoEntryAnimation.beforeNaturalStop(world, instance);
        inner$frames.get().push(new AnimationStopContext(world, instance,
                reason instanceof Enum<?> value && value.name().equals("NATURAL_END")));
    }

    // Staged animations finish inline rather than through stopInstance. This call exists
    // only in their final-stage branch, before either the stop or transform-reset packets.
    @Inject(method = ADVANCE, at = @At(value = "INVOKE",
            target = "Lcom/nonid/internal/animation/server/ServerAnimationController;scheduleMobMountRestores(Lnet/minecraft/class_3218;Ljava/util/Collection;J)V"), remap = false)
    private static void inner$coverFinalStage(class_3218 world, UUID instance, @Coerce Object active,
                                              long now, List<?> advances, List<UUID> removed,
                                              CallbackInfoReturnable<Boolean> callback) {
        DildoEntryAnimation.beforeNaturalStop(world, instance);
    }

    @Inject(method = ADVANCE, at = @At("RETURN"), remap = false)
    private static void inner$naturalCompletion(class_3218 world, UUID instance, @Coerce Object active,
                                                long now, List<?> advances, List<UUID> removed,
                                                CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) DildoEntryAnimation.stopped(world, instance, true);
        inner$frames.get().pop();
    }
    // The public STOPPED event runs before restoreTransforms and omits the stop reason.
    // NoN's package-private StopReason is deliberately coerced rather than accessed directly.
    @Inject(method = STOP,
            at = @At("RETURN"), remap = false)
    private static void inner$afterStop(class_3218 world, UUID instance, boolean restore,
                                       @Coerce Object reason, Set<UUID> skippedActors,
                                       CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()) {
            DildoEntryAnimation.stopped(world, instance,
                    reason instanceof Enum<?> value && value.name().equals("NATURAL_END"));
        }
        inner$frames.get().pop();
    }
}
