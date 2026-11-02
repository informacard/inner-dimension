package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainHealingPipeline;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D12 minecraft:panda (green).
 *
 * <p>Repose: a friendly entity that has neither attacked nor been really hurt for 5 seconds
 * recovers 1 health every 2 seconds. A player additionally has to keep the same position
 * for 2 seconds (view rotation and ordinary float jitter do not count as movement); a
 * non-player mob may walk around freely. Any attack or real damage restarts the clock, and
 * damage cancelled by D04 never counts as damage taken.</p>
 */
public final class D12 implements DomainAbility {
    public static final D12 INSTANCE = new D12();
    private D12() {}
    @Override public String id() { return "minecraft:panda"; }
    @Override public String code() { return "D12"; }

    private static final long OUT_OF_COMBAT = 100;   // 5 seconds
    private static final long HEAL_INTERVAL = 40;    // 2 seconds
    private static final long PLAYER_STILL = 40;     // 2 seconds
    private static final double STILL_EPSILON = 0.01;
    private static final String TIMER = "d12_heal";
    private static final String MAP_STILL_SINCE = "d12_player_still_since";
    private static final String MAP_LAST_POS = "d12_player_last_pos";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        List<class_1309> friendly = com.innerdimension.domain.runtime.DomainObjects.inside(session,"D12");
        Set<UUID> eligible = new HashSet<>();
        Set<UUID> players = new HashSet<>();
        Map<UUID, double[]> lastPos = D01D23Support.uuidMap(session, MAP_LAST_POS);
        Map<UUID, Long> stillSince = D01D23Support.uuidMap(session, MAP_STILL_SINCE);

        for (class_1309 entity : friendly) {
            UUID id = entity.method_5667();
            long lastCombat = Math.max(DomainDamagePipeline.lastHurt(id), DomainDamagePipeline.lastAttack(id));
            if (now - lastCombat < OUT_OF_COMBAT) {
                stillSince.put(id, now);
                continue;                       // still in combat: no rest, clock restarts
            }
            if (entity instanceof class_1657 player) {
                players.add(id);
                double[] previous = lastPos.get(id);
                double x = player.method_23317(), y = player.method_23318(), z = player.method_23321();
                if (previous == null || Math.abs(previous[0] - x) > STILL_EPSILON
                        || Math.abs(previous[1] - y) > STILL_EPSILON
                        || Math.abs(previous[2] - z) > STILL_EPSILON) {
                    stillSince.put(id, now);    // moved: the stillness window restarts
                }
                lastPos.put(id, new double[]{x, y, z});
                if (now - stillSince.getOrDefault(id, now) < PLAYER_STILL) continue;
            }
            eligible.add(id);
            if (D01D23Support.periodFromEligibility(session, TIMER, id, HEAL_INTERVAL, now))
                DomainHealingPipeline.domainHeal(entity, 1.0f);
        }

        D01D23Support.retainEligible(session, TIMER, eligible);
        stillSince.keySet().retainAll(players);
        lastPos.keySet().retainAll(players);
    }

    /** True when the entity would currently rest (used by tests and the status HUD). */
    public static boolean resting(DomainSession session, class_1309 entity, long now) {
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D12",entity)) return false;
        UUID id = entity.method_5667();
        long lastCombat = Math.max(DomainDamagePipeline.lastHurt(id), DomainDamagePipeline.lastAttack(id));
        return now - lastCombat >= OUT_OF_COMBAT;
    }

    @Override
    public void onEnd(DomainSession session) {
        session.putState(MAP_STILL_SINCE, null);
        session.putState(MAP_LAST_POS, null);
    }
}
