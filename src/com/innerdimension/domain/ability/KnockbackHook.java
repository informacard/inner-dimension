package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

/**
 * Knockback participation. Only the owner's attack knockback intent enters here
 * (D56 scale, D48 turn upward); a horse's rear hoof is its own ability and never passes
 * through this pipeline.
 */
public interface KnockbackHook {
    default class_243 modifyKnockback(DomainSession session, class_3222 attacker, class_1309 target, class_243 knockback) {
        return knockback;
    }
}
