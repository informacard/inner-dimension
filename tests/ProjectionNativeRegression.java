import com.innerdimension.projection.*;
import net.minecraft.*;
import java.util.*;
import io.netty.buffer.Unpooled;
public final class ProjectionNativeRegression {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    static ProjectionUpdatePayload.Entry entry(class_2338 pos,class_2680 state,class_2487 data) {
        return new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(state),data);
    }
    public static void main(String[] args) {
        class_155.method_36208(); class_2966.method_12851();
        check(class_5636.field_27888.name().equals("NONE"),"Camera NONE mapping");
        check(class_5636.field_27886.name().equals("WATER"),"Camera WATER mapping");
        check(class_5636.field_27885.name().equals("LAVA"),"Camera LAVA mapping");
        class_2338 pos=new class_2338(4,32,4);
        class_2680 water=class_7923.field_41175.method_63535(class_2960.method_60655("minecraft","water")).method_9564();
        class_2680 air=class_2246.field_10124.method_9564();
        class_2487 data=new class_2487(); data.method_10582("text","roundtrip");
        var payload=new ProjectionUpdatePayload(7,UUID.randomUUID(),3,4,false,0,1,List.of(entry(pos,water,data),entry(pos.method_10084(),air,null)));
        var buf=new class_9129(Unpooled.buffer(),null);
        try {
            ProjectionUpdatePayload.CODEC.encode(buf,payload);
            var decoded=ProjectionUpdatePayload.CODEC.decode(buf);
            check(payload.equals(decoded),"Real network codec including nullable NBT");
        } finally { buf.release(); }
        var snapshot=new RoomProjectionContraption();
        check(snapshot.validInterior(pos),"Interior accepted");
        check(!snapshot.validInterior(new class_2338(-1,32,4)),"Static shell excluded");
        check(!snapshot.validInterior(new class_2338(4,31,4)),"Floor excluded");
        snapshot.applyBatch(List.of(entry(pos,water,null),entry(pos.method_10084(),water,null)),false,1);
        var view=snapshot.snapshotView();
        check(!view.method_8316(pos).method_15769(),"Snapshot fluid query must not read real chunks");
        check(view.method_8316(pos).method_15763(view,pos)==1f,"Stacked source water has full height");
        snapshot.applyBatch(List.of(entry(pos.method_10084(),air,null)),false,2);
        float height=view.method_8316(pos).method_15763(view,pos);
        check(height>0.88f && height<0.90f,"Water surface lowers after deleting upper cell");
        snapshot.applyBatch(List.of(entry(pos,air,null)),false,3);
        check(view.method_8316(pos).method_15769(),"Deleted water disappears from physics");
        check(snapshot.getBlocks().isEmpty(),"Explicit deletions remove ring members");
        class_2680 sign=class_7923.field_41175.method_63535(class_2960.method_60655("minecraft","oak_sign")).method_9564();
        snapshot.applyBatch(List.of(entry(pos,sign,data)),false,4);
        long geometry=snapshot.geometryVersion();
        class_2487 changedText=data.method_10553(); changedText.method_10582("text","updated");
        snapshot.applyBatch(List.of(entry(pos,sign,changedText)),false,5);
        check(snapshot.geometryVersion()==geometry,"Display-only updates do not invalidate navigation");
        check(snapshot.version()==5,"Display-only transactions still advance transport version");
        snapshot.applyBatch(List.of(entry(pos,water,null)),false,6);
        check(snapshot.getBlocks().get(pos).comp_1343()==null,"Replacement clears old BE data");
        snapshot.applyBatch(List.of(),true,6);
        check(snapshot.getBlocks().isEmpty(),"Empty full resync removes stale client blocks");
        System.out.println("ProjectionNativeRegression: 16 checks passed");
    }
}
