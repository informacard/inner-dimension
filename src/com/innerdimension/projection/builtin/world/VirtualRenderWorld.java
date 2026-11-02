package com.innerdimension.projection.builtin.world;


import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10286;
import net.minecraft.class_11749;
import net.minecraft.class_12205;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1508;
import net.minecraft.class_1657;
import net.minecraft.class_1845;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_1959;
import net.minecraft.class_22;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_2394;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_269;
import net.minecraft.class_2784;
import net.minecraft.class_2791;
import net.minecraft.class_2802;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_3568;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_3612;
import net.minecraft.class_4076;
import net.minecraft.class_4543;
import net.minecraft.class_5269;
import net.minecraft.class_5362;
import net.minecraft.class_5577;
import net.minecraft.class_5712;
import net.minecraft.class_6012;
import net.minecraft.class_6756;
import net.minecraft.class_6880;
import net.minecraft.class_765;
import net.minecraft.class_7699;
import net.minecraft.class_8921;
import net.minecraft.class_9209;
import net.minecraft.class_9895;
import net.minecraft.class_1937.class_7867;
import net.minecraft.class_2338.class_2339;
import net.minecraft.class_5217.class_12064;
import net.minecraft.class_5712.class_7397;


@Environment(EnvType.CLIENT)
public class VirtualRenderWorld extends class_1937 {
   protected final class_1937 level;
   protected final int minBuildHeight;
   protected final int height;
   protected final class_2382 biomeOffset;
   protected final VirtualChunkSource chunkSource;
   protected final class_3568 lightEngine;
   protected final Map<class_2338, class_2680> blockStates = new HashMap<>();
   protected final Map<class_2338, class_2586> blockEntities = new HashMap<>();
   protected final Object2ShortMap<class_4076> nonEmptyBlockCounts = new Object2ShortOpenHashMap();
   protected final class_5577<class_1297> entityGetter = new VirtualLevelEntityGetter();
   protected final class_2339 scratchPos = new class_2339();
   protected final Runnable onBlockUpdated;
   private int externalPackedLight = 0;

   public VirtualRenderWorld(class_1937 level, int minBuildHeight, int height, class_2382 biomeOffset, Runnable onBlockUpdated) {
      super((class_5269)level.method_8401(), level.method_27983(), level.method_30349(), level.method_40134(), true, false, 0L, 0);
      this.level = level;
      this.minBuildHeight = nextMultipleOf16(minBuildHeight);
      this.height = nextMultipleOf16(height);
      this.biomeOffset = biomeOffset;
      this.chunkSource = new VirtualChunkSource(this);
      this.lightEngine = new class_3568(this.chunkSource, true, false);
      this.onBlockUpdated = onBlockUpdated;
   }

   @Override public net.minecraft.class_12204 method_75598() { return level.method_75598(); }

   public class_12205 method_75728() {
      return this.level.method_75728();
   }

   public void method_27873(class_12064 spawnPoint) {
      this.level.method_27873(spawnPoint);
   }

   public class_12064 method_74854() {
      return this.level.method_74854();
   }

   public class_2784 method_8621() {
      return this.level.method_8621();
   }

   public static int nextMultipleOf16(int a) {
      return a < 0 ? -((Math.abs(a) - 1 | 15) + 1) : (a - 1 | 15) + 1;
   }

   public void setExternalLight(int packedLight) {
      this.externalPackedLight = packedLight;
   }

   public void resetExternalLight() {
      this.externalPackedLight = 0;
   }

   public void method_8413(class_2338 pos, class_2680 oldState, class_2680 newState, int flags) {
      this.onBlockUpdated.run();
   }

   public int method_8314(class_1944 lightType, class_2338 blockPos) {
      int selfBrightness = super.method_8314(lightType, blockPos);
      return lightType == class_1944.field_9284
         ? Math.max(selfBrightness, class_765.method_24187(this.externalPackedLight))
         : Math.max(selfBrightness, class_765.method_24186(this.externalPackedLight));
   }

   public void clear() {
      this.blockStates.clear();
      this.blockEntities.clear();
      this.nonEmptyBlockCounts.forEach((sectionPos, nonEmptyBlockCount) -> {
         if (nonEmptyBlockCount > 0) {
            this.lightEngine.method_15551(sectionPos, true);
         }
      });
      this.nonEmptyBlockCounts.clear();
   }

   public void setBlockEntities(Collection<class_2586> blockEntities) {
      this.blockEntities.clear();
      blockEntities.forEach(this::method_8438);
   }

   public void runLightEngine() {
      Set<class_1923> chunkPosSet = new ObjectOpenHashSet();
      this.nonEmptyBlockCounts.object2ShortEntrySet().forEach(entry -> {
         if (entry.getShortValue() > 0) {
            chunkPosSet.add(((class_4076)entry.getKey()).method_18692());
         }
      });

      for (class_1923 chunkPos : chunkPosSet) {
         this.lightEngine.method_51471(chunkPos);
      }

      this.lightEngine.method_15516();
   }

   public class_2818 method_8497(int chunkX, int chunkZ) {
      return (class_2818)this.method_22342(chunkX, chunkZ, class_2806.field_12803);
   }

   public class_2791 method_22350(class_2338 pos) {
      return this.method_8497(class_4076.method_18675(pos.method_10263()), class_4076.method_18675(pos.method_10260()));
   }

   public boolean method_30092(class_2338 pos, class_2680 newState, int flags, int recursionLeft) {
      if (this.method_31606(pos)) {
         return false;
      } else {
         pos = pos.method_10062();
         class_2680 oldState = this.method_8320(pos);
         if (oldState == newState) {
            return false;
         } else {
            this.blockStates.put(pos, newState);
            class_4076 sectionPos = class_4076.method_18682(pos);
            short nonEmptyBlockCount = this.nonEmptyBlockCounts.getShort(sectionPos);
            boolean prevEmpty = nonEmptyBlockCount == 0;
            if (!oldState.method_26215()) {
               nonEmptyBlockCount--;
            }

            if (!newState.method_26215()) {
               nonEmptyBlockCount++;
            }

            this.nonEmptyBlockCounts.put(sectionPos, nonEmptyBlockCount);
            boolean nowEmpty = nonEmptyBlockCount == 0;
            if (prevEmpty != nowEmpty) {
               this.lightEngine.method_15551(sectionPos, nowEmpty);
            }

            this.lightEngine.method_15513(pos);
            return true;
         }
      }
   }

   public class_3568 method_22336() {
      return this.lightEngine;
   }

   public class_2680 method_8320(class_2338 pos) {
      if (this.method_31606(pos)) {
         return class_2246.field_10243.method_9564();
      } else {
         class_2680 state = this.blockStates.get(pos);
         return state != null ? state : class_2246.field_10124.method_9564();
      }
   }

   public class_2680 getBlockState(int x, int y, int z) {
      return this.method_8320(this.scratchPos.method_10103(x, y, z));
   }

   public class_3610 method_8316(class_2338 pos) {
      return this.method_31606(pos) ? class_3612.field_15906.method_15785() : this.method_8320(pos).method_26227();
   }

   
   public class_2586 method_8321(class_2338 pos) {
      return !this.method_31606(pos) ? this.blockEntities.get(pos) : null;
   }

   public void method_8438(class_2586 blockEntity) {
      class_2338 pos = blockEntity.method_11016();
      if (!this.method_31606(pos)) {
         this.blockEntities.put(pos, blockEntity);
      }
   }

   public void method_8544(class_2338 pos) {
      if (!this.method_31606(pos)) {
         class_2586 blockEntity = this.blockEntities.remove(pos);
         if (blockEntity != null) {
            blockEntity.method_11012();
         }
      }
   }

   public class_5577<class_1297> method_31592() {
      return this.entityGetter;
   }

   public class_2802 method_8398() {
      return this.chunkSource;
   }

   public int method_31607() {
      return this.minBuildHeight;
   }

   public int method_31605() {
      return this.height;
   }

   public class_6880<class_1959> method_23753(class_2338 pos) {
      return super.method_23753(pos.method_10081(this.biomeOffset));
   }

   public class_6880<class_1959> method_16359(int x, int y, int z) {
      return this.level.method_16359(x + this.biomeOffset.method_10263(), y + this.biomeOffset.method_10264(), z + this.biomeOffset.method_10260());
   }

   public class_6880<class_1959> method_22387(int x, int y, int z) {
      return this.level.method_22387(x + this.biomeOffset.method_10263(), y + this.biomeOffset.method_10264(), z + this.biomeOffset.method_10260());
   }

   public int method_8615() {
      return this.level.method_8615();
   }

   public int method_22339(class_2338 pos) {
      return 15;
   }

   public float method_24852(class_2350 direction, boolean shade) {
      return 1.0F;
   }

   public class_269 method_8428() {
      return this.level.method_8428();
   }

   public class_10286 method_8433() {
      return this.level.method_8433();
   }

   public class_4543 method_22385() {
      return this.level.method_22385();
   }

   public class_6756<class_2248> method_8397() {
      return this.level.method_8397();
   }

   public class_6756<class_3611> method_8405() {
      return this.level.method_8405();
   }

   public class_7699 method_45162() {
      return this.level.method_45162();
   }

   public class_1845 method_59547() {
      return this.level.method_59547();
   }

   public class_9895 method_61269() {
      return this.level.method_61269();
   }

   public void method_8455(class_2338 pos, class_2248 block) {
   }

   public boolean method_8477(class_2338 pos) {
      return true;
   }

   public void method_8465(
      class_1297 player, double x, double y, double z, class_6880<class_3414> soundEvent, class_3419 soundSource, float volume, float pitch, long seed
   ) {
   }

   public void method_8449(
      class_1297 player, class_1297 entity, class_6880<class_3414> soundEvent, class_3419 soundSource, float volume, float pitch, long seed
   ) {
   }

   public void method_8454(
       class_1297 entity,
       class_1282 damageSource,
       class_5362 behavior,
      double x,
      double y,
      double z,
      float power,
      boolean createFire,
      class_7867 explosionSourceType,
      class_2394 smallParticle,
      class_2394 largeParticle,
      class_6012<class_11749> blockParticles,
      class_6880<class_3414> soundEvent
   ) {
      this.level
         .method_8454(entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType, smallParticle, largeParticle, blockParticles, soundEvent);
   }

   public String method_31419() {
      return "";
   }

   
   public class_1297 method_8469(int id) {
      return null;
   }

   public Collection<class_1508> method_65097() {
      return this.level.method_65097();
   }

   public class_8921 method_54719() {
      return this.level.method_54719();
   }

   
   public class_22 method_17891(class_9209 mapId) {
      return null;
   }

   public void method_8517(int breakerId, class_2338 pos, int progress) {
   }

   public void method_8444( class_1297 player, int type, class_2338 pos, int data) {
   }

   public void method_32888(class_6880<class_5712> gameEvent, class_243 pos, class_7397 context) {
   }

   public List<? extends class_1657> method_18456() {
      return Collections.emptyList();
   }

   public class_2818 getChunkAtImmediately(int chunkX, int chunkZ) {
      return this.chunkSource.method_12126(chunkX, chunkZ, false);
   }

   public class_2791 getAnyChunkImmediately(int chunkX, int chunkZ) {
      return this.chunkSource.getChunkForLighting(chunkX, chunkZ);
   }

   public int method_31600() {
      return this.method_31607() + this.method_31605() - 1;
   }

   public int method_32890() {
      return this.method_31597() - this.method_32891() + 1;
   }

   public int method_32891() {
      return class_4076.method_18675(this.method_31607());
   }

   public int method_31597() {
      return class_4076.method_18675(this.method_31600());
   }

   public boolean method_62871(int y) {
      return y >= this.method_31607() && y <= this.method_31600();
   }

   public boolean method_31606(class_2338 pos) {
      return this.method_31601(pos.method_10264());
   }

   public boolean method_31601(int y) {
      return y < this.method_31607() || y > this.method_31600();
   }

   public int method_31602(int y) {
      return this.method_31603(class_4076.method_18675(y));
   }

   public int method_31603(int coord) {
      return coord - this.method_32891();
   }

   public int method_31604(int index) {
      return index + this.method_32891();
   }
}
