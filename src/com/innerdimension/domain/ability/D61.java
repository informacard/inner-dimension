package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.world.DomainTeleportGuard;
import net.minecraft.*;
import java.util.*;

/**
 * D61 minecraft:trader_llama (blue) — 行商来访 / trader visit.
 *
 * <p>Three independent 20% rolls per expansion: at STABLE, once the expansion is 200 ticks old
 * and once it is 400 ticks old. Each roll is guarded by its own {@code once} key, so a roll
 * cannot repeat and a missed tick cannot be made up later ("no safe point this time = the
 * chance is spent").</p>
 *
 * <p>A successful roll spawns exactly one {@code minecraft:wandering_trader} — no llamas, no
 * emeralds — at {@link DomainGeometry#safeStandingPoint}; its UUID is kept in session state so
 * no second trader is created while the first is alive (including from a later expansion of the
 * same owner, which finds the live entity through its stored UUID).</p>
 *
 * <p>The return point is captured at STABLE with vanilla world collision only, deliberately
 * excluding projected geometry. On end the ordinary entity teleport API moves the trader to
 * that reality-safe point under the domain safe-teleport whitelist; inventory and despawn
 * state remain owned by the vanilla wandering trader.</p>
 */
public final class D61 implements DomainAbility {
    public static final D61 INSTANCE = new D61();
    private D61() {}
    @Override public String id() { return "minecraft:trader_llama"; }
    @Override public String code() { return "D61"; }
    @Override public int priority() { return 100; }

    private static final String TRADER_KEY = "d61_trader";
    private static final String TRADER_TYPE = "minecraft:wandering_trader";
    private static final double CHANCE = 0.20;
    private static final String RETURN_POINT = "d61_reality_return";

    @Override
    public void onStable(DomainSession s) {
        s.putState(RETURN_POINT, DomainGeometry.safeRealityStandingPoint(s, s.centerX(), s.centerZ()));
        if (s.once("d61_stable")) roll(s);
    }

    @Override
    public void onTick(DomainSession s, long now) {
        // >= instead of == so a roll can never be skipped by a long server tick.
        if (s.age(now) >= 200 && s.once("d61_age200")) roll(s);
        if (s.age(now) >= 400 && s.once("d61_age400")) roll(s);
    }

    private void roll(DomainSession s) {
        if (s.world.field_9229.method_43058() >= CHANCE) return;
        if (liveTrader(s) != null) return;
        if (traderNearby(s)) return;
        class_243 point = DomainGeometry.safeStandingPoint(s, s.centerX(), s.centerZ());
        if (point == null) return; // no safe point: this chance is spent
        class_1297 trader = D47D69Support.create(s.world, TRADER_TYPE,
                point.field_1352, point.field_1351, point.field_1350);
        if (trader == null) return;
        if (!(s.world instanceof class_3218 server)) return;
        server.method_8649(trader);
        s.putState(TRADER_KEY, trader.method_5667());
    }

    private static class_1297 liveTrader(DomainSession s) {
        if (!(s.world instanceof class_3218 server)) return null;
        Object stored = s.state(TRADER_KEY, null);
        if (!(stored instanceof UUID uuid)) return null;
        class_1297 trader = server.method_73284(uuid);
        return trader != null && trader.method_5805() && !trader.method_7325() ? trader : null;
    }

    /**
     * A trader of this kind still standing near the interior — in practice the one a previous
     * expansion of the same owner produced. The session state alone cannot see it across
     * expansions and a static owner-keyed map is forbidden, so the world is asked instead.
     */
    private static boolean traderNearby(DomainSession s) {
        return !s.world.method_8333(null, s.bounds(8),
                e -> e instanceof class_1308 && DomainRelations.typeId(e).equals(TRADER_TYPE)).isEmpty();
    }

    @Override
    public void onEnd(DomainSession s) {
        class_1297 trader = liveTrader(s);
        if (trader == null) return;
        class_243 point = s.state(RETURN_POINT, (class_243) null);
        if (point == null) return;
        if (!(s.world instanceof class_3218 world)) return;
        DomainTeleportGuard.push(DomainTeleportGuard.Reason.SAFE_TELEPORT);
        try {
            trader.method_48105(world, point.field_1352, point.field_1351, point.field_1350,
                    Set.of(), trader.method_36454(), trader.method_36455(), false);
        } finally {
            DomainTeleportGuard.pop();
        }
    }
}
