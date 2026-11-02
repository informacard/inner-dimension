package com.innerdimension.transport;

import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.state.InnerState;
import com.innerdimension.exit.ExitRouter;
import com.nonid.api.animation.NonAnimationApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.class_3222;
import java.util.LinkedHashMap;
import java.util.UUID;

/** Bounded anticipation of an actual entry animation or an approaching floor exit. */
public final class TeleportWarmup {
    private record Preparation(long due, boolean onJoin) {}
    private static final LinkedHashMap<UUID, Preparation> ENTRIES = new LinkedHashMap<>();
    private static long nextPreparation;
    private static final java.util.Set<UUID> SNEAKING = new java.util.HashSet<>();
    private static final java.util.Set<UUID> RETURNS = new java.util.LinkedHashSet<>();
    private TeleportWarmup() {}
    public static void request(class_3222 player) {
        if (!DynamicDimensionManager.isInnerWorld(player.method_51469().method_27983()))
            ENTRIES.putIfAbsent(player.method_5667(), new Preparation(com.innerdimension.core.ServerClock.now() + 10, false));
    }
    private static void clear() { ENTRIES.clear(); RETURNS.clear(); SNEAKING.clear(); nextPreparation = 0; }
    private static boolean atBottom(class_3222 player) {
        return player.method_23318() <= 3
                && DynamicDimensionManager.isInnerWorld(player.method_51469().method_27983());
    }
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clear());
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ENTRIES.remove(handler.field_14140.method_5667());
            RETURNS.remove(handler.field_14140.method_5667());
            SNEAKING.remove(handler.field_14140.method_5667());
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ENTRIES.put(handler.field_14140.method_5667(),
                        new Preparation(com.innerdimension.core.ServerClock.now() + 20, true)));
        // This callback is registered after the dynamic-world end-of-tick callback.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = com.innerdimension.core.ServerClock.now();
            var iterator = ENTRIES.entrySet().iterator();
            while (now >= nextPreparation && iterator.hasNext()) {
                var entry = iterator.next();
                if (entry.getValue().due() > now) continue;
                boolean onJoin = entry.getValue().onJoin();
                UUID owner = entry.getKey();
                iterator.remove();
                var player = server.method_3760().method_14602(owner);
                if (player == null) continue;
                if (!onJoin && (DynamicDimensionManager.isInnerWorld(player.method_51469().method_27983())
                        || !NonAnimationApi.isActorPendingOrActive(player.method_51469(), owner))) continue;
                nextPreparation = now + 20;
                try {
                    InnerState.get(server.method_30002()).addRoomOwner(owner);
                    var room = DynamicDimensionManager.getOrCreate(server, owner);
                    // Prime the actual arrival chunk even when the world already exists.
                    com.innerdimension.chunk.TemporaryChunkLoads.holdChunk(room, 0, 0);
                    room.method_14178().method_12121(0, 0, net.minecraft.class_2806.field_12803, true);
                } catch (RuntimeException failure) {
                    org.slf4j.LoggerFactory.getLogger("inner_dimension").warn("Entry warmup skipped for {}", owner, failure);
                }
                break; // Space room preparations by at least one second, including simultaneous joins.
            }
            // Detect the transition into sneaking, rather than renewing on a timer.
            // A held sneak key never creates another preload request.
            for (var player : server.method_3760().method_14571()) {
                UUID id = player.method_5667();
                if (!player.method_5715()) {
                    SNEAKING.remove(id);
                    RETURNS.remove(id);
                    continue;
                }
                boolean started = SNEAKING.add(id);
                if (started && atBottom(player)) RETURNS.add(id);
            }
            var returns = RETURNS.iterator();
            while (returns.hasNext()) {
                UUID id = returns.next();
                returns.remove();
                var player = server.method_3760().method_14602(id);
                if (player == null || !player.method_5715() || !atBottom(player)) continue;
                ExitRouter.prefetchReturn(player);
                break; // At most one landing footprint per tick; retain other requests.
            }
        });
    }
}