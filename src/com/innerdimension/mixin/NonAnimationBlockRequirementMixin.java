package com.innerdimension.mixin;

import com.innerdimension.projection.ProjectionBlocks;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3610;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Teaches NoN's animation placement about the projected room.
 *
 * <p>Every support/requirement decision NoN makes is a cell-level question — "what block is
 * under the anchor", "is this a solid wall", "is this bed/water", "is the footprint clear" —
 * and each one reads {@code ServerWorld.getBlockState/getFluidState} directly. Those reads are
 * redirected to {@link ProjectionBlocks}, so the room's own blocks (floor, walls, beds, water)
 * satisfy them exactly like real ones instead of the animation snapping down to the vanilla
 * blocks underneath.</p>
 *
 * <p>Only NoN's placement helpers are redirected. Vanilla collision keeps using the exact
 * contraption shapes through {@code ProjectionEnvironment}, because the snapshot is offset by
 * the anchor's fractional part and cell-aligned answers there would be a block off.</p>
 */
@Mixin(targets = "com.nonid.internal.animation.server.ServerAnimationController", remap = false)
public abstract class NonAnimationBlockRequirementMixin {
    private static final String STATE =
            "Lnet/minecraft/class_3218;method_8320(Lnet/minecraft/class_2338;)Lnet/minecraft/class_2680;";
    private static final String FLUID =
            "Lnet/minecraft/class_3218;method_8316(Lnet/minecraft/class_2338;)Lnet/minecraft/class_3610;";

    /* --------------------------------------------------------------- support */

    @Redirect(method = "supportTopOffset", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$supportTop(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "supportSurfaceTopOffset", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$supportSurface(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "isSameOrLowerSurface", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$sameOrLowerSurface(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "findNearestFloor", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$nearestFloor(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "isFreeSpace", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$freeSpace(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    /* ----------------------------------------------------------------- walls */

    @Redirect(method = "isSolidWallBlock", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$solidWall(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "isSolidFloorBlock", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$solidFloor(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "isWallRequirementBlock", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$wallRequirement(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "resolveWallColumn", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$wallColumn(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "lambda$exceedsMaximumWallRun$0", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$wallRun(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    @Redirect(method = "lambda$addWallVisualization$0", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$wallVisualization(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    /* ------------------------------------------------------------------ beds */

    @Redirect(method = "resolveBedFootprintFacing", at = @At(value = "INVOKE", target = STATE), remap = false)
    private static class_2680 inner$bedFootprint(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.state(world, pos);
    }

    /* ----------------------------------------------------------------- water */

    @Redirect(method = "anyActorOnWaterFooting", at = @At(value = "INVOKE", target = FLUID), remap = false)
    private static class_3610 inner$waterFooting(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.fluid(world, pos);
    }

    @Redirect(method = "containsFluid", at = @At(value = "INVOKE", target = FLUID), remap = false)
    private static class_3610 inner$containsFluid(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.fluid(world, pos);
    }

    @Redirect(method = "containsFluidInPlacementColumn", at = @At(value = "INVOKE", target = FLUID), remap = false)
    private static class_3610 inner$fluidColumn(class_3218 world, class_2338 pos) {
        return ProjectionBlocks.fluid(world, pos);
    }
}
