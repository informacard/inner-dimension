package com.innerdimension.projection;

import net.minecraft.class_2338;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3610;

/**
 * Cell-level block view of an active projection, for consumers that read the world directly.
 *
 * <p>The room snapshot carries real block states (that is how the shell, its floor, beds and
 * water are built), but it lives in a Create contraption offset by the anchor's fractional
 * part, so vanilla never sees it. NoN's animation placement asks the world "what block is
 * here" for support points, walls, beds and water, which is exactly a cell-level question:
 * answering it from the snapshot makes those checks behave as if the room were real.</p>
 *
 * <p>Deliberately <b>not</b> a global {@code World.getBlockState} override: the snapshot is
 * offset by up to a block from the world grid, so cell-aligned answers would add phantom
 * collision next to the exact shapes {@link ProjectionEnvironment} already feeds to
 * movement. Callers that need physics keep using {@code ProjectionEnvironment}.</p>
 */
public final class ProjectionBlocks {
    private ProjectionBlocks() {}

    /** The room's block state at this world cell, or null when the real world owns the cell. */
    public static class_2680 projected(class_1937 world, class_2338 pos) {
        if (world == null || pos == null || !ProjectionIndex.any()) return null;
        class_2680 shell = null;
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, new class_238(pos))) {
            var contraption = projection.getContraption();
            if (contraption == null) continue;
            class_243 anchor = projection.getAnchorVec();
            // The room is deliberately placed at a fractional anchor (that offset is part of the
            // design), so one world cell is usually cut between two local blocks. Ask for the cell
            // centre and take the block that really contains it: that is the block covering most
            // of the queried cell, which is what a cell-level question ("is this a fence gate /
            // solid floor / wall column") is actually about. The old floor(cell - anchor) picked
            // the neighbouring block whenever the anchor had any fraction, which made NoN match the
            // placement variant one block off.
            class_2338 local = new class_2338(
                    (int) Math.floor(pos.method_10263() + 0.5 - anchor.field_1352),
                    (int) Math.floor(pos.method_10264() + 0.5 - anchor.field_1351),
                    (int) Math.floor(pos.method_10260() + 0.5 - anchor.field_1350));
            var info = contraption.getBlocks().get(local);
            if (info == null) continue;
            // The interior half is the room; the shell is only a fallback while it opens.
            if (!projection.isShell()) return info.comp_1342();
            shell = info.comp_1342();
        }
        return shell;
    }

    /** {@link #projected} when the room covers the cell, otherwise the real block state. */
    public static class_2680 state(class_1937 world, class_2338 pos) {
        class_2680 projected = projected(world, pos);
        return projected != null ? projected : world.method_8320(pos);
    }

    /** {@link #projected} fluid when the room covers the cell, otherwise the real fluid. */
    public static class_3610 fluid(class_1937 world, class_2338 pos) {
        class_2680 projected = projected(world, pos);
        return projected != null ? projected.method_26227() : world.method_8316(pos);
    }
}
