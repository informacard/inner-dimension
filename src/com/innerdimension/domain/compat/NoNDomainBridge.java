package com.innerdimension.domain.compat;

import net.minecraft.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Every NoN call in the ability layer goes through here. Methods are verified once at
 * startup so a NoN update degrades to "effect unavailable" instead of an exception inside
 * the tick loop.
 */
public final class NoNDomainBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension.domain.non");
    private static boolean energy, pregnancy, skin, animation;
    private static boolean verified;

    private NoNDomainBridge() {}

    /** Resolves and records which NoN bridge surfaces are usable. */
    public static void verify() {
        if (verified) return;
        verified = true;
        energy = probe(() -> com.nonid.api.NonEntityApi.class.getMethod("getEnergy", class_1309.class));
        pregnancy = probe(() -> com.nonid.api.NonPlayerApi.class.getMethod("clearPregnancy", class_3222.class));
        skin = probe(() -> com.nonid.api.NonPlayerApi.class.getMethod("damageDestroyedSkin", class_3222.class, int.class));
        // Probe by name and arity, not by parameter types: NoN declares startMatched against
        // ServerWorld and its own MatchOptions/StartOptions records, and asking getMethod for
        // (World, List, Object, Object) fails there. That mismatch silently disabled every
        // animation this mod starts (the log line read animation=false), so the probe now only
        // checks that a four-argument startMatched exists.
        animation = probe(() -> {
            for (java.lang.reflect.Method method : com.nonid.api.animation.NonAnimationApi.class.getMethods())
                if (method.getName().equals("startMatched") && method.getParameterCount() == 4) return;
            throw new NoSuchMethodException("NonAnimationApi.startMatched/4");
        });
        LOGGER.info("NoN bridge: energy={} pregnancy={} skin={} animation={}", energy, pregnancy, skin, animation);
    }

    private interface Probe { void run() throws Throwable; }

    private static boolean probe(Probe probe) {
        try { probe.run(); return true; } catch (Throwable failure) { return false; }
    }

    public static boolean hasLibido(class_1309 entity) {
        verify();
        if (!energy || entity == null) return false;
        try {
            return com.nonid.api.NonEntityApi.hasEnergy(entity);
        } catch (Throwable failure) {
            return false;
        }
    }

    public static int maxLibido(class_1309 entity) {
        try {
            return com.nonid.api.NonEntityApi.getEnergy(entity).maxEnergy();
        } catch (Throwable failure) {
            return 0;
        }
    }

    /**
     * The entity's current energy (NoN's desire value, 0..200 for players), or -1 when NoN's
     * energy surface is unavailable. Stored in the entity's DataTracker, so the client sees its
     * own value and the domain screen can score a draft without a round trip.
     */
    public static int currentEnergy(class_1309 entity) {
        verify();
        if (!energy || entity == null) return -1;
        try {
            var snapshot = com.nonid.api.NonEntityApi.getEnergy(entity);
            return snapshot == null ? -1 : snapshot.energy();
        } catch (Throwable failure) {
            return -1;
        }
    }

    /**
     * Amplifier of NoN's fertility effect on this entity, or -1 when it does not carry it.
     * Status effects travel to the client, so this is usable from both sides.
     */
    public static int fertilityAmplifier(class_1309 entity) {
        if (entity == null) return -1;
        int amplifier = -1;
        try {
            for (class_1293 effect : entity.method_6026())
                if (effect.method_5579().comp_349() == com.nonid.effect.NonStatusEffects.FERTILITY_INCREASER)
                    amplifier = Math.max(amplifier, effect.method_5578());
        } catch (Throwable failure) {
            return -1;
        }
        return amplifier;
    }

    /** Adds energy, clamped by NoN. Returns the amount actually added. */
    public static int addLibido(class_1309 entity, int amount) {
        verify();
        if (!energy || entity == null || amount <= 0) return 0;
        try {
            return com.nonid.api.NonEntityApi.addEnergy(entity, amount);
        } catch (Throwable failure) {
            return 0;
        }
    }

    public static boolean pregnancyEnabled() {
        verify();
        try {
            return com.nonid.api.NonPlayerApi.isPregnancyEnabled();
        } catch (Throwable failure) {
            return false;
        }
    }

    public static boolean activePregnancy(class_1657 player) {
        verify();
        if (!pregnancy || player == null) return false;
        try {
            return com.nonid.api.NonPlayerApi.getPregnancy(player).active();
        } catch (Throwable failure) {
            return false;
        }
    }

    public static boolean clearPregnancy(class_3222 player) {
        verify();
        if (!pregnancy || player == null) return false;
        try {
            return com.nonid.api.NonPlayerApi.clearPregnancy(player);
        } catch (Throwable failure) {
            return false;
        }
    }

    public static boolean skinSystemEnabled() {
        verify();
        try {
            return com.nonid.api.NonPlayerApi.isDestroyedSkinSystemEnabled();
        } catch (Throwable failure) {
            return false;
        }
    }

    /** Raises the player's clothing to its highest damage stage, read from NoN at runtime. */
    public static boolean raiseSkinDamage(class_3222 player) {
        verify();
        if (!skin || player == null) return false;
        try {
            var snapshot = com.nonid.api.NonPlayerApi.getDestroyedSkin(player);
            int missing = Math.max(0, snapshot.maxDamage() - snapshot.damage());
            if (missing <= 0) return true;
            return com.nonid.api.NonPlayerApi.damageDestroyedSkin(player, missing);
        } catch (Throwable failure) {
            return false;
        }
    }

    /**
     * {@code {stage, damage, maxDamage}} for diagnostics - D05 needs to say what it actually
     * changed. Returns null when the NoN skin surface is unusable, so the caller logs "?".
     */
    public static int[] destroyedSkinState(class_3222 player) {
        verify();
        if (!skin || player == null) return null;
        try {
            var snapshot = com.nonid.api.NonPlayerApi.getDestroyedSkin(player);
            return new int[]{snapshot.stage(), snapshot.damage(), snapshot.maxDamage()};
        } catch (Throwable failure) {
            return null;
        }
    }

    public static boolean animationBusy(class_3218 world, java.util.UUID actor) {
        verify();
        if (!animation || world == null) return false;
        try {
            return com.nonid.api.animation.NonAnimationApi.isActorPendingOrActive(world, actor);
        } catch (Throwable failure) {
            return false;
        }
    }

    /** Starts a matched NoN animation. Returns the instance id, or null when unavailable. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static java.util.UUID startMatched(class_3218 world, List<class_1297> actors) {
        verify();
        if (!animation || world == null || actors.isEmpty()) return null;
        try {
            return com.nonid.api.animation.NonAnimationApi.startMatched(world, (List) actors, null, null);
        } catch (Throwable failure) {
            return null;
        }
    }
}
