package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D56 minecraft:hoglin (orange) — 蛮劲 / brute force.
 *
 * <p>Scales the owner's raw melee knockback intent: x1.75 for a normal hit, x2.25 while the
 * attacker is sprinting. Damage is untouched, target knockback resistance is still applied by
 * vanilla afterwards, and the pipeline clips the final sweep to the domain interior so nobody
 * is pushed out of the domain.</p>
 */
public final class D56 implements DomainAbility, KnockbackHook {
    public static final D56 INSTANCE = new D56();
    private D56() {}
    @Override public String id() { return "minecraft:hoglin"; }
    @Override public String code() { return "D56"; }
    @Override public int priority() { return 100; }

    private static final double NORMAL = 1.75;
    private static final double SPRINTING = 2.25;

    @Override
    public class_243 modifyKnockback(DomainSession session, class_3222 attacker, class_1309 target, class_243 knockback) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D56",target))return knockback;
        if (knockback == null || attacker == null) return knockback;
        // DomainAttackPipeline is not reachable from here, so the sprint state is read live
        // from the attacker: the pipeline arms the intent in the same tick as the melee.
        double scale = attacker.method_5624() ? SPRINTING : NORMAL;
        return new class_243(knockback.field_1352 * scale, knockback.field_1351, knockback.field_1350 * scale);
    }
}
