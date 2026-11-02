package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D49 minecraft:zombie_horse (green) — 不死余力 / undying stamina.
 *
 * <p>Once per expansion, when the owner's lethal damage survives every already-registered
 * life-saving mechanism, the death is cancelled, health is set back to 2 and Resistance II is
 * leased for 60 ticks. The "once" flag is keyed by session, so switching loadout slot or
 * re-engraving the same domain cannot refresh it; only a genuinely new expansion resets it.</p>
 *
 * <p>System kills are never intercepted: {@code minecraft:out_of_world} (void, forced removal
 * by {@code /kill} style code paths) is skipped, and a used-up or non-owner death abstains so
 * the other covering sessions (and D16's inventory transaction) still get their turn.</p>
 *
 * <p>The hook is called by the death mixin owned by the damage-pipeline teammate. {@link
 * DeathHook}'s existing contract is used unchanged: {@code TRUE} means a revive happened and
 * the death must be cancelled, {@code null} abstains.</p>
 */
public final class D49 implements DomainAbility, DeathHook {
    public static final D49 INSTANCE = new D49();
    private D49() {}
    @Override public String id() { return "minecraft:zombie_horse"; }
    @Override public String code() { return "D49"; }
    @Override public int priority() { return 100; }

    private static final String ONCE_KEY = "d49";
    private static final String USED_KEY = "d49_used";
    private static final float REVIVE_HEALTH = 2.0f;
    private static final int RESISTANCE_TICKS = 60;

    @Override
    public Boolean allowDeath(DomainSession session, class_1309 entity, class_1282 source, float amount) {
        if (entity == null || session == null) return null;
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D49",entity)) return null;
        // System kills and forced removal are not survivable events.
        if (DomainDamageTypes.keyOf(source).equals("minecraft:out_of_world")) return null;
        if (!session.once(ONCE_KEY+entity.method_5667())) return null;
        session.flag(USED_KEY, true);
        entity.method_6033(REVIVE_HEALTH);
        // The effect lease is applied with the current pipeline tick; the entity is still
        // alive at this point, so the vanilla immunity check (wither skeleton, ...) applies.
        EffectLeaseManager.apply(entity, "minecraft:resistance", "D49", session.sessionId,
                RESISTANCE_TICKS, 1, com.innerdimension.domain.combat.DomainDamagePipeline.now());
        return Boolean.TRUE;
    }

    public static boolean used(DomainSession session) {
        return session != null && session.flag(USED_KEY);
    }
}
