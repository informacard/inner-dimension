package com.innerdimension.domain.ability;

import com.innerdimension.domain.compat.NoNDomainBridge;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D03 minecraft:cow (neutral).
 *
 * <p>Cleansing milk: every friendly entity is purified once every 100 ticks — one random
 * entry out of the currently clearable harmful-effect pool. Buffs are never touched. An
 * active NoN pregnancy joins the pool as a real entry and is cleared through NoN's own
 * API, not by only dropping an icon.</p>
 *
 * <p>Domain-fired debuffs reappear with the next lease refresh, which is the documented
 * behaviour; a cleared pregnancy does not come back.</p>
 */
public final class D03 implements DomainAbility {
    public static final D03 INSTANCE = new D03();
    private D03() {}
    @Override public String id() { return "minecraft:cow"; }
    @Override public String code() { return "D03"; }

    private static final long PURIFY_INTERVAL = 100;
    private static final String TIMER = "d03_purify";
    private static final String PREGNANCY = "d03_pregnancy_entry";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        List<class_1309> friendly = com.innerdimension.domain.runtime.DomainObjects.inside(session,"D03");
        Set<UUID> eligible = new HashSet<>();
        for (class_1309 entity : friendly) {
            eligible.add(entity.method_5667());
            if (D01D23Support.dueAfterEligibility(session, TIMER, entity.method_5667(), PURIFY_INTERVAL, now))
                purify(session, entity);
        }
        D01D23Support.retainEligible(session, TIMER, eligible);
    }

    /** Builds the pool from the live effect map plus the real pregnancy entry. */
    private void purify(DomainSession session, class_1309 entity) {
        List<Runnable> pool = new ArrayList<>();
        for (Map.Entry<class_6880<class_1291>, class_1293> entry
                : new ArrayList<>(entity.method_6088().entrySet())) {
            class_6880<class_1291> holder = entry.getKey();
            class_1291 effect = holder.comp_349();
            // Only the harmful category: beneficial and neutral effects are never cleared.
            if (effect == null || effect.method_18792() != class_4081.field_18272) continue;
            pool.add(() -> entity.method_6111(holder));
        }
        if (entity instanceof class_3222 player && NoNDomainBridge.pregnancyEnabled()
                && NoNDomainBridge.activePregnancy(player)) {
            pool.add(() -> NoNDomainBridge.clearPregnancy(player));
        }
        if (pool.isEmpty()) return;
        int choice = Math.floorMod(D01D23Support.random(session).method_43048(Integer.MAX_VALUE), pool.size());
        pool.get(choice).run();
    }

    /** True when this entity currently offers anything D03 could clear. */
    public static boolean hasPurifiablePool(class_1309 entity) {
        if (entity == null) return false;
        for (class_6880<class_1291> holder : entity.method_6088().keySet()) {
            class_1291 effect = holder.comp_349();
            if (effect != null && effect.method_18792() == class_4081.field_18272) return true;
        }
        return entity instanceof class_3222 player && NoNDomainBridge.activePregnancy(player);
    }

    @Override
    public void onEnd(DomainSession session) {
        // Nothing to roll back: a cleared effect stays cleared.
        session.putState(PREGNANCY, null);
    }
}
