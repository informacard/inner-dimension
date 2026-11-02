package com.innerdimension.domain;

import com.innerdimension.domain.ai.DomainAiPolicy;
import com.innerdimension.domain.combat.DomainAttackPipeline;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainRuntime;
import com.innerdimension.projection.RoomProjectionManager;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;

/**
 * Registration and scheduling facade for the domain ability layer.
 *
 * <p>It owns nothing: every ability lives in {@code domain/ability}, every pipeline in
 * {@code domain/combat} and every lease in {@code domain/runtime}. The two entry points are
 * kept because {@link com.innerdimension.InnerDimension} and
 * {@link RoomProjectionManager#tick} call them.</p>
 */
public final class DomainAbilityEngine {
    private DomainAbilityEngine() {}

    public static void register() {
        DomainRuntime.register();
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(com.innerdimension.network.UnlimitedVoidPayload.ID,com.innerdimension.network.UnlimitedVoidPayload.CODEC);
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.method_8608() || player.method_7325()) return class_1269.field_5811;
            if (!(player instanceof class_3222 owner)) return class_1269.field_5811;
            var session = com.innerdimension.domain.runtime.DomainSessionIndex.byOwner(owner.method_5667());
            if (session == null || !session.stable() || session.world != world || !session.hasCode("D04"))
                return class_1269.field_5811;
            return com.innerdimension.domain.ability.D04.INSTANCE.onOwnerUse(session, owner, target)
                    ? class_1269.field_5812 : class_1269.field_5811;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if(com.innerdimension.domain.ability.D77.shell(target))return class_1269.field_5812;
            if (world.method_8608() || hand != class_1268.field_5808) return class_1269.field_5811;
            if (!(player instanceof class_3222 attacker) || !(target instanceof class_1309 living)) return class_1269.field_5811;
            com.innerdimension.domain.runtime.DomainObjects.attacked(attacker,living);
            // D04 animation replacement / D10 ambush / D37 / D58 / D09 all live in the pipeline.
            if (DomainAttackPipeline.handleMelee(attacker, living)) return class_1269.field_5812;
            // D53 golden truce refuses a non-player living attacker. Fabric's AttackEntityCallback
            // only ever fires for players, so this branch is unreachable from here; mob melee is
            // refused through DomainAiPolicy.denyTarget and the damage pipeline instead.
            if (!(attacker instanceof class_1657) && DomainAiPolicy.denyAttack(attacker, living)) return class_1269.field_5812;
            return class_1269.field_5811;
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity,source,amount)->!com.innerdimension.domain.ability.D77.shell(entity)&&DomainDamagePipeline.pre(entity,source,amount));
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if(damageTaken>0&&source!=null&&source.method_5529()!=null)DebugDomain.attacked(source.method_5529(),entity);
            DomainDamagePipeline.after(entity, source, baseDamageTaken, damageTaken, blocked);
            DomainAttackPipeline.afterMelee(entity, source, damageTaken);
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register(DomainDamagePipeline::allowDeath);
    }

    /**
     * Called from {@link RoomProjectionManager#tick} on the server thread after the
     * projection manager has updated its own state. END_SERVER_TICK registration is
     * deliberately not duplicated here.
     */
    public static void tick(MinecraftServer server, List<RoomProjectionManager.AbilityView> views) {
        DomainRuntime.tick(server);
    }
}
