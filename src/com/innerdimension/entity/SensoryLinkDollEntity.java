package com.innerdimension.entity;

import java.util.UUID;
import com.innerdimension.core.Tuning;
import com.innerdimension.security.DollSecurityManager;
import com.innerdimension.state.InnerState;
import com.mojang.authlib.GameProfile;
import net.minecraft.class_11368;
import net.minecraft.class_11372;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1282;
import net.minecraft.class_1657;
import net.minecraft.class_3222;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1313;
import net.minecraft.class_1314;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_2945.class_9222;
import net.minecraft.class_3218;
import net.minecraft.class_5132.class_5133;
import net.minecraft.class_5134;
import net.minecraft.class_9296;
import nonplayerplushie.SensoryLinkData;
import nonplayerplushie.SensoryLinkItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The sensory link doll placed on top of a stick.
 *
 * <p>A standalone entity (the same shape of entity as the stick) rather than a block:
 * vanilla pistons refuse to move any block that owns a block entity, and non_player_plushie's
 * sensory link doll is exactly such a block, so a block doll could never be pushed. Its renderer
 * re-creates the plushie player-head shape from non_player_plushie's own model, with the skin taken
 * from the doll's own profile, and breaking it returns a rebuilt
 * {@code non_player_plushie:sensory_link_doll} item instead of anything from this mod.</p>
 */
public class SensoryLinkDollEntity extends class_1314 {
    private static final class_2940<String> DATA_DOLL=class_2945.method_12791(SensoryLinkDollEntity.class,class_2943.field_13326);
    /** Complete upstream data, including appearance and future extension keys. */
    public net.minecraft.class_2487 getDollData(){
        String encoded=field_6011.method_12789(DATA_DOLL);
        if(!encoded.isEmpty())try{return net.minecraft.class_2522.method_67315(encoded);}catch(com.mojang.brigadier.exceptions.CommandSyntaxException invalid){LOGGER.warn("Invalid doll appearance data",invalid);}
        var profile=getProfile();var gp=profile==null?null:profile.method_73313();
        UUID id=ownerUuid!=null?ownerUuid:boundOwner!=null?boundOwner:gp==null?null:gp.id();
        String name=ownerName!=null?ownerName:gp==null?null:gp.name();
        return id!=null&&name!=null?SensoryLinkData.create(id,name,profile):new net.minecraft.class_2487();
    }
    public void setDollData(net.minecraft.class_2487 data){
        var copy=data==null?new net.minecraft.class_2487():data.method_10553();
        field_6011.method_12778(DATA_DOLL,copy.toString());
        setProfile(SensoryLinkData.profile(copy));
    }
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");
    /**
     * Head profile of the doll, which is also what provides its skin.
     *
     * <p>This has to live in the entity's {@code DataTracker} instead of a plain field: custom
     * entity NBT ({@code method_5652}/{@code method_5749}) only ever goes to the save file and
     * never to clients -- the spawn packet carries nothing but the id, type and transform. With a
     * plain field the renderer therefore saw a null profile on the client and drew nothing at all.
     * Vanilla's own profile-carrying entity, the mannequin ({@code class_11891}), syncs it the
     * same way.</p>
     */
    private static final class_2940<class_9296> DATA_PROFILE =
            class_2945.method_12791(SensoryLinkDollEntity.class, class_2943.field_63001);
    /**
     * Stand-in used whenever the doll has no profile.
     *
     * <p>A tracked-data entry must never start out {@code null}: {@code class_2945.class_2946}
     * keeps the initial value as its reference value and {@code method_46358()} calls
     * {@code reference.equals(current)} on every full sync, so a null default NPEs inside
     * {@code class_3231}'s constructor the moment the server starts tracking the doll and the
     * player never finishes joining the world. Vanilla's mannequin uses this very singleton
     * (an empty profile: no name, no uuid, no properties) as its default for that reason.</p>
     */
    private static final class_9296 NO_PROFILE = class_9296.class_11758.field_63034;
    /**
     * The display-only lock flag sent to clients.
     *
     * <p>The authority is the lock record in the world data; this flag only drives the visual hint,
     * and a client can never write it back. A server that receives a stale or forged flag therefore
     * changes nothing: every gate re-reads the record.</p>
     */
    private static final class_2940<Boolean> DATA_LOCKED =
            class_2945.method_12791(SensoryLinkDollEntity.class, class_2943.field_13323);
    /** Follows the sticky piston that shoved this doll, so it is pulled back like a block. */
    private final PistonTow pistonTow = new PistonTow();
    /** Let both halves of the piston stroke finish before aligning the doll. */
    private int pistonSnapDelay;
    /**
     * The dildo this doll is currently bound to, if any. Persisted so a reload can restore the
     * connection without re-running detection, and so a doll that is moved away keeps its
     * binding until it is explicitly removed.
     */
    private UUID boundDildo;
    /** The dimension owner resolved from this doll's profile when the binding was made. */
    private UUID boundOwner;
    /**
     * The owner this doll belongs to, as written by the placement that created it.
     *
     * <p>This is the binding identity. It comes from
     * {@code non_player_plushie}'s {@code SensoryLinkTargetResolver}, the only authority for it, and
     * is deliberately stored apart from the profile: a sensory link doll may legitimately carry no
     * profile at all (it then shows the default skin) while still naming its owner. Deriving the
     * owner from the skin profile, the entity uuid or the display name is not allowed.</p>
     */
    private UUID ownerUuid;
    private String ownerName;
    /** Authorization belongs to the placed doll and survives replacing the port below it. */
    private boolean ownerPlaced;
    /**
     * The lock this doll currently carries, as a local reference.
     *
     * <p>Only a reference: the authoritative record with the belt stack lives in the world data. A
     * local id that no longer resolves puts the pair into "waiting for verification" instead of
     * either denying forever or silently unlocking.</p>
     */
    private String lockId;
    /** Whether the authoritative lock record was already consulted after this entity loaded. */
    private boolean securityRefreshed;
    /** Consecutive ticks this doll has been standing exactly in its cell. */
    private int settledTicks;
    /** Whether a previous position sample exists, for the "no drift" rule. */
    private boolean settledSampleValid;
    private double settledSampleX;
    private double settledSampleY;
    private double settledSampleZ;

    public SensoryLinkDollEntity(class_1299<? extends SensoryLinkDollEntity> type, class_1937 world) {
        super(type, world);
        this.method_5977(true); // no AI
        this.method_5971(); // Placed dolls must survive vanilla distance despawning.
        this.method_5875(true); // no gravity, it floats where it is put
    }

    public static class_5133 createAttributes() {
        return class_1314.method_26828()
                .method_26868(class_5134.field_23716, 20.0)
                .method_26868(class_5134.field_23719, 0.0)
                .method_26868(class_5134.field_23718, 1.0);
    }

    @Override
    protected void method_5693(class_9222 tracker) {
        super.method_5693(tracker);
        tracker.method_56912(DATA_DOLL, "");
        tracker.method_56912(DATA_PROFILE, NO_PROFILE);
        tracker.method_56912(DATA_LOCKED, Boolean.FALSE);
    }

    /** The display-only lock flag; the authoritative lock record is always consulted separately. */
    public boolean isLockedDisplay() {
        return Boolean.TRUE.equals(this.field_6011.method_12789(DATA_LOCKED));
    }

    /** Updates the display-only lock flag. Never used as a permission check. */
    public void setLockedDisplay(boolean locked) {
        this.field_6011.method_12778(DATA_LOCKED, locked);
    }

    /** The lock id this doll references locally, or {@code null}. */
    public String getLockId() {
        return this.lockId;
    }

    /** Sets or clears the local lock reference; the authoritative record is stored elsewhere. */
    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    @Override
    public class_1269 method_5688(class_1657 player, class_1268 hand) {
        return this.handleSecurityInteraction(player, hand);
    }

    @Override
    protected class_1269 method_5992(class_1657 player, class_1268 hand) {
        return this.handleSecurityInteraction(player, hand);
    }

    /**
     * The lock and unlock click on the doll itself.
     *
     * <p>Security spec sections 5 and 6: only the main hand may lock, only the doll's own owner may
     * do either, unlocking needs both hands empty, and at most one lock state change happens per
     * player per server tick - otherwise one right-click would reach the main and the off hand and
     * lock and unlock in the same instant.</p>
     */
    private class_1269 handleSecurityInteraction(class_1657 player, class_1268 hand) {
        class_1937 world = this.method_73183();
        var appearanceItem=player.method_5998(hand);
        boolean shears=appearanceItem.method_7909() instanceof net.minecraft.class_1820;
        boolean wool=appearanceItem.method_31573(net.minecraft.class_3489.field_15544);
        if(shears||wool){
            if(!world.method_8608())setDollData(com.innerdimension.compat.PlushieDollData.withDestroyed(getDollData(),shears));
            return class_1269.field_5812;
        }
        if (world.method_8608() || !(player instanceof class_3222 actor)) {
            return class_1269.field_5811;
        }
        InnerState state = InnerState.get(world.method_8503().method_30002());
        if (hand == class_1268.field_5808) {
            class_1799 held = actor.method_5998(class_1268.field_5808);
            if (DollSecurityManager.isAcceptedBelt(held)) {
                EnderPearlDildoEntity dildo = boundDildoEntity(world);
                DollSecurityManager.LockOutcome outcome = dildo == null
                        ? DollSecurityManager.LockOutcome.MISSING_DILDO
                        : DollSecurityManager.tryLock(dildo, this, actor);
                if (outcome == DollSecurityManager.LockOutcome.LOCKED) {
                    DollSecurityManager.send(actor, DollSecurityManager.MSG_LOCK_DONE);
                } else {
                    // Say why, once, with the numbers. A silent refusal is what makes a working rule
                    // look broken.
                    LOGGER.info("[InnerDimension] Lock refused for doll {}: {} [{}]",
                            this.method_5667(), outcome,
                            outcome == DollSecurityManager.LockOutcome.MOVING ? this.settledDebug() : "-");
                    DollSecurityManager.send(actor, outcome == DollSecurityManager.LockOutcome.ALREADY_LOCKED
                            ? DollSecurityManager.MSG_LOCKED
                            : DollSecurityManager.MSG_LOCK_FAILED);
                }
                return class_1269.field_5812;
            }
        }
        if (state.lockOfDoll(this.method_5667()) != null || this.lockId != null) {
            DollSecurityManager.LockOutcome outcome = DollSecurityManager.tryUnlock(this, actor, state);
            if (outcome == DollSecurityManager.LockOutcome.UNLOCKED) {
                DollSecurityManager.send(actor, DollSecurityManager.MSG_UNLOCK_DONE);
            } else if (outcome != DollSecurityManager.LockOutcome.HANDS_NOT_EMPTY) {
                LOGGER.info("[InnerDimension] Unlock refused for doll {}: {}", this.method_5667(), outcome);
                if (outcome == DollSecurityManager.LockOutcome.NO_RETURNABLE_BELT) {
                    DollSecurityManager.send(actor, DollSecurityManager.MSG_LOCK_FAILED);
                }
            }
            return class_1269.field_5812;
        }
        return class_1269.field_5811;
    }

    /** The live dildo this doll is bound to, or {@code null} when it is not loaded. */
    public EnderPearlDildoEntity boundDildoEntity(class_1937 world) {
        if (this.boundDildo == null) {
            return null;
        }
        class_1297 entity = world.method_73284(this.boundDildo);
        return entity instanceof EnderPearlDildoEntity dildo ? dildo : null;
    }

    /** The doll's profile, or {@code null} when it has none (the tracker never holds null). */
    public class_9296 getProfile() {
        class_9296 profile = this.field_6011.method_12789(DATA_PROFILE);
        return profile == null || profile.method_73317().isEmpty() ? null : profile;
    }

    public void setProfile(class_9296 profile) {
        this.field_6011.method_12778(DATA_PROFILE, profile == null ? NO_PROFILE : profile);
    }

    /** Spawns a doll in the given cell with the skin profile its sensory link data carried. */
    public static SensoryLinkDollEntity place(class_1937 world, class_2338 cell, class_9296 profile,
                                              float yaw) {
        SensoryLinkDollEntity doll = new SensoryLinkDollEntity(ModEntities.SENSORY_LINK_DOLL, world);
        doll.method_5814(cell.method_10263() + 0.5D, cell.method_10264(), cell.method_10260() + 0.5D);
        doll.method_36456(yaw);
        doll.method_36457(0.0F);
        // A null profile is legal and only means "default skin"; the owner is a separate value.
        doll.setProfile(profile);
        if (!world.method_8649(doll)) {
            // The caller must not consume the placed item when the entity did not appear.
            doll.method_31472();
            return null;
        }
        return doll;
    }

    /**
     * The player this doll belongs to, or {@code null} when it has no identity yet.
     *
     * <p>Only the identity that the placement path recorded is used: an older doll written before
     * this field existed still resolves through its profile, which is the only place the identity
     * used to live.</p>
     */
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    /**
     * Records the binding identity of this doll.
     *
     * <p>Called by the placement path with the uuid the plushie mod's resolver returned, and by the
     * conversion of a placed doll block with the uuid of its own sensory link data.</p>
     */
    public void setOwnerUuid(UUID owner) {
        this.ownerUuid = owner;
    }

    public void setOwnerName(String name) { this.ownerName = name; }

    public boolean isOwnerPlaced() { return this.ownerPlaced; }

    public void markOwnerPlaced() { this.ownerPlaced = true; }

    /** The dildo this doll is bound to, or {@code null}. */
    public UUID getBoundDildo() {
        return this.boundDildo;
    }

    /** The dimension owner resolved for this doll's binding, or {@code null}. */
    public UUID getBoundOwner() {
        return this.boundOwner;
    }

    /** Records the connection this doll belongs to. Called by the binding manager. */
    public void setBoundDildo(UUID dildoUuid, UUID ownerUuid) {
        this.boundDildo = dildoUuid;
        this.boundOwner = ownerUuid;
    }

    /**
     * True when this doll is standing still in its own block cell.
     *
     * <p>Fixed conditions (implementation plan section 0.12): the re-alignment delay is over, every
     * axis of the velocity is at most {@code SETTLED_VELOCITY_EPSILON}, the position moved at most
     * {@code SETTLED_POSITION_EPSILON} since the previous server tick, the doll is back near the centre of
     * its cell and near an integer Y, and that has held for
     * {@code SETTLED_CONSECUTIVE_TICKS} ticks.</p>
     *
     * <p>The piston binding is deliberately not required to be idle: a doll held by an extended
     * piston that is waiting to be retracted is perfectly still, and refusing to use it would stall
     * every output around the machine.</p>
     *
     * <p>Note the two tolerances are different things. {@code SETTLED_POSITION_EPSILON} (0.002) is
     * the drift allowed <em>between two server ticks</em>; the cell alignment uses the looser
     * {@code SETTLED_CELL_TOLERANCE} (0.05), because vanilla pistons park a pushed entity with about
     * 0.01 blocks of separation. Using 0.002 as a distance-to-centre test made a perfectly still doll
     * look permanently "moving" and blocked locking.</p>
     */
    public boolean isSettled() {
        class_243 velocity = this.method_18798();
        return this.pistonSnapDelay == 0
                && this.settledTicks >= Tuning.SETTLED_CONSECUTIVE_TICKS
                && Math.abs(velocity.field_1352) <= Tuning.SETTLED_VELOCITY_EPSILON
                && Math.abs(velocity.field_1351) <= Tuning.SETTLED_VELOCITY_EPSILON
                && Math.abs(velocity.field_1350) <= Tuning.SETTLED_VELOCITY_EPSILON
                && Math.abs(this.method_23317() - this.settledSampleX) <= Tuning.SETTLED_POSITION_EPSILON
                && Math.abs(this.method_23318() - this.settledSampleY) <= Tuning.SETTLED_POSITION_EPSILON
                && Math.abs(this.method_23321() - this.settledSampleZ) <= Tuning.SETTLED_POSITION_EPSILON;
    }

    /** Samples once after the server tick's movement; queries never add still-tick evidence. */
    private boolean sampleSettled() {
        if (this.pistonSnapDelay > 0) {
            clearSettled();
            return false;
        }
        class_243 velocity = this.method_18798();
        if (Math.abs(velocity.field_1352) > Tuning.SETTLED_VELOCITY_EPSILON
                || Math.abs(velocity.field_1351) > Tuning.SETTLED_VELOCITY_EPSILON
                || Math.abs(velocity.field_1350) > Tuning.SETTLED_VELOCITY_EPSILON) {
            clearSettled();
            return false;
        }
        double x = this.method_23317();
        double y = this.method_23318();
        double z = this.method_23321();
        if (this.settledSampleValid) {
            double driftX = Math.abs(x - this.settledSampleX);
            double driftY = Math.abs(y - this.settledSampleY);
            double driftZ = Math.abs(z - this.settledSampleZ);
            this.settledSampleX = x;
            this.settledSampleY = y;
            this.settledSampleZ = z;
            if (driftX > Tuning.SETTLED_POSITION_EPSILON
                    || driftY > Tuning.SETTLED_POSITION_EPSILON
                    || driftZ > Tuning.SETTLED_POSITION_EPSILON) {
                this.settledTicks = 0;
                return false;
            }
        } else {
            this.settledSampleValid = true;
            this.settledSampleX = x;
            this.settledSampleY = y;
            this.settledSampleZ = z;
        }
        double centreX = Math.floor(x) + 0.5D;
        double centreZ = Math.floor(z) + 0.5D;
        boolean aligned = Math.abs(x - centreX) <= Tuning.SETTLED_CELL_TOLERANCE
                && Math.abs(z - centreZ) <= Tuning.SETTLED_CELL_TOLERANCE
                && Math.abs(y - Math.round(y)) <= Tuning.SETTLED_CELL_TOLERANCE;
        if (!aligned) {
            this.settledTicks = 0;
            return false;
        }
        if (this.settledTicks < Tuning.SETTLED_CONSECUTIVE_TICKS) {
            this.settledTicks++;
        }
        return this.settledTicks >= Tuning.SETTLED_CONSECUTIVE_TICKS;
    }

    /** Forgets the still-tick evidence, used whenever the doll is clearly not settled. */
    private void clearSettled() {
        this.settledSampleValid = false;
        this.settledTicks = 0;
    }

    /**
     * A one-line description of the doll's current state, attached to refusal logs.
     *
     * <p>Without the numbers a "still moving" refusal is indistinguishable from a wrong tolerance,
     * which is exactly what made the first in-game lock report hard to read.</p>
     */
    public String settledDebug() {
        class_243 velocity = this.method_18798();
        return String.format(java.util.Locale.ROOT,
                "pos=(%.4f,%.4f,%.4f) velocity=(%.4f,%.4f,%.4f) snapDelay=%d settledTicks=%d settled=%s",
                this.method_23317(), this.method_23318(), this.method_23321(),
                velocity.field_1352, velocity.field_1351, velocity.field_1350,
                this.pistonSnapDelay, this.settledTicks, this.isSettled());
    }

    /**
     * Explicit teardown: a doll that is killed, broken or otherwise discarded really is gone,
     * so its connections are dropped and, when it held the strong port, that pointer is
     * cleared. {@code method_31472()} reports {@code class_5529.field_26999}, while a plain
     * chunk unload reports a different reason - which is exactly why an unloaded doll must not
     * be confused with a destroyed one.
     */
    @Override
    public void method_5650(class_1297.class_5529 reason) {
        if (reason == class_1297.class_5529.field_26999 && !this.method_73183().method_8608()) {
            try {
                com.innerdimension.binding.DollBindingManager.clearConnectionsOfDoll(this);
            } catch (Throwable throwable) {
                LOGGER.error("[InnerDimension] Failed to drop the connections of a doll", throwable);
            }
        }
        super.method_5650(reason);
    }

    /**
     * Rebuilds the non_player_plushie sensory link doll item that this entity stands for.
     *
     * <p>The whole record goes back in through the plushie mod's own data contract, so the dropped
     * item is recognised as a doll again and stacks with a freshly crafted one. Nothing of the old
     * player-head shape is written: no {@code minecraft:player_head}, no custom name and no marker.</p>
     */
    public class_1799 createDollStack() {
        class_1799 stack = new class_1799(SensoryLinkItems.SENSORY_LINK_DOLL);
        var complete=getDollData();
        if(SensoryLinkData.valid(complete)){SensoryLinkData.write(stack,complete);return stack;}
        class_9296 dollProfile = this.getProfile();
        GameProfile gameProfile = dollProfile == null ? null : dollProfile.method_73313();
        UUID ownerUuid = this.ownerUuid != null ? this.ownerUuid : this.boundOwner;
        String ownerName = this.ownerName != null ? this.ownerName
                : (gameProfile == null ? null : gameProfile.name());
        if (ownerUuid == null && gameProfile != null && gameProfile.id() != null) {
            // Legacy dolls may have only a skin profile. Never overwrite a recorded binding UUID.
            ownerUuid = gameProfile.id();
        }
        if (ownerUuid == null || ownerName == null || ownerName.isBlank()) {
            // No trustworthy identity is available, so no owner is invented: the doll keeps its shape
            // but carries no sensory link data and can never be bound by a name guess.
            LOGGER.warn("[InnerDimension] Doll {} has no resolvable owner; dropping an unbound sensory "
                    + "link doll", this.method_5667());
            return stack;
        }
        SensoryLinkData.write(stack, SensoryLinkData.create(ownerUuid, ownerName, dollProfile));
        return stack;
    }

    @Override
    protected void method_5959() {
        // No AI ticking.
    }

    @Override
    protected void method_5749(class_11368 nbt) {
        super.method_5749(nbt);
        // Old saves may restore PersistenceRequired=false; migrate them on load.
        this.method_5971();
        this.setProfile(nbt.<class_9296>method_71426("DollProfile", class_9296.field_49359).orElse(null));
        this.ownerUuid = readUuid(nbt.method_71428("OwnerUuid", ""));
        String savedName = nbt.method_71428("OwnerName", "");
        this.ownerName = savedName.isBlank() ? null : savedName;
        // Existing owned doll entities retain their established authorization on upgrade.
        this.ownerPlaced = nbt.method_71433("OwnerPlaced", this.ownerUuid != null);
        this.boundDildo = readUuid(nbt.method_71428("BoundDildo", ""));
        this.boundOwner = readUuid(nbt.method_71428("BoundOwner", ""));
        this.setDollData(nbt.method_71426("DollData",net.minecraft.class_2487.field_25128).orElseGet(this::getDollData));
        // Only the local reference is stored; whether it really locks anything is decided by the
        // authoritative record in the world data, never by this string.
        String storedLock = nbt.method_71428("LockId", "");
        this.lockId = storedLock.isEmpty() ? null : storedLock;
        // A reloaded doll rejoins the sweep, and remembers which piston was pushing it.
        this.pistonTow.readNbt(nbt);
    }

    @Override
    protected void method_5652(class_11372 nbt) {
        super.method_5652(nbt);
        nbt.method_71468("DollData",net.minecraft.class_2487.field_25128,getDollData());
        // Skip when unset: encoding a null profile fails the write view's validation and logs
        // a "[Serialization errors]" warning. A missing key reads back as "no profile".
        class_9296 profile = this.getProfile();
        if (profile != null) {
            nbt.method_71477("DollProfile", class_9296.field_49359, profile);
        }
        // The binding identity survives a reload on its own, so a doll that carries no profile still
        // knows whose it is after the chunk was unloaded.
        if (this.ownerUuid != null) {
            nbt.method_71469("OwnerUuid", this.ownerUuid.toString());
        }
        if (this.ownerName != null) nbt.method_71469("OwnerName", this.ownerName);
        nbt.method_71472("OwnerPlaced", this.ownerPlaced);
        // The binding survives a chunk reload, so a restored doll still knows which dildo it
        // belonged to before detection runs again.
        if (this.boundDildo != null) {
            nbt.method_71469("BoundDildo", this.boundDildo.toString());
        }
        if (this.boundOwner != null) {
            nbt.method_71469("BoundOwner", this.boundOwner.toString());
        }
        if (this.lockId != null) {
            nbt.method_71469("LockId", this.lockId);
        }
        // Which piston is retracting this doll has to survive a chunk unload, otherwise a doll
        // that was put away mid-stroke comes back and never walks out of the piston's cell.
        this.pistonTow.writeNbt(nbt);
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

    @Override
    public boolean method_64397(class_3218 world, class_1282 source, float amount) {
        if (!this.method_31481()) {
            InnerState state = InnerState.get(world.method_8503().method_30002());
            if (state.lockOfDoll(this.method_5667()) != null || this.lockId != null) {
                // A locked doll cannot be removed to get around the lock: the owner unlocks first.
                return true;
            }
            // A single hit takes the doll down and gives back the plushie mod's own doll;
            // nothing from this mod drops.
            this.method_5775(world, this.createDollStack());
            this.method_31472();
        }
        return true;
    }

    @Override
    public void method_5784(class_1313 movementType, class_243 movement) {
        super.method_5784(movementType, movement);
        if (movementType == class_1313.field_6310) {
            this.pistonSnapDelay = 2;
        }
        if (movementType == class_1313.field_6310
                || movement.field_1352 != 0.0D || movement.field_1351 != 0.0D
                || movement.field_1350 != 0.0D) {
            clearSettled();
        }
    }

    @Override
    public void method_5773() {
        com.innerdimension.voiddomain.VoidContamination.dollFalling(this,getOwnerUuid());
        super.method_5773();
        if (this.method_73183().method_8608()) {
            return;
        }
        if (!this.securityRefreshed) {
            // The authoritative lock record is read once the entity is really in a server world,
            // so a stale local id can never keep a released port locked.
            this.securityRefreshed = true;
            class_1937 world = this.method_73183();
            if (world.method_8503() != null) {
                com.innerdimension.security.DollSecurityManager.refreshDisplayState(this,
                        InnerState.get(world.method_8503().method_30002()));
            }
        }
        // No AI/travel runs for this anchored decoration. Collision impulses otherwise remain
        // forever in delta movement, even though they never move the doll. Pistons move it
        // directly through method_5784 and retain their own snap delay and tow state.
        this.method_18800(0.0D, 0.0D, 0.0D);
        this.snapToCell();
        this.pistonTow.tick(this);
        this.sampleSettled();
    }

    /** Keeps the floating doll centred in exactly one block cell after pushes. */
    private void snapToCell() {
        if (this.pistonSnapDelay > 0) {
            this.pistonSnapDelay--;
            return;
        }
        class_243 velocity = this.method_18798();
        if (Math.abs(velocity.field_1352) > 0.05D || Math.abs(velocity.field_1350) > 0.05D
                || Math.abs(velocity.field_1351) > 0.05D) {
            return;
        }
        double centerX = Math.floor(this.method_23317()) + 0.5D;
        // Vanilla adds 0.01 blocks of piston separation. Flooring a downward stroke
        // (e.g. 63.99 instead of 64) incorrectly drops the doll one extra cell.
        double centerY = Math.floor(this.method_23318() + 0.5D);
        double centerZ = Math.floor(this.method_23321()) + 0.5D;
        if (Math.abs(this.method_23317() - centerX) > 0.002D
                || Math.abs(this.method_23321() - centerZ) > 0.002D
                || Math.abs(this.method_23318() - centerY) > 0.002D) {
            this.method_5814(centerX, centerY, centerZ);
        }
    }

}
