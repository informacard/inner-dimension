package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.function.Predicate;

/**
 * D43 minecraft:pillager (blue) — "连弩".
 *
 * <p>The charge-time halving itself lives in the crossbow mixin owned by another teammate;
 * this class exposes the single question that mixin needs to ask:
 * {@link #shouldHalveCharge(class_1309)} returns true exactly when the wielder owns a STABLE
 * expansion carrying D43. Nothing else about the crossbow (ammunition consumption, enchantment
 * order, client animation/progress agreement, the five-tick floor) is decided here.</p>
 */
public final class D43 implements DomainAbility {
    public static final D43 INSTANCE = new D43();
    private D43() {}
    @Override public String id() { return "minecraft:pillager"; }
    @Override public String code() { return "D43"; }
    private static Predicate<class_1309> clientState = entity -> false;

    public static void installClientState(Predicate<class_1309> state) {
        clientState = state == null ? entity -> false : state;
    }

    /** TRUE when the wielder owns a STABLE session that carries D43. */
    public static boolean shouldHalveCharge(class_1309 wielder) {
        if (wielder == null) return false;
        if (wielder.method_73183().method_8608()) return clientState.test(wielder);
        DomainSession session = DomainSessionIndex.byOwner(wielder.method_5667());
        return session != null && session.stable() && !session.ended() && session.hasCode("D43");
    }
}
