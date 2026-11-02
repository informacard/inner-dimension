package com.innerdimension.animation;

import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.security.EntrySafetyGuard;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.nonid.api.animation.NonAnimationApi;
import com.nonid.api.animation.NonAnimationEvents;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

/** Server-owned entry requests, completed only by NoN's natural end of this instance. */
public final class DildoEntryAnimation {
    /**
     * The client applies NoN's stop payload on the tick after the server ends the animation.
     * Wait one additional complete tick before changing dimensions so that reset is observable
     * before the destination's player state is installed.
     */
    private static final long TELEPORT_DELAY_TICKS = 2L;
    private static final class_2960 ID = class_2960.method_60655("infinitycard", "insert_enderpearl_dildo");
    private static final class_2960 HAND_ID = class_2960.method_60655("infinitycard", "hand_insert_enderpearl_dildo");
    private static final Map<UUID, Request> PENDING = new HashMap<>();
    private static final List<FinishedRequest> FINISHED = new ArrayList<>();
    private static final java.util.Set<UUID> CLOSING = new java.util.HashSet<>();

    /** dildo is null for the held-item animation, whose entry anchor is the player. */
    private record Request(class_3218 world, UUID player, UUID dildo) {}
    private record FinishedRequest(Request request, long eligibleAtTick) {}

    private DildoEntryAnimation() {}

    public static void register() {
        NonAnimationEvents.STARTED.register(context -> {
            var session = context.session();
            if (!HAND_ID.equals(session.animationId())) return;
            int playerIndex = session.actorKeys().indexOf("player");
            if (playerIndex < 0 || playerIndex >= session.actorUuids().size()) return;
            UUID playerUuid = session.actorUuids().get(playerIndex);
            class_3222 player = context.world().method_8503().method_3760().method_14602(playerUuid);
            if (player == null || player.method_51469() != context.world()
                    || hasPendingPlayer(playerUuid)) return;
            PENDING.put(session.instanceId(), new Request(context.world(), playerUuid, null));
            com.innerdimension.transport.TeleportWarmup.request(player);
        });
    }

    private static boolean hasPendingPlayer(UUID player) {
        return PENDING.values().stream().anyMatch(request -> request.player().equals(player))
                || FINISHED.stream().anyMatch(finished -> finished.request().player().equals(player));
    }

    public static void start(class_3222 player, EnderPearlDildoEntity dildo) {
        if (hasPendingPlayer(player.method_5667())) return;
        if (EntrySafetyGuard.wearsEntryBlockingBelt(player)) {
            DollSecurityManager.send(player, EntrySafetyGuard.MSG_BELT);
            return;
        }
        class_3218 world = player.method_51469();
        if (dildo.method_73183() != world || DynamicDimensionManager.isInnerWorld(world.method_27983())) return;
        var actors = List.of(player, dildo);
        var keys = List.of("player", "ender_pearl_dildo");
        if (!NonAnimationApi.canStart(world, ID, actors, keys, dildo)) {
            failed(player);
            return;
        }
        var defaults = NonAnimationApi.StartOptions.defaults();
        var options = new NonAnimationApi.StartOptions(defaults.damageBehavior(), defaults.ignoreAttackers(),
                dildo, player, Map.of("inner_dimension_entry", "true"), false, false);
        UUID instance = NonAnimationApi.start(world, ID, actors, keys, options);
        if (instance == null) {
            failed(player);
            return;
        }
        PENDING.put(instance, new Request(world, player.method_5667(), dildo.method_5667()));
        com.innerdimension.transport.TeleportWarmup.request(player);
    }

    private static void failed(class_3222 player) {
        player.method_7353(class_2561.method_43471("inner_dimension.entry.animation_failed"), true);
    }

    /** Cover the reset without moving the actor before NoN releases its locks. */
    public static void beforeNaturalStop(class_3218 world, UUID instance) {
        sendEyeTransition(world, instance, true);
    }

    private static void sendEyeTransition(class_3218 world, UUID instance, boolean closed) {
        Request request = PENDING.get(instance);
        if (request == null || request.world() != world) return;
        var player = world.method_8503().method_3760().method_14602(request.player());
        if (player != null && player.method_51469() == world
                && net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player,
                    com.innerdimension.network.EntryTransitionPayload.ID)) {
            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,
                    new com.innerdimension.network.EntryTransitionPayload(closed, false, request.dildo() == null ? 2 : 1));
            org.slf4j.LoggerFactory.getLogger("inner_dimension.entry_transition").info(
                    "[InnerEntryMask] SENT animation instance={} player={}", instance, request.player());
        }
    }

    /** Called after NoN restores transforms, releases locks and removes the active instance. */
    public static void stopped(class_3218 world, UUID instance, boolean naturalEnd) {
        Request request = PENDING.get(instance);
        if (request == null || request.world() != world) return;
        PENDING.remove(instance);
        CLOSING.remove(instance);
        if (naturalEnd) {
            FINISHED.add(new FinishedRequest(request, com.innerdimension.core.ServerClock.now() + TELEPORT_DELAY_TICKS));
        } else {
            var player = world.method_8503().method_3760().method_14602(request.player());
            if (player != null) com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
        }
    }

    /** Runs at server-tick end after NoN's restoration. */
    public static void tick(MinecraftServer server) {
        // Announce seven ticks before the final stage ends so the earlier camera
        // impulse is visible; client eyelid timing compensates for the extra two ticks.
        // The stop hook still
        // forces fully shut eyes before reset packets, including accelerated endings.
        for (var entry : PENDING.entrySet()) {
            var request = entry.getValue();
            if (CLOSING.contains(entry.getKey())) continue;
            var snapshot = com.nonid.internal.animation.server.ServerAnimationController
                    .getActiveInstanceSnapshot(request.world(), entry.getKey());
            if (snapshot == null || snapshot.stages().isEmpty()
                    || snapshot.stageIndex() != snapshot.stages().size() - 1) continue;
            long left = snapshot.stageEndTick() - request.world().method_75260();
            if (snapshot.stageEndTick() != Long.MAX_VALUE && left > 0 && left <= 7) {
                CLOSING.add(entry.getKey());
                sendEyeTransition(request.world(), entry.getKey(), false);
            }
        }
        var ready = new ArrayList<FinishedRequest>();
        FINISHED.removeIf(finished -> {
            if (com.innerdimension.core.ServerClock.now() < finished.eligibleAtTick()) return false;
            ready.add(finished);
            return true;
        });
        for (FinishedRequest finished : ready) {
            Request request = finished.request();
            class_3222 player = server.method_3760().method_14602(request.player());
            if (player == null || !player.method_5805() || player.method_51469() != request.world()) continue;
            // A queued successor must not be interrupted by entering another dimension.
            if (!NonAnimationApi.isActorPendingOrActive(request.world(), request.player())) {
                if (request.dildo() == null) {
                    com.innerdimension.sound.ModSounds.personal(player,"lubricated_entry",.8f);
                    
                    if (DynamicDimensionManager.isInnerWorld(request.world().method_27983())) {
                        DynamicDimensionManager.returnToLanding(player);
                    } else {
                        DynamicDimensionManager.enter(player);
                    }
                } else if (request.world().method_66347(request.dildo()) instanceof EnderPearlDildoEntity dildo
                        && dildo.method_5805()) {
                    var gate = DollSecurityManager.forInteraction(dildo, request.player());
                    if (gate.blocked()) DollSecurityManager.sendFeedback(player, gate);
                    else DynamicDimensionManager.enter(player, dildo);
                }
            }
            com.innerdimension.network.EntryTransitionPayload.send(player, true, true);
        }
        // stop-all/disconnect paths can remove sessions without calling the single-instance stop.
        PENDING.entrySet().removeIf(entry -> NonAnimationApi.getSession(entry.getValue().world(), entry.getKey()) == null);
        CLOSING.retainAll(PENDING.keySet());
    }

    public static void clear() {
        PENDING.clear();
        FINISHED.clear();
        CLOSING.clear();
    }
}
