package com.innerdimension.exit;

import net.minecraft.class_3218;

import java.util.UUID;

/**
 * The structured answer of the exit resolver.
 *
 * <p>Implementation plan section 0.10: a caller may only transfer when the status is
 * {@link Status#READY}. Every other status means "keep the object exactly where it is, and do not
 * fall back to something else" - which is what prevents a locked, unverified or still-loading port
 * from silently degrading into the entry source, a weak port or the room owner's return point.</p>
 */
public record ExitResolution(Status status, class_3218 world,
                             double x, double y, double z,
                             UUID dildoUuid, UUID dollUuid,
                             int bindingRevision, int securityRevision,
                             String reason) {

    public enum Status {
        /** A destination was resolved and is ready to receive the object. */
        READY,
        /** The destination chunk is not loaded yet: a budgeted probe is running. */
        WAIT_LOAD,
        /** The port exists but has not been verified (an unclaimed old connection, for example). */
        WAIT_VALIDATION,
        /** The output doll is still moving, so nothing may be placed next to it yet. */
        WAIT_MOVING,
        /** The dimension has connections, but no strong port: weak ports never output. */
        WAIT_NO_STRONG,
        /** No binding and no usable source record for this object. */
        WAIT_NO_SOURCE,
        /** A destination exists but nothing fits there right now. */
        WAIT_BLOCKED,
        /** The port carries a lock: the pair is suspended, not unbound. */
        WAIT_LOCKED,
        /** The actor is not allowed to use this port. */
        DENY_AUTH,
        /** The recorded world does not exist right now; the record is kept. */
        WAIT_MISSING_WORLD,
        /** The source record is present but cannot be trusted (wrong room owner, bad fields). */
        INVALID_SOURCE
    }

    public boolean ready() {
        return status == Status.READY;
    }

    /** True when the caller should keep the object and retry later rather than give up. */
    public boolean waiting() {
        return status != Status.READY;
    }

    public static ExitResolution ready(class_3218 world, double x, double y, double z,
                                       UUID dildoUuid, UUID dollUuid,
                                       int bindingRevision, int securityRevision) {
        return new ExitResolution(Status.READY, world, x, y, z, dildoUuid, dollUuid,
                bindingRevision, securityRevision, "ready");
    }

    public static ExitResolution waiting(Status status, String reason) {
        return new ExitResolution(status, null, 0.0D, 0.0D, 0.0D, null, null, 0, 0, reason);
    }

    /** The same resolution with a different world position, keeping its identity. */
    public ExitResolution at(double newX, double newY, double newZ) {
        return new ExitResolution(status, world, newX, newY, newZ, dildoUuid, dollUuid,
                bindingRevision, securityRevision, reason);
    }
}
