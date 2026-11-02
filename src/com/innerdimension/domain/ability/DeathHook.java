package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * Death-pipeline participation. Consulted by
 * {@link com.innerdimension.domain.combat.DomainDamagePipeline#allowDeath} after every
 * already-registered vanilla life-saving mechanism (totem, ...) and before the mob is
 * actually removed from the world.
 *
 * <p>The return contract mirrors the damage hooks: {@code null} abstains and lets the
 * remaining covering sessions decide, {@code TRUE} means this hook revived the entity and
 * the death must be cancelled. {@code FALSE} is treated as an abstention.</p>
 */
public interface DeathHook {
    /** TRUE = a revive happened, cancel the death; null = abstain. */
    default Boolean allowDeath(DomainSession session, class_1309 entity, class_1282 source, float amount) {
        return null;
    }
}
