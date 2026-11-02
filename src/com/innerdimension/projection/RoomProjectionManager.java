package com.innerdimension.projection;

import com.innerdimension.InnerDimension;
import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.dimension.RoomGeometry;
import com.innerdimension.dimension.RoomShellGenerator;
import com.innerdimension.transport.SafeEntityTeleport;
import com.innerdimension.state.InnerState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2378;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RoomProjectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension.projection");
    public static class_1299<RoomProjectionEntity> ENTITY_TYPE;
    private static final class Domain {
        final RoomProjectionEntity shell,interior;
        final class_3222 caster;
        final java.util.List<String> abilities;
        final int expansionSlot;
        final int maximumTicks;
        final boolean reversed;
        ProjectionSafetySearch.Point landing;
        final boolean previousNoGravity;
        final long started=ticks;
        boolean holding;
        double remainingTicks, lifetimeRate=1;
        Domain(RoomProjectionEntity shell,RoomProjectionEntity interior,class_3222 caster,int expansionSlot,boolean open) {
            this.shell=shell; this.interior=interior; this.caster=caster;
            this.expansionSlot=expansionSlot;
            maximumTicks=open?OpenDomainBinding.get(caster.method_51469()).duration(caster.method_5667()):DomainLifetime.MAX_TICKS;
            remainingTicks=maximumTicks;
            reversed=!open&&ClosedDomainBinding.get(caster.method_51469()).forCast(caster.method_5667(),caster.method_5715());
            shell.reversedShell(reversed);interior.reversedShell(reversed);
            var state=com.innerdimension.progression.DomainState.get(caster.method_51469());
            this.abilities=java.util.List.copyOf(state.loadout(caster.method_5667(),expansionSlot));
            previousNoGravity=caster.method_5740();
        }
        RoomProjectionEntity shell() { return shell; }
        RoomProjectionEntity interior() { return interior; }
        boolean open(){var spec=((RoomProjectionContraption)interior.getContraption()).sourceSpec();return spec.shell()==null&&spec.mirror()!=null;}
        void release() { if (holding) { holding=false; caster.method_5875(previousNoGravity); } }
        void remove() { release(); shell.method_31472(); interior.method_31472(); }
        void retract() { release(); shell.retract(); interior.retract(); }
        void pin() {
            if (!holding || !caster.method_5805() || caster.method_73183()!=shell.method_73183()) return;
            caster.method_5875(true);
            caster.method_18799(class_243.field_1353);
            caster.field_6017=0;
            if (Math.abs(caster.method_23317()-landing.x())>1e-6
                    || Math.abs(caster.method_23318()-landing.y())>1e-6
                    || Math.abs(caster.method_23321()-landing.z())>1e-6)
                caster.field_13987.method_14363(landing.x(),landing.y(),landing.z(),caster.method_36454(),caster.method_36455());
        }
    }
    private static final Map<UUID, Domain> ACTIVE = new HashMap<>();
    private static final Set<UUID> PENDING = new LinkedHashSet<>();
    private static final Map<UUID,Integer> PENDING_SLOT=new HashMap<>();
    private static final Set<UUID> PENDING_OPEN = new LinkedHashSet<>();
    private static final Map<UUID, Long> LAST_TOGGLE = new HashMap<>();
    private record DomainReturn(class_5321<class_1937> world, double x, double y, double z,
                                float yaw, float pitch) { }
    private static final Map<UUID, DomainReturn> DOMAIN_RETURNS = new HashMap<>();
    private static long ticks;
    private static final Map<UUID,Long> BURNOUT_UNTIL=new HashMap<>();
    public static int burnoutRemaining(UUID owner) {
        return (int)Math.max(0,BURNOUT_UNTIL.getOrDefault(owner,0L)-ticks);
    }
    public static int domainAge(UUID owner) {
        Domain d=ACTIVE.get(owner);
        return d==null || d.shell.phase()==ProjectionAnimation.RETRACTING ? -1 : (int)Math.max(0,d.maximumTicks-d.remainingTicks);
    }
    public static void suppressForEmbodiment(class_3222 player){var d=ACTIVE.get(player.method_5667());if(d!=null)close(player.method_5667(),d,false);}
    private static void close(UUID owner,Domain d,boolean forced) {
        OpenDomainPerformance.finish(owner);
        com.innerdimension.domain.SimpleDomain.end(owner);
        if(d.shell.phase()==ProjectionAnimation.RETRACTING) return;
        if(!d.open())com.innerdimension.domain.runtime.DomainRuntime.end(owner);
        BURNOUT_UNTIL.put(owner,ticks+(d.open()?OpenDomainBinding.burnout(ticks-d.started,d.maximumTicks,forced):(forced ? DomainLifetime.FORCED_BURNOUT_TICKS : DomainLifetime.burnoutTicks(ticks-d.started))));
        if(forced&&!d.open()){d.release();d.shell.breakApart();d.interior.breakApart();}else d.retract();
    }

    public static void register() {
        ProjectionMirror.register();
        PayloadTypeRegistry.playS2C().register(com.innerdimension.network.DomainContestPayload.ID,com.innerdimension.network.DomainContestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(com.innerdimension.network.DomainHudPayload.ID,com.innerdimension.network.DomainHudPayload.CODEC);
        class_5321<class_1299<?>> key = class_5321.method_29179(class_7924.field_41266,
                InnerDimension.id("room_projection"));
        ENTITY_TYPE = class_2378.method_39197(class_7923.field_41177, key,
                class_1299.class_1300.<RoomProjectionEntity>method_5903(RoomProjectionEntity::new,
                                class_1311.field_17715)
                        .method_17687(1, 1).method_5901().method_5904()
                        .method_27299(16).method_27300(1).method_5905(key));
        PayloadTypeRegistry.playC2S().register(ToggleProjectionPayload.ID, ToggleProjectionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleOpenProjectionPayload.ID, ToggleOpenProjectionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleProjectionPayload.ID, (payload, context) ->
                context.server().execute(() -> requestToggle(context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ToggleOpenProjectionPayload.ID, (payload, context) ->
                context.server().execute(() -> requestOpenToggle(context.player())));
        registerWheel();
        PayloadTypeRegistry.playC2S().register(com.innerdimension.network.SaveFloorPatternPayload.ID,com.innerdimension.network.SaveFloorPatternPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(com.innerdimension.network.SaveFloorPatternPayload.ID,(payload,context)->context.server().execute(()->{
            var player=context.player();
            if(!com.innerdimension.progression.InnerAdvancements.done(player,"enlightenment"))return;
            try {
                FloorPatternSampler.validate(payload.settings());
                com.innerdimension.progression.DomainState.get(player.method_51469()).floorPattern(player.method_5667(),payload.settings());
                ServerPlayNetworking.send(player,com.innerdimension.item.OpenDomainCorePayload.forPlayer(player));
            } catch(IllegalArgumentException invalid) {
                player.method_7353(class_2561.method_43469("screen.inner_dimension.floor.invalid",invalid.getMessage()),false);
            }
        }));
        // World creation must run after DynamicDimensionManager has exited the world-tick loop.
        ServerTickEvents.END_SERVER_TICK.register(RoomProjectionManager::tick);
        ServerTickEvents.START_SERVER_TICK.register(server -> {ExpansionPositionLease.tick();ACTIVE.values().forEach(Domain::pin);});
        ServerLifecycleEvents.SERVER_STARTED.register(server -> clear());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clear());
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> {
            Domain domain=ACTIVE.remove(handler.field_14140.method_5667());
            if (domain!=null) { close(handler.field_14140.method_5667(),domain,false); domain.remove(); }
        });
    }

    private static void registerWheel(){
        PayloadTypeRegistry.playC2S().register(DomainWheelPayload.ID,DomainWheelPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DomainWheelStatePayload.ID,DomainWheelStatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DomainWheelPayload.ID,(payload,context)->context.server().execute(()->{
            var player=context.player();var id=player.method_5667();
            if(com.innerdimension.voiddomain.VoidEmbodiment.active(player)){player.method_7353(class_2561.method_43471("inner_dimension.void.crest_locked"),true);return;}
            if(com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(player)||com.innerdimension.voiddomain.RitualControl.locked(player))return;
            if(!com.innerdimension.progression.InnerAdvancements.done(player,"cursed_core"))return;
            if(payload.action()==2){
                if(!com.innerdimension.progression.InnerAdvancements.done(player,"enlightenment"))return;
                var held=player.method_6047();var off=player.method_6079();
                boolean owns=(held.method_7909() instanceof com.innerdimension.item.DomainCoreItem && id.equals(nonplayerplushie.SensoryLinkData.ownerUuid(nonplayerplushie.SensoryLinkData.read(held)))) || (off.method_7909() instanceof com.innerdimension.item.DomainCoreItem && id.equals(nonplayerplushie.SensoryLinkData.ownerUuid(nonplayerplushie.SensoryLinkData.read(off))));
                if(!owns)return;
                if(!payload.open()){ClosedDomainBinding.get(player.method_51469()).select(id,payload.value()<0);player.method_7353(class_2561.method_43471(payload.value()<0?"inner_dimension.closed.reversed":"inner_dimension.closed.normal"),true);return;}
                var binding=OpenDomainBinding.get(player.method_51469());
                if(binding.adjust(id,payload.value()))player.method_7353(class_2561.method_43469("inner_dimension.open.binding",binding.radius(id),binding.duration(id)/20),true);
                return;
            }
            if(payload.open()&&!com.innerdimension.progression.InnerAdvancements.done(player,"enlightenment"))return;
            int count=com.innerdimension.progression.InnerAdvancements.done(player,"fallen")?7:6;
            if(payload.action()==0){var saved=com.innerdimension.progression.DomainState.get(player.method_51469());ServerPlayNetworking.send(player,new DomainWheelStatePayload(payload.open(),java.util.stream.IntStream.range(0,count).mapToObj(slot->saved.slotName(id,slot)).toList()));}
            else if(payload.action()==1&&payload.value()>=0&&payload.value()<count){PENDING_SLOT.put(id,payload.value());if(payload.open())requestOpenToggle(player);else requestToggle(player);}
        }));
    }
    public static void requestToggle(class_3222 player) { if(!com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(player)&&!com.innerdimension.voiddomain.RitualControl.locked(player))PENDING.add(player.method_5667()); }
    public static void requestOpenToggle(class_3222 player) {
        if(com.innerdimension.domain.runtime.UnlimitedVoidControl.locked(player)||com.innerdimension.voiddomain.RitualControl.locked(player))return;
        if(!com.innerdimension.progression.InnerAdvancements.done(player,"enlightenment"))return;
        UUID owner = player.method_5667();
        PENDING.add(owner);
        PENDING_OPEN.add(owner);
    }
    public static boolean originalNoGravity(net.minecraft.class_1297 actor) {
        var d=ACTIVE.get(actor.method_5667());return d==null?actor.method_5740():d.previousNoGravity;
    }
    /** A second expansion must update the first caster's held landing too. */
    public static void relocated(java.util.List<ProjectionSafety.Move> moves) {
        com.innerdimension.domain.DebugDomain.relocated(moves);
        ExpansionPositionLease.relocated(moves);
        for(var move:moves) {
            var d=ACTIVE.get(move.entity().method_5667());
            if(d!=null){
                d.landing=move.to();
                if(d.abilities.contains(com.innerdimension.domain.DomainContest.SIMPLE)) {
                    double dx=move.to().x()-move.from().x(),dy=move.to().y()-move.from().y(),dz=move.to().z()-move.from().z();
                    for(var half:java.util.List.of(d.shell,d.interior))half.method_5814(half.method_23317()+dx,half.method_23318()+dy,half.method_23321()+dz);
                    var session=com.innerdimension.domain.runtime.DomainSessionIndex.byOwner(move.entity().method_5667());if(session!=null)session.capture(com.innerdimension.domain.runtime.DomainRuntime.now());
                }
            }
        }
    }
    public static java.util.List<String> activeAbilities(UUID owner) { Domain d=ACTIVE.get(owner); return d==null ? java.util.List.of() : d.abilities; }
    public static RoomProjectionEntity active(UUID owner) { Domain d=ACTIVE.get(owner); return d==null ? null : d.shell(); }
    public static boolean reversed(UUID owner){var d=ACTIVE.get(owner);return d!=null&&d.reversed;}
    /** Health is remaining ticks / 20, so attacks and natural decay share one clock. */
    public static boolean damageShell(RoomProjectionEntity shell,net.minecraft.class_1282 source,float amount){
        var d=ACTIVE.get(shell.owner());var attacker=source==null?null:source.method_5529();
        if(d==null||d.shell!=shell||d.open()||d.abilities.contains(com.innerdimension.domain.DomainContest.SIMPLE)||shell.phase()==ProjectionAnimation.RETRACTING
          ||!(attacker instanceof net.minecraft.class_1309)||attacker.method_5667().equals(shell.owner())||attacker.method_73183()!=shell.method_73183()||!Float.isFinite(amount)||amount<=0)return false;
        var entry=com.innerdimension.domain.DomainContest.entry(shell.owner());
        if(entry==null||!DomainLifetime.vulnerableSide(d.reversed,entry.bounds().method_1008(attacker.method_23317(),attacker.method_23318()+attacker.method_17682()*.5,attacker.method_23321())))return false;
        d.remainingTicks=DomainLifetime.shellDamage(d.remainingTicks,amount);
        if(d.remainingTicks<=0)close(shell.owner(),d,true);
        return true;
    }
    public static double remaining(UUID owner) { var d=ACTIVE.get(owner);return d==null?com.innerdimension.domain.DebugDomain.remaining(owner):d.remainingTicks; }
    public static void remaining(UUID owner,double value) { var d=ACTIVE.get(owner);if(d!=null)d.remainingTicks=Math.max(0,value);else com.innerdimension.domain.DebugDomain.remaining(owner,value); }
    public static void lifetimeRate(UUID owner,double value) { var d=ACTIVE.get(owner);if(d!=null)d.lifetimeRate=value;else com.innerdimension.domain.DebugDomain.rate(owner,value); }
    public record AbilityView(UUID owner,class_3222 caster,RoomProjectionEntity shell,java.util.List<String> abilities,long age){
        /** The runtime session id of this expansion, or null when no session is live. */
        public java.util.UUID sessionId(){
            var session=com.innerdimension.domain.runtime.DomainSessionIndex.byOwner(owner);
            return session==null?null:session.sessionId;
        }
    }
    public static java.util.List<AbilityView> abilityViews(){return ACTIVE.entrySet().stream().filter(e->e.getValue().open()||e.getValue().shell.phase()!=ProjectionAnimation.RETRACTING).map(e->new AbilityView(e.getKey(),e.getValue().caster,e.getValue().shell,e.getValue().abilities,ticks-e.getValue().started)).toList();}
    /** Called on the server thread after vanilla's packet thread dispatch. */
    public static boolean holdMovement(class_3222 player,net.minecraft.class_2828 packet) {
        if(ExpansionPositionLease.holdMovement(player,packet))return true;
        Domain domain=ACTIVE.get(player.method_5667());
        if (domain==null || !domain.holding || player.method_73183()!=domain.shell.method_73183()
                || !player.method_5805()) return false;
        float yaw=packet.method_12271(player.method_36454()),pitch=packet.method_12270(player.method_36455());
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)) return false;
        player.field_13987.method_14363(domain.landing.x(),domain.landing.y(),domain.landing.z(),yaw,pitch);
        player.method_18799(class_243.field_1353);
        player.field_6017=0;
        return true;
    }

    private static void tick(MinecraftServer server) {
        ticks++;
        com.innerdimension.boss.BossDomains.tick(server);
        com.innerdimension.domain.SimpleDomain.tick();
        com.innerdimension.progression.VillagerSoulChallenge.tick(server);
        com.innerdimension.domain.DebugDomain.tick(server);
        com.innerdimension.domain.DomainContest.tick(server);
        ProjectionEvacuation.tick();
        BURNOUT_UNTIL.entrySet().removeIf(e -> {if(e.getValue()>ticks)return false;var player=server.method_3760().method_14602(e.getKey());if(player!=null&&player.method_5805())com.innerdimension.sound.ModSounds.personal(player,"recharge",.6f);return true;});
        ACTIVE.entrySet().removeIf(entry -> {
            class_3222 owner = server.method_3760().method_14602(entry.getKey());
            Domain domain = entry.getValue();
            RoomProjectionEntity entity = domain.shell();
            if(domain.abilities.contains(com.innerdimension.domain.DomainContest.SIMPLE)) {
                ProjectionMirror.tickStatic(entity);ProjectionMirror.tickStatic(domain.interior());
            }
            if (owner == null || !owner.method_5805() || owner.method_73183() != entity.method_73183()
                    || entity.method_31747() || domain.interior().method_31747()) {
                close(entry.getKey(),domain,false);
                com.innerdimension.domain.runtime.DomainRuntime.end(entry.getKey());
                domain.remove();
                return true;
            }
            if (entity.phase() == ProjectionAnimation.RETRACTING && entity.elapsed(0) >= entity.closeTicks()) {
                com.innerdimension.domain.runtime.DomainRuntime.end(entry.getKey());
                domain.remove();
                return true;
            }
            if(entity.phase()!=ProjectionAnimation.RETRACTING && !domain.abilities.contains(com.innerdimension.domain.DomainContest.SIMPLE) && !com.innerdimension.boss.BossDomains.debited(entry.getKey()) && !com.innerdimension.domain.DomainContest.debited(entry.getKey()))domain.remainingTicks-=domain.lifetimeRate;
            if(entity.phase()!=ProjectionAnimation.RETRACTING && !domain.abilities.contains(com.innerdimension.domain.DomainContest.SIMPLE) && !com.innerdimension.domain.DomainContest.suppressed(entry.getKey()) && domain.remainingTicks<=0) {
                close(entry.getKey(),domain,true);
                com.innerdimension.progression.InnerAdvancements.grant(owner,"endurance_training");
                owner.method_7353(class_2561.method_43471("inner_dimension.projection.exhausted"),true);
            }
            boolean shellReady = entity.phase() == ProjectionAnimation.EXPANDING && entity.elapsed(0) >= entity.shellTicks();
            if (domain.interior().mirror() != null) domain.interior().mirror().tick();
            if(domain.holding&&domain.open()&&OpenDomainRules.movementReady(entity.phase(),entity.elapsed(0))) {
                try {
                    // Resolve geometry before restoring movement/gravity, including projected pillars.
                    var moves=ProjectionSafety.prepareStage(domain.shell(),domain.interior(),false);
                    ProjectionSafety.applyStage(moves,domain.shell(),domain.interior());
                    domain.interior().enableOpenCollision();
                    ExpansionPositionLease.releaseCaster(domain.shell());
                    domain.release();
                } catch(RuntimeException transientFailure) {
                    LOGGER.debug("Open-domain movement activation will retry for {}",entry.getKey(),transientFailure);
                }
            }
            domain.pin();
            boolean interiorReady = entity.phase() == ProjectionAnimation.INTERIOR && entity.elapsed(0) >= (domain.open()?Math.max(OpenDomainRules.MOVE_UNLOCK_TICKS,entity.interiorTicks()):entity.interiorTicks());
            if (shellReady || interiorReady) {
                try {
                    var moves=ProjectionSafety.prepareStage(domain.shell(),domain.interior(),shellReady);
                    moves=ProjectionSafety.applyStage(moves,domain.shell(),domain.interior());
                    for (var move:moves) LOGGER.info("Projection {} relocated {} ({}) from {} to {}",
                            shellReady ? "shell" : "interior",move.entity().method_5667(),
                            move.entity().getClass().getSimpleName(),move.from(),move.to());
                    if (shellReady) {
                        domain.shell().beginInterior(); domain.interior().beginInterior();
                    } else {
                        domain.shell().stabilize(); domain.interior().stabilize();
                        if(domain.open())OpenDomainPerformance.stable(entry.getKey());
                        if(!domain.open())com.innerdimension.domain.runtime.DomainRuntime.stable(entry.getKey(),owner.method_73183(),domain.shell(),domain.interior(),domain.abilities,domain.expansionSlot);
                        domain.release();
                        var spec=((RoomProjectionContraption)entity.getContraption()).sourceSpec();
                        var center=entity.getAnchorVec().method_1031(spec.anchorX(),spec.anchorY(),spec.anchorZ());
                        LOGGER.info("Projection opened owner={} center={} ownerPosition={}",
                                entry.getKey(),center,owner.method_73189());
                    }
                } catch (RuntimeException failure) {
                    if (failure instanceof ProjectionSafety.CasterPlacementException) {
                        domain.remove();
                        LOGGER.warn("Projection caster placement failed for {}", entry.getKey(),failure);
                        owner.method_7353(class_2561.method_43471("inner_dimension.projection.failed"), true);
                        return true;
                    }
                    // An occupant or transient world operation must not retract this domain.
                    // Keep the current safe collision phase and retry the transition next tick.
                    if ((long)entity.elapsed(0)%20==0)
                        LOGGER.warn("Projection transition retry for {}",entry.getKey(),failure);
                }
            }
            return false;
        });
        LAST_TOGGLE.keySet().removeIf(id -> server.method_3760().method_14602(id) == null);
        // A remembered location belongs to this visit to this inner dimension.
        DOMAIN_RETURNS.entrySet().removeIf(entry -> {
            class_3222 player = server.method_3760().method_14602(entry.getKey());
            return player == null || !player.method_5805()
                    || !player.method_73183().method_27983().equals(entry.getValue().world());
        });
        for (UUID id : PENDING.stream().sorted().toList()) {
            class_3222 player = server.method_3760().method_14602(id);
            if (player == null || !player.method_5805() || player.method_7325()) continue;
            if(com.innerdimension.voiddomain.RitualControl.locked(player))continue;
            if (ticks - LAST_TOGGLE.getOrDefault(id, -100L) < 5) continue;
            LAST_TOGGLE.put(id, ticks);
            boolean open = PENDING_OPEN.remove(id);
            try { toggle(server, player, open); }
            catch (Exception exception) {
                LOGGER.error("Failed to toggle room projection for {}", id, exception);
                player.method_7353(class_2561.method_43471("inner_dimension.projection.failed"), true);
            }
        }
        PENDING_SLOT.clear();
        PENDING.clear();
        PENDING_OPEN.clear();
        com.innerdimension.domain.DomainAbilityEngine.tick(server,abilityViews());
    }

    private static void toggle(MinecraftServer server, class_3222 player, boolean open) {
        if (!com.innerdimension.progression.InnerAdvancements.done(player, "cursed_core")) {
            player.method_7353(class_2561.method_43471("inner_dimension.domain.locked"), true);
            return;
        }
        UUID owner = player.method_5667();
        var route=com.innerdimension.voiddomain.VoidEmbodimentRules.route(DynamicDimensionManager.isInnerWorld(player.method_73183().method_27983()),open,com.innerdimension.voiddomain.VoidEmbodiment.active(player));
        if(route==com.innerdimension.voiddomain.VoidEmbodimentRules.Input.BLOCKED){player.method_7353(class_2561.method_43471("inner_dimension.void.domain_locked"),true);return;}
        if (route==com.innerdimension.voiddomain.VoidEmbodimentRules.Input.ROOM_TRAVEL) {
            toggleDomainTravel(player);
            return;
        }
        Domain previous = ACTIVE.get(owner);
        if (previous != null) {
            close(owner,previous,false);
            player.method_7353(class_2561.method_43471("inner_dimension.projection.closed"), true);
            return;
        }
        if(burnoutRemaining(owner)>0) {
            player.method_7353(class_2561.method_43469("inner_dimension.projection.burnout",(burnoutRemaining(owner)+19)/20),true);
            return;
        }
        // A NoN animation in flight owns the caster's pose and position; expanding would fight
        // it. Releasing an open domain is handled above and stays available.
        if (com.innerdimension.domain.compat.NoNDomainBridge.animationBusy(player.method_51469(), owner)) {
            player.method_7353(class_2561.method_43471("inner_dimension.projection.failed"), true);
            return;
        }
        var enemy=com.innerdimension.domain.DomainContest.threat(player);
        boolean response=com.innerdimension.domain.DomainContest.canRespond(player,enemy);
        boolean forcedClosed=!open&&PENDING_SLOT.containsKey(owner)&&enemy!=null;
        boolean forcedInside=forcedClosed&&!enemy.open()&&!response;
        boolean counter=!open && enemy!=null && !enemy.open() && (response||forcedInside);
        boolean simple=false;
        var saved=com.innerdimension.progression.DomainState.get(player.method_51469());
        int expansionSlot=PENDING_SLOT.getOrDefault(owner,saved.selectedSlot(owner));
        // Saved crests survive revocation, but cannot bypass their advancement unlocks.
        var loadout=saved.loadout(owner,expansionSlot);
        if((loadout.contains(com.innerdimension.voiddomain.VoidContamination.ABILITY)
                && !com.innerdimension.progression.InnerAdvancements.done(player,"fallen"))
                || (loadout.contains(com.innerdimension.domain.UnlimitedVoidRules.ID)
                && !com.innerdimension.progression.InnerAdvancements.done(player,"void_baptism"))
                || (loadout.contains(com.innerdimension.domain.DomainModes.SUCCUBUS)
                && !com.innerdimension.progression.InnerAdvancements.done(player,"enlightenment"))){
            player.method_7353(class_2561.method_43471("inner_dimension.projection.failed"),true);return;
        }
        if(saved.loadout(owner,expansionSlot).contains(com.innerdimension.domain.DomainContest.SIMPLE)) {
            if(open)return;
            if(!com.innerdimension.progression.InnerAdvancements.done(player,"mental_seeding"))return;
            simple=true;counter=false;open=false;
        } else if(enemy!=null && !enemy.open() && !counter && !open && !forcedClosed) {
            int chosen=-1;
            int selected=expansionSlot;
            if(saved.loadout(owner,selected).contains(com.innerdimension.domain.DomainContest.SIMPLE))chosen=selected;
            else for(int slot=0;slot<7;slot++)if(saved.loadout(owner,slot).contains(com.innerdimension.domain.DomainContest.SIMPLE)){chosen=slot;break;}
            if(chosen<0 || !com.innerdimension.progression.InnerAdvancements.done(player,"mental_seeding")) {
                player.method_7353(class_2561.method_43471("inner_dimension.simple.missing"),true);return;
            }
            expansionSlot=chosen;simple=true;open=false;
        } else if(saved.loadout(owner,expansionSlot).contains(com.innerdimension.domain.DomainContest.SIMPLE)) {
            if(!com.innerdimension.progression.InnerAdvancements.done(player,"mental_seeding"))return;
            simple=true;counter=false;open=false;
        }
        if(saved.loadout(owner,expansionSlot).contains(com.innerdimension.domain.DomainModes.SHRINE)&&!com.innerdimension.progression.DomainFeatures.canUseShrine(player)){
            player.method_7353(class_2561.method_43471("inner_dimension.shrine.locked"),true);return;
        }
        if(saved.loadout(owner,expansionSlot).contains(com.innerdimension.domain.DomainModes.SHRINE)){
            int cost=com.innerdimension.domain.DomainAbilityCatalog.weight(saved.loadout(owner,expansionSlot));
            if(cost>saved.capacity(owner)){player.method_7353(class_2561.method_43469("inner_dimension.shrine.capacity",cost,saved.capacity(owner)),true);return;}
        }
        class_3218 source = simple ? null : DynamicDimensionManager.getOrCreate(server, owner);
        InnerState.get(server.method_30002()).addRoomOwner(owner);
        if (!SafeEntityTeleport.prepare(player)) throw new IllegalStateException("Could not prepare domain caster");
        var actualFeet = new net.minecraft.class_243(player.method_23317(),player.method_5829().field_1322,player.method_23321());
        // Open-domain blocks align to the destination world's integer grid on all three axes.
        // Entity safety still starts at each entity's real feet, independently of this anchor.
        var feet = open ? new net.minecraft.class_243(Math.rint(actualFeet.field_1352),
                Math.rint(actualFeet.field_1351),Math.rint(actualFeet.field_1350)) : actualFeet;
        if(counter) {
            var firstSpec=((RoomProjectionContraption)enemy.shell.getContraption()).sourceSpec();
            feet=enemy.shell.getAnchorVec().method_1031(firstSpec.anchorX(),firstSpec.anchorY()-.01,firstSpec.anchorZ());
        }
        if(open&&!simple)OpenDomainPerformance.startIfArmed(owner);
        RoomProjectionContraption snapshot = simple ? RoomProjectionContraption.simpleSnapshot() : open
                ? RoomProjectionContraption.openSnapshot(source, player.method_51469(), feet, saved.floorPattern(owner),OpenDomainBinding.get(player.method_51469()).radius(owner))
                : RoomProjectionContraption.snapshot(source);
        if(open)for(var closed:com.innerdimension.domain.DomainContest.entries()) {
            if(closed.simple||closed.open()||!closed.covers(player)||com.innerdimension.boss.BossDomains.cooperate(player,closed))continue;
            com.innerdimension.domain.DomainContest.paintClosed(snapshot,feet,closed,source,saved.floorPattern(owner));
            if(!com.innerdimension.domain.DomainContest.canRespond(player,closed))com.innerdimension.domain.DomainContest.clip(snapshot,feet,closed);
        }
        if(!open&&!simple&&!counter)feet=com.innerdimension.boss.BossDomains.sharedCenter(player,snapshot,feet);
        RoomProjectionEntity projection = new RoomProjectionEntity(ENTITY_TYPE, player.method_73183());
        RoomProjectionEntity interior = new RoomProjectionEntity(ENTITY_TYPE, player.method_73183());
        Domain domain = new Domain(projection, interior,player,expansionSlot,open);
        try {
            // method_33571() is the EYE position, not the feet. The collision-box
            // bottom is the support surface while standing, including crouched/scaled players.
            projection.expansionSlot(expansionSlot);interior.expansionSlot(expansionSlot);
            projection.initialize(open || simple ? snapshot.emptyPart() : snapshot.part(true),feet,true,owner);
            interior.initialize(open || simple ? snapshot : snapshot.part(false),feet,false,owner);
            // Open domains skip the enclosing-shell phase. The empty shell entity remains only as
            // the lifecycle/crest handle expected by the shared projection runtime.
            if (open || simple) { projection.beginInterior(); interior.beginInterior(); }
            // Set presentation before tracking sends the initial entity metadata.
            if(simple) {
                projection.presentation("简易领域",0xffffff);interior.presentation("简易领域",0xffffff);
                projection.stabilize();interior.stabilize();
            }
            if(!simple)interior.mirror(new ProjectionMirror(source,projection,interior));
            // Closed rooms search their copied interior. Open domains search only upward from the
            // caster's real feet in the original X/Z column: buildings can lift the caster onto
            // their roof, while caves below can never become candidates.
            domain.landing=counter ? null : simple ? new ProjectionSafetySearch.Point(actualFeet.field_1352,actualFeet.field_1351,actualFeet.field_1350) : open
                    ? ProjectionSafety.openExpansionLanding(snapshot,projection,player)
                    : ProjectionSafety.expansionLanding(snapshot,source,projection,player);
            if(!simple) {
                com.innerdimension.domain.DomainContest.announce(projection);
                var moves=ProjectionSafety.prepareExpansion(projection,interior,player,domain.landing);
                ProjectionSafety.apply(moves);relocated(moves);ExpansionPositionLease.hold(moves,projection,interior);
                domain.landing=new ProjectionSafetySearch.Point(player.method_23317(),player.method_23318(),player.method_23321());
            }
            if (!player.method_73183().method_8649(projection) || !player.method_73183().method_8649(interior)) {
                throw new IllegalStateException("Projection entity spawn rejected");
            }
            domain.holding=!simple&&!counter;
            domain.pin();
            LOGGER.info("Projection caster placed owner={} counter={} originalFeet={} landing={} ghostInterior={}",owner,counter,actualFeet,domain.landing,interior.contestGhost());
            if (com.innerdimension.config.InnerConfig.get().projectionDebug)
                LOGGER.info("[ProjectionDebug] SERVER_SPAWN owner={} shell={} interior={} shellBlocks={} interiorBlocks={} world={}",
                        owner, projection.method_5667(), interior.method_5667(),
                        projection.getContraption().getBlocks().size(), interior.getContraption().getBlocks().size(),
                        player.method_73183().method_27983());
            ACTIVE.put(owner, domain);
            if(open)com.innerdimension.domain.runtime.DomainRuntime.stable(owner,player.method_73183(),projection,interior,domain.abilities,expansionSlot);
            var added=com.innerdimension.domain.DomainContest.add(owner,projection,interior,
                com.innerdimension.domain.DomainContest.strength(player,domain.abilities),simple,()->close(owner,domain,true));
            com.innerdimension.boss.BossDomains.added(added,actualFeet);
            if(forcedInside&&!simple)added.effectiveStrength=com.innerdimension.domain.DomainContestRules.penalized(added.strength);
            if(counter)com.innerdimension.domain.DomainContest.pair(enemy,added);
            else if(!simple)com.innerdimension.domain.DomainContest.pairOpen(added,enemy,response);
            if(simple) {
                com.innerdimension.domain.SimpleDomain.start(player,enemy==null?0:enemy.strength,()->close(owner,domain,false));
                com.innerdimension.domain.runtime.DomainRuntime.stable(owner,player.method_73183(),projection,interior,domain.abilities,domain.expansionSlot);
            }
        } catch (RuntimeException | Error failure) {
            // Entity tracking can throw AFTER adding to the world. Never leave an untracked
            // projection behind to break later serialization or disconnect cleanup.
            ACTIVE.remove(owner, domain);
            com.innerdimension.domain.runtime.DomainRuntime.end(owner);
            domain.remove();
            throw failure;
        }
    }

    /** The same key travels vertically while inside a room, and projects while outside. */
    private static void toggleDomainTravel(class_3222 player) {
        if(!SafeEntityTeleport.prepare(player)){player.method_7353(class_2561.method_43471("inner_dimension.projection.failed"),true);return;}
        class_3218 world = player.method_51469();
        UUID id = player.method_5667();
        RoomShellGenerator.ensureShell(world, DynamicDimensionManager.ownerOfWorld(world.method_27983()));
        boolean entering = player.method_23318() < RoomGeometry.DOMAIN_FLOOR;
        DomainReturn before = new DomainReturn(world.method_27983(), player.method_23317(),
                player.method_23318(), player.method_23321(), player.method_36454(), player.method_36455());
        DomainReturn remembered = DOMAIN_RETURNS.get(id);
        boolean validReturn = remembered != null && remembered.world().equals(world.method_27983())
                && remembered.x()>=0&&remembered.x()<16&&remembered.z()>=0&&remembered.z()<16
                && remembered.y()>=0&&remembered.y()<RoomGeometry.INNER_CEILING;
        double x = entering ? RoomGeometry.INTERIOR_SIZE / 2.0
                : validReturn ? remembered.x() : DynamicDimensionManager.LANDING_X;
        double y = entering ? RoomGeometry.DOMAIN_FLOOR + 1.0
                : validReturn ? remembered.y() : DynamicDimensionManager.LANDING_Y;
        double z = entering ? RoomGeometry.INTERIOR_SIZE / 2.0
                : validReturn ? remembered.z() : DynamicDimensionManager.LANDING_Z;
        float yaw = !entering && validReturn ? remembered.yaw() : before.yaw();
        float pitch = !entering && validReturn ? remembered.pitch() : before.pitch();
        if (entering) {
            if(!com.innerdimension.voiddomain.VoidEmbodiment.active(player)&&!com.innerdimension.voiddomain.InfectionPillars.present(world)){
            var safe = ProjectionSafety.domainLanding(world, player,
                    new ProjectionSafetySearch.Point(x, y, z));
            x = safe.x();
            y = safe.y();
            z = safe.z();
            }
        }
        if(entering&&com.innerdimension.voiddomain.VoidEmbodiment.active(player)){
            x=1+world.method_8409().method_43058()*14;z=1+world.method_8409().method_43058()*14;
            y=44+world.method_8409().method_43058()*2;
        }
        if(com.innerdimension.voiddomain.InfectionPillars.present(world)||com.innerdimension.voiddomain.VoidEmbodiment.active(player)){
            var safe=com.innerdimension.dimension.InnerRoomLanding.find(world,player,new ProjectionSafetySearch.Point(x,y,z),entering);
            if(safe==null){player.method_7353(class_2561.method_43471("inner_dimension.projection.failed"),true);return;}
            x=safe.x();y=safe.y();z=safe.z();
        }
        if (!SafeEntityTeleport.move(player, world, x, y, z, Set.of(), yaw, pitch, false)) {
            player.method_7353(class_2561.method_43471("inner_dimension.projection.failed"), true);
            return;
        }
        // Never carry the old fall distance or falling velocity across vertical travel.
        player.method_18799(class_243.field_1353);
        player.field_6017 = 0;
        if (entering) DOMAIN_RETURNS.put(id, before);
        else DOMAIN_RETURNS.remove(id);
        if(entering) {
            com.innerdimension.progression.DomainFeatures.introduce(player);
            var enteredOwner=DynamicDimensionManager.ownerOfWorld(world.method_27983());
            if(player.method_5667().equals(enteredOwner)||com.innerdimension.progression.InnerAdvancements.done(player,"disciple_slayer"))
                player.method_7353(com.innerdimension.progression.DomainFeatures.announcement(player,enteredOwner,false),true);
        } else player.method_7353(class_2561.method_43471("inner_dimension.domain.returned"),true);
    }

    private static void clear() {
        ExpansionPositionLease.clear();
        com.innerdimension.domain.runtime.DomainRuntime.clear();
        com.innerdimension.domain.DomainContest.clear();
        com.innerdimension.progression.VillagerSoulChallenge.clear();
        ProjectionEvacuation.clear();
        ProjectionMirror.clearStatic();
        ACTIVE.values().forEach(Domain::remove);
        ACTIVE.clear();
        PENDING_SLOT.clear();
        PENDING.clear();
        PENDING_OPEN.clear();
        LAST_TOGGLE.clear();
        BURNOUT_UNTIL.clear();
        DOMAIN_RETURNS.clear();
        ticks = 0;
    }
}
