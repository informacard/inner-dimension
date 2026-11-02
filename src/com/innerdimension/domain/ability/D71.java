package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.DomainSession;
public final class D71 implements DomainAbility {
    public static final D71 INSTANCE=new D71();
    public String id(){return com.innerdimension.voiddomain.VoidContamination.ABILITY;}
    public String code(){return "D71";}
    public void onTick(DomainSession session,long now){if(now%10==0)for(var victim:com.innerdimension.domain.runtime.DomainObjects.inside(session,code()))com.innerdimension.voiddomain.VoidContamination.hurt(victim,4);}
}
