package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

import java.util.UUID;

/**
 * Shared registry lookups for D47-D69.
 *
 * <p>Everything here resolves by vanilla identifier through a static registry instead of a
 * hard-coded obfuscated constant, so a mapping change degrades to "not found" instead of a
 * wrong block / attribute / entity.</p>
 */
final class D47D69Support {
    /** Vanilla {@code minecraft:flowers} block tag; the caller filters out tall flowers. */
    static final class_6862<class_2248> FLOWERS =
            class_6862.method_40092(class_7924.field_41254, class_2960.method_60655("minecraft", "flowers"));

    /**
     * Spawn reason for the abilities' own entity creation (position 13 of
     * {@code class_3730}'s declaration order, cross-checked against the mapped
     * {@code MobSpawnType} enumeration order).
     */
    static final class_3730 SPAWN_REASON = class_3730.field_16462;

    private D47D69Support() {}

    private static class_2960 id(String id) {
        int colon = id.indexOf(':');
        String namespace = colon < 0 ? "minecraft" : id.substring(0, colon);
        String path = colon < 0 ? id : id.substring(colon + 1);
        return class_2960.method_60655(namespace, path);
    }

    /** Attribute holder (the lease managers work on holders, not on the attribute value). */
    static class_6880<class_1320> attribute(String id) {
        return class_7923.field_41190.method_10223(id(id)).orElse(null);
    }

    static class_2248 block(String id) {
        return class_7923.field_41175.method_10223(id(id)).map(class_6880::comp_349).orElse(null);
    }

    static class_1299<?> entityType(String id) {
        return class_7923.field_41177.method_10223(id(id)).map(class_6880::comp_349).orElse(null);
    }

    /** Creates an entity of the given registry id at the given position, or null. */
    static class_1297 create(class_1937 world, String typeId, double x, double y, double z) {
        if (!(world instanceof class_3218 server)) return null;
        class_1299<?> type = entityType(typeId);
        if (type == null) return null;
        class_1297 entity = type.method_5883(server, SPAWN_REASON);
        if (entity == null) return null;
        // setPos also rebuilds the bounding box; setPosRaw leaves it at the creation origin.
        entity.method_5814(x, y, z);
        return entity;
    }

    static class_1309 living(class_1937 world, UUID uuid) {
        if (uuid == null || !(world instanceof class_3218 server)) return null;
        class_1297 entity = server.method_73284(uuid);
        return entity instanceof class_1309 living && living.method_5805() ? living : null;
    }

    /** Live entity behind a session-state UUID value, or null when it is absent or stale. */
    static class_1309 stored(DomainSession session, Object stored) {
        return stored instanceof UUID uuid ? living(session.world, uuid) : null;
    }

    /** Any golden armour piece in any equipment slot (hands do not count). */
    static boolean goldenArmour(class_1309 entity) {
        if (entity == null) return false;
        for (class_1304 slot : class_1304.field_54086) {
            class_1799 stack = entity.method_6118(slot);
            if (stack == null || stack.method_7960()) continue;
            class_2960 item = class_7923.field_41178.method_10221(stack.method_7909());
            if (item == null) continue;
            String path = item.method_12832();
            if (!path.startsWith("golden_")) continue;
            if (path.endsWith("_helmet") || path.endsWith("_chestplate")
                    || path.endsWith("_leggings") || path.endsWith("_boots")) return true;
        }
        return false;
    }

    /**
     * A real (never projected) single-block flower at {@code pos}. Uses the vanilla flower
     * tag and rejects the two-block and non-cube members.
     */
    static boolean realFlower(class_1937 world, class_2338 pos) {
        if (world == null || pos == null) return false;
        class_2680 state = world.method_8320(pos);
        if (state.method_26215() || !state.method_26164(FLOWERS)) return false;
        for (class_2769<?> property : state.method_28501())
            if (property.method_11899().equals("half")) return false;
        class_2960 blockId = class_7923.field_41175.method_10221(state.method_26204());
        if (blockId == null) return false;
        String path = blockId.method_12832();
        return !path.contains("pot") && !path.contains("petal") && !path.contains("wildflower");
    }
}
