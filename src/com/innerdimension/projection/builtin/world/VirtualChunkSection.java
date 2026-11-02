package com.innerdimension.projection.builtin.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2680;
import net.minecraft.class_2826;
import net.minecraft.class_3610;
import net.minecraft.class_4076;

@Environment(EnvType.CLIENT)
public class VirtualChunkSection extends class_2826 {
   public final VirtualChunk owner;
   public final int xStart;
   public final int yStart;
   public final int zStart;

   public VirtualChunkSection(VirtualChunk owner, int yBase) {
      super(owner.world.method_74142());
      this.owner = owner;
      this.xStart = owner.method_12004().method_8326();
      this.yStart = yBase;
      this.zStart = owner.method_12004().method_8328();
   }

   public class_2680 method_12254(int x, int y, int z) {
      return this.owner.world.getBlockState(x + this.xStart, y + this.yStart, z + this.zStart);
   }

   public class_3610 method_12255(int x, int y, int z) {
      return this.method_12254(x, y, z).method_26227();
   }

   public class_2680 method_12256(int x, int y, int z, class_2680 state, boolean useLocks) {
      throw new UnsupportedOperationException("Chunk sections cannot be mutated in a fake world.");
   }

   public boolean method_38292() {
      class_4076 sectionPos = class_4076.method_18676(this.xStart >> 4, this.yStart >> 4, this.zStart >> 4);
      return this.owner.world.nonEmptyBlockCounts.getShort(sectionPos) == 0;
   }
}
