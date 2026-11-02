package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D59 minecraft:wither (purple) — 衰败 / decay.
 *
 * <p>Every enemy inside the interior carries a Wither II lease; the effect is applied through
 * {@code EffectLeaseManager}, so vanilla's real wither immunity (wither skeletons, the wither
 * itself, ...) still refuses the instance exactly as it would for a potion — nothing bypasses
 * {@code LivingEntity#addStatusEffect}.</p>
 *
 * <p>Enemies receive no positive healing through {@code HealHook.healingMultiplier}.
 * Owners and allies are unaffected. The contribution ends with the session.</p>
 */
public final class D59 implements DomainAbility, HealHook {
    public static final D59 INSTANCE = new D59();
    private D59() {}
    @Override public String id() { return "minecraft:wither"; }
    @Override public String code() { return "D59"; }
    @Override public int priority() { return 100; }

    private static final int WITHER_REFRESH = 60;
    private static final float HEAL_MULTIPLIER = 0.0f;

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D59")) {
            if (!com.innerdimension.domain.runtime.DomainObjects.matches(s,"D59",entity)) continue;
            EffectLeaseManager.apply(entity, "minecraft:wither", "D59", s.sessionId, WITHER_REFRESH, 1, now);
        }
    }

    @Override
    public float healingMultiplier(DomainSession session, class_1309 entity) {
        return com.innerdimension.domain.runtime.DomainObjects.matches(session,"D59",entity) ? HEAL_MULTIPLIER : 1.0f;
    }
}
