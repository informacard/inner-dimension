package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.*;

/**
 * D17 minecraft:mule (orange).
 *
 * <p>Endurance: the owner never loses hunger or saturation. The ability cannot fill hunger
 * back up and cannot run every tick — it only answers a question, and the food mixin asks
 * it at the exact consumption entry points (exhaustion, food level decrease, saturation
 * decrease). Eating still increases food normally.</p>
 *
 * <p>Integration contract: the hunger mixin must call {@code D17.blockHunger(player)} for
 * negative hunger changes only. There is no {@code DomainHungerGuard} class in this
 * codebase, so this static method is the whole surface.</p>
 */
public final class D17 implements DomainAbility {
    public static final D17 INSTANCE = new D17();
    private D17() {}
    @Override public String id() { return "minecraft:mule"; }
    @Override public String code() { return "D17"; }

    /**
     * True when this player's hunger/saturation losses must be refused. Side-effect free:
     * it only reads the session index, so it is safe to call from any mixin or from inside
     * a food-consumption branch.
     */
    public static boolean blockHunger(class_1657 player) {
        if (player == null) return false;
        return DomainSessionIndex.coveringWith(player,"D17").stream()
                .anyMatch(session -> com.innerdimension.domain.runtime.DomainObjects.matches(session,"D17",player));
    }
}
