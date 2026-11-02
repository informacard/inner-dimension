package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.class_3222;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * D16 donkey keepsake. Vanilla remains the inventory authority: the death mixin only makes
 * PlayerEntity.dropInventory observe keep-inventory for this one death, and the Fabric
 * COPY_FROM callback invokes vanilla PlayerInventory.copyFrom on the replacement player.
 */
public final class D16 implements DomainAbility {
    public static final D16 INSTANCE = new D16();
    private D16() {}
    @Override public String id() { return "minecraft:donkey"; }
    @Override public String code() { return "D16"; }

    private static final Set<UUID> KEEP_NEXT_RESPAWN = new HashSet<>();

    public static boolean protects(class_3222 player) {
        if (player == null) return false;
        for (DomainSession session : DomainSessionIndex.covering(player))
            if (session.hasCode("D16") && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D16",player)) return true;
        DomainSession own = DomainSessionIndex.byOwner(player.method_5667());
        return own != null && own.stable() && !own.ended() && own.hasCode("D16") && com.innerdimension.domain.runtime.DomainObjects.matches(own,"D16",player);
    }

    /** Makes this death take vanilla's keep-inventory branch and marks its respawn copy. */
    public static boolean keepInventoryForDeath(class_3222 player) {
        if (!protects(player)) return false;
        synchronized (KEEP_NEXT_RESPAWN) { KEEP_NEXT_RESPAWN.add(player.method_5667()); }
        return true;
    }

    /** Delegates all slot-copy semantics to vanilla PlayerInventory.copyFrom. */
    public static void copyOnRespawn(class_3222 oldPlayer, class_3222 newPlayer, boolean alive) {
        if (oldPlayer == null || newPlayer == null || alive) return;
        boolean marked;
        synchronized (KEEP_NEXT_RESPAWN) { marked = KEEP_NEXT_RESPAWN.remove(oldPlayer.method_5667()); }
        if (marked) newPlayer.method_31548().method_7377(oldPlayer.method_31548());
    }

    public static void forget(UUID player) {
        if (player == null) return;
        synchronized (KEEP_NEXT_RESPAWN) { KEEP_NEXT_RESPAWN.remove(player); }
    }
}
