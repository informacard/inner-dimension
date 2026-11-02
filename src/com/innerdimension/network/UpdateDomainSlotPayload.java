package com.innerdimension.network;
import com.innerdimension.item.OpenDomainCorePayload;
import com.innerdimension.progression.DomainState;
import java.util.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.*;

/** Separate operations ensure saving a crest name can never accidentally engrave draft choices. */
public record UpdateDomainSlotPayload(int operation,int slot,String name,List<String> loadout) implements class_8710 {
    public static final int SELECT=0,SAVE_NAME=1,ENGRAVE=2,SAVE_OBJECTS=3;
    public static final class_8710.class_9154<UpdateDomainSlotPayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","update_domain_slot"));
    public static final class_9139<class_9129,UpdateDomainSlotPayload> CODEC=class_9139.method_56437((buf,p)->{buf.writeInt(p.operation);buf.writeInt(p.slot);buf.method_10814(p.name);buf.writeInt(p.loadout.size());for(String id:p.loadout)buf.method_10814(id);},buf->{int op=buf.readInt(),slot=buf.readInt();String name=buf.method_19772();int n=Math.max(0,Math.min(4096,buf.readInt()));List<String> ids=new ArrayList<>();for(int i=0;i<n;i++)ids.add(buf.method_19772());return new UpdateDomainSlotPayload(op,slot,name,List.copyOf(ids));});
    public class_8710.class_9154<UpdateDomainSlotPayload> method_56479(){return ID;}
    public static void receive(class_3222 player,UpdateDomainSlotPayload p){
        if(com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(player)||com.innerdimension.voiddomain.RitualControl.locked(player))return;
        if(!com.innerdimension.progression.DomainAccess.isUnlocked(player))return;
        if(com.innerdimension.voiddomain.VoidEmbodiment.active(player)&&(p.operation==SELECT||p.operation==ENGRAVE)){
            player.method_7353(class_2561.method_43471("inner_dimension.void.crest_locked"),true);
            ServerPlayNetworking.send(player,new SelectCrestSlotPayload(DomainState.get(player.method_51469()).selectedSlot(player.method_5667())));return;
        }
        DomainState state=DomainState.get(player.method_51469());
        if(p.slot<0||p.slot>6||(p.slot==6&&!com.innerdimension.progression.InnerAdvancements.done(player,"fallen")))return;
        int slot=p.slot;
        try{
            if(p.operation==SELECT){if(slot!=state.selectedSlot(player.method_5667())&&state.voidRooms.containsKey(player.method_5667()))com.innerdimension.voiddomain.VoidContamination.rollback(player);state.selectSlot(player.method_5667(),slot);if(state.loadout(player.method_5667(),slot).contains(com.innerdimension.voiddomain.VoidContamination.ABILITY))com.innerdimension.pregnancy.MentalPregnancySystem.abortOrdinaryForVoid(player);}
            else if(p.operation==SAVE_NAME){state.renameSlot(player.method_5667(),slot,p.name);player.method_7353(class_2561.method_43471("screen.inner_dimension.name.saved"),true);}
            else if(p.operation==SAVE_OBJECTS){if(p.loadout.size()!=1||!com.innerdimension.progression.DomainAccess.canImprint(player)||!com.innerdimension.domain.DomainModes.owned(state.species(player.method_5667()),p.loadout.getFirst()))return;var ability=com.innerdimension.domain.DomainAbilityCatalog.byEntity(p.loadout.getFirst());if(ability==null||(ability.code().equals("D71")&&!com.innerdimension.progression.InnerAdvancements.done(player,"void_baptism"))||!com.innerdimension.domain.DomainModes.settings(ability.code()))return;int mask=Integer.parseInt(p.name);if(!com.innerdimension.domain.DomainModes.valid(ability.code(),mask))return;state.objectMask(player.method_5667(),slot,p.loadout.getFirst(),mask);}
            else if(p.operation==ENGRAVE){
                // The engrave controls only exist after the endurance chapter; a client that asks
                // anyway (stale screen, hand-built packet) is ignored rather than believed.
                if(!com.innerdimension.progression.DomainAccess.canImprint(player)){return;}
                if(p.loadout.stream().anyMatch(id->!com.innerdimension.domain.DomainModes.owned(state.species(player.method_5667()),id))){return;}
                int weight=com.innerdimension.domain.DomainAbilityCatalog.weight(com.innerdimension.domain.DomainAbilityCatalog.sanitize(p.loadout));
                if(weight>state.capacity(player.method_5667())&&!(p.loadout.size()==1&&com.innerdimension.domain.DomainAbilityCatalog.infinite(p.loadout))) {
                    
                    com.innerdimension.progression.InnerAdvancements.grant(player,"overload");
                    state.settleCapacity(player);
                    ServerPlayNetworking.send(player,OpenDomainCorePayload.forPlayer(player).withPreviewState(-2));
                    player.field_6008=0;
                    player.method_64397(player.method_51469(),com.innerdimension.domain.combat.DomainDamageTypes.source(player.method_51469(),"minecraft:generic_kill",null,null),2.0f);
                    return;
                }
                else {Map<String,Integer> objects=new HashMap<>();if(!p.name.isBlank()){var json=com.google.gson.JsonParser.parseString(p.name).getAsJsonObject();for(var entry:json.entrySet()){if(!p.loadout.contains(entry.getKey()))continue;var ability=com.innerdimension.domain.DomainAbilityCatalog.byEntity(entry.getKey());if(ability==null||(ability.code().equals("D71")&&!com.innerdimension.progression.InnerAdvancements.done(player,"void_baptism"))||!com.innerdimension.domain.DomainModes.settings(ability.code()))continue;int mask=entry.getValue().getAsInt();if(!com.innerdimension.domain.DomainModes.valid(ability.code(),mask)){return;}objects.put(entry.getKey(),mask);}}state.engrave(player.method_5667(),slot,p.loadout);if(slot==state.selectedSlot(player.method_5667())&&!com.innerdimension.voiddomain.VoidContamination.equipped(player))com.innerdimension.voiddomain.VoidContamination.rollback(player);objects.forEach((id,mask)->state.objectMask(player.method_5667(),slot,id,mask));com.innerdimension.progression.InnerAdvancements.grant(player,"imprinting");if(p.loadout.contains(com.innerdimension.domain.DomainContest.SIMPLE))com.innerdimension.progression.InnerAdvancements.grant(player,"simple_domain");player.method_7353(class_2561.method_43471("screen.inner_dimension.engraved.done"),true);ServerPlayNetworking.send(player,OpenDomainCorePayload.forPlayer(player).withPreviewState(-3));return; }
            }
        }catch(IllegalArgumentException conflict){player.method_7353(class_2561.method_43471(conflict.getMessage()),true);}
        ServerPlayNetworking.send(player,OpenDomainCorePayload.forPlayer(player));
    }
}
