package com.innerdimension.domain.ability;
import com.innerdimension.domain.UnlimitedVoidRules;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.projection.ProjectionAnimation;
public final class D79 implements DomainAbility {
    public static final D79 INSTANCE=new D79();
    public String id(){return UnlimitedVoidRules.ID;}
    public String code(){return "D79";}
    public void onTick(DomainSession s,long now){
        // Open domains share the animated membership bounds of other open abilities.
        // Closed domains still wait for both projections to finish unfolding.
        if(!s.open()&&(s.shell.phase()!=ProjectionAnimation.STABLE||s.interior.phase()!=ProjectionAnimation.STABLE))return;
        for(var target:DomainObjects.inside(s,code()))UnlimitedVoidControl.expose(target);
    }
}
