package com.innerdimension.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.class_18;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_4284;
import net.minecraft.class_10741;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Persistent state stored on the overworld.
 *
 * <p>Holds: (1) the set of room owner UUIDs, (2) the block shell layout version of each
 * room, (3) every player's return point, (4) the dildo/doll bindings of every dimension
 * owner and (5) the entry source of every non-player object that was fed into a room.</p>
 *
 * <p>The dimension owner is always the identity the sensory link doll itself carries, which the
 * placement path reads from non_player_plushie's sensory link data. Doll entity UUIDs and dildo
 * entity UUIDs only identify the actual connection objects; they never define ownership.</p>
 *
 * <p>Every field added by this stage is optional on read, so saves written by earlier
 * builds still load: {@code rooms}, {@code layout} and {@code returns} stay readable and
 * each new map defaults to empty.</p>
 */
public class InnerState extends class_18 {
    /**
     * Schema version of the stored data. Bumped whenever the meaning of a stored field
     * changes, so a reader can decide whether a migration pass is needed.
     */
    public static final int DATA_VERSION = 4;

    /** Only chunks this mod added to vanilla forceload; persisted for safe switch/restart cleanup. */
    public record ForcedPortChunk(class_2960 dim, int x, int z) {
        public static final Codec<ForcedPortChunk> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(class_2960.field_25139.fieldOf("dim").forGetter(ForcedPortChunk::dim),
                        Codec.INT.fieldOf("x").forGetter(ForcedPortChunk::x),
                        Codec.INT.fieldOf("z").forGetter(ForcedPortChunk::z))
                        .apply(instance, ForcedPortChunk::new));
    }

    public static final Codec<UUID> UUID_CODEC =
            Codec.STRING.xmap(UUID::fromString, UUID::toString);

    /** A player's saved return location. */
    public static final class ReturnPoint {
        public final class_2960 dimId;
        public final double x;
        public final double y;
        public final double z;
        public final float yaw;
        public final float pitch;

        public ReturnPoint(class_2960 dimId, double x, double y, double z, float yaw, float pitch) {
            this.dimId = dimId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static final Codec<ReturnPoint> RETURN_POINT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    class_2960.field_25139.fieldOf("dim").forGetter(point -> point.dimId),
                    Codec.DOUBLE.fieldOf("x").forGetter(point -> point.x),
                    Codec.DOUBLE.fieldOf("y").forGetter(point -> point.y),
                    Codec.DOUBLE.fieldOf("z").forGetter(point -> point.z),
                    Codec.FLOAT.fieldOf("yaw").forGetter(point -> point.yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(point -> point.pitch)
            ).apply(instance, ReturnPoint::new));

    /**
     * One dildo-to-doll connection, keyed by the dildo's entity UUID.
     *
     * <p>{@code verified} only caches the outcome of the last completed validation pass. A
     * connection whose doll cannot currently be seen because its chunk is unloaded must not
     * be read as "gone": it stays in the set with {@code verified == false} until the chunk
     * is loaded and the entity load has finished.</p>
     */
    public static final class Binding {
        public final UUID dildoUuid;
        public final UUID dollUuid;
        public final UUID ownerUuid;
        public final class_2960 dimId;
        /** Position of the dildo, i.e. of the connection itself. */
        public final double x;
        public final double y;
        public final double z;
        /**
         * Last known position of the doll, which is where a non-player object is sent out.
         *
         * <p>Kept separately from the dildo position because the doll is the thing that can be
         * pushed away by a piston: cleaning up an invalid connection requires knowing which
         * chunk has to be loaded before "the entity is not there" may be believed.</p>
         */
        public final double dollX;
        public final double dollY;
        public final double dollZ;
        public boolean verified;
        /**
         * True when this connection was established after the identity check succeeded.
         *
         * <p>It is stored with the connection so the exit resolver can decide without the entities
         * being loaded: a connection restored from an old save has {@code authorized == false} and
         * therefore waits for its owner to claim it, instead of quietly transferring anything
         * through a port whose authorization nobody ever proved.</p>
         */
        public final boolean authorized;
        /** Revision of this connection's identity, useful to detect stale resolutions. */
        public final int bindingRevision;

        public Binding(UUID dildoUuid, UUID dollUuid, UUID ownerUuid, class_2960 dimId,
                       double x, double y, double z, boolean verified) {
            this(dildoUuid, dollUuid, ownerUuid, dimId, x, y, z, x, y + 1.0D, z, verified, false, 0);
        }

        public Binding(UUID dildoUuid, UUID dollUuid, UUID ownerUuid, class_2960 dimId,
                       double x, double y, double z,
                       double dollX, double dollY, double dollZ, boolean verified) {
            this(dildoUuid, dollUuid, ownerUuid, dimId, x, y, z, dollX, dollY, dollZ, verified, false, 0);
        }

        public Binding(UUID dildoUuid, UUID dollUuid, UUID ownerUuid, class_2960 dimId,
                       double x, double y, double z,
                       double dollX, double dollY, double dollZ, boolean verified,
                       boolean authorized, int bindingRevision) {
            this.dildoUuid = dildoUuid;
            this.dollUuid = dollUuid;
            this.ownerUuid = ownerUuid;
            this.dimId = dimId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dollX = dollX;
            this.dollY = dollY;
            this.dollZ = dollZ;
            this.verified = verified;
            this.authorized = authorized;
            this.bindingRevision = bindingRevision;
        }

        /** A copy sitting at the same connection, with the doll position moved. */
        public Binding withDollPosition(double newDollX, double newDollY, double newDollZ) {
            return new Binding(this.dildoUuid, this.dollUuid, this.ownerUuid, this.dimId,
                    this.x, this.y, this.z, newDollX, newDollY, newDollZ, this.verified,
                    this.authorized, this.bindingRevision);
        }

        /** A copy marked as verified or not, which never changes identity. */
        public Binding withVerified(boolean newVerified) {
            return new Binding(this.dildoUuid, this.dollUuid, this.ownerUuid, this.dimId,
                    this.x, this.y, this.z, this.dollX, this.dollY, this.dollZ, newVerified,
                    this.authorized, this.bindingRevision);
        }
    }

    public static final Codec<Binding> BINDING_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUID_CODEC.fieldOf("dildo").forGetter(binding -> binding.dildoUuid),
                    UUID_CODEC.fieldOf("doll").forGetter(binding -> binding.dollUuid),
                    UUID_CODEC.fieldOf("owner").forGetter(binding -> binding.ownerUuid),
                    class_2960.field_25139.optionalFieldOf("dim", class_2960.method_60655("minecraft", "overworld"))
                            .forGetter(binding -> binding.dimId),
                    Codec.DOUBLE.fieldOf("x").forGetter(binding -> binding.x),
                    Codec.DOUBLE.fieldOf("y").forGetter(binding -> binding.y),
                    Codec.DOUBLE.fieldOf("z").forGetter(binding -> binding.z),
                    // Absent on saves written before the doll position was tracked: the doll stands
                    // one block above the dildo in the ordinary state, which is the best default.
                    Codec.DOUBLE.optionalFieldOf("dollX").forGetter(b -> Optional.of(b.dollX)),
                    Codec.DOUBLE.optionalFieldOf("dollY").forGetter(b -> Optional.of(b.dollY)),
                    Codec.DOUBLE.optionalFieldOf("dollZ").forGetter(b -> Optional.of(b.dollZ)),
                    Codec.BOOL.optionalFieldOf("verified", false).forGetter(binding -> binding.verified),
                    // Default false on purpose: a record from before authorization existed must be
                    // claimed by its owner rather than treated as an authorized port.
                    Codec.BOOL.optionalFieldOf("authorized", false).forGetter(b -> b.authorized),
                    Codec.INT.optionalFieldOf("revision", 0).forGetter(b -> b.bindingRevision)
            ).apply(instance, (dildo, doll, owner, dimId, x, y, z, dollX, dollY, dollZ, verified,
                               authorized, revision) ->
                    new Binding(dildo, doll, owner, dimId, x, y, z,
                            dollX.orElse(x), dollY.orElse(y + 1.0D), dollZ.orElse(z), verified,
                            authorized, revision)));

    /**
     * Everything one dimension owner knows about its output ports.
     *
     * <p>The weak/strong distinction is derived from the single {@link #strongDildoUuid}
     * pointer rather than duplicated onto every binding: a connection is strong exactly when
     * it is that pointer, and every other connection in the set is weak. Promoting a new
     * strong port therefore only moves the pointer, and the previous holder degrades to weak
     * on its own while staying in the set.</p>
     */
    public static final class BindingSet {
        public final Map<UUID, Binding> bindings = new HashMap<>();
        /** The one strong output port, or {@code null} when the dimension has none. */
        public UUID strongDildoUuid;
        /**
         * True once this dimension ever had a valid connection. Drives the rule that the
         * first connection becomes the default strong binding, and that removing the strong
         * port later never silently promotes one of the remaining weak ones.
         */
        public boolean hasEverBound;
        /**
         * Bumped whenever the identity of the set changes: a connection is registered or removed,
         * or the strong pointer moves. A resolution computed before such a change is stale and is
         * re-checked before anything is transferred.
         */
        public int bindingRevision;

        public BindingSet() {
        }

        public BindingSet(UUID strongDildoUuid, boolean hasEverBound, Map<UUID, Binding> bindings) {
            this(strongDildoUuid, hasEverBound, bindings, 0);
        }

        public BindingSet(UUID strongDildoUuid, boolean hasEverBound, Map<UUID, Binding> bindings,
                          int bindingRevision) {
            this.strongDildoUuid = strongDildoUuid;
            this.hasEverBound = hasEverBound;
            this.bindings.putAll(bindings);
            this.bindingRevision = bindingRevision;
        }
    }

    public static final Codec<BindingSet> BINDING_SET_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUID_CODEC.optionalFieldOf("strong").forGetter(set -> Optional.ofNullable(set.strongDildoUuid)),
                    Codec.BOOL.optionalFieldOf("everBound", false).forGetter(set -> set.hasEverBound),
                    Codec.unboundedMap(UUID_CODEC, BINDING_CODEC)
                            .optionalFieldOf("bindings", Collections.emptyMap())
                            .forGetter(set -> set.bindings),
                    Codec.INT.optionalFieldOf("revision", 0).forGetter(set -> set.bindingRevision)
            ).apply(instance, (strong, everBound, bindings, revision) ->
                    new BindingSet(strong.orElse(null), everBound, bindings, revision)));

    /**
     * Where one non-player object came from, so it can be sent back to that exact place when
     * the dimension has no strong output port.
     *
     * <p>Stored per object - per item stack, per mob - rather than per dimension: two objects
     * that entered through different dildos must each leave through their own entry.</p>
     *
     * <p>This is a record on purpose. The value is stored on an {@code ItemStack} as a data
     * component, and components take part in stack equality, so two sources describing the same
     * entry have to be equal - otherwise identical items fed through the same dildo would refuse to
     * merge for no visible reason.</p>
     *
     * <p>{@code valid} is false when a record written in the current format is missing required
     * fields or holds non-finite coordinates. Such a source is never silently repaired into
     * "overworld (0,0,0)": the exit resolver treats it as "no usable source" instead. Old records
     * (format 1) were written without the marker and keep their historical, including zero,
     * coordinates.</p>
     */
    public record EntrySource(class_2960 dimId, UUID entryDildoUuid,
                              double dildoX, double dildoY, double dildoZ,
                              double fromX, double fromY, double fromZ,
                              UUID roomOwner, boolean valid) {
        /** Format written by this build; anything lower is an old, leniently read record. */
        public static final int FORMAT = 2;

        /** A source this build just built: complete and therefore valid. */
        public EntrySource(class_2960 dimId, UUID entryDildoUuid,
                           double dildoX, double dildoY, double dildoZ,
                           double fromX, double fromY, double fromZ,
                           UUID roomOwner) {
            this(dimId, entryDildoUuid, dildoX, dildoY, dildoZ, fromX, fromY, fromZ, roomOwner,
                    dimId != null && isFinite(dildoX, dildoY, dildoZ)
                            && isFinite(fromX, fromY, fromZ));
        }

        static boolean isFinite(double... values) {
            for (double value : values) {
                if (!Double.isFinite(value)) {
                    return false;
                }
            }
            return true;
        }

        /** True when every coordinate of this source is a real number. */
        public boolean coordinatesFinite() {
            return isFinite(dildoX, dildoY, dildoZ) && isFinite(fromX, fromY, fromZ);
        }

        /** The same source, marked unusable without throwing its data away. */
        public EntrySource asInvalid() {
            return new EntrySource(dimId, entryDildoUuid, dildoX, dildoY, dildoZ,
                    fromX, fromY, fromZ, roomOwner, false);
        }
    }

    public static final Codec<EntrySource> ENTRY_SOURCE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // Written on every record of this build, so a reader can tell "current, strict"
                    // apart from "old, lenient" without guessing from the payload.
                    Codec.INT.optionalFieldOf("fmt", 1).forGetter(source -> EntrySource.FORMAT),
                    class_2960.field_25139.optionalFieldOf("dim")
                            .forGetter(source -> Optional.ofNullable(source.dimId())),
                    UUID_CODEC.optionalFieldOf("dildo")
                            .forGetter(source -> Optional.ofNullable(source.entryDildoUuid())),
                    Codec.DOUBLE.optionalFieldOf("dildoX").forGetter(source -> Optional.of(source.dildoX())),
                    Codec.DOUBLE.optionalFieldOf("dildoY").forGetter(source -> Optional.of(source.dildoY())),
                    Codec.DOUBLE.optionalFieldOf("dildoZ").forGetter(source -> Optional.of(source.dildoZ())),
                    Codec.DOUBLE.optionalFieldOf("fromX").forGetter(source -> Optional.of(source.fromX())),
                    Codec.DOUBLE.optionalFieldOf("fromY").forGetter(source -> Optional.of(source.fromY())),
                    Codec.DOUBLE.optionalFieldOf("fromZ").forGetter(source -> Optional.of(source.fromZ())),
                    UUID_CODEC.optionalFieldOf("owner")
                            .forGetter(source -> Optional.ofNullable(source.roomOwner()))
            ).apply(instance, InnerState::decodeEntrySource));

    /** Decodes an entry source, applying the strictness rules of the format marker. */
    private static EntrySource decodeEntrySource(int format,
                                                 Optional<class_2960> dim,
                                                 Optional<UUID> dildo,
                                                 Optional<Double> dildoX, Optional<Double> dildoY,
                                                 Optional<Double> dildoZ,
                                                 Optional<Double> fromX, Optional<Double> fromY,
                                                 Optional<Double> fromZ,
                                                 Optional<UUID> owner) {
        if (format < EntrySource.FORMAT) {
            // Old records: zero is a legitimate value here, so it is accepted as written.
            return new EntrySource(
                    dim.orElse(class_2960.method_60655("minecraft", "overworld")),
                    dildo.orElse(null),
                    dildoX.orElse(0.0D), dildoY.orElse(0.0D), dildoZ.orElse(0.0D),
                    fromX.orElse(0.0D), fromY.orElse(0.0D), fromZ.orElse(0.0D),
                    owner.orElse(null));
        }
        boolean complete = dim.isPresent() && owner.isPresent()
                && dildoX.isPresent() && dildoY.isPresent() && dildoZ.isPresent()
                && fromX.isPresent() && fromY.isPresent() && fromZ.isPresent();
        EntrySource decoded = new EntrySource(dim.orElse(null), dildo.orElse(null),
                dildoX.orElse(Double.NaN), dildoY.orElse(Double.NaN), dildoZ.orElse(Double.NaN),
                fromX.orElse(Double.NaN), fromY.orElse(Double.NaN), fromZ.orElse(Double.NaN),
                owner.orElse(null), false);
        if (!complete || !decoded.coordinatesFinite()) {
            return decoded;
        }
        return new EntrySource(decoded.dimId(), decoded.entryDildoUuid(),
                decoded.dildoX(), decoded.dildoY(), decoded.dildoZ(),
                decoded.fromX(), decoded.fromY(), decoded.fromZ(), decoded.roomOwner(), true);
    }

    /**
     * Why a non-player object is waiting to leave, and therefore which retry applies to it.
     *
     * <p>Stored with the request so a reload cannot turn a surface sweep into a boundary ejection
     * (or the other way round).</p>
     */
    public enum ExitTrigger {
        SURFACE,
        BOUNDARY
    }

    public static final Codec<ExitTrigger> EXIT_TRIGGER_CODEC =
            Codec.STRING.xmap(ExitTrigger::valueOf, ExitTrigger::name);

    /**
     * One non-player object that could not leave yet.
     *
     * <p>Persistent on purpose: a request whose object is in an unloaded chunk must still be there
     * after a reload, otherwise the object would simply never be retried.</p>
     */
    public record PendingExit(UUID roomOwner, ExitTrigger trigger) {
        public static final Codec<PendingExit> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        UUID_CODEC.fieldOf("owner").forGetter(PendingExit::roomOwner),
                        EXIT_TRIGGER_CODEC.optionalFieldOf("trigger", ExitTrigger.BOUNDARY)
                                .forGetter(PendingExit::trigger)
                ).apply(instance, PendingExit::new));
    }

    /**
     * The authoritative lock of one dildo/doll pair.
     *
     * <p>Keyed by the actual doll entity UUID and storing the belt stack exactly as it was taken,
     * so the item handed back on unlock is the same one - damage, name, dye and enchantments
     * included - and so a new entity that merely appears at the old coordinates can never inherit
     * or lift somebody else's lock.</p>
     */
    public record SecurityLock(String lockId, UUID dollUuid, UUID dildoUuid, UUID ownerUuid,
                               class_1799 beltStack, int securityRevision) {
        public static final Codec<SecurityLock> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("lockId").forGetter(SecurityLock::lockId),
                        UUID_CODEC.fieldOf("doll").forGetter(SecurityLock::dollUuid),
                        UUID_CODEC.fieldOf("dildo").forGetter(SecurityLock::dildoUuid),
                        UUID_CODEC.fieldOf("owner").forGetter(SecurityLock::ownerUuid),
                        class_1799.field_24671.optionalFieldOf("belt", class_1799.field_8037)
                                .forGetter(SecurityLock::beltStack),
                        Codec.INT.optionalFieldOf("revision", 0).forGetter(SecurityLock::securityRevision)
                ).apply(instance, SecurityLock::new));

        /** True when the stored belt can still be handed back. */
        public boolean hasReturnableBelt() {
            return beltStack != null && !beltStack.method_7960();
        }
    }

    /** owner UUID -> shell generated flag */
    private final Map<UUID, Boolean> rooms = new HashMap<>();
    /** owner UUID -> shell layout version that has already been generated */
    private final Map<UUID, Integer> layouts = new HashMap<>();
    /** player UUID -> return point (legacy map, still kept in step) */
    private final Map<UUID, ReturnPoint> returns = new HashMap<>();
    /**
     * player UUID -> where that player actually stood before entering.
     *
     * <p>Separate from {@link #returns}: that map also carries the legacy dildo-position
     * fallback, while this one is only ever written from a real player position, so a player
     * exit can prefer the exact spot without trusting a dildo coordinate.</p>
     */
    private final Map<UUID, ReturnPoint> playerReturns = new HashMap<>();
    /** owner UUID -> its bindings and single strong output port */
    private final Map<UUID, BindingSet> bindings = new HashMap<>();
    /** object UUID -> where it entered, for non-player objects */
    private final Map<UUID, EntrySource> entrySources = new HashMap<>();
    /** object UUID -> the output request that is waiting for a usable port */
    private final Map<UUID, PendingExit> pendingExits = new HashMap<>();
    /** doll entity UUID -> the authoritative lock of that pair */
    private final Map<UUID, SecurityLock> securityLocks = new HashMap<>();
    private final List<ForcedPortChunk> forcedPortChunks = new ArrayList<>();
    /** Schema version this state was last written with. */
    private int dataVersion = DATA_VERSION;

    public InnerState() {
    }

    private InnerState(Map<UUID, Boolean> rooms,
                       Map<UUID, Integer> layouts,
                       Map<UUID, ReturnPoint> returns,
                       Map<UUID, ReturnPoint> playerReturns,
                       Map<UUID, BindingSet> bindings,
                       Map<UUID, EntrySource> entrySources,
                       Map<UUID, PendingExit> pendingExits,
                       Map<UUID, SecurityLock> securityLocks,
                       List<ForcedPortChunk> forcedPortChunks,
                       int dataVersion) {
        this.rooms.putAll(rooms);
        this.layouts.putAll(layouts);
        this.returns.putAll(returns);
        this.playerReturns.putAll(playerReturns);
        this.bindings.putAll(bindings);
        this.entrySources.putAll(entrySources);
        this.pendingExits.putAll(pendingExits);
        this.securityLocks.putAll(securityLocks);
        this.forcedPortChunks.addAll(forcedPortChunks);
        this.dataVersion = dataVersion;
    }

    public static final Codec<InnerState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(UUID_CODEC, Codec.BOOL)
                            .optionalFieldOf("rooms", Collections.emptyMap())
                            .forGetter(state -> state.rooms),
                    Codec.unboundedMap(UUID_CODEC, Codec.INT)
                            .optionalFieldOf("layout", Collections.emptyMap())
                            .forGetter(state -> state.layouts),
                    Codec.unboundedMap(UUID_CODEC, RETURN_POINT_CODEC)
                            .optionalFieldOf("returns", Collections.emptyMap())
                            .forGetter(state -> state.returns),
                    Codec.unboundedMap(UUID_CODEC, RETURN_POINT_CODEC)
                            .optionalFieldOf("player_returns", Collections.emptyMap())
                            .forGetter(state -> state.playerReturns),
                    Codec.unboundedMap(UUID_CODEC, BINDING_SET_CODEC)
                            .optionalFieldOf("bindings", Collections.emptyMap())
                            .forGetter(state -> state.bindings),
                    Codec.unboundedMap(UUID_CODEC, ENTRY_SOURCE_CODEC)
                            .optionalFieldOf("entry_sources", Collections.emptyMap())
                            .forGetter(state -> state.entrySources),
                    Codec.unboundedMap(UUID_CODEC, PendingExit.CODEC)
                            .optionalFieldOf("pending_exits", Collections.emptyMap())
                            .forGetter(state -> state.pendingExits),
                    Codec.unboundedMap(UUID_CODEC, SecurityLock.CODEC)
                            .optionalFieldOf("security_locks", Collections.emptyMap())
                            .forGetter(state -> state.securityLocks),
                    Codec.INT.optionalFieldOf("data_version", DATA_VERSION)
                            .forGetter(state -> state.dataVersion),
                    ForcedPortChunk.CODEC.listOf().optionalFieldOf("forced_port_chunks", List.of())
                            .forGetter(state -> state.forcedPortChunks)
            ).apply(instance, (rooms, layouts, returns, playerReturns, bindings, sources, pending, locks, version, forced) ->
                    new InnerState(rooms, layouts, returns, playerReturns, bindings, sources, pending, locks, forced, version)));

    public static final Supplier<InnerState> FACTORY = InnerState::new;

    public static final class_10741<InnerState> TYPE =
            new class_10741<>("inner_dimension_rooms", FACTORY, CODEC, class_4284.field_19212);

    public static InnerState get(class_3218 overworld) {
        return overworld.method_17983().method_17924(TYPE);
    }

    public Set<ForcedPortChunk> ownedForcedPortChunks() {
        return Set.copyOf(this.forcedPortChunks);
    }

    public void setOwnedForcedPortChunks(Set<ForcedPortChunk> chunks) {
        if (ownedForcedPortChunks().equals(chunks)) return;
        this.forcedPortChunks.clear();
        this.forcedPortChunks.addAll(chunks);
        method_80();
    }

    // ------------------------------------------------------------------ rooms

    public Set<UUID> roomOwners() {
        return Collections.unmodifiableSet(rooms.keySet());
    }

    public void addRoomOwner(UUID owner) {
        if (!rooms.containsKey(owner)) {
            rooms.put(owner, false);
            method_80();
        }
    }

    public boolean isShellGenerated(UUID owner) {
        return rooms.getOrDefault(owner, false);
    }

    /** Layout version of the shell that has been built for this owner (0 = never built). */
    public int getLayout(UUID owner) {
        return layouts.getOrDefault(owner, 0);
    }

    public void setLayout(UUID owner, int version) {
        layouts.put(owner, version);
        rooms.put(owner, true);
        method_80();
    }

    /** Forces the shell to be rebuilt the next time the room is opened. */
    public void resetLayout(UUID owner) {
        layouts.remove(owner);
        method_80();
    }

    // ------------------------------------------------------- player positions

    /**
     * Legacy return record. Kept readable and writable so old saves keep working; new code
     * should call {@link #setPlayerReturn} so a dildo position can never be mistaken for a
     * player position.
     */
    public void setReturn(UUID player, class_2960 dimId, double x, double y, double z, float yaw, float pitch) {
        returns.put(player, new ReturnPoint(dimId, x, y, z, yaw, pitch));
        method_80();
    }

    public ReturnPoint getReturn(UUID player) {
        return returns.get(player);
    }

    /**
     * Records where a player actually stood before being sent into a room. Never receives a
     * dildo position: only real player coordinates are passed in.
     */
    public void setPlayerReturn(UUID player, class_2960 dimId, double x, double y, double z, float yaw, float pitch) {
        // Nested entries must retain the position before the player first left the exterior.
        if (com.innerdimension.dimension.DynamicDimensionManager.isInnerWorld(
                net.minecraft.class_5321.method_29179(net.minecraft.class_7924.field_41223, dimId))) return;
        ReturnPoint point = new ReturnPoint(dimId, x, y, z, yaw, pitch);
        playerReturns.put(player, point);
        // Keep the legacy map in step so older code paths and existing saves agree.
        returns.put(player, point);
        method_80();
    }

    /** The exact spot a player entered from, or {@code null} when none was recorded yet. */
    public ReturnPoint getPlayerReturn(UUID player) {
        ReturnPoint point = playerReturns.get(player);
        return point != null ? point : returns.get(player);
    }

    // ------------------------------------------------------------- bindings

    /** The binding data of a dimension owner, created empty on first use. */
    public BindingSet bindingSet(UUID owner) {
        return bindings.computeIfAbsent(owner, key -> new BindingSet());
    }

    /** The binding data of a dimension owner, or {@code null} when it has none. */
    public BindingSet bindingSetOrNull(UUID owner) {
        return bindings.get(owner);
    }

    public Map<UUID, BindingSet> allBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    /**
     * Applies the rule "the first valid connection of a dimension is its default strong
     * binding": stores the connection and, unless {@code autoStrong} is false, makes it the
     * strong port when the dimension never had one.
     *
     * <p>{@code autoStrong == false} is used by the migration of legacy dildos: those are
     * registered during load, and the default strong port of an old save has to be chosen in a
     * deterministic order rather than by whichever chunk happened to load first, so the pointer
     * is left alone until the migration pass has seen every loaded legacy dildo. The
     * {@code hasEverBound} flag is still set, so a later connection does not silently claim the
     * port in the meantime.</p>
     *
     * @return the binding as stored
     */
    public Binding registerBinding(Binding binding, boolean autoStrong) {
        BindingSet set = bindingSet(binding.ownerUuid);
        boolean wasEverBound = set.hasEverBound;
        set.hasEverBound = true;
        set.bindingRevision++;
        Binding stored = new Binding(binding.dildoUuid, binding.dollUuid, binding.ownerUuid,
                binding.dimId, binding.x, binding.y, binding.z,
                binding.dollX, binding.dollY, binding.dollZ,
                binding.verified, binding.authorized, set.bindingRevision);
        set.bindings.put(stored.dildoUuid, stored);
        if (autoStrong && !wasEverBound && set.strongDildoUuid == null) {
            set.strongDildoUuid = stored.dildoUuid;
        }
        method_80();
        return stored;
    }

    /** The revision of a dimension's connection set, or 0 when it has none. */
    public int bindingRevision(UUID owner) {
        BindingSet set = bindings.get(owner);
        return set == null ? 0 : set.bindingRevision;
    }

    /** Replaces a stored connection, e.g. with refreshed positions. */
    public void replaceBinding(Binding binding) {
        BindingSet set = bindings.get(binding.ownerUuid);
        if (set == null || !set.bindings.containsKey(binding.dildoUuid)) {
            return;
        }
        set.bindings.put(binding.dildoUuid, binding);
        method_80();
    }

    /**
     * Atomic promotion: makes {@code dildoUuid} the one strong port of {@code owner}.
     *
     * <p>The previous strong dildo stays in {@link BindingSet#bindings} and becomes weak
     * purely because the pointer moved; nothing else has to be rewritten.</p>
     *
     * @return {@code true} when the pointer actually changed
     */
    public boolean promoteStrong(UUID owner, UUID dildoUuid) {
        BindingSet set = bindingSet(owner);
        if (dildoUuid == null || dildoUuid.equals(set.strongDildoUuid)) {
            return false;
        }
        set.strongDildoUuid = dildoUuid;
        set.bindingRevision++;
        method_80();
        return true;
    }

    /** The strong output port of a dimension, or {@code null} when it has none. */
    public UUID strongDildo(UUID owner) {
        BindingSet set = bindings.get(owner);
        return set == null ? null : set.strongDildoUuid;
    }

    public Binding strongBinding(UUID owner) {
        BindingSet set = bindings.get(owner);
        if (set == null || set.strongDildoUuid == null) {
            return null;
        }
        return set.bindings.get(set.strongDildoUuid);
    }

    public Binding bindingOfDildo(UUID dildoUuid) {
        if (dildoUuid == null) {
            return null;
        }
        for (BindingSet set : bindings.values()) {
            Binding binding = set.bindings.get(dildoUuid);
            if (binding != null) {
                return binding;
            }
        }
        return null;
    }

    /** Marks a connection as validated or not, without ever dropping it. */
    public void setBindingVerified(UUID owner, UUID dildoUuid, boolean verified) {
        BindingSet set = bindings.get(owner);
        if (set == null) {
            return;
        }
        Binding binding = set.bindings.get(dildoUuid);
        if (binding == null || binding.verified == verified) {
            return;
        }
        binding.verified = verified;
        method_80();
    }

    /**
     * Removes one connection.
     *
     * <p>When the removed connection was the strong port the pointer is cleared and
     * <em>nothing</em> is promoted in its place: after the first automatic binding, tearing
     * the strong dildo down must not silently hand the role to a weak one. The user picks the
     * next strong port with Shift + right-click.</p>
     */
    public void removeBinding(UUID owner, UUID dildoUuid) {
        BindingSet set = bindings.get(owner);
        if (set == null || set.bindings.remove(dildoUuid) == null) {
            return;
        }
        if (dildoUuid.equals(set.strongDildoUuid)) {
            set.strongDildoUuid = null;
        }
        set.bindingRevision++;
        method_80();
    }

    /**
     * Permanently wipes every piece of state an owner accumulated: the room
     * record itself, its layout marker, the whole binding set, and every entry
     * source, pending exit and security lock tied to that owner.
     *
     * <p>Player return points of <em>visitors</em> are deliberately kept: they
     * belong to the visiting players and only matter while those players are
     * inside, which the caller prevents by ejecting them first. Returns that
     * point into the deleted room are refused by the exit resolver anyway.</p>
     */
    public void removeRoomData(UUID owner) {
        if (owner == null) {
            return;
        }
        boolean changed = rooms.remove(owner) != null;
        changed |= layouts.remove(owner) != null;
        changed |= bindings.remove(owner) != null;
        changed |= entrySources.values().removeIf(source -> owner.equals(source.roomOwner()));
        changed |= pendingExits.values().removeIf(pending -> owner.equals(pending.roomOwner()));
        changed |= securityLocks.values().removeIf(lock -> owner.equals(lock.ownerUuid()));
        if (changed) {
            method_80();
        }
    }

    // --------------------------------------------------------- entry sources

    /** Records where a non-player object came from. */
    public void setEntrySource(UUID objectUuid, EntrySource source) {
        if (objectUuid == null || source == null) {
            return;
        }
        entrySources.put(objectUuid, source);
        method_80();
    }

    public EntrySource getEntrySource(UUID objectUuid) {
        return objectUuid == null ? null : entrySources.get(objectUuid);
    }

    public void clearEntrySource(UUID objectUuid) {
        if (objectUuid != null && entrySources.remove(objectUuid) != null) {
            method_80();
        }
    }

    /** Read-only snapshot of every recorded entry source, for the exit resolver. */
    public Map<UUID, EntrySource> entrySources() {
        return Collections.unmodifiableMap(entrySources);
    }

    // ---------------------------------------------------------- pending exits

    /**
     * Remembers that this object still has to leave, so the next retry can find it.
     *
     * <p>One request per object: a second trigger for the same object replaces nothing, because the
     * object either leaves or keeps waiting - it is never queued twice.</p>
     */
    public void setPendingExit(UUID objectUuid, PendingExit request) {
        if (objectUuid == null || request == null) {
            return;
        }
        if (pendingExits.putIfAbsent(objectUuid, request) == null) {
            method_80();
        }
    }

    public PendingExit getPendingExit(UUID objectUuid) {
        return objectUuid == null ? null : pendingExits.get(objectUuid);
    }

    public void clearPendingExit(UUID objectUuid) {
        if (objectUuid != null && pendingExits.remove(objectUuid) != null) {
            method_80();
        }
    }

    /** Read-only snapshot of the waiting output requests, for the retry pass. */
    public Map<UUID, PendingExit> pendingExits() {
        return Collections.unmodifiableMap(pendingExits);
    }

    // ---------------------------------------------------------- security locks

    /** The authoritative lock of a doll, or {@code null} when it is not locked. */
    public SecurityLock lockOfDoll(UUID dollUuid) {
        return dollUuid == null ? null : securityLocks.get(dollUuid);
    }

    /** The lock that currently governs a dildo, or {@code null}. */
    public SecurityLock lockOfDildo(UUID dildoUuid) {
        if (dildoUuid == null) {
            return null;
        }
        for (SecurityLock lock : securityLocks.values()) {
            if (dildoUuid.equals(lock.dildoUuid())) {
                return lock;
            }
        }
        return null;
    }

    /**
     * Stores a lock. One doll can only ever hold one belt, so an existing record is never replaced
     * silently.
     *
     * @return {@code false} when this doll already carries a lock
     */
    public boolean addLock(SecurityLock lock) {
        if (lock == null || lock.dollUuid() == null) {
            return false;
        }
        if (securityLocks.putIfAbsent(lock.dollUuid(), lock) != null) {
            return false;
        }
        method_80();
        return true;
    }

    /** Removes the lock of a doll, returning it so the caller can hand the belt back exactly once. */
    public SecurityLock removeLock(UUID dollUuid) {
        if (dollUuid == null) {
            return null;
        }
        SecurityLock removed = securityLocks.remove(dollUuid);
        if (removed != null) {
            method_80();
        }
        return removed;
    }

    /** Read-only snapshot of every lock, for diagnostics and the anti-bypass checks. */
    public Map<UUID, SecurityLock> locks() {
        return Collections.unmodifiableMap(securityLocks);
    }

    // --------------------------------------------------------- housekeeping

    public int dataVersion() {
        return dataVersion;
    }

    /**
     * Drops every entry record that pointed at a dimension which no longer exists.
     *
     * <p>Only ever called with a dimension the server has actually removed, so an object whose
     * chunk is merely unloaded keeps its record.</p>
     *
     * @return the number of records dropped
     */
    public int dropEntrySourcesForDimension(class_2960 dimId) {
        if (dimId == null) {
            return 0;
        }
        int before = entrySources.size();
        if (entrySources.values().removeIf(source -> dimId.equals(source.dimId))) {
            method_80();
        }
        return before - entrySources.size();
    }

    /** How many non-player entry records are stored, for diagnostics. */
    public int entrySourceCount() {
        return entrySources.size();
    }

    /** Sorted snapshot of a dimension's dildo UUIDs, used by the deterministic migration pass. */
    public List<UUID> sortedDildoUuids(UUID owner) {
        BindingSet set = bindings.get(owner);
        if (set == null) {
            return Collections.emptyList();
        }
        List<UUID> sorted = new ArrayList<>(new LinkedHashSet<>(set.bindings.keySet()));
        sorted.sort(UUID::compareTo);
        return sorted;
    }
}
