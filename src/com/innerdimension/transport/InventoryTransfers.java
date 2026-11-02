package com.innerdimension.transport;

import com.innerdimension.exit.ModExitComponents;
import net.minecraft.class_1263;
import net.minecraft.class_1799;
import net.minecraft.class_2350;
import net.minecraft.class_2614;

/** Single-item server-thread commit: copy first, deduct only after insertion succeeds. */
public final class InventoryTransfers {
    private InventoryTransfers() {}

    public static boolean moveOne(class_1263 source, class_1263 target, class_2350 side,
                                  boolean clearExitSource) {
        if (source == target) return false;
        for (int slot = 0; slot < source.method_5439(); slot++) {
            class_1799 original = source.method_5438(slot);
            if (original.method_7960()) continue;
            // method_7971 is split(), NOT copyWithCount(): never use it for a payload copy.
            class_1799 payload = original.method_46651(1);
            if (clearExitSource) ModExitComponents.clear(payload);
            class_1799 remainder = class_2614.method_11260(source, target, payload, side);
            if (!remainder.method_7960()) continue;
            original.method_7934(1);
            if (original.method_7960()) source.method_5447(slot, class_1799.field_8037);
            source.method_5431();
            target.method_5431();
            return true;
        }
        return false;
    }
}
