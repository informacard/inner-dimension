package com.innerdimension.security;

import com.innerdimension.core.ServerClock;
import com.innerdimension.core.Tuning;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.class_1799;
import net.minecraft.class_3222;
import net.minecraft.class_3545;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Runtime-only guards against chastity-belt entry and self-sustaining teleport loops. */
public final class EntrySafetyGuard {
    public static final String MSG_BELT = "inner_dimension.entry.blocked_by_belt";
    public static final String MSG_DILDO_LOOP = "inner_dimension.entry.dildo_loop_guard";
    public static final String MSG_PLAYER_LOOP = "inner_dimension.entry.player_loop_guard";

    private record Activity(long lastAttempt, int count, boolean disabled) {}

    private static final Map<UUID, Activity> DILDO_ACTIVITY = new HashMap<>();
    private static final Map<UUID, Activity> PLAYER_ACTIVITY = new HashMap<>();
    private static final Map<UUID, Long> LAST_LEAVE = new HashMap<>();

    private EntrySafetyGuard() {}

    /** True only for an A or A/V protector in an actually equipped Trinkets slot. */
    public static boolean wearsEntryBlockingBelt(class_3222 player) {
        if (player == null) return false;
        var component = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (component == null) return false;
        for (class_3545<?, ?> equipped : component.getAllEquipped()) {
            Object value = equipped.method_15441();
            if (value instanceof class_1799 stack && DollSecurityManager.isAcceptedBelt(stack)) {
                return true;
            }
        }
        return false;
    }

    /** Checks the target player's personal-room restrictions before any entry-side effects. */
    public static boolean allowPlayerEntry(class_3222 player, UUID roomOwner) {
        if (player == null || roomOwner == null || !roomOwner.equals(player.method_5667())) return true;
        if (wearsEntryBlockingBelt(player)) {
            DollSecurityManager.send(player, MSG_BELT);
            return false;
        }

        UUID id = player.method_5667();
        long now = ServerClock.now();
        Activity old = PLAYER_ACTIVITY.get(id);
        if (old != null && old.disabled()) {
            if (now - old.lastAttempt() < Tuning.ENTRY_LOOP_QUIET_TICKS) {
                PLAYER_ACTIVITY.put(id, new Activity(now, old.count(), true));
                DollSecurityManager.send(player, MSG_PLAYER_LOOP);
                return false;
            }
            PLAYER_ACTIVITY.remove(id);
            old = null;
        }

        Long leftAt = LAST_LEAVE.get(id);
        boolean rapidReturn = leftAt != null && now - leftAt <= Tuning.RAPID_RETURN_TICKS;
        if (!rapidReturn) return true;

        int count = old != null && now - old.lastAttempt() <= Tuning.ENTRY_LOOP_WINDOW_TICKS
                ? old.count() + 1 : 1;
        boolean disable = count >= Tuning.ENTRY_LOOP_THRESHOLD;
        PLAYER_ACTIVITY.put(id, new Activity(now, count, disable));
        if (disable) {
            DollSecurityManager.send(player, MSG_PLAYER_LOOP);
            return false;
        }
        return true;
    }

    /** Called for each player-carrying feed decision, including attempts made while disabled. */
    public static boolean allowDildoPlayerFeed(UUID dildoUuid, class_3222 player) {
        if (dildoUuid == null) return true;
        long now = ServerClock.now();
        Activity old = DILDO_ACTIVITY.get(dildoUuid);
        if (old != null && old.disabled()) {
            if (now - old.lastAttempt() < Tuning.ENTRY_LOOP_QUIET_TICKS) {
                DILDO_ACTIVITY.put(dildoUuid, new Activity(now, old.count(), true));
                DollSecurityManager.send(player, MSG_DILDO_LOOP);
                return false;
            }
            old = null;
        }
        int count = old != null && now - old.lastAttempt() <= Tuning.ENTRY_LOOP_WINDOW_TICKS
                ? old.count() + 1 : 1;
        boolean disable = count >= Tuning.ENTRY_LOOP_THRESHOLD;
        DILDO_ACTIVITY.put(dildoUuid, new Activity(now, count, disable));
        if (disable) DollSecurityManager.send(player, MSG_DILDO_LOOP);
        return !disable;
    }

    /** A successful exit arms the rapid-return detector; failed exit attempts do not. */
    public static void noteSuccessfulLeave(UUID playerUuid) {
        if (playerUuid != null) LAST_LEAVE.put(playerUuid, ServerClock.now());
    }

    public static void reset() {
        DILDO_ACTIVITY.clear();
        PLAYER_ACTIVITY.clear();
        LAST_LEAVE.clear();
    }
}
