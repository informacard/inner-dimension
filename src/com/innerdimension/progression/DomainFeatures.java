package com.innerdimension.progression;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.item.ModItems;
import com.innerdimension.transport.SafeEntityTeleport;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import nonplayerplushie.SensoryLinkData;
import java.util.*;
public final class DomainFeatures {
    public static boolean introduce(class_3222 player) {
        class_3218 world=player.method_51469(); UUID owner=player.method_5667();
        if(!owner.equals(DynamicDimensionManager.ownerOfWorld(world.method_27983()))) return false;
        DomainState state=DomainState.get(world);
        if(state.introduced.contains(owner)) return false;
        class_1799 core=new class_1799(ModItems.DOMAIN_CORE);
        SensoryLinkData.write(core,SensoryLinkData.fromPlayer(player));
        class_1542 item=new class_1542(world,8,33.5,8,core,0,0,0);
        item.method_48349(owner);item.method_5875(true);item.method_35190();item.method_6975();
        // NoGravity is saved by vanilla: the effect survives server restarts until pickup.
        if(!world.method_8649(item)) return false;
        state.introduce(owner);
        SafeEntityTeleport.move(player,world,.5,32.5,.5,Set.of(),player.method_36454(),player.method_36455(),false);
        player.method_18799(class_243.field_1353);player.field_6017=0;
        return true;
    }
    public static class_2561 announcement(class_3222 player,UUID owner,boolean expanded) {
        return class_2561.method_43469(expanded ? "inner_dimension.domain.expanded_named" : "inner_dimension.domain.entered_named",DomainState.get(player.method_51469()).name(owner));
    }
    public static void grantOpenAbilities(class_3222 player) {
        var state=DomainState.get(player.method_51469());
        syncAbility(state,player.method_5667(),com.innerdimension.domain.DomainModes.SUCCUBUS,InnerAdvancements.done(player,"enlightenment"));

    }
    public static boolean shrineAvailable(boolean baptized,Collection<String> species){return baptized&&species.contains("minecraft:vindicator");}
    public static boolean canUseShrine(class_3222 player){return shrineAvailable(InnerAdvancements.done(player,"void_baptism"),DomainState.get(player.method_51469()).species(player.method_5667()));}
    public static void grantAdvancedAbilities(class_3222 player){
        var state=DomainState.get(player.method_51469());var id=player.method_5667();
        boolean baptized=InnerAdvancements.done(player,"void_baptism");
        syncAbility(state,id,com.innerdimension.domain.UnlimitedVoidRules.ID,baptized);
        if(shrineAvailable(baptized,state.species(id)))state.addSpecies(id,com.innerdimension.domain.DomainModes.SHRINE);
        else state.removeSpecies(id,com.innerdimension.domain.DomainModes.SHRINE);
    }
    private static void syncAbility(DomainState state,UUID owner,String ability,boolean unlocked){
        // Advancement commands must revoke the unlock as well as grant it.
        if(unlocked)state.addSpecies(owner,ability);
        else state.removeSpecies(owner,ability);
    }
    public static void tick(MinecraftServer server) {
        Set<class_3218> rooms=new HashSet<>();
        for(class_3222 player:server.method_3760().method_14571()) {
            ManualProgression.tick(player);
            grantOpenAbilities(player);
            grantAdvancedAbilities(player);
            syncAbility(DomainState.get(player.method_51469()),player.method_5667(),com.innerdimension.voiddomain.VoidContamination.ABILITY,InnerAdvancements.done(player,"fallen"));
            syncAbility(DomainState.get(player.method_51469()),player.method_5667(),com.innerdimension.domain.DomainContest.SIMPLE,InnerAdvancements.done(player,"mental_seeding"));
            // Also repair the discovery for players already wearing the formerly omitted clip-on.
            if(com.innerdimension.item.NavelPiercingItem.isEquipped(player)
                    && !InnerAdvancements.done(player,"great_creation")) InnerAdvancements.grant(player,"great_creation","crafted");
            var world=player.method_51469();
            if(DynamicDimensionManager.isInnerWorld(world.method_27983())) {
                rooms.add(world);
                if(player.method_23318()>=31 && InnerAdvancements.done(player,"cursed_core")) introduce(player);
            }
            for(int i=0;i<player.method_31548().method_5439();i++) {
                class_1799 stack=player.method_31548().method_5438(i);
                if(stack.method_7909()==ModItems.DOMAIN_CORE && player.method_5667().equals(SensoryLinkData.ownerUuid(SensoryLinkData.read(stack)))) {
                    InnerAdvancements.grant(player,"cursed_core");
                }
            }
        }
        for(class_3218 world:rooms) {
            UUID owner=DynamicDimensionManager.ownerOfWorld(world.method_27983());
            if(owner==null) continue;
            class_3222 player=server.method_3760().method_14602(owner);
            for(class_1542 item:world.method_8390(class_1542.class,new class_238(0,31,0,16,48,16),e -> !e.method_31747())) {
                class_1799 stack=item.method_6983();
                if(stack.method_7909()==ModItems.DOMAIN_CORE && item.method_5740()) {
                    item.method_18799(class_243.field_1353);
                    if(com.innerdimension.core.ServerClock.now()%4==0)
                        world.method_14199(class_2398.field_23190,false,false,item.method_23317(),item.method_23318()+.15,item.method_23321(),2,.15,.08,.15,.025);
                }
                if(stack.method_7909()!=class_7923.field_41178.method_63535(class_2960.method_12829("minecraft:name_tag"))
                    || player==null || player.method_51469()!=world || !stack.method_57826(class_9334.field_49631)) continue;
                // Only the room owner's own dropped item can name their domain.
                if(item.method_24921()!=player) continue;
                String name=stack.method_58694(class_9334.field_49631).getString();
                if(name.isBlank()) continue;
                DomainState.get(world).rename(owner,name);
                stack.method_7934(1);
                if(stack.method_7960()) item.method_31472();else item.method_6979(stack);
                InnerAdvancements.grant(player,"naming");
                player.method_7353(class_2561.method_43469("inner_dimension.domain.named",class_2561.method_43470(name)),true);
            }
        }
    }
}
