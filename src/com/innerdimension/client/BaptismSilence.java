package com.innerdimension.client;
import net.minecraft.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
/** Transient gain gate: user sound settings and existing playback positions stay intact. */
public final class BaptismSilence {
 private static volatile boolean silenced;
 private static long until;
 private BaptismSilence(){}
 public static boolean active(){return silenced;}
 public static void begin(long millis){until=System.nanoTime()+millis*1_000_000L;silenced=true;refresh();}
 public static void end(){if(!silenced)return;silenced=false;until=0;refresh();}
 private static void refresh(){class_310.method_1551().method_1483().method_76569(class_3419.field_15250);}
 public static void register(){
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(silenced&&System.nanoTime()>=until)end();});
  ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->client.execute(BaptismSilence::end));
 }
}
