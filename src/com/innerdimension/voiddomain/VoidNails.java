package com.innerdimension.voiddomain;
import net.minecraft.*;
import java.util.*;
import com.innerdimension.progression.*;
public final class VoidNails {
 private record Shot(class_3222 owner,class_3218 world,class_243 pos,class_243 velocity,int age){}
 private static final List<Shot> SHOTS=new ArrayList<>();
 public static void fire(class_3222 p,double charge){SHOTS.add(new Shot(p,p.method_51469(),p.method_33571(),p.method_5828(1).method_1021(.6+2.4*charge),0));}
 public static void register(){
 net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
  var next=new ArrayList<Shot>();for(var s:SHOTS){if(s.age>=60||!s.owner.method_5805())continue;
   var end=s.pos.method_1019(s.velocity);var block=s.world.method_17742(new class_3959(s.pos,end,class_3959.class_3960.field_17558,class_3959.class_242.field_1348,s.owner));
   double limit=s.pos.method_1025(block.method_17784());class_1309 hit=null;
   for(var e:s.world.method_8390(class_1309.class,new class_238(s.pos,end).method_1014(1),e->e!=s.owner&&e.method_5805())){
    var intersection=e.method_5829().method_1014(.2).method_992(s.pos,end);if(intersection.isPresent()){double d=s.pos.method_1025(intersection.get());if(d<limit){limit=d;hit=e;}}
   }
   if(hit!=null){if(hit instanceof class_3222 player){net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,new com.innerdimension.network.EnlightenmentVisualPayload(4,true));}
    else if(hit instanceof class_1646)hit.method_5780("inner_dimension_void_awakened");continue;}
   if(block.method_17783()!=class_239.class_240.field_1333)continue;
   for(int i=0;i<5;i++){var v=s.pos.method_1019(s.velocity.method_1021(i/5.0));s.world.method_14199(new class_2390(0x713487,.6f),false,false,v.field_1352,v.field_1351,v.field_1350,1,0,0,0,0);}
   next.add(new Shot(s.owner,s.world,end,s.velocity.method_1031(0,-.025,0),s.age+1));
  }SHOTS.clear();SHOTS.addAll(next);
  if(server.method_30002().method_75260()%20==0)for(var world:server.method_3738())for(var player:world.method_18456())
   for(var villager:world.method_8390(class_1646.class,player.method_5829().method_1014(12),v->v.method_5805()&&v.method_5752().contains("inner_dimension_void_awakened")))
    if(player.method_5805()&&!player.method_7325())VillagerSoulChallenge.awakenedAttack(villager);
 });
 net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s->SHOTS.clear());
 }
}
