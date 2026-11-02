package com.innerdimension.client.domain;

import com.innerdimension.network.DomainAbilityInputPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_10185;
import net.minecraft.class_310;
import net.minecraft.class_746;

/**
 * Owner input for the goat dash gesture:
 *
 * <ul>
 *   <li>D06 goat dash - the same movement key tapped twice within 250 ms while sprinting;</li>

 * </ul>
 *
 * <p>The client only reports the gesture. Direction, cooldown, sprint state and every other
 * eligibility rule are re-checked on the server ({@code DomainAbilityInputPayload.receive}).</p>
 *
 * <p>Key state is read from the vanilla {@code PlayerInput} snapshot
 * ({@code class_10185}: forward/backward/left/right/jump/sneak/sprint) instead of consuming the
 * key bindings, so nothing else that polls those keys this tick is disturbed.</p>
 */
public final class DomainAbilityInputClient {
    /** 250 ms of double-tap window at 20 ticks per second. */
    private static final int DOUBLE_TAP_TICKS = 5;
    private static final int NO_PRESS = Integer.MIN_VALUE / 2;

    /** 0 = W, 1 = A, 2 = S, 3 = D - the same order the payload uses. */
    private static final int DIRECTIONS = 4;

    private static final int[] LAST_PRESS_TICK = new int[DIRECTIONS];
    private static final boolean[] WAS_PRESSED = new boolean[DIRECTIONS];

    private static long ticks;

    static {
        reset();
    }

    private DomainAbilityInputClient() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(DomainAbilityInputClient::tick);
    }

    private static void tick(class_310 client) {
        ticks++;
        class_746 player = client.field_1724;
        if (player == null || client.field_1687 == null || client.field_1755 != null
                || player.field_3913 == null) {
            reset();
            return;
        }
        if(DomainClientState.d44Available()&&client.field_1690.field_1886.method_1434())send(DomainAbilityInputPayload.ACTION_SLASH,0);
        class_10185 input = player.field_3913.field_54155;
        if (input == null) {
            reset();
            return;
        }
        boolean[] pressed = {
                input.comp_3159(),   // W
                input.comp_3161(),   // A
                input.comp_3160(),   // S
                input.comp_3162()};  // D
        boolean sprinting = player.method_5624();
        for (int direction = 0; direction < DIRECTIONS; direction++) {
            if (pressed[direction] && !WAS_PRESSED[direction]) {
                int lastPress = LAST_PRESS_TICK[direction];
                boolean doubleTap = lastPress != NO_PRESS && ticks - lastPress <= DOUBLE_TAP_TICKS;
                // Only offer the gesture when the running domain actually engraved D06. Without
                // this gate a plain sprint double-tap (or any mid-air jump below) sends an intent
                // the server has to refuse, and the owner sees "this ability is unavailable"
                // over and over during ordinary movement.
                if (doubleTap && sprinting && DomainClientState.d06Available()) {
                    send(DomainAbilityInputPayload.ACTION_DASH, direction);
                    LAST_PRESS_TICK[direction] = NO_PRESS;
                } else {
                    LAST_PRESS_TICK[direction] = (int) ticks;
                }
            }
            WAS_PRESSED[direction] = pressed[direction];
        }

    }

    private static void send(int action, int direction) {
        if (!ClientPlayNetworking.canSend(DomainAbilityInputPayload.ID)) return;
        ClientPlayNetworking.send(new DomainAbilityInputPayload(action, direction));
    }

    /** Forgets every edge; used whenever the client cannot own an input this tick. */
    private static void reset() {
        for (int direction = 0; direction < DIRECTIONS; direction++) {
            LAST_PRESS_TICK[direction] = NO_PRESS;
            WAS_PRESSED[direction] = false;
        }

    }
}

