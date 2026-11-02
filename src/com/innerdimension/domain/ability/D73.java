package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;import net.minecraft.*;
public final class D73 implements DomainAbility {
 public static final D73 INSTANCE=new D73();public String id(){return "minecraft:salmon";}public String code(){return "D73";}
 private static final class_2960 MOD=class_2960.method_60655("inner_dimension","fish_water_movement");
 public void onTick(DomainSession s,long now){var a=D47D69Support.attribute("minecraft:water_movement_efficiency");if(a==null)return;D24D46Support.leaseSet(s,"d73_objects",DomainObjects.inside(s,code()),e->{double amount=DomainAttributeFloor.missing(e,a,MOD,1);AttributeLeaseManager.apply(e,a,MOD,amount,class_1322.class_1323.field_6330,amount>0,s.sessionId,code());},e->AttributeLeaseManager.apply(e,a,MOD,0,class_1322.class_1323.field_6330,false,s.sessionId,code()));}
}
