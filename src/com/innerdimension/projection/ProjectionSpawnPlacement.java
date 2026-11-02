package com.innerdimension.projection;
import com.innerdimension.domain.geometry.DomainGeometry;
import net.minecraft.*;
/** Place the whole body on the exposed face, including fractional projection anchors. */
public final class ProjectionSpawnPlacement {
    private ProjectionSpawnPlacement() {}
    public static class_243 facePosition(class_243 hit, class_2350 face, double width, double height) {
        double radius = width * 0.5 + 0.025;
        return new class_243(hit.field_1352 + face.method_10148() * radius,
                hit.field_1351 + (face.method_10164() > 0 ? 0.025 : face.method_10164() < 0 ? -height - 0.025 : 0),
                hit.field_1350 + face.method_10165() * radius);
    }
    public static boolean place(class_1937 world, class_1297 entity, class_243 hit, class_2350 face) {
        class_243 point = facePosition(hit, face, entity.method_17681(), entity.method_17682());
        entity.method_5814(point.field_1352, point.field_1351, point.field_1350);
        if (DomainGeometry.solid(world, entity.method_5829().method_1014(-0.001))) return false;
        if (face.method_10164() == 0) {
            double drop = DomainGeometry.supportDistance(world, entity.method_5829(), 1);
            if (!Double.isNaN(drop) && drop > 0) entity.method_5814(point.field_1352, point.field_1351-drop+0.001, point.field_1350);
        }
        return !DomainGeometry.solid(world, entity.method_5829().method_1014(-0.001));
    }
}
