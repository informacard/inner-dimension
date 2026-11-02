import com.innerdimension.projection.*;
import com.innerdimension.progression.DomainState;
import com.innerdimension.item.OpenDomainCorePayload;
import com.innerdimension.network.SaveFloorPatternPayload;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
import com.mojang.serialization.JsonOps;
import java.util.*;

public final class FloorPatternIntegrationRegression {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static class_2680 block(String name){return FloorPatternSampler.resolve("minecraft:"+name,null);}
    public static void main(String[] args) {
        class_155.method_36208();class_2966.method_12851();
        class_2680 bg=block("black_concrete"),fg=block("white_concrete"),ornament=block("diamond_block");
        class_2680[][] tile=new class_2680[16][16];
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)tile[x][z]=(x%4==0)?fg:bg;
        tile[8][8]=ornament;
        FloorPatternSampler tiled=new FloorPatternSampler(tile,FloorPatternSettings.DEFAULT);
        check(tiled.at(24,24)==ornament,"default tiles centre motif");
        check(tiled.at(-8,-8)==ornament,"negative tiling coordinates");
        var keep=new FloorPatternSettings(FloorPatternSettings.Mode.RINGS,"",true,"","");
        var rings=new FloorPatternSampler(tile,keep);
        check(rings.foreground().method_26204()==fg.method_26204(),"auto foreground excludes centre ornament");
        check(rings.background().method_26204()==bg.method_26204(),"auto background from outer rows");
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)check(rings.at(x,z)==tile[x][z],"all centre states stay exact");
        for(int x=-24;x<40;x++)for(int z=-24;z<40;z++)if(!FloorPatternSettings.central(x,z))
            check(rings.at(x,z).method_26204()!=ornament.method_26204(),"centre never repeats outside");
        var full=new FloorPatternSettings(FloorPatternSettings.Mode.CUSTOM,"r % 4 < 1",false,"minecraft:gold_block","minecraft:stone");
        var drawn=new FloorPatternSampler(tile,full);
        check(drawn.at(8,8).method_26204()==block("gold_block").method_26204(),"whole-floor mode replaces centre");
        check(drawn.at(10,8).method_26204()==block("stone").method_26204(),"custom background");
        var invalid=new FloorPatternSettings(FloorPatternSettings.Mode.RINGS,"",true,"minecraft:does_not_exist","");
        try {FloorPatternSampler.validate(invalid);throw new AssertionError("accepted unknown material");}catch(IllegalArgumentException expected){checks++;}
        var owner=UUID.randomUUID();var other=UUID.randomUUID();var state=new DomainState();state.floorPattern(owner,full);
        var json=DomainState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var restored=DomainState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        check(restored.floorPattern(owner).equals(full),"settings survive save/load");
        check(restored.floorPattern(other).equals(FloorPatternSettings.DEFAULT),"players have independent defaults");
        var old=DomainState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow();
        check(old.floorPattern(owner).equals(FloorPatternSettings.DEFAULT),"old save migrates to tiling");
        var encoded=new class_9129(Unpooled.buffer(),null);
        try {
            SaveFloorPatternPayload.CODEC.encode(encoded,new SaveFloorPatternPayload(full));
            check(SaveFloorPatternPayload.CODEC.decode(encoded).settings().equals(full),"settings wire round trip");
        }finally{encoded.release();}
        List<Integer> samples=new ArrayList<>();for(int x=0;x<16;x++)for(int z=0;z<16;z++)samples.add(class_2248.method_9507(tile[x][z]));
        var core=new OpenDomainCorePayload(List.of(),"",0,0,List.of("a"),List.of(""),false,false,false,6,false,List.of(),false,true,samples,full);
        encoded=new class_9129(Unpooled.buffer(),null);
        try {
            OpenDomainCorePayload.CODEC.encode(encoded,core);
            var decoded=OpenDomainCorePayload.CODEC.decode(encoded);
            check(decoded.equals(core),"unlock, actual floor and settings reach UI unchanged");
            check(decoded.withPreviewState(-3).openUnlocked(),"refresh preserves unlock");
        }finally{encoded.release();}
        System.out.println("FloorPatternIntegrationRegression passed: "+checks+" checks");
    }
}
