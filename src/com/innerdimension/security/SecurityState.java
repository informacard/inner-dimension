package com.innerdimension.security;

/**
 * The security state of one actual dildo/doll pair.
 *
 * <p>Security spec section 4: these six states must stay distinguishable. In particular a locked
 * port, a mismatched owner or an unverified old port may never be collapsed into "no connection",
 * because that is exactly what would let the ordinary fallbacks (default entry, weak-port output or
 * the room owner's return point) run in their place.</p>
 */
public enum SecurityState {
    /** No doll, no recorded refusal and no old record waiting to be verified. */
    UNBOUND,
    /** Identity checked, connection valid and unlocked. */
    AUTHORIZED,
    /** The actual connection object carries a lock: every dimension transfer of this pair is denied. */
    LOCKED,
    /** The dildo's placer and the candidate doll owner disagree. */
    OWNER_MISMATCH,
    /** Profile, connection or lock record not verified yet: wait, never fall back. */
    AUTH_PENDING,
    /** Old save without provable authorization: waits for the owner to claim it. */
    LEGACY_UNVERIFIED;

    /** True when this state permits dimension transfers of the pair. */
    public boolean allowsTransfer() {
        return this == AUTHORIZED || this == UNBOUND;
    }

    /** True when the object must be kept and nothing may be routed through it. */
    public boolean blocksPort() {
        return this == LOCKED || this == OWNER_MISMATCH
                || this == AUTH_PENDING || this == LEGACY_UNVERIFIED;
    }
}
