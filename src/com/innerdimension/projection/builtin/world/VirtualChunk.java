package com.innerdimension.projection.builtin.world;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1923;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2902;
import net.minecraft.class_3195;
import net.minecraft.class_3449;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_4076;
import net.minecraft.class_6754;
import net.minecraft.class_6763;
import net.minecraft.class_2791.class_6745;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_7225.class_7874;



@Environment(EnvType.CLIENT)
public class VirtualChunk extends class_2818 {
   public final VirtualRenderWorld world;
   private final VirtualChunkSection[] sections;
   private boolean needsLight;

   public VirtualChunk(VirtualRenderWorld world, int x, int z) {
      super(world, new class_1923(x, z));
      this.world = world;
      int sectionCount = world.method_32890();
      this.sections = new VirtualChunkSection[sectionCount];
      int i = 0;

      for (int bottom = world.method_32891(); i < sectionCount; i++) {
         this.sections[i] = new VirtualChunkSection(this, i + bottom << 4);
      }

      this.needsLight = true;
   }

   
   public class_2680 method_12010(class_2338 pos, class_2680 state, int flags) {
      return null;
   }

   public void method_12007(class_2586 blockEntity) {
   }

   public void method_12002(class_1297 entity) {
   }

   public Set<class_2338> method_12021() {
      return Collections.emptySet();
   }

   public class_2826[] method_12006() {
      return this.sections;
   }

   public Collection<Entry<class_2903, class_2902>> method_12011() {
      return Collections.emptySet();
   }

   public void method_12037(class_2903 type, long[] data) {
   }

   public class_2902 method_12032(class_2903 type) {
      return null;
   }

   public int method_12005(class_2903 type, int x, int z) {
      return 0;
   }

   
   public class_3449 method_12181(class_3195 structure) {
      return null;
   }

   public void method_12184(class_3195 structure, class_3449 structureStart) {
   }

   public Map<class_3195, class_3449> method_12016() {
      return Collections.emptyMap();
   }

   public void method_12034(Map<class_3195, class_3449> structureStarts) {
   }

   public LongSet method_12180(class_3195 pStructure) {
      return LongSets.emptySet();
   }

   public void method_12182(class_3195 structure, long reference) {
   }

   public Map<class_3195, LongSet> method_12179() {
      return Collections.emptyMap();
   }

   public void method_12183(Map<class_3195, LongSet> structureReferencesMap) {
   }

   public void method_65063() {
   }

   public boolean method_12044() {
      return false;
   }

   public class_2806 method_12009() {
      return class_2806.field_12805;
   }

   public void method_12041(class_2338 pos) {
   }

   public ShortList[] method_12012() {
      return new ShortList[0];
   }

   
   public class_2487 method_12024(class_2338 pos) {
      return null;
   }

   
   public class_2487 method_20598(class_2338 pos, class_7874 registries) {
      return null;
   }

   public void method_51525( Predicate<class_2680> roughFilter,  BiConsumer<class_2338, class_2680> output) {
      this.world
         .blockStates
         .forEach(
            (blockPos, state) -> {
               if (class_4076.method_18675(blockPos.method_10263()) == this.field_34538.field_9181
                  && class_4076.method_18675(blockPos.method_10260()) == this.field_34538.field_9180
                  && roughFilter.test(state)) {
                  output.accept(blockPos, state);
               }
            }
         );
   }

   public class_6763<class_2248> method_12013() {
      return class_6754.method_39361();
   }

   public class_6763<class_3611> method_12014() {
      return class_6754.method_39361();
   }

   public class_6745 method_39296(long time) {
      throw new UnsupportedOperationException();
   }

   public long method_12033() {
      return 0L;
   }

   public void method_12028(long amount) {
   }

   public boolean method_12038() {
      return this.needsLight;
   }

   public void method_12020(boolean lightCorrect) {
      this.needsLight = lightCorrect;
   }

   
   public class_2586 method_8321(class_2338 pos) {
      return this.world.method_8321(pos);
   }

   public class_2680 method_8320(class_2338 pos) {
      return this.world.method_8320(pos);
   }

   public class_3610 method_8316(class_2338 pos) {
      return this.world.method_8316(pos);
   }
}
