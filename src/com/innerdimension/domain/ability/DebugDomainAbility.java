package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;
import net.minecraft.*;

/** Debug-only marker: no damage, status effects, summons or progression side effects. */
public final class DebugDomainAbility implements DomainAbility {
    public static final DebugDomainAbility INSTANCE=new DebugDomainAbility();
    private DebugDomainAbility() {}
    public String id(){return "inner_dimension:debug_particles";}
    public String code(){return "DEBUG";}
    public void onTick(DomainSession session,long now) {
        if(now%5!=0||!(session.world instanceof class_3218 world))return;
        int mask=session.state("debug_mask",DomainObjects.ALL_CREATURES);
        for(var entity:session.livingInside())if(DomainObjects.selected(session,mask,entity))
            world.method_14199(new class_2390(0xA855F7,1f),false,false,entity.method_23317(),entity.method_5829().field_1325+.3,entity.method_23321(),3,.12,.08,.12,0);
    }
}
