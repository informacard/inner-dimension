package com.innerdimension.client;
import com.innerdimension.network.RitualControlPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.*;
/** Release held and queued keys at both edges so input cannot leak into or out of a film. */
public final class RitualInput {
    private static boolean locked;
    private RitualInput(){}
    public static boolean active(){var p=class_310.method_1551().field_1724;return locked&&p!=null&&p.method_5805();}
    private static void accept(boolean next){locked=next;class_304.method_1437();}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(RitualControlPayload.ID,(p,c)->c.client().execute(()->{accept(p.locked());if(p.locked())c.client().method_1507(null);}));
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(()->accept(false)));
        ClientTickEvents.START_CLIENT_TICK.register(c->{if(active())class_304.method_1437();});
    }
}
