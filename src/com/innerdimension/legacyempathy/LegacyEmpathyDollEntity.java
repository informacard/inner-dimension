package com.innerdimension.legacyempathy;

import com.innerdimension.entity.SensoryLinkDollEntity;
import net.minecraft.class_1299;
import net.minecraft.class_1937;

/**
 * Deserialization target for the doll entity id this mod used before the sensory link rename.
 *
 * <p>This class exists so that a save written by an older build does not turn its dolls into unknown
 * entities. It adds no behaviour at all: it is the same doll, and it is deliberately the <em>only</em>
 * type registered under that old id, so no new interaction can ever produce one.</p>
 *
 * <p>A loaded doll of this type is left exactly as it is. Its entity uuid, its binding, its lock, its
 * piston relationship and its position all stay untouched, because rewriting or respawning it on load
 * would invalidate the bindings, the locks and the machine around it. It simply keeps working until a
 * player breaks it normally, which hands back a new sensory link doll.</p>
 */
public class LegacyEmpathyDollEntity extends SensoryLinkDollEntity {
    public LegacyEmpathyDollEntity(class_1299<? extends SensoryLinkDollEntity> type, class_1937 world) {
        super(type, world);
    }
}
