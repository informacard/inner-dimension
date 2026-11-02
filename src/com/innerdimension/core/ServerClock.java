package com.innerdimension.core;

/**
 * The one server tick clock of this mod.
 *
 * <p>Every timing decision - the rescan period, the absence sampling intervals, the probe budget,
 * the pending retry period and the migration window - is expressed against this counter, so two
 * modules can never disagree about "the same tick". It is advanced once, at the start of the
 * server tick, before any module runs.</p>
 *
 * <p>The counter is server-tick based on purpose, never wall-clock: pausing a single-player game
 * must not age a temporary chunk load or expire a migration window.</p>
 */
public final class ServerClock {
    private static long ticks;

    private ServerClock() {
    }

    /** Advances the clock. Registered on START_SERVER_TICK, before any other module runs. */
    public static void advance() {
        ticks++;
    }

    /** The current server tick, shared by every module. */
    public static long now() {
        return ticks;
    }

    /** Resets the clock when a server starts, so an integrated restart begins from zero. */
    public static void reset() {
        ticks = 0L;
    }
}
