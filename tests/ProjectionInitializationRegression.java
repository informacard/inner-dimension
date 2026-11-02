import com.innerdimension.client.*;
import com.innerdimension.projection.*;
import com.innerdimension.projection.builtin.*;
import com.innerdimension.projection.builtin.world.VirtualRenderWorld;
import net.minecraft.*;
import java.lang.reflect.*;
import java.util.*;
import jdk.jfr.FlightRecorder;

/** Real renderer update entry point, full-snapshot state and ordinary-play profiler gate. */
public final class ProjectionInitializationRegression {
    static int checks;static sun.misc.Unsafe unsafe;
    static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    static void field(Object target,Class<?> owner,String name,Object value)throws Exception{var f=owner.getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    static final class World extends VirtualRenderWorld {
        Map<class_2338,class_2680> states;int writes,lightRuns;class_12205 attributes;
        World(){super(null,0,64,null,()->{});}
        @Override public class_2680 method_8320(class_2338 pos){return states.getOrDefault(pos,class_2246.field_10124.method_9564());}
        @Override public boolean method_30092(class_2338 pos,class_2680 state,int flags,int recursion){writes++;states.put(pos,state);return true;}
        @Override public void runLightEngine(){lightRuns++;}
        @Override public class_12205 method_75728(){return attributes;}
        @Override public long method_75260(){return 100;}
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    static RoomProjectionEntity entity(World world,UUID owner,boolean shell,ProjectionSourceSpec spec)throws Exception{
        var type=class_7923.field_41177.method_10223(class_2960.method_60655("minecraft","armor_stand")).orElseThrow().comp_349();
        var entity=new RoomProjectionEntity((class_1299)type,world);
        var ownerField=RoomProjectionEntity.class.getDeclaredField("OWNER");ownerField.setAccessible(true);
        entity.method_5841().method_12778((class_2940<String>)ownerField.get(null),owner.toString());
        var shellField=RoomProjectionEntity.class.getDeclaredField("SHELL");shellField.setAccessible(true);
        entity.method_5841().method_12778((class_2940<Boolean>)shellField.get(null),shell);
        var snapshot=new RoomProjectionContraption();snapshot.sourceSpec(spec);entity.setContraption(snapshot);return entity;
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    public static void main(String[] args)throws Exception{
        System.clearProperty("inner_dimension.performance.record");
        boolean recordingWasInitialized=FlightRecorder.isInitialized();
        OpenDomainPerformance.startIfArmed(UUID.randomUUID());
        check(OpenDomainPerformance.begin()==0&&OpenDomainPerformance.span(OpenDomainPerformance.Metric.CLIENT_FRAME)==null,"ordinary play never arms recording");
        check(FlightRecorder.isInitialized()==recordingWasInitialized,"ordinary-play gate does not initialize JFR");
        class_155.method_36208();class_2966.method_12851();
        var uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=(sun.misc.Unsafe)uf.get(null);

        var committedField=ProjectionSyncClient.class.getDeclaredField("committedFull");committedField.setAccessible(true);
        Map<RoomProjectionContraption,Boolean> committed=(Map)committedField.get(null);
        var snapshot=new RoomProjectionContraption();var replacement=new RoomProjectionContraption();
        var pos=new class_2338(4,33,4);var stone=class_7923.field_41175.method_63535(class_2960.method_60655("minecraft","stone")).method_9564();
        var entry=new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(stone),null);
        check(ProjectionBatch.decide(0,-1,0,true,committed.containsKey(snapshot))==ProjectionBatch.Decision.APPLY,"first version-zero complete snapshot must apply");
        snapshot.applyBatch(List.of(entry),true,0);committed.put(snapshot,true);long geometry=snapshot.geometryVersion();
        for(int i=0;i<8;i++){
            var decision=ProjectionBatch.decide(snapshot.version(),-1,0,true,committed.containsKey(snapshot));
            if(decision==ProjectionBatch.Decision.APPLY)snapshot.applyBatch(List.of(entry),true,0);
            check(decision==ProjectionBatch.Decision.STALE,"redundant initial full snapshot ignored");
        }
        check(snapshot.geometryVersion()==geometry&&snapshot.getBlocks().size()==1,"duplicates do not invalidate geometry or clear the snapshot");
        check(ProjectionBatch.decide(0,-1,0,true,committed.containsKey(replacement))==ProjectionBatch.Decision.APPLY,"new snapshot after entity re-tracking must initialize again");
        check(ProjectionBatch.decide(0,-1,0,true,false)==ProjectionBatch.Decision.APPLY,"explicit same-version repair still applies");
        check(ProjectionBatch.decide(0,-1,1,true,true)==ProjectionBatch.Decision.APPLY,"newer full snapshot still applies");
        check(ProjectionBatch.decide(1,-1,0,true,true)==ProjectionBatch.Decision.STALE,"old full snapshot cannot roll back");
        check(ProjectionBatch.decide(0,0,1,false,true)==ProjectionBatch.Decision.APPLY&&ProjectionBatch.decide(0,1,2,false,true)==ProjectionBatch.Decision.RESYNC,"delta continuity remains enforced");
        committed.clear();

        World world=(World)unsafe.allocateInstance(World.class);world.states=new HashMap<>();world.attributes=class_12205.method_76394().method_76410();
        var volume=new ProjectionSourceSpec.Volume(0,0,0,3,3,3);
        var open=new ProjectionSourceSpec(volume,null,volume,0,0,0);var closed=new ProjectionSourceSpec(volume,volume,volume,0,0,0);
        UUID owner=UUID.randomUUID();var shell=entity(world,owner,true,open);var interior=entity(world,owner,false,open);
        var ringType=Class.forName("com.innerdimension.client.RoomProjectionRenderer$RingClient");var ring=unsafe.allocateInstance(ringType);
        field(ring,ringType,"entity",shell);field(ring,ClientContraption.class,"contraption",shell.getContraption());field(ring,ClientContraption.class,"renderLevel",world);
        field(ring,ringType,"sections",new LinkedHashMap<>());field(ring,ringType,"fluids",new HashMap<>());field(ring,ringType,"keys",new ArrayList<>());
        var cachesField=RoomProjectionRenderer.class.getDeclaredField("CACHES");cachesField.setAccessible(true);Map caches=(Map)cachesField.get(null);caches.put(shell,ring);
        var localEntry=new ProjectionUpdatePayload.Entry(new class_2338(1,1,1),class_2248.method_9507(stone),null);
        try{
            RoomProjectionRenderer.applyUpdates(interior,List.of(localEntry),true);
            RoomProjectionRenderer.applyUpdates(interior,List.of(localEntry),false);
            check(world.writes==0&&world.lightRuns==0,"open-domain updates never mirror terrain into the empty shell or compute its light");
            var neighbours=ringType.getDeclaredMethod("neighbours");neighbours.setAccessible(true);neighbours.invoke(ring);
            check(world.writes==0&&world.lightRuns==0,"open bookkeeping shell skips initial sibling-light construction");
            ((RoomProjectionContraption)shell.getContraption()).sourceSpec(closed);((RoomProjectionContraption)interior.getContraption()).sourceSpec(closed);
            RoomProjectionRenderer.applyUpdates(interior,List.of(localEntry),true);
            check(world.writes>0&&world.lightRuns==1&&world.states.get(new class_2338(1,1,1))==stone,"closed-room cross-boundary block/light update preserved");
        }finally{caches.remove(shell);}
        System.out.println("ProjectionInitializationRegression passed: "+checks+" checks");
    }
}
