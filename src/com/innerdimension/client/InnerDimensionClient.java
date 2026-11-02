package com.innerdimension.client;

import com.innerdimension.block.ModBlocks;
import com.innerdimension.entity.ModEntities;
import net.minecraft.class_877;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_3264;

public class InnerDimensionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type,renderer,helper,context)->{
            if(renderer instanceof net.minecraft.class_1007) helper.register(new DomainCrestFeature((net.minecraft.class_3883)renderer));
        });
        EntityRendererRegistry.register(ModEntities.MENTAL_FETUS,MentalFetusRenderer::new);
        EntityRendererRegistry.register(ModEntities.VOID_FETUS,context->new MentalFetusRenderer(context,true));
        ColorProviderRegistry.BLOCK.register((state,view,pos,index)->0xff151515,ModBlocks.INFECTOR,ModBlocks.SHEATHED_INFECTOR);
        ColorProviderRegistry.BLOCK.register((state,view,pos,index)->VoidMaterialColors.WATER,ModBlocks.VOID_WATER,ModBlocks.SOLID_VOID_WATER);
        ColorProviderRegistry.BLOCK.register((state,view,pos,index)->VoidMaterialColors.MIST,ModBlocks.VOID_MIST);
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(MentalFetusRenderer.RELOAD);
        InnerSoundClient.register();
        ExtractionClient.register();
        EnlightenmentOverlay.register();
        UnlimitedVoidClient.register();
        RoomProjectionClient.register();
        DomainCrestHud.register();
        DomainExpansionHud.register();
        BossDomainHud.register();
        BossDomainVisuals.register();
        com.innerdimension.client.domain.DomainClientState.register();
        com.innerdimension.client.domain.DomainContestClient.register();
        com.innerdimension.domain.ability.D43.installClientState(entity ->
                entity == net.minecraft.class_310.method_1551().field_1724
                        && com.innerdimension.client.domain.DomainClientState.d43FastCharge());
        com.innerdimension.domain.ability.D41.installClientState(entity ->
                entity == net.minecraft.class_310.method_1551().field_1724
                        && com.innerdimension.client.domain.DomainClientState.d41Available());
        com.innerdimension.domain.ability.D52.installClientState(entity ->
                com.innerdimension.client.domain.DomainObjectClientState.lava(entity.method_5667()));
        com.innerdimension.client.domain.DomainOutlineRenderer.register();
        com.innerdimension.client.domain.DomainSlashRenderer.register();
        com.innerdimension.client.domain.DomainAbilityHud.register();
        com.innerdimension.client.domain.DomainAbilityInputClient.register();

        registerCentrifugeScreen();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.innerdimension.network.SelectCrestSlotPayload.ID,(payload,context)->context.client().execute(()->{if(payload.slot()<0||payload.slot()>6)return;com.innerdimension.client.crest.DomainCrestClient.currentDesign();com.innerdimension.client.crest.CrestDesignStore.select(payload.slot());com.innerdimension.client.crest.DomainCrestClient.applyLocalDesign(com.innerdimension.client.crest.CrestDesignStore.slot(payload.slot()),com.innerdimension.client.crest.CrestDesignStore.resolution(payload.slot()));}));
        ClientTickEvents.END_CLIENT_TICK.register(TetheredContainerMovementLock::tick);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.innerdimension.item.OpenDomainCorePayload.ID,
            (payload,context) -> context.client().execute(() -> {
                if(payload.previewState()==-2){if(context.client().field_1755 instanceof DomainCoreScreen || context.client().field_1755 instanceof CrestEditorScreen || context.client().field_1755 instanceof FloorPatternScreen)context.client().method_1507(null);return;}
                DomainCoreScreen next=new DomainCoreScreen(payload.species(),payload.previewSource(),payload.previewState(),payload.selectedSlot(),payload.slotNames(),payload.slotLoadouts(),payload.revealAbilities(),payload.imprintUnlocked(),payload.mentalVolumeUnlocked(),payload.capacity(),payload.overloadUnlocked(),payload.objectSelections(),payload.strengthUnlocked(),payload.openUnlocked(),payload.floorStates(),payload.floorPattern());
                // A single slot click in the crest editor only selects it; its reply must refresh that
                // editor's parent instead of closing the editor. Double click and Save still return.
                if(context.client().field_1755 instanceof CrestEditorScreen editor&&editor.consumeHold()){editor.adoptParent(next);return;}
                if(context.client().field_1755 instanceof FloorPatternScreen floor){next.inheritDraft(floor.parent());}
                if(payload.previewState()!=-3&&context.client().field_1755 instanceof DomainCoreScreen previous)next.inheritDraft(previous);
                context.client().method_1507(next);
            }));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.innerdimension.network.DomainVisualStatePayload.ID,
            (payload,context) -> context.client().execute(() -> {com.innerdimension.client.crest.DomainCrestClient.accept(payload.playerUuid(),payload.active(),payload.visualRevision(),payload.domainAge(),payload.burnoutRemaining());com.innerdimension.client.domain.DomainObjectClientState.accept(payload.playerUuid(),payload.objectFlags());}));
        // Fabric can invoke DISCONNECT on Netty's IO thread; GPU resources belong to the client thread.
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> client.execute(com.innerdimension.client.crest.DomainCrestClient::clear));
        EntityRendererRegistry.register(ModEntities.ENDER_PEARL_DILDO, EnderPearlDildoRenderer::new);
        EntityRendererRegistry.register(ModEntities.TETHERED_ENDER_PEARL_DILDO,
                context -> new EnderPearlDildoRenderer(context, "entity/roped_ender_pearl_dildo"));
        EntityRendererRegistry.register(ModEntities.SENSORY_LINK_DOLL, SensoryLinkDollRenderer::new);
        // The pre-rename doll id gets the same renderer, so a doll loaded from an older save looks
        // exactly like a new one instead of falling back to the missing-entity box.
        if (ModEntities.LEGACY_DOLL != null) {
            EntityRendererRegistry.register(ModEntities.LEGACY_DOLL, SensoryLinkDollRenderer::new);
        }
        ColorProviderRegistry.BLOCK.register(PearlFaceColors::getColor, ModBlocks.PEARL_FACE);
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new PearlFaceColors());
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new com.innerdimension.client.crest.DomainCrestClient());
    }

    /** HandledScreens' intermediary factory is package-private in the compile jar, so bridge it reflectively. */
    private static void registerCentrifugeScreen() {
        try {
            Class<?> factoryType = Class.forName("net.minecraft.class_3929$class_3930");
            Object factory = com.innerdimension.core.InterfaceFactoryBridge.create(factoryType,
                    args -> new CentrifugeScreen(
                            (com.innerdimension.centrifuge.CentrifugeScreenHandler) args[0],
                            (net.minecraft.class_1661) args[1], (net.minecraft.class_2561) args[2]));
            java.lang.reflect.Method register = net.minecraft.class_3929.class.getDeclaredMethod("method_17542", net.minecraft.class_3917.class, factoryType);
            register.setAccessible(true);
            register.invoke(null, ModBlocks.CENTRIFUGE_SCREEN_HANDLER, factory);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to register centrifuge screen", e);
        }
    }
}

