package com.innerdimension.voiddomain;
import net.minecraft.*;
import java.util.*;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.progression.InnerAdvancements;
import com.innerdimension.dimension.*;
import com.innerdimension.sound.ModSounds;
public final class VoidEmbodiment {
 private static final org.slf4j.Logger LOGGER=org.slf4j.LoggerFactory.getLogger("inner_dimension.impalement");
 private record Before(class_3218 world,double y){}
 private record Trace(class_3218 world,ImpalementFallTrace descent){}
 private static final Map<UUID,Before> BEFORE=new HashMap<>();
 private static final Map<UUID,Trace> DESCENTS=new HashMap<>();
 private static final Map<UUID,Long> LAST_ATTEMPT=new HashMap<>();
 public static void beforeMovement(class_3222 p){BEFORE.put(p.method_5667(),new Before(p.method_51469(),p.method_23318()));}
 public static void acceptedMovement(class_3222 p){
  var id=p.method_5667();var before=BEFORE.remove(id);if(before==null)return;
  if(before.world()!=p.method_51469()){DESCENTS.remove(id);return;}
  var trace=DESCENTS.get(id);if(trace==null||trace.world()!=before.world()){trace=new Trace(before.world(),new ImpalementFallTrace());DESCENTS.put(id,trace);}
  boolean interrupted=!p.method_5805()||p.method_7325()||p.method_31549().field_7479||p.method_6128()||p.method_5799();
  double distance=trace.descent().accept(before.y(),p.method_23318(),p.method_24828(),interrupted);
  if(!interrupted&&before.y()>p.method_23318()+1e-5&&p.method_24828())landing(p,Math.max(distance,p.field_6017),"MOVEMENT_PACKET");
 }
 private static final Map<UUID,Integer> FLIGHT=new HashMap<>();
 private static final Map<UUID,Long> BAPTISM_STARTED=new HashMap<>();
 private static void baptismFilm(class_3222 p){
  if(!net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(p,com.innerdimension.network.EnlightenmentVisualPayload.ID))return;
  BAPTISM_STARTED.put(p.method_5667(),System.nanoTime());
  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new com.innerdimension.network.EnlightenmentVisualPayload(5,true));
 }
 public static void baptismFinished(class_3222 p){
  var started=BAPTISM_STARTED.get(p.method_5667());
  if(started==null||!VoidEmbodimentRules.baptismReady(VoidEmbodimentState.get(p).players.getOrDefault(p.method_5667(),0),System.nanoTime()-started))return;
  VoidEmbodimentState.get(p).put(p.method_5667(),3);BAPTISM_STARTED.remove(p.method_5667());InnerAdvancements.grant(p,"void_baptism");
 }
 public static boolean active(class_3222 p){return VoidEmbodimentRules.embodied(VoidEmbodimentState.get(p).players.getOrDefault(p.method_5667(),0));}
 public static boolean flying(class_3222 p){return active(p)&&p.method_23318()<0&&p.method_6128();}
 public static class_6880<class_1291> effect(){return class_7923.field_41174.method_47983(com.innerdimension.effect.ModStatusEffects.VOID_EMBODIMENT);}
 public static boolean landing(class_3222 p,double distance){
  return landing(p,distance,"FALL_DAMAGE");
 }
 private static boolean landing(class_3222 p,double distance,String source){
  if(!p.method_5805()||p.method_7325()||active(p))return false;
  // During vanilla move(), the bounding box reaches the landing before cached entity coordinates.
  var box=p.method_5829();double y=box.field_1322;
  for(int x=(int)Math.floor(box.field_1323);x<=Math.floor(box.field_1320-1e-6);x++)
   for(int z=(int)Math.floor(box.field_1321);z<=Math.floor(box.field_1324-1e-6);z++)
    for(int by=(int)Math.floor(y)-1;by<=Math.floor(y);by++){
   var at=new class_2338(x,by,z);var block=p.method_51469().method_8320(at);
   if(block.method_26204()!=ModBlocks.SHEATHED_INFECTOR)continue;
   if(box.field_1320<=x+7/16.0||box.field_1323>=x+9/16.0||box.field_1324<=z+7/16.0||box.field_1321>=z+9/16.0)continue;
   if(y<by-.15||y>by+1.15)continue;
   boolean up=block.method_11654(class_2318.field_10927)==class_2350.field_11036;
   boolean contaminated=VoidContamination.active(p),enlightened=InnerAdvancements.done(p,"enlightenment");
   long now=p.method_51469().method_8503().method_30002().method_75260();
   if(now-LAST_ATTEMPT.getOrDefault(p.method_5667(),-100L)>=20){LAST_ATTEMPT.put(p.method_5667(),now);LOGGER.info("IMPALEMENT_ATTEMPT player={} source={} distance={} required=22 block={} facing_up={} contaminated={} enlightened={} creative={}",p.method_5667(),source,distance,at,up,contaminated,enlightened,p.method_31549().field_7477);}
   if(!ImpalementFallTrace.highEnough(distance)||!contaminated||!up)continue;
   // The placed device is consumed by an effective impalement, including its fatal branch.
   if(!p.method_51469().method_8652(at,class_2246.field_10124.method_9564(),3))return false;
   if(!InnerAdvancements.done(p,"enlightenment")){
    p.method_6033(0);p.method_6078(com.innerdimension.domain.combat.DomainDamageTypes.source(p.method_51469(),VoidContamination.DAMAGE,null,null));return true;
   }
   VoidEmbodimentState.get(p).put(p.method_5667(),1);InnerAdvancements.grant(p,"impalement");
   com.innerdimension.projection.RoomProjectionManager.suppressForEmbodiment(p);
   com.innerdimension.pregnancy.MentalPregnancySystem.abortForEmbodiment(p);
   var room=DynamicDimensionManager.getOrCreate(p.method_51469().method_8503(),p.method_5667());
   RoomShellGenerator.ensureShell(room,p.method_5667());InfectionPillars.ensure(room,p.method_5667());LOGGER.info("IMPALEMENT_SUCCESS player={} source={} distance={}",p.method_5667(),source,distance);return true;
  }return false;
 }
 public static void register(){
  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.BaptismFinishedPayload.ID,com.innerdimension.network.BaptismFinishedPayload.CODEC);
  net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.BaptismFinishedPayload.ID,(payload,context)->context.server().execute(()->baptismFinished(context.player())));
  VoidNails.register();
  net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((e,d,a)->!(e instanceof class_3222 p&&flying(p)&&"minecraft:out_of_world".equals(com.innerdimension.domain.combat.DomainDamageTypes.keyOf(d))));
  net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((e,d)->{
   if(e instanceof class_3222 p){
    var id=p.method_5667();var saved=VoidEmbodimentState.get(p);int phase=saved.players.getOrDefault(id,0);
    if(VoidEmbodimentRules.afterDeath(phase)!=phase)saved.put(id,VoidEmbodimentRules.afterDeath(phase));
    var room=p.method_51469().method_8503().method_3847(DynamicDimensionManager.worldKey(id));
    if(room!=null)InfectionPillars.remove(room,id);
    FLIGHT.remove(id);BAPTISM_STARTED.remove(id);BEFORE.remove(id);DESCENTS.remove(id);LAST_ATTEMPT.remove(id);
   }
  });
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
   long now=server.method_30002().method_75260();for(var p:server.method_3760().method_14571()){
    if(p.method_5805()&&VoidEmbodimentState.get(p).players.getOrDefault(p.method_5667(),0)==4){if(!BAPTISM_STARTED.containsKey(p.method_5667()))baptismFilm(p);continue;}
    if(!p.method_5805()||!active(p))continue;
    if(!p.method_6059(effect()))p.method_6092(new class_1293(effect(),-1,0,false,true,true));
    com.innerdimension.pregnancy.MentalPregnancySystem.abortForEmbodiment(p);
    if(flying(p)){
     int ticks=FLIGHT.merge(p.method_5667(),1,Integer::sum);
     if(ticks%40==0){String[] sequence={"saturation","regeneration","fire_resistance","resistance","slow_falling"};int step=ticks/40-1;
      var buff=class_7923.field_41174.method_63535(class_2960.method_60655("minecraft",sequence[Math.min(4,step)]));p.method_6092(new class_1293(class_7923.field_41174.method_47983(buff),200,0));
      if(step>=4){
       var end=server.method_3847(VoidEmbodimentRules.baptismWorld());
       if(end==null||!com.innerdimension.transport.SafeEntityTeleport.move(p,end,p.method_23317(),320,p.method_23321(),Set.of(),p.method_36454(),p.method_36455(),false))continue;
       VoidEmbodimentState.get(p).put(p.method_5667(),4);p.method_6016(effect());FLIGHT.remove(p.method_5667());
       var room=server.method_3847(DynamicDimensionManager.worldKey(p.method_5667()));if(room!=null)InfectionPillars.remove(room,p.method_5667());
       p.field_6017=0;p.method_18799(class_243.field_1353);baptismFilm(p);
      }
     }
    }else{FLIGHT.remove(p.method_5667());if(now%40==0&&p.method_6032()>1)p.method_6033(Math.max(1,p.method_6032()-1));}
   }
   if(now%5==0)for(var world:server.method_3738()){
    var owner=DynamicDimensionManager.ownerOfWorld(world.method_27983());if(owner==null)continue;
    var state=world.method_8503().method_30002().method_17983().method_17924(VoidEmbodimentState.TYPE);if(!state.players.containsKey(owner))continue;
    if(!VoidEmbodimentRules.pillars(state.players.get(owner))){InfectionPillars.remove(world,owner);continue;}
    if(!world.method_18456().isEmpty())InfectionPillars.ensure(world,owner);
    for(int base:new int[]{0,32}){world.method_14199(new class_2390(0x30113F,1.3f),false,false,8,base+8,8,20,1.2,7,1.2,.004);
     if(now%80==0)ModSounds.at(world,new class_2338(8,base+8,8),"pillar_drone",.65f);
     if(base==32)for(int y:new int[]{32,48}){
      for(int n=0;n<12;n++)world.method_14199(new class_2390(0xF5BADB,.8f),false,false,7+world.method_8409().method_43058()*2,y,7+world.method_8409().method_43058()*2,0,(world.method_8409().method_43058()-.5)*.8,y==32?-1.8:1.8,(world.method_8409().method_43058()-.5)*.8,.6);
      if(now%40==0)ModSounds.at(world,new class_2338(8,y,8),y==32?"pillar_leak_bottom":"pillar_leak_top",.65f);
     }
    }
   }
  });
  net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s->{FLIGHT.clear();BEFORE.clear();DESCENTS.clear();LAST_ATTEMPT.clear();BAPTISM_STARTED.clear();InfectionPillars.clear();});
  net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{var id=handler.field_14140.method_5667();FLIGHT.remove(id);BEFORE.remove(id);DESCENTS.remove(id);LAST_ATTEMPT.remove(id);BAPTISM_STARTED.remove(id);});
 }
}

