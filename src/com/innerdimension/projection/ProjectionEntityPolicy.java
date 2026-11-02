package com.innerdimension.projection;

import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.entity.SensoryLinkDollEntity;
import net.minecraft.*;

/** Shared by evacuation and Create collision on both client and server. */
public final class ProjectionEntityPolicy {
    private ProjectionEntityPolicy() { }

    public static boolean ignores(class_1297 entity) {
        if (com.innerdimension.boss.BossDomains.isBoss(entity)) return true;
        if (entity instanceof EnderPearlDildoEntity || entity instanceof SensoryLinkDollEntity
                || entity instanceof class_1531 || entity instanceof class_1530) return true;
        // Other packs can register their own dildo/plushie entities as living mobs.
        String path = class_7923.field_41177.method_10221(entity.method_5864()).method_12832();
        return path.contains("dildo") || path.contains("plushie") || path.contains("plush")
                || path.contains("sensory_link_doll");
    }
}
