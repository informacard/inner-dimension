import com.innerdimension.network.BossDomainPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.class_9129;
import java.util.*;
public final class BossWireRegression {
    public static void main(String[] args){
        for(var payload:List.of(new BossDomainPayload(12,List.of()),new BossDomainPayload(999,List.of(new BossDomainPayload.View(UUID.randomUUID(),3,0,false,600,2400,540,99,0,true),new BossDomainPayload.View(UUID.randomUUID(),8,2,true,-1,0,35,0,160))))){
            var buffer=new class_9129(Unpooled.buffer(),null);
            try{BossDomainPayload.CODEC.encode(buffer,payload);if(!BossDomainPayload.CODEC.decode(buffer).equals(payload))throw new AssertionError("Boss payload roundtrip");}finally{buffer.release();}
        }
        var buffer=new class_9129(Unpooled.buffer(),null);boolean rejected=false;
        try{buffer.writeLong(1);buffer.writeInt(9);BossDomainPayload.CODEC.decode(buffer);}catch(IllegalArgumentException expected){rejected=true;}finally{buffer.release();}
        if(!rejected)throw new AssertionError("Unbounded packet accepted");
        System.out.println("PASS BossWireRegression: empty/active/multiple casts and bounded packet codec");
    }
}
