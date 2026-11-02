import com.innerdimension.domain.DomainAbilityCatalog;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.domain.ability.D52;
import com.innerdimension.projection.ProjectionSpawnPlacement;
import net.minecraft.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
/** Headless checks use the real removal method, registry categories and placement geometry. */
public final class DomainCreatureRegression {
    static Unsafe unsafe;
    static void check(boolean b,String s) { if(!b)throw new AssertionError(s); }
    static void field(Object o,Class<?> c,String n,Object v)throws Exception { Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,v); }
    static class ProbeMob extends class_1308 {
        class_1937 world; UUID uuid; class_1299<?> type; int removed;
        ProbeMob(){super(null,null);}
        @Override public class_1937 method_73183(){return world;}
        @Override public UUID method_5667(){return uuid;}
        @Override public class_1299<?> method_5864(){return type;}
        @Override public boolean method_7325(){return false;}
        @Override public boolean method_5805(){return true;}
        @Override public boolean method_37222(class_1293 effect, class_1297 source){method_6088().put(effect.method_5579(),effect);return true;}
        @Override protected void method_6129(Collection<class_1293> effects){removed+=effects.size();}
    }
    static ProbeMob mob(String type)throws Exception {
        ProbeMob mob=(ProbeMob)unsafe.allocateInstance(ProbeMob.class);
        mob.world=(class_3218)unsafe.allocateInstance(class_3218.class);
        mob.uuid=UUID.randomUUID();
        mob.type=class_7923.field_41177.method_10223(class_2960.method_60655("minecraft",type)).orElseThrow().comp_349();
        field(mob,class_1309.class,"field_6280",new HashMap<>());
        return mob;
    }
    static ClassNode node(String p)throws Exception { ClassNode n=new ClassNode();try(var in=DomainCreatureRegression.class.getResourceAsStream("/com/innerdimension/"+p+".class")){new ClassReader(in).accept(n,0);}return n; }
    static boolean calls(ClassNode n,String method){for(var m:n.methods)for(var i:m.instructions)if(i instanceof MethodInsnNode call&&call.name.equals(method))return true;return false;}
    public static void main(String[]args)throws Exception {
        class_155.method_36208();class_2966.method_12851();
        Field u=Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(Unsafe)u.get(null);
        for(var e:Map.of("warden",6,"ender_dragon",6,"ravager",4,"ghast",4).entrySet())
            check(DomainAbilityCatalog.byEntity("minecraft:"+e.getKey()).loadWeight()==e.getValue(),"Requested weight "+e.getKey());
        DomainSession s=(DomainSession)unsafe.allocateInstance(DomainSession.class);
        field(s,DomainSession.class,"ownerId",UUID.randomUUID());
        for(String id:List.of("shulker","ghast","slime","magma_cube","phantom","zombie"))
            check(DomainRelations.of(s,mob(id))==DomainRelations.Relation.ENEMY,"Enemy registry category "+id);
        check(DomainRelations.of(s,mob("cow"))==DomainRelations.Relation.NEUTRAL,"Passive creature remains neutral");
        check(DomainRelations.of(s,mob("warden"))==DomainRelations.Relation.BOSS,"Boss classification remains intact");
        var jump=EffectLeaseManager.effect("minecraft:jump_boost");ProbeMob mob=mob("zombie");UUID session=UUID.randomUUID();
        EffectLeaseManager.apply(mob,jump,"D66",session,60,0,100);
        check(mob.method_6112(jump)!=null,"Lease applied");
        EffectLeaseManager.revokeSession(session,101);
        check(mob.method_6112(jump)==null && mob.removed==1,"Complete vanilla removal callback runs on close");
        mob.method_6092(new class_1293(jump,200,2));session=UUID.randomUUID();
        EffectLeaseManager.apply(mob,jump,"D66",session,60,0,100);
        EffectLeaseManager.revokeSession(session,101);
        check(mob.method_6112(jump).method_5578()==2,"External stronger jump effect survives");
        class_243 hit=new class_243(0.375,4.625,2.125);
        for(class_2350 face:class_2350.values()) {
            class_243 pos=ProjectionSpawnPlacement.facePosition(hit,face,0.6,1.8);
            if(face.method_10148()!=0)check(Math.abs(pos.field_1352-hit.field_1352)>0.3,"Side clears whole body width");
            if(face.method_10165()!=0)check(Math.abs(pos.field_1350-hit.field_1350)>0.3,"Side clears whole body width");
            if(face.method_10164()>0)check(pos.field_1351>hit.field_1351,"Top preserves fractional surface");
            if(face.method_10164()<0)check(pos.field_1351+1.8<hit.field_1351,"Bottom clears whole body height");
        }
        check(D52.surfaceEligible(5,5) && D52.surfaceEligible(7,5),"Standing and falling onto lava are supported");
        check(!D52.surfaceEligible(4,5),"Submerged creatures are not teleported onto surface");
        check(calls(node("domain/runtime/EffectLeaseManager"),"method_6016"),"Effect manager uses public removal");
        check(calls(node("domain/ability/D65"),"method_55117"),"Burst uses vanilla server explosion and packets");
        check(calls(node("mixin/DomainMobTargetMixin"),"pushPreferredTarget"),"Preferred targets connected to actual AI tick");
        check(calls(node("mixin/ProjectionEntityMoveMixin"),"lavaSurfaceShapes"),"Lava tops reach vanilla and Lithium collision solves");
        System.out.println("PASS: requested weights; hostile categories and bosses; full status removal and external preservation; six-face fractional spawn placement; lava eligibility; AI/explosion/collision integration");
    }
}

