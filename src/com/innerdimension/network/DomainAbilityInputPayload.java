package com.innerdimension.network;

import com.innerdimension.domain.runtime.DomainAbilityInput;
import com.innerdimension.domain.runtime.DomainRuntime;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

/**
 * Client intent for an owner-driven ability. The client never decides an outcome: it only
 * reports "I double-tapped W/A/S/D" (D06), and the
 * server re-validates the session, the loadout, the player state and the cooldown before
 * the intent reaches the ability (see {@link DomainAbilityInput}).
 */
public record DomainAbilityInputPayload(int action, int direction) implements class_8710 {
    /** D06 goat double-tap dash; {@code direction} 0..3 = W/A/S/D. */
    public static final int ACTION_DASH = 0;
    /** D41 phantom glide toggle; {@code direction} is unused. */
    public static final int ACTION_GLIDE = 1;
    public static final int ACTION_SLASH = 2;

    /** Matches the D06 blueprint cooldown; the session timer is armed on acceptance. */
    public static final int DASH_COOLDOWN_TICKS = 40;
    /** Session cooldown key D06 shares with this input gate. */
    public static final String DASH_COOLDOWN_KEY = "ability.d06.dash";

    public static final class_8710.class_9154<DomainAbilityInputPayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "domain_ability_input"));
    public static final class_9139<class_9129, DomainAbilityInputPayload> CODEC = class_9139.method_56437(
            (buf, p) -> {
                buf.writeInt(p.action());
                buf.writeInt(p.direction());
            },
            buf -> new DomainAbilityInputPayload(buf.readInt(), buf.readInt()));

    @Override
    public class_8710.class_9154<DomainAbilityInputPayload> method_56479() {
        return ID;
    }

    /**
     * Server receiver. Everything is validated here, on the server thread, before the intent
     * is stored; a rejected intent changes no game state and only produces one status line.
     */
    public static void receive(class_3222 player, DomainAbilityInputPayload payload) {
        if (player == null || payload == null) return;
        if(com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(player)||com.innerdimension.voiddomain.RitualControl.locked(player))return;
        if(com.innerdimension.domain.DomainContest.suppressed(player.method_5667()))return;
        DomainSession session = DomainSessionIndex.byOwner(player.method_5667());
        if (session == null || session.ended() || !session.stable() || !player.method_5805()
                || session.world != player.method_73183()) {
            reject(player, "inner_dimension.ability.input.rejected");
            return;
        }
        long now = DomainRuntime.now();
        if(payload.action()==ACTION_SLASH){com.innerdimension.domain.ability.D44.fire(session,player,now);return;}
        if (payload.action() == ACTION_DASH) {
            if (!session.hasCode("D06")) {
                reject(player, "inner_dimension.ability.input.rejected");
                return;
            }
            if (payload.direction() < 0 || payload.direction() > 3 || !player.method_5624()) {
                reject(player, "inner_dimension.ability.d06.blocked");
                return;
            }
            long remaining = session.remainingSelf(DASH_COOLDOWN_KEY, DASH_COOLDOWN_TICKS, now);
            if (remaining > 0) {
                reject(player, "inner_dimension.ability.d06.cooldown", Long.toString(remaining));
                return;
            }
            session.armSelf(DASH_COOLDOWN_KEY, now);
            DomainAbilityInput.submitDash(player, payload.direction());
        } else {
            reject(player, "inner_dimension.ability.input.rejected");
        }
    }

    /** Action-bar feedback; ids use {@code %s} so the same key serves every Minecraft locale. */
    private static void reject(class_3222 player, String key, Object... args) {
        player.method_7353(class_2561.method_43469(key, args), true);
    }
}

