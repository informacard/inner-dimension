import com.innerdimension.projection.*;
import com.innerdimension.projection.builtin.world.VirtualRenderWorld;
import net.minecraft.*;
import java.lang.reflect.*;
import java.util.*;

/** Runs vanilla's actual fluid scan and carrier base tick in a small headless world. */
public final class ProjectionCarrierFluidRegression {
    static int checks;
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    static final class ProbeWorld extends VirtualRenderWorld {
        int reads;class_3610 fluid;class_12205 attributes;long time;
        ProbeWorld(){super(null,0,64,null,()->{});}
        @Override public class_3610 method_8316(class_2338 pos){reads++;return fluid;}
        @Override public class_2680 method_8320(class_2338 pos){return class_2246.field_10124.method_9564();}
        @Override public boolean method_33597(int minX,int minZ,int maxX,int maxZ){return true;}
        @Override public class_12205 method_75728(){return attributes;}
        @Override public long method_75260(){return time;}
    }
    static final class OrdinaryEntity extends class_1297 {
        OrdinaryEntity(class_1299<?> type,class_1937 world){super(type,world);}
        @Override protected void method_5693(class_2945.class_9222 builder){}
        @Override protected void method_5749(class_11368 view){}
        @Override protected void method_5652(class_11372 view){}
        @Override public boolean method_64397(class_3218 world,class_1282 damage,float amount){return false;}
        @Override public boolean method_5675(){return false;}
    }
    static ProjectionUpdatePayload.Entry entry(class_2338 pos,class_2680 state){return new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(state),null);}
    public static void main(String[] args)throws Exception {
        class_155.method_36208();class_2966.method_12851();
        // This standalone JVM has no datapack reload. Bind the vanilla fluid tags for the test.
        var bind=class_6880.class_6883.class.getDeclaredMethod("method_40235",Collection.class);bind.setAccessible(true);
        for(class_3611 fluid:class_7923.field_41173){
            Collection<?> tags=fluid==class_3612.field_15910||fluid==class_3612.field_15909?Set.of(class_3486.field_15517)
                    :fluid==class_3612.field_15908||fluid==class_3612.field_15907?Set.of(class_3486.field_15518):Set.of();
            bind.invoke(fluid.method_40178(),tags);
        }
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        // Skip only the world constructor: all entity constructors and base ticks run normally.
        ProbeWorld world=(ProbeWorld)unsafe.allocateInstance(ProbeWorld.class);
        world.fluid=class_3612.field_15906.method_15785();world.attributes=class_12205.method_76394().method_76410();world.time=100;
        class_1299<?> type=class_7923.field_41177.method_10223(class_2960.method_60655("minecraft","armor_stand")).orElseThrow().comp_349();
        var legacy=new OrdinaryEntity(type,world);var box=new class_238(0,0,0,64,64,64);legacy.method_5857(box);
        long before=System.nanoTime();
        check(!legacy.method_5692(class_3486.field_15517,.014),"empty real world has no water");
        check(!legacy.method_5692(class_3486.field_15518,.002),"empty real world has no lava");
        double legacyMillis=(System.nanoTime()-before)/1e6;
        check(world.reads==2*64*64*64,"actual vanilla water and lava refresh scanned 524288 cells for one large body");

        @SuppressWarnings({"rawtypes","unchecked"}) var carrier=new RoomProjectionEntity((class_1299)type,world);
        var ownerField=RoomProjectionEntity.class.getDeclaredField("OWNER");ownerField.setAccessible(true);
        @SuppressWarnings("unchecked") var ownerKey=(class_2940<String>)ownerField.get(null);
        carrier.method_5841().method_12778(ownerKey,UUID.randomUUID().toString());
        var snapshot=new RoomProjectionContraption();var volume=new ProjectionSourceSpec.Volume(0,0,0,63,63,63);
        snapshot.sourceSpec(new ProjectionSourceSpec(volume,null,volume,0,0,0));
        class_2680 water=class_3612.field_15910.method_15785().method_15759();
        class_2680 stone=class_7923.field_41175.method_63535(class_2960.method_60655("minecraft","stone")).method_9564();
        snapshot.applyBatch(List.of(entry(new class_2338(8,33,8),water),entry(new class_2338(8,34,8),water),entry(new class_2338(8,35,8),water),entry(new class_2338(9,33,8),stone)),true,0);
        carrier.setContraption(snapshot);carrier.stabilize();
        check(carrier.method_5829().equals(box),"full domain bounds preserved");
        world.reads=0;
        class_1297 dispatched=carrier;
        check(!dispatched.method_5692(class_3486.field_15517,.014)&&!dispatched.method_5692(class_3486.field_15518,.002),"vanilla virtual dispatch skips the carrier fluid body");
        check(world.reads==0,"carrier water and lava scan does not read world cells");
        check(ProjectionEnvironment.fluid(carrier).kind()==ProjectionEnvironment.FluidKind.NONE,"carrier cannot immerse itself in projected scenery");
        check(!ProjectionEnvironment.eyesIn(carrier,class_3486.field_15517)&&!ProjectionEnvironment.eyesIn(carrier,class_3486.field_15518),"carrier has no projected eye fluid");

        world.reads=0;long ticks=carrier.diagnosticContraptionTicks();before=System.nanoTime();
        for(int i=0;i<200;i++){world.time++;carrier.method_5773();}
        double fixedMillis=(System.nanoTime()-before)/1e6;
        check(carrier.diagnosticContraptionTicks()==ticks+200,"base tick still advances the contraption lifecycle once");
        check(world.reads<=400,"200 real carrier base ticks perform only point checks, not volume scans");
        check(carrier.method_5829().equals(box)&&carrier.collisionEnabled()&&carrier.phase()==ProjectionAnimation.STABLE,"tick preserves full bounds, phase and terrain collision participation");

        var ordinary=new OrdinaryEntity(type,world);ordinary.method_5814(8.5,33.1,8.5);
        ordinary.method_5857(new class_238(8.1,33.1,8.1,8.9,34.9,8.9));
        var projected=ProjectionEnvironment.fluid(ordinary);
        check(projected.kind()==ProjectionEnvironment.FluidKind.WATER&&projected.depth()>1.8,"ordinary bodies still contact projected water");
        check(ProjectionEnvironment.eyesIn(ordinary,class_3486.field_15517),"ordinary eye submersion in projected water preserved");
        var solids=ProjectionEnvironment.collisionShapes(world,new class_238(9.1,33.1,8.1,9.9,33.9,8.9));
        check(solids.size()==1&&solids.getFirst().method_1107().equals(new class_238(9,33,8,10,34,9)),"exact projected solid collision preserved");
        ordinary.method_5857(new class_238(1,1,1,2,2,2));
        world.fluid=class_3612.field_15910.method_15785();world.reads=0;
        check(ordinary.method_5692(class_3486.field_15517,.014)&&ordinary.method_5861(class_3486.field_15517)>.99,"ordinary vanilla water detection still updates depth");
        check(world.reads>0,"ordinary bodies still perform vanilla fluid queries");
        world.fluid=class_3612.field_15908.method_15785();
        check(ordinary.method_5692(class_3486.field_15518,.002)&&ordinary.method_5861(class_3486.field_15518)>.99,"ordinary vanilla lava detection preserved");
        world.reads=0;
        check(!dispatched.method_5692(class_3486.field_15518,.002)&&world.reads==0,"carrier stays independent even inside real lava");
        System.out.println("ProjectionCarrierFluidRegression passed: "+checks+" checks; vanilla two scans="+legacyMillis+" ms; 200 carrier ticks="+fixedMillis+" ms");
    }
}
