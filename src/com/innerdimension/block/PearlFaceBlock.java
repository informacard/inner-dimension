package com.innerdimension.block;

import com.innerdimension.pearl.PearlSurfaceTransport;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_3965;
import net.minecraft.class_4970.class_2251;

/**
 * The floor block of an inner room. Sneak + right-clicking it sends the player back to the
 * point they entered from and sweeps everything lying on the pearl surface out with them.
 *
 * <p>The teleport is triggered from the block's own use hook, so it works through vanilla's
 * client/server interaction flow without an extra Fabric event. The same transaction is also
 * reachable through {@code UseBlockCallback}, which covers the case where a held item makes vanilla
 * skip this hook; both paths collapse into one server-side run per tick.</p>
 */
public class PearlFaceBlock extends class_2248 {
    public PearlFaceBlock(class_2251 settings) {
        super(settings);
    }

    @Override
    protected class_1269 method_55766(class_2680 state, class_1937 world, class_2338 pos,
                                     class_1657 player, class_3965 hit) {
        if (player instanceof class_3222 serverPlayer
                && PearlSurfaceTransport.leaveFromSurface(serverPlayer, world)) {
            return class_1269.field_5812;
        }
        return class_1269.field_5811;
    }
}
