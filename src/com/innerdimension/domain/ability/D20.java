package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.*;

/**
 * D20 minecraft:frog (green).
 *
 * <p>Glowshroom gift: a creature really killed by a player (or by that owner's own domain
 * damage) drops one extra random froglight — ochre, pearlescent or verdant, one third
 * each — independent of Looting. Only a real death counts once per victim, and the drop
 * goes through the ordinary item chain so D23 can collect it afterwards.</p>
 *
 * <p>Integration contract (no {@code DeathHook} interface exists in this codebase):
 * {@code D20.onDeath(victim, source)} must be called from the death/drop path with the
 * killing damage source. Training dummies, cancelled deaths and D49 saves never reach a
 * real death and therefore never call it.</p>
 */
public final class D20 implements DomainAbility {
    public static final D20 INSTANCE = new D20();
    private D20() {}
    @Override public String id() { return "minecraft:frog"; }
    @Override public String code() { return "D20"; }

    private static final List<String> FROGLIGHTS = List.of(
            "minecraft:ochre_froglight", "minecraft:pearlescent_froglight", "minecraft:verdant_froglight");

    /**
     * One extra froglight for a real player kill inside (or credited to) a D20 domain.
     * Returns true when a drop was produced.
     */
    public static boolean onDeath(class_1309 victim, class_1282 source) {
        if (victim == null || victim.method_7325()) return false;
        if (victim.method_73183() == null || victim.method_73183().method_8608()) return false;
        class_1297 killer = source == null ? null : source.method_5529();

        List<DomainSession> sessions = DomainSessionIndex.covering(victim);
        if (sessions.isEmpty() && killer != null) sessions = DomainSessionIndex.covering(killer);
        for (DomainSession session : sessions) {
            if (!session.hasCode("D20") || !com.innerdimension.domain.runtime.DomainObjects.matches(session,"D20",victim)) continue;
            class_3222 owner = session.owner();
            if (!creditedTo(session, owner, killer, source)) continue;
            // Once per (session, victim): a duplicate death callback cannot double-drop.
            if (!session.once("d20_death_" + victim.method_5667())) continue;
            class_1792 drop = D01D23Support.item(
                    FROGLIGHTS.get(D01D23Support.random(session).method_43048(FROGLIGHTS.size())));
            if (drop == null) return false;
            class_2248.method_9577(session.world, D01D23Support.blockPos(victim), new class_1799(drop, 1));
            return true;
        }
        return false;
    }

    private static boolean creditedTo(DomainSession session, class_3222 owner, class_1297 killer, class_1282 source) {
        if (owner != null && killer == owner) return true;                    // owner's own kill
        if (killer instanceof class_1657 player && DomainRelations.isFriendly(session, player)) return true;
        // Autonomous domain damage attributed to this owner.
        return owner != null && DomainDamageTypes.isDomainDamage(source) && source.method_5529() == owner;
    }
}
