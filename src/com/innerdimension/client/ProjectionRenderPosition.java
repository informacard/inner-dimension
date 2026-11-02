package com.innerdimension.client;

import com.innerdimension.projection.RoomProjectionEntity;
import net.minecraft.class_1297;
import net.minecraft.class_2338;

/** Only the world renderer's section lookup uses this point; entity physics stay anchored. */
public final class ProjectionRenderPosition {
    public static class_2338 sectionPosition(class_1297 entity, class_2338 original) {
        if (!(entity instanceof RoomProjectionEntity projection) || projection.getContraption() == null)
            return original;
        var bounds = projection.method_5829();
        return new class_2338((int)Math.floor((bounds.field_1323 + bounds.field_1320) / 2),
                (int)Math.floor((bounds.field_1322 + bounds.field_1325) / 2),
                (int)Math.floor((bounds.field_1321 + bounds.field_1324) / 2));
    }
}
