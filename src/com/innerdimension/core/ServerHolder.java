package com.innerdimension.core;

import net.minecraft.server.MinecraftServer;

/**
 * Holds the running server so static helpers can reach the overworld {@code InnerState}.
 *
 * <p>Several checks start from an entity UUID rather than from a world - an unlock click, a pending
 * output request, a lock lookup for a doll whose chunk is not loaded. They all need "the server that
 * is running right now", and the only authoritative place for that is the lifecycle events. The
 * reference is set on {@code SERVER_STARTED} and cleared on {@code SERVER_STOPPING}, so nothing
 * keeps a dead server alive between sessions.</p>
 */
public final class ServerHolder {
    private static MinecraftServer server;

    private ServerHolder() {
    }

    public static void set(MinecraftServer running) {
        server = running;
    }

    public static void clear() {
        server = null;
    }

    public static MinecraftServer server() {
        return server;
    }
}
