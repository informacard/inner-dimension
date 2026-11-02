package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;import com.innerdimension.domain.combat.*;import net.minecraft.*;import java.util.*;
public final class D37 implements DomainAbility,AttackHook {
 public static final D37 INSTANCE=new D37();public String id(){return "minecraft:cave_spider";}public String code(){return "D37";}
 public void afterMelee(DomainSession s,class_3222 attacker,class_1309 target,float taken){if(com.innerdimension.domain.DomainModes.reverse(s,code())||target==null||taken<=0||!s.contains(target))return;EffectLeaseManager.apply(target,"minecraft:poison",code(),s.sessionId,120,1,DomainDamagePipeline.now());}
 public void onTick(DomainSession s,long now){
  if(!com.innerdimension.domain.DomainModes.reverse(s,code())||!(s.world instanceof class_3218 world))return;
  AnimationCompletionTracker tracker=s.state("d37_completion",(AnimationCompletionTracker)null);
  if(tracker==null){tracker=new AnimationCompletionTracker();s.putState("d37_completion",tracker);}
  var snapshots=com.nonid.internal.animation.server.ServerAnimationController.getActiveInstanceSnapshots(world);
  var observations=snapshots.stream().map(snapshot->new AnimationCompletionTracker.Observation(snapshot.instanceId(),snapshot.animationId().toString(),Set.copyOf(snapshot.actorUuids()),snapshot.startContext().joinPredecessor()==null?null:snapshot.startContext().joinPredecessor().toString())).toList();
  Set<UUID> completed=tracker.update(s.ownerId,observations,actor->{var raw=world.method_73284(actor);return raw instanceof class_1309 living&&!(living instanceof class_1657)&&!com.innerdimension.boss.BossDomains.domainImmune(living)&&s.contains(living);},now);
  var owner=s.owner();if(owner==null)return;
  for(UUID actor:completed){var raw=world.method_73284(actor);if(raw instanceof class_1309 victim&&!(victim instanceof class_1657)&&!victim.method_7325()&&victim.method_5805()&&!com.innerdimension.boss.BossDomains.domainImmune(victim)&&s.contains(victim)&&victim.method_6032()<=50){victim.method_66277(owner,100);victim.method_6015(owner);victim.method_6033(0);victim.method_6078(DomainDamageTypes.source(world,"minecraft:player_attack",owner,owner));}}
 }
 public void onEnd(DomainSession s){s.putState("d37_completion",null);}
}