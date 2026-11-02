package com.innerdimension.pregnancy;

import com.innerdimension.effect.ModStatusEffects;
import com.innerdimension.progression.InnerAdvancements;
import com.innerdimension.progression.DomainState;
import com.nonid.api.NonEvents;
import com.nonid.api.NonPlayerApi;
import com.nonid.effect.NonStatusEffects;
import java.util.*;
import net.minecraft.*;

/** Preserve NoN gestation, complete mental pregnancies as persistent domain rewards. */
public final class MentalPregnancySystem {
    private static final long DURATION=3L*60L*20L;
    /** Temporary rapid-opening stacks; deliberately transient, never persisted. */
    private static final Map<UUID,Integer> RAPID_CHAIN=new HashMap<>();
    private static final Map<UUID,Long> LAST_OPEN_MILLIS=new HashMap<>();
    private static boolean registered;
    private MentalPregnancySystem(){}
    public static void register(){
        if(registered)return;registered=true;
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(MentalPregnancySystem::tick);
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
                (handler,server)->forget(handler.field_14140.method_5667()));
        NonEvents.PREGNANCY_CHANGED.register((player,snapshot)->{
            if(!snapshot.active()){
                DomainState state=DomainState.get(player.method_51469());
                state.voidGestations.remove(player.method_5667());
                if(state.mentalPregnancies.remove(player.method_5667())!=null){state.method_80();player.method_6016(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY));removeFetus(player);}
            }
        });
    }
    /** Called before NoN can queue the hatch animation. */
    public static boolean interceptTick(class_3222 player,long now){
        if(com.innerdimension.voiddomain.VoidEmbodiment.active(player)){abortForEmbodiment(player);return false;}
        if(!isMental(player))return false;
        var snapshot=NonPlayerApi.getPregnancy(player);
        if(snapshot.active()&&(snapshot.pendingHatch()||snapshot.endTick()<=now)){
            DomainState state=DomainState.get(player.method_51469());
            List<String> types=new ArrayList<>(state.mentalPregnancies.get(player.method_5667()));
            // Old saves only stored additional species; include NoN's primary species.
            if(!types.contains(snapshot.entityTypeId().toString()))types.add(snapshot.entityTypeId().toString());
            var rewards=new ArrayList<>(state.mentalRewards.getOrDefault(player.method_5667(),List.of()));
            boolean voidGestation=state.voidGestations.contains(player.method_5667());
            rewards.add((voidGestation?"void:":"")+MentalPregnancyChance.fragmentReward(
                    com.innerdimension.domain.DomainAbilityCatalog.weight(types),voidGestation));
            state.mentalRewards.put(player.method_5667(),List.copyOf(rewards));state.method_80();
            NonPlayerApi.clearPregnancy(player);
        }
        else if(snapshot.active()&&!player.method_6059(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY)))NonPlayerApi.clearPregnancy(player);
        return true;
    }
    private static class_3218 room(class_3222 player){return player.method_51469().method_8503().method_3847(com.innerdimension.dimension.DynamicDimensionManager.worldKey(player.method_5667()));}
    private static void removeFetus(class_3222 player){var world=room(player);if(world!=null)for(var fetus:world.method_8390(com.innerdimension.entity.MentalFetusEntity.class,new class_238(0,31,0,16,48,16),e->true))fetus.method_31472();if(world!=null)for(var fetus:world.method_8390(com.innerdimension.entity.VoidFetusEntity.class,new class_238(0,31,0,16,48,16),e->true))fetus.method_31472();}
    private static void tick(net.minecraft.server.MinecraftServer server){
        for(var player:server.method_3760().method_14571()){
            var state=DomainState.get(player.method_51469());var world=room(player);if(world==null&&isMental(player))world=com.innerdimension.dimension.DynamicDimensionManager.getOrCreate(server,player.method_5667());if(world==null)continue;
            if(isMental(player)||state.mentalRewards.containsKey(player.method_5667()))world.method_8500(new class_2338(8,39,8));
            var fetuses=world.method_8390(com.innerdimension.entity.MentalFetusEntity.class,new class_238(0,31,0,16,48,16),e->!e.method_31747());
            if(isMental(player)&&!state.voidGestations.contains(player.method_5667())){
                for(var fetus:fetuses)if(fetus.method_23318()!=40)fetus.method_5814(8,40,8);
                var flags=state.mentalRewards.getOrDefault(player.method_5667(),List.of());
                if(fetuses.isEmpty()){
                    var fetus=new com.innerdimension.entity.MentalFetusEntity(com.innerdimension.entity.ModEntities.MENTAL_FETUS,world);fetus.method_5814(8,40,8);
                    if(world.method_8649(fetus)){var copy=new ArrayList<>(flags);copy.removeIf("fetus_spawned"::equals);copy.add("fetus_spawned");state.mentalRewards.put(player.method_5667(),List.copyOf(copy));state.method_80();}
                }
            }else{
                for(var fetus:fetuses)fetus.method_31472();
            }
            var voidFetuses=world.method_8390(com.innerdimension.entity.VoidFetusEntity.class,new class_238(0,31,0,16,48,16),e->!e.method_31481());
            if(isMental(player)&&state.voidGestations.contains(player.method_5667())){
                if(voidFetuses.isEmpty()){var fetus=new com.innerdimension.entity.VoidFetusEntity(com.innerdimension.entity.ModEntities.VOID_FETUS,world);fetus.method_5814(8,40,8);world.method_8649(fetus);}
            }else for(var fetus:voidFetuses)fetus.method_31472();
            {
                // Both fragment types remain saved rewards until the owner returns to the room.
                if(!com.innerdimension.voiddomain.VoidContamination.inOwnRoom(player))continue;
                var rewards=state.mentalRewards.get(player.method_5667());if(rewards==null)continue;
                var remaining=new ArrayList<String>();if(isMental(player)&&rewards.contains("fetus_spawned"))remaining.add("fetus_spawned");
                for(String reward:rewards){if(reward.equals("fetus_spawned"))continue;boolean voidReward=reward.startsWith("void:");int count=Integer.parseInt(voidReward?reward.substring(5):reward);
                    while(count>0){int size=Math.min(64,count);var item=new class_1542(world,8,39.5,8,new class_1799(voidReward?com.innerdimension.item.ModItems.VOID_FRAGMENT:com.innerdimension.item.ModItems.MENTAL_FRAGMENT,size),0,0,0);item.method_35190();if(!world.method_8649(item))break;count-=size;}
                    if(count>0)remaining.add((voidReward?"void:":"")+Integer.toString(count));
                }
                if(remaining.equals(rewards))continue;
                if(remaining.isEmpty())state.mentalRewards.remove(player.method_5667());else state.mentalRewards.put(player.method_5667(),List.copyOf(remaining));state.method_80();
            }
        }
    }
    public static void tryStart(class_3222 player,List<String> engraved){
        if(com.innerdimension.voiddomain.VoidEmbodiment.active(player))return;
        DomainState state=DomainState.get(player.method_51469());
        UUID owner=player.method_5667();
        if(engraved==null||engraved.isEmpty()||!NonPlayerApi.isPregnancyEnabled()||NonPlayerApi.getPregnancy(player).active())return;
        List<class_2960> candidates=new ArrayList<>();
        boolean voidPregnancy=com.innerdimension.voiddomain.VoidContamination.active(player);
        List<String> conceptionSources=voidPregnancy?state.species(owner).stream().filter(id->!id.startsWith("inner_dimension:")).toList():engraved.stream().map(com.innerdimension.domain.DomainModes::sourceSpecies).distinct().toList();
        if(voidPregnancy&&conceptionSources.isEmpty())conceptionSources=List.of("minecraft:chicken");
        for(String raw:conceptionSources){class_2960 id=class_2960.method_12829(raw);if(id!=null&&class_7923.field_41177.method_10250(id))candidates.add(id);}
        if(candidates.isEmpty())return;
        int steps=state.mentalChanceSteps.getOrDefault(owner,0);
        // Rapid re-opening of the domain is itself a risk: every consecutive opening inside the
        // six second window adds 20 temporary percentage points on top of the engraved chance.
        int rapidChain=advanceRapidChain(owner);
        double chance=MentalPregnancyChance.probability(steps,fertilityAmplifier(player),voidPregnancy,rapidChain);
        state.mentalChanceSteps.put(owner,MentalPregnancyChance.next(steps));
        boolean discovered=InnerAdvancements.done(player,"mental_seeding");
        int attempts=Math.min(50,state.mentalDiscoveryAttempts.getOrDefault(owner,0)+1);
        if(!discovered)state.mentalDiscoveryAttempts.put(owner,attempts);
        state.method_80();
        if(!MentalPregnancyChance.discoveryGuaranteed(attempts,discovered,fertilityAmplifier(player))
                && player.method_51469().method_8409().method_43058()>=Math.min(1.0,chance))return;
        Collections.shuffle(candidates,new Random(player.method_51469().method_8409().method_43055()));
        int kinds=1+player.method_51469().method_8409().method_43048(candidates.size());
        List<class_2960> conceived=List.copyOf(candidates.subList(0,kinds));
        if(!beginGestation(player,conceived,DURATION,voidPregnancy))return;
        state.mentalChanceSteps.put(owner,0);state.mentalDiscoveryAttempts.remove(owner);state.method_80();
        // A completed conception ends the rapid-opening chain; the next opening starts fresh.
        RAPID_CHAIN.remove(owner);LAST_OPEN_MILLIS.remove(owner);
        InnerAdvancements.grant(player,"mental_seeding");
        if(voidPregnancy)InnerAdvancements.grant(player,"void_seeding");
    }
    /** Debug uses every valid species in the selected engraving, without a probability roll. */
    public static boolean debugCurrentEngraving(class_3222 player){
        DomainState state=DomainState.get(player.method_51469());UUID owner=player.method_5667();
        List<String> engraved=state.loadout(owner,state.selectedSlot(owner));
        boolean voidPregnancy=com.innerdimension.voiddomain.VoidContamination.active(player);
        List<String> sources=voidPregnancy?state.species(owner).stream().filter(id->!id.startsWith("inner_dimension:")).toList():engraved.stream().map(com.innerdimension.domain.DomainModes::sourceSpecies).distinct().toList();
        if(voidPregnancy&&sources.isEmpty())sources=List.of("minecraft:chicken");
        List<class_2960> conceived=sources.stream().map(class_2960::method_12829)
                .filter(id->id!=null&&class_7923.field_41177.method_10250(id)).distinct().toList();
        if(conceived.isEmpty()||!NonPlayerApi.isPregnancyEnabled())return false;
        if(NonPlayerApi.getPregnancy(player).active()){
            if(!isMental(player))return false;
            NonPlayerApi.clearPregnancy(player);
        }
        return beginGestation(player,conceived,100L,voidPregnancy);
    }
    private static boolean beginGestation(class_3222 player,List<class_2960> conceived,long duration,boolean voidPregnancy){
        DomainState state=DomainState.get(player.method_51469());UUID owner=player.method_5667();
        state.mentalPregnancies.put(owner,conceived.stream().map(Object::toString).toList());state.method_80();
        long now=player.method_51469().method_8503().method_30002().method_75260();
        if(!beginForAnyGender(player,conceived.getFirst(),now+duration)){
            state.mentalPregnancies.remove(owner);state.method_80();return false;
        }
        var retained=new ArrayList<>(state.mentalRewards.getOrDefault(owner,List.of()));retained.removeIf("fetus_spawned"::equals);
        if(retained.isEmpty())state.mentalRewards.remove(owner);else state.mentalRewards.put(owner,List.copyOf(retained));
        if(voidPregnancy)state.voidGestations.add(owner);else state.voidGestations.remove(owner);
        state.method_80();
        player.method_6092(new class_1293(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY),(int)duration,0,false,true,true));
        com.innerdimension.sound.ModSounds.personal(player,"mental_conception",.8f);
        
        
        return true;
    }
    /** Use the pregnancy record, even before the visual fetus has had a chance to spawn. */
    public static void abortForEmbodiment(class_3222 player){
        if(!isMental(player))return;
        NonPlayerApi.clearPregnancy(player);
        var state=DomainState.get(player.method_51469());state.mentalPregnancies.remove(player.method_5667());state.voidGestations.remove(player.method_5667());state.method_80();
        player.method_6016(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY));removeFetus(player);
    }
    public static boolean abortOrdinaryForVoid(class_3222 player){
        DomainState state=DomainState.get(player.method_51469());UUID owner=player.method_5667();
        if(!state.mentalPregnancies.containsKey(owner)||state.voidGestations.contains(owner))return false;
        NonPlayerApi.clearPregnancy(player);
        state.mentalPregnancies.remove(owner);state.voidGestations.remove(owner);
        player.method_6016(class_7923.field_41174.method_47983(ModStatusEffects.MENTAL_PREGNANCY));
        removeFetus(player);
        var retained=new ArrayList<>(state.mentalRewards.getOrDefault(owner,List.of()));retained.removeIf("fetus_spawned"::equals);
        if(retained.isEmpty())state.mentalRewards.remove(owner);else state.mentalRewards.put(owner,List.copyOf(retained));
        state.method_80();InnerAdvancements.grant(player,"end_traditional_abortion");return true;
    }
    private static boolean beginForAnyGender(class_3222 player,class_2960 type,long endTick){
        return NonPlayerApi.beginPregnancy(player,type,endTick);
    }
    public static boolean isMental(class_3222 player){
        return DomainState.get(player.method_51469()).mentalPregnancies.containsKey(player.method_5667());
    }
    /** Drops the temporary rapid-opening state of one player (logout, death cleanup). */
    public static void forget(UUID owner){if(owner!=null){RAPID_CHAIN.remove(owner);LAST_OPEN_MILLIS.remove(owner);}}
    public static void clearTransient(){RAPID_CHAIN.clear();LAST_OPEN_MILLIS.clear();}
    private static int advanceRapidChain(UUID owner){
        long now=System.currentTimeMillis();
        Long last=LAST_OPEN_MILLIS.get(owner);
        int chain=MentalPregnancyChance.rapidChain(RAPID_CHAIN.getOrDefault(owner,0),last==null?Long.MAX_VALUE:now-last);
        LAST_OPEN_MILLIS.put(owner,now);RAPID_CHAIN.put(owner,chain);
        return chain;
    }
    private static int fertilityAmplifier(class_3222 player){
        int amplifier=-1;
        for(class_1293 effect:player.method_6026())if(effect.method_5579().comp_349()==NonStatusEffects.FERTILITY_INCREASER)amplifier=Math.max(amplifier,effect.method_5578());
        return amplifier;
    }
}
