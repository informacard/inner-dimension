import com.innerdimension.domain.DomainAbilityCatalog;
import com.innerdimension.item.OpenDomainCorePayload;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.util.*;

public final class DomainUnknownSpeciesRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static void persistent(String entity)throws Exception{
        ClassNode node=new ClassNode();
        try(var in=DomainUnknownSpeciesRegression.class.getResourceAsStream("/com/innerdimension/entity/"+entity+".class")){
            new ClassReader(in).accept(node,0);
        }
        for(String name:List.of("<init>","method_5749")){
            MethodNode method=node.methods.stream().filter(m->m.name.equals(name)).findFirst().orElseThrow();
            boolean restored=name.equals("<init>"),persistent=false;
            for(var instruction:method.instructions){
                if(instruction instanceof MethodInsnNode call){
                    if(call.name.equals("method_5749")&&call.getOpcode()==Opcodes.INVOKESPECIAL)restored=true;
                    if(call.name.equals("method_5971")){check(restored,"Persistence must follow superclass restore");persistent=true;}
                }
            }
            check(persistent,entity+" "+name+" persistent");
        }
    }
    public static void main(String[] args)throws Exception{
        class_155.method_36208();class_2966.method_12851();
        for(String id:List.of("minecraft:squid","example:custom_creature")){
            var unknown=DomainAbilityCatalog.forRecordedEntity(id);
            check(unknown.entityId().equals(id),"Retains recorded identity");
            check(unknown.color()==-1&&unknown.colorWeight()==0&&unknown.loadWeight()==0,"Default template has no color or load");
            check(DomainAbilityCatalog.byEntity(id)==null,"No executable ability for unsupported entity");
            var ids=DomainAbilityCatalog.sanitize(List.of(id,id,"minecraft:cat"));
            check(ids.equals(List.of(id,"minecraft:cat")),"Unsupported species survives save normalization, duplicates removed");
            check(DomainAbilityCatalog.weight(ids)==4,"Unsupported species contributes zero load");
            check(Arrays.equals(DomainAbilityCatalog.nameColors(ids),DomainAbilityCatalog.nameColors(List.of("minecraft:cat"))),"Unsupported species cannot change crest colors");
            check(DomainAbilityCatalog.colorOrder(List.of(id)).isEmpty(),"No primary color");
        }
        check(DomainAbilityCatalog.sanitize(List.of("bad id","missing_namespace")).isEmpty(),"Invalid IDs rejected");
        try{DomainAbilityCatalog.sanitize(List.of("minecraft:copper_golem","minecraft:vindicator","example:custom_creature"));throw new AssertionError("Conflict lost");}catch(IllegalArgumentException expected){}
        for(boolean unlocked:List.of(false,true)){
            var payload=new OpenDomainCorePayload(List.of("minecraft:squid"),"minecraft:squid",2,0,List.of("a","b","c"),List.of("minecraft:squid","",""),true,true,unlocked);
            var buffer=new class_9129(Unpooled.buffer(),null);
            try{
                OpenDomainCorePayload.CODEC.encode(buffer,payload);
                check(OpenDomainCorePayload.CODEC.decode(buffer).equals(payload),"Unlock flag and unknown species round trip");
                check(payload.withPreviewState(-2).mentalVolumeUnlocked()==unlocked,"Preview refresh retains unlock flag");
            }finally{buffer.release();}
        }
        persistent("SensoryLinkDollEntity");persistent("EnderPearlDildoEntity");
        System.out.println("PASS: unknown species, weights, crest colors, save normalization, payload round trips, persistence hooks");
    }
}
