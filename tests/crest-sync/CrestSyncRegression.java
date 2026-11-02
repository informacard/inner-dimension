package com.innerdimension.tests;
import com.innerdimension.network.DomainVisualStateSync;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
public final class CrestSyncRegression {
    public static void main(String[] args) {
        DomainVisualStateSync.clear();
        MinecraftServer server = new MinecraftServer();
        class_3222 player = new class_3222();
        player.unlocked = true;
        server.players.online.add(player);
        DomainVisualStateSync.joined(server, player);
        DomainVisualStateSync.tick(server);
        check(ServerPlayNetworking.SENT.isEmpty(), "No packet before channel registration");
        player.canSend = true;
        DomainVisualStateSync.tick(server);
        check(received(player, player.method_5667(), true), "Unlocked crest must arrive after late channel registration");
        int count = ServerPlayNetworking.SENT.size();
        DomainVisualStateSync.tick(server);
        check(ServerPlayNetworking.SENT.size() == count, "Unchanged tick must not repeat snapshot");
        class_3222 other = new class_3222();
        other.canSend = true;
        server.players.online.add(other);
        DomainVisualStateSync.joined(server, other);
        DomainVisualStateSync.tick(server);
        check(received(other, player.method_5667(), true), "New observer receives existing unlocked player");
        com.innerdimension.projection.RoomProjectionManager.age=300;
        DomainVisualStateSync.tick(server);
        check(ServerPlayNetworking.SENT.stream().anyMatch(s -> s.payload().domainAge()==300), "Observers receive decay age");
        com.innerdimension.projection.RoomProjectionManager.age=-1;
        com.innerdimension.projection.RoomProjectionManager.burnout=200;
        DomainVisualStateSync.tick(server);
        check(ServerPlayNetworking.SENT.stream().anyMatch(s -> s.payload().burnoutRemaining()==200), "Observers receive burnout");
        ServerPlayNetworking.SENT.clear();
        com.innerdimension.projection.RoomProjectionManager.burnout=0;
        DomainVisualStateSync.tick(server);
        check(ServerPlayNetworking.SENT.stream().anyMatch(s -> s.payload().burnoutRemaining()==0), "Recovery is synchronized");
        ServerPlayNetworking.SENT.clear();
        player.unlocked = false;
        DomainVisualStateSync.tick(server);
        check(received(other, player.method_5667(), false), "Unlock-state changes reach observers");
        ServerPlayNetworking.SENT.clear();
        server.players.online.remove(player);
        DomainVisualStateSync.left(server, player.method_5667());
        check(received(other, player.method_5667(), false), "Departure clears observer state");
        DomainVisualStateSync.clear();
        ServerPlayNetworking.SENT.clear();
        DomainVisualStateSync.tick(server);
        check(received(other, other.method_5667(), false), "Cleared server starts with fresh snapshot");
        System.out.println("PASS: delayed negotiation, snapshots, changes, departure, restart");
    }
    private static boolean received(class_3222 recipient, UUID owner, boolean active) {
        return ServerPlayNetworking.SENT.stream().anyMatch(s -> s.recipient() == recipient
            && s.payload().playerUuid().equals(owner) && s.payload().active() == active);
    }
    private static void check(boolean passed, String message) { if (!passed) throw new AssertionError(message); }
}
