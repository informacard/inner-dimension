package com.innerdimension.projection.builtin;
import com.innerdimension.projection.builtin.world.VirtualRenderWorld;









import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11352;
import net.minecraft.class_11515;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2343;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3499.class_3501;
import net.minecraft.class_8942.class_11340;
import org.apache.commons.lang3.tuple.Pair;


@Environment(EnvType.CLIENT)
public class ClientContraption {
   private final VirtualRenderWorld renderLevel;
   public final List<class_2586> renderedBlockEntities = new ArrayList<>();
   public final List<class_2586> renderedBlockEntityView = Collections.unmodifiableList(this.renderedBlockEntities);
   public final BitSet shouldRenderBlockEntities = new BitSet();
   public final BitSet scratchErroredBlockEntities = new BitSet();
   protected final Contraption contraption;
   private int structureVersion = 0;
   private int childrenVersion = 0;

   public ClientContraption(Contraption contraption) {
      class_1937 level = contraption.entity.method_73183();
      this.contraption = contraption;
      class_2338 origin = contraption.anchor;
      int minY = VirtualRenderWorld.nextMultipleOf16(class_3532.method_15357(contraption.bounds.field_1322 - 1.0));
      int height = VirtualRenderWorld.nextMultipleOf16(class_3532.method_15384(contraption.bounds.field_1325 + 1.0)) - minY;
      this.renderLevel = new VirtualRenderWorld(level, minY, height, origin, this::invalidateStructure) {
         public boolean supportsVisualization() {
            return false;
         }
      };
      this.setupRenderLevelAndRenderedBlockEntities();
   }

   public Contraption getContraption() {
      return this.contraption;
   }

   public int structureVersion() {
      return this.structureVersion;
   }

   public int childrenVersion() {
      return this.childrenVersion;
   }

   public void resetRenderLevel() {
      this.renderedBlockEntities.clear();
      this.renderLevel.clear();
      this.shouldRenderBlockEntities.clear();
      this.setupRenderLevelAndRenderedBlockEntities();
      this.invalidateStructure();
      this.invalidateChildren();
   }

   public void invalidateChildren() {
      this.childrenVersion++;
   }

   public void invalidateStructure() {
      for (class_11515 renderType : class_11515.values()) {
         SuperByteBufferCache.getInstance().invalidate(ContraptionEntityRenderer.CONTRAPTION, Pair.of(this.contraption, renderType));
      }

      this.structureVersion++;
   }

   private void setupRenderLevelAndRenderedBlockEntities() {
      for (class_3501 info : this.contraption.getBlocks().values()) {
         this.renderLevel.method_8652(info.comp_1341(), info.comp_1342(), 0);
         class_2586 blockEntity = this.readBlockEntity(this.renderLevel, info, false);
         if (blockEntity != null) {
            this.renderLevel.method_8438(blockEntity);
            this.renderedBlockEntities.add(blockEntity);
         }
      }

      this.shouldRenderBlockEntities.set(0, this.renderedBlockEntities.size());
      this.renderLevel.runLightEngine();
   }

   
   public class_2586 readBlockEntity(class_1937 level, class_3501 info, boolean legacy) {
      class_2680 state = info.comp_1342();
      class_2338 pos = info.comp_1341();
      class_2487 nbt = info.comp_1343();
      if (legacy) {
         if (nbt == null) {
            return null;
         } else {
            nbt.method_10569("x", pos.method_10263());
            nbt.method_10569("y", pos.method_10264());
            nbt.method_10569("z", pos.method_10260());
            class_2586 be = class_2586.method_11005(pos, state, nbt, level.method_30349());
            postprocessReadBlockEntity(level, be, state);
            return be;
         }
      } else if (state.method_31709() && state.method_26204() instanceof class_2343 entityBlock) {
         class_2586 var15 = entityBlock.method_10123(pos, state);
         postprocessReadBlockEntity(level, var15, state);
         if (var15 != null && nbt != null) {
            class_11340 logging = new class_11340(var15.method_71402(), org.slf4j.LoggerFactory.getLogger("inner_dimension.projection"));

            try {
               var15.method_58690(class_11352.method_71417(logging, level.method_30349(), nbt));
            } catch (Throwable var13) {
               try {
                  logging.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }

               throw var13;
            }

            logging.close();
         }

         return var15;
      } else {
         return null;
      }
   }

   protected static void postprocessReadBlockEntity(class_1937 level,  class_2586 be, class_2680 blockState) {
      if (be != null) {
         be.method_31662(level);
         be.method_31664(blockState);

      }
   }

   public VirtualRenderWorld getRenderLevel() {
      return this.renderLevel;
   }

   public ClientContraption.RenderedBlocks getRenderedBlocks() {
      return new ClientContraption.RenderedBlocks(pos -> {
         class_3501 info = (class_3501)this.contraption.getBlocks().get(pos);
         return info == null ? class_2246.field_10124.method_9564() : info.comp_1342();
      }, this.contraption.getBlocks().keySet());
   }

   
   public class_2586 getBlockEntity(class_2338 localPos) {
      return this.renderLevel.method_8321(localPos);
   }

   public BitSet getAndAdjustShouldRenderBlockEntities() {
      return this.shouldRenderBlockEntities;
   }

   public static void resetClientContraption(Contraption contraption) {
      AtomicReference<Object> clientContraption = contraption.clientContraption;
      ClientContraption maybeNullClientContraption = (ClientContraption)clientContraption.getAcquire();
      if (maybeNullClientContraption != null) {
         maybeNullClientContraption.resetRenderLevel();
      }
   }

   public static void invalidateClientContraptionStructure(Contraption contraption) {
      AtomicReference<Object> clientContraption = contraption.clientContraption;
      ClientContraption maybeNullClientContraption = (ClientContraption)clientContraption.getAcquire();
      if (maybeNullClientContraption != null) {
         maybeNullClientContraption.invalidateStructure();
      }
   }

   public static void invalidateClientContraptionChildren(Contraption contraption) {
      AtomicReference<Object> clientContraption = contraption.clientContraption;
      ClientContraption maybeNullClientContraption = (ClientContraption)clientContraption.getAcquire();
      if (maybeNullClientContraption != null) {
         maybeNullClientContraption.invalidateChildren();
      }
   }

   
   public static class_2586 getBlockEntityClientSide(Contraption contraption, class_2338 localPos) {
      AtomicReference<Object> clientContraption = contraption.clientContraption;
      ClientContraption maybeNullClientContraption = (ClientContraption)clientContraption.getAcquire();
      return maybeNullClientContraption == null ? null : maybeNullClientContraption.getBlockEntity(localPos);
   }

   @Environment(EnvType.CLIENT)
   public static record RenderedBlocks(Function<class_2338, class_2680> lookup, Iterable<class_2338> positions) {
   }
}
