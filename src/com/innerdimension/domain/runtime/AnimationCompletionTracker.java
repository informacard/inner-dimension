package com.innerdimension.domain.runtime;

import java.util.*;
import java.util.function.Predicate;

/** Distinguishes NoN join replacement from completion of an independent animation. */
public final class AnimationCompletionTracker {
 public record Observation(UUID instanceId,String animationId,Set<UUID> actors,String joinPredecessor) {}
 private static final class Entry {
  final Observation observation; final Set<UUID> victims=new HashSet<>();
  long missingSince=Long.MIN_VALUE;
  Entry(Observation observation){this.observation=observation;}
 }
 private final Map<UUID,Entry> tracked=new LinkedHashMap<>();
 public Set<UUID> update(UUID owner,List<Observation> observations,Predicate<UUID> eligible,long now){
  Map<UUID,Observation> active=new LinkedHashMap<>();
  for(var observation:observations){
   if(!observation.actors().contains(owner))continue;
   active.put(observation.instanceId(),observation);
   Entry entry=tracked.computeIfAbsent(observation.instanceId(),id->new Entry(observation));
   entry.missingSince=Long.MIN_VALUE;
   for(UUID actor:observation.actors())if(!actor.equals(owner)&&eligible.test(actor))entry.victims.add(actor);
  }
  Set<UUID> completed=new HashSet<>();
  var iterator=tracked.entrySet().iterator();
  while(iterator.hasNext()){
   var item=iterator.next();if(active.containsKey(item.getKey()))continue;
   Entry previous=item.getValue();
   Observation replacement=active.values().stream().filter(next->
    previous.observation.animationId().equals(next.joinPredecessor())&&next.actors().containsAll(previous.observation.actors())).findFirst().orElse(null);
   if(replacement!=null){
    tracked.get(replacement.instanceId()).victims.addAll(previous.victims);
    iterator.remove();continue;
   }
   // Give STOP -> START one server tick to publish the explicitly marked join successor.
   if(previous.missingSince==Long.MIN_VALUE){previous.missingSince=now;continue;}
   if(now<=previous.missingSince)continue;
   completed.addAll(previous.victims);iterator.remove();
  }
  return completed;
 }
}
