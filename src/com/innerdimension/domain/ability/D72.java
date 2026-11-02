package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;
import net.minecraft.*;
import java.util.*;
public final class D72 implements DomainAbility {
 public static final D72 INSTANCE=new D72();public String id(){return com.innerdimension.domain.DomainModes.SUCCUBUS;}public String code(){return "D72";}
 public void onTick(DomainSession s,long now){
  if(!(s.world instanceof class_3218 world))return;
  Set<UUID> participants=new HashSet<>();
  for(var snapshot:com.nonid.internal.animation.server.ServerAnimationController.getActiveInstanceSnapshots(world)){
   if(!snapshot.actorUuids().contains(s.ownerId))continue;
   boolean withCreature=snapshot.actorUuids().stream().anyMatch(actor->!actor.equals(s.ownerId)&&world.method_73284(actor) instanceof class_1309 living&&!(living instanceof class_1657));
   if(withCreature)participants.addAll(snapshot.actorUuids());
  }
  var active=s.livingInside().stream().filter(e->participants.contains(e.method_5667())&&(e.method_5667().equals(s.ownerId)||!(e instanceof class_1657))).filter(e->DomainObjects.matches(s,code(),e)).toList();
  D24D46Support.leaseSet(s,"d72_objects",active,e->{EffectLeaseManager.apply(e,"minecraft:resistance",code(),s.sessionId,60,2,now);EffectLeaseManager.apply(e,"minecraft:regeneration",code(),s.sessionId,60,1,now);},e->{EffectLeaseManager.revoke(e,EffectLeaseManager.effect("minecraft:resistance"),s.sessionId,code(),now);EffectLeaseManager.revoke(e,EffectLeaseManager.effect("minecraft:regeneration"),s.sessionId,code(),now);});
 }
}