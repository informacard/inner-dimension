package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;import net.minecraft.*;
/** Double new external beneficial effects once, never the lease refreshes. */
public final class D34 implements DomainAbility {
 public static final D34 INSTANCE=new D34();public String id(){return "minecraft:parched";}public String code(){return "D34";}
 public static class_1293 extend(class_1309 entity,class_1293 incoming){
  if(entity.method_73183().method_8608()||EffectLeaseManager.applying()||incoming.method_5584()<=0||incoming.method_5579().comp_349().method_18792()!=class_4081.field_18271||incoming.method_5579().comp_349().method_5561())return incoming;
  for(var s:DomainSessionIndex.covering(entity))if(s.hasCode("D34")&&DomainObjects.matches(s,"D34",entity))return incoming.method_66227(2f);
  return incoming;
 }
}
