package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;
public final class D26 implements DomainAbility {
 public static final D26 INSTANCE=new D26();public String id(){return "minecraft:snow_golem";}public String code(){return "D26";}
 public void onTick(DomainSession s,long now){for(var e:DomainObjects.inside(s,code()))e.method_5646();}
}
