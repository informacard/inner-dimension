package com.innerdimension;

import com.innerdimension.block.ModBlocks;
import com.innerdimension.binding.DollBindingManager;
import com.innerdimension.command.InnerDimensionCommand;
import com.innerdimension.config.InnerConfig;
import com.innerdimension.core.ServerClock;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.entity.ModEntities;
import com.innerdimension.exit.ModExitComponents;
import com.innerdimension.item.ModItems;
import com.innerdimension.pearl.PearlSurfaceTransport;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1761;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7923;

public class InnerDimension implements ModInitializer {
    public static final String MOD_ID = "inner_dimension";

    public static class_2960 id(String path) {
        return class_2960.method_60655(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> com.innerdimension.projection.OpenDomainPerformance.shutdown());
        com.innerdimension.domain.DomainInteractionGuard.register();
        com.innerdimension.domain.DomainAbilityEngine.register();
        InnerConfig.load(FabricLoader.getInstance().getConfigDir());

        com.innerdimension.sound.ModSounds.init();
        ModBlocks.init();
        ModItems.init();
        // NoN has already registered its data-driven accessories by now (dependency order), so the
        // addon can attach an iron-plug-identical definition to its own item id.
        com.innerdimension.compat.NonBottlingCompat.installAutoPlugDefinition("legs/a", "legs/v");
        com.innerdimension.effect.ModStatusEffects.init();
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.DomainSlashVisualPayload.ID,com.innerdimension.network.DomainSlashVisualPayload.CODEC);
        com.innerdimension.pregnancy.MentalPregnancySystem.register();
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.item.OpenDomainCorePayload.ID,com.innerdimension.item.OpenDomainCorePayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.EntryTransitionPayload.ID,com.innerdimension.network.EntryTransitionPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.EnlightenmentVisualPayload.ID,com.innerdimension.network.EnlightenmentVisualPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.EnlightenmentVisualPayload.ID,com.innerdimension.network.EnlightenmentVisualPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.DomainVisualStatePayload.ID,com.innerdimension.network.DomainVisualStatePayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.DomainAbilityStatePayload.ID,com.innerdimension.network.DomainAbilityStatePayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.DomainAbilityInputPayload.ID,com.innerdimension.network.DomainAbilityInputPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.DomainAbilityInputPayload.ID,(payload,context)->context.server().execute(()->com.innerdimension.network.DomainAbilityInputPayload.receive(context.player(),payload)));
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.CrestChangedPayload.ID,com.innerdimension.network.CrestChangedPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.CrestChangedPayload.ID,(payload,context)->context.server().execute(()->com.innerdimension.network.CrestChangedPayload.receive(context.player())));
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.UpdateDomainSlotPayload.ID,com.innerdimension.network.UpdateDomainSlotPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.UpdateDomainSlotPayload.ID,(payload,context)->context.server().execute(()->com.innerdimension.network.UpdateDomainSlotPayload.receive(context.player(),payload)));
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.SubmitDomainBottlePayload.ID,com.innerdimension.network.SubmitDomainBottlePayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.SubmitDomainBottlePayload.ID,(payload,context)->context.server().execute(()->com.innerdimension.network.SubmitDomainBottlePayload.receive(context.player())));
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(com.innerdimension.network.CentrifugeActionPayload.ID, com.innerdimension.network.CentrifugeActionPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.CentrifugeActionPayload.ID,
                (payload, context) -> context.server().execute(() -> com.innerdimension.network.CentrifugeActionPayload.receive(context.player(), payload)));
        ServerTickEvents.END_SERVER_TICK.register(com.innerdimension.progression.DomainFeatures::tick);
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler,sender,server) -> com.innerdimension.network.DomainVisualStateSync.joined(server,handler.field_14140));
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> com.innerdimension.network.DomainVisualStateSync.left(server,handler.field_14140.method_5667()));
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            com.innerdimension.item.EnderPearlPlugItem.forgetWearTicks(handler.field_14140.method_5667());
            com.innerdimension.item.AutoBottlingPlugItem.forget(handler.field_14140.method_5667());
        });
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.SoundCuePayload.ID,com.innerdimension.network.SoundCuePayload.CODEC);
        com.innerdimension.sound.ModSounds.register();
        ModEntities.init();
        ModExitComponents.init();
        com.innerdimension.animation.DildoEntryAnimation.register();
        com.innerdimension.animation.DildoInteractionGuard.register();

        TrinketsApi.registerTrinket(ModItems.ENDER_PEARL_PLUG, (Trinket) ModItems.ENDER_PEARL_PLUG);
        TrinketsApi.registerTrinket(ModItems.AUTO_BOTTLING_PLUG, (Trinket) ModItems.AUTO_BOTTLING_PLUG);
        TrinketsApi.registerTrinket(ModItems.STICKY_ENDER_PEARL_PLUG, new Trinket() {
        });
        TrinketsApi.registerTrinket(ModItems.AMETHYST_NAVEL_PIERCING,(Trinket)ModItems.AMETHYST_NAVEL_PIERCING);
        TrinketsApi.registerTrinket(ModItems.CLIP_ON_AMETHYST_NAVEL_PIERCING,(Trinket)ModItems.CLIP_ON_AMETHYST_NAVEL_PIERCING);

        class_1761 group = FabricItemGroup.builder()
                .method_47321(class_2561.method_43471("itemGroup.inner_dimension.main"))
                .method_47320(() -> new class_1799(ModItems.ENDER_PEARL_DILDO))
                .method_47317((displayContext, entries) -> {
                    entries.method_45421(ModItems.MENTAL_FRAGMENT);
                    entries.method_45421(ModItems.VOID_FRAGMENT);
                    entries.method_45421(ModItems.INFECTOR);
                    entries.method_45421(ModItems.VOID_NAIL);
                    entries.method_45421(ModItems.SHEATHED_INFECTOR);
                    entries.method_45421(ModItems.VOID_WATER_ITEM);
                    entries.method_45421(ModItems.VOID_MIST_ITEM);
                    entries.method_45421(ModItems.VILLAGER_SOUL_CORE);
                    entries.method_45421(ModItems.DOMAIN_CORE);
                    entries.method_45421(ModItems.AMETHYST_NAVEL_PIERCING);
                    entries.method_45421(ModItems.CLIP_ON_AMETHYST_NAVEL_PIERCING);
                    entries.method_45421(ModItems.ENDER_PEARL_PLUG);
                    entries.method_45421(ModItems.STICKY_ENDER_PEARL_PLUG);
                    entries.method_45421(ModItems.ENDER_PEARL_DILDO);
                    entries.method_45421(ModItems.TETHERED_ENDER_PEARL_DILDO);
                    entries.method_45421(ModItems.PEARL_FACE_ITEM);
                    entries.method_45421(ModItems.INTESTINES_BLOCK_ITEM);
                    entries.method_45421(ModItems.ABYSS_BLOCK_ITEM);
                    entries.method_45421(ModItems.FLESH_WATER_ITEM);
                    entries.method_45421(ModItems.SOLID_FLESH_WATER_ITEM);
                    entries.method_45421(ModItems.SOLID_VOID_WATER_ITEM);
                    entries.method_45421(ModItems.SOUL_MIST_ITEM);
                    entries.method_45421(ModItems.CENTRIFUGE_ITEM);
                    entries.method_45421(ModItems.CREATIVE_SEMEN_BOTTLE);
                    entries.method_45421(ModItems.AUTO_BOTTLING_PLUG);
                    entries.method_45421(ModItems.SEMEN_EXTRACTOR);
                })
                .method_47324();
        class_2378.method_10230(class_7923.field_44687, id("main"), group);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            com.innerdimension.item.AutoBottlingPlugItem.clear();
            com.innerdimension.pregnancy.MentalPregnancySystem.clearTransient();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(DynamicDimensionManager::onServerStarted);
        ServerLifecycleEvents.SERVER_STARTED.register(com.innerdimension.progression.VillagerSoulChallenge::startWorld);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DynamicDimensionManager.onServerStopping());
        ServerLifecycleEvents.SERVER_STARTED.register(server -> com.innerdimension.animation.DildoEntryAnimation.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> com.innerdimension.animation.DildoEntryAnimation.clear());
        // The clock is advanced once, at the very start of the tick, before any module reads it.
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            ServerClock.advance();
            DynamicDimensionManager.beginServerTick();
            com.innerdimension.chunk.StrongPortChunkLoads.tick(server);
        });
        // One fixed end-of-tick order: bindings, queued inputs, waiting outputs, boundary, probes.
        ServerTickEvents.END_SERVER_TICK.register(DynamicDimensionManager::tickEnd);
        com.innerdimension.voiddomain.VoidContamination.register();
        com.innerdimension.voiddomain.VoidEmbodiment.register();
        com.innerdimension.item.SoulboundCores.register();
        com.innerdimension.item.DomainCoreBottleUse.register();
        com.innerdimension.item.SemenExtraction.register();
        com.innerdimension.voiddomain.VoidRitual.register();
        com.innerdimension.transport.TeleportWarmup.register();
        ServerTickEvents.START_SERVER_TICK.register(com.innerdimension.progression.DomainRitual::before);
        ServerTickEvents.END_SERVER_TICK.register(com.innerdimension.progression.DomainRitual::after);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> com.innerdimension.progression.DomainRitual.clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> com.innerdimension.network.DomainVisualStateSync.clear());
        ServerTickEvents.END_SERVER_TICK.register(com.innerdimension.animation.DildoEntryAnimation::tick);
        com.innerdimension.boss.BossDomains.register();
        com.innerdimension.projection.RoomProjectionManager.register();
        ServerTickEvents.END_SERVER_TICK.register(com.innerdimension.network.DomainVisualStateSync::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                InnerDimensionCommand.register(dispatcher));

        // Sneak + right-click a pearl_face floor block to leave. Handled on the server
        // through Fabric's block-use event so it also fires when the player is holding
        // an item (vanilla skips the block's own use hook in that case). Both entry points
        // funnel into one transaction that runs at most once per player per tick.
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.method_8608()
                    || !player.method_5715()) {
                return class_1269.field_5811;
            }
            class_2338 pos = hit.method_17777();
            if (world.method_8320(pos).method_26204() != ModBlocks.PEARL_FACE) {
                return class_1269.field_5811;
            }
            if (player instanceof class_3222 serverPlayer
                    && PearlSurfaceTransport.leaveFromSurface(serverPlayer, world)) {
                return class_1269.field_5812;
            }
            return class_1269.field_5811;
        });
    }
}

