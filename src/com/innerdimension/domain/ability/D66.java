package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import net.minecraft.*;
import java.util.*;

/**
 * D66 minecraft:happy_ghast (blue) — 云托 / cloud carry.
 *
 * <p>Every ally inside the interior carries Slow Falling I and Jump Boost I while the expansion
 * is stable. No flight is granted and no cloud platform is created; normal mixed-geometry
 * landing is untouched. Jump Boost merges with D07 at the highest same-effect amplifier instead
 * of stacking a second potion.</p>
 */
public final class D66 implements DomainAbility {
    public static final D66 INSTANCE = new D66();
    private D66() {}
    @Override public String id() { return "minecraft:happy_ghast"; }
    @Override public String code() { return "D66"; }
    @Override public int priority() { return 100; }

    private static final int REFRESH = 60;

    @Override
    public void onTick(DomainSession s, long now) {
        Set<class_1309> present = new HashSet<>();
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D66")) {
            if (!com.innerdimension.domain.runtime.DomainObjects.matches(s,"D66",entity)) continue;
            present.add(entity);
            EffectLeaseManager.apply(entity, "minecraft:slow_falling", "D66", s.sessionId, REFRESH, 0, now);
            EffectLeaseManager.apply(entity, "minecraft:jump_boost", "D66", s.sessionId, REFRESH, 0, now);
        }
        Set<class_1309> previous = s.state("d66_friends", Set.of());
        for (class_1309 entity : previous) if (!present.contains(entity)) revoke(s,entity,now);
        s.putState("d66_friends",present);
    }
    private static void revoke(DomainSession s, class_1309 entity, long now) {
        EffectLeaseManager.revoke(entity,EffectLeaseManager.effect("minecraft:slow_falling"),s.sessionId,"D66",now);
        EffectLeaseManager.revoke(entity,EffectLeaseManager.effect("minecraft:jump_boost"),s.sessionId,"D66",now);
    }
    @Override public void onEnd(DomainSession s) {
        Set<class_1309> previous = s.state("d66_friends", Set.of());
        for (class_1309 entity : previous) revoke(s,entity,com.innerdimension.domain.combat.DomainDamagePipeline.now());
        s.putState("d66_friends",null);
    }
}
