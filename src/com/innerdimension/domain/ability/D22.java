package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.network.DomainAbilityStatePayload;
import net.minecraft.*;
import java.util.*;

/**
 * D22 minecraft:bat (purple).
 *
 * <p>Echolocation: server-side collection plus a sync to the owner only. Every half second
 * the exact interior is scanned for living entities and their network ids are sent through
 * {@link DomainAbilityStatePayload#outline}; the client draws team-independent local
 * outlines, including for invisible entities. No entity ever gets the global glowing flag
 * and no other player is told anything.</p>
 */
public final class D22 implements DomainAbility {
    public static final D22 INSTANCE = new D22();
    private D22() {}
    @Override public String id() { return "minecraft:bat"; }
    @Override public String code() { return "D22"; }

    private static final long SCAN_INTERVAL = 10;
    private static final String TIMER = "d22_scan";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        if (!session.dueSelf(TIMER, SCAN_INTERVAL, now)) return;
        session.armSelf(TIMER, now);
        class_3222 owner = session.owner();
        if (owner == null) return;
        DomainAbilityStatePayload.outline(owner, D01D23Support.sessionKey(session),
                outlineEntities(session), D19.outlineContainers(session));
    }

    /**
     * Network ids of the living entities in the interior, excluding the owner itself.
     * Invisibility is deliberately not filtered: the outline is the point of the ability.
     */
    public static List<Integer> outlineEntities(DomainSession session) {
        List<Integer> out = new ArrayList<>();
        if (session == null || session.ended() || !session.hasCode("D22")) return out;
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(session,"D22"))
            if (!entity.method_5667().equals(session.ownerId)) out.add(entity.method_5628());
        return out;
    }

    /** Delegates to D19 so a merged payload can be built from either side. */
    public static List<class_2338> outlineContainers(DomainSession session) {
        return D19.outlineContainers(session);
    }

    @Override
    public void onEnd(DomainSession session) {
        class_3222 owner = session.owner();
        if (owner == null) return;
        // Leaving removes the outline set immediately.
        DomainAbilityStatePayload.outline(owner, D01D23Support.sessionKey(session), List.of(), List.of());
    }
}
