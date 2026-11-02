package com.innerdimension.domain.runtime;

import net.minecraft.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The single relationship service. Nothing in the ability layer may guess relations from
 * entity names or from {@code instanceof Monster}; every question goes through here.
 *
 * <ul>
 *   <li>SELF  – the domain owner.</li>
 *   <li>ALLY  – the owner's tamed pets, leashed mobs, team-mates and explicitly authorised players.</li>
 *   <li>NEUTRAL – ordinary visitors. Visiting players are neutral, not enemies.</li>
 *   <li>ENEMY – hostile mobs that are not allied.</li>
 *   <li>BOSS  – a hostile mob that must not be hard-controlled (no AI pinning, no levitation).</li>
 * </ul>
 */
public final class DomainRelations {
    public enum Relation { SELF, ALLY, NEUTRAL, ENEMY, BOSS }

    private static final Set<String> BOSSES = Set.of(
            "minecraft:wither", "minecraft:ender_dragon", "minecraft:warden", "minecraft:elder_guardian");

    /** owner -> players explicitly authorised to be treated as allies inside that owner's domain. */
    private static final Map<UUID, Set<UUID>> AUTHORIZED = new ConcurrentHashMap<>();

    private DomainRelations() {}

    public static void authorize(UUID owner, UUID ally) {
        AUTHORIZED.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()).add(ally);
    }

    public static void revoke(UUID owner, UUID ally) {
        Set<UUID> set = AUTHORIZED.get(owner);
        if (set != null) set.remove(ally);
    }

    public static boolean authorized(UUID owner, UUID candidate) {
        Set<UUID> set = AUTHORIZED.get(owner);
        return set != null && set.contains(candidate);
    }

    public static void clear() { AUTHORIZED.clear(); }

    public static Relation of(DomainSession session, class_1297 entity) {
        if (session == null || entity == null) return Relation.NEUTRAL;
        if (entity.method_5667().equals(session.ownerId)) return Relation.SELF;
        if (com.innerdimension.domain.SimpleDomain.forcedEnemy(session.ownerId,entity.method_5667()) || com.innerdimension.domain.SimpleDomain.locked(session.ownerId,entity)) return Relation.ENEMY;
        if (entity instanceof class_1309 living) {
            if (isAllyOf(session, living)) return Relation.ALLY;
            if (isBoss(living)) return Relation.BOSS;
            if (living.method_5864().method_5891() == class_1311.field_6302) return Relation.ENEMY;
        }
        return Relation.NEUTRAL;
    }

    /** Relation of an entity towards a specific player, for pipelines without a session. */
    public static boolean isAllyOf(DomainSession session, class_1309 entity) {
        if (entity == null) return false;
        if (entity.method_5667().equals(session.ownerId)) return true;
        if(entity.method_5752().contains(com.innerdimension.domain.ability.DomainSummons.OWNER+session.ownerId))return true;
        if (entity instanceof class_1321 tame) {
            var owner = tame.method_35057();
            if (owner != null && owner.method_5667().equals(session.ownerId)) return true;
        }
        if (entity.method_5854() != null && entity.method_5854().method_5667().equals(session.ownerId)) return true;
        if (entity instanceof class_1657 player) {
            if (authorized(session.ownerId, player.method_5667())) return true;
            var owner = session.ownerEntity();
            if (owner != null && sameTeam(owner, player)) return true;
        }
        return false;
    }

    public static boolean sameTeam(class_1297 a, class_1297 b) {
        class_268 ta = a.method_5781(), tb = b.method_5781();
        return ta != null && tb != null && ta.method_1197().equals(tb.method_1197());
    }

    public static boolean isBoss(class_1297 entity) { return BOSSES.contains(typeId(entity)); }

    public static boolean isEnemy(DomainSession session, class_1297 entity) {
        Relation r = of(session, entity);
        return r == Relation.ENEMY || r == Relation.BOSS;
    }

    public static boolean isFriendly(DomainSession session, class_1297 entity) {
        Relation r = of(session, entity);
        return r == Relation.SELF || r == Relation.ALLY;
    }

    /** True when no covering session considers the entity friendly, so it may be targeted. */
    public static boolean hostileToAny(List<DomainSession> sessions, class_1297 entity) {
        for (DomainSession s : sessions) if (isEnemy(s, entity)) return true;
        return false;
    }

    public static String typeId(class_1297 entity) {
        if (entity == null) return "";
        return class_7923.field_41177.method_10221(entity.method_5864()).toString();
    }

    public static String typeIdOf(DomainSession session) { return typeId(session.owner()); }

    public static boolean isPlayer(class_1297 entity) { return entity instanceof class_1657; }
}
