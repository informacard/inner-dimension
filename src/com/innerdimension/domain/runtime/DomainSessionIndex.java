package com.innerdimension.domain.runtime;

import com.innerdimension.projection.RoomProjectionEntity;
import net.minecraft.*;

import java.util.*;

/**
 * Spatial lookup of the sessions that currently cover an entity, a point or a block.
 * Overlapping domains are all returned; callers decide how to combine them (see the
 * combination table in the blueprint).
 */
public final class DomainSessionIndex {
    private static final Map<class_1937, List<DomainSession>> BY_WORLD = new WeakHashMap<>();
    private static final Map<UUID, DomainSession> BY_OWNER = new HashMap<>();
    private static final Map<RoomProjectionEntity, DomainSession> BY_SHELL = new IdentityHashMap<>();

    private DomainSessionIndex() {}

    public static synchronized void add(DomainSession session) {
        BY_WORLD.computeIfAbsent(session.world, w -> new ArrayList<>()).add(session);
        BY_OWNER.put(session.ownerId, session);
        BY_SHELL.put(session.shell, session);
    }

    public static synchronized void remove(DomainSession session) {
        if (session == null) return;
        List<DomainSession> list = BY_WORLD.get(session.world);
        if (list != null) list.remove(session);
        if (BY_OWNER.get(session.ownerId) == session) BY_OWNER.remove(session.ownerId);
        BY_SHELL.remove(session.shell);
    }

    public static synchronized void clear() {
        BY_WORLD.clear();
        BY_OWNER.clear();
        BY_SHELL.clear();
    }

    public static synchronized List<DomainSession> all(class_1937 world) {
        List<DomainSession> list = BY_WORLD.get(world);
        return list == null ? List.of() : List.copyOf(list);
    }

    /** Every session in every world, for the global tick loop and lifecycle cleanup. */
    public static synchronized List<DomainSession> sessions() {
        if (BY_SHELL.isEmpty()) return List.of();
        return List.copyOf(new LinkedHashSet<>(BY_SHELL.values()));
    }

    public static synchronized DomainSession byOwner(UUID owner) { return BY_OWNER.get(owner); }

    public static synchronized DomainSession bySession(UUID sessionId) {
        if (sessionId == null) return null;
        for (DomainSession session : BY_SHELL.values())
            if (session.sessionId.equals(sessionId)) return session;
        return null;
    }

    public static synchronized DomainSession byShell(RoomProjectionEntity shell) { return BY_SHELL.get(shell); }

    /** Sessions whose interior overlaps the entity's collision box. */
    public static List<DomainSession> covering(class_1297 entity) {
        if (entity == null || com.innerdimension.boss.BossDomains.domainImmune(entity)) return List.of();
        List<DomainSession> out=new ArrayList<>(2);
        for(var s:all(entity.method_73183()))if(!s.ended()&&s.stable()&&s.contains(entity))out.add(s);
        return out;
    }

    public static List<DomainSession> covering(class_1937 world, class_238 box) {
        List<DomainSession> out = new ArrayList<>(2);
        for (DomainSession s : all(world)) if (!s.ended() && s.stable() && s.contains(box)) out.add(s);
        return out;
    }

    public static List<DomainSession> coveringPoint(class_1937 world, class_243 point) {
        List<DomainSession> out = new ArrayList<>(2);
        for (DomainSession s : all(world))
            if (!s.ended() && s.stable() && s.containsPoint(point.field_1352, point.field_1351, point.field_1350)) out.add(s);
        return out;
    }

    public static List<DomainSession> coveringBlock(class_1937 world, class_2338 pos) {
        List<DomainSession> out = new ArrayList<>(2);
        for (DomainSession s : all(world)) if (!s.ended() && s.stable() && s.intersectsBlock(pos)) out.add(s);
        return out;
    }

    /** Sessions where the given entity is the owner, stable or not. */
    public static List<DomainSession> ownedBy(class_1937 world, UUID owner) {
        DomainSession s = byOwner(owner);
        return s == null || s.world != world ? List.of() : List.of(s);
    }

    /** Every session covering the entity that also carries the given ability code. */
    public static List<DomainSession> coveringWith(class_1297 entity, String code) {
        List<DomainSession> out = new ArrayList<>(2);
        for (DomainSession s : covering(entity)) if (s.hasCode(code)) out.add(s);
        return out;
    }

    public static boolean anyCovering(class_1297 entity, String code) { return !coveringWith(entity, code).isEmpty(); }

    /** True when at least one covering session carries the ability, and the entity is in its world. */
    public static boolean covered(class_1297 entity) { return !covering(entity).isEmpty(); }
}
