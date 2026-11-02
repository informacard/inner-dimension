package com.innerdimension.projection.builtin;
import net.minecraft.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
/** Snapshot geometry only. Assembly, machinery, storage and movement are never run. */
public class Contraption {
 public AbstractContraptionEntity entity;
 public class_2338 anchor=class_2338.field_10980;
 public class_238 bounds=new class_238(0,0,0,1,1,1);
 protected final Map<class_2338,class_3499.class_3501> blocks=new HashMap<>();
 protected final Map<class_2338,class_2487> updateTags=new HashMap<>();
 protected ContraptionWorld collisionLevel;
 // Keep common-side class descriptors free of client-only types.
 public final AtomicReference<Object> clientContraption=new AtomicReference<>();
 public Map<class_2338,class_3499.class_3501> getBlocks(){return blocks;}
 public ContraptionWorld getContraptionWorld(){return collisionLevel;}
 public void invalidateColliders(){collisionLevel=null;}
 public void write(class_11372 view,boolean spawn){}
 public void read(class_1937 world,class_11368 view,boolean spawn){}
 public boolean assemble(class_1937 world,class_2338 pos){return false;}
 public boolean canBeStabilized(class_2350 direction,class_2338 pos){return false;}
 public void onEntityCreated(AbstractContraptionEntity entity){this.entity=entity;}
 public void onEntityInitialize(class_1937 world,AbstractContraptionEntity entity){}
 public void tickStorage(AbstractContraptionEntity entity){}
 public void startMoving(class_1937 world){}
 public void stop(class_1937 world){}
 public void removeBlocksFromWorld(class_1937 world,class_2338 pos){}
 public void addBlocksToWorld(class_1937 world,StructureTransform transform){}
}
