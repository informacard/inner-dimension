package com.innerdimension.entity;

import net.minecraft.class_11368;
import net.minecraft.class_11372;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2318;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2665;
import net.minecraft.class_2680;
import net.minecraft.class_3218;

/**
 * Makes a pushed entity walk back when the sticky piston that pushed it retracts, which vanilla
 * never does: pistons shove entities, but only sticky pistons pull <em>blocks</em> back.
 *
 * <p>The previous version looked for any {@code piston_head} block next to the entity every tick.
 * That is wrong in both directions: two entities sharing a cell (a doll shoved onto a stick) both
 * saw the same head and were both dragged back, and an entity that merely ended up in front of some
 * other piston's head adopted that piston instead of the one that actually pushed it. It also just
 * plain failed on a normal push, because a piston does not place its head immediately - the head
 * cell holds {@code moving_piston} for the two ticks the animation runs, and only then becomes
 * {@code piston_head}, by which time the entity has already finished moving.</p>
 *
 * <p>So the entity now binds to the <em>base</em> of the piston that shoved it, at the moment it is
 * shoved, and keeps that binding:</p>
 *
 * <ul>
 *   <li>a shove is a move of exactly one block along one axis; the responsible piston is the one
 *       whose base sits one cell behind the cell the entity just left and faces the way it moved -
 *       that base is the binding, and the entity is pulled back into the cell right in front of it
 *       (base + facing) when it retracts;</li>
 *   <li>only the entity that actually moved binds, so a partner sitting in the same cell is left
 *       alone;</li>
 *   <li>the binding lasts until that piston retracts: other pistons, and simply being in front of
 *       another extended head, cannot take the entity over;</li>
 *   <li>the pull is bounded by {@link #MAX_FOLLOW_DISTANCE} and only happens once the target cell
 *       is free, so a stale binding can never teleport the entity somewhere solid.</li>
 * </ul>
 *
 * <p>Each entity that wants this behaviour owns one instance and calls {@link #tick} once per
 * server tick, after it has been snapped to its cell.</p>
 */
public final class PistonTow {
    /** Ticks to keep waiting for the piston base to report "extended" after a shove. */
    private static final int CONFIRM_TICKS = 8;
    /** Ticks to keep waiting for the cell in front of the piston to become free again. */
    private static final int RETURN_TICKS = 8;
    /** Safety bound for the pull-back, in blocks per axis. */
    private static final int MAX_FOLLOW_DISTANCE = 8;

    private static final int IDLE = 0;
    /** Shoved and waiting for the piston to read as extended. */
    private static final int WAITING = 1;
    /** Extended and holding us; we go back when it retracts. */
    private static final int BOUND = 2;

    private int state = IDLE;
    private int ticks;
    /** The sticky piston's base cell. */
    private class_2338 baseCell;
    /** Direction it pushes; the cell we came from is {@code baseCell + facing}. */
    private class_2350 facing;
    /** The entity's cell last tick, used to spot the one-block shove a piston applies. */
    private class_2338 lastCell;

    /** Call once per server tick, after the entity has been snapped into its cell. */
    public void tick(class_1297 entity) {        class_1937 world = entity.method_73183();
        if (world.method_8608()) {
            return;
        }
        class_2338 cell = entity.method_24515();

        if (this.state != IDLE && !isBaseReady(world, this.baseCell)) {
            // The base is in a chunk that is not loaded right now: wait. This must not consume the
            // confirmation or return window, otherwise a walk back would time out while nothing was
            // even observable, and the record would be dropped for the wrong reason.
            this.lastCell = cell;
            return;
        }

        if (this.state == WAITING) {
            if (isMovingPiston(world, this.baseCell)) {
                // The animation is still running: wait for the base to settle.
                this.ticks = 0;
            } else if (isExtendedSticky(world, this.baseCell, this.facing)) {
                this.state = BOUND;
                this.ticks = 0;
            } else if (isSticky(world, this.baseCell, this.facing)) {
                // The base is a sticky piston that points the same way but is no longer extended.
                // That is the case where the piston finished its stroke while this entity was not
                // loaded: the stored state was WAITING because the extension was never confirmed, so
                // one safe walk back is allowed here - with exactly the same conditions as below.
                if (!tryWalkBack(entity, cell) && ++this.ticks > CONFIRM_TICKS) {
                    this.reset();
                }
            } else if (++this.ticks > CONFIRM_TICKS) {
                // The base is neither our piston nor a moving one any more: cancel rather than adopt
                // whatever else is nearby.
                this.reset();
            }
        } else if (this.state == BOUND) {
            if (isMovingPiston(world, this.baseCell)) {
                // The base is still a moving_piston while the head cell is already air: returning now
                // would put a full-height doll in the retracting head's sweep and vanilla would push
                // it down again.
                this.ticks = 0;
            } else if (isExtendedSticky(world, this.baseCell, this.facing)) {
                this.ticks = 0;
            } else {
                if (!tryWalkBack(entity, cell) && ++this.ticks > RETURN_TICKS) {
                    this.reset();
                }
            }
        }

        if (this.state == IDLE) {
            this.bindIfShoved(world, cell);
        }
        this.lastCell = entity.method_24515();
    }

    /**
     * Steps back into the cell in front of the piston base, if that is still a legal move.
     *
     * <p>Every condition of the original behaviour is kept: the base has to be that sticky piston
     * pointing the same way, the target cell has to be free, the distance has to stay bounded, and the
     * entity's complete collision box has to fit there. A stale record therefore can never teleport
     * anything through a wall.</p>
     *
     * @return true when the entity was moved back
     */
    private boolean tryWalkBack(class_1297 entity, class_2338 cell) {
        class_1937 world = entity.method_73183();
        if (this.baseCell == null || this.facing == null
                || !isSticky(world, this.baseCell, this.facing)) {
            return false;
        }
        class_2338 target = this.baseCell.method_10093(this.facing);
        if (!world.method_8320(target).method_26215() || !withinFollowDistance(cell, target)) {
            return false;
        }
        class_238 box = entity.method_5829();
        class_238 moved = box.method_989(
                target.method_10263() + 0.5D - entity.method_23317(),
                target.method_10264() - entity.method_23318(),
                target.method_10260() + 0.5D - entity.method_23321());
        if (!world.method_8587(entity, moved)) {
            return false;
        }
        entity.method_5814(target.method_10263() + 0.5D,
                target.method_10264(),
                target.method_10260() + 0.5D);
        entity.method_18800(0.0D, 0.0D, 0.0D);
        this.reset();
        return true;
    }

    /**
     * True when the piston base is in a loaded chunk and can be inspected at all.
     *
     * <p>The check uses the server chunk loading manager: {@code WorldView.method_22347} is
     * {@code isAir}, and a piston base is of course not air, so using it here made every tow wait
     * forever - the doll and the stick were never pulled back again.</p>
     */
    private static boolean isBaseReady(class_1937 world, class_2338 base) {
        if (base == null) {
            return false;
        }
        if (world instanceof class_3218 serverWorld) {
            return com.innerdimension.chunk.LoadedChunks.contains(serverWorld, base);
        }
        return true; // a client-side instance never drives the tow
    }

    /** True while the base is mid-stroke, i.e. still a moving_piston block. */
    private static boolean isMovingPiston(class_1937 world, class_2338 base) {
        return base != null && world.method_8320(base).method_26204() == class_2246.field_10008;
    }

    /** True while this entity is being towed, i.e. while it may still move on its own. */
    public boolean isBusy() {
        return this.state != IDLE;
    }

    /**
     * Stores the piston binding so a reloaded entity can finish the retraction it was in the
     * middle of.
     *
     * <p>Without this the binding only lived in memory: a doll that was unloaded while its piston
     * was still extended came back with no idea which piston had shoved it, and the previous
     * implementation's answer - adopting whatever {@code piston_head} happened to be next to it -
     * is exactly the "search for any nearby piston" behaviour that was removed.</p>
     */
    public void writeNbt(class_11372 nbt) {
        if (this.state == IDLE) {
            return;
        }
        nbt.method_71465("PistonTowState", this.state);
        if (this.baseCell != null) {
            nbt.method_71465("PistonTowBaseX", this.baseCell.method_10263());
            nbt.method_71465("PistonTowBaseY", this.baseCell.method_10264());
            nbt.method_71465("PistonTowBaseZ", this.baseCell.method_10260());
        }
        if (this.facing != null) {
            nbt.method_71465("PistonTowFacing", this.facing.ordinal());
        }
        if (this.lastCell != null) {
            nbt.method_71465("PistonTowLastX", this.lastCell.method_10263());
            nbt.method_71465("PistonTowLastY", this.lastCell.method_10264());
            nbt.method_71465("PistonTowLastZ", this.lastCell.method_10260());
        }
    }

    /**
     * Restores a stored binding.
     *
     * <p>The piston base is deliberately not trusted: the first tick re-checks it through
     * {@link #isSticky}/{@link #isExtendedSticky}, and the walk back additionally requires the
     * target cell to be free and the distance to stay within {@link #MAX_FOLLOW_DISTANCE}. A stale
     * record therefore cannot teleport anything - it just stops waiting.</p>
     */
    public void readNbt(class_11368 nbt) {
        int storedState = nbt.method_71424("PistonTowState", IDLE);
        int baseX = nbt.method_71424("PistonTowBaseX", Integer.MIN_VALUE);
        if (storedState == IDLE || baseX == Integer.MIN_VALUE) {
            this.reset();
            return;
        }
        int baseY = nbt.method_71424("PistonTowBaseY", 0);
        int baseZ = nbt.method_71424("PistonTowBaseZ", 0);
        class_2350[] directions = class_2350.values();
        int facingIndex = nbt.method_71424("PistonTowFacing", -1);
        if (facingIndex < 0 || facingIndex >= directions.length) {
            this.reset();
            return;
        }
        int lastX = nbt.method_71424("PistonTowLastX", Integer.MIN_VALUE);
        this.state = storedState;
        // A fresh confirmation window: the piston may have finished its stroke while this entity
        // was not loaded, and the ticks counter of the previous session means nothing now.
        this.ticks = 0;
        this.baseCell = new class_2338(baseX, baseY, baseZ);
        this.facing = directions[facingIndex];
        this.lastCell = lastX == Integer.MIN_VALUE ? null
                : new class_2338(lastX, nbt.method_71424("PistonTowLastY", 0),
                        nbt.method_71424("PistonTowLastZ", 0));
    }

    /** Binds to the sticky piston that just shoved the entity from {@link #lastCell} to {@code cell}. */
    private void bindIfShoved(class_1937 world, class_2338 cell) {
        class_2338 from = this.lastCell;
        if (from == null) {
            return;
        }
        class_2350 push = unitStep(from, cell);
        if (push == null || !withinFollowDistance(from, cell)) {
            return;
        }
        // The piston base sits one cell behind the cell the entity was pushed out of and points
        // the way the entity went. Its head cell is the one the entity just left.
        class_2338 base = from.method_10093(push.method_10153());
        if (!isSticky(world, base, push)) {
            return;
        }
        this.baseCell = base;
        this.facing = push;
        this.ticks = 0;
        this.state = WAITING;
    }

    private void reset() {
        this.state = IDLE;
        this.baseCell = null;
        this.facing = null;
        this.ticks = 0;
    }

    private static boolean withinFollowDistance(class_2338 from, class_2338 to) {
        return Math.abs(from.method_10263() - to.method_10263()) <= MAX_FOLLOW_DISTANCE
                && Math.abs(from.method_10264() - to.method_10264()) <= MAX_FOLLOW_DISTANCE
                && Math.abs(from.method_10260() - to.method_10260()) <= MAX_FOLLOW_DISTANCE;
    }

    /** The single axis step from {@code from} to {@code to}, or {@code null} for anything else. */
    private static class_2350 unitStep(class_2338 from, class_2338 to) {
        int dx = to.method_10263() - from.method_10263();
        int dy = to.method_10264() - from.method_10264();
        int dz = to.method_10260() - from.method_10260();
        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
            return null;
        }
        if (dx == 1) {
            return class_2350.field_11034; // east
        }
        if (dx == -1) {
            return class_2350.field_11039; // west
        }
        if (dy == 1) {
            return class_2350.field_11036; // up
        }
        if (dy == -1) {
            return class_2350.field_11033; // down
        }
        return dz == 1 ? class_2350.field_11035 : class_2350.field_11043; // south : north
    }

    /** True when {@code base} holds a sticky piston pointing along {@code facing}. */
    private static boolean isSticky(class_1937 world, class_2338 base, class_2350 facing) {
        class_2680 state = world.method_8320(base);
        return state.method_26204() == class_2246.field_10615
                && state.method_11654(class_2318.field_10927) == facing;
    }

    /** Same, but the piston also has to be extended right now. */
    private static boolean isExtendedSticky(class_1937 world, class_2338 base, class_2350 facing) {
        class_2680 state = world.method_8320(base);
        return state.method_26204() == class_2246.field_10615
                && state.method_11654(class_2318.field_10927) == facing
                && state.method_11654(class_2665.field_12191);
    }
}
