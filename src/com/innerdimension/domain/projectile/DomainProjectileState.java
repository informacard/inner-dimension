package com.innerdimension.domain.projectile;

import net.minecraft.*;

import java.util.*;

/**
 * Per-projectile ability state: which session fired it, which ability owns it, how many
 * times D65 has reflected it and whether it has already resolved a hit.
 */
public final class DomainProjectileState {
    public static final class State {
        public UUID sessionId;
        public UUID ownerId;
        public String code;
        public int reflections;
        public boolean consumed;
        public UUID target;
        public long born;
        public boolean homing;

        public boolean belongsTo(DomainSessionHost host) {
            return host != null && host.sessionId().equals(sessionId);
        }
    }

    /** Structural interface so this package does not depend on the runtime package. */
    public interface DomainSessionHost { UUID sessionId(); }

    private static final Map<class_1297, State> STATES = new WeakHashMap<>();

    private DomainProjectileState() {}

    public static State of(class_1297 projectile) {
        return STATES.computeIfAbsent(projectile, p -> new State());
    }

    public static State peek(class_1297 projectile) { return STATES.get(projectile); }

    public static void forget(class_1297 projectile) { if (projectile != null) STATES.remove(projectile); }

    public static void clear() { STATES.clear(); }

    /** Marks a projectile as fired by one session's ability. */
    public static State bind(class_1297 projectile, UUID sessionId, UUID ownerId, String code, long now) {
        State state = of(projectile);
        state.sessionId = sessionId;
        state.ownerId = ownerId;
        state.code = code;
        state.born = now;
        state.consumed = false;
        state.reflections = 0;
        return state;
    }
}
