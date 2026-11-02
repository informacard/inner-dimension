package com.innerdimension.projection.builtin;
import com.innerdimension.projection.builtin.world.VirtualRenderWorld;






import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11659;
import net.minecraft.class_11954;
import net.minecraft.class_12075;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_824;
import net.minecraft.class_827;

import org.joml.Matrix4f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class BlockEntityRenderHelper {
   
   public static BlockEntityRenderHelper.BlockEntityListRenderState getBlockEntitiesRenderState(
      boolean supportsVisualization,
      List<class_2586> blockEntities,
      BitSet shouldRenderBEs,
      BitSet erroredBEsOut,
       VirtualRenderWorld renderLevel,
      class_1937 realLevel,
       Matrix4f lightTransform,
      class_243 camera,
      float pt
   ) {
      int size = blockEntities.size();
      if (size == 0) {
         return null;
      } else {
         class_310 mc = class_310.method_1551();
         class_824 dispatcher = mc.method_31975();
         List<class_11954> states = new ArrayList<>();

         for (int i = shouldRenderBEs.nextSetBit(0); i >= 0 && i < size; i = shouldRenderBEs.nextSetBit(i + 1)) {
            class_2586 blockEntity = blockEntities.get(i);
            if (true) {
               class_827<class_2586, class_11954> renderer = dispatcher.method_3550(blockEntity);
               if (renderer == null) {
                  erroredBEsOut.set(i);
               } else {
                  try {
                     class_11954 renderState = renderer.method_74335();
                     int realLevelLight = class_761.method_23794(realLevel, getLightPos(lightTransform, blockEntity.method_11016()));
                     if (renderLevel != null) {
                        renderLevel.setExternalLight(realLevelLight);
                     }

                     renderer.method_74331(blockEntity, renderState, pt, camera, null);
                     if (renderLevel == null) {
                        renderState.field_62676 = realLevelLight;
                     }

                     states.add(renderState);
                  } catch (Exception var18) {
                     erroredBEsOut.set(i);
                     org.slf4j.LoggerFactory.getLogger("inner_dimension.projection").error("Cannot render projection block entity at {}", blockEntity.method_11016(), var18);
                  }
               }
            }
         }

         if (renderLevel != null) {
            renderLevel.resetExternalLight();
         }

         return states.isEmpty() ? null : new BlockEntityRenderHelper.BlockEntityListRenderState(dispatcher, camera, class_2338.method_49638(camera), states);
      }
   }

   private static class_2338 getLightPos( Matrix4f lightTransform, class_2338 contraptionPos) {
      if (lightTransform != null) {
         Vector4f lightVec = new Vector4f(
            (float)contraptionPos.method_10263() + 0.5F, (float)contraptionPos.method_10264() + 0.5F, (float)contraptionPos.method_10260() + 0.5F, 1.0F
         );
         lightVec.mul(lightTransform);
         return class_2338.method_49637((double)lightVec.x(), (double)lightVec.y(), (double)lightVec.z());
      } else {
         return contraptionPos;
      }
   }

   @Environment(EnvType.CLIENT)
   public static record BlockEntityListRenderState(class_824 dispatcher, class_243 camera, class_2338 cameraPos, List<class_11954> states, com.innerdimension.projection.ProjectionAnimation.BreakFrame fracture) {
      public BlockEntityListRenderState(class_824 dispatcher,class_243 camera,class_2338 cameraPos,List<class_11954> states){this(dispatcher,camera,cameraPos,states,null);}
      public void render(class_4587 matrices, class_11659 queue, class_12075 cameraRenderState) {
         class_243 prevPos = cameraRenderState.field_63078;
         class_2338 prevBlockPos = cameraRenderState.field_63077;
         class_243 prevEntityPos = cameraRenderState.field_63080;
         cameraRenderState.field_63078 = this.camera;
         cameraRenderState.field_63077 = this.cameraPos;
         cameraRenderState.field_63080 = new class_243(
            prevEntityPos.field_1352 - prevPos.field_1352 + this.camera.field_1352,
            prevEntityPos.field_1351 - prevPos.field_1351 + this.camera.field_1351,
            prevEntityPos.field_1350 - prevPos.field_1350 + this.camera.field_1350
         );

         for (class_11954 state : this.states) {
            class_827<class_2586, class_11954> renderer = this.dispatcher.method_74349(state);
            if (renderer != null) {
               class_2338 pos = state.field_62673;
               float scale=fracture==null?1:(float)com.innerdimension.projection.ProjectionAnimation.breakScale(fracture,pos.method_10263(),pos.method_10264(),pos.method_10260());
               if(scale<=.001f)continue;
               matrices.method_22903();
               matrices.method_46416((float)pos.method_10263(), (float)pos.method_10264(), (float)pos.method_10260());
               if(fracture!=null){matrices.method_46416(.5f,.5f,.5f);matrices.method_22905(scale,scale,scale);matrices.method_46416(-.5f,-.5f,-.5f);}
               renderer.method_3569(state, matrices, queue, cameraRenderState);
               matrices.method_22909();
            }
         }

         cameraRenderState.field_63078 = prevPos;
         cameraRenderState.field_63077 = prevBlockPos;
         cameraRenderState.field_63080 = prevEntityPos;
      }
   }
}
