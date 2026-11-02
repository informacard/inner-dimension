package com.innerdimension.entity;

import com.innerdimension.dimension.DynamicDimensionManager;
import com.innerdimension.binding.DollBindingManager;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.exit.ModExitComponents;
import com.innerdimension.item.ModItems;
import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.security.EntrySafetyGuard;
import com.innerdimension.security.SecurityState;
import com.innerdimension.state.InnerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_2246;
import net.minecraft.class_11368;
import net.minecraft.class_11372;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2484;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1314;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_10255;
import net.minecraft.class_1703;
import net.minecraft.class_1722;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_3908;
import net.minecraft.class_5132.class_5133;
import net.minecraft.class_5134;
import net.minecraft.class_1277;
import net.minecraft.class_9296;
import net.minecraft.class_9817;
import net.minecraft.server.MinecraftServer;
import nonplayerplushie.SensoryLinkData;
import nonplayerplushie.SensoryLinkItems;
import nonplayerplushie.SensoryLinkTargetResolver;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The placed ender pearl dildo. Behaves like a decorative "armor stand"-ish object:
 * gravity, unbreakable-by-tools but destroyed by a single hit which returns the item,
 * and sneak + right-click teleports the user into their own inner dimension.
 */
public class EnderPearlDildoEntity extends class_1314 implements GeoEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");
    /** Interior of the room contents fall into, matching the design notes. */
    private static final double ROOM_ITEM_X = 8.0D;
    private static final double ROOM_ITEM_Y = 8.5D;
    private static final double ROOM_ITEM_Z = 8.0D;
    private static final double ROOM_MOB_X = 8.0D;
    private static final double ROOM_MOB_Y = 0.5D;
    private static final double ROOM_MOB_Z = 8.0D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final class_1277 container = new class_1277(5);
    /** Schema version of the binding data this stick carries, for future migrations. */
    public static final int DATA_VERSION = 1;
    /** Bound player of the sensory link doll sitting on top of this stick, if any. */
    private UUID plushieOwner;
    /**
     * The doll entity this stick is connected to, as far as this stick knows.
     *
     * <p>Only a hint: the authoritative connection lives in {@link InnerState}, which survives
     * this entity being pushed, unloaded or replaced. It is stored so a reloaded stick can hand its
     * own opinion to the binding manager, which then verifies it against the real doll.</p>
     */
    private UUID boundDoll;
    /**
     * Version of the binding data stored on this stick; 0 means "written by a build that only
     * knew about {@code PlushieOwner}", which is what the migration pass looks for.
     */
    private int migrationVersion;
    /** Version of the security data on this stick. */
    public static final int SECURITY_VERSION = 1;
    /**
     * Who placed this stick, captured when the item creates the entity.
     *
     * <p>Fixed for the lifetime of the entity: a rescan, a piston, a reload, a binding or a
     * promotion never rewrites it, and a legacy stick keeps it unknown instead of having the doll
     * owner copied into it.</p>
     */
    private UUID placerUuid;
    /** The doll owner this stick was explicitly authorized for by that owner's own placement. */
    private UUID authorizedOwnerUuid;
    /** The candidate owner this stick refused, kept so the refusal survives rescans and reloads. */
    private UUID rejectedOwnerUuid;
    /**
     * Set while a locked or refused contact must not become an input request.
     *
     * <p>Cleared only once the doll leaves this stick's own cell, so unlocking cannot replay the
     * contact that happened while the pair was locked.</p>
     */
    private boolean contactSuppressed;
    private int securityVersion;
    /**
     * Latched while a doll sits in this stick's cell, so the feed fires once per arrival.
     *
     * <p>Persisted on purpose: leaving the inner dimension reloads the chunks around the stick
     * (the player spent the whole visit in another dimension), and a latched-but-forgotten state
     * would make the reloaded stick feed the same doll again the very tick the player steps out -
     * which teleported them straight back in and made it look like they were trapped.</p>
     */
    private boolean plushieTriggered;
    private class_2338 lastCell;
    /** Cell used to notice that a push moved this stick, so its detection re-runs at once. */
    private class_2338 sweepCell;
    /** Follows the sticky piston that shoved this stick, so it is pulled back like a block. */
    private final PistonTow pistonTow = new PistonTow();
    private int age;
    private int lastInteractionAge = -100;

    public EnderPearlDildoEntity(class_1299<? extends EnderPearlDildoEntity> type, class_1937 world) {
        super(type, world);
        this.method_5977(true); // no AI
        this.method_5971(); // MobEntity#setPersistent: placed objects must never distance-despawn.
    }

    /** The tether variant has its own entity type: right-click opens a hopper-sized
     *  container and leads can be tied to it. */
    public boolean isTethered() {
        return this.method_5864() == ModEntities.TETHERED_ENDER_PEARL_DILDO;
    }

    public net.minecraft.class_1263 hopperInventory() {
        return this.container;
    }

    public static class_5133 createAttributes() {
        return class_1314.method_26828()
                .method_26868(class_5134.field_23716, 20.0)
                .method_26868(class_5134.field_23719, 0.0)
                .method_26868(class_5134.field_23718, 1.0);
    }

    @Override
    protected void method_5959() {
        // No AI ticking.
    }

    // NOTE: do not override method_5863() here. It is Entity#canHit(), the predicate the
    // client crosshair ray uses (EntityPredicates.EXCEPT_SPECTATOR_AND_HITTABLE uses
    // !isSpectator() && canHit()), so overriding it to false makes the entity impossible
    // to target with either mouse button.

    @Override
    public class_1269 method_5688(class_1657 player, class_1268 hand) {
        return this.handleInteraction(player, hand);
    }

    @Override
    protected class_1269 method_5992(class_1657 player, class_1268 hand) {
        return this.handleInteraction(player, hand);
    }

    public class_1269 handleSneakInteraction(class_1657 player, class_1268 hand) {
        return this.handleInteraction(player, hand);
    }

    private class_1269 handleInteraction(class_1657 player, class_1268 hand) {
        class_1937 world = this.method_73183();
        boolean client = world.method_8608();
        boolean sneaking = player.method_5715();

        // The right-click reaches us through more than one hook (interact + interactAt),
        // so ignore a repeat within the same tick: otherwise tying mobs on with a lead
        // also opened the container on the second call.
        if (this.lastInteractionAge == this.age) {
            return class_1269.field_5812;
        }
        this.lastInteractionAge = this.age;

        if (client || !(player instanceof class_3222 actor)) {
            // Every state change below is server-side; the client only gets its interaction consumed.
            return class_1269.field_5812;
        }

        // A held hopper places a real vanilla block on the player's side, pointing inward.
        class_1799 held = actor.method_5998(hand);
        if (held.method_7909() instanceof net.minecraft.class_1747 item
                && item.method_7711() == class_2246.field_10312) {
            this.placeSideHopper(actor, hand, held, item);
            return class_1269.field_5812;
        }

        // 1) holding a sensory link doll: a dedicated branch with its own three-state outcome, so
        //    "not a doll" can never be reported as "the doll was refused".
        InteractionOutcome placed = this.placeSensoryLinkDoll(actor, hand);
        if (placed == InteractionOutcome.DENIED) {
            return class_1269.field_5812;
        }
        if (placed == InteractionOutcome.PLACED) {
            return class_1269.field_5812;
        }

        // 2) every dimension branch passes the security gate first: a locked, mismatched, pending or
        //    unclaimed port consumes the interaction, says why, and never falls through into the
        //    ordinary "enter my own dimension" path.
        DollSecurityManager.Gate gate = DollSecurityManager.forInteraction(this, actor.method_5667());
        if (gate.blocked()) {
            DollSecurityManager.sendFeedback(actor, gate);
            return class_1269.field_5812;
        }

        // 3) Shift + right-click on a connected stick: make it (or claim it as) the strong port.
        if (sneaking) {
            DollBindingManager.Promotion promotion =
                    DollBindingManager.promoteStrong((class_3218) world, this, actor.method_5667());
            switch (promotion) {
                case PROMOTED, ALREADY_STRONG -> {
                    DollSecurityManager.send(actor, "inner_dimension.binding.strong_set");
                    return class_1269.field_5812;
                }
                case CLAIMED -> {
                    DollSecurityManager.send(actor, DollSecurityManager.MSG_CLAIM_DONE);
                    return class_1269.field_5812;
                }
                case LOCKED -> {
                    DollSecurityManager.send(actor, DollSecurityManager.MSG_LOCKED);
                    return class_1269.field_5812;
                }
                case DENIED -> {
                    DollSecurityManager.send(actor, DollSecurityManager.MSG_NOT_YOURS);
                    return class_1269.field_5812;
                }
                default -> {
                    // Not connected: the ordinary entry path below still applies.
                }
            }
        }

        if (!this.isTethered()) {
            if (!sneaking) {
                return class_1269.field_5811;
            }
            com.innerdimension.animation.DildoEntryAnimation.start(actor, this);
            return class_1269.field_5812;
        }

        // Tether variant: sneaking always means "go in", otherwise tie led mobs on,
        // otherwise open the container.
        if (sneaking) {
            com.innerdimension.animation.DildoEntryAnimation.start(actor, this);
            return class_1269.field_5812;
        }
        if (this.attachLeashedMobs(player)) {
            this.method_43077(class_3417.field_60855);
            return class_1269.field_5812;
        }
        this.openContainer(actor);
        return class_1269.field_5812;
    }

    /** The three outcomes of the "holding a sensory link doll" branch. */
    private enum InteractionOutcome {
        /** The held item is not a doll: the caller continues with the other branches. */
        PASS,
        /** A doll was placed and the connection authorized. */
        PLACED,
        /** A doll was held but the placement was refused; nothing changed. */
        DENIED
    }

    private void placeSideHopper(class_3222 actor, class_1268 hand, class_1799 held,
                                  net.minecraft.class_1747 item) {
        class_2338 cell = this.method_24515();
        double dx = actor.method_23317() - this.method_23317();
        double dz = actor.method_23321() - this.method_23321();
        class_2350 side = Math.abs(dx) > Math.abs(dz)
                ? (dx >= 0 ? class_2350.field_11034 : class_2350.field_11039)
                : (dz >= 0 ? class_2350.field_11035 : class_2350.field_11043);
        class_2338 target = cell.method_10093(side);
        class_1937 world = this.method_73183();
        if (!world.method_8320(target).method_45474()) {
            actor.method_7353(class_2561.method_43471("inner_dimension.hopper.place_failed"), true);
            return;
        }
        var hit = new net.minecraft.class_3965(new class_243(target.method_10263() + 0.5D,
                target.method_10264() + 0.5D, target.method_10260() + 0.5D), side, target, false);
        // Vanilla BlockItem handles placement permission, collision, NBT/components, sound and cost.
        if (!item.method_7712(new net.minecraft.class_1750(actor, hand, held, hit)).method_23665()) {
            actor.method_7353(class_2561.method_43471("inner_dimension.hopper.place_failed"), true);
        }
    }

    /**
     * Right-clicking this stick while holding a sensory link doll puts the doll on the block above it.
     *
     * <p>Security spec section 3.1: only the doll's own owner may place it here, a locked stick or a
     * stick connected to somebody else cannot be overwritten by holding a different doll, and a
     * refused attempt changes nothing at all - no entity, no consumed item, no authorization and no
     * later transfer branch.</p>
     */
    private InteractionOutcome placeSensoryLinkDoll(class_3222 player, class_1268 hand) {
        class_1799 stack = player.method_5998(hand);
        if (!isSensoryLinkDoll(stack)) {
            return InteractionOutcome.PASS; // not a doll: not a refusal either
        }
        class_1937 world = this.method_73183();
        if (!(world instanceof class_3218 serverWorld)) {
            return InteractionOutcome.DENIED;
        }
        UUID owner = sensoryLinkDollOwner(stack);
        if (owner == null || !owner.equals(player.method_5667())) {
            LOGGER.warn("[InnerDimension] {} tried to place a doll owned by {} on stick {}; refused",
                    player.method_5667(), owner, this.method_5667());
            DollSecurityManager.send(player, DollSecurityManager.MSG_NOT_YOURS);
            return InteractionOutcome.DENIED;
        }
        DollSecurityManager.Gate gate = DollSecurityManager.forDirectOwnerClaim(this, owner);
        if (gate.state() == SecurityState.LOCKED || gate.state() == SecurityState.OWNER_MISMATCH) {
            DollSecurityManager.send(player, gate.state() == SecurityState.LOCKED
                    ? DollSecurityManager.MSG_LOCKED : "inner_dimension.binding.other_connection");
            return InteractionOutcome.DENIED;
        }
        class_2338 above = this.method_24515().method_10084();
        if (!world.method_8320(above).method_26215()
                || !DollBindingManager.canPlaceDollAt(serverWorld, above, this)) {
            DollSecurityManager.send(player, "inner_dimension.binding.occupied");
            return InteractionOutcome.DENIED;
        }

        // The skin comes from the doll's own sensory link data and may be absent; the binding
        // identity below comes from the resolver, never from that profile.
        class_2487 data = SensoryLinkData.read(stack);
        class_9296 profile = SensoryLinkData.profile(data);
        SensoryLinkDollEntity doll = SensoryLinkDollEntity.place(serverWorld, above, profile,
                player.method_36454());
        if (doll == null || doll.method_31481()) {
            // The spawn failed, so the item must not be consumed either.
            LOGGER.warn("[InnerDimension] Could not place the doll above stick {}", this.method_5667());
            DollSecurityManager.send(player, "inner_dimension.binding.place_failed");
            return InteractionOutcome.DENIED;
        }
        // Recorded before the sweep can see the doll, so a doll without a profile still binds.
        doll.setDollData(data);
        doll.setOwnerUuid(owner);
        doll.setOwnerName(SensoryLinkData.ownerName(data));
        doll.markOwnerPlaced();
        this.plushieOwner = owner;
        this.boundDoll = doll.method_5667();
        this.plushieTriggered = false;
        // This is the one and only path that grants authorization, and it comes from the owner's own
        // successful interaction - a rescan or a piston can never produce it.
        this.authorize(owner);
        this.markMigrated();
        // Registering the connection right away means the new doll is known before the next
        // periodic sweep, instead of the stick looking unbound for up to ten ticks.
        DollBindingManager.onDildoPlaced(this);
        if (!player.method_68878()) {
            stack.method_7934(1);
        }
        return InteractionOutcome.PLACED;
    }

    /**
     * True for a held item that is a usable non_player_plushie sensory link doll.
     *
     * <p>The recognition is delegated to the plushie mod's own resolver, which verifies the exact
     * item id, the data schema, the owner uuid and the owner name. No player-head marker, no
     * translatable custom name and no profile component is consulted any more: plushie 1.5.0 moved
     * all of that into {@code custom_data.sensory_link}. A plain player head, an old marked head or
     * a merely renamed item must therefore never be mistaken for a doll, and no raw NBT, string or
     * reflection fallback is kept here.</p>
     */
    public static boolean isSensoryLinkDoll(class_1799 stack) {
        return stack != null
                && !stack.method_7960()
                && stack.method_7909() == SensoryLinkItems.SENSORY_LINK_DOLL
                && SensoryLinkTargetResolver.resolve(stack) != null;
    }

    /**
     * The player a sensory link doll is bound to.
     *
     * <p>The uuid comes from the resolver, which is the only authority for it; it is never derived
     * from the display name or from the skin profile.</p>
     */
    public static UUID sensoryLinkDollOwner(class_1799 stack) {
        SensoryLinkTargetResolver.Target target = SensoryLinkTargetResolver.resolve(stack);
        return target == null ? null : target.uuid;
    }

    /** Ties every mob the player is currently leading to this stick, like a fence knot. */
    private boolean attachLeashedMobs(class_1657 player) {
        boolean attached = false;
        for (class_9817 leashable : class_9817.method_71004(player)) {
            if (leashable.method_71002(this)) {
                leashable.method_60964(this, true);
                attached = true;
            }
        }
        return attached;
    }

    private void openContainer(class_3222 player) {
        player.method_17355(new class_3908() {
            @Override
            public class_2561 method_5476() {
                return class_2561.method_43471(isTethered()
                        ? "entity.inner_dimension.tethered_ender_pearl_dildo" : "entity.inner_dimension.ender_pearl_dildo");
            }

            @Override
            public class_1703 createMenu(int syncId, class_1661 inventory, class_1657 owner) {
                return new class_1722(syncId, inventory, EnderPearlDildoEntity.this.container);
            }
        });
    }

    /**
     * Moves everything attached to this stick into the room: led mobs are released (the
     * lead drops at the stick, outside) and teleported in, then the container contents are
     * dropped inside the room.
     *
     * <p>Every object moved gets its own entry record first - which dildo it came through, that
     * dildo's position, where the object itself stood, and which room it entered. That record is
     * what lets the object leave through the same dildo when the dimension has no strong binding,
     * so it can never be shared between two objects.</p>
     */
    public void moveContentsInto(class_3218 origin, class_3218 room, UUID roomOwner) {
        moveContentsInto(origin, room, roomOwner, false);
    }

    /**
     * @param ownerEntryGuarded true when the caller already ran both player and dildo entry guards
     *                          for this room owner's transfer
     */
    public void moveContentsInto(class_3218 origin, class_3218 room, UUID roomOwner,
                                 boolean ownerEntryGuarded) {
        if (!this.isTethered()) {
            return;
        }
        MinecraftServer server = origin.method_8503();
        InnerState state = server == null ? null : InnerState.get(server.method_30002());
        class_2960 originId = origin.method_27983().method_29177();
        double dildoX = this.method_23317();
        double dildoY = this.method_23318();
        double dildoZ = this.method_23321();

        // Security spec section 7: the device is re-checked after the request was queued, and a
        // refused or locked device takes nothing in - neither items nor mobs.
        DollSecurityManager.Gate gate = DollSecurityManager.forPort(this, roomOwner);
        if (gate.blocked()) {
            LOGGER.debug("[InnerDimension] Stick {} refuses content input: {}", this.method_5667(),
                    gate.state());
            return;
        }

        for (class_9817 leashable : new ArrayList<>(class_9817.method_71004(this))) {
            // A leashed boat is not a passenger carrier to send through: only the living beings
            // riding it go in, while the hull itself and its leash stay outside. This branch has
            // to come first, because a boat is an entity like any other and the generic path
            // below would teleport the boat itself.
            if (leashable instanceof class_10255 boat) {
                this.moveBoatPassengersInto(leashable, boat, origin, room, roomOwner,
                        state, originId, dildoX, dildoY, dildoZ, ownerEntryGuarded);
                continue;
            }
            if (!(leashable instanceof class_1297 mob)) {
                continue;
            }
            if (mob instanceof SensoryLinkDollEntity || mob instanceof EnderPearlDildoEntity
                    || !com.innerdimension.transport.SafeEntityTeleport.prepare(mob)) continue;
            double fromX = mob.method_23317();
            double fromY = mob.method_23318();
            double fromZ = mob.method_23321();
            // The leash is recorded before it is touched, so a failed teleport can restore it.
            leashable.method_65894(); // detach without dropping...
            boolean moved = com.innerdimension.transport.SafeEntityTeleport.move(mob, room, ROOM_MOB_X, ROOM_MOB_Y, ROOM_MOB_Z,
                    Set.of(), mob.method_36454(), mob.method_36455(), false);
            if (!moved) {
                // Nothing left the outer world, so the leash goes back exactly where it was.
                leashable.method_60964(this, true);
                LOGGER.warn("[InnerDimension] Could not move a leashed mob into the room; leash restored");
                continue;
            }
            // ...the lead lands at the stick, outside the dimension, and the source is recorded
            // against whatever entity instance the teleport produced.
            this.method_5775(origin, new class_1799(class_1802.field_8719));
            if (state != null) {
                class_1297 arrived = room.method_73284(mob.method_5667());
                UUID key = arrived == null ? mob.method_5667() : arrived.method_5667();
                if (mob instanceof class_3222) {
                    state.setPlayerReturn(key, originId, fromX, fromY, fromZ,
                            mob.method_36454(), mob.method_36455());
                }
                state.setEntrySource(key, new InnerState.EntrySource(originId, this.method_5667(),
                        dildoX, dildoY, dildoZ, fromX, fromY, fromZ, roomOwner));
            }
        }

        for (int i = 0; i < this.container.method_5439(); i++) {
            // Non-destructive read: the slot is only lowered once the drop really exists.
            class_1799 snapshot = this.container.method_5438(i).method_7972();
            if (snapshot.method_7960()) {
                continue;
            }
            int snapshotCount = snapshot.method_7947();
            class_1799 payload = snapshot.method_7972();
            ModExitComponents.attach(payload, new InnerState.EntrySource(originId, this.method_5667(),
                    dildoX, dildoY, dildoZ, dildoX, dildoY, dildoZ, roomOwner));
            class_1542 item = new class_1542(room, ROOM_ITEM_X, ROOM_ITEM_Y, ROOM_ITEM_Z, payload);
            item.method_5814(ROOM_ITEM_X, ROOM_ITEM_Y, ROOM_ITEM_Z);
            // The spawn result decides whether the source slot may change at all.
            if (!room.method_8649(item)) {
                LOGGER.warn("[InnerDimension] Could not place the content of slot {} in the room; "
                        + "the slot is left untouched", i);
                continue;
            }
            class_1799 current = this.container.method_5438(i);
            if (current.method_7947() != snapshotCount) {
                // The slot changed underneath us: do not deduct a stack that is no longer the one
                // that was copied.
                LOGGER.warn("[InnerDimension] Slot {} changed during the input; keeping the current stack", i);
                continue;
            }
            this.container.method_5447(i, class_1799.field_8037);
        }
        this.container.method_5431();
    }

    /**
     * Sends the living passengers of a leashed boat into the room, and nothing else.
     *
     * <p>The boat itself never crosses: it is not teleported, its leash is never detached and no
     * lead is ever dropped for it. Whatever the outcome for one passenger, the hull and its tether
     * are exactly as they were before, which is also what makes the whole thing repeatable - a new
     * rider can be carried in on the same boat later on.</p>
     *
     * <p>{@code leashable} is only part of the signature so it is obvious which tethered object
     * this call belongs to; none of its detach or re-attach operations are used here.</p>
     */
    private void moveBoatPassengersInto(
            class_9817 leashable,
            class_10255 boat,
            class_3218 origin,
            class_3218 room,
            UUID roomOwner,
            InnerState state,
            class_2960 originId,
            double dildoX,
            double dildoY,
            double dildoZ,
            boolean ownerEntryGuarded) {
        // getPassengers() is the direct rider list, which is exactly "sitting in the boat":
        // an ordinary boat carries at most two, so no nearby-entity or world-wide scan is needed.
        List<class_1297> passengers = new ArrayList<>(boat.method_5685());
        List<class_1297> livingPassengers = new ArrayList<>();
        for (class_1297 passenger : passengers) {
            if (passenger instanceof class_1309) {
                livingPassengers.add(passenger);
            }
        }
        // An empty boat transports nobody, and this branch may not touch any entity state.
        if (livingPassengers.isEmpty()) {
            return;
        }

        class_3222 firstPlayer = null;
        for (class_1297 passenger : livingPassengers) {
            if (passenger instanceof class_3222 player) {
                firstPlayer = player;
                break;
            }
        }
        // DILDO_MOVED normally has no direct player target, but a tethered boat can still carry
        // players. That path must trip the same device breaker before anybody is dismounted.
        if (!ownerEntryGuarded && firstPlayer != null
                && !EntrySafetyGuard.allowDildoPlayerFeed(this.method_5667(), firstPlayer)) {
            return;
        }

        // One passenger failing must not stop the others already in this snapshot: each is
        // dismounted, teleported and - on failure - put straight back on the boat.
        for (class_1297 passenger : livingPassengers) {
            if (passenger instanceof class_3222 player
                    && (!ownerEntryGuarded || !player.method_5667().equals(roomOwner))
                    && !EntrySafetyGuard.allowPlayerEntry(player, roomOwner)) {
                // A blocked passenger stays mounted. In particular, never dismount a belted owner:
                // repeated server-side dismounts without moving the boat desynchronise its client
                // tracking and make the hull appear to vanish.
                continue;
            }
            if (passenger instanceof SensoryLinkDollEntity || passenger instanceof EnderPearlDildoEntity
                    || !com.innerdimension.transport.SafeEntityTeleport.prepare(passenger)) continue;
            double fromX = passenger.method_23317();
            double fromY = passenger.method_23318();
            double fromZ = passenger.method_23321();
            float fromYaw = passenger.method_36454();
            float fromPitch = passenger.method_36455();
            passenger.method_5848(); // step off the boat, never off the leash
            boolean moved = com.innerdimension.transport.SafeEntityTeleport.move(passenger, room,
                    ROOM_MOB_X, ROOM_MOB_Y, ROOM_MOB_Z,
                    Set.of(), passenger.method_36454(), passenger.method_36455(), false);
            if (!moved) {
                // The passenger is still outside; put them back where they were sitting. No source
                // record is written for a passenger that never arrived.
                passenger.method_5873(boat, true, true);
                LOGGER.warn("[InnerDimension] Could not move a boat passenger into the room; "
                        + "put back on the boat");
                continue;
            }
            if (state != null) {
                class_1297 arrived = room.method_73284(passenger.method_5667());
                UUID key = arrived == null ? passenger.method_5667() : arrived.method_5667();
                if (passenger instanceof class_3222) {
                    state.setPlayerReturn(key, originId, fromX, fromY, fromZ, fromYaw, fromPitch);
                }
                if (key.equals(roomOwner) && passenger instanceof class_3222) {
                    // The owner entering their own room must always retain a personal way out.
                    // A generic EntrySource routes players through the current strong port; when
                    // no strong port exists that made a boat-carried owner permanently unable to
                    // leave. Other passengers intentionally keep the strong-port rule below.
                    state.clearEntrySource(key);
                } else {
                    state.setEntrySource(key, new InnerState.EntrySource(
                            originId, this.method_5667(),
                            dildoX, dildoY, dildoZ,
                            fromX, fromY, fromZ,
                            roomOwner));
                }
            }
        }
    }

    @Override
    protected void method_5749(class_11368 nbt) {
        super.method_5749(nbt);
        // The superclass restores PersistenceRequired (default false in older saves),
        // so reapply after loading to protect both existing and newly placed variants.
        this.method_5971();
        String owner = nbt.method_71428("PlushieOwner", "");
        this.plushieOwner = null;
        if (!owner.isEmpty()) {
            try {
                this.plushieOwner = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
                // Malformed uuid in the save: treat the stick as having no doll.
            }
        }
        // Survives the chunk reload that every trip out of the inner dimension causes.
        this.plushieTriggered = nbt.method_71433("PlushieTriggered", false);
        this.boundDoll = readUuid(nbt.method_71428("BoundDoll", ""));
        this.migrationVersion = nbt.method_71424("DataVersion", 0);
        // Security data: the placer is only ever what was really written, never inferred.
        this.securityVersion = nbt.method_71424("SecurityVersion", 0);
        this.placerUuid = readUuid(nbt.method_71428("PlacerUuid", ""));
        this.authorizedOwnerUuid = readUuid(nbt.method_71428("AuthorizedOwnerUuid", ""));
        this.rejectedOwnerUuid = readUuid(nbt.method_71428("RejectedOwnerUuid", ""));
        this.contactSuppressed = nbt.method_71433("SecuritySuppressed", false);
        // Which piston was pushing this stick has to survive an unload too.
        this.pistonTow.readNbt(nbt);
        // A reloaded stick rejoins the binding sweeps, so its connection is revalidated once
        // the chunk that holds its doll is loaded again. An unloaded doll is never taken as
        // evidence that the connection is gone.
        DollBindingManager.track(this);
        for (int i = 0; i < this.container.method_5439(); i++) {
            this.container.method_5447(i, nbt.<class_1799>method_71426("Slot" + i, class_1799.field_24671)
                    .orElse(class_1799.field_8037));
        }
    }

    /** Parses a stored UUID, treating malformed or absent text as "not set". */
    private static UUID readUuid(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException malformed) {
            return null;
        }
    }

    /** The doll this stick believes it is connected to, or {@code null}. */
    public UUID getBoundDoll() {
        return this.boundDoll;
    }

    /** Called by the binding manager once a connection to a doll was established. */
    public void setBoundDoll(UUID dollUuid) {
        this.boundDoll = dollUuid;
    }

    /** Clears every identity supplied by a removed doll; the original placer is only history. */
    public void clearBindingIdentity() {
        this.boundDoll = null;
        this.plushieOwner = null;
        this.authorizedOwnerUuid = null;
        this.rejectedOwnerUuid = null;
        this.plushieTriggered = false;
        this.contactSuppressed = false;
    }

    /** The player whose dimension this stick feeds, from the placed doll or the connection. */
    public UUID getPlushieOwner() {
        return this.plushieOwner;
    }

    /**
     * True when this stick was written by a build that only stored {@code PlushieOwner}.
     *
     * <p>Those sticks are the ones the migration pass has to register, and for which the default
     * strong port of the dimension must be picked deterministically instead of by chunk order.</p>
     */
    public boolean isLegacyDildo() {
        return this.migrationVersion < DATA_VERSION;
    }

    /** Marks the stored binding data as up to date, so it never migrates twice. */
    public void markMigrated() {
        this.migrationVersion = DATA_VERSION;
    }

    // ------------------------------------------------------------------ security

    /** The player who placed this stick, or {@code null} on an old save that never recorded one. */
    public UUID getPlacerUuid() {
        return this.placerUuid;
    }

    /** Fixed at placement time, before any detection runs. */
    public void setPlacerUuid(UUID placer) {
        this.placerUuid = placer;
    }

    /** The doll owner this stick is explicitly authorized for, or {@code null}. */
    public UUID getAuthorizedOwner() {
        return this.authorizedOwnerUuid;
    }

    /**
     * Records the authorization that only a successful placement by the doll's own owner grants.
     *
     * <p>It also clears a previous refusal for that same owner, which is exactly the "the owner
     * showed up and claimed it" path the security spec describes; a rescan can never do this.</p>
     */
    public void authorize(UUID ownerUuid) {
        this.authorizedOwnerUuid = ownerUuid;
        if (ownerUuid != null && ownerUuid.equals(this.rejectedOwnerUuid)) {
            this.rejectedOwnerUuid = null;
        }
    }

    /** The candidate owner this stick refused, or {@code null}. */
    public UUID getRejectedOwner() {
        return this.rejectedOwnerUuid;
    }

    /** Records a refusal so it survives rescans, reloads and a temporarily absent candidate. */
    public void rejectOwner(UUID ownerUuid) {
        if (ownerUuid != null) {
            this.rejectedOwnerUuid = ownerUuid;
        }
    }

    /** True when contact with a locked or refused port must not become an input request. */
    public boolean isContactSuppressed() {
        return this.contactSuppressed;
    }

    /** Suppresses the current contact until the doll has fully left this stick's cell. */
    public void suppressContactUntilSeparated() {
        this.contactSuppressed = true;
    }

    /** Re-arms the input trigger after the doll left the cell. */
    public void clearContactSuppression() {
        this.contactSuppressed = false;
    }

    /**
     * Explicit teardown - destroyed by a hit, or otherwise discarded - drops this stick's
     * connection, including the strong pointer when it pointed here. A plain chunk unload
     * reports a different reason and leaves the binding alone.
     */    @Override
    public void method_5650(class_1297.class_5529 reason) {
        if (reason == class_1297.class_5529.field_26999 && !this.method_73183().method_8608()) {
            try {
                DollBindingManager.untrack(this);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to drop the connections of a dildo", throwable);
            }
        }
        super.method_5650(reason);
    }

    @Override
    protected void method_5652(class_11372 nbt) {
        super.method_5652(nbt);
        nbt.method_71469("PlushieOwner", this.plushieOwner == null ? "" : this.plushieOwner.toString());
        nbt.method_71472("PlushieTriggered", this.plushieTriggered);
        if (this.boundDoll != null) {
            nbt.method_71469("BoundDoll", this.boundDoll.toString());
        }
        nbt.method_71465("DataVersion", DATA_VERSION);
        nbt.method_71465("SecurityVersion", SECURITY_VERSION);
        if (this.placerUuid != null) {
            nbt.method_71469("PlacerUuid", this.placerUuid.toString());
        }
        if (this.authorizedOwnerUuid != null) {
            nbt.method_71469("AuthorizedOwnerUuid", this.authorizedOwnerUuid.toString());
        }
        if (this.rejectedOwnerUuid != null) {
            nbt.method_71469("RejectedOwnerUuid", this.rejectedOwnerUuid.toString());
        }
        nbt.method_71472("SecuritySuppressed", this.contactSuppressed);
        // The piston binding has to survive an unload, or a stick put away mid-stroke comes back
        // with no idea that it was being retracted.
        this.pistonTow.writeNbt(nbt);
        for (int i = 0; i < this.container.method_5439(); i++) {
            class_1799 stack = this.container.method_5438(i);
            // Writing an empty stack would fail validation (count 0 is not a legal stack) and
            // spams "[Serialization errors] ... Value must be within range [1;1000000000]".
            // Missing slots read back as empty anyway, so just skip them.
            if (stack.method_7960()) {
                continue;
            }
            nbt.method_71477("Slot" + i, class_1799.field_24671, stack);
        }
    }

    @Override
    public boolean method_64397(class_3218 world, class_1282 source, float amount) {
        if (!this.method_31481()) {
            // Always empty the container: a plain stick has nothing in it, and a stick
            // placed by an older build must not swallow whatever was stored inside.
            for (int i = 0; i < this.container.method_5439(); i++) {
                class_1799 stack = this.container.method_5441(i);
                if (!stack.method_7960()) {
                    this.method_5775(world, stack);
                }
            }
            this.method_5775(world, new class_1799(
                    this.isTethered() ? ModItems.TETHERED_ENDER_PEARL_DILDO : ModItems.ENDER_PEARL_DILDO));
            this.method_31472();
        }
        return true;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void method_5773() {
        super.method_5773();
        if (this.method_73183().method_8608()) {
            return;
        }
        this.age++;
        this.snapToBlockCenter();
        this.pistonTow.tick(this);
        // A stick that was just pushed looks for its doll again immediately, instead of waiting
        // out the full rescan period on its new cell.
        class_2338 cell = this.method_24515();
        if (this.sweepCell == null || !this.sweepCell.equals(cell)) {
            this.sweepCell = cell;
            DollBindingManager.markMoved(this);
        }
        this.checkPlushieFeed();
    }

    /**
     * Piston pushes shove this entity around and collisions can leave it straddling a
     * block border, which would make the "doll landed on the stick" test unreliable.
     * Whenever the stick is resting and not moving we pull it back to the centre of its
     * own block so it always occupies exactly one block cell.
     */
    private void snapToBlockCenter() {
        class_243 velocity = this.method_18798();
        if (!this.method_24828()
                || Math.abs(velocity.field_1352) > 0.001D
                || Math.abs(velocity.field_1350) > 0.001D) {
            return;
        }
        double centerX = Math.floor(this.method_23317()) + 0.5D;
        double centerZ = Math.floor(this.method_23321()) + 0.5D;
        double centerY = Math.floor(this.method_23318());
        boolean offCenter = Math.abs(this.method_23317() - centerX) > 0.002D
                || Math.abs(this.method_23321() - centerZ) > 0.002D
                || Math.abs(this.method_23318() - centerY) > 0.002D;
        if (offCenter) {
            this.method_5814(centerX, centerY, centerZ);
        }
    }

    /**
     * Fires when the doll ends up in the very same block cell as this stick: either the doll
     * was pushed onto the stick, or the stick was pushed onto the doll. The first case teleports
     * the doll's bound player, the second only moves the stick's contents (mobs tied to it and
     * the items stored inside).
     *
     * <p>Sitting on top of the stick is <em>not</em> this state: that is where right-click places
     * the doll, and it only means the stick is bound to it.</p>
     */
    private void checkPlushieFeed() {
        class_2338 cell = this.method_24515();
        UUID owner = feedOwnerAt(cell);
        if (owner == null) {
            this.plushieTriggered = false;
            this.lastCell = cell;
            // The contact ended, so a suppressed one may arm again for the next arrival.
            if (this.contactSuppressed) {
                this.clearContactSuppression();
            }
            return;
        }

        boolean stickMoved = this.lastCell != null && !this.lastCell.equals(cell);
        boolean firstTickAfterLoad = this.lastCell == null;
        this.lastCell = cell;

        // A locked or refused pair must not queue anything, and the contact stays suppressed so
        // lifting the lock later cannot replay what happened while it was locked.
        DollSecurityManager.Gate gate = DollSecurityManager.forPort(this, owner);
        if (gate.blocked()) {
            this.suppressContactUntilSeparated();
            this.plushieTriggered = true; // consumed for this contact
            return;
        }
        if (this.contactSuppressed || this.plushieTriggered) {
            return;
        }
        this.plushieTriggered = true;
        if (firstTickAfterLoad) {
            // A freshly loaded stick cannot tell whether the two moved into each other or were
            // already like this when the save was written, so it never triggers on the first tick.
            return;
        }
        if (this.method_73183() instanceof class_3218 serverWorld) {
            class_3222 player = serverWorld.method_8503().method_3760().method_14602(owner);
            boolean pushed = stickMoved && com.innerdimension.progression.PistonProgress.recent(this);
            if (!stickMoved) {
                for (SensoryLinkDollEntity doll : serverWorld.method_8390(SensoryLinkDollEntity.class, new class_238(cell), candidate -> true)) {
                    if (owner.equals(DollBindingManager.ownerOf(doll)) && com.innerdimension.progression.PistonProgress.recent(doll)) pushed = true;
                }
            }
            if (player != null && pushed) com.innerdimension.progression.InnerAdvancements.grant(player, "relativity", stickMoved ? "dildo_moved" : "doll_moved");
        }
        DynamicDimensionManager.queueFeed(this, owner,
                stickMoved ? DynamicDimensionManager.FeedCause.DILDO_MOVED
                        : DynamicDimensionManager.FeedCause.DOLL_MOVED);
    }

    /**
     * The dimension owner a doll standing in {@code cell} belongs to, or {@code null}.
     *
     * <p>The doll is not accepted just because it shares the cell: it has to be the doll this
     * stick is connected to, or at least name the owner this stick was bound to. Otherwise a
     * second player's doll shoved into the same cell would feed its own player through this
     * stick, which is exactly the mix-up of doll UUIDs and owners the specification forbids.</p>
     */
    private UUID feedOwnerAt(class_2338 cell) {
        class_1937 world = this.method_73183();
        UUID boundOwner = DollBindingManager.ownerOfDildo(this);
        if (boundOwner == null) return null;
        for (SensoryLinkDollEntity doll : world.method_8390(SensoryLinkDollEntity.class, new class_238(cell),
                candidate -> true)) {
            if (!doll.method_24515().equals(cell)) {
                continue;
            }
            UUID dollOwner = DollBindingManager.ownerOf(doll);
            if (dollOwner == null) {
                continue;
            }
            if (boundOwner == null || boundOwner.equals(dollOwner)) {
                return dollOwner;
            }
            LOGGER.debug("[InnerDimension] Ignoring doll {} above stick {}: it belongs to {}, not {}",
                    doll.method_5667(), this.method_5667(), dollOwner, boundOwner);
        }
        // A placed sensory link doll block that was never converted still counts, but only when the
        // owner stored in its own sensory link data is the owner this stick is bound to.
        if (this.plushieOwner == null
                || !(world.method_8321(cell) instanceof nonplayerplushie.SensoryLinkDollBlockEntity placed)) {
            return null;
        }
        UUID placedOwner = nonplayerplushie.SensoryLinkData.ownerUuid(placed.data());
        return this.plushieOwner.equals(placedOwner) ? this.plushieOwner : null;
    }
}
