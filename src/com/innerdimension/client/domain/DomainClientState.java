package com.innerdimension.client.domain;

import com.innerdimension.network.DomainAbilityStatePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_2338;
import net.minecraft.class_310;

import java.util.List;

/**
 * Client-side mirror of the last {@link DomainAbilityStatePayload} the owner received.
 *
 * <p>This is presentation state only. It is replaced wholesale by every incoming snapshot
 * (the server always sends a complete picture, never a delta), and it is dropped as soon as
 * the receiver leaves the world so no cooldown, outline or glide flag can leak into the next
 * connection.</p>
 */
public final class DomainClientState {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("InnerDimension");

    private static volatile DomainAbilityStatePayload state;
    /** One line per connection, so a missing HUD element can be traced to a missing payload. */
    private static boolean loggedFirstFlags;
    private static int snapshotAge;
    private static net.minecraft.class_1937 snapshotWorld;

    private DomainClientState() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(DomainAbilityStatePayload.ID,
                (payload, context) -> context.client().execute(() -> accept(payload)));
        // Fabric can invoke DISCONNECT on Netty's IO thread; holding a payload there is harmless,
        // but clearing from the client thread keeps every read/write of the field on one thread.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(DomainClientState::clear));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.field_1687 == null || client.field_1724 == null) clear();
            else if(state!=null&&(client.field_1687!=snapshotWorld||++snapshotAge>40))state=null;
        });
    }

    /** Replaces the snapshot. Called on the client thread. */
    public static void accept(DomainAbilityStatePayload payload) {
        state = payload;
        snapshotAge=0;snapshotWorld=class_310.method_1551().field_1687;
        if (!loggedFirstFlags && payload != null && payload.flags() != 0) {
            loggedFirstFlags = true;
            LOGGER.info("client ability state: flags=0x{} d10(charging/ready/cooldown)={}/{}/{} glideActive={}",
                    Integer.toHexString(payload.flags()), payload.d10Charging(), payload.d10Ready(),
                    payload.d10Cooldown(), payload.glideActive());
        }
    }

    /** The last snapshot, or null before the first one / after the world was left. */
    public static DomainAbilityStatePayload state() {
        return state;
    }

    public static void clear() {
        DomainObjectClientState.clear();
        state = null;
        loggedFirstFlags = false;
    }

    /* ------------------------------------------------------- convenience readers */

    public static boolean glideActive() {
        DomainAbilityStatePayload current = state;
        return current != null && current.glideActive();
    }

    public static boolean d43FastCharge() {
        DomainAbilityStatePayload current = state;
        return current != null && current.d43FastCharge();
    }

    /**
     * True while the running session's loadout carries D06. The client only offers the dash
     * gesture when this is set, so an ordinary sprint double-tap outside a domain never asks
     * the server for something it must refuse.
     */
    public static boolean d06Available() {
        DomainAbilityStatePayload current = state;
        return current != null && current.d06Available();
    }

    /** True while the running session's loadout carries D41; gates the glide gesture. */
    public static boolean d41Available() {
        DomainAbilityStatePayload current = state;
        return current != null && current.d41Available();
    }

    public static boolean d44Available(){return (flags()&DomainAbilityStatePayload.FLAG_D44_AVAILABLE)!=0;}
    public static int flags() {
        DomainAbilityStatePayload current = state;
        return current == null ? 0 : current.flags();
    }

    public static int dashCooldownTicks() {
        DomainAbilityStatePayload current = state;
        return current == null ? 0 : current.dashCooldownTicks();
    }

    public static int dodgeCooldownTicks() {
        DomainAbilityStatePayload current = state;
        return current == null ? 0 : current.dodgeCooldownTicks();
    }

    public static int remainingUses() {
        DomainAbilityStatePayload current = state;
        return current == null ? 0 : current.remainingUses();
    }

    public static List<Integer> outlinedEntityIds() {
        DomainAbilityStatePayload current = state;
        return current == null ? List.of() : current.outlinedEntityIds();
    }

    public static List<class_2338> outlinedContainers() {
        DomainAbilityStatePayload current = state;
        return current == null ? List.of() : current.outlinedContainers();
    }

    /** The client player, or null when there is no world. */
    public static class_310 client() {
        return class_310.method_1551();
    }
}
