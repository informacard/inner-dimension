package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.dimension.DynamicDimensionManager;
import net.minecraft.*;import java.util.*;
/** Container positions are scanned once per expansion. Inventory contents stay live. */
final class DomainTrappedStorage {
 record Destination(class_3218 world,class_2338 pos,class_243 footprint){}
 record Cache(List<Destination> local,List<Destination> room){}
 static void capture(DomainSession s){if(!(s.world instanceof class_3218 world))return;List<Destination> local=new ArrayList<>(),room=new ArrayList<>();scan(world,s.minX(),s.minY(),s.minZ(),s.maxX(),s.maxY(),s.maxZ(),local);
  var mirror=s.interior.mirror();if(mirror!=null){var source=mirror.sourceWorld();var snapshot=(com.innerdimension.projection.RoomProjectionContraption)s.interior.getContraption();for(var entry:snapshot.getBlocks().entrySet()){var pos=entry.getKey();if(!snapshot.sourceSpec().isShell(pos)&&class_7923.field_41175.method_10221(entry.getValue().comp_1342().method_26204()).toString().equals("minecraft:trapped_chest")&&source.method_8393(pos.method_10263()>>4,pos.method_10260()>>4)&&isChest(source,pos)){var footprint=s.interior.toGlobalVector(new class_243(pos.method_10263()+.5,pos.method_10264()+.5,pos.method_10260()+.5),0);local.add(new Destination(source,pos.method_10062(),footprint));}}}
  var inner=world.method_8503().method_3847(DynamicDimensionManager.worldKey(s.ownerId));if(inner!=null)scan(inner,0,0,0,16,16,16,room);
  s.putState("d23_storage",new Cache(List.copyOf(local),List.copyOf(room)));
 }
 private static void scan(class_3218 world,double minX,double minY,double minZ,double maxX,double maxY,double maxZ,List<Destination> out){for(int cx=((int)Math.floor(minX))>>4;cx<=((int)Math.ceil(maxX)-1)>>4;cx++)for(int cz=((int)Math.floor(minZ))>>4;cz<=((int)Math.ceil(maxZ)-1)>>4;cz++){if(!world.method_8393(cx,cz))continue;var chunk=world.method_8500(new class_2338(cx<<4,0,cz<<4));for(var pos:chunk.method_12214().keySet())if(pos.method_10263()>=minX&&pos.method_10263()<maxX&&pos.method_10264()>=minY&&pos.method_10264()<maxY&&pos.method_10260()>=minZ&&pos.method_10260()<maxZ)add(world,pos,out);}}
 private static void add(class_3218 world,class_2338 pos,List<Destination> out){if(!world.method_8393(pos.method_10263()>>4,pos.method_10260()>>4))return;if(isChest(world,pos))out.add(new Destination(world,pos.method_10062(),null));}
 private static boolean isChest(class_3218 world,class_2338 pos){return class_7923.field_41175.method_10221(world.method_8320(pos).method_26204()).toString().equals("minecraft:trapped_chest");}
 static void prepare(DomainSession s){Cache cache=s.state("d23_storage",(Cache)null);if(cache==null)return;var local=cache.local().stream().filter(d->valid(s,d,true)).toList();s.putState("d23_active_storage",local.isEmpty()?cache.room():local);}
 static void insert(DomainSession s,class_1542 item){var stack=item.method_6983();List<Destination> destinations=s.state("d23_active_storage",List.of());
   for(var d:destinations){if(!valid(s,d,false))continue;var be=d.world().method_8321(d.pos());if(!(be instanceof class_1263 inventory))continue;boolean changed=false;
   for(int slot=0;slot<inventory.method_5439()&&!stack.method_7960();slot++){if(!inventory.method_5437(slot,stack))continue;var existing=inventory.method_5438(slot);int limit=Math.min(inventory.method_5444(),stack.method_7914());if(existing.method_7960()){int n=Math.min(limit,stack.method_7947());inventory.method_5447(slot,stack.method_46651(n));stack.method_7934(n);changed=true;}else if(class_1799.method_31577(existing,stack)){int n=Math.min(limit-existing.method_7947(),stack.method_7947());if(n>0){existing.method_7933(n);stack.method_7934(n);changed=true;}}}
   if(changed)inventory.method_5431();if(stack.method_7960()){item.method_31472();return;}
  }
  item.method_6979(stack);
 }
 private static boolean valid(DomainSession s,Destination d,boolean local){boolean inside=!local||(d.footprint()!=null?s.containsPoint(d.footprint().field_1352,d.footprint().field_1351,d.footprint().field_1350):s.containsBlock(d.pos()));return inside&&d.world().method_8393(d.pos().method_10263()>>4,d.pos().method_10260()>>4)&&isChest(d.world(),d.pos());}
}
