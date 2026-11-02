package com.innerdimension.item;
import net.minecraft.*;
import java.util.*;
/** Keep real stacks in the dead player's inventory so saving before respawn is safe. */
public final class SoulboundCores {
 public record Saved(int slot,class_1799 stack){}
 public static List<Saved> take(class_1263 inventory){
  var saved=new ArrayList<Saved>();
  for(int slot=0;slot<inventory.method_5439();slot++)if(inventory.method_5438(slot).method_7909() instanceof DomainCoreItem)saved.add(new Saved(slot,inventory.method_5441(slot)));
  return saved;
 }
 public static void restore(class_1263 inventory,List<Saved> saved){for(var entry:saved)inventory.method_5447(entry.slot(),entry.stack());}
 public static List<class_1799> copyRetained(class_1263 oldInventory,class_1263 newInventory){
  var displaced=new ArrayList<class_1799>();
  for(int slot=0;slot<Math.min(oldInventory.method_5439(),newInventory.method_5439());slot++){
   var core=oldInventory.method_5438(slot);if(!(core.method_7909() instanceof DomainCoreItem))continue;
   var existing=newInventory.method_5438(slot);
   if(!class_1799.method_7973(existing,core)||existing.method_7947()!=core.method_7947()){
    if(!existing.method_7960())displaced.add(existing);
    newInventory.method_5447(slot,core.method_7972());
   }
   oldInventory.method_5447(slot,class_1799.field_8037);
  }
  return displaced;
 }
 public static void register(){
  net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer,newPlayer,alive)->{
   if(alive)return;
   for(var stack:copyRetained(oldPlayer.method_31548(),newPlayer.method_31548()))if(!newPlayer.method_31548().method_7394(stack))newPlayer.method_7329(stack,false,false);
  });
 }
 private SoulboundCores(){}
}
