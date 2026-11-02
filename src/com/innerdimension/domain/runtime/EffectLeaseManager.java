package com.innerdimension.domain.runtime;

import net.minecraft.*;

import java.util.*;

/**
 * Source-tracked status-effect leases.
 *
 * <p>Each contribution is identified by (session, ability). The applied instance is the
 * merge of every contribution plus whatever external instance existed before the first
 * contribution was recorded. Revoking one contribution removes only that contribution:
 * the external amplitude/duration survives, and if a third party changed the effect while
 * we held it, the leased instance is not blindly cleared.</p>
 */
public final class EffectLeaseManager {
    private record Source(UUID sessionId, String code) {}
    private record Contribution(UUID sessionId, String code, int amplifier, long expiresAt) {}

    private static final class Entry {
        boolean externalKnown;
        int externalAmplifier;
        long externalRemaining;
        boolean externalAmbient, externalParticles, externalIcon;
        final Map<Source, Contribution> contributions = new LinkedHashMap<>();
        boolean applied;
    }

    private static final Map<class_1309, Map<class_6880<class_1291>, Entry>> LEASES = new WeakHashMap<>();
    /** session -> entities it leased anything on, so a session end can revoke precisely. */
    private static final Map<UUID, Set<class_1309>> BY_SESSION = new HashMap<>();

    private static final ThreadLocal<Boolean> APPLYING=ThreadLocal.withInitial(()->false);
    public static boolean applying(){return APPLYING.get();}
    private static boolean leasedAdd(class_1309 entity,class_1293 effect){boolean before=APPLYING.get();APPLYING.set(true);try{return entity.method_6092(effect);}finally{APPLYING.set(before);}}
    private EffectLeaseManager() {}

    public static class_6880<class_1291> effect(String id) {
        String namespace = "minecraft", path = id;
        int colon = id.indexOf(':');
        if (colon >= 0) { namespace = id.substring(0, colon); path = id.substring(colon + 1); }
        return class_7923.field_41174.method_10223(class_2960.method_60655(namespace, path)).orElse(null);
    }

    /** Register or refresh one session's contribution and apply the merged instance. */
    public static boolean apply(class_1309 entity, class_6880<class_1291> holder, String code,
                                UUID sessionId, int duration, int amplifier, long now) {
        if (entity == null || holder == null || entity.method_7325()) return false;
        Map<class_6880<class_1291>, Entry> byEffect = LEASES.computeIfAbsent(entity, e -> new HashMap<>());
        Entry entry = byEffect.computeIfAbsent(holder, h -> new Entry());
        snapshotExternal(entity, holder, entry, now);
        entry.contributions.put(new Source(sessionId, code), new Contribution(sessionId, code, amplifier, now + duration));
        BY_SESSION.computeIfAbsent(sessionId, k -> Collections.newSetFromMap(new WeakHashMap<>())).add(entity);
        return flush(entity, holder, entry, now);
    }

    /** Convenience for the very common "one session, one ability" case. */
    public static boolean apply(class_1309 entity, String effectId, String code, UUID sessionId,
                                int duration, int amplifier, long now) {
        return apply(entity, effect(effectId), code, sessionId, duration, amplifier, now);
    }

    private static void snapshotExternal(class_1309 entity, class_6880<class_1291> holder, Entry entry, long now) {
        if (entry.externalKnown) return;
        entry.externalKnown = true;
        class_1293 existing = entity.method_6112(holder);
        if (existing == null) return;
        entry.externalAmplifier = existing.method_5578();
        entry.externalRemaining = existing.method_5584();
        entry.externalAmbient = existing.method_5591();
        entry.externalParticles = existing.method_5581();
        entry.externalIcon = existing.method_5592();
    }

    private record Merged(int amplifier, int duration) {}

    private static Merged merge(Entry entry, long now) {
        entry.contributions.values().removeIf(c -> c.expiresAt() <= now);
        int amplifier = entry.externalRemaining > 0 ? entry.externalAmplifier : Integer.MIN_VALUE;
        long desired = entry.externalRemaining;
        for (Contribution c : entry.contributions.values()) {
            // A weaker long-lived effect must not extend a stronger temporary effect.
            if (c.amplifier() > amplifier) {
                amplifier = c.amplifier();
                desired = c.expiresAt() - now;
            } else if (c.amplifier() == amplifier) {
                desired = Math.max(desired, c.expiresAt() - now);
            }
        }
        amplifier = Math.max(0, amplifier);
        desired = Math.max(1, Math.min(Integer.MAX_VALUE, desired));
        return new Merged(amplifier, (int) desired);
    }

    private static boolean flush(class_1309 entity, class_6880<class_1291> holder, Entry entry, long now) {
        Merged merged = merge(entry, now);
        if (entry.contributions.isEmpty()) return false;
        int amplifier = merged.amplifier();
        int desired = merged.duration();
        boolean ambient = entry.externalKnown && entry.externalAmbient;
        boolean marker=holder.comp_349()==com.innerdimension.effect.ModStatusEffects.UPSIDE_DOWN;
        boolean particles = !marker&&(entry.externalRemaining <= 0 || entry.externalParticles);
        boolean icon = !marker&&(entry.externalRemaining <= 0 || entry.externalIcon);
        class_1293 current = entity.method_6112(holder);
        if (current != null && current.method_5578() == amplifier && current.method_5584() > desired / 2) return true;
        // Vanilla addStatusEffect will not downgrade an existing stronger instance.
        if (current != null && current.method_5578() > amplifier) entity.method_6016(holder);
        leasedAdd(entity,new class_1293(holder, (int) desired, amplifier, ambient, particles, icon));
        entry.applied = true;
        return true;
    }

    /** Withdraw one session's contribution to one effect; external state is restored. */
    public static void revoke(class_1309 entity, class_6880<class_1291> holder, UUID sessionId, long now) {
        revoke(entity, holder, sessionId, null, now);
    }

    /** Withdraw only one ability, preserving other abilities in the same expansion. */
    public static void revoke(class_1309 entity, class_6880<class_1291> holder, UUID sessionId, String code, long now) {
        if (entity == null || holder == null) return;
        Map<class_6880<class_1291>, Entry> byEffect = LEASES.get(entity);
        if (byEffect == null) return;
        Entry entry = byEffect.get(holder);
        if (entry == null) return;
        entry.contributions.keySet().removeIf(source -> source.sessionId().equals(sessionId)
                && (code == null || code.equals(source.code())));
        if (entry.contributions.isEmpty()) {
            byEffect.remove(holder);
            if (byEffect.isEmpty()) LEASES.remove(entity);
            restore(entity, holder, entry);
            return;
        }
        flush(entity, holder, entry, now);
    }

    /** Removes every contribution to one effect regardless of session. */
    public static void revokeAll(class_1309 entity, class_6880<class_1291> holder) {
        Map<class_6880<class_1291>, Entry> byEffect = LEASES.get(entity);
        if (byEffect == null) return;
        Entry entry = byEffect.remove(holder);
        if (byEffect.isEmpty()) LEASES.remove(entity);
        if (entry != null) restore(entity, holder, entry);
    }

    private static void restore(class_1309 entity, class_6880<class_1291> holder, Entry entry) {
        if (entity.method_7325() || entity.method_73183().method_8608()) return;
        if (entry.externalKnown && entry.externalRemaining > 0) {
            // Rebuild the external instance instead of clearing the effect outright.
            if (entry.applied) entity.method_6016(holder);
            leasedAdd(entity,new class_1293(holder, (int) Math.min(Integer.MAX_VALUE, entry.externalRemaining),
                    entry.externalAmplifier, entry.externalAmbient, entry.externalParticles, entry.externalIcon));
            return;
        }
        if (!entry.applied) return;
        entity.method_6016(holder);
    }

    /** Withdraw everything one session ever applied. Called on session end. */
    public static void revokeSession(UUID sessionId, long now) {
        Set<class_1309> entities = BY_SESSION.remove(sessionId);
        if (entities == null) return;
        for (class_1309 entity : entities) {
            if (entity == null) continue;
            Map<class_6880<class_1291>, Entry> byEffect = LEASES.get(entity);
            if (byEffect == null) continue;
            for (class_6880<class_1291> holder : new ArrayList<>(byEffect.keySet())) revoke(entity, holder, sessionId, now);
        }
    }

    /** A simple domain withdraws foreign domain contributions, preserving external potions. */
    public static void shieldEntity(class_1309 entity,long now) {
        Map<class_6880<class_1291>,Entry> effects=LEASES.get(entity);
        if(effects==null)return;
        for(var holder:new ArrayList<>(effects.keySet())) {
            var entry=effects.get(holder);if(entry==null)continue;
            var sessions=entry.contributions.values().stream().map(Contribution::sessionId).distinct().toList();
            for(var session:sessions)revoke(entity,holder,session,now);
        }
    }

    /** Decline recorded external durations so a restored effect does not outlive its real lifetime. */
    public static void tickExternal() {
        long now = com.innerdimension.domain.combat.DomainDamagePipeline.now();
        for (var entityIterator = LEASES.entrySet().iterator(); entityIterator.hasNext();) {
            var entityEntry = entityIterator.next();
            class_1309 entity = entityEntry.getKey();
            for (var effectIterator = entityEntry.getValue().entrySet().iterator(); effectIterator.hasNext();) {
                var effectEntry = effectIterator.next();
                Entry entry = effectEntry.getValue();
                if (entry.externalRemaining > 0) entry.externalRemaining--;
                boolean expired = entry.contributions.values().removeIf(c -> c.expiresAt() <= now);
                if (entry.contributions.isEmpty()) {
                    effectIterator.remove();
                    restore(entity, effectEntry.getKey(), entry);
                } else if (expired) {
                    flush(entity, effectEntry.getKey(), entry, now);
                }
            }
            if (entityEntry.getValue().isEmpty()) entityIterator.remove();
        }
    }

    public static void revokeOutside(DomainSession session,long now){var entities=BY_SESSION.get(session.sessionId);if(entities==null)return;for(var entity:new ArrayList<>(entities)){if(session.contains(entity))continue;var effects=LEASES.get(entity);if(effects!=null)for(var holder:new ArrayList<>(effects.keySet()))revoke(entity,holder,session.sessionId,now);}}
    public static void clear() { LEASES.clear(); BY_SESSION.clear(); }
}
