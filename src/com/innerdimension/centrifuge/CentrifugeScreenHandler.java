package com.innerdimension.centrifuge;

import com.innerdimension.block.ModBlocks;
import net.minecraft.class_1263;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2338;

/** Three real machine slots plus synced, non-interactive display slots for the liquid list. */
public final class CentrifugeScreenHandler extends class_1703 {
    private static final int MACHINE_SYNC_SLOTS = CentrifugeBlockEntity.SIZE + 1;
    private final class_1263 inventory;

    public CentrifugeScreenHandler(int syncId, class_1661 playerInventory, class_2338 pos) {
        this(syncId, playerInventory, new net.minecraft.class_1277(CentrifugeBlockEntity.SIZE));
    }

    public CentrifugeScreenHandler(int syncId, class_1661 playerInventory, class_1263 inventory) {
        super(ModBlocks.CENTRIFUGE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        method_17359(inventory, CentrifugeBlockEntity.SIZE);
        method_7621(new InputSlot(inventory, CentrifugeBlockEntity.INPUT, 18, 35));
        method_7621(new GunpowderSlot(inventory, CentrifugeBlockEntity.GUNPOWDER, 18, 71));
        method_7621(new BottleSlot(inventory, CentrifugeBlockEntity.BOTTLES, 18, 107));
        // A separate, non-interactive inventory transports UI data. Vanilla drops only the three real slots.
        net.minecraft.class_1277 display = new net.minecraft.class_1277(1) {
            @Override public class_1799 method_5438(int slot) {
                return inventory instanceof CentrifugeBlockEntity machine ? machine.storageSnapshot() : super.method_5438(slot);
            }
        };
        method_7621(new DisplaySlot(display, 0, -1000, -1000));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            method_7621(new class_1735(playerInventory, col + row * 9 + 9, 69 + col * 18, 154 + row * 18));
        for (int col = 0; col < 9; col++) method_7621(new class_1735(playerInventory, col, 69 + col * 18, 212));
    }

    public void act(class_1657 player, net.minecraft.class_2960 species, boolean discard) {
        if (!method_7597(player) || !(inventory instanceof CentrifugeBlockEntity machine)) return;
        if (discard) {
            machine.discard(species);
        } else {
            boolean mixed = machine.extractsMixed(species);
            class_1799 output = machine.extract(species);
            if (mixed && !output.method_7960() && player instanceof net.minecraft.class_3222 serverPlayer)
                com.innerdimension.progression.InnerAdvancements.grant(serverPlayer, "purification");
            if (!output.method_7960() && !player.method_31548().method_7394(output)) player.method_7329(output, false, false);
        }
        method_7605();
    }
    @Override public class_1799 method_7601(class_1657 player, int index) {
        if (index < 0 || index >= field_7761.size()) return class_1799.field_8037;
        class_1735 slot = field_7761.get(index);
        if (!slot.method_7681()) return class_1799.field_8037;
        class_1799 original = slot.method_7677();
        class_1799 copy = original.method_7972();
        if (index < 3) {
            if (!method_7616(original, MACHINE_SYNC_SLOTS, MACHINE_SYNC_SLOTS + 36, true)) return class_1799.field_8037;
        } else if (index >= MACHINE_SYNC_SLOTS) {
            int target = machineSlot(original);
            if (target >= 0 && method_7616(original, target, target + 1, false)) {
                // Moved into the machine.
            } else if (index < MACHINE_SYNC_SLOTS + 27) {
                if (!method_7616(original, MACHINE_SYNC_SLOTS + 27, MACHINE_SYNC_SLOTS + 36, false)) return class_1799.field_8037;
            } else if (!method_7616(original, MACHINE_SYNC_SLOTS, MACHINE_SYNC_SLOTS + 27, false)) return class_1799.field_8037;
        } else return class_1799.field_8037;
        if (original.method_7960()) slot.method_7673(class_1799.field_8037); else slot.method_7668();

        if (original.method_7947() == copy.method_7947()) return class_1799.field_8037;
        slot.method_7667(player, original);
        return copy;
    }

    @Override public boolean method_7597(class_1657 player) { return inventory.method_5443(player); }

    private static int machineSlot(class_1799 stack) {
        if ((com.nonid.potion.NonPotions.isLiquidBottle(stack)||com.innerdimension.item.CreativeSemenBottle.isBottle(stack)||stack.method_31574(net.minecraft.class_1802.field_20417))) return 0;
        if (CentrifugeBlockEntity.isGunpowder(stack)) return 1;
        if (stack.method_31574(net.minecraft.class_1802.field_8469)) return 2;
        return -1;
    }
    private static final class InputSlot extends class_1735 { InputSlot(class_1263 i,int n,int x,int y){super(i,n,x,y);} @Override public boolean method_7680(class_1799 s){return (com.nonid.potion.NonPotions.isLiquidBottle(s)||com.innerdimension.item.CreativeSemenBottle.isBottle(s)||s.method_31574(net.minecraft.class_1802.field_20417));} }
    private static final class GunpowderSlot extends class_1735 { GunpowderSlot(class_1263 i,int n,int x,int y){super(i,n,x,y);} @Override public boolean method_7680(class_1799 s){return CentrifugeBlockEntity.isGunpowder(s);} }
    private static final class BottleSlot extends class_1735 { BottleSlot(class_1263 i,int n,int x,int y){super(i,n,x,y);} @Override public boolean method_7680(class_1799 s){return s.method_31574(net.minecraft.class_1802.field_8469);} }
    private static final class DisplaySlot extends class_1735 { DisplaySlot(class_1263 i,int n,int x,int y){super(i,n,x,y);} @Override public boolean method_7680(class_1799 s){return false;} @Override public boolean method_7674(class_1657 p){return false;} @Override public boolean method_7682(){return false;} }
}


