package com.innerdimension.domain.combat;

import com.innerdimension.domain.ability.DomainAbilities;
import com.innerdimension.domain.ability.KnockbackHook;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

import java.util.*;

/**
 * Knockback pipeline for the owner's own attack intent only.
 *
 * <p>Order (blueprint section 5.2): vanilla intent -&gt; D56 multiplier -&gt; target
 * resistance (applied by vanilla) -&gt; D48 turns the remaining horizontal component
 * upward, capped at 0.65 -&gt; mixed geometry / domain boundary clipping. A horse's rear
 * hoof never enters this pipeline; it applies its own velocity directly.</p>
 */
public final class DomainKnockbackPipeline {
    private record Intent(UUID attacker, UUID target, long tick, boolean sprinting) {}

    private static final Map<UUID, Intent> PENDING = new HashMap<>();
    private static final Map<UUID, Double> VERTICAL = new HashMap<>();
    private static final ThreadLocal<Boolean> BUSY = ThreadLocal.withInitial(() -> false);

    private DomainKnockbackPipeline() {}

    public static void arm(class_3222 attacker, class_1309 target, boolean sprinting) {
        PENDING.put(target.method_5667(),
                new Intent(attacker.method_5667(), target.method_5667(), DomainDamagePipeline.now(), sprinting));
    }

    public static void clear() {
        PENDING.clear();
        VERTICAL.clear();
    }

    /**
     * Scalar applied to vanilla's knockback strength. Returning 0 suppresses the
     * horizontal push entirely (D48); the upward replacement is emitted at RETURN.
     */
    public static double strengthFactor(class_1309 target, double strength, double x, double z) {
        if (BUSY.get()) return strength;
        Intent intent = PENDING.remove(target.method_5667());
        if (intent == null || intent.tick() != DomainDamagePipeline.now()) return strength;
        DomainSession session = DomainSessionIndex.byOwner(intent.attacker());
        if (session == null || !session.stable()) return strength;
        class_1297 attacker = target.method_73183().method_8503() == null ? null
                : target.method_73183().method_8503().method_3760().method_14602(intent.attacker());
        if (!(attacker instanceof class_3222 player)) return strength;
        class_243 original = new class_243(x * strength, 0, z * strength);
        double originalLength = original.method_1033();
        if (originalLength < 1.0E-6) return strength;
        class_243 current = original;
        for (KnockbackHook hook : DomainAbilities.hooks(session, KnockbackHook.class)) {
            try {
                current = hook.modifyKnockback(session, player, target, current);
            } catch (RuntimeException ignored) {
                // Keep the original intent.
            }
        }
        double vertical = current.field_1351;
        class_243 horizontal = new class_243(current.field_1352, 0, current.field_1350);
        if (Math.abs(vertical) > 1.0E-6) VERTICAL.put(target.method_5667(), vertical);
        return horizontal.method_1033() / originalLength;
    }

    /** Applies the D48 vertical replacement and clips the result to the domain interior. */
    public static void afterKnockback(class_1309 target) {
        if (BUSY.get()) return;
        Double vertical = VERTICAL.remove(target.method_5667());
        if (vertical == null) return;
        BUSY.set(true);
        try {
            class_243 velocity = target.method_18798();
            double upward = Math.max(0, Math.min(0.65, vertical));
            double y = Math.max(velocity.field_1351, upward);
            class_243 next = new class_243(0, y, 0);
            DomainSession session = DomainSessionIndex.byOwner(target.method_5667());
            if (session == null) {
                for (DomainSession covering : DomainSessionIndex.covering(target)) { session = covering; break; }
            }
            if (session != null) next = DomainGeometry.clampInside(session, target, next);
            target.method_18799(next);
            target.field_6008 = 0;
        } finally {
            BUSY.set(false);
        }
    }
}
