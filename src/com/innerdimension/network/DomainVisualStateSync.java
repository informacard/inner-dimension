package com.innerdimension.network;

import com.innerdimension.progression.DomainAccess;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

/** Server-thread state replication; new recipients are initialized only after channel negotiation. */
public final class DomainVisualStateSync {
    private static final int REVISION = 1;
    private static final Map<UUID, DomainVisualStatePayload> LAST = new HashMap<>();
    private static final Set<UUID> INITIALIZED = new HashSet<>();

    private DomainVisualStateSync() {}

    public static void tick(MinecraftServer server) {
        var players = server.method_3760().method_14571();
        Map<UUID, DomainVisualStatePayload> current = new HashMap<>();
        Map<UUID, DomainVisualStatePayload> changed = new HashMap<>();
        for (class_3222 player : players) {
            UUID id = player.method_5667();
            boolean active = DomainAccess.isUnlocked(player);
            var state=new DomainVisualStatePayload(id,active,REVISION,com.innerdimension.projection.RoomProjectionManager.domainAge(id),com.innerdimension.projection.RoomProjectionManager.burnoutRemaining(id),com.innerdimension.domain.ability.D52.protectedFromLava(player)?1:0);
            current.put(id,state);
            var old=LAST.put(id,state);
            if (!state.equals(old)) changed.put(id,state);
        }
        LAST.keySet().retainAll(current.keySet());
        INITIALIZED.retainAll(current.keySet());
        for (class_3222 recipient : players) {
            UUID id = recipient.method_5667();
            if (!ServerPlayNetworking.canSend(recipient, DomainVisualStatePayload.ID)) {
                INITIALIZED.remove(id);
                continue;
            }
            // JOIN can run before the client advertises its channels. Retry each tick until
            // canSend is true; an unsent state must never count as delivered.
            Map<UUID, DomainVisualStatePayload> updates = INITIALIZED.contains(id) ? changed : current;
            updates.forEach((owner, active) -> ServerPlayNetworking.send(recipient,
                    active));
            INITIALIZED.add(id);
        }
    }

    public static void joined(MinecraftServer server, class_3222 joined) {
        LAST.remove(joined.method_5667());
        INITIALIZED.remove(joined.method_5667());
        // The next tick sends a complete snapshot once this recipient can actually receive it.
    }

    public static void left(MinecraftServer server, UUID id) {
        LAST.remove(id);
        INITIALIZED.remove(id);
        DomainVisualStatePayload departure = new DomainVisualStatePayload(id,false,REVISION,-1,0);
        for (class_3222 recipient : server.method_3760().method_14571()) {
            if (INITIALIZED.contains(recipient.method_5667())
                    && ServerPlayNetworking.canSend(recipient, DomainVisualStatePayload.ID)) {
                ServerPlayNetworking.send(recipient, departure);
            }
        }
    }

    public static void clear() {
        LAST.clear();
        INITIALIZED.clear();
    }
}
