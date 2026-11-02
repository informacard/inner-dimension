package smoke;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.*;
import java.util.*;
public final class UnlimitedVoidSmoke implements ClientModInitializer {
    public void onInitializeClient(){try{
        Class.forName("UnlimitedVoidRegression").getMethod("run").invoke(null);
        for(var type:List.of(class_3244.class,class_1308.class,class_1297.class,class_310.class,class_1140.class)){
            if(Arrays.stream(type.getDeclaredMethods()).noneMatch(m->m.getName().contains("information")))throw new AssertionError("Control mixin absent: "+type);
        }
        var uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);var unsafe=(sun.misc.Unsafe)uf.get(null);
        var mixer=(class_1140)unsafe.allocateInstance(class_1140.class);
        var gain=class_1140.class.getDeclaredMethod("method_43222",float.class,class_3419.class);gain.setAccessible(true);
        var duration=com.innerdimension.client.UnlimitedVoidClient.class.getDeclaredField("remaining");duration.setAccessible(true);
        var silence=com.innerdimension.client.BaptismSilence.class.getDeclaredField("silenced");silence.setAccessible(true);
        try{
            duration.setInt(null,100);silence.setBoolean(null,true);
            for(var category:class_3419.values())if((float)gain.invoke(mixer,.8f,category)!=0)throw new AssertionError("Baptism priority");
            silence.setBoolean(null,false);
            var attenuation=Arrays.stream(class_1140.class.getDeclaredMethods()).filter(m->m.getName().contains("informationGain")).findFirst().orElseThrow();attenuation.setAccessible(true);
            for(var category:class_3419.values()){
                var callback=new org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Float>("gain",true,.8f);
                attenuation.invoke(mixer,.8f,category,callback);
                if(Math.abs(callback.getReturnValue()-.144f)>.0001f)throw new AssertionError("All category attenuation: "+category);
            }
            duration.setInt(null,0);
            var callback=new org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Float>("gain",true,.8f);
            attenuation.invoke(mixer,.8f,class_3419.field_15250,callback);
            if(callback.getReturnValue()!=.8f)throw new AssertionError("Audio restored");
        }finally{duration.setInt(null,0);silence.setBoolean(null,false);}
        for(int ticks:new int[]{0,1,120,240}){
            var p=new com.innerdimension.network.UnlimitedVoidPayload(ticks);var b=new class_9129(io.netty.buffer.Unpooled.buffer(),null);
            try{com.innerdimension.network.UnlimitedVoidPayload.CODEC.encode(b,p);if(!p.equals(com.innerdimension.network.UnlimitedVoidPayload.CODEC.decode(b)))throw new AssertionError("Protocol");}finally{b.release();}
        }
        var path=net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("inner_dimension").orElseThrow().findPath("assets/inner_dimension/sounds/information_ring.ogg").orElseThrow();
        if(java.nio.file.Files.size(path)<1000)throw new AssertionError("Ear ringing resource missing");
        System.out.println("UNLIMITED_VOID_SMOKE_PASS: transformed packet/AI/movement/input/mixer classes, baptism priority, protocol and audio asset");System.exit(0);
    }catch(Throwable e){e.printStackTrace();System.out.println("UNLIMITED_VOID_SMOKE_FAIL");System.exit(1);}}
}
