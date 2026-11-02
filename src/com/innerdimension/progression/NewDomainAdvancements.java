package com.innerdimension.progression;
import net.minecraft.*;
import com.innerdimension.domain.DomainContest;
import com.innerdimension.domain.runtime.DomainSessionIndex;
public final class NewDomainAdvancements {
    private NewDomainAdvancements(){}
    public static void died(class_1309 victim,class_1282 source){
            var attacker=source.method_5529();
            if(victim instanceof class_3222 p){
                if(DomainSessionIndex.covering(victim).stream().anyMatch(s->s.ownerId.equals(p.method_5667())))InnerAdvancements.grant(p,"suicide_king_i");
                if(com.innerdimension.voiddomain.VoidContamination.isDamage(source)&&DomainSessionIndex.covering(victim).stream().anyMatch(session->session.ownerId.equals(p.method_5667())))InnerAdvancements.grant(p,"suicide_king_ii");
            }
            if(attacker instanceof class_3222 p){
                var own=DomainContest.entry(p.method_5667());var enemy=DomainContest.entry(victim.method_5667());
                if(own!=null&&enemy!=null&&com.innerdimension.domain.DomainContest.opponents(p.method_5667(),victim.method_5667())&&!com.innerdimension.domain.combat.DomainDamageTypes.isDomainDamage(source))InnerAdvancements.grant(p,"physical_attack");
            }
    }
}
