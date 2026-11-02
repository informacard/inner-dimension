package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

import java.util.*;

/**
 * Shared plumbing for the D01..D23 handlers.
 *
 * <p>This is deliberately not a service: it owns no state of its own. Every timer, set or
 * map it returns is stored inside the {@link DomainSession} that asked for it, so closing
 * and reopening a projection can never leak an ability counter into the next expansion.
 * Nothing here invents a second geometry, AI or damage system — those all go through the
 * frozen services in {@code DOMAIN_ABILITY_API.md}.</p>
 */
final class D01D23Support {
    private D01D23Support() {}

    /* ------------------------------------------------------------- sessions */

    static boolean live(DomainSession s) {
        return s != null && s.stable() && !s.ended();
    }

    /** The session owner, but only while it really stands inside the fixed interior. */
    static class_3222 ownerInside(DomainSession s) {
        if (!live(s)) return null;
        class_3222 owner = s.owner();
        return owner != null && s.contains(owner) ? owner : null;
    }

    static List<class_1309> friendlyInside(DomainSession s) {
        List<class_1309> out = new ArrayList<>();
        if (!live(s)) return out;
        for (class_1309 entity : s.livingInside()) if (DomainRelations.isFriendly(s, entity)) out.add(entity);
        return out;
    }

    static List<class_1309> enemyInside(DomainSession s) {
        List<class_1309> out = new ArrayList<>();
        if (!live(s)) return out;
        for (class_1309 entity : s.livingInside())
            if (!entity.method_5667().equals(s.ownerId) && DomainRelations.isEnemy(s, entity)) out.add(entity);
        return out;
    }

    /** A tamed pet that really belongs to this session's owner. */
    static boolean isPet(DomainSession s, class_1297 entity) {
        if (!(entity instanceof class_1321 tame)) return false;
        class_1309 tamer = tame.method_35057();
        return tamer != null && tamer.method_5667().equals(s.ownerId);
    }

    /* -------------------------------------------------------------- timers */

    @SuppressWarnings("unchecked")
    private static Set<UUID> knownSet(DomainSession s, String key) {
        Object raw = s.states().get(key);
        if (raw instanceof Set<?> set) return (Set<UUID>) set;
        Set<UUID> fresh = new HashSet<>();
        s.states().put(key, fresh);
        return fresh;
    }

    /** Drops targets that stopped being eligible, so a re-entry restarts the full interval. */
    static void retainEligible(DomainSession s, String key, Collection<UUID> eligible) {
        knownSet(s, key).retainAll(eligible);
    }

    /**
     * True every {@code interval} ticks, but the first execution only happens one full
     * interval after the target became eligible ("取得资格 N tick 后"), never immediately.
     */
    static boolean dueAfterEligibility(DomainSession s, String key, UUID target, long interval, long now) {
        Set<UUID> known = knownSet(s, key);
        if (known.add(target)) {
            s.arm(key, target, now);
            return false;
        }
        if (!s.due(key, target, interval, now)) return false;
        s.arm(key, target, now);
        return true;
    }

    /** True immediately on eligibility, then once per {@code interval}. */
    static boolean periodFromEligibility(DomainSession s, String key, UUID target, long interval, long now) {
        Set<UUID> known = knownSet(s, key);
        if (known.add(target)) {
            s.arm(key, target, now);
            return true;
        }
        if (!s.due(key, target, interval, now)) return false;
        s.arm(key, target, now);
        return true;
    }

    /* ---------------------------------------------------------------- state */

    @SuppressWarnings("unchecked")
    static <V> Map<UUID, V> uuidMap(DomainSession s, String key) {
        Object raw = s.states().get(key);
        if (raw instanceof Map<?, ?> map) return (Map<UUID, V>) map;
        Map<UUID, V> fresh = new HashMap<>();
        s.states().put(key, fresh);
        return fresh;
    }

    /** Stable long form of a session id, for the client sync payload. */
    static long sessionKey(DomainSession s) {
        return s.sessionId.getMostSignificantBits() ^ s.sessionId.getLeastSignificantBits();
    }

    /* ------------------------------------------------------------ registry */

    static class_1792 item(String id) {
        class_2960 key = class_2960.method_60655(namespace(id), path(id));
        return class_7923.field_41178.method_10223(key).map(class_6880::comp_349).orElse(null);
    }

    static class_2248 block(String id) {
        class_2960 key = class_2960.method_60655(namespace(id), path(id));
        return class_7923.field_41175.method_10223(key).map(class_6880::comp_349).orElse(null);
    }

    private static String namespace(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? "minecraft" : id.substring(0, colon);
    }

    private static String path(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    /* ----------------------------------------------------------- inventory */

    /** Removes exactly one of the item from the player's main inventory. */
    static boolean takeOne(class_3222 owner, class_1792 wanted) {
        if (owner == null || wanted == null) return false;
        class_1661 inventory = owner.method_31548();
        for (int slot = 0; slot < inventory.method_5439(); slot++) {
            class_1799 stack = inventory.method_5438(slot);
            if (stack.method_7960() || stack.method_7909() != wanted) continue;
            stack.method_7939(stack.method_7947() - 1);
            if (stack.method_7960()) inventory.method_5447(slot, class_1799.field_8037);
            return true;
        }
        return false;
    }

    /**
     * Inserts into the real inventory and drops only what genuinely did not fit. The
     * {@code remainder} is exactly what vanilla's insertStack left behind.
     */
    static void give(class_3222 owner, DomainSession s, class_2338 pos, class_1799 stack) {
        if (owner == null || stack == null || stack.method_7960()) return;
        class_1799 remainder = stack.method_7972();
        owner.method_31548().method_7394(remainder);
        if (remainder.method_7960()) return;
        if (pos != null && s != null) class_2248.method_9577(s.world, pos, remainder);
        else owner.method_7328(remainder, false);
    }

    /* --------------------------------------------------------------- misc */

    /** True when the entity is neither walking nor being pushed up: the D10/D14 stillness test. */
    static boolean still(class_1297 entity) {
        class_243 velocity = entity.method_18798();
        return Math.abs(velocity.field_1352) < 0.005 && Math.abs(velocity.field_1350) < 0.005
                && Math.abs(velocity.field_1351) < 0.02;
    }

    static double horizontalDistance(class_1297 a, class_1297 b) {
        double dx = a.method_23317() - b.method_23317();
        double dz = a.method_23321() - b.method_23321();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Block position of an entity, for world transactions / drop spawning. */
    static class_2338 blockPos(class_1297 entity) {
        return class_2338.method_49637(entity.method_23317(), entity.method_23318(), entity.method_23321());
    }

    static class_5819 random(DomainSession s) {
        return s.world.method_8409();
    }
}
