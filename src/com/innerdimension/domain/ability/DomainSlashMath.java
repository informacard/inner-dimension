package com.innerdimension.domain.ability;

import net.minecraft.class_238;
import net.minecraft.class_243;

/** Geometry and tick quantization shared by the weapon and its regression tests. */
public final class DomainSlashMath {
    private DomainSlashMath() {}

    public static int cooldownTicks(double seconds) {
        return Math.max(1, (int) Math.ceil(seconds * 20));
    }

    public static boolean intersects(class_243 start, class_243 end, class_238 target, double radius) {
        class_238 expanded = target.method_1014(radius);
        return expanded.method_1006(start) || expanded.method_992(start, end).isPresent();
    }

    public static class_243 clipToRange(class_243 start, class_243 end, java.util.function.Predicate<class_243> inside) {
        if (inside.test(end)) return end;
        class_243 delta = end.method_1020(start);
        double low = 0, high = 1;
        for (int i = 0; i < 14; i++) {
            double middle = (low + high) / 2;
            if (inside.test(start.method_1019(delta.method_1021(middle)))) low = middle;
            else high = middle;
        }
        return start.method_1019(delta.method_1021(low));
    }
}
