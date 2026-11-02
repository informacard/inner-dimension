package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D50 minecraft:skeleton_horse (cyan) — 亡骨水息 / boned water-breathing.
 *
 * <p>Every living entity inside the fixed interior is immune to the drowning damage category:
 * the pre-damage chain cancels {@link DomainDamageTypes#isDrowning} — both vanilla's drowning
 * damage type tag and the {@code inner_dimension:domain_drowning} category that D30 (drowned)
 * submits. Nothing refills the air bar, so vanilla's air counter keeps running and the normal
 * air rules come straight back when the expansion ends.</p>
 *
 * <p>Priority 10 keeps the ordering required by blueprint section 1.3: D04's blanket cancel
 * (priority 0) still wins first, D40's projectile dodge, D51/D52's category immunities and
 * D14's late multiplier are later in the same chain.</p>
 *
 * <p>// VERIFY: two static tests must be run against the unified pre-damage chain —
 * (1) a vanilla drown source (damage type tagged {@code minecraft:is_drowning}) is cancelled;
 * (2) D30's {@code inner_dimension:domain_drowning} source is cancelled by the same rule;
 * (3) a non-drowning source (e.g. {@code minecraft:mob_attack}) is allowed through untouched.
 * Then the in-game pair check: run out of air naturally with D30 + D50 engraved and confirm no
 * drowning damage while the loadout UI shows {@code domain.loadout.tip.drown_negated}.</p>
 */
public final class D50 implements DomainAbility, DamageHook {
    public static final D50 INSTANCE = new D50();
    private D50() {}
    @Override public String id() { return "minecraft:skeleton_horse"; }
    @Override public String code() { return "D50"; }
    @Override public int priority() { return 10; }

    /** Loadout-UI conflict tip: this ability negates D30's drowning damage. */
    public static java.util.List<String> conflictNotes() {
        return java.util.List.of("domain.loadout.tip.drown_negated");
    }

    @Override
    public Boolean preDamage(DomainSession session, class_1309 victim, class_1282 source, float amount) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D50",victim))return null;
        if (DomainDamageTypes.isDrowning(source)) return Boolean.TRUE;
        return null;
    }
}
