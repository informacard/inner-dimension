package com.innerdimension.transport;

import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.chunk.LoadedChunks;
import com.innerdimension.entity.EnderPearlDildoEntity;
import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.state.InnerState;
import net.minecraft.*;

/** Horizontal chest outputs and physical hopper inputs to the tethered device. */
public final class DildoHopperTransport {
    private static final class_2350[] SIDES = {class_2350.field_11043, class_2350.field_11035,
            class_2350.field_11039, class_2350.field_11034};

    private DildoHopperTransport() {}

    public static boolean outputToChests(class_3218 room, class_2614 hopper) {
        var owner = DynamicDimensionManager.ownerOfWorld(room.method_27983());
        if (owner == null) return false;
        var server = room.method_8503();
        InnerState state = InnerState.get(server.method_30002());
        InnerState.BindingSet set = state.bindingSetOrNull(owner);
        InnerState.Binding binding = set == null ? null : set.bindings.get(set.strongDildoUuid);
        if (binding == null || !binding.authorized || state.lockOfDoll(binding.dollUuid) != null
                || state.lockOfDildo(binding.dildoUuid) != null) return false;
        class_5321<class_1937> key = class_5321.method_29179(class_7924.field_41223, binding.dimId);
        if (DynamicDimensionManager.isInnerWorld(key)) return false;
        class_3218 world = server.method_3847(key);
        if (world == null || !(world.method_66347(binding.dildoUuid) instanceof EnderPearlDildoEntity dildo)
                || dildo.method_31481() || !binding.dollUuid.equals(dildo.getBoundDoll())
                || DollSecurityManager.forPort(dildo, owner).blocked()) return false;
        class_2338 cell = dildo.method_24515();
        // Fixed order: north, south, west, east. Only chests in these four same-height cells.
        for (class_2350 side : SIDES) {
            class_2338 pos = cell.method_10093(side);
            if (!LoadedChunks.contains(world, pos)) continue;
            class_2680 block = world.method_8320(pos);
            if (!(block.method_26204() instanceof class_2281 chest)) continue;
            // Vanilla chest inventory resolution joins both halves of a double chest.
            class_1263 inventory = class_2281.method_17458(chest, block, world, pos, true);
            if (inventory != null && InventoryTransfers.moveOne(hopper, inventory, side.method_10153(), true)) {
                return true;
            }
        }
        return false; // No chest/space: source inventory stays untouched; no dropped-item fallback.
    }

    /** null means ordinary vanilla output; false means matched but full/ambiguous. */
    public static Boolean inputToTethered(class_3218 world, class_2338 pos, class_2614 hopper) {
        class_2350 facing = world.method_8320(pos).method_11654(class_2377.field_11129);
        class_2338 target = pos.method_10093(facing);
        if (target.method_10264() != pos.method_10264() || !LoadedChunks.contains(world, target)) return null;
        var candidates = world.method_8390(EnderPearlDildoEntity.class, new class_238(target),
                entity -> entity.isTethered() && !entity.method_31481() && entity.method_24515().equals(target));
        if (candidates.isEmpty()) return null;
        if (candidates.size() != 1) return false;
        return InventoryTransfers.moveOne(hopper, candidates.get(0).hopperInventory(), facing.method_10153(), false);
    }
}
