import com.innerdimension.projection.ProjectionBatch;
import java.util.*;
public final class ProjectionBatchRegression {
    static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
    static void rejects(Runnable action,Class<? extends Throwable> type) {
        try { action.run(); } catch(Throwable error) { if(type.isInstance(error)) return; throw error; }
        throw new AssertionError("Expected "+type.getName());
    }
    public static void main(String[] args) {
        var batch=new ProjectionBatch<String>(3);
        batch.receive(2,List.of("delete")); batch.receive(0,List.of("door"));
        check(!batch.complete(),"No partial commit");
        rejects(batch::entries,IllegalStateException.class);
        batch.receive(0,List.of("door")); check(!batch.complete(),"Duplicate cannot advance receipt count");
        rejects(() -> batch.receive(0,List.of("old-door")),IllegalArgumentException.class);
        batch.receive(1,List.of("water","sign"));
        check(batch.entries().equals(List.of("door","water","sign","delete")),"Arrival order must not reorder transaction");
        rejects(() -> new ProjectionBatch<>(ProjectionBatch.MAX_PARTS+1),IllegalArgumentException.class);
        rejects(() -> batch.receive(3,List.of()),IllegalArgumentException.class);
        check(ProjectionBatch.decide(5,5,6,false)==ProjectionBatch.Decision.APPLY,"Next version applies");
        check(ProjectionBatch.decide(5,4,5,false)==ProjectionBatch.Decision.STALE,"Duplicate version ignored");
        check(ProjectionBatch.decide(5,6,7,false)==ProjectionBatch.Decision.RESYNC,"Missing version resyncs");
        check(ProjectionBatch.decide(5,-1,7,true)==ProjectionBatch.Decision.APPLY,"Full repair advances version");
        check(ProjectionBatch.decide(5,-1,5,true)==ProjectionBatch.Decision.APPLY,"Same-version complete snapshot repairs cache");
        check(ProjectionBatch.decide(5,-1,4,true)==ProjectionBatch.Decision.STALE,"Old full snapshot cannot roll back");
        check(ProjectionBatch.decide(5,5,8,false)==ProjectionBatch.Decision.RESYNC,"Malformed version interval rejected");
        check(ProjectionBatch.decide(5,-1,-1,true)==ProjectionBatch.Decision.RESYNC,"Negative version rejected");
        var large=new ProjectionBatch<Integer>(129);
        List<Integer> full=Collections.nCopies(ProjectionBatch.FRAGMENT_SIZE,42);
        for(int part=128;part>=1;part--)check(large.receiveNew(part,full),"Reverse-order new fragments advance progress");
        check(!large.complete(),"Large snapshot is invisible until complete");
        check(!large.receiveNew(128,full),"Duplicate fragment cannot refresh timeout");
        check(large.receiveNew(0,full),"Last fragment completes snapshot");
        check(large.entries().size()==33024,"Snapshot above old 32768 limit reassembles completely");
        var maximum=new ProjectionBatch<Integer>(ProjectionBatch.MAX_PARTS);
        for(int part=0;part<ProjectionBatch.MAX_PARTS;part++)maximum.receive(part,full);
        check(maximum.complete()&&maximum.entries().size()==128*128*128,"Largest legal radius bounding cube fits");
        rejects(() -> batch.receive(0,Collections.nCopies(ProjectionBatch.FRAGMENT_SIZE+1,"x")),IllegalArgumentException.class);
        System.out.println("ProjectionBatchRegression passed: atomicity, ordering, duplicates, idle progress, 33024 entries and 2097152-entry endpoint");
    }
}
