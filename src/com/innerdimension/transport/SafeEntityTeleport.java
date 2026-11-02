package com.innerdimension.transport;

import com.nonid.api.animation.NonAnimationApi;
import com.nonid.internal.animation.server.ServerAnimationController;
import com.nonid.internal.animation.network.StopAnimationS2CPayload;
import com.innerdimension.mixin.NonAnimationQueueAccess;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.*;

/** Finish the actor's animation restoration before moving its body to another location. */
public final class SafeEntityTeleport {
    private static final ThreadLocal<Boolean> RESTORING = ThreadLocal.withInitial(() -> false);
    /** Narrow bypass used only while deleting a room that must not retain live players. */
    private static final ThreadLocal<Boolean> FORCED_ROOM_EVACUATION = ThreadLocal.withInitial(() -> false);
    private SafeEntityTeleport() {}

    public static boolean prepare(class_1297 entity) {
        if (!(entity.method_73183() instanceof class_3218 world)) return false;
        if (FORCED_ROOM_EVACUATION.get()) return true;
        if (RESTORING.get()) return true;
        RESTORING.set(true);
        try {
            // Scan full instances: stopping one actor must stop the entire shared session,
            // not merely its player queue or the actor-index entry.
            var sessions = ServerAnimationController.getActiveInstanceSnapshots(world).stream()
                    .filter(session -> session.actorUuids().contains(entity.method_5667())).toList();
            Set<UUID> actors = new HashSet<>();
            actors.add(entity.method_5667());
            for (var session : sessions) actors.addAll(session.actorUuids());
            // Mobs can be queued in another player's queue without that player in this session.
            for (UUID actor : actors) NonAnimationQueueAccess.inner$clearQueuesContainingActor(world, actor);
            for (var session : sessions) {
                // EXTERNAL_STOP restores every actor's transform, AI and player locks synchronously.
                if (!NonAnimationApi.stop(world, session.instanceId())) return false;
                // NoN normally schedules against the origin world's time + 1. A dimension switch
                // can change the client clock, so use an already-due instance-specific stop.
                var stop = new StopAnimationS2CPayload(session.instanceId(), 0L);
                for (class_3222 observer : world.method_8503().method_3760().method_14571()) {
                    if (ServerPlayNetworking.canSend(observer, StopAnimationS2CPayload.ID)) {
                        ServerPlayNetworking.send(observer, stop);
                    }
                }
            }
            for (UUID actor : actors) {
                if (NonAnimationApi.isActorPendingOrActive(world, actor)) return false;
            }
            return true;
        } finally {
            RESTORING.remove();
        }
    }

    public static boolean move(class_1297 entity, class_3218 target, double x, double y, double z,
                               Set<class_2709> relative, float yaw, float pitch, boolean resetCamera) {
        if (!prepare(entity)) return false;
        entity.method_5848();
        // D62 whitelist: this is the mod's own safe relocation (domain entry/exit included),
        // never an ordinary hostile migration.
        com.innerdimension.domain.world.DomainTeleportGuard.push(
                com.innerdimension.domain.world.DomainTeleportGuard.Reason.SAFE_TELEPORT);
        try {
            return entity.method_48105(target, x, y, z, relative, yaw, pitch, resetCamera);
        } finally {
            com.innerdimension.domain.world.DomainTeleportGuard.pop();
        }
    }

    /**
     * Last-resort evacuation used exclusively before an inner world is destroyed.
     *
     * <p>This deliberately bypasses animation cleanup refusal (including the player teleport
     * mixin's second prepare call), but it does not pretend success: callers must still verify the
     * entity is no longer in the source world before deleting anything.</p>
     */
    public static boolean forceRoomEvacuation(class_1297 entity, class_3218 target,
                                              double x, double y, double z,
                                              float yaw, float pitch) {
        if (entity == null || target == null) return false;
        FORCED_ROOM_EVACUATION.set(true);
        com.innerdimension.domain.world.DomainTeleportGuard.push(
                com.innerdimension.domain.world.DomainTeleportGuard.Reason.DOMAIN_EVACUATION);
        try {
            entity.method_5848();
            return entity.method_48105(target, x, y, z, Set.of(), yaw, pitch, false);
        } finally {
            com.innerdimension.domain.world.DomainTeleportGuard.pop();
            FORCED_ROOM_EVACUATION.remove();
        }
    }
}
