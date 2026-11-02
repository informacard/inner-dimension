package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/** Player-melee attack participation (D04, D09, D10, D37, D53, D58). */
public interface AttackHook {
    /**
     * A player melee hit that vanilla is about to resolve.
     *
     * @return TRUE when the hit is fully consumed: vanilla damage, knockback and hurt
     *         sound must all be suppressed. FALSE or null lets vanilla proceed.
     */
    default Boolean onOwnerMelee(DomainSession session, class_3222 attacker, class_1309 target) { return null; }

    /**
     * Extra damage merged into the same damage request. Never a second {@code hurt} call,
     * which would be swallowed by the invulnerability window.
     */
    default float bonusDamage(DomainSession session, class_3222 attacker, class_1309 target, float baseDamage) {
        return 0;
    }

    /** Called right after vanilla applied the melee damage. */
    default void afterMelee(DomainSession session, class_3222 attacker, class_1309 target, float taken) {}

    /** TRUE when this entity's own attack must be refused (D53 truce). */
    default boolean denyOwnAttack(DomainSession session, class_1309 attacker, class_1309 target) { return false; }
}
