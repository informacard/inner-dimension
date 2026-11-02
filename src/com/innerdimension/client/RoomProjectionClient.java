package com.innerdimension.client;

import com.innerdimension.InnerDimension;
import com.innerdimension.projection.RoomProjectionManager;
import com.innerdimension.projection.ToggleProjectionPayload;
import com.innerdimension.projection.ToggleOpenProjectionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

public final class RoomProjectionClient {
    public static void register() {
        ProjectionDiagnostics.register();
        TeleportDiagnostics.register();
        IrisTeleportWarmup.register();
        EntryTransition.register();
        ProjectionWarmup.register();
        ProjectionSyncClient.register();
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->RoomProjectionRenderer.clearCaches());
        net.fabricmc.fabric.api.resource.ResourceManagerHelper.get(net.minecraft.class_3264.field_14188).registerReloadListener(
            new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                public net.minecraft.class_2960 getFabricId(){return InnerDimension.id("projection_meshes");}
                public void method_14491(net.minecraft.class_3300 manager){RoomProjectionRenderer.clearCaches();}
            });
        EntityRendererRegistry.register(RoomProjectionManager.ENTITY_TYPE,
                RoomProjectionRenderer::new);
        class_304.class_11900 category = class_304.class_11900.method_74698(InnerDimension.id("projection"));
        class_304 key = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.inner_dimension.toggle_projection", class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_R, category));
        class_304 openKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.inner_dimension.toggle_open_projection", class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_C, category));
        DomainWheelInput.register(key,openKey);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ProjectionSyncClient.tick(client);
            RoomProjectionRenderer.tickEffects(client);
            ProjectionDiagnostics.tick(client);
            DomainWheelInput.tick(client);
        });
    }
}
