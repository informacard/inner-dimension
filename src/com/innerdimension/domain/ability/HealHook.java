package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/** Healing-pipeline participation (D34 parched, D59 wither). */
public interface HealHook {
    /**
     * Multiplier applied to a positive heal. Multiple sources take the strongest
     * reduction instead of multiplying: D59's complete block takes precedence over D34.
     */
    default float healingMultiplier(DomainSession session, class_1309 entity) { return 1.0f; }

    /** Called after a positive heal really landed. */
    default void afterHealing(DomainSession session, class_1309 entity, float healed) {}
}
