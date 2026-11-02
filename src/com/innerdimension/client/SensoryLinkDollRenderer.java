package com.innerdimension.client;
import com.innerdimension.entity.SensoryLinkDollEntity;
import net.minecraft.*;
import org.joml.Quaternionf;
/** Uses the installed plushie mod's meshes and appearance resolver directly. */
public class SensoryLinkDollRenderer extends class_897<SensoryLinkDollEntity,SensoryLinkDollRenderState> {
 private final PlushieAppearanceCompat appearance=new PlushieAppearanceCompat();
 private final class_11786 skins;
 private boolean warned;
 public SensoryLinkDollRenderer(class_5617.class_5618 context){super(context);skins=context.method_73540();}
 @Override public SensoryLinkDollRenderState method_55269(){return new SensoryLinkDollRenderState();}
 @Override public void method_62354(SensoryLinkDollEntity entity,SensoryLinkDollRenderState state,float delta){
  super.method_62354(entity,state,delta);
  var data=entity.getDollData();state.layer=PlushieAppearanceCompat.defaultLayer();state.slim=false;state.genderMask=0;
  state.headDegrees=class_7718.method_45482((int)Math.floor(entity.method_36454()*16/360+.5)&15);
  try{
   appearance.appearance(data,skins,state);
  }catch(RuntimeException error){if(!warned){warned=true;org.slf4j.LoggerFactory.getLogger("inner_dimension").warn("Doll skin resolution failed; using default",error);}}
 }
 @Override public void method_3936(SensoryLinkDollRenderState state,class_4587 matrices,class_11659 queue,class_12075 camera){
  matrices.method_22903();matrices.method_22905(-1,-1,1);
  matrices.method_22907(new Quaternionf().rotationY((float)Math.toRadians(state.headDegrees)));
  queue.method_73491(appearance.root(state.slim,state.genderMask),matrices,state.layer==null?PlushieAppearanceCompat.defaultLayer():state.layer,state.field_61820,class_4608.field_21444,null);
  matrices.method_22909();super.method_3936(state,matrices,queue,camera);
 }
}
