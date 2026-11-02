package com.innerdimension.item;

import com.innerdimension.compat.NonBottlingCompat;
import com.nonid.api.NonLiquidComposition;
import com.nonid.api.NonLiquidTankSnapshot;
import com.nonid.api.NonPlayerApi;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1792.class_1793;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_3222;

/**
 * Auto-bottling plug: the iron plug's accessory effects come from the definition installed by
 * {@link NonBottlingCompat}, and this class adds the one extra behaviour — while the worn player's
 * NoN liquid tank holds more than a third of its capacity, one glass bottle is turned into the
 * matching NoN liquid bottle. No bottling animation or sound is played.
 */
public class AutoBottlingPlugItem extends class_1792 implements Trinket {
    /** Half a second between attempts; a single bottle always drains the tank back below the gate. */
    private static final int CHECK_INTERVAL_TICKS = 10;
    /** Mirrors NoN's own bottle requirement: one bottle is worth 30% of the tank. */
    private static final double BOTTLE_FRACTION = 0.3D;

    private static final Map<UUID, Integer> TICKS = new ConcurrentHashMap<>();

    public AutoBottlingPlugItem(class_1793 settings) {
        super(settings);
    }

    public static void forget(UUID player) {
        if (player != null) TICKS.remove(player);
    }

    public static void clear() {
        TICKS.clear();
    }

    @Override
    public void tick(class_1799 stack, SlotReference slot, class_1309 entity) {
        class_1937 world = entity.method_73183();
        if (world == null || world.method_8608() || !(entity instanceof class_3222 player)) return;
        UUID id = player.method_5667();
        if (TICKS.merge(id, 1, Integer::sum) < CHECK_INTERVAL_TICKS) return;
        TICKS.put(id, 0);
        tryBottle(player);
    }

    @Override
    public void onEquip(class_1799 stack, SlotReference slot, class_1309 entity) {
        TICKS.remove(entity.method_5667());
    }

    @Override
    public void onUnequip(class_1799 stack, SlotReference slot, class_1309 entity) {
        TICKS.remove(entity.method_5667());
    }

    private static void tryBottle(class_3222 player) {
        NonLiquidTankSnapshot tank = NonPlayerApi.getLiquidTank(player);
        int stored = tank.storedMl();
        int capacity = tank.capacityMl();
        if (stored <= 0 || capacity <= 0) return;
        // Strictly more than one third of the capacity.
        if ((long) stored * 3L <= (long) capacity) return;
        if (tank.composition() == NonLiquidComposition.EMPTY) return;
        class_1799 bottle = findGlassBottle(player);
        if (bottle == null) return;
        class_1799 result = NonBottlingCompat.bottleForTank(player);
        if (result == null || result.method_7960()) return;
        if (NonPlayerApi.drainLiquid(player, bottleCost(capacity)) <= 0) return;
        bottle.method_7934(1);
        // Inventory.markDirty(): method_5448() is clear(), never an update notification.
        player.method_31548().method_5431();
        // Deliberately silent: the auto plug must not replay the manual bottling feedback.
        NonBottlingCompat.giveOrDrop(player, result);
    }

    /** One bottle is worth 30% of the capacity, exactly like NoN's manual fill. */
    public static int bottleCost(int capacityMl) {
        if (capacityMl <= 0) return 1;
        return Math.max(1, (int) Math.ceil(capacityMl * BOTTLE_FRACTION));
    }

    /** Prefer the hands, then any inventory slot, so the plug also works while both hands are busy. */
    private static class_1799 findGlassBottle(class_1657 player) {
        class_1799 offhand = player.method_6079();
        if (isGlassBottle(offhand)) return offhand;
        class_1799 main = player.method_5998(net.minecraft.class_1268.field_5808);
        if (isGlassBottle(main)) return main;
        var inventory = player.method_31548();
        for (int i = 0; i < inventory.method_5439(); i++) {
            class_1799 stack = inventory.method_5438(i);
            if (isGlassBottle(stack)) return stack;
        }
        return null;
    }

    private static boolean isGlassBottle(class_1799 stack) {
        return stack != null && !stack.method_7960() && stack.method_31574(class_1802.field_8469);
    }
}
