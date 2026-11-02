package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.network.DomainSlashVisualPayload;
import java.util.*;
import net.minecraft.*;

/** A penetrating straight-flight blade, replacing the owner's left-click attack. */
public final class D44 implements DomainAbility, AttackHook {
 public static final D44 INSTANCE=new D44();
 public static final double SPEED=2;
 public static final float DAMAGE=4;
 public static final int COOLDOWN=DomainSlashMath.cooldownTicks(.33);
 private static final String FLIGHTS="d44_flights";
 private static final class Flight {
  class_243 position;final class_243 direction,blade;
  final Set<UUID> hit=new HashSet<>();
  Flight(class_243 position,class_243 direction,class_243 blade){this.position=position;this.direction=direction;this.blade=blade;}
 }
 private D44(){}
 public String id(){return "minecraft:vindicator";}public String code(){return "D44";}
 public Boolean onOwnerMelee(DomainSession s,class_3222 owner,class_1309 target){return s.contains(owner)?Boolean.TRUE:null;}
 @SuppressWarnings("unchecked") private static List<Flight> flights(DomainSession s){
  List<Flight> list=s.state(FLIGHTS,(List<Flight>)null);
  if(list==null){list=new ArrayList<>();s.putState(FLIGHTS,list);}return list;
 }
 public static boolean fire(DomainSession s,class_3222 owner,long now){
  if(!s.hasCode("D44")||s.ended()||!s.phaseActive()||!s.contains(owner)||owner.method_7325()||!s.dueSelf("d44_fire",COOLDOWN,now))return false;
  class_243 origin=owner.method_33571(),direction=owner.method_5828(1).method_1029();
  if(!s.containsPoint(origin.field_1352,origin.field_1351,origin.field_1350))return false;
  class_243 blade=new class_243(-direction.field_1350,0,direction.field_1352);
  blade=blade.method_1033()<1e-6?new class_243(1,0,0):blade.method_1029();
  flights(s).add(new Flight(origin,direction,blade));
  s.armSelf("d44_fire",now);DomainDamagePipeline.markAttack(owner);owner.method_23667(class_1268.field_5808,true);return true;
 }
 public static boolean enemy(DomainRelations.Relation relation,boolean attacked){
  return relation!=DomainRelations.Relation.SELF;
 }
 public void onTick(DomainSession s,long now){
  if(!(s.world instanceof class_3218 world))return;var owner=s.owner();if(owner==null)return;
  var flights=flights(s);if(flights.isEmpty())return;
  List<class_1309> candidates=DomainObjects.inside(s,code());
  List<DomainSlashVisualPayload.Stroke> strokes=new ArrayList<>();
  var iterator=flights.iterator();
  while(iterator.hasNext()){
   Flight flight=iterator.next();class_243 start=flight.position;
   if(!s.containsPoint(start.field_1352,start.field_1351,start.field_1350)){iterator.remove();continue;}
   class_243 next=start.method_1019(flight.direction.method_1021(SPEED));
   boolean leaving=!s.containsPoint(next.field_1352,next.field_1351,next.field_1350);
   if(leaving)next=DomainSlashMath.clipToRange(start,next,p->s.containsPoint(p.field_1352,p.field_1351,p.field_1350));
   for(var victim:candidates){
    if(flight.hit.contains(victim.method_5667()))continue;
    if(victim.method_5667().equals(s.ownerId))continue;
    if(!DomainSlashMath.intersects(start,next,victim.method_5829(),.6))continue;
    flight.hit.add(victim.method_5667());
    if(DomainDamagePipeline.hurt(victim,owner,DomainDamageTypes.FLYING_SLASH,DAMAGE)){
     DomainObjects.attacked(owner,victim);
     if(victim.method_5805())victim.method_6005(.6,-flight.direction.field_1352,-flight.direction.field_1350);
    }
   }
   strokes.add(new DomainSlashVisualPayload.Stroke(next.method_1020(flight.blade.method_1021(1.1)),next.method_1019(flight.blade.method_1021(1.1)),0xE5A078,2));
   strokes.add(new DomainSlashVisualPayload.Stroke(start,next,0xF5DECE,2));
   flight.position=next;if(leaving)iterator.remove();
  }
  DomainSlashVisualPayload.send(world,strokes);
 }
 public void onEnd(DomainSession s){s.putState(FLIGHTS,null);}
}
