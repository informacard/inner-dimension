package com.innerdimension.client;
import com.innerdimension.network.UnlimitedVoidPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;
import net.minecraft.*;

/** Server-owned control, locally predicted input lock and reversible mixer attenuation. */
public final class UnlimitedVoidClient {
    private static int remaining,ringTicks;private static long started;
    private static boolean audible;
    private UnlimitedVoidClient(){}
    public static boolean active(){return remaining>0;}
    private static void accept(int ticks){
        boolean before=active();remaining=Math.max(0,Math.min(240,ticks));
        if(!before&&active()){started=System.nanoTime();ringTicks=0;}
        if(before!=active())refresh();
    }
    private static void refresh(){var c=class_310.method_1551();if(c!=null)c.method_1483().method_76569(class_3419.field_15250);audible=active();}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(UnlimitedVoidPayload.ID,(p,c)->c.client().execute(()->accept(p.ticks())));
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(()->accept(0)));
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(c.field_1724==null||!c.field_1724.method_5805()){accept(0);return;}
            if(!active()){if(audible)refresh();return;}
            if(ringTicks--<=0){InnerSoundClient.personal("information_ring",.35f);ringTicks=35;}
            var o=c.field_1690;
            for(var key:new class_304[]{o.field_1894,o.field_1881,o.field_1913,o.field_1849,o.field_1903,o.field_1832,o.field_1867})key.method_23481(false);
            c.field_1724.method_18799(class_243.field_1353);
            remaining--;if(!active())refresh();
        });
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,class_2960.method_60655("inner_dimension","information_overload"),(g,t)->{
            if(active())EnlightenmentOverlay.renderInformation(g,(System.nanoTime()-started)/1_000_000L);
        });
    }
}
