package com.innerdimension.item;

import com.innerdimension.config.InnerConfig;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1792.class_1793;
import net.minecraft.class_1799;
import net.minecraft.class_1937;

public class EnderPearlPlugItem extends class_1792 implements Trinket {
    private static final Map<UUID, Integer> WEAR_TICKS = new ConcurrentHashMap<>();

    public static void clearWearTicks() {
        WEAR_TICKS.clear();
    }

    public static void forgetWearTicks(UUID player) {
        WEAR_TICKS.remove(player);
    }

    public EnderPearlPlugItem(class_1793 settings) {
        super(settings);
    }

    @Override
    public void tick(class_1799 stack, SlotReference slot, class_1309 entity) {
        class_1937 world = entity.method_73183();
        if (world == null || world.method_8608()) {
            return;
        }
        UUID id = entity.method_5667();
        int ticks = WEAR_TICKS.merge(id, 1, Integer::sum);
        long target = Math.max(1L, InnerConfig.get().transformSeconds) * 20;
        if (ticks >= target) {
            WEAR_TICKS.remove(id);
            slot.inventory().method_5447(slot.index(), new class_1799(ModItems.STICKY_ENDER_PEARL_PLUG));
            slot.inventory().markUpdate();
                        if (entity instanceof net.minecraft.class_3222 player)
                com.innerdimension.progression.InnerAdvancements.grant(player, "transformation");
        }
    }

    @Override
    public void onEquip(class_1799 stack, SlotReference slot, class_1309 entity) {
        WEAR_TICKS.remove(entity.method_5667());
    }

    @Override
    public void onUnequip(class_1799 stack, SlotReference slot, class_1309 entity) {
        WEAR_TICKS.remove(entity.method_5667());
    }
}
