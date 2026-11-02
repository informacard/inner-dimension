package com.innerdimension.core;

/**
 * Every fixed parameter of the binding, exit, probe and surface rules in one place.
 *
 * <p>Implementation plan section 0.3: callers must not define their own defaults, and all durations
 * are server ticks measured with {@link ServerClock} - never wall-clock time, so pausing a
 * single-player game cannot age a temporary load or expire a window.</p>
 */
public final class Tuning {
    private Tuning() {
    }

    // ---------------------------------------------------------------- binding
    /** Stable-period automatic detection of a dildo. */
    public static final int BINDING_RESCAN_TICKS = 10;
    /** Collection window for migrating the connections of an old save. */
    public static final int LEGACY_COLLECTION_TICKS = 100;
    /** Grace after a chunk reports its entities loaded before a miss may be sampled. */
    public static final int ENTITY_LOAD_GRACE_TICKS = 20;
    /** Absence evidences needed before an unlocked connection may be cleaned up. */
    public static final int ABSENCE_CONFIRMATIONS = 2;
    /** Minimum distance between two absence evidences. */
    public static final int ABSENCE_SAMPLE_INTERVAL = 10;
    /** Cleanup cannot be proven while the entity chunk changed more recently than this. */
    public static final int ABSENCE_RESET_GRACE_TICKS = 20;

    // ------------------------------------------------------------- chunk loads
    /** Budget of one probe/ticket round, and of a source-mechanism hold. */
    public static final int PROBE_TICKS = 40;
    /** Wait after a probe budget ran out before the next round may be requested. */
    public static final int PROBE_COOLDOWN_TICKS = 200;
    /** Vanilla ticket radius argument used for a probe (not a 2x2 chunk area). */
    public static final int PROBE_RADIUS = 2;
    /** Chunks held around a mechanism that must keep ticking (centre +/- 1). */
    public static final int MECHANISM_HOLD_RADIUS = 1;

    // ------------------------------------------------------------------- exits
    /** Retry period of a pending non-player output request. */
    public static final int EXIT_RETRY_TICKS = 20;
    /** First warning after an object waited this long. */
    public static final int WAIT_WARN_TICKS = 200;
    /** Minimum distance between two warnings for the same reason. */
    public static final int REPEAT_WARN_TICKS = 1200;
    /** Vanilla successful-transfer cooldown; this mod never adds its own timer. */
    public static final int HOPPER_SUCCESS_COOLDOWN = 8;

    // ------------------------------------------------------------------ surface
    /** Vertical contact tolerance between feet and the surface. */
    public static final double SURFACE_VERTICAL_EPSILON = 0.05D;
    /** Real X/Z overlap a candidate has to have on both axes. */
    public static final double SURFACE_OVERLAP_EPSILON = 0.001D;
    /** How long the first result of a surface click is reused for the other hook. */
    public static final int SURFACE_DEDUPE_TICKS = 4;

    // ------------------------------------------------------------------- piston
    /** Retained delay before a pushed entity re-aligns to its cell. */
    public static final int PISTON_SNAP_DELAY = 2;
    /** Ticks to keep waiting for a piston base after a shove. */
    public static final int PISTON_CONFIRM_TICKS = 8;
    /** Ticks to keep waiting for the cell in front of a retracting piston. */
    public static final int PISTON_RETURN_TICKS = 8;
    /** Safety bound of a pull-back, per axis. */
    public static final int PISTON_MAX_FOLLOW_DISTANCE = 8;

    // -------------------------------------------------------------------- settled
    /** Velocity below which an output doll counts as still. */
    public static final double SETTLED_VELOCITY_EPSILON = 0.001D;
    /** Maximum position drift between consecutive server ticks of a still output doll. */
    public static final double SETTLED_POSITION_EPSILON = 0.002D;
    /**
     * How far the doll may sit from the centre of its cell and still count as re-aligned.
     *
     * <p>Looser than {@link #SETTLED_POSITION_EPSILON} on purpose: a vanilla piston parks a pushed
     * entity with about 0.01 blocks of separation, and a doll that is standing still at 0.01 off the
     * centre is still perfectly usable as an output port.</p>
     */
    public static final double SETTLED_CELL_TOLERANCE = 0.05D;
    /** Consecutive still ticks required before output through a doll. */
    public static final int SETTLED_CONSECUTIVE_TICKS = 2;

    // ----------------------------------------------------------- entry safety
    /** Window in which a second repeated trigger is treated as a loop risk. */
    public static final int ENTRY_LOOP_WINDOW_TICKS = 200;
    /** Attempts required to trip either entry circuit breaker. */
    public static final int ENTRY_LOOP_THRESHOLD = 3;
    /** Continuous trigger-free time required before a tripped guard recovers. */
    public static final int ENTRY_LOOP_QUIET_TICKS = 200;
    /** An entry this soon after a successful exit counts as a rapid return. */
    public static final int RAPID_RETURN_TICKS = 60;

    // ------------------------------------------------------------------- landing
    /** How far above the remembered spot a blocked player may be lifted. */
    public static final int PLAYER_SAFE_UP_SEARCH = 8;
    /** Horizontal search radius for a non-player landing spot. */
    public static final int NONPLAYER_SEARCH_RADIUS = 2;
    /** How far up a non-player landing spot may be searched. */
    public static final int NONPLAYER_SEARCH_UP = 2;
}
