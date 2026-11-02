package com.innerdimension.domain.runtime;

import net.minecraft.class_3222;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hand-off buffer between the validated network intents and the abilities that consume them.
 *
 * <p>{@link com.innerdimension.network.DomainAbilityInputPayload} is the only writer; D06 is
 * the only reader. An intent is stored per <em>session</em> (never per owner UUID) and
 * expires after {@link #TTL_TICKS}, so a dash asked for by a client can never survive into the
 * next expansion, a slot switch or a re-login.</p>
 *
 * <p>All access is on the server thread; the maps are synchronised defensively because the
 * receiver may run before the payload handler is re-scheduled onto the server thread.</p>
 */
public final class DomainAbilityInput {
    /** How long an accepted intent stays valid. A dash lasts 4 ticks. */
    public static final long TTL_TICKS = 20;

    private record Dash(int direction, long tick) {}


    private static final Map<UUID, Dash> DASH = new HashMap<>();


    private DomainAbilityInput() {}

    /** Records an accepted D06 dash; {@code direction} 0..3 = W/A/S/D. */
    public static synchronized void submitDash(class_3222 player, int direction) {
        if (player == null) return;
        DomainSession session = DomainSessionIndex.byOwner(player.method_5667());
        if (session == null) return;
        long now = DomainRuntime.now();
        prune(now);
        DASH.put(session.sessionId, new Dash(direction, now));
    }

    /**
     * Consumes one pending dash direction for this session.
     *
     * @return 0..3 (W/A/S/D), or -1 when no dash was asked for or the intent expired
     */
    public static synchronized int consumeDashDirection(DomainSession session, long now) {
        if (session == null) return -1;
        Dash dash = DASH.remove(session.sessionId);
        if (dash == null || now - dash.tick() > TTL_TICKS) return -1;
        return dash.direction() < 0 || dash.direction() > 3 ? -1 : dash.direction();
    }

    /** Drops an ended session so a later expansion cannot inherit its intents. */
    public static synchronized void forget(DomainSession session) {
        if (session == null) return;
        DASH.remove(session.sessionId);

    }

    public static synchronized void clear() {
        DASH.clear();

    }

    private static void prune(long now) {
        DASH.entrySet().removeIf(entry -> now - entry.getValue().tick() > TTL_TICKS);

    }
}

