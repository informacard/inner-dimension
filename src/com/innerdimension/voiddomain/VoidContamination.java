package com.innerdimension.voiddomain;

import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.progression.*;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.domain.combat.DomainDamageTypes;
import net.minecraft.*;
import java.util.*;

/** Outside-in conversion; restoration replaces all void layers in the domain room. */
public final class VoidContamination {
    public static final String ABILITY="inner_dimension:void";
    public static final String DAMAGE="inner_dimension:domain_void";
    private static final class_238 ROOM=new class_238(0,32,0,16,48,16);
    private static final ThreadLocal<Boolean> TOTEM=ThreadLocal.withInitial(()->false);
    public static void noteTotem(){TOTEM.set(true);}
    private VoidContamination(){}
    public static boolean isDamage(class_1282 source){return DAMAGE.equals(DomainDamageTypes.keyOf(source));}
    public static boolean equipped(class_3222 player){var state=DomainState.get(player.method_51469());return state.loadout(player.method_5667(),state.selectedSlot(player.method_5667())).contains(ABILITY);}
    /** The engraving owns the state after the first trial; waves only describe its visual transition. */
    public static boolean active(class_3222 player){return InnerAdvancements.done(player,"fallen")&&equipped(player);}
    public static boolean inOwnRoom(class_3222 p){return p.method_5667().equals(DynamicDimensionManager.ownerOfWorld(p.method_51469().method_27983()))&&ROOM.method_1008(p.method_23317(),p.method_23318(),p.method_23321());}
    public static void dollFalling(class_1297 doll,UUID owner){
        if(owner==null||!(doll.method_73183() instanceof class_3218 world))return;
        // Vanilla destroys dropped items 64 blocks below the world's minimum height.
        if(doll.method_23318()+Math.min(0,doll.method_18798().field_1351)>world.method_31607()-64)return;
        var player=world.method_8503().method_3760().method_14602(owner);
        if(player!=null)ManualProgression.noteVoidTrial(player,world);
        if(player!=null&&player.method_5805()&&inOwnRoom(player)){
            var state=DomainState.get(player.method_51469());
            var progress=state.voidRooms.get(owner);
            // Recheck an already active room too: new or restored blocks may need conversion.
            if(active(player)&&progress!=null&&progress.startsWith("active:"))finish(player);
            else {
                if(progress!=null&&!progress.startsWith("pending:"))rollback(player);
                begin(player,true);
            }
        }
    }
    public static void register(){
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.SelectCrestSlotPayload.ID,com.innerdimension.network.SelectCrestSlotPayload.CODEC);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var player:server.method_3760().method_14571())tick(player);
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity,source)->{
            if(entity instanceof class_3222 p){
                var state=DomainState.get(p.method_51469());
                var progress=state.voidRooms.get(p.method_5667());
                if(isDamage(source)&&inOwnRoom(p)&&(active(p)||progress!=null&&progress.startsWith("pending:")))InnerAdvancements.grant(p,"suicide_king_ii");
                if(active(p)){finish(p);return;}
                if(state.voidRooms.containsKey(p.method_5667())){
                    rollback(p);
                }
            }
        });
    }
    public static void begin(class_3222 p,boolean initial){
        var state=DomainState.get(p.method_51469());UUID id=p.method_5667();
        if(!initial&&!active(p))return;
        if(state.voidRooms.containsKey(id))return;
        state.voidRooms.put(id,"pending:"+(initial?1:0)+":0");state.method_80();
        com.innerdimension.pregnancy.MentalPregnancySystem.abortOrdinaryForVoid(p);
    }
    private static void tick(class_3222 p){
        var state=DomainState.get(p.method_51469());UUID id=p.method_5667();
        var progress=state.voidRooms.get(id);
        boolean equipped=state.loadout(id,state.selectedSlot(id)).contains(ABILITY);
        if(equipped&&p.method_5805())com.innerdimension.pregnancy.MentalPregnancySystem.abortOrdinaryForVoid(p);
        if(progress==null){if(equipped&&p.method_5805())begin(p,false);return;}
        // Older versions left death-latched records. An equipped, unlocked crest restores them automatically.
        if(progress.startsWith("failed:")){if(active(p))finish(p);else rollback(p);return;}
        if(!p.method_5805()){if(!active(p))rollback(p);return;}
        if(progress.startsWith("active:")){
            if(!active(p)||!progress.equals("active:"+state.selectedSlot(id)))rollback(p);
            return;
        }
        String[] header=progress.split(":");boolean initial=header[1].equals("1");int elapsed=Integer.parseInt(header[2]);
        if(!initial&&!equipped){rollback(p);return;}
        var room=DynamicDimensionManager.getOrCreate(p.method_51469().method_8503(),id);
        com.innerdimension.chunk.TemporaryChunkLoads.holdChunk(room,0,0);
        if(elapsed%10==0&&elapsed<80){int ring=elapsed/10;
            com.innerdimension.sound.ModSounds.at(room,new class_2338(8,40,8),"void_infect",1f);
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)if(Math.min(Math.min(x,15-x),Math.min(z,15-z))==ring)
                for(int y=32;y<48;y++){
                    var pos=new class_2338(x,y,z);var original=room.method_8320(pos);var block=original.method_26204();
                    if(block!=ModBlocks.FLESH_WATER&&block!=ModBlocks.SOUL_MIST)continue;
                    
                    room.method_8652(pos,(block==ModBlocks.FLESH_WATER?ModBlocks.VOID_WATER:ModBlocks.VOID_MIST).method_9564(),3);
                }
        }
        if(elapsed==80){
            // Commit repeat transitions before damage: death must never replay the pulse after respawn.
            if(!initial){state.voidRooms.put(id,"active:"+state.selectedSlot(id));state.method_80();}
            // Apply the same lethal pulse once to every occupant, owner last. Totems use vanilla's resurrection path.
            var victims=new ArrayList<>(room.method_8390(class_1309.class,ROOM,e->e.method_5805()&&!e.method_7325()));victims.remove(p);if(inOwnRoom(p))victims.add(p);
            boolean ownerTotem=false;
            for(var victim:victims){boolean saved=hurt(victim,Float.MAX_VALUE);if(victim==p)ownerTotem=saved;}
            if(!p.method_5805())return;
            if(initial&&(!inOwnRoom(p)||!ownerTotem)){rollback(p);return;}
            if(initial){state.addSpecies(id,ABILITY);state.engrave(id,6,List.of(ABILITY));state.selectSlot(id,6);InnerAdvancements.grant(p,"fallen");}
            state.voidRooms.put(id,"active:"+state.selectedSlot(id));state.method_80();
            if(initial&&net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(p,com.innerdimension.network.SelectCrestSlotPayload.ID))net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new com.innerdimension.network.SelectCrestSlotPayload(6));
            return;
        }
        state.voidRooms.put(id,"pending:"+(initial?1:0)+":"+(elapsed+1));state.method_80();
    }
    public static boolean hurt(class_1309 victim,float amount){
        TOTEM.set(false);try{if(victim.method_73183() instanceof class_3218 world)victim.method_64397(world,DomainDamageTypes.source(world,DAMAGE,null,null),amount);return TOTEM.get();}finally{TOTEM.remove();}
    }
    /** Preserve contamination across death, finishing any half-converted room without another damage pulse. */
    private static void finish(class_3222 p){
        var state=DomainState.get(p.method_51469());UUID id=p.method_5667();
        state.voidRooms.put(id,"active:"+state.selectedSlot(id));state.method_80();
        var room=DynamicDimensionManager.getOrCreate(p.method_51469().method_8503(),id);
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=32;y<48;y++){
            var pos=new class_2338(x,y,z);var block=room.method_8320(pos).method_26204();
            if(block==ModBlocks.FLESH_WATER||block==ModBlocks.SOUL_MIST)room.method_8652(pos,(block==ModBlocks.FLESH_WATER?ModBlocks.VOID_WATER:ModBlocks.VOID_MIST).method_9564(),3);
        }
    }
    public static void rollback(class_3222 p){
        var state=DomainState.get(p.method_51469());var progress=state.voidRooms.remove(p.method_5667());if(progress==null)return;
        var room=DynamicDimensionManager.getOrCreate(p.method_51469().method_8503(),p.method_5667());
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=32;y<48;y++){
            var pos=new class_2338(x,y,z);var block=room.method_8320(pos).method_26204();
            if(block==ModBlocks.VOID_WATER||block==ModBlocks.VOID_MIST)room.method_8652(pos,(block==ModBlocks.VOID_WATER?ModBlocks.FLESH_WATER:ModBlocks.SOUL_MIST).method_9564(),3);
        }state.method_80();
    }
}
