package com.innerdimension.item;
import com.innerdimension.compat.NonBottlingCompat;
import com.innerdimension.core.ServerClock;
import com.innerdimension.network.ExtractionPayload;
import net.minecraft.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import java.util.*;
/** Server-owned one-shot extraction. Only a successful completion consumes an empty bottle. */
public final class SemenExtraction {
 private record Attempt(class_3222 player,class_1309 target,class_1799 tool,class_1799 bottle,class_2960 species,class_3218 world,class_243 anchor,long started){}
 private static final Map<UUID,Attempt> RUNNING=new HashMap<>();
 public static boolean active(class_3222 p){return RUNNING.containsKey(p.method_5667());}
 private static void send(class_3222 p,boolean active){if(ServerPlayNetworking.canSend(p,ExtractionPayload.ID))ServerPlayNetworking.send(p,new ExtractionPayload(active,p.method_23317(),p.method_23318(),p.method_23321()));}
 public static boolean start(class_3222 p,class_1309 target,class_1799 tool){
  if(active(p))return true;
  // PlayerEntity.interact uses a COPY of the main-hand stack in creative mode.
  // Track and consume the actual inventory stack, not that temporary argument.
  tool=p.method_5998(class_1268.field_5808);
  if(tool.method_7960())return false;
  var bottle=p.method_6079();
  if(target==p||!target.method_5805()||!NonBottlingCompat.isMale(target)||!bottle.method_31574(class_1802.field_8469)||bottle.method_7960()||p.method_33571().method_1025(target.method_33571())>36)return false;
  var species=class_7923.field_41177.method_10221(target.method_5864());
  if(NonBottlingCompat.bottleForEntity(species).method_7960())return false;
  // method_33571 is getEyePos; movement/teleport anchors must use the feet position.
  RUNNING.put(p.method_5667(),new Attempt(p,target,tool,bottle,species,p.method_51469(),p.method_73189(),ServerClock.now()));
  p.method_18799(class_243.field_1353);send(p,true);return true;
 }
 private static void finish(Attempt a,boolean success){
  if(!RUNNING.remove(a.player().method_5667(),a))return;
  var p=a.player();send(p,false);
  a.tool().method_7934(1);p.method_31548().method_5431();
  var sound=class_7923.field_41172.method_63535(class_2960.method_60655("minecraft","entity.item.break"));
  a.world().method_43128(null,p.method_23317(),p.method_23318(),p.method_23321(),sound,class_3419.field_15248,1,1);
  if(!success)return;
  var result=NonBottlingCompat.bottleForEntity(a.species());if(result.method_7960())return;
  a.bottle().method_7934(1);NonBottlingCompat.giveOrDrop(p,result);p.method_31548().method_5431();
  var fill=class_7923.field_41172.method_63535(class_2960.method_60655("minecraft","item.bottle.fill"));
  a.world().method_43128(null,p.method_23317(),p.method_23318(),p.method_23321(),fill,class_3419.field_15248,1,1);
 }
 private static void tick(net.minecraft.server.MinecraftServer server){
  for(var a:List.copyOf(RUNNING.values())){
   var p=a.player();var t=a.target();
   if(!p.method_5805()||!t.method_5805()||t.method_31481()||p.method_51469()!=a.world()||t.method_73183()!=a.world()||p.method_5998(class_1268.field_5808)!=a.tool()||a.tool().method_7960()||p.method_6079()!=a.bottle()||!a.bottle().method_31574(class_1802.field_8469)||a.bottle().method_7960()||p.method_33571().method_1025(t.method_33571())>36){finish(a,false);continue;}
   p.method_18799(class_243.field_1353);
   if(p.method_73189().method_1025(a.anchor())>.0001)p.field_13987.method_14363(a.anchor().field_1352,a.anchor().field_1351,a.anchor().field_1350,p.method_36454(),p.method_36455());
   if(ExtractionTiming.complete(ServerClock.now()-a.started()))finish(a,true);
  }
 }
 public static void register(){
  PayloadTypeRegistry.playS2C().register(ExtractionPayload.ID,ExtractionPayload.CODEC);
  ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity,source,amount)->{if(amount>0)for(var a:List.copyOf(RUNNING.values()))if(entity==a.player()||entity==a.target())finish(a,false);return true;});
  ServerTickEvents.END_SERVER_TICK.register(SemenExtraction::tick);
  ServerPlayConnectionEvents.DISCONNECT.register((h,s)->{var a=RUNNING.get(h.field_14140.method_5667());if(a!=null)finish(a,false);});
  ServerLifecycleEvents.SERVER_STOPPING.register(s->{for(var a:List.copyOf(RUNNING.values()))finish(a,false);RUNNING.clear();});
 }
}
