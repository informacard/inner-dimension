package com.innerdimension.voiddomain;
import net.minecraft.*;
import java.util.*;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.network.EntryTransitionPayload;
import com.innerdimension.progression.InnerAdvancements;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
/** One bounded ritual per player; cancellation always releases the camera mask and gravity. */
public final class VoidRitual {
    private record Ritual(class_3218 world,int age,boolean gravity,class_2338 candle,int playTicks){}
    private static final Map<UUID,Ritual> RUNNING=new HashMap<>();
    private static final Map<UUID,Integer> VISUALS=new HashMap<>();
    public static void selectVisual(class_3222 player,int variant){if(variant>=1&&variant<=3)VISUALS.put(player.method_5667(),variant);}
    private VoidRitual(){}
    public static boolean running(class_3222 p){return RUNNING.containsKey(p.method_5667());}
    public static boolean start(class_3222 p,class_2338 shell,class_2350 face){
        if(!p.method_5805()||!VoidContamination.inOwnRoom(p)||!VoidContamination.active(p)||running(p)||InnerAdvancements.done(p,"enlightenment"))return false;
        if(p.method_51469().method_8320(shell).method_26204()!=ModBlocks.ABYSS_BLOCK)return false;
        int x=shell.method_10263(),y=shell.method_10264(),z=shell.method_10260();
        if(x< -1||x>16||z< -1||z>16||y<31||y>48||!(x==-1||x==16||z==-1||z==16||y==31||y==48))return false;
        var candle=shell.method_10093(face);var world=p.method_51469();
        if(!world.method_8320(candle).method_26215())return false;
        // The lit tip points toward the clicked abyss face.
        var rod=ModBlocks.INFECTOR;
        var facing=(net.minecraft.class_2754)rod.method_9595().method_11663("facing");
        var rodState=rod.method_9564().method_11657(facing,face.method_10153());
        if(!world.method_8652(candle,rodState,3))return false;
        int visual=3;
        if(ServerPlayNetworking.canSend(p,com.innerdimension.network.EnlightenmentVisualPayload.ID))ServerPlayNetworking.send(p,new com.innerdimension.network.EnlightenmentVisualPayload(visual,false));
        RUNNING.put(p.method_5667(),new Ritual(world,0,p.method_5740(),candle,RitualVisualSchedule.playTicks(visual)));
        send(p,true,false,3);return true;
    }
    private static void send(class_3222 p,boolean closed,boolean complete,int impact){if(ServerPlayNetworking.canSend(p,EntryTransitionPayload.ID))ServerPlayNetworking.send(p,new EntryTransitionPayload(closed,complete,impact));}
    public static void register(){
        RitualControl.register();
        ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.EnlightenmentVisualPayload.ID,(payload,context)->{
            if(!payload.preview())context.server().execute(()->selectVisual(context.player(),payload.variant()));
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{VISUALS.remove(handler.field_14140.method_5667());var r=RUNNING.remove(handler.field_14140.method_5667());if(r!=null)handler.field_14140.method_5875(r.gravity);});
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var id:List.copyOf(RUNNING.keySet())){
                var r=RUNNING.get(id);var p=server.method_3760().method_14602(id);
                if(p==null||!p.method_5805()||p.method_51469()!=r.world){if(p!=null){p.method_5875(r.gravity);send(p,true,true,0);}RUNNING.remove(id);continue;}
                int age=r.age;int closingAge=6+r.playTicks,leavingAge=closingAge+6;
                if(age<=6){
                if(r.world.method_8320(r.candle).method_26204()!=ModBlocks.INFECTOR){p.method_5875(r.gravity);send(p,true,true,0);RUNNING.remove(id);continue;}
                var facing=(net.minecraft.class_2754)ModBlocks.INFECTOR.method_9595().method_11663("facing");var tip=(class_2350)r.world.method_8320(r.candle).method_11654(facing);
                r.world.method_14199(class_2398.field_11214,false,false,r.candle.method_10263()+.5+.45*tip.method_10148(),r.candle.method_10264()+.5+.45*tip.method_10164(),r.candle.method_10260()+.5+.45*tip.method_10165(),12,.18,.18,.18,.03);
                }
                if(age==6){
                    if(!com.innerdimension.transport.SafeEntityTeleport.move(p,r.world,8,49,8,Set.of(),p.method_36454(),-90,true)){p.method_5875(r.gravity);send(p,true,true,0);RUNNING.remove(id);continue;}
                    // Consume only after teleport succeeds; later ritual ticks no longer require it.
                    r.world.method_8652(r.candle,class_2246.field_10124.method_9564(),3);
                    p.method_5875(true);p.method_18800(0,0,0);send(p,false,true,3);
                }
                if(age>=6&&age<closingAge){p.method_5814(8,49,8);p.method_18800(0,0,0);p.method_36457(-90);}
                if(age==closingAge){InnerAdvancements.grant(p,"enlightenment");send(p,true,false,0);}
                if(age>=leavingAge){
                    if(com.innerdimension.dimension.DynamicDimensionManager.leave(p)){
                        VoidContamination.rollback(p);
                        com.innerdimension.progression.DomainState.get(p.method_51469()).selectSlot(id,0);
                        if(ServerPlayNetworking.canSend(p,com.innerdimension.network.SelectCrestSlotPayload.ID))ServerPlayNetworking.send(p,new com.innerdimension.network.SelectCrestSlotPayload(0));
                        p.method_5875(r.gravity);RUNNING.remove(id);continue;
                    }
                    if(age>=leavingAge+600){p.method_5875(r.gravity);send(p,true,true,0);RUNNING.remove(id);continue;}
                }
                RUNNING.put(id,new Ritual(r.world,age+1,r.gravity,r.candle,r.playTicks));
            }
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->{for(var e:RUNNING.entrySet()){var p=server.method_3760().method_14602(e.getKey());if(p!=null)p.method_5875(e.getValue().gravity);}RUNNING.clear();VISUALS.clear();});
    }
}
