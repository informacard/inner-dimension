package com.innerdimension.domain.combat;

import net.minecraft.*;

/**
 * Registered damage categories. No ability may classify a hit by a substring of
 * {@code DamageSource.getMsgId()}; everything is a registry key or a vanilla damage tag.
 */
public final class DomainDamageTypes {
    public static final String NS = "inner_dimension";

    /* Vanilla tags, resolved through the damage-type registry. */
    public static final class_6862<class_8110> TAG_DROWNING = tag("minecraft:is_drowning");
    public static final class_6862<class_8110> TAG_FIRE = tag("minecraft:is_fire");
    public static final class_6862<class_8110> TAG_FALL = tag("minecraft:is_fall");
    public static final class_6862<class_8110> TAG_PROJECTILE = tag("minecraft:is_projectile");
    public static final class_6862<class_8110> TAG_FREEZING = tag("minecraft:is_freezing");
    public static final class_6862<class_8110> TAG_PLAYER_ATTACK = tag("minecraft:is_player_attack");

    /* Our own categories, declared in data/inner_dimension/damage_type/. */
    public static final String DROWNING = NS + ":domain_drowning";
    public static final String FREEZE = NS + ":domain_freeze";
    public static final String GROUND_SHOCK = NS + ":domain_ground_shock";
    public static final String GROUND_SHOCK_MAGIC = NS + ":domain_ground_shock_magic";
    public static final String SILENCE = NS + ":domain_silence";
    public static final String FELINE = NS + ":domain_feline";
    public static final String HOOF = NS + ":domain_hoof";
    public static final String SPIT = NS + ":domain_spit";
    public static final String SPIT_MAGIC = NS + ":domain_spit_magic";
    public static final String DRAGON_BREATH = NS + ":domain_dragon_breath";
    public static final String REFLECTED = NS + ":domain_reflected";
    public static final String REFLECTED_MAGIC = NS + ":domain_reflected_magic";
    public static final String FLYING_SLASH = NS + ":domain_flying_slash";
    public static final String SHRINE_SLASH = NS + ":domain_shrine_slash";
    public static final String GENERIC = NS + ":domain_generic";

    private DomainDamageTypes() {}

    private static class_6862<class_8110> tag(String id) {
        int colon = id.indexOf(':');
        return class_6862.method_40092(class_7924.field_42534,
                class_2960.method_60655(id.substring(0, colon), id.substring(colon + 1)));
    }

    /** Resolves a damage type holder from the server's dynamic registry, or null. */
    public static class_6880<class_8110> holder(class_1937 world, String id) {
        int colon = id.indexOf(':');
        String namespace = colon < 0 ? "minecraft" : id.substring(0, colon);
        String path = colon < 0 ? id : id.substring(colon + 1);
        return world.method_30349().method_30530(class_7924.field_42534)
                .method_10223(class_2960.method_60655(namespace, path)).orElse(null);
    }

    public static class_1282 source(class_1937 world, String id, class_1297 attacker, class_1297 direct) {
        class_6880<class_8110> holder = holder(world, id);
        if (holder == null) holder = holder(world, GENERIC);
        if (holder == null) return world.method_48963().method_48794();
        return new class_1282(holder, attacker, direct);
    }

    public static boolean hasTag(class_1282 source, class_6862<class_8110> tag) {
        return source != null && source.method_48789(tag);
    }

    public static String keyOf(class_1282 source) {
        if (source == null) return "";
        class_6880<class_8110> holder = source.method_48793();
        if (holder == null) return "";
        return holder.method_40230().map(key -> key.method_29177().toString()).orElse("");
    }

    /** True for any damage this mod's own pipelines generate. */
    public static boolean isDomainDamage(class_1282 source) {
        return keyOf(source).startsWith(NS + ":");
    }

    public static boolean isDrowning(class_1282 source) {
        return hasTag(source, TAG_DROWNING) || keyOf(source).equals(DROWNING);
    }

    public static boolean isFire(class_1282 source) {
        return hasTag(source, TAG_FIRE) && !isLava(source);
    }

    /** Lava is a member of the fire tag, but D51 must never absorb it. */
    public static boolean isLava(class_1282 source) {
        String key = keyOf(source);
        return key.equals("minecraft:lava") || key.equals("minecraft:hot_floor");
    }

    public static boolean isFall(class_1282 source) {
        return hasTag(source, TAG_FALL);
    }

    public static boolean isProjectile(class_1282 source) {
        return hasTag(source, TAG_PROJECTILE);
    }

    public static boolean isReflected(class_1282 source) {
        String key = keyOf(source);
        return key.equals(REFLECTED) || key.equals(REFLECTED_MAGIC);
    }
}
