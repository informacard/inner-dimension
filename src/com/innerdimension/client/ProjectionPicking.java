package com.innerdimension.client;
import com.innerdimension.projection.*;
import net.minecraft.*;
/** Projection surfaces route held-item use to destination-world coordinates. */
public final class ProjectionPicking {
 public static boolean attack(class_310 client){
  var player=client.field_1724;
  if(player==null||client.field_1687==null||client.field_1761==null)return false;
  class_243 start=player.method_33571(),end=start.method_1019(player.method_5828(1).method_1021(player.method_55755()));
  RoomProjectionEntity target=null;double nearest=Double.POSITIVE_INFINITY;
  for(var shell:ProjectionIndex.solid(client.field_1687,new class_238(start,end).method_1014(1))){
   if(!shell.isShell()||shell.owner()==null||shell.owner().equals(player.method_5667())||shell.open()||shell.phase()==ProjectionAnimation.RETRACTING)continue;
   var snapshot=(RoomProjectionContraption)shell.getContraption();if(snapshot==null||snapshot.sourceSpec().shell()==null)continue;
   var box=shell.method_5829();boolean inside=box.method_1008(player.method_23317(),player.method_23318()+player.method_17682()*.5,player.method_23321());
   if(!DomainLifetime.vulnerableSide(shell.reversedShell(),inside))continue;
   // Reverse the ray when starting inside: Box.raycast then finds the exit surface.
   var hit=inside?box.method_992(end,start):box.method_992(start,end);
   if(hit.isEmpty())continue;double distance=start.method_1025(hit.get());
   var real=client.field_1765;
   if(real!=null&&real.method_17783()!=class_239.class_240.field_1333&&start.method_1025(real.method_17784())<distance-1e-5)continue;
   if(distance<nearest){target=shell;nearest=distance;}
  }
  if(target==null)return false;
  client.field_1761.method_2918(player,target);player.method_6104(class_1268.field_5808);return true;
 }
 public static boolean use(class_310 client){
  if(com.innerdimension.client.domain.DomainContestClient.simple())return true;
  var player=client.field_1724;
  if(player==null || client.field_1687==null || client.field_1761==null || player.method_6115())return false;
  class_243 start=player.method_33571(),end=start.method_1019(player.method_5828(1).method_1021(player.method_55754()));
  class_3965 miss=class_3965.method_17778(end,class_2350.field_11036,class_2338.method_49638(end));
  class_3965 projected=ProjectionProjectiles.hit(client.field_1687,new class_3959(start,end,class_3959.class_3960.field_17559,class_3959.class_242.field_1348,player),miss,true);
  if(projected==miss)return false;
  var real=client.field_1765;
  if(real!=null && real.method_17783()!=class_239.class_240.field_1333 && start.method_1025(real.method_17784())<=start.method_1025(projected.method_17784()))return false;
  class_3965 worldHit=ProjectionItemTarget.worldHit(projected.method_17784(),projected.method_17780());
  for(class_1268 hand:class_1268.values()){
   if(player.method_5998(hand).method_7960())continue;
   class_1269 result=client.field_1761.method_2896(player,hand,worldHit);
   if(result==class_1269.field_5811)result=client.field_1761.method_2919(player,hand);
   if(result instanceof class_1269.class_9860 success){
    if(success.comp_2909()==class_1269.class_9861.field_52427)player.method_6104(hand);
    client.field_1773.field_4012.method_3215(hand);break;
   }
   if(result==class_1269.field_5814)break;
  }
  return true;
 }
}
