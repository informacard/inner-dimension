package com.innerdimension.domain.world;

import com.innerdimension.domain.ability.D62;
import net.minecraft.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * D62 spatial disturbance: ordinary teleports of an entity that is an enemy of a covering
 * session (at the origin or at the destination) are refused.
 *
 * <p>The guard is deliberately scoped. It is consulted only from the verified
 * {@code class_1297.method_48105} teleport entry point; raw third-party
 * {@code setPosition}/{@code refreshPositionAndAngles} writes are out of scope because
 * cancelling them would freeze ordinary physics movement. Whitelisted callers mark
 * themselves with {@link #push}/{@link #pop}, so domain evacuation, the mod's own safe
 * relocation, NoN migration, administrator/system travel and boss stage resets all pass
 * through untouched.</p>
 */
public final class DomainTeleportGuard {
    /** Why the current teleport must not be filtered. */
    public enum Reason { DOMAIN_EVACUATION, SAFE_TELEPORT, NON_MIGRATION, SYSTEM, BOSS_RESET }

    private static final ThreadLocal<Deque<Reason>> ALLOW = ThreadLocal.withInitial(ArrayDeque::new);

    private DomainTeleportGuard() {}

    /** Marks every teleport performed before the matching {@link #pop()} as whitelisted. */
    public static void push(Reason reason) {
        if (reason != null) ALLOW.get().push(reason);
    }

    public static void pop() {
        Deque<Reason> stack = ALLOW.get();
        if (!stack.isEmpty()) stack.pop();
    }

    public static boolean allowed() {
        return !ALLOW.get().isEmpty();
    }

    public static Reason current() {
        return ALLOW.get().peek();
    }

    /** Convenience form of {@link #blocked(class_1297, class_1937, double, double, double)}. */
    public static boolean blocked(class_1297 entity, double x, double y, double z) {
        return blocked(entity, entity == null ? null : entity.method_73183(), x, y, z);
    }

    /**
     * TRUE when D62 must cancel this teleport. {@code destinationWorld} is the world the
     * entity is being moved into, which may differ from its current one.
     */
    public static boolean blocked(class_1297 entity, class_1937 destinationWorld, double x, double y, double z) {
        if (entity == null || allowed()) return false;
        return com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(entity)||com.innerdimension.domain.ability.D77.locked(entity)||D62.cancelsTeleport(entity, destinationWorld, x, y, z);
    }
}
