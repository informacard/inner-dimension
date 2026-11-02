package com.innerdimension.voiddomain;
import com.innerdimension.network.RitualControlPayload;
import java.util.*;
import net.minecraft.*;
import net.fabricmc.fabric.api.networking.v1.*;
/** Server-owned input lock for automatic enlightenment and baptism films. */
public final class RitualControl {
    private static final Map<UUID,Boolean> SENT=new HashMap<>();
    private RitualControl(){}
    public static boolean locked(class_1297 entity){return entity instanceof class_3222 p&&p.method_5805()&&(VoidRitual.running(p)||VoidEmbodimentState.get(p).players.getOrDefault(p.method_5667(),0)==4);}
    public static void register(){
        PayloadTypeRegistry.playS2C().register(RitualControlPayload.ID,RitualControlPayload.CODEC);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var p:server.method_3760().method_14571()){
                boolean locked=locked(p);var previous=SENT.get(p.method_5667());
                if((previous==null||previous!=locked)&&ServerPlayNetworking.canSend(p,RitualControlPayload.ID)){ServerPlayNetworking.send(p,new RitualControlPayload(locked));SENT.put(p.method_5667(),locked);}
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((h,s)->SENT.remove(h.field_14140.method_5667()));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s->SENT.clear());
    }
}
