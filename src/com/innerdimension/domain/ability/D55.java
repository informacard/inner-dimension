package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D55 minecraft:zombified_piglin (green) — 同仇 / shared grudge.
 *
 * <p>When any allied pet really loses health to a hostile source, the source is recorded as
 * the session's latest attacker for 160 ticks. While a record is live every allied pet is
 * granted Strength I for the same 160 ticks, refreshed by each new hit. Damage that D04
 * cancelled never reaches {@code afterDamage}, so it never triggers.</p>
 *
 * <p>Target priority: a live D09 (wolf) mark wins, otherwise the latest attacker. The D09
 * state is read defensively through {@link DomainSessionIndex#coveringWith} because D09 is a
 * different teammate's file and may legitimately be absent or store a different value.</p>
 *
 * <p>Pathing is left to the navigation adaptation: only {@code preferredTargets} is supplied,
 * which the mob-behaviour mixin consumes via {@code DomainAiPolicy}; nothing here calls
 * {@code setTarget} every tick.</p>
 */
public final class D55 implements DomainAbility, DamageHook, AiHook {
    public static final D55 INSTANCE = new D55();
    private D55() {}
    @Override public String id() { return "minecraft:zombified_piglin"; }
    @Override public String code() { return "D55"; }
    @Override public int priority() { return 100; }

    private static final String ATTACKER_KEY = "d55_attacker";
    private static final String ATTACKER_TICK_KEY = "d55_attacker_tick";
    private static final long RECORD_TICKS = 160;
    private static final int STRENGTH_TICKS = 160;

    @Override
    public void afterDamage(DomainSession session, class_1309 victim, class_1282 source,
                            float baseAmount, float taken, boolean blocked) {
        if (taken <= 0 || source == null) return;
        if (!DomainRelations.isFriendly(session, victim)) return;
        class_1297 raw = source.method_5529();
        if (!(raw instanceof class_1309 attacker) || attacker == victim) return;
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D55",attacker)) return;
        long now = DomainDamagePipeline.now();
        session.putState(ATTACKER_KEY, attacker.method_5667());
        session.putState(ATTACKER_TICK_KEY, now);
    }

    @Override
    public void onTick(DomainSession s, long now) {
        if (!recordLive(s, now)) return;
        for (class_1309 entity : s.livingInside()) {
            if (DomainRelations.of(s, entity) != DomainRelations.Relation.ALLY) continue;
            long hitAt = s.state(ATTACKER_TICK_KEY, now);
            int remaining = (int) Math.min(STRENGTH_TICKS, RECORD_TICKS - (now - hitAt));
            EffectLeaseManager.apply(entity, "minecraft:strength", "D55", s.sessionId, remaining, 0, now);
        }
    }

    private static boolean recordLive(DomainSession s, long now) {
        Object stored = s.state(ATTACKER_TICK_KEY, null);
        return stored instanceof Long tick && now - tick < RECORD_TICKS;
    }

    @Override
    public List<class_1309> preferredTargets(DomainSession session, class_1308 mob) {
        if (mob == null || !D01D23Support.isPet(session, mob)) return List.of();
        List<class_1309> out = new ArrayList<>(2);
        class_1309 marked = d09Mark(session, mob);
        if (marked != null) out.add(marked);
        class_1309 latest = recordLive(session, DomainDamagePipeline.now())
                ? D47D69Support.living(session.world, session.state(ATTACKER_KEY, null)) : null;
        if (latest != null && latest != mob && !out.contains(latest)
                && session.contains(latest) && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D55",latest)) out.add(latest);
        return out;
    }

    /** The D09 mark of any covering wolf session for this pet, or null. Defensive by design. */
    private static class_1309 d09Mark(DomainSession session, class_1308 mob) {
        for (DomainSession wolf : DomainSessionIndex.coveringWith(mob, "D09")) {
            class_1309 entity = D09.markedEntity(wolf, DomainDamagePipeline.now());
            if (entity != null && entity != mob && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D55",entity)) return entity;
        }
        return null;
    }
}
