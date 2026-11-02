package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;

/**
 * D24 minecraft:villager (green) — "安居".
 *
 * <p>Every player inside the fixed interior holds Hero of the Village II while the
 * expansion lasts. Villager restocking is an environment mechanic and is deliberately not
 * gated by the object mask: every villager inside gets an extra restock opportunity every
 * 200 ticks regardless of which categories the player selected. The decision stays entirely
 * with vanilla: {@code shouldRestock(ServerLevel)} already folds in
 * the profession/offer state ("real out of stock") and the per-day restock cap, and the
 * vanilla {@code restock()} transaction then counts itself. Nothing here registers a POI,
 * fakes a workstation or forges a restock.</p>
 *
 * <p>GAP: vanilla 1.21's restock path no longer tests workstation reachability at all, so
 * "workstation reachable" cannot be honoured beyond what vanilla itself does — a villager
 * whose job site was destroyed keeps restocking on vanilla's own schedule. Fixing that
 * would mean editing vanilla brain/POI code, which is outside this file set.</p>
 */
public final class D24 implements DomainAbility {
    public static final D24 INSTANCE = new D24();
    private D24() {}
    @Override public String id() { return "minecraft:villager"; }
    @Override public String code() { return "D24"; }

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D24")) {
            {
                EffectLeaseManager.apply(entity, "minecraft:hero_of_the_village", "D24",
                        s.sessionId, 60, 1, now);
            }
        }
        if (!(s.world instanceof class_3218 world)) return;
        for (class_1309 entity : s.livingInside()) {
            if (!(entity instanceof class_1646 villager)) continue;
            if (villager.method_7325() || !villager.method_5805()) continue;
            if (!D24D46Support.periodReady(s, "d24", villager.method_5667(), 200, now)) continue;
            if (villager.method_20822(world)) villager.method_19182();
        }
    }
}
