package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.network.DomainAbilityStatePayload;
import net.minecraft.*;
import java.util.*;

/**
 * D19 minecraft:dolphin (blue).
 *
 * <p>Treasure sense: server-side only. Once per second the real container block entities
 * inside the fixed interior are collected and the positions are sent to the owner alone
 * through {@link DomainAbilityStatePayload#outline}; the client renders the through-wall
 * outlines. No inventory contents and no NBT are ever transmitted, and a projection
 * snapshot is never written.</p>
 */
public final class D19 implements DomainAbility {
    public static final D19 INSTANCE = new D19();
    private D19() {}
    @Override public String id() { return "minecraft:dolphin"; }
    @Override public String code() { return "D19"; }

    private static final long SCAN_INTERVAL = 20;
    private static final String TIMER = "d19_scan";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        if (!session.dueSelf(TIMER, SCAN_INTERVAL, now)) return;
        session.armSelf(TIMER, now);
        sync(session);
    }

    private static void sync(DomainSession session) {
        class_3222 owner = session.owner();
        if (owner == null) return;
        DomainAbilityStatePayload.outline(owner, D01D23Support.sessionKey(session),
                D22.outlineEntities(session), outlineContainers(session));
    }

    /**
     * Positions of real container block entities inside the interior. Read-only: it queries
     * the real world through the session's own block range and never touches the projection
     * snapshot.
     */
    public static List<class_2338> outlineContainers(DomainSession session) {
        List<class_2338> out = new ArrayList<>();
        if (session == null || session.ended() || !session.hasCode("D19") || session.world.method_8608()) return out;
        for (class_2338 pos : session.blocksIn()) {
            class_2586 blockEntity = session.world.method_8321(pos);
            if (blockEntity instanceof class_1263 || class_7923.field_41175.method_10221(session.world.method_8320(pos).method_26204()).toString().equals("minecraft:ancient_debris")) out.add(pos.method_10062());
        }
        return out;
    }

    /** Delegates to D22 so a merged payload can be built from either side. */
    public static List<Integer> outlineEntities(DomainSession session) {
        return D22.outlineEntities(session);
    }

    @Override
    public void onEnd(DomainSession session) {
        class_3222 owner = session.owner();
        if (owner == null) return;
        // Leaving clears the owner's outline set immediately.
        DomainAbilityStatePayload.outline(owner, D01D23Support.sessionKey(session), List.of(), List.of());
    }
}
