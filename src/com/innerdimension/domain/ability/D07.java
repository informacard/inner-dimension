package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;
public final class D07 implements DomainAbility {
 public static final D07 INSTANCE=new D07();
 public String id(){return "minecraft:rabbit";}public String code(){return "D07";}
 public void onTick(DomainSession s,long now){D24D46Support.leaseSet(s,"D07_objects",DomainObjects.inside(s,code()),e->EffectLeaseManager.apply(e,"needsofnature:fertility_increaser",code(),s.sessionId,60,1,now),e->EffectLeaseManager.revoke(e,EffectLeaseManager.effect("needsofnature:fertility_increaser"),s.sessionId,code(),now));}
}
