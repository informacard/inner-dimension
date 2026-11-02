package com.innerdimension.mixin;

import com.innerdimension.block.ModBlocks;
import com.innerdimension.dimension.DynamicDimensionManager;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2614;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes pearl-floor hoppers into adjacent strong-port chests, and side hoppers into tethered devices.
 *
 * <p>Injected at the head of {@code HopperBlockEntity#insert(World, BlockPos, HopperBlockEntity)},
 * which is the single "push one item to my output" step: it is called from the hopper's tick only
 * after the block's {@code enabled} state was checked, the transfer cooldown elapsed and the hopper
 * turned out to be non-empty. A redstone-locked hopper therefore never reaches this point at all,
 * and the pickup half of the tick is untouched - the mod changes where a funnel sends items, not
 * what it can pick up.</p>
 *
 * <p>Ordinary hopper output remains vanilla outside those two narrowly matched scenes.</p>
 */
@Mixin(class_2614.class)
public abstract class HopperBlockEntityMixin {
    @Inject(method = "method_11246", at = @At("HEAD"), cancellable = true)
    private static void inner$outputThroughPearlFace(class_1937 world, class_2338 pos, class_2614 hopper,
                                                     CallbackInfoReturnable<Boolean> callback) {
        if (!(world instanceof class_3218 serverWorld)) {
            return;
        }
        if (DynamicDimensionManager.isInnerWorld(serverWorld.method_27983())
                && world.method_8320(pos.method_10074()).method_26204() == ModBlocks.PEARL_FACE) {
            callback.setReturnValue(com.innerdimension.transport.DildoHopperTransport.outputToChests(serverWorld, hopper));
        } else {
            Boolean input = com.innerdimension.transport.DildoHopperTransport.inputToTethered(serverWorld, pos, hopper);
            if (input != null) callback.setReturnValue(input);
        }
    }
}
