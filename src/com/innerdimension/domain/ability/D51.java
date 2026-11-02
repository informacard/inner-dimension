package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D51 minecraft:blaze (orange) — 炎域 / fire field.
 *
 * <p>Owner side: the pre-damage chain cancels the fire category (the helper already excludes
 * lava) for the owner only, at priority 30 — after D50's drowning cancel. Lava damage is not
 * absorbed. Ignition contributions are cancelled implicitly by cancelling the fire damage that
 * would follow.</p>
 *
 * <p>Enemy side: every 20 ticks each enemy's remaining burn time is raised to at least 60
 * ticks. Water and vanilla fire-immune entity types cancel it exactly as vanilla would
 * ({@code EntityType.fireImmune}), so no extra immunity model is invented.</p>
 *
 * <p>Fire ledger: the tick at which this ability last set fire on a target is recorded per
 * target. {@code onEnd} extinguishes only the targets whose fire was started by us within the
 * last 20 ticks; an externally caused fire (or one we merely refreshed long ago) is left
 * burning, exactly as the blueprint requires. There is no way to distinguish an external
 * ignition that happened inside our 20 tick window, and that case is deliberately resolved in
 * favour of leaving the fire alone only when our own record is older than the window.</p>
 */
public final class D51 implements DomainAbility, DamageHook {
    public static final D51 INSTANCE = new D51();
    private D51() {}
    @Override public String id() { return "minecraft:blaze"; }
    @Override public String code() { return "D51"; }
    @Override public int priority() { return 30; }

    private static final int REFRESH_INTERVAL = 20;
    private static final int BURN_TICKS = 60;
    private static final long EXTINGUISH_WINDOW = 20;
    private static final String FIRE_LEDGER = "d51_fire";

    @Override
    public Boolean preDamage(DomainSession session, class_1309 victim, class_1282 source, float amount) {
        if (victim == null || !victim.method_5667().equals(session.ownerId)) return null;
        if (DomainDamageTypes.isFire(source)) return Boolean.TRUE;
        return null;
    }

    @Override
    public void onTick(DomainSession s, long now) {
        if(s.open()){Map<UUID,Long> ledger=s.state(FIRE_LEDGER,null);if(ledger!=null)for(var it=ledger.entrySet().iterator();it.hasNext();){var entry=it.next();var target=D47D69Support.living(s.world,entry.getKey());if(target==null||!s.contains(target)){if(target!=null&&now-entry.getValue()<=EXTINGUISH_WINDOW)target.method_20803(0);it.remove();}}}
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D51")) {
            if (!com.innerdimension.domain.runtime.DomainObjects.matches(s,"D51",entity)) continue;
            if (!s.due("d51_ignite", entity.method_5667(), REFRESH_INTERVAL, now)) continue;
            s.arm("d51_ignite", entity.method_5667(), now);
            if (entity.method_5864().method_19946()) continue; // vanilla fire immunity
            if (entity.method_20802() < BURN_TICKS) entity.method_20803(BURN_TICKS);
            record(s, entity.method_5667(), now);
        }
    }

    @SuppressWarnings("unchecked")
    private static void record(DomainSession s, UUID target, long now) {
        Object stored = s.state(FIRE_LEDGER, null);
        Map<UUID, Long> ledger = stored instanceof Map<?, ?> ? (Map<UUID, Long>) stored : new HashMap<>();
        ledger.put(target, now);
        s.putState(FIRE_LEDGER, ledger);
    }

    @Override
    public void onEnd(DomainSession s) {
        Object stored = s.state(FIRE_LEDGER, null);
        if (!(stored instanceof Map<?, ?> raw)) return;
        long now = com.innerdimension.domain.combat.DomainDamagePipeline.now();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (!(entry.getKey() instanceof UUID uuid) || !(entry.getValue() instanceof Long setAt)) continue;
            // Only a fire this ability started recently is ours to put out.
            if (now - setAt > EXTINGUISH_WINDOW) continue;
            class_1309 entity = D47D69Support.living(s.world, uuid);
            if (entity != null) entity.method_20803(0);
        }
    }
}
