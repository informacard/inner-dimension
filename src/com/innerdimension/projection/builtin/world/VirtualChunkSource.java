package com.innerdimension.projection.builtin.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2791;
import net.minecraft.class_2802;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_3568;


@Environment(EnvType.CLIENT)
public class VirtualChunkSource extends class_2802 {
   private final VirtualRenderWorld world;
   private final Long2ObjectMap<VirtualChunk> chunks = new Long2ObjectOpenHashMap();

   public VirtualChunkSource(VirtualRenderWorld world) {
      this.world = world;
   }

   public class_1937 getLevel() {
      return this.world;
   }

   @Override public net.minecraft.class_1922 method_16399() { return world; }
   @Override public net.minecraft.class_8527 method_12246(int x,int z) { return getChunkForLighting(x,z); }
   public class_2791 getChunkForLighting(int x, int z) {
      return (class_2791)this.chunks
         .computeIfAbsent(
            class_1923.method_8331(x, z), packedPos -> new VirtualChunk(this.world, class_1923.method_8325(packedPos), class_1923.method_8332(packedPos))
         );
   }

   
   public class_2818 method_12126(int x, int z, boolean load) {
      return null;
   }

   
   public class_2791 method_12121(int x, int z, class_2806 status, boolean load) {
      return this.getChunkForLighting(x, z);
   }

   public void method_12127(BooleanSupplier hasTimeLeft, boolean tickChunks) {
   }

   public String method_12122() {
      return "VirtualChunkSource";
   }

   public int method_14151() {
      return 0;
   }

   public class_3568 method_12130() {
      return this.world.method_22336();
   }
}
