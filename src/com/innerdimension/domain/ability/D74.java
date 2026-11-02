package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;import net.minecraft.*;import java.util.*;
public final class D74 implements DomainAbility {
 public static final D74 INSTANCE=new D74();public String id(){return "minecraft:squid";}public String code(){return "D74";}
 private static final String NAME_TAG="inner_dimension:temporary_dinnerbone";
 // Shared across overlapping domains: only the last contributor restores the name.
 private static final Map<class_1309,Set<UUID>> NAMED=new WeakHashMap<>();
 public void onTick(DomainSession s,long now){var current=DomainObjects.inside(s,code());List<class_1309> previous=s.state("d74_references",List.of());for(var e:previous)if(!current.contains(e)){release(e,s.sessionId);EffectLeaseManager.revoke(e,EffectLeaseManager.effect("inner_dimension:upside_down"),s.sessionId,code(),now);}for(var e:current){
  if(!(e instanceof class_1657)&&e.method_5797()==null){e.method_5665(class_2561.method_43470("Dinnerbone"));e.method_5752().add(NAME_TAG);NAMED.computeIfAbsent(e,k->new HashSet<>()).add(s.sessionId);}else if(NAMED.containsKey(e))NAMED.get(e).add(s.sessionId);
  EffectLeaseManager.apply(e,"inner_dimension:upside_down",code(),s.sessionId,60,0,now);
 }s.putState("d74_references",List.copyOf(current));}
 private static void release(class_1309 e,UUID session){var sources=NAMED.get(e);if(sources==null)return;sources.remove(session);if(!sources.isEmpty())return;NAMED.remove(e);if(e.method_5752().remove(NAME_TAG)&&e.method_5797()!=null&&e.method_5797().getString().equals("Dinnerbone"))e.method_5665(null);}
 public void onEnd(DomainSession s){for(var e:new ArrayList<>(NAMED.keySet()))release(e,s.sessionId);}
 public static void restoreOrphanName(class_1309 e){if(e.method_73183().method_8608()||!e.method_5752().contains(NAME_TAG)||NAMED.containsKey(e))return;e.method_5752().remove(NAME_TAG);if(e.method_5797()!=null&&e.method_5797().getString().equals("Dinnerbone"))e.method_5665(null);e.method_6016(EffectLeaseManager.effect("inner_dimension:upside_down"));}
}
