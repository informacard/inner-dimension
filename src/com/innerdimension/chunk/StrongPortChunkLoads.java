package com.innerdimension.chunk;

import com.innerdimension.core.ServerClock;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.state.InnerState;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;

/** Persistent vanilla forceload follows the authorized strong dildo and its chest edge cells. */
public final class StrongPortChunkLoads {
    private StrongPortChunkLoads() {}

    public static void tick(MinecraftServer server) {
        if (ServerClock.now() % 20 != 1) return;
        InnerState state = InnerState.get(server.method_30002());
        Set<InnerState.ForcedPortChunk> desired = new HashSet<>();
        for (var entry : state.allBindings().entrySet()) {
            InnerState.BindingSet bindings = entry.getValue();
            InnerState.Binding binding = bindings.bindings.get(bindings.strongDildoUuid);
            if (binding == null || !binding.authorized) continue;
            class_5321<class_1937> key = class_5321.method_29179(class_7924.field_41223, binding.dimId);
            if (DynamicDimensionManager.isInnerWorld(key)) continue;
            class_3218 world = server.method_3847(key);
            class_1297 live = world == null ? null : world.method_66347(binding.dildoUuid);
            int x = (int) Math.floor(live == null ? binding.x : live.method_23317());
            int z = (int) Math.floor(live == null ? binding.z : live.method_23321());
            add(desired, binding.dimId, x, z);
            add(desired, binding.dimId, x - 1, z);
            add(desired, binding.dimId, x + 1, z);
            add(desired, binding.dimId, x, z - 1);
            add(desired, binding.dimId, x, z + 1);
        }
        Set<InnerState.ForcedPortChunk> owned = new HashSet<>(state.ownedForcedPortChunks());
        for (var chunk : new HashSet<>(owned)) {
            if (desired.contains(chunk)) continue;
            class_3218 world = world(server, chunk);
            if (world == null) continue;
            world.method_17988(chunk.x(), chunk.z(), false);
            owned.remove(chunk);
        }
        for (var chunk : desired) {
            class_3218 world = world(server, chunk);
            if (world == null) continue;
            long packed = new class_1923(chunk.x(), chunk.z()).method_8324();
            if (!world.method_17984().contains(packed)) {
                if (world.method_17988(chunk.x(), chunk.z(), true)) owned.add(chunk);
            }
            // Existing user/other-system forceload is never adopted as mod-owned.
        }
        state.setOwnedForcedPortChunks(owned);
    }

    private static class_3218 world(MinecraftServer server, InnerState.ForcedPortChunk chunk) {
        return server.method_3847(class_5321.method_29179(class_7924.field_41223, chunk.dim()));
    }

    private static void add(Set<InnerState.ForcedPortChunk> chunks, class_2960 dim, int x, int z) {
        chunks.add(new InnerState.ForcedPortChunk(dim, x >> 4, z >> 4));
    }
}
