package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * One engraved species' ability. Instances are stateless singletons; all runtime data
 * lives in the {@link DomainSession} that invoked them, keyed by session so nothing can
 * leak across expansions.
 */
public interface DomainAbility {
    /** Entity id the ability is engraved as, e.g. {@code minecraft:chicken}. */
    String id();

    /** Short code, e.g. {@code D04}. */
    String code();

    /** Lower runs first inside each pipeline. 100 is the default. */
    default int priority() { return 100; }

    /** The projection reached STABLE and the fixed bounds were captured. */
    default void onStable(DomainSession session) {}

    /** Called once per server tick while the session is STABLE. */
    default void onTick(DomainSession session, long now) {}

    /** Called once when the session ends for any reason, before leases are revoked. */
    default void onEnd(DomainSession session) {}

    /** Called when a covering session's owner leaves the world or dies. */
    default void onOwnerGone(DomainSession session) {}
}
