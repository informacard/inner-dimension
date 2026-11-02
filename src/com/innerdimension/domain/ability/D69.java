package com.innerdimension.domain.ability;

import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import com.innerdimension.domain.world.DomainWorldActions;
import net.minecraft.*;
import java.util.*;

/**
 * D69 minecraft:bee (yellow) — 花甸 / flower meadow.
 *
 * <p>World transaction: every 40 ticks the ability walks a rotating slice of at most 16 real
 * grass blocks that have real air above them. Each candidate is re-checked at write time
 * ({@link DomainWorldActions#writable}), positions occupied by a projected solid are skipped
 * (the same mixed geometry the rest of the layer uses decides), the flower state must be able to
 * survive where it is being placed, and each accepted block has a 10% server-side chance of
 * becoming a random single-block flower. Flowers are permanent.</p>
 *
 * <p>Healing: each tick, any living entity whose feet stand on a real single-block flower
 * receives a Regeneration I lease; the moment it steps off, this session's contribution is
 * revoked explicitly, so an external regeneration instance is restored by the lease manager.
 * Projected flowers never qualify — only real world blocks are read here. Both allies and
 * enemies are healed.</p>
 *
 * <p>Source gate: this ability is only ever engraved when the bee source was unlocked through
 * the pure honey-bottle provenance ledger. That check lives in
 * {@code com.innerdimension.progression.traits.NoNBottleSourceResolver} (with the NoN bridge
 * providing the mixed-source rejection); it is not duplicated here and no honey bottle without a
 * recorded source can reach this class.</p>
 */
public final class D69 implements DomainAbility {
    public static final D69 INSTANCE = new D69();
    private D69() {}
    @Override public String id() { return "minecraft:bee"; }
    @Override public String code() { return "D69"; }
    @Override public int priority() { return 100; }

    private static final String REGEN_KEY = "d69_regen";
    private static final long PLANT_INTERVAL = 40;
    private static final int BUDGET = 16;
    private static final double CHANCE = 0.10;
    // A lease refreshes at half-duration. Keep vanilla Regeneration I's 50-tick
    // pulse in that live window; a 40-tick lease refreshed above 20 never heals.
    public static final int REGEN_TICKS = 100;

    /** Ordinary survival-plantable single-block flowers; tall and potted forms are excluded. */
    private static final String[] FLOWERS = {
            "minecraft:dandelion", "minecraft:poppy", "minecraft:blue_orchid", "minecraft:allium",
            "minecraft:azure_bluet", "minecraft:red_tulip", "minecraft:orange_tulip",
            "minecraft:white_tulip", "minecraft:pink_tulip", "minecraft:oxeye_daisy",
            "minecraft:cornflower", "minecraft:lily_of_the_valley"
    };

    @Override
    public void onTick(DomainSession s, long now) {
        if (s.dueSelf("d69_plant", PLANT_INTERVAL, now)) {
            s.armSelf("d69_plant", now);
            plant(s);
        }
        regenerate(s, now);
    }

    private void plant(DomainSession s) {
        class_2248 grass = D47D69Support.block("minecraft:grass_block");
        if (grass == null || !(s.world instanceof class_3218 world)) return;
        List<class_2338> candidates = new ArrayList<>();
        for (class_2338 pos : s.blocksIn()) {
            if (world.method_8320(pos).method_26204() != grass) continue;
            class_2338 above = pos.method_10084();
            if (!s.containsBlock(above)) continue;
            if (!world.method_8320(above).method_26215()) continue; // real air required
            if (DomainGeometry.solid(s.world, new class_238(above))) continue; // projected solid
            if (!DomainWorldActions.writable(s, above)) continue;   // permission re-check
            candidates.add(above);
        }
        if (candidates.isEmpty()) return;
        for (class_2338 pos : DomainWorldActions.slice(s, "d69_flowers", candidates, BUDGET)) {
            if (world.field_9229.method_43058() >= CHANCE) continue;
            class_2248 flower = D47D69Support.block(FLOWERS[world.field_9229.method_43048(FLOWERS.length)]);
            if (flower == null) continue;
            class_2680 state = flower.method_9564();
            if (!state.method_26184(s.world, pos)) continue; // cannot survive here
            DomainWorldActions.setBlock(s, pos, state, 3);
        }
    }

    private static void regenerate(DomainSession s, long now) {
        Set<UUID> current = new HashSet<>();
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D69")) {
            class_2338 feet = flowerPosition(entity.method_23317(),entity.method_23318(),entity.method_23321());
            if (!D47D69Support.realFlower(s.world, feet)) continue;
            EffectLeaseManager.apply(entity, "minecraft:regeneration", "D69", s.sessionId,
                    REGEN_TICKS, 0, now);
            current.add(entity.method_5667());
        }
        Object previous = s.state(REGEN_KEY, null);
        if (previous instanceof Set<?> set) {
            class_6880<class_1291> regeneration = EffectLeaseManager.effect("minecraft:regeneration");
            for (Object value : set) {
                if (!(value instanceof UUID uuid) || current.contains(uuid)) continue;
                class_1309 entity = D47D69Support.living(s.world, uuid);
                if (entity != null) EffectLeaseManager.revoke(entity, regeneration, s.sessionId, "D69", now);
            }
        }
        s.putState(REGEN_KEY, current);
    }

    public static class_2338 flowerPosition(double x,double feetY,double z) {
        return class_2338.method_49638(new class_243(x,feetY+.001,z));
    }
}
