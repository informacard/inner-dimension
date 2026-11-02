import com.innerdimension.projection.*;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;

/** Real server splitter -> packet codec -> atomic client reassembly, without a game. */
public final class ProjectionLargeSyncRegression {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    @SuppressWarnings("unchecked")
    public static void main(String[] args)throws Exception {
        class_155.method_36208();class_2966.method_12851();
        List<ProjectionUpdatePayload.Entry> entries=new ArrayList<>();
        for(int i=0;i<70000;i++)entries.add(new ProjectionUpdatePayload.Entry(new class_2338(i%128,(i/128)%128,i/16384),0,null));
        Method splitter=ProjectionMirror.class.getDeclaredMethod("fragments",List.class);splitter.setAccessible(true);
        var fragments=(List<List<ProjectionUpdatePayload.Entry>>)splitter.invoke(null,entries);
        check(fragments.size()==274,"Server splits 70000 cells beyond the old 128-part limit");
        ProjectionBatch<ProjectionUpdatePayload.Entry> batch=new ProjectionBatch<>(fragments.size());
        UUID instance=UUID.randomUUID();
        for(int part=fragments.size()-1;part>=0;part--){
            var p=new ProjectionUpdatePayload(7,instance,-1,0,true,part,fragments.size(),fragments.get(part));
            var b=new class_9129(Unpooled.buffer(),null);
            try{ProjectionUpdatePayload.CODEC.encode(b,p);check(b.readableBytes()<262144,"Individual packet remains small");var decoded=ProjectionUpdatePayload.CODEC.decode(b);check(decoded.equals(p),"Large batch wire data survives");batch.receive(decoded.part(),decoded.entries());}
            finally{b.release();}
            check(batch.complete()==(part==0),"Partial batches never commit");
        }
        check(batch.entries().equals(entries),"No missing or reordered cells after real wire round trip");
        for(int count:new int[]{ProjectionUpdatePayload.MAX_PARTS,ProjectionUpdatePayload.MAX_PARTS+1}){
            var p=new ProjectionUpdatePayload(7,instance,-1,0,true,count-1,count,List.of());
            var b=new class_9129(Unpooled.buffer(),null);
            try{ProjectionUpdatePayload.CODEC.encode(b,p);if(count==ProjectionUpdatePayload.MAX_PARTS)check(ProjectionUpdatePayload.CODEC.decode(b).equals(p),"Decoder accepts maximum shared bound");else{try{ProjectionUpdatePayload.CODEC.decode(b);throw new AssertionError("Decoder accepted excessive parts");}catch(IllegalArgumentException expected){}}}finally{b.release();}
        }
        System.out.println("ProjectionLargeSyncRegression passed: server splitter, 274 packets, 70000-cell wire reassembly and decoder boundary");
    }
}
