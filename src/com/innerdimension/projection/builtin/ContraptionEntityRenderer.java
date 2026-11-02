package com.innerdimension.projection.builtin;
import com.innerdimension.projection.builtin.world.VirtualRenderWorld;















import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_1087;
import net.minecraft.class_10889;
import net.minecraft.class_11515;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2464;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4604;
import net.minecraft.class_4608;
import net.minecraft.class_4696;
import net.minecraft.class_5819;
import net.minecraft.class_776;
import net.minecraft.class_778;
import net.minecraft.class_897;
import net.minecraft.class_11659.class_11660;
import net.minecraft.class_3499.class_3501;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_5617.class_5618;
import org.apache.commons.lang3.tuple.Pair;

import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ContraptionEntityRenderer<C extends AbstractContraptionEntity, S extends ContraptionEntityRenderer.AbstractContraptionState>
   extends class_897<C, S> {
   public static final SuperByteBufferCache.Compartment<Pair<Contraption, class_11515>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
   private static final ThreadLocal<ContraptionEntityRenderer.ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
      ContraptionEntityRenderer.ThreadLocalObjects::new
   );
   private final class_4587 matrixStack = new class_4587();

   public ContraptionEntityRenderer(class_5618 context) {
      super(context);
   }

   public static SuperByteBuffer getBuffer(Contraption contraption, ClientContraption clientContraption, VirtualRenderWorld renderWorld, class_11515 renderType) {
      return SuperByteBufferCache.getInstance()
         .get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(clientContraption, renderWorld, renderType));
   }

   public ClientContraption getOrCreateClientContraptionLazy(Contraption contraption) {
      AtomicReference<Object> clientContraption = contraption.clientContraption;
      ClientContraption out = (ClientContraption)clientContraption.getAcquire();
      if (out == null) {
         clientContraption.compareAndExchangeRelease(null, this.createClientContraption(contraption));
         out = (ClientContraption)clientContraption.getAcquire();
      }

      return out;
   }

   protected ClientContraption createClientContraption(Contraption contraption) {
      return new ClientContraption(contraption);
   }

   private static SuperByteBuffer buildStructureBuffer(ClientContraption clientContraption, VirtualRenderWorld renderWorld, class_11515 layer) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
      class_776 dispatcher = class_310.method_1551().method_1541();
      class_778 renderer = dispatcher.method_3350();
      ContraptionEntityRenderer.ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
      class_4587 poseStack = objects.poseStack;
      class_5819 random = objects.random;
      ClientContraption.RenderedBlocks blocks = clientContraption.getRenderedBlocks();
      ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
      sbbBuilder.begin();
      class_778.method_20544();

      for (class_2338 pos : blocks.positions()) {
         class_2680 state = blocks.lookup().apply(pos);
         if (state.method_26217() == class_2464.field_11458) {
            class_1087 model = dispatcher.method_3349(state);
            long randomSeed = state.method_26190(pos);
            random.method_43052(randomSeed);
            if (class_4696.method_23679(state) == layer) {
               sbbBuilder.blockCenter(pos);
               poseStack.method_22903();
               poseStack.method_46416((float)pos.method_10263(), (float)pos.method_10264(), (float)pos.method_10260());
               List<class_10889> parts = new ObjectArrayList();
               model.method_68513(random, parts);

               renderer.method_3374(renderWorld, parts, state, pos, poseStack, sbbBuilder, !(clientContraption.getContraption().entity instanceof com.innerdimension.projection.RoomProjectionEntity e && (e.broken() || e.isShell() && (e.phase()==com.innerdimension.projection.ProjectionAnimation.EXPANDING || e.phase()==com.innerdimension.projection.ProjectionAnimation.RETRACTING))), class_4608.field_21444);
               poseStack.method_22909();
            }
         }
      }

      class_778.method_20545();
      return sbbBuilder.end();
   
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.MESH_BUILD,auditStart); }
    }

   public boolean shouldRender(C entity, class_4604 frustum, double cameraX, double cameraY, double cameraZ) {
      if (entity.getContraption() == null) {
         return false;
      } else if (!entity.isAliveOrStale()) {
         return false;
      } else {
         return !entity.isReadyForRender() ? false : super.method_3933(entity, frustum, cameraX, cameraY, cameraZ);
      }
   }

   public S createRenderState() {
      return (S)(new ContraptionEntityRenderer.AbstractContraptionState());
   }

   public void extractRenderState(C entity, S state, float tickProgress) {
      state.field_58171 = entity.method_5864();
      Contraption contraption = entity.getContraption();
      ClientContraption clientContraption = contraption != null ? this.getOrCreateClientContraptionLazy(contraption) : null;
      if (clientContraption != null) {
         class_4184 camera = this.field_4676.field_4686;
         if (camera != null) {
            state.contraption = contraption;
            state.field_53325 = class_3532.method_16436((double)tickProgress, entity.field_6038, entity.method_23317());
            state.field_53326 = class_3532.method_16436((double)tickProgress, entity.field_5971, entity.method_23318());
            state.field_53327 = class_3532.method_16436((double)tickProgress, entity.field_5989, entity.method_23321());
            this.matrixStack.method_22903();
            this.transform(state, this.matrixStack);
            state.modelEntry = this.matrixStack.method_23760().method_56822();
            this.matrixStack.method_22909();
            class_1937 world = entity.method_73183();
            VirtualRenderWorld renderWorld = clientContraption.getRenderLevel();
            Matrix4f worldMatrix4f = new Matrix4f().setTranslation((float)state.field_53325, (float)state.field_53326, (float)state.field_53327);
            boolean support = false;
            if (!support) {
               state.layers = createLayers(contraption, clientContraption, renderWorld, world, worldMatrix4f);
            }

            BitSet adjustRenderedBlockEntities = clientContraption.getAndAdjustShouldRenderBlockEntities();
            clientContraption.scratchErroredBlockEntities.clear();
            class_243 cameraPos = camera.method_71156();
            Matrix4f lightTransform = worldMatrix4f.mul(state.modelEntry.method_23761(), new Matrix4f());
            long blockEntityStart=com.innerdimension.projection.OpenDomainPerformance.begin();
            try { state.blockEntity = BlockEntityRenderHelper.getBlockEntitiesRenderState(
               support,
               clientContraption.renderedBlockEntityView,
               adjustRenderedBlockEntities,
               clientContraption.scratchErroredBlockEntities,
               renderWorld,
               world,
               lightTransform,
               contraption.entity.toLocalVector(cameraPos, tickProgress),
               tickProgress
            );
            } finally {com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.BLOCK_ENTITY_RENDER,blockEntityStart);}
            clientContraption.shouldRenderBlockEntities.andNot(clientContraption.scratchErroredBlockEntities);
            state.actors = null;
         }
      }
   }

   public void submit(S state, class_4587 poseStack, class_11659 queue, class_12075 cameraRenderState) {
      if (state.contraption != null) {
         poseStack.method_22903();
         class_4665 entry = poseStack.method_23760();
         entry.method_23761().mul(state.modelEntry.method_23761());
         entry.method_23762().mul(state.modelEntry.method_23762());
         if (state.layers != null) {
            for (ContraptionEntityRenderer.ContraptionBlockLayer layer : state.layers) {
               queue.method_73483(poseStack, layer.renderLayer, layer);
            }
         }

         if (state.blockEntity != null) {
            state.blockEntity.render(poseStack, queue, cameraRenderState);
         }


         poseStack.method_22909();
      }
   }

   @Override public S method_55269() { return createRenderState(); }
   @Override public void method_62354(C entity,S state,float partial) { super.method_62354(entity,state,partial); extractRenderState(entity,state,partial); }
   @Override public void method_3936(S state,class_4587 matrices,class_11659 queue,class_12075 camera) { submit(state,matrices,queue,camera); }
   @Override public boolean method_3933(C entity,class_4604 frustum,double x,double y,double z) { return shouldRender(entity,frustum,x,y,z); }
   public void transform(S state, class_4587 matrixStack) {
   }

   
   public static List<ContraptionEntityRenderer.ContraptionBlockLayer> createLayers(
      Contraption contraption, ClientContraption clientContraption, VirtualRenderWorld renderWorld, class_1937 world, Matrix4f lightTransform
   ) {
      return createLayers(contraption, clientContraption, renderWorld, world, lightTransform, 0);
   }

   public static List<ContraptionEntityRenderer.ContraptionBlockLayer> createLayers(
      Contraption contraption, ClientContraption clientContraption, VirtualRenderWorld renderWorld, class_1937 world, Matrix4f lightTransform,
      float surfaceOffset
   ) {
      List<ContraptionEntityRenderer.ContraptionBlockLayer> layers = new ArrayList<>();

      for (class_11515 blockLayer : class_11515.values()) {
         SuperByteBuffer buffer = getBuffer(contraption, clientContraption, renderWorld, blockLayer);
         if (!buffer.isEmpty()) {
            layers.add(new ContraptionEntityRenderer.ContraptionBlockLayer(getRenderLayer(blockLayer), buffer, world, lightTransform, surfaceOffset));
         }
      }

      return layers.isEmpty() ? null : layers;
   }

   private static class_1921 getRenderLayer(class_11515 layer) {
      return switch (layer) {
         case field_60923 -> class_12249.method_75965();
         case field_60925 -> class_12249.method_75972();
         case field_60926 -> class_12249.method_75977();
         case field_60927 -> class_12249.method_76009();
         default -> throw new MatchException(null, null);
      };
   }

   @Environment(EnvType.CLIENT)
   public static class AbstractContraptionState extends class_10017 {
      public Contraption contraption;
      public class_4665 modelEntry;
      public List<ContraptionEntityRenderer.ContraptionBlockLayer> layers;
      public BlockEntityRenderHelper.BlockEntityListRenderState blockEntity;
      
      public Object actors;
   }

   @Environment(EnvType.CLIENT)
   public static record ContraptionBlockLayer(class_1921 renderLayer, SuperByteBuffer buffer, class_1937 world, Matrix4f lightTransform, float surfaceOffset, com.innerdimension.projection.ProjectionAnimation.BreakFrame fracture, FrameLightCache lights) implements class_11660 {
      public ContraptionBlockLayer(class_1921 renderLayer,SuperByteBuffer buffer,class_1937 world,Matrix4f transform,float offset,com.innerdimension.projection.ProjectionAnimation.BreakFrame fracture) {
         this(renderLayer,buffer,world,transform,offset,fracture,null);
      }
      public ContraptionBlockLayer(class_1921 renderLayer, SuperByteBuffer buffer, class_1937 world, Matrix4f lightTransform,float surfaceOffset) {
         this(renderLayer,buffer,world,lightTransform,surfaceOffset,null);
      }
      public ContraptionBlockLayer(class_1921 renderLayer, SuperByteBuffer buffer, class_1937 world, Matrix4f lightTransform) {
         this(renderLayer, buffer, world, lightTransform, 0);
      }
      public void render(class_4665 matricesEntry, class_4588 vertexConsumer) {
         this.buffer.useLevelLight(this.world, this.lightTransform).renderInto(matricesEntry, vertexConsumer, this.surfaceOffset,this.fracture,this.lights);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class ThreadLocalObjects {
      public final class_4587 poseStack = new class_4587();
      public final class_5819 random = class_5819.method_43053();
      public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
   }
}
