package com.innerdimension.projection;

import net.minecraft.*;

/** Merge solid projection shapes into vanilla's projectile block raycast. */
public final class ProjectionProjectiles {
    private ProjectionProjectiles() {}

    public static boolean supportsArrow(class_1297 arrow) {
        class_238 probe = new class_238(arrow.method_73189(), arrow.method_73189()).method_1014(.06);
        for (RoomProjectionEntity projection : ProjectionIndex.solid(arrow.method_73183(), probe.method_1014(1))) {
            var snapshot = (RoomProjectionContraption)projection.getContraption();
            if (snapshot == null) continue;
            var origin = projection.getAnchorVec();
            for (var entry : snapshot.getBlocks().entrySet()) {
                var pos = entry.getKey();
                if (!new class_238(pos).method_989(origin.field_1352, origin.field_1351, origin.field_1350).method_994(probe)) continue;
                for (var box : entry.getValue().comp_1342().method_26194(snapshot.getContraptionWorld(), pos,
                        class_3726.method_16194()).method_1090())
                    if (box.method_989(origin.field_1352 + pos.method_10263(), origin.field_1351 + pos.method_10264(),
                            origin.field_1350 + pos.method_10260()).method_994(probe)) return true;
            }
        }
        return false;
    }

    public static class_3965 hit(class_1937 world, class_3959 ray, class_3965 real) {
        return hit(world, ray, real, false);
    }

    /** Camera rays use vanilla's camera shape rather than projectile collision shapes. */
    public static class_3965 hit(class_1937 world, class_3959 ray, class_3965 real, boolean camera) {
        class_243 start = ray.method_17750(), end = ray.method_17747();
        class_3965 nearest = real;
        double distance = real.method_17783() == class_239.class_240.field_1333
                ? Double.POSITIVE_INFINITY : start.method_1025(real.method_17784());
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, new class_238(start, end).method_1014(1))) {
            var snapshot = (RoomProjectionContraption) projection.getContraption();
            if (snapshot == null) continue;
            class_243 origin = projection.getAnchorVec();
            class_243 localStart = start.method_1020(origin), localEnd = end.method_1020(origin);
            for (var entry : snapshot.getBlocks().entrySet()) {
                class_2338 pos = entry.getKey();
                if (!new class_238(pos).method_989(origin.field_1352, origin.field_1351, origin.field_1350)
                        .method_994(new class_238(start, end).method_1014(.001))) continue;
                var shape = camera
                        ? ray.method_17748(entry.getValue().comp_1342(), snapshot.getContraptionWorld(), pos)
                        : entry.getValue().comp_1342().method_26194(
                                snapshot.getContraptionWorld(), pos, class_3726.method_16194());
                class_3965 local = shape.method_1092(localStart, localEnd, pos);
                if (local == null) continue;
                class_243 point = local.method_17784().method_1019(origin);
                double candidate = start.method_1025(point);
                if (candidate < distance) {
                    nearest = new class_3965(point, local.method_17780(), class_2338.method_49638(point), local.method_17781());
                    distance = candidate;
                }
            }
        }
        return nearest;
    }
}
