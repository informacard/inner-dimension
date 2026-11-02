package com.innerdimension.domain.runtime;

import com.innerdimension.domain.ability.DomainAbility;
import com.innerdimension.domain.ability.DomainAbilities;
import com.innerdimension.domain.ability.D16;
import com.innerdimension.domain.ability.D20;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.network.DomainAbilityStatePayload;
import com.innerdimension.projection.ProjectionAnimation;
import com.innerdimension.projection.RoomProjectionEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.*;

import java.util.*;

/**
 * Lifecycle owner for {@link DomainSession}. One session exists between the moment a
 * closed projection reaches STABLE, or an open projection starts expanding, and its cleanup (or its owner dies, logs
 * out, changes world, or the projection is removed). Everything the ability layer leased
 * is revoked exactly once, here.
 */
public final class DomainRuntime {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("InnerDimension");
    private static boolean registered;
    private static long tick;

    private DomainRuntime() {}

    public static void register() {
        if (registered) return;
        registered = true;
        DomainAbilities.registerAll();
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> clear());
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {UnlimitedVoidControl.disconnected(handler.field_14140.method_5667());end(handler.field_14140.method_5667());});
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            com.innerdimension.progression.NewDomainAdvancements.died(entity,source);
            com.innerdimension.progression.VillagerSoulChallenge.died(entity,source);
            D20.onDeath(entity, source);
            com.innerdimension.domain.ability.D78.releaseAll(entity);
            end(entity.method_5667());
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.COPY_FROM.register(D16::copyOnRespawn);
    }

    /** Called once at ability activation; open domains activate at expansion start. */
    public static synchronized DomainSession stable(UUID owner, class_1937 world,
                                                    RoomProjectionEntity shell, RoomProjectionEntity interior,
                                                    List<String> abilities) {
        return stable(owner,world,shell,interior,abilities,-1);
    }
    public static synchronized DomainSession stable(UUID owner, class_1937 world,
                                                    RoomProjectionEntity shell, RoomProjectionEntity interior,
                                                    List<String> abilities,int expansionSlot) {
        end(owner);
        DomainSession session = new DomainSession(owner, world, shell, interior, abilities,expansionSlot);
        session.capture(tick);
        DomainSessionIndex.add(session);
        activate(session);
        return session;
    }

    private static void activate(DomainSession session) {
        if(com.innerdimension.domain.DomainContest.suppressed(session.ownerId)||!session.once("runtime_activated"))return;
        var owner=session.owner();
        if(owner!=null)try { com.innerdimension.pregnancy.MentalPregnancySystem.tryStart(owner,session.loadout); }
        catch(RuntimeException failure) { LOGGER.warn("Mental pregnancy activation failed for domain {}",session.ownerId,failure); }
        for (DomainAbility ability : DomainAbilities.of(session)) {
            try {
                ability.onStable(session);
            } catch (RuntimeException ignored) {
                // One ability failing must not prevent the rest of the loadout.
            }
        }
    }

    /** Ends the owner's session, if any, and revokes everything it leased. */
    public static synchronized void end(UUID owner) {
        DomainSession session = DomainSessionIndex.byOwner(owner);
        if (session == null || session.ended()) return;
        endSession(session);
    }

    public static synchronized void endSession(DomainSession session) {
        if (session.ended()) return;
        session.markEnded();
        for (DomainAbility ability : DomainAbilities.of(session)) {
            try {
                ability.onEnd(session);
            } catch (RuntimeException ignored) {
                // Cleanup continues regardless.
            }
        }
        EffectLeaseManager.revokeSession(session.sessionId, tick);
        AttributeLeaseManager.revokeSession(session.sessionId);
        DomainAbilityStatePayload.end(session);
        DomainSessionIndex.remove(session);
        DomainAbilities.forget(session);
    }

    /** Once per server tick, after the projection manager has updated its own state. */
    public static synchronized void tick(MinecraftServer server) {
        long performanceStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        tick++;
        DomainDamagePipeline.advance();
        EffectLeaseManager.tickExternal();
        for (DomainSession session : new ArrayList<>(DomainSessionIndex.sessions())) {
            if (session.ended()) continue;
            if (!session.stable()) {
                if (!session.phaseActive()) endSession(session);
                continue;
            }
            class_3222 owner = session.owner();
            if (owner == null || !session.valid() || !session.phaseActive()) {
                endSession(session);
                continue;
            }
            if(com.innerdimension.domain.DomainContest.suppressed(session.ownerId)){
                com.innerdimension.domain.ability.D77.INSTANCE.onTick(session,tick);
                com.innerdimension.domain.ability.D78.INSTANCE.onTick(session,tick);
                continue;
            }
            if(session.open()){EffectLeaseManager.revokeOutside(session,tick);AttributeLeaseManager.revokeOutside(session);}
            activate(session);
            for (DomainAbility ability : DomainAbilities.of(session)) {
                try {
                    ability.onTick(session, tick);
                } catch (RuntimeException failure) {
                    // A single ability must never take down the tick loop, but silently swallowing
                    // the failure makes a broken ability look like an ability that does nothing.
                    report(session, ability, failure);
                }
            }
            DomainAbilityStatePayload.publish(session, tick);
        }
        UnlimitedVoidControl.tick(tick);
        } finally {com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.ABILITY_TICK,performanceStart);}
    }

    private static List<DomainSession> allSessions() {
        return DomainSessionIndex.sessions();
    }

    /**
     * Logs the first failure of one ability inside one expansion, with its stack trace, and
     * stays quiet afterwards: a throw here used to be invisible, which made "the ability does
     * nothing" impossible to tell apart from "the ability threw every tick".
     */
    private static void report(DomainSession session, DomainAbility ability, RuntimeException failure) {
        if (!session.once("runtime_failure_" + ability.code())) return;
        LOGGER.warn("{} failed in domain {} (first occurrence this expansion)",
                ability.code(), session.ownerId, failure);
    }

    public static List<DomainSession> sessionsIn(class_1937 world) { return DomainSessionIndex.all(world); }

    public static long now() { return tick; }

    public static synchronized void clear() {
        for (DomainSession session : allSessions()) endSession(session);
        DomainSessionIndex.clear();
        EffectLeaseManager.clear();
        AttributeLeaseManager.clear();
        DomainDamagePipeline.clear();
        DomainAbilities.clear();
        DomainAbilityStatePayload.clear();
        UnlimitedVoidControl.clear();
        tick = 0;
    }
}
