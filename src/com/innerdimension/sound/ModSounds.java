package com.innerdimension.sound;
import java.util.*;
import net.minecraft.*;
import net.fabricmc.fabric.api.networking.v1.*;
public final class ModSounds {
    private static final Map<String,class_3414> EVENTS=new LinkedHashMap<>();
    static {for(String key:new String[]{"information_ring","pillar_drone","pillar_leak_top","pillar_leak_bottom","void_rewind","recharge","void_infect","abyss_passage","void_ambience","fetus_pulse","enlightenment","domain_expand","domain_retract","domain_fracture","injection","mental_conception","lubricated_entry"}){
        var id=class_2960.method_60655("inner_dimension",key);EVENTS.put(key,class_2378.method_10230(class_7923.field_41172,id,class_3414.method_47908(id)));
    }}
    static {for(int radius=4;radius<=64;radius+=4){String key="domain_expand_open_"+radius;var id=class_2960.method_60655("inner_dimension",key);EVENTS.put(key,class_2378.method_10230(class_7923.field_41172,id,class_3414.method_47908(id)));}
        var id=class_2960.method_60655("inner_dimension","domain_clash");EVENTS.put("domain_clash",class_2378.method_10230(class_7923.field_41172,id,class_3414.method_47908(id)));}
    private ModSounds(){}
    public static void init(){}
    public static class_3414 event(String key){return EVENTS.get(key);}
    public static void personal(class_3222 p,String key,float volume){if(ServerPlayNetworking.canSend(p,com.innerdimension.network.SoundCuePayload.ID))ServerPlayNetworking.send(p,new com.innerdimension.network.SoundCuePayload(key,volume));}
    public static void at(class_1937 world,class_2338 pos,String key,float volume){if(world instanceof class_3218 server)server.method_8396(null,pos,event(key),class_3419.field_15254,volume,1);}
    public static final class_2498 WATER=new class_2498(.5f,1,class_3417.field_14998,class_3417.field_14998,class_3417.field_14998,class_3417.field_14998,class_3417.field_14998);
    private static final Map<UUID,String> AMBIENT=new HashMap<>();
    public static void register(){
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
            if(server.method_30002().method_75260()%20!=0)return;
            for(var player:server.method_3760().method_14571()){
                var world=player.method_51469();var owner=com.innerdimension.dimension.DynamicDimensionManager.ownerOfWorld(world.method_27983());
                var progress=owner==null?null:com.innerdimension.progression.DomainState.get(world).voidRooms.get(owner);
                boolean active=com.innerdimension.voiddomain.VoidEmbodimentRules.ambience(player.method_5805(),progress!=null&&(progress.startsWith("active:")||progress.startsWith("pending:")),com.innerdimension.voiddomain.VoidEmbodiment.active(player));
                String next=world.method_27983().method_29177()+":"+active;
                if(!next.equals(AMBIENT.put(player.method_5667(),next))||server.method_30002().method_75260()%100==0)personal(player,active?"void_ambience_on":"void_ambience_off",.09f);
            }
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->AMBIENT.remove(handler.field_14140.method_5667()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->AMBIENT.clear());
    }
}
