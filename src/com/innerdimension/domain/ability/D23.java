package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * D23 minecraft:allay (cyan).
 *
 * <p>Gathering: every half second up to 32 item entities and 32 experience orbs inside the
 * interior are handed to the owner. Both paths go through the entity's own vanilla pickup
 * entry ({@code method_5694}), which is what enforces the pickup delay, the thrower/owner
 * permission and the real "insert into the inventory, keep the remainder" semantics — a
 * full inventory leaves the stack exactly where it was. No block occlusion is involved, and
 * a cross-session atomic claim stops two overlapping domains from collecting the same
 * entity twice.</p>
 */
public final class D23 implements DomainAbility {
    public static final D23 INSTANCE = new D23();
    private D23() {}
    @Override public String id() { return "minecraft:allay"; }
    @Override public String code() { return "D23"; }

    private static final long INTERVAL = 10;
    private static final int ITEM_BUDGET = 32;
    private static final int ORB_BUDGET = 32;
    private static final String TIMER = "d23_collect";

    /**
     * Entity uuid -> tick of the last claim. Keyed by entity (never by owner), so two
     * overlapping domains cannot both collect the same drop on the same tick.
     */
    private static final Map<UUID, Long> CLAIMED = new ConcurrentHashMap<>();

    @Override public void onStable(DomainSession s){if(com.innerdimension.domain.DomainModes.reverse(s,code()))DomainTrappedStorage.capture(s);}
    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        if (!session.dueSelf(TIMER, INTERVAL, now)) return;
        session.armSelf(TIMER, now);
        class_3222 owner = session.owner();
        if (owner == null) return;
        class_238 box = session.bounds();
        if(com.innerdimension.domain.DomainModes.reverse(session,code()))DomainTrappedStorage.prepare(session);

        List<class_1542> items = session.world.method_8390(class_1542.class, box,
                item -> item.method_5805() && !item.method_7325() && session.contains(item)
                        && !item.method_6977());
        int collected = 0;
        for (class_1542 item : items) {
            if (collected >= ITEM_BUDGET) break;
            if (!claim(item, now)) continue;
            // The real pickup: delay, thrower permission and inventory remainder all apply.
            if(com.innerdimension.domain.DomainModes.reverse(session,code())){java.util.UUID pickupOwner=((com.innerdimension.mixin.DomainItemOwnerAccessor)item).inner$pickupOwner();if(pickupOwner==null||pickupOwner.equals(session.ownerId))DomainTrappedStorage.insert(session,item);}else item.method_5694(owner);
            collected++;
        }

        List<class_1303> orbs = session.world.method_8390(class_1303.class, box,
                orb -> orb.method_5805() && !orb.method_7325() && session.contains(orb));
        int absorbed = 0;
        for (class_1303 orb : orbs) {
            if (absorbed >= ORB_BUDGET) break;
            if (!claim(orb, now)) continue;
            // Normal player pickup path: experience and mending repair stay vanilla.
            orb.method_5694(owner);
            absorbed++;
        }
    }

    private static boolean claim(class_1297 entity, long now) {
        UUID id = entity.method_5667();
        Long previous = CLAIMED.get(id);
        if (previous != null && previous == now) return false;
        CLAIMED.put(id, now);
        if (CLAIMED.size() > 4096) CLAIMED.values().removeIf(tick -> now - tick > 200);
        return true;
    }

    @Override
    public void onEnd(DomainSession session) {
        // Nothing to roll back: collected items and experience stay collected.
    }
}
