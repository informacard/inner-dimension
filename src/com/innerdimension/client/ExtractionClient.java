package com.innerdimension.client;
import net.minecraft.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import com.innerdimension.network.ExtractionPayload;
import com.innerdimension.item.ExtractionTiming;
public final class ExtractionClient {
 private static boolean active;
 private static int ticks;
 private static class_243 anchor;
 public static boolean active(){return active;}
 public static float scale(float delta){return active?ExtractionTiming.fovScale(Math.max(0,ticks-1)+delta):1;}
 public static void register(){
  ClientPlayNetworking.registerGlobalReceiver(ExtractionPayload.ID,(p,c)->c.client().execute(()->{active=p.active();ticks=0;anchor=active?new class_243(p.x(),p.y(),p.z()):null;if(active&&c.client().field_1724!=null){c.client().field_1724.method_5814(p.x(),p.y(),p.z());c.client().field_1724.method_18799(class_243.field_1353);}}));
  ClientTickEvents.END_CLIENT_TICK.register(c->{if(c.field_1724==null||!c.field_1724.method_5805()){active=false;ticks=0;anchor=null;}else if(active){ticks=Math.min(20,ticks+1);c.field_1724.method_18799(class_243.field_1353);if(anchor!=null)c.field_1724.method_5814(anchor.field_1352,anchor.field_1351,anchor.field_1350);}});
  ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(()->{active=false;ticks=0;}));
 }
}
