package com.innerdimension.projection.builtin;
import net.minecraft.*;
import java.util.*;
public abstract class AbstractContraptionEntity extends class_1297 {
 protected Contraption contraption;
 public final Map<class_1297,Integer> collidingEntities=new IdentityHashMap<>();
 public int staleTicks;
 protected AbstractContraptionEntity(class_1299<?> type,class_1937 world){super(type,world);}
 public Contraption getContraption(){return contraption;}
 public void setContraption(Contraption value){contraption=value;value.onEntityCreated(this);updateBounds();}
 public void updateBounds(){if(contraption!=null)method_5857(contraption.bounds.method_997(method_73189()));}
 @Override public void method_5814(double x,double y,double z){super.method_5814(x,y,z);updateBounds();}
 @Override public void method_5773(){super.method_5773();tickContraption();updateBounds();}
 @Override protected void method_5693(class_2945.class_9222 builder){}
 @Override protected void method_5749(class_11368 view){readAdditional(view,false);}
 @Override protected void method_5652(class_11372 view){}
 protected void readAdditional(class_11368 view,boolean spawn){}
 @Override public boolean method_64397(class_3218 world,class_1282 source,float damage){return false;}
 @Override public boolean method_5863(){return false;}
 @Override public boolean method_30949(class_1297 other){return false;}
 @Override public boolean method_5654(double x,double y,double z){return getContraption()!=null && super.method_5654(x,y,z);}
 public boolean collisionEnabled(){return false;}
 public boolean isReadyForRender(){return contraption!=null;}
 public boolean isAliveOrStale(){return !method_31481();}
 public class_243 getAnchorVec(){return method_73189();}
 public class_243 getPrevAnchorVec(){return getPrevPositionVec();}
 public class_243 getPrevPositionVec(){return new class_243(field_6038,field_5971,field_5989);}
 public class_243 toLocalVector(class_243 world,float partial){return world.method_1020(getAnchorVec());}
 public class_243 toGlobalVector(class_243 local,float partial){return local.method_1019(getAnchorVec());}
 public class_243 applyRotation(class_243 local,float partial){return local;}
 public class_243 reverseRotation(class_243 local,float partial){return local;}
 public boolean supportsTerrainCollision(){return false;}
 public void registerColliding(class_1297 actor){}
 public void setContraptionMotion(class_243 motion){}
 protected void tickContraption(){}
 public void tickActors(){}
 public class_243 getContactPointMotion(class_243 point){return class_243.field_1353;}
 public boolean handlePlayerInteraction(class_1657 player,class_2338 pos,class_2350 side,class_1268 hand){return false;}
 public void disassemble(){method_31472();}
}
