package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D57 minecraft:zoglin (red) — 狂乱 / frenzy.
 *
 * <p>Every 20 ticks an ordinary hostile mob is pointed at the nearest other enemy inside the
 * interior. The choice is held for at least 60 ticks and is only replaced early when the held
 * target dies, leaves the domain or stops being an enemy. Candidates exclude the mob itself,
 * players, allies and bosses; a boss never participates in re-targeting at all (it still keeps
 * every other ability).</p>
 *
 * <p>The result is published through {@code AiHook.preferredTargets}, so the mob-behaviour
 * mixin applies it via {@code DomainAiPolicy} and the vanilla AI cannot instantly overwrite it.
 * With no candidate the mob simply keeps acting on its own.</p>
 */
public final class D57 implements DomainAbility, AiHook {
    public static final D57 INSTANCE = new D57();
    private D57() {}
    @Override public String id() { return "minecraft:zoglin"; }
    @Override public String code() { return "D57"; }
    @Override public int priority() { return 100; }

    private static final String TARGET_KEY = "d57_target";
    private static final String HOLD_UNTIL_KEY = "d57_hold_until";
    private static final long SCAN_INTERVAL = 20;
    private static final long MIN_HOLD = 60;

    @Override public void onTick(DomainSession s, long now) {
        Map<class_1308,class_1309> targets = tracked(s);
        for (var it = targets.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (s.contains(entry.getKey()) && entry.getValue().method_5805()
                    && s.contains(entry.getValue())) continue;
            it.remove();
            s.putState(TARGET_KEY + ":" + entry.getKey().method_5667(), null);
            s.putState(HOLD_UNTIL_KEY + ":" + entry.getKey().method_5667(), null);
            release(entry.getKey(), entry.getValue());
        }
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D57"))
            if (entity instanceof class_1308 mob && !preferredTargets(s,mob).isEmpty())
                com.innerdimension.domain.ai.DomainAiPolicy.pushPreferredTarget(mob);
    }
    @SuppressWarnings("unchecked")
    private static Map<class_1308,class_1309> tracked(DomainSession s) {
        Map<class_1308,class_1309> map = s.state("d57_forced",null);
        if (map == null) { map = new WeakHashMap<>(); s.putState("d57_forced",map); }
        return map;
    }
    private static void release(class_1308 mob, class_1309 forced) {
        if (mob.method_5968() == forced) mob.method_5980(null);
        if (mob.method_18868().method_18904(class_4140.field_22355).orElse(null) == forced)
            mob.method_18868().method_18878(class_4140.field_22355,(class_1309)null);
    }
    @Override public void onEnd(DomainSession s) {
        tracked(s).forEach(D57::release);
        s.putState("d57_forced",null);
    }

    @Override
    public List<class_1309> preferredTargets(DomainSession session, class_1308 mob) {
        if (mob == null || session.ended()) return List.of();
        if (DomainRelations.isBoss(mob)) return List.of();
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D57",mob)) return List.of();
        long now = DomainDamagePipeline.now();
        String targetKey = TARGET_KEY + ":" + mob.method_5667();
        String holdKey = HOLD_UNTIL_KEY + ":" + mob.method_5667();
        class_1309 held = D47D69Support.living(session.world, session.state(targetKey, null));
        if (held != null && valid(session, mob, held)) {
            Object until = session.state(holdKey, null);
            if (until instanceof Long limit && now < limit) return List.of(held);
        }
        if (!session.due("d57_scan", mob.method_5667(), SCAN_INTERVAL, now)) return held != null && valid(session, mob, held) ? List.of(held) : List.of();
        session.arm("d57_scan", mob.method_5667(), now);
        class_1309 nearest = nearestOther(session, mob);
        if (nearest == null) {
            session.putState(targetKey, null);
            session.putState(holdKey, null);
            return List.of();
        }
        tracked(session).put(mob,nearest);
        session.putState(targetKey, nearest.method_5667());
        session.putState(holdKey, now + MIN_HOLD);
        return List.of(nearest);
    }

    private static boolean valid(DomainSession session, class_1308 mob, class_1309 candidate) {
        if (candidate == mob || !candidate.method_5805() || candidate.method_7325()) return false;
        if (!session.contains(candidate)) return false;

        return com.innerdimension.domain.runtime.DomainObjects.matches(session,"D57",candidate);
    }

    private static class_1309 nearestOther(DomainSession session, class_1308 mob) {
        class_1297 self = mob;
        class_1309 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (class_1309 candidate : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D57")) {
            if (!valid(session, mob, candidate)) continue;
            double distance = candidate.method_33571().method_1022(self.method_33571());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }
}
