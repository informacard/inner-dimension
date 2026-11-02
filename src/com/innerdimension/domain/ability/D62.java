package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.*;

/**
 * D62 minecraft:endermite (purple) — 空间扰动 / spatial disturbance.
 *
 * <p>This class deliberately contains no tick behaviour: the normal teleport and ender-pearl
 * landing entry points are hooked by the teleport-guard mixin owned by the teleport teammate,
 * which calls {@link #cancelsTeleport} and forwards the answer to
 * {@code DomainTeleportGuard.blocked(...)}. Everything this ability contributes is the static
 * predicate below.</p>
 *
 * <p>Position of the check: an entity that is an enemy of a covering session is blocked when it
 * starts a teleport inside that domain or when the destination lands inside it. Explicit
 * whitelists (domain evacuation, {@code SafeEntityTeleport}, NoN safe migration, admin/system
 * teleports and boss phase returns) are the caller's decision — a purely positional predicate
 * cannot see the caller's intent, so they must be filtered before the call. Third-party code
 * that writes a position directly remains uncovered by design; no global setPosition cancel is
 * installed.</p>
 */
public final class D62 implements DomainAbility {
    public static final D62 INSTANCE = new D62();
    private D62() {}
    @Override public String id() { return "minecraft:endermite"; }
    @Override public String code() { return "D62"; }
    @Override public int priority() { return 100; }

    /**
     * TRUE when an enemy of a covering session would teleport out of, into or inside a domain.
     *
     * @param entity the teleporting entity
     * @param x      destination x
     * @param y      destination y
     * @param z      destination z
     */
    public static boolean cancelsTeleport(class_1297 entity, double x, double y, double z) {
        return cancelsTeleport(entity, entity == null ? null : entity.method_73183(), x, y, z);
    }

    public static boolean cancelsTeleport(class_1297 entity, class_1937 destinationWorld,
                                          double x, double y, double z) {
        if (entity == null) return false;
        for (DomainSession session : DomainSessionIndex.covering(entity))
            if (session.hasCode("D62") && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D62",entity)) return true;
        class_1937 world = destinationWorld == null ? entity.method_73183() : destinationWorld;
        if (world == null || world.method_8608()) return false;
        class_243 destination = new class_243(x, y, z);
        for (DomainSession session : DomainSessionIndex.coveringPoint(world, destination))
            if (session.hasCode("D62") && com.innerdimension.domain.runtime.DomainObjects.qualifies(session,"D62",entity)) return true;
        return false;
    }
}
