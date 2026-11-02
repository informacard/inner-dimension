package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;import net.minecraft.*;import java.util.*;
public final class D75 implements DomainAbility,AiHook {
 public static final D75 INSTANCE=new D75();public String id(){return "minecraft:evoker";}public String code(){return "D75";}
 public void onStable(DomainSession s){for(int i=0;i<3;i++){var mob=DomainSummons.spawn(s,code(),"minecraft:vex");if(mob!=null){var sword=D01D23Support.item("minecraft:iron_sword");if(sword!=null)mob.method_5673(class_1304.field_6169,new class_1799(sword));}}}
 public List<class_1309> preferredTargets(DomainSession s,class_1308 mob){return DomainSummons.list(s,code()).contains(mob)?DomainObjects.inside(s,code()).stream().filter(e->!DomainRelations.isFriendly(s,e)).sorted(Comparator.comparingDouble(mob::method_5858)).toList():List.of();}
 public void onEnd(DomainSession s){DomainSummons.end(s,code());}
}
