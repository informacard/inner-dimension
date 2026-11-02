package com.innerdimension.projection;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_3965;

/** Prefer the world cell containing the picked surface. */
public final class ProjectionItemTarget {
    private ProjectionItemTarget() {}

    public static class_3965 worldHit(class_243 point, class_2350 side) {
        // Only resolve exact grid boundaries toward the exposed side. A half-block
        // offset skips the containing cell on positive faces of fractional anchors.
        class_2338 cell = class_2338.method_49638(new class_243(
                outsideBoundary(point.field_1352, side.method_10148()),
                outsideBoundary(point.field_1351, side.method_10164()),
                outsideBoundary(point.field_1350, side.method_10165())));
        // A synthetic hit must still belong to its declared world cell for vanilla's
        // server packet validation. Preserve the actual hit within that cell's bounds.
        class_243 hit = new class_243(
                clamp(point.field_1352, cell.method_10263()),
                clamp(point.field_1351, cell.method_10264()),
                clamp(point.field_1350, cell.method_10260()));
        return new class_3965(hit, side, cell, false);
    }

    private static double outsideBoundary(double value, int normal) {
        double boundary = Math.rint(value);
        return normal != 0 && Math.abs(value - boundary) < 1e-7
                ? boundary + normal * 1e-7 : value;
    }

    private static double clamp(double value, int cell) {
        return Math.max(cell, Math.min(cell + 1.0, value));
    }
}
