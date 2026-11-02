package com.innerdimension.block;
import net.minecraft.*;
import net.minecraft.class_4970.class_2251;
/** Replaceable decorative skin, one sixteenth of a block thick. */
public final class DomainLayerBlock extends class_2248 {
    private final boolean ceiling;
    public DomainLayerBlock(class_2251 settings, boolean ceiling) { super(settings); this.ceiling = ceiling; }
    @Override protected boolean method_9616(class_2680 state, class_1750 context) { return true; }
    @Override protected class_265 method_9530(class_2680 state, class_1922 world, class_2338 pos, class_3726 context) {
        return class_2248.method_9541(0, ceiling ? 15 : 0, 0, 16, ceiling ? 16 : 1, 16);
    }
    @Override protected boolean method_9558(class_2680 state, class_4538 world, class_2338 pos) {
        return !world.method_8320(ceiling ? pos.method_10084() : pos.method_10074()).method_26215();
    }
    @Override protected class_2680 method_9559(class_2680 state, class_4538 world, class_10225 ticks, class_2338 pos,
            class_2350 direction, class_2338 neighborPos, class_2680 neighbor, class_5819 random) {
        return method_9558(state, world, pos) ? state : class_2248.method_9531(0);
    }
}
