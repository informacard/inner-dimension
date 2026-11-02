package com.innerdimension.domain.ability;

import com.innerdimension.domain.compat.NoNDomainBridge;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * D05 minecraft:sheep (yellow).
 *
 * <p>Clothes shearing: every player in the domain has their NoN clothing damage raised to
 * the real maximum stage immediately, then kept there once per second. Armour items and
 * their durability are never touched, and damage that was already dealt is never rolled
 * back. The maximum stage is read from NoN at runtime, never hard-coded.</p>
 *
 * <p>The first execution of every expansion is logged with the state it found and the state
 * it left behind, plus one line when no player is inside the interior. That is the only way
 * to tell "NoN refused the change" apart from "nobody was in the domain" in a bug report;
 * the ability itself stays silent afterwards.</p>
 */
public final class D05 implements DomainAbility {
    public static final D05 INSTANCE = new D05();
    private D05() {}
    @Override public String id() { return "minecraft:sheep"; }
    @Override public String code() { return "D05"; }

    private static final Logger LOGGER = LoggerFactory.getLogger("InnerDimension");

    private static final long MAINTAIN = 20;
    private static final String TIMER = "d05_maintain";
    private static final String LOGGED = "d05_logged";
    private static final String LOGGED_FAIL = "d05_logged_fail";
    private static final String LOGGED_EMPTY = "d05_logged_empty";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        class_3222 owner = session.owner();
        Set<UUID> eligible = new HashSet<>();
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D05")) {
            if (!(entity instanceof class_3222 player)) continue;
            eligible.add(player.method_5667());
            if (!D01D23Support.periodFromEligibility(session, TIMER, player.method_5667(), MAINTAIN, now)) continue;
            int[] before = NoNDomainBridge.destroyedSkinState(player);
            boolean raised = NoNDomainBridge.raiseSkinDamage(player);
            int[] after = NoNDomainBridge.destroyedSkinState(player);
            // One line per player per expansion, plus one line if the raise failed: a silent
            // per-second success would flood the log, a silent failure would hide the reason.
            boolean first = session.once(LOGGED + player.method_5667());
            if (first || (!raised && session.once(LOGGED_FAIL + player.method_5667())))
                LOGGER.info("D05 sheep: player={} playersInside={} skin {} -> {} raised={}",
                        player.method_5477().getString(), eligible.size(), state(before), state(after), raised);
        }
        D01D23Support.retainEligible(session, TIMER, eligible);
        if (eligible.isEmpty() && session.once(LOGGED_EMPTY)) {
            // One line that separates "the query missed the owner" from "the owner really stood
            // outside": raw counts for two query paths plus the membership test per predicate.
            int rawClass = session.world.method_8390(class_1309.class, session.bounds(), e -> true).size();
            int rawNear = session.world.method_8390(class_1309.class, session.bounds().method_1014(1.0), e -> true).size();
            LOGGER.info("D05 sheep: nobody inside the domain, nothing to shear (domain {} rawClass={} rawNear={} | {})",
                    box(session.bounds()), rawClass, rawNear, session.membership(owner));
        }
    }

    /** {@code x[a..b] y[c..d] z[e..f]} so the log shows whether the owner really stood inside. */
    private static String box(class_238 b) {
        return b == null ? "?" : String.format(java.util.Locale.ROOT, "x[%.2f..%.2f] y[%.2f..%.2f] z[%.2f..%.2f]",
                b.field_1323, b.field_1320, b.field_1322, b.field_1325, b.field_1321, b.field_1324);
    }

    /** {@code stage/damage/max} for the log, or {@code ?} when NoN did not answer. */
    private static String state(int[] snapshot) {
        return snapshot == null ? "?" : (snapshot[0] + "(" + snapshot[1] + "/" + snapshot[2] + ")");
    }

    @Override
    public void onEnd(DomainSession session) {
        // Clothing damage is a permanent world change: deliberately nothing to undo.
    }
}
