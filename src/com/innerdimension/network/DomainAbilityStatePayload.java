package com.innerdimension.network;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import com.innerdimension.domain.ability.D06;
import com.innerdimension.domain.ability.D10;
import com.innerdimension.domain.ability.D40;
import com.innerdimension.domain.ability.D41;
import com.innerdimension.domain.ability.D49;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Owner-only snapshot of the domain ability state. It carries exactly what the owner's own
 * client cannot derive locally: ability cooldowns, the D10 ambush state machine, the D49
 * remaining uses, the D41 virtual glide flag and the D19/D22 outline targets.
 *
 * <p>Nothing here is authority: every bit is produced by the server side of an ability and
 * only drives presentation (HUD, outlines, glide prediction). Anything that decides an
 * outcome is validated again on the server (see {@link DomainAbilityInputPayload}).</p>
 *
 * <p>Outline collections carry positions and entity ids only - never container contents.</p>
 */
public record DomainAbilityStatePayload(int flags, int dashCooldownTicks, int dodgeCooldownTicks,
                                        int remainingUses, List<Integer> outlinedEntityIds,
                                        List<class_2338> outlinedContainers) implements class_8710 {
    /** bit 0 - D41 virtual glide is active for the owner. */
    public static final int FLAG_GLIDE_ACTIVE = 1;
    /** bit 1 - D06 dash cooldown is running (show it in the HUD). */
    public static final int FLAG_DASH_COOLDOWN = 1 << 1;
    /** bit 2 - D10 ambush is READY. */
    public static final int FLAG_D10_READY = 1 << 2;
    /** bit 3 - D10 ambush is CHARGING. */
    public static final int FLAG_D10_CHARGING = 1 << 3;
    /** bit 4 - D10 ambush is on COOLDOWN. */
    public static final int FLAG_D10_COOLDOWN = 1 << 4;
    /** bit 5 - D40 dodge is on cooldown (show it in the HUD). */
    public static final int FLAG_DODGE_COOLDOWN = 1 << 5;
    /** bit 6 - D49 has already been used in this expansion. */
    public static final int FLAG_D49_USED = 1 << 6;
    /** bit 7 - D43 uses the same halved vanilla crossbow timer on this client. */
    public static final int FLAG_D43_FAST_CHARGE = 1 << 7;
    /** bit 8 - D06 is engraved in the running session's loadout (the owner may dash). */
    public static final int FLAG_D06_AVAILABLE = 1 << 8;
    /** bit 9 - D41 is engraved in the running session's loadout (the owner may glide). */
    public static final int FLAG_D41_AVAILABLE = 1 << 9;

    public static final int FLAG_D52_AVAILABLE = 1 << 10;
    public static final int FLAG_D44_AVAILABLE = 1 << 11;

    /** Hard caps: a malformed or hostile payload must never allocate without bound. */
    public static final int MAX_ENTITIES = 256;
    public static final int MAX_CONTAINERS = 512;

    public static final class_8710.class_9154<DomainAbilityStatePayload> ID =
            new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "domain_ability_state"));
    public static final class_9139<class_9129, DomainAbilityStatePayload> CODEC = class_9139.method_56437(
            (buf, p) -> {
                buf.writeInt(p.flags());
                buf.writeInt(p.dashCooldownTicks());
                buf.writeInt(p.dodgeCooldownTicks());
                buf.writeInt(p.remainingUses());
                List<Integer> ids = p.outlinedEntityIds();
                int idCount = Math.min(ids.size(), MAX_ENTITIES);
                buf.writeInt(idCount);
                for (int i = 0; i < idCount; i++) buf.writeInt(ids.get(i));
                List<class_2338> containers = p.outlinedContainers();
                int containerCount = Math.min(containers.size(), MAX_CONTAINERS);
                buf.writeInt(containerCount);
                for (int i = 0; i < containerCount; i++) {
                    class_2338 pos = containers.get(i);
                    buf.writeInt(pos.method_10263());
                    buf.writeInt(pos.method_10264());
                    buf.writeInt(pos.method_10260());
                }
            },
            buf -> {
                int flags = buf.readInt();
                int dashCooldownTicks = buf.readInt();
                int dodgeCooldownTicks = buf.readInt();
                int remainingUses = buf.readInt();
                int idCount = clamp(buf.readInt(), 0, MAX_ENTITIES);
                List<Integer> ids = new ArrayList<>(idCount);
                for (int i = 0; i < idCount; i++) ids.add(buf.readInt());
                int containerCount = clamp(buf.readInt(), 0, MAX_CONTAINERS);
                List<class_2338> containers = new ArrayList<>(containerCount);
                for (int i = 0; i < containerCount; i++) {
                    containers.add(new class_2338(buf.readInt(), buf.readInt(), buf.readInt()));
                }
                return new DomainAbilityStatePayload(flags, dashCooldownTicks, dodgeCooldownTicks,
                        remainingUses, ids, containers);
            });

    public DomainAbilityStatePayload {
        outlinedEntityIds = List.copyOf(trim(outlinedEntityIds, MAX_ENTITIES));
        outlinedContainers = List.copyOf(trim(outlinedContainers, MAX_CONTAINERS));
    }

    @Override
    public class_8710.class_9154<DomainAbilityStatePayload> method_56479() {
        return ID;
    }

    /* ------------------------------------------------------------------- flags */

    public boolean glideActive() { return has(FLAG_GLIDE_ACTIVE); }
    public boolean dashCooldownShown() { return has(FLAG_DASH_COOLDOWN); }
    public boolean d10Ready() { return has(FLAG_D10_READY); }
    public boolean d10Charging() { return has(FLAG_D10_CHARGING); }
    public boolean d10Cooldown() { return has(FLAG_D10_COOLDOWN); }
    public boolean dodgeCooldownShown() { return has(FLAG_DODGE_COOLDOWN); }
    public boolean d49Used() { return has(FLAG_D49_USED); }
    public boolean d43FastCharge() { return has(FLAG_D43_FAST_CHARGE); }
    /** True while the session loadout carries D06, i.e. the dash gesture can be accepted. */
    public boolean d06Available() { return has(FLAG_D06_AVAILABLE); }
    /** True while the session loadout carries D41, i.e. the glide gesture can be accepted. */
    public boolean d41Available() { return has(FLAG_D41_AVAILABLE); }

    public boolean has(int mask) { return (flags & mask) != 0; }

    /* ----------------------------------------------------------------- sending */

    /**
     * Sends this snapshot to its owner. The channel is only used once the client has
     * negotiated it; an unsent state must never be treated as delivered.
     */
    public static void send(class_3222 player, DomainAbilityStatePayload payload) {
        if (player == null || payload == null) return;
        if (!ServerPlayNetworking.canSend(player, ID)) return;
        ServerPlayNetworking.send(player, payload);
    }

    /** Empty snapshot: tells the owner's client that every ability presentation ended. */
    public static DomainAbilityStatePayload empty() {
        return new DomainAbilityStatePayload(0, 0, 0, 0, List.of(), List.of());
    }

    /* --------------------------------------------------------- server merge */

    private record Snapshot(long sessionKey, DomainAbilityStatePayload payload) {}

    /** Last full snapshot per owner, so one ability can update its part without clearing others. */
    private static final Map<UUID, Snapshot> LAST = new HashMap<>();

    /**
     * Publishes the D19/D22 outline sets for one owner, keeping every other field of that
     * owner's current snapshot (cooldowns, the D10 state machine, the D41 glide flag).
     *
     * <p>{@code sessionKey} is the {@code sessionId} fingerprint the ability obtained from its
     * own session; an update whose key is not the owner's live session is ignored, so a late
     * {@code onEnd} clear from an older expansion can never wipe the outlines of a new one.</p>
     */
    public static synchronized void outline(class_3222 owner, long sessionKey, List<Integer> entityIds,
                                            List<class_2338> containers) {
        if (owner == null) return;
        UUID id = owner.method_5667();
        DomainSession current = DomainSessionIndex.byOwner(id);
        if (current == null) {
            LAST.remove(id);
            return;
        }
        if (sessionKeyOf(current) != sessionKey) return;
        Snapshot previous = LAST.get(id);
        DomainAbilityStatePayload base = previous != null && previous.sessionKey() == sessionKey
                ? previous.payload() : empty();
        DomainAbilityStatePayload next = new DomainAbilityStatePayload(base.flags(),
                base.dashCooldownTicks(), base.dodgeCooldownTicks(), base.remainingUses(),
                entityIds, containers);
        remember(id, new Snapshot(sessionKey, next));
        send(owner, next);
    }

    /**
     * Publishes the HUD/cooldown fields for one owner, keeping that owner's current D19/D22
     * outline sets. {@code flags} is a {@code FLAG_*} mask; the D06/D10/D40/D49/D41 producers
     * call this once per change and send nothing when nothing changed.
     */
    public static synchronized void state(class_3222 owner, long sessionKey, int flags,
                                          int dashCooldownTicks, int dodgeCooldownTicks, int remainingUses) {
        if (owner == null) return;
        UUID id = owner.method_5667();
        DomainSession current = DomainSessionIndex.byOwner(id);
        if (current == null) {
            LAST.remove(id);
            return;
        }
        if (sessionKeyOf(current) != sessionKey) return;
        Snapshot previous = LAST.get(id);
        DomainAbilityStatePayload base = previous != null && previous.sessionKey() == sessionKey
                ? previous.payload() : empty();
        DomainAbilityStatePayload next = new DomainAbilityStatePayload(flags, dashCooldownTicks,
                dodgeCooldownTicks, remainingUses, base.outlinedEntityIds(), base.outlinedContainers());
        if (previous != null && previous.sessionKey() == sessionKey && previous.payload().equals(next)) return;
        remember(id, new Snapshot(sessionKey, next));
        send(owner, next);
    }

    /** Builds the complete owner HUD/glide snapshot after every ability has ticked. */
    public static void publish(DomainSession session, long now) {
        if (session == null || session.ended()) return;
        class_3222 owner = session.owner();
        if (owner == null) return;
        int flags = 0;
        int dash = 0;
        int dodge = 0;
        if(session.hasCode("D44")&&session.contains(owner))flags|=FLAG_D44_AVAILABLE;
        int uses = session.hasCode("D49") && !D49.used(session) ? 1 : 0;
        if (session.hasCode("D52") && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D52",owner)) flags |= FLAG_D52_AVAILABLE;
        if (D41.available(session, owner)) {
            flags |= FLAG_D41_AVAILABLE;
            if (D41.isGliding(session)) flags |= FLAG_GLIDE_ACTIVE;
        }
        if (session.hasCode("D06")) {
            flags |= FLAG_D06_AVAILABLE;
            dash = (int) Math.min(Integer.MAX_VALUE, D06.remainingCooldown(session, now));
            if (dash > 0) flags |= FLAG_DASH_COOLDOWN;
        }
        if (session.hasCode("D10")) {
            switch (D10.stateOf(session)) {
                case "READY" -> flags |= FLAG_D10_READY;
                case "CHARGING" -> flags |= FLAG_D10_CHARGING;
                case "COOLDOWN" -> flags |= FLAG_D10_COOLDOWN;
                default -> { }
            }
        }
        if (session.hasCode("D40")) {
            dodge = (int) Math.min(Integer.MAX_VALUE, D40.remainingCooldown(session, now));
            if (dodge > 0) flags |= FLAG_DODGE_COOLDOWN;
        }
        if (session.hasCode("D49") && D49.used(session)) flags |= FLAG_D49_USED;
        if (session.hasCode("D43")) flags |= FLAG_D43_FAST_CHARGE;
        state(owner, sessionKeyOf(session), flags, dash, dodge, uses);
        if(now%20==0){var snapshot=LAST.get(session.ownerId);if(snapshot!=null)send(owner,snapshot.payload());}
    }

    /** Sends the terminal empty snapshot before the session is removed from the live index. */
    public static synchronized void end(DomainSession session) {
        if (session == null) return;
        UUID id = session.ownerId;
        class_3222 owner = session.world.method_8503().method_3760().method_14602(id);
        Snapshot previous = LAST.get(id);
        if (previous != null && previous.sessionKey() == sessionKeyOf(session)) {
            if (owner != null) send(owner, empty());
            LAST.remove(id);
        }
    }

    /** Forgets one owner's cached snapshot (disconnect / server stop). */
    public static synchronized void forget(UUID owner) {
        if (owner != null) LAST.remove(owner);
    }

    public static synchronized void clear() {
        LAST.clear();
    }

    /** The same fingerprint {@code D01D23Support.sessionKey} publishes for a session. */
    public static long sessionKeyOf(DomainSession session) {
        return session.sessionId.getMostSignificantBits() ^ session.sessionId.getLeastSignificantBits();
    }

    /** Keeps the cache from growing with owners that left while an ability was running. */
    private static void remember(UUID id, Snapshot snapshot) {
        if (LAST.size() > 64 && !LAST.containsKey(id)) {
            LAST.entrySet().removeIf(entry -> DomainSessionIndex.byOwner(entry.getKey()) == null);
        }
        if (LAST.size() > 512) LAST.clear();
        LAST.put(id, snapshot);
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    private static <T> List<T> trim(List<T> source, int max) {
        if (source == null || source.isEmpty()) return List.of();
        return source.size() <= max ? source : new ArrayList<>(source.subList(0, max));
    }
}

