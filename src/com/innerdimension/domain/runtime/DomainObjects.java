package com.innerdimension.domain.runtime;
import net.minecraft.*;
import java.util.*;

/** Per-ability object selection. Relations describe allegiance, never an ability's scope. */
public final class DomainObjects {
    public static final int FRIENDLY=1,NEUTRAL=2,HOSTILE=4,ACTIVE=8,PASSIVE=16,SELF=32,ALL_CREATURES=SELF|FRIENDLY|NEUTRAL|HOSTILE;
    private DomainObjects(){}
    // Preserve existing engraved behaviour until the player explicitly chooses a mask.
    public static boolean configurable(String code){return switch(code){
        // Two kinds of "cannot select". No object at all: self input/gear/charge
        // (D06,D10,D36,D41,D43,D48), container/item/block/summon mechanics
        // (D19,D21,D23,D35,D44,D61), real copper blocks (D27, the mirror of D44) and the
        // fixed kill credit of D20. Fixed scope that is not a player choice: companion
        // cooperation (D09,D55) and the always-all-creatures melee knockback (D56).
        // D00 is unimplemented.
        case "D72","D37","D58","D65","D64","D00","D06","D09","D10","D19","D20","D21","D23","D27","D35","D36","D41","D43","D44","D48","D55","D56","D61","D70"->false;
        default->true;
    };}
    public static int availableMask(String code){return code.equals("D76")?63&~SELF:63;}
    public static int defaultMask(String code){if(com.innerdimension.domain.DomainModes.selectable(code))return 1;return switch(code){
        case "D26","D30","D34","D72","D73","D40","D60","D63","D01","D07","D11","D13","D14","D18","D38","D42","D49","D53","D54","D03","D12","D25","D66"->SELF|FRIENDLY;
        case "D79","D77","D75","D44","D08","D09","D15","D28","D29","D31","D32","D33","D37","D45","D46","D47","D51","D55","D57","D59","D62","D64","D67","D68"->HOSTILE;
        case "D76"->FRIENDLY|NEUTRAL|HOSTILE;
        case "D06","D10","D17","D19","D21","D23","D36","D41","D43","D48"->SELF;
        default->ALL_CREATURES;
    };}
    public static boolean matches(DomainSession session,String code,class_1297 entity){
        return session!=null&&entity!=null&&session.contains(entity)&&qualifies(session,code,entity);
    }
    public static boolean qualifies(DomainSession session,String code,class_1297 entity){
        if(session==null||entity==null||entity.method_7325())return false;
        if(com.innerdimension.boss.BossDomains.domainImmune(entity))return false;
        if(com.innerdimension.domain.DomainContest.suppressed(session.ownerId))return false;
        if(session.hasCode("D70")&&com.innerdimension.domain.DomainModes.reverse(session,"D70"))return session.ownerId.equals(entity.method_5667());
        if(com.innerdimension.domain.SimpleDomain.active(entity)&&!session.ownerId.equals(entity.method_5667()))return false;
        if(com.innerdimension.domain.SimpleDomain.locked(session.ownerId,entity))return true;
        if(com.innerdimension.domain.DomainContest.opponents(session.ownerId,entity.method_5667()))return true;
        if(code.equals("D72"))return session.ownerId.equals(entity.method_5667())||!(entity instanceof class_1657); if(code.equals("D44"))return !session.ownerId.equals(entity.method_5667()); if(code.equals("D76")&&session.ownerId.equals(entity.method_5667()))return false; if(java.util.Set.of("D37","D58","D65").contains(code))return true;
        int mask=configurable(code)&&!com.innerdimension.domain.DomainModes.selectable(code)?session.objectMask(code):defaultMask(code);return selected(session,mask,entity);
    }
    public static boolean selected(DomainSession session,int mask,class_1297 entity) {
        DomainRelations.Relation relation=DomainRelations.of(session,entity);
        if(relation==DomainRelations.Relation.SELF)return (mask&SELF)!=0;
        int category=switch(relation){case SELF->SELF;case ALLY->FRIENDLY;case NEUTRAL->NEUTRAL;case ENEMY,BOSS->HOSTILE;};
        return (mask&category)!=0 || (mask&ACTIVE)!=0&&session.activeObjects.contains(entity.method_5667()) || (mask&PASSIVE)!=0&&passive(session,entity);
    }
    public static List<class_1309> inside(DomainSession session,String code){return session.livingInside().stream().filter(e->matches(session,code,e)).toList();}
    public static class_1309 nearest(DomainSession session,String code,class_243 from){return inside(session,code).stream().min(Comparator.comparingDouble(e->e.method_33571().method_1025(from))).orElse(null);}
    private static boolean passive(DomainSession session,class_1297 entity){
        if(!(entity instanceof class_1308 mob))return false;class_1309 owner=session.ownerEntity();if(owner==null)return false;
        class_1309 target=mob.method_5968();if(target!=null&&target.method_5667().equals(session.ownerId))return true;
        var attack=mob.method_18868().method_46873(class_4140.field_22355);if(attack.isPresent()&&attack.get().method_5667().equals(session.ownerId))return true;
        var path=mob.method_5942().method_6345();if(path==null||path.method_46())return false;
        class_2338 destination=path.method_48();if(destination==null)return false;
        double dx=destination.method_10263()+0.5-owner.method_23317(),dy=destination.method_10264()-owner.method_23318(),dz=destination.method_10260()+0.5-owner.method_23321();
        double endDistance=dx*dx+dy*dy+dz*dz;return endDistance<=9&&endDistance<mob.method_33571().method_1025(owner.method_33571());
    }
    public static void attacked(class_1309 attacker,class_1309 target){for(DomainSession session:DomainSessionIndex.sessions())if(session.ownerId.equals(attacker.method_5667())&&session.contains(target))session.activeObjects.add(target.method_5667());}
}
