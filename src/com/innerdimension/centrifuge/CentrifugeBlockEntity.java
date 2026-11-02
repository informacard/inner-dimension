package com.innerdimension.centrifuge;

import com.innerdimension.block.ModBlocks;
import com.nonid.potion.NonPotions;
import java.util.*;
import net.minecraft.*;

public final class CentrifugeBlockEntity extends class_2586 implements class_1263,
        net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<class_2338> {
    public static final int INPUT = 0, GUNPOWDER = 1, BOTTLES = 2, ENERGY = 3, SIZE = 3;
    private static final class_1792 GUNPOWDER_ITEM = item("gunpowder");
    private static final class_1792 ENERGY_ITEM = item("lime_stained_glass_pane");
    private final Map<class_2960, Integer> liquids = new LinkedHashMap<>();
    private final class_1799[] slots = {class_1799.field_8037, class_1799.field_8037, class_1799.field_8037};
    private final Map<class_2960, Integer> mixedLiquids = new LinkedHashMap<>();
    private int energy;
    private int bottleMl = CentrifugeBottleData.LEGACY_BOTTLE_ML;

    public CentrifugeBlockEntity(class_2338 pos, class_2680 state) { super(ModBlocks.CENTRIFUGE_BLOCK_ENTITY, pos, state); }
    @Override public class_2561 method_5476() { return class_2561.method_43471("container.inner_dimension.centrifuge"); }
    @Override public class_2338 getScreenOpeningData(class_3222 player) { return method_11016(); }
    @Override public class_1703 createMenu(int syncId, class_1661 inventory, class_1657 player) {
        return new CentrifugeScreenHandler(syncId, inventory, this);
    }
    @Override public int method_5439() { return SIZE; }
    @Override public boolean method_5442() { return liquids.isEmpty() && Arrays.stream(slots).allMatch(class_1799::method_7960); }
    @Override public class_1799 method_5438(int slot) {
        if (slot >= INPUT && slot <= BOTTLES) return slots[slot];
        return class_1799.field_8037;
    }
    /** UI metadata is never part of the droppable machine inventory. */
    public class_1799 storageSnapshot() {
        class_1799 state = new class_1799(ENERGY_ITEM, energy + 1);
        CentrifugeBottleData.writeStorage(state, liquids, bottleMl, inputStatus());
        return state;
    }
    @Override public class_1799 method_5434(int slot, int amount) {
        if (slot < INPUT || slot > BOTTLES || amount <= 0) return class_1799.field_8037;
        class_1799 taken = slots[slot].method_7971(amount);
        if (slots[slot].method_7960()) slots[slot] = class_1799.field_8037;
        if (!taken.method_7960()) method_5431();
        return taken;
    }
    @Override public class_1799 method_5441(int slot) {
        if (slot < INPUT || slot > BOTTLES) return class_1799.field_8037;
        class_1799 removed = slots[slot]; slots[slot] = class_1799.field_8037; method_5431(); return removed;
    }
    @Override public void method_5447(int slot, class_1799 incoming) {
        if (slot < INPUT || slot > BOTTLES) return;
        slots[slot] = incoming == null || incoming.method_7960() ? class_1799.field_8037 : incoming;
        if (!slots[slot].method_7960()) slots[slot].method_7939(Math.min(slots[slot].method_7947(), slots[slot].method_7914()));
        method_5431();
    }
    @Override public boolean method_5437(int slot, class_1799 stack) {
        if (slot == INPUT) return (NonPotions.isLiquidBottle(stack)||com.innerdimension.item.CreativeSemenBottle.isBottle(stack)||stack.method_31574(net.minecraft.class_1802.field_20417));
        if (slot == GUNPOWDER) return isGunpowder(stack);
        if (slot == BOTTLES) return stack.method_31574(class_1802.field_8469);
        return false;
    }
    @Override public void method_5431() { super.method_5431(); }
    @Override public boolean method_5443(class_1657 player) { return class_1263.method_49106(this, player, 4.0F); }
    @Override public void method_5448() { liquids.clear(); mixedLiquids.clear(); Arrays.fill(slots, class_1799.field_8037); energy = 0; method_5431(); }

    /** Processing occurs after inventory clicks finish, never inside a slot setter. */
    public static void tick(class_1937 world, class_2338 pos, class_2680 state, CentrifugeBlockEntity machine) {
        machine.processInput();
    }
    private void processInput() {
        class_1799 input = slots[INPUT];
        if (input.method_7960()) return;
        if (input.method_31574(class_1802.field_8469)) { moveReturnedBottles(); return; }
        var contents = CentrifugeBottleData.read(input);
        if (!contents.provenanceKnown()) return;
        int count = input.method_7947();
        boolean mixed = contents.sources().size() > 1;
        if (mixed && energy + slots[GUNPOWDER].method_7947() * 16 < count) return;
        for (var entry : contents.sources().entrySet()) {
            long total = (long)liquids.getOrDefault(entry.getKey(), 0) + (long)entry.getValue() * count;
            if (total > Integer.MAX_VALUE) return;
        }
        if (mixed) {
            int fuel = Math.max(0, (count - energy + 15) / 16);
            slots[GUNPOWDER].method_7934(fuel);
            energy += fuel * 16 - count;
        }
        contents.sources().forEach((id, ml) -> liquids.merge(id, ml * count, Integer::sum));
        if (mixed) contents.sources().forEach((id, ml) -> mixedLiquids.merge(id, ml * count, Integer::sum));
        bottleMl = contents.totalMl();
        slots[INPUT] = new class_1799(class_1802.field_8469, count);
        moveReturnedBottles();
        method_5431();
    }
    private void moveReturnedBottles() {
        class_1799 returned = slots[INPUT], bottles = slots[BOTTLES];
        if (!returned.method_31574(class_1802.field_8469)) return;
        int moved = Math.min(returned.method_7947(), 64 - bottles.method_7947());
        if (moved <= 0) return;
        slots[BOTTLES] = new class_1799(class_1802.field_8469, bottles.method_7947() + moved);
        returned.method_7934(moved);
        if (returned.method_7960()) slots[INPUT] = class_1799.field_8037;
        method_5431();
    }
    private String inputStatus() {
        class_1799 input = slots[INPUT];
        if (input.method_7960()) return "";
        if (input.method_31574(class_1802.field_8469)) return "bottles_full";
        var contents = CentrifugeBottleData.read(input);
        if (!contents.provenanceKnown()) return "missing_data";
        if (contents.sources().size() > 1 && energy + slots[GUNPOWDER].method_7947() * 16 < input.method_7947()) return "no_energy";
        return "";
    }

    public boolean extractsMixed(class_2960 species) { return mixedLiquids.getOrDefault(species, 0) > 0; }
    public class_1799 extract(class_2960 species) {
        int available = liquids.getOrDefault(species, 0);
        if (available < bottleMl || slots[BOTTLES].method_7960()) return class_1799.field_8037;
        class_1799 output = CentrifugeBottleData.createPureBottle(species);
        CentrifugeBottleData.write(output, Map.of(species, bottleMl), bottleMl);
        int remainingMixed = mixedLiquids.getOrDefault(species, 0) - bottleMl;
        if (remainingMixed > 0) mixedLiquids.put(species, remainingMixed); else mixedLiquids.remove(species);
        slots[BOTTLES].method_7934(1);
        if (available == bottleMl) liquids.remove(species); else liquids.put(species, available - bottleMl);
        method_5431();
        return output;
    }
    public boolean discard(class_2960 species) {
        if (liquids.remove(species) == null) return false;
        mixedLiquids.remove(species);
        method_5431(); return true;
    }

    @Override protected void method_11014(class_11368 view) {
        super.method_11014(view);
        // Preserve old inventories while migrating to actual persistent ItemStacks.
        int[] legacy = {view.method_71424("InputEmptyBottles", 0), view.method_71424("Gunpowder", 0), view.method_71424("EmptyBottles", 0)};
        for (int i = 0; i < 3; i++) slots[i] = view.method_71426("Slot" + i, class_1799.field_24671)
                .orElse(legacy[i] <= 0 ? class_1799.field_8037 : new class_1799(i == GUNPOWDER ? GUNPOWDER_ITEM : class_1802.field_8469, Math.min(64, legacy[i])));
        energy = Math.max(0, Math.min(16, view.method_71424("Energy", 0)));
        bottleMl = Math.max(1, view.method_71424("BottleMl", CentrifugeBottleData.LEGACY_BOTTLE_ML));
        mixedLiquids.clear(); mixedLiquids.putAll(CentrifugeBottleData.decodeSources(view.method_71428("MixedLiquids", "")));
        liquids.clear(); liquids.putAll(CentrifugeBottleData.decodeSources(view.method_71428("Liquids", "")));
    }
    @Override protected void method_11007(class_11372 view) {
        super.method_11007(view);
        for (int i = 0; i < 3; i++) if (!slots[i].method_7960()) view.method_71477("Slot" + i, class_1799.field_24671, slots[i]);
        view.method_71465("Energy", energy);
        view.method_71465("BottleMl", bottleMl);
        view.method_71469("MixedLiquids", CentrifugeBottleData.encodeSources(mixedLiquids));
        view.method_71469("Liquids", CentrifugeBottleData.encodeSources(liquids));
    }
    private static class_1792 item(String path) { return class_7923.field_41178.method_63535(class_2960.method_60655("minecraft", path)); }
    public static boolean isGunpowder(class_1799 stack) { return stack.method_31574(GUNPOWDER_ITEM); }
}

