package com.innerdimension.domain.combat;

import com.innerdimension.domain.ability.DamageHook;
import com.innerdimension.domain.ability.DeathHook;
import com.innerdimension.domain.ability.DomainAbilities;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

import java.util.*;

/**
 * The single damage entry point: one ordered pre-damage chain, one post-armor chain, one
 * after-damage chain and one autonomous-damage helper that keeps every periodic ledger
 * independent of vanilla's invulnerability frames.
 *
 * <p>Order (blueprint section 5.3):
 * D04 cancel -&gt; D50 drowning -&gt; D40 projectile dodge -&gt; D51 fire / D52 lava / D39 fall
 * -&gt; other immunities; then vanilla armor/resistance; then D14 x0.4; then D55/D60/D63
 * and the combat timers.</p>
 */
public final class DomainDamagePipeline {
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private record PreparedHit(class_1309 target, class_1282 physical, class_1282 magic) {
        boolean matches(class_1309 victim, class_1282 source) {
            return victim == target && (source == physical || source == magic);
        }
    }
    private static final ThreadLocal<PreparedHit> PREPARED = new ThreadLocal<>();

    /** victim uuid -> tick of the last damage that actually reduced health. */
    private static final Map<UUID, Long> LAST_HURT = new HashMap<>();
    /** victim uuid -> tick of the last outgoing attack (melee or projectile). */
    private static final Map<UUID, Long> LAST_ATTACK = new HashMap<>();
    /** victim uuid -> most recent hostile attacker that actually hurt it. */
    private static final Map<UUID, Attacker> LAST_ATTACKER = new HashMap<>();
    /** entity uuid -> source of the last positive heal. */
    private static final Map<UUID, Long> LAST_HEAL = new HashMap<>();
    private static long tick;

    public record Attacker(UUID uuid, long tick) {}

    private DomainDamagePipeline() {}

    public static void advance() { tick++; }

    public static long now() { return tick; }
    public static boolean autonomous() { return DEPTH.get()>0; }

    public static long lastHurt(UUID id) { return LAST_HURT.getOrDefault(id, Long.MIN_VALUE / 2); }

    public static long lastAttack(UUID id) { return LAST_ATTACK.getOrDefault(id, Long.MIN_VALUE / 2); }

    public static long lastHeal(UUID id) { return LAST_HEAL.getOrDefault(id, Long.MIN_VALUE / 2); }

    public static Attacker lastAttacker(UUID id) { return LAST_ATTACKER.get(id); }

    public static void markAttack(class_1297 attacker) {
        if (attacker != null) LAST_ATTACK.put(attacker.method_5667(), tick);
    }

    public static void markHeal(class_1309 entity) {
        if (entity != null) LAST_HEAL.put(entity.method_5667(), tick);
    }

    /* ------------------------------------------------------------- pre pass */

    /**
     * Ordered pre-damage decision.
     *
     * @return {@code false} to cancel the vanilla damage entirely (a hook returned TRUE),
     *         {@code true} to let it through.
     */
    public static boolean pre(class_1309 victim, class_1282 source, float amount) {
        if(source!=null&&source.method_5529()!=null&&com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(source.method_5529()))return false;
        if(victim!=null&&com.innerdimension.boss.BossDomains.domainImmune(victim)&&source!=null){
            var attacker=source.method_5529();
            boolean summoned=attacker!=null&&attacker.method_5752().stream().anyMatch(tag->tag.startsWith(com.innerdimension.domain.ability.DomainSummons.SESSION));
            if(com.innerdimension.boss.BossCombatRules.domainSource(DomainDamageTypes.keyOf(source),summoned))return false;
        }
        PreparedHit prepared = PREPARED.get();
        if (prepared != null && prepared.matches(victim, source)) return true;
        if(com.innerdimension.voiddomain.VoidContamination.isDamage(source))return true;
        if (victim == null || victim.method_73183().method_8608()) return true;
        if (com.innerdimension.domain.SimpleDomain.absorb(victim,source,amount)) return false;
        List<DomainSession> sessions = DomainSessionIndex.covering(victim);
        if (sessions.isEmpty()) return true;
        // A reflected domain hit must never be re-processed by the reactions it triggers.
        if (DomainDamageTypes.isReflected(source)) return true;
        for (DomainSession session : sessions) {
            // DomainAbilities.hooks is already ordered by DomainAbility.priority():
            // D04 (0) -> D50 (10) -> D40 (20) -> D51 (30) -> D52 (31) -> D39 (32) -> rest.
            for (DamageHook hook : DomainAbilities.hooks(session, DamageHook.class)) {
                Boolean decision;
                try {
                    decision = hook.preDamage(session, victim, source, amount);
                } catch (RuntimeException failure) {
                    continue;
                }
                if (decision == null) continue;
                // TRUE cancels, FALSE forces the hit through.
                return !decision;
            }
        }
        return true;
    }

    /* ---------------------------------------------------------- final pass */

    /** Post-armor, post-resistance multiplier, injected at the actual-hurt boundary. */
    public static float finalAmount(class_1309 victim, class_1282 source, float amount) {
        if(com.innerdimension.voiddomain.VoidContamination.isDamage(source))return amount;
        if (victim == null || amount <= 0) return amount;
        List<DomainSession> sessions = DomainSessionIndex.covering(victim);
        if (sessions.isEmpty()) return amount;
        float result = amount;
        for (DomainSession session : sessions)
            for (DamageHook hook : DomainAbilities.hooks(session, DamageHook.class)) {
                try {
                    result = hook.postArmor(session, victim, source, result);
                } catch (RuntimeException ignored) {
                    // A broken hook must never inflate damage.
                }
            }
        return Math.max(0, result);
    }

    /* ---------------------------------------------------------- after pass */

    public static void after(class_1309 victim, class_1282 source, float baseAmount, float taken, boolean blocked) {
        if (victim == null) return;
        DomainShieldBreak.after(victim, source, taken, blocked);
        if(DEPTH.get()==0&&taken>0&&source!=null&&source.method_5529() instanceof class_1657 attacker)com.innerdimension.domain.runtime.DomainObjects.attacked(attacker,victim);
        if (taken > 0) {
            LAST_HURT.put(victim.method_5667(), tick);
            class_1297 attacker = source == null ? null : source.method_5529();
            if (attacker != null && !attacker.method_5667().equals(victim.method_5667()))
                LAST_ATTACKER.put(victim.method_5667(), new Attacker(attacker.method_5667(), tick));
        }
        List<DomainSession> sessions = DomainSessionIndex.covering(victim);
        if (sessions.isEmpty()) return;
        for (DomainSession session : sessions) {
            for (DamageHook hook : DomainAbilities.hooks(session, DamageHook.class)) {
                try {
                    hook.afterDamage(session, victim, source, baseAmount, taken, blocked);
                } catch (RuntimeException ignored) {
                    // Reactions are best-effort; the health change already happened.
                }
            }
            if (taken > 0 && victim.method_5667().equals(session.ownerId)) {
                for (DamageHook hook : DomainAbilities.hooks(session, DamageHook.class)) {
                    try {
                        hook.ownerHurt(session, source, taken);
                    } catch (RuntimeException ignored) {
                        // As above.
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------ death pass */

    /**
     * Last-chance revive chain, consulted by the {@code ALLOW_DEATH} event after every
     * already-registered vanilla life saver (totem, ...) and before the entity is removed.
     * Returning FALSE cancels the death.
     */
    public static boolean allowDeath(class_1309 entity, class_1282 source, float amount) {
        if(com.innerdimension.voiddomain.VoidContamination.isDamage(source))return true;
        if (entity == null) return true;
        List<DomainSession> sessions = DomainSessionIndex.covering(entity);
        if (sessions.isEmpty()) return true;
        for (DomainSession session : sessions) {
            for (DeathHook hook : DomainAbilities.hooks(session, DeathHook.class)) {
                Boolean decision;
                try {
                    decision = hook.allowDeath(session, entity, source, amount);
                } catch (RuntimeException failure) {
                    continue;
                }
                // TRUE means a revive happened and the death must be cancelled.
                if (Boolean.TRUE.equals(decision)) return false;
            }
        }
        return true;
    }

    /* --------------------------------------------------- autonomous damage */

    /**
     * Applies damage from one of this mod's independent periodic ledgers. The vanilla
     * invulnerability window is cleared first so two different abilities firing on the
     * same tick both land, exactly as the combination table requires.
     */
    public static boolean hurt(class_1309 target, class_1297 attacker, String typeId, float amount) {
        return hurt(target, attacker, typeId, amount, true);
    }

    public static boolean hurt(class_1309 target, class_1297 attacker, String typeId, float amount,
                               boolean bypassRegen) {
        if (amount <= 0 || target == null || target.method_7325() || !target.method_5805() || com.innerdimension.boss.BossDomains.domainImmune(target)) return false;
        if (!(target.method_73183() instanceof class_3218 world)) return false;
        if (DEPTH.get() >= 4) return false;
        DEPTH.set(DEPTH.get() + 1);
        try {
            class_1282 source = DomainDamageTypes.source(world, typeId, attacker, attacker);
            DomainDamageProfile profile = DomainDamageProfile.of(typeId);
            if (!profile.mixed()) return component(target, world, source, amount, bypassRegen);
            // One logical attack: dodge, cancellation and Simple Domain absorption see the
            // full amount once. Vanilla armor, shielding, effects and enchantments still
            // process the physical and magic requests independently.
            if (!pre(target, source, amount)) return false;
            class_1282 magic = DomainDamageTypes.source(world, profile.magicType(), attacker, attacker);
            PreparedHit previous = PREPARED.get();
            PREPARED.set(new PreparedHit(target, source, magic));
            try {
                return profile.apply(amount, (type, part) -> component(target, world,
                        type.equals(typeId) ? source : magic, part, bypassRegen || type.equals(profile.magicType())));
            } finally {
                if (previous == null) PREPARED.remove(); else PREPARED.set(previous);
            }
        } finally {
            DEPTH.set(DEPTH.get() - 1);
        }
    }

    private static boolean component(class_1309 target, class_3218 world, class_1282 source,
                                     float amount, boolean bypassRegen) {
        if (amount <= 0 || !target.method_5805() || target.method_7325()) return false;
        if (bypassRegen) target.field_6008 = 0;
        return target.method_64397(world, source, amount);
    }

    /** Owner-attributed autonomous damage, used by D15/D26/D30/D45/D46/D60/D68. */
    public static boolean hurtFromOwner(class_1309 target, class_3222 owner, String typeId, float amount) {
        return hurt(target, owner, typeId, amount);
    }

    /* ------------------------------------------------------------- helpers */

    /** Whether any covering session counts this victim as an enemy or ally. */
    public static List<DomainSession> sessionsFor(class_1297 entity) {
        return DomainSessionIndex.covering(entity);
    }

    public static boolean anyEnemy(List<DomainSession> sessions, class_1297 entity) {
        for (DomainSession s : sessions) if (DomainRelations.isEnemy(s, entity)) return true;
        return false;
    }

    public static boolean anyFriendly(List<DomainSession> sessions, class_1297 entity) {
        for (DomainSession s : sessions) if (DomainRelations.isFriendly(s, entity)) return true;
        return false;
    }

    /** Selects the owner that should be credited for damage against {@code victim}. */
    public static class_3222 creditOwner(List<DomainSession> sessions, class_1297 victim) {
        for (DomainSession s : sessions) if (DomainRelations.isEnemy(s, victim)) { class_3222 o = s.owner(); if (o != null) return o; }
        for (DomainSession s : sessions) { class_3222 o = s.owner(); if (o != null) return o; }
        return null;
    }

    public static void clear() {
        LAST_HURT.clear();
        LAST_ATTACK.clear();
        LAST_ATTACKER.clear();
        LAST_HEAL.clear();
        tick = 0;
    }
}
