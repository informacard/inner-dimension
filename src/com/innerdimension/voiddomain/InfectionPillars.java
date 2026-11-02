package com.innerdimension.voiddomain;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.projection.*;
import net.minecraft.*;
import java.util.*;
public final class InfectionPillars {
 private static final Set<class_3218> MIGRATED=Collections.newSetFromMap(new IdentityHashMap<>());
 private static final String TAG="inner_dimension_infection_pillar";
 public static boolean present(class_3218 world){
  var owner=com.innerdimension.dimension.DynamicDimensionManager.ownerOfWorld(world.method_27983());
  if(owner!=null&&VoidEmbodimentRules.pillars(VoidEmbodimentState.get(world).players.getOrDefault(owner,0)))return true;
  return !world.method_8390(RoomProjectionEntity.class,new class_238(6,-1,6,10,50,10),e->e.method_5752().contains(TAG)).isEmpty();
 }
 private static void migrate(class_3218 world){
  if(MIGRATED.add(world))for(int base:new int[]{0,32})for(int x=7;x<9;x++)for(int z=7;z<9;z++)for(int y=base;y<base+16;y++){
   var pos=new class_2338(x,y,z);if(world.method_8320(pos).method_26204()==ModBlocks.ABYSS_BLOCK){
    var restored=class_2246.field_10124.method_9564();
    if(y==32||y==47){var skin=world.method_8320(new class_2338(6,y,7));var b=skin.method_26204();if(b==ModBlocks.VOID_WATER||b==ModBlocks.FLESH_WATER||b==ModBlocks.VOID_MIST||b==ModBlocks.SOUL_MIST)restored=skin;}
    world.method_8652(pos,restored,3);
   }
  }
 }
 public static void ensure(class_3218 world,UUID owner){
  migrate(world);
  for(int base:new int[]{0,32}){
   UUID identity=UUID.nameUUIDFromBytes(("infection_pillar:"+owner+":"+base).getBytes(java.nio.charset.StandardCharsets.UTF_8));
   var existing=world.method_8390(RoomProjectionEntity.class,new class_238(6,base-1,6,10,base+17,10),e->e.method_5752().contains(TAG)&&identity.equals(e.owner()));
   if(!existing.isEmpty()){for(var pillar:existing)pillar.method_5665(class_2561.method_43471("block.inner_dimension.infector"));ProjectionMirror.tickStatic(existing.getFirst());continue;}
   var pillar=new RoomProjectionEntity(RoomProjectionManager.ENTITY_TYPE,world);
   pillar.initialize(RoomProjectionContraption.infectionPillar(),new class_243(7,base,7),false,identity);pillar.method_5780(TAG);pillar.method_5665(class_2561.method_43471("block.inner_dimension.infector"));pillar.stabilize();world.method_8649(pillar);
  }
 }
 public static void remove(class_3218 world,UUID owner){
  migrate(world);
  for(var pillar:world.method_8390(RoomProjectionEntity.class,new class_238(6,-1,6,10,50,10),e->e.method_5752().contains(TAG)))pillar.method_31472();
 }
 public static void clear(){MIGRATED.clear();}
 private InfectionPillars(){}
}
