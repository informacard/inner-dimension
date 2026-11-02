import com.innerdimension.domain.ability.*;
import com.innerdimension.domain.DomainAbilityCatalog;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import com.innerdimension.projection.*;
import com.innerdimension.projection.builtin.AbstractContraptionEntity;
import net.minecraft.*;
import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class WolfHoofSynergyRegression {
    static Unsafe unsafe;
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static void set(Object o,Class<?> c,String field,Object value)throws Exception{
        Field f=c.getDeclaredField(field);f.setAccessible(true);f.set(o,value);
    }
    static class Body extends class_1297 {
        Body(){super(class_7923.field_41177.method_10223(class_2960.method_12829("minecraft:wolf")).orElseThrow().comp_349(),null);}
        protected void method_5693(class_2945.class_9222 b){}
        protected void method_5749(class_11368 v){}
        protected void method_5652(class_11372 v){}
        public boolean method_64397(class_3218 w,class_1282 s,float a){return false;}
    }
    static class World extends class_3218 {
        List<class_238> blocks;
        World(){super(null,null,null,null,null,null,false,0,List.of(),false,null);}
        @Override public List<class_265> method_20743(class_1297 e,class_238 region){
            return blocks.stream().filter(b->b.method_994(region)).map(class_259::method_1078).toList();
        }
        @Override public boolean method_8587(class_1297 e,class_238 box){return true;}
        @Override public class_3610 method_8316(class_2338 pos){return class_3612.field_15906.method_15785();}
    }
    static class DamageProbe extends class_1308 {
        float total; int calls;
        class_1293 effect;
        DamageProbe(){super(null,null);}
        @Override public boolean method_5805(){return true;}
        @Override public boolean method_7325(){return false;}
        @Override public class_1293 method_6112(class_6880<class_1291> holder){return effect;}
        @Override public boolean method_37222(class_1293 value,class_1297 source){effect=value;return true;}
        @Override public boolean method_64397(class_3218 w,class_1282 source,float amount){
            check(field_6008==0,"Every autonomous damage component reaches vanilla with cleared immunity frames");
            total+=amount;calls++;field_6008=20;return true;
        }
    }
    static DomainSession session(World world)throws Exception{
        DomainSession s=(DomainSession)unsafe.allocateInstance(DomainSession.class);
        set(s,DomainSession.class,"world",world);
        for(String name:List.of("minX","minZ"))set(s,DomainSession.class,name,-5d);
        for(String name:List.of("maxX","maxZ"))set(s,DomainSession.class,name,5d);
        set(s,DomainSession.class,"minY",10d);set(s,DomainSession.class,"maxY",20d);
        RoomProjectionEntity shell=(RoomProjectionEntity)unsafe.allocateInstance(RoomProjectionEntity.class);
        set(shell,AbstractContraptionEntity.class,"contraption",new RoomProjectionContraption());
        set(s,DomainSession.class,"shell",shell);
        return s;
    }
    public static void main(String[] args)throws Exception{
        class_155.method_36208();class_2966.method_12851();
        // Vanilla's standalone bootstrap freezes registries; Fabric normally registers
        // these effects earlier. Reproduce that registration in this isolated test JVM.
        Field frozen=class_2370.class.getDeclaredField("field_36463");frozen.setAccessible(true);
        frozen.setBoolean(class_7923.field_41174,false);
        try{com.innerdimension.effect.ModStatusEffects.init();}finally{frozen.setBoolean(class_7923.field_41174,true);}
        Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);
        Body wolf=new Body();
        // Reproduce the exact vanilla raw-position bug, then verify setPos synchronizes it.
        wolf.method_23327(42,10.01,-17);
        check(wolf.method_5829().field_1322!=wolf.method_23318(),"Raw position leaves a stale collision box");
        wolf.method_5814(42,10.01,-17);
        check(Math.abs(wolf.method_5829().field_1322-10.01)<1e-6,"setPos updates wolf feet");
        check(Math.abs((wolf.method_5829().field_1323+wolf.method_5829().field_1320)/2-42)<1e-6,"setPos updates horizontal collision box");
        ClassNode helper=new ClassNode();
        try(var stream=WolfHoofSynergyRegression.class.getResourceAsStream("/com/innerdimension/domain/ability/D47D69Support.class")){new ClassReader(stream).accept(helper,0);}
        boolean positioned=false;
        for(var method:helper.methods)if(method.name.equals("create"))for(var insn:method.instructions)if(insn instanceof MethodInsnNode call){
            check(!call.name.equals("method_23327"),"Summon factory cannot use raw-position-only update");
            positioned|=call.name.equals("method_5814");
        }
        check(positioned,"Factory uses the verified full position update");
        World world=(World)unsafe.allocateInstance(World.class);DomainSession s=session(world);
        world.blocks=List.of(new class_238(-5,9,-5,5,10.01,5));
        class_243 spawn=DomainGeometry.safeSummonPoint(s,wolf,0,10.01,0);
        check(spawn!=null&&Math.abs(spawn.field_1351-10.011)<1e-6,"Exact fractional floor is used instead of rounded room-height grid");
        world.blocks=List.of(new class_238(-5,9,-5,5,10.5,5));
        spawn=DomainGeometry.safeSummonPoint(s,wolf,0,10,0);
        check(spawn!=null&&Math.abs(spawn.field_1351-10.501)<1e-6,"Slab-height surface is used exactly");
        world.blocks=List.of(new class_238(-5,9,-5,5,10.01,5),new class_238(-.5,10.01,-.5,.5,14,.5));
        spawn=DomainGeometry.safeSummonPoint(s,wolf,0,10.01,0);
        check(spawn!=null,"Nearby clear spot is found beside an obstructed column");
        wolf.method_5814(spawn.field_1352,spawn.field_1351,spawn.field_1350);
        check(!DomainGeometry.solid(world,wolf.method_5829()),"Final real wolf body is outside all solid geometry");
        world.blocks=List.of(new class_238(-5,9,-5,5,10.01,5),new class_238(-5,10.7,-5,5,20,5));
        check(DomainGeometry.safeSummonPoint(s,wolf,0,10.01,0)==null,"No spawn into a floor-ceiling gap too low for the wolf");
        world.blocks=List.of(new class_238(-5,14,-5,5,15,5));
        check(DomainGeometry.safeSummonPoint(s,wolf,0,10.01,0)==null,"No teleport onto a distant upper floor");
        check(D09.SUMMON_INTERVAL==100&&D09.MAX_SUMMONS==6,"Five-second summon interval and six-wolf limit");
        check(D15.DAMAGE==6&&D15.COOLDOWN==30&&D15.RANGE==4.5,"Rear kick balance");
        check(DomainAbilityCatalog.byEntity("minecraft:horse").loadWeight()==3,"Rear kick capacity cost reduced");
        class_243 facing=new class_243(0,0,1);
        check(D15.rearTarget(facing,new class_243(0,0,-4.5)),"Rear range includes 4.5-block boundary");
        check(D15.rearTarget(facing,new class_243(4,0,-.1)),"Wide rear arc catches flankers");
        check(!D15.rearTarget(facing,new class_243(0,0,1)),"Front enemies cannot trigger rear kick");
        check(!D15.rearTarget(facing,new class_243(0,0,-4.51)),"Outside range is excluded");
        class_243 still=new class_243(0,10,0);
        check(!D46.moved(still,still),"Stillness does not trigger sonic strike");
        check(!D46.moved(null,still),"Initial observation does not count as displacement");
        check(D46.moved(still,still.method_1031(0,.55,0)),"Ground shock's vertical knockback triggers sonic strike");
        check(D46.moved(still,still.method_1031(0,-.08,0)),"Falling after the launch also counts");
        check(D46.moved(still,still.method_1031(.1,0,0)),"External horizontal knockback counts");
        check(D46.moved(still,still.method_1031(.001,0,0)),"Slow crouch displacement counts");
        DamageProbe victim=(DamageProbe)unsafe.allocateInstance(DamageProbe.class);
        Method component=DomainDamagePipeline.class.getDeclaredMethod("component",class_1309.class,class_3218.class,class_1282.class,float.class,boolean.class);
        component.setAccessible(true);victim.field_6008=20;
        for(float damage:new float[]{1.5f,.5f,4f})
            check((boolean)component.invoke(null,victim,world,null,damage,true),"Shock physical, shock magic and sonic hit all land on one tick");
        check(victim.calls==3&&victim.total==6,"No damage is swallowed by the immunity window");
        check(!D40.discardProjectile(wolf)&&!D40.discardProjectile(victim),"Dodge never discards virtual-slash casters or living spitters");
        check(D40.discardProjectile((class_1667)unsafe.allocateInstance(class_1667.class)),"A real arrow is still removed on dodge");
        check(!D40.discardProjectile((class_1685)unsafe.allocateInstance(class_1685.class)),"A trident is preserved for return");
        check(D69.flowerPosition(.5,64,.5).method_10264()==64,"Flower query uses flower cell, not soil below the feet");
        check(D69.flowerPosition(.5,-1,.5).method_10264()==-1,"Flower query also works below Y=0");
        var regen=EffectLeaseManager.effect("minecraft:regeneration");
        DamageProbe patient=(DamageProbe)unsafe.allocateInstance(DamageProbe.class);
        UUID lease=UUID.randomUUID();int pulses=0;
        for(int tick=0;tick<200;tick++) {
            EffectLeaseManager.apply(patient,regen,"D69",lease,D69.REGEN_TICKS,0,tick);
            int remaining=patient.effect.method_5584();
            if(regen.comp_349().method_5552(remaining,0))pulses++;
            patient.effect=new class_1293(regen,remaining-1,0);
        }
        check(pulses==4,"Real effect lease and vanilla Regeneration I produce one heal every 50 ticks");
        System.out.println("PASS: real wolf-sized spawn geometry and bounding-box synchronization, fractional floors/slabs/obstructions/headroom, summon interval/cap, rear kick balance/arc, passive movement sonic synergy");
    }
}
