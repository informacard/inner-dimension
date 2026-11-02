package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * Damage-pipeline participation. A hook returns {@code null} to abstain; the first
 * non-null decision inside the ordered pre-damage chain wins for "cancel", while every
 * hook may still scale the amount.
 */
public interface DamageHook {
    /** Pre-armor decision: return TRUE to cancel, FALSE to force-allow, null to abstain. */
    default Boolean preDamage(DomainSession session, class_1309 victim, class_1282 source, float amount) { return null; }

    /** Post-armor, post-resistance multiplier on the damage that will actually be applied. */
    default float postArmor(DomainSession session, class_1309 victim, class_1282 source, float amount) { return amount; }

    /** After the health actually changed. {@code taken} is the real life loss. */
    default void afterDamage(DomainSession session, class_1309 victim, class_1282 source,
                             float baseAmount, float taken, boolean blocked) {}

    /** Owner-side notification: the session owner was actually hurt. */
    default void ownerHurt(DomainSession session, class_1282 source, float taken) {}
}
