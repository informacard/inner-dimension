package com.innerdimension.domain.runtime;

import net.minecraft.*;

import java.util.*;

/**
 * Attribute-modifier leases keyed by our own modifier id. Modifiers are added when a
 * session first grants them and removed only by that same session, so foreign modifiers,
 * other domains and re-entrant ticks never fight over the attribute's value.
 */
public final class AttributeLeaseManager {
    private record Lease(UUID sessionId, String code, class_6880<class_1320> attribute,
                         class_2960 id, double amount, class_1322.class_1323 operation) {}
    private record Source(UUID sessionId, String code) {}

    private static final Map<class_1309, Map<class_2960, Map<Source, Lease>>> LEASES = new WeakHashMap<>();
    private static final Map<UUID, Set<class_1309>> BY_SESSION = new HashMap<>();

    private AttributeLeaseManager() {}

    public static class_2960 id(String path) { return class_2960.method_60655("inner_dimension", path); }

    /**
     * Idempotent grant/withdraw. Only our own modifier id is ever touched.
     */
    public static void apply(class_1309 entity, class_6880<class_1320> attribute, class_2960 id,
                             double amount, class_1322.class_1323 operation,
                             boolean enabled, UUID sessionId, String code) {
        if (entity == null || attribute == null || entity.method_7325()) return;
        Map<class_2960, Map<Source, Lease>> leases = LEASES.computeIfAbsent(entity, e -> new HashMap<>());
        Map<Source, Lease> sources = leases.computeIfAbsent(id, key -> new LinkedHashMap<>());
        Source source = new Source(sessionId, code);
        Lease existing = strongest(sources);
        if (!enabled) {
            if (sources.remove(source) == null) return;
            if (sources.isEmpty()) leases.remove(id);
            if (leases.isEmpty()) LEASES.remove(entity);
            update(entity, id, existing, strongest(sources));
            return;
        }
        class_1324 instance = entity.method_5996(attribute);
        if (instance == null) return;
        sources.put(source, new Lease(sessionId, code, attribute, id, amount, operation));
        update(entity, id, existing, strongest(sources));
        BY_SESSION.computeIfAbsent(sessionId, k -> Collections.newSetFromMap(new WeakHashMap<>())).add(entity);
    }

    /** The same modifier is shared without stacking, but every source retains ownership. */
    private static Lease strongest(Map<Source, Lease> sources) {
        return sources.values().stream().max(Comparator.comparingDouble(lease -> Math.abs(lease.amount()))).orElse(null);
    }

    private static void update(class_1309 entity, class_2960 id, Lease before, Lease after) {
        if (before != null && after != null && before.attribute() == after.attribute()
                && before.amount() == after.amount() && before.operation() == after.operation()) return;
        if (before != null) {
            class_1324 instance = entity.method_5996(before.attribute());
            if (instance != null) instance.method_6200(id);
        }
        if (after != null) {
            class_1324 instance = entity.method_5996(after.attribute());
            if (instance != null) {
                instance.method_6200(id);
                instance.method_55696(new class_1322(id, after.amount(), after.operation()));
            }
        }
    }

    public static void apply(class_1309 entity, class_6880<class_1320> attribute, String idPath,
                             double amount, class_1322.class_1323 operation,
                             boolean enabled, UUID sessionId, String code) {
        apply(entity, attribute, id(idPath), amount, operation, enabled, sessionId, code);
    }

    /** Total of our own modifiers of a given operation on the entity, for reporting. */
    public static double sum(class_1309 entity, class_6880<class_1320> attribute, class_1322.class_1323 operation) {
        Map<class_2960, Map<Source, Lease>> leases = LEASES.get(entity);
        if (leases == null) return 0;
        double total = 0;
        for (Map<Source, Lease> sources : leases.values()) {
            Lease lease = strongest(sources);
            if (lease == null) continue;
            if (lease.attribute() == attribute && lease.operation() == operation) total += lease.amount();
        }
        return total;
    }

    /** Removes every modifier this session owns, clamping health when a max-health lease ends. */
    public static void revokeSession(UUID sessionId) {
        Set<class_1309> entities = BY_SESSION.remove(sessionId);
        if (entities == null) return;
        for (class_1309 entity : entities) {
            if (entity == null) continue;
            Map<class_2960, Map<Source, Lease>> leases = LEASES.get(entity);
            if (leases == null) continue;
            boolean healthTouched = false;
            for (var entry : new ArrayList<>(leases.entrySet())) {
                Map<Source, Lease> sources = entry.getValue();
                Lease before = strongest(sources);
                if (!sources.keySet().removeIf(source -> source.sessionId().equals(sessionId))) continue;
                if (sources.isEmpty()) leases.remove(entry.getKey());
                Lease after = strongest(sources);
                update(entity, entry.getKey(), before, after);
                if (before != null && before.attribute() == class_5134.field_23716) healthTouched = true;
            }
            if (leases.isEmpty()) LEASES.remove(entity);
            if (healthTouched && !entity.method_7325() && entity.method_6032() > entity.method_6063())
                entity.method_6033(entity.method_6063());
        }
    }

    public static void revokeOutside(DomainSession session){UUID sessionId=session.sessionId;var entities=BY_SESSION.get(sessionId);if(entities==null)return;
        for (class_1309 entity : entities) {
            if (entity == null || session.contains(entity)) continue;
            Map<class_2960, Map<Source, Lease>> leases = LEASES.get(entity);
            if (leases == null) continue;
            boolean healthTouched = false;
            for (var entry : new ArrayList<>(leases.entrySet())) {
                Map<Source, Lease> sources = entry.getValue();
                Lease before = strongest(sources);
                if (!sources.keySet().removeIf(source -> source.sessionId().equals(sessionId))) continue;
                if (sources.isEmpty()) leases.remove(entry.getKey());
                Lease after = strongest(sources);
                update(entity, entry.getKey(), before, after);
                if (before != null && before.attribute() == class_5134.field_23716) healthTouched = true;
            }
            if (leases.isEmpty()) LEASES.remove(entity);
            if (healthTouched && !entity.method_7325() && entity.method_6032() > entity.method_6063())
                entity.method_6033(entity.method_6063());
        }
    }

    /** Removes one session's lease on one entity regardless of attribute (world change etc.). */
    public static void revokeEntity(class_1309 entity) {
        Map<class_2960, Map<Source, Lease>> leases = LEASES.remove(entity);
        if (leases == null) return;
        for (Map<Source, Lease> sources : leases.values()) {
            Lease lease = strongest(sources);
            if (lease == null) continue;
            class_1324 instance = entity.method_5996(lease.attribute());
            if (instance != null) instance.method_6200(lease.id());
        }
    }

    public static void clear() { LEASES.clear(); BY_SESSION.clear(); }
}
