package com.innerdimension.centrifuge;

import com.mojang.serialization.MapCodec;
import com.innerdimension.block.ModBlocks;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2237;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_4970.class_2251;

public final class CentrifugeBlock extends class_2237 {
    public static final MapCodec<CentrifugeBlock> CODEC = method_54094(CentrifugeBlock::new);

    public CentrifugeBlock(class_2251 settings) { super(settings); }

    @Override protected MapCodec<? extends class_2237> method_53969() { return CODEC; }

    @Override public class_2586 method_10123(class_2338 pos, class_2680 state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Override public <T extends class_2586> net.minecraft.class_5558<T> method_31645(
            class_1937 world, class_2680 state, net.minecraft.class_2591<T> type) {
        return world.method_8608() ? null : method_31618(type,
                ModBlocks.CENTRIFUGE_BLOCK_ENTITY, CentrifugeBlockEntity::tick);
    }

    @Override protected class_1269 method_55766(class_2680 state, class_1937 world, class_2338 pos,
                                                class_1657 player, class_3965 hit) {
        if (!world.method_8608()) {
            class_2586 be = world.method_8321(pos);
            if (be instanceof CentrifugeBlockEntity centrifuge && player instanceof net.minecraft.class_3222 serverPlayer) {
                serverPlayer.method_17355(centrifuge);
            }
        }
        return class_1269.field_5812;
    }
}
