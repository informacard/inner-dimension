package com.innerdimension.animation;

import com.innerdimension.entity.EnderPearlDildoEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.*;

/** A sneak-click on a placed device owns the click, including the offhand fallback. */
public final class DildoInteractionGuard {
    private static final Map<class_1657, Long> CLICKS = new WeakHashMap<>();
    private DildoInteractionGuard() {}
    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (!player.method_5715() || player.method_7325()
                    || !(target instanceof EnderPearlDildoEntity dildo)) return class_1269.field_5811;
            synchronized (CLICKS) { CLICKS.put(player, world.method_75260()); }
            dildo.handleSneakInteraction(player, hand);
            return class_1269.field_5812;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            synchronized (CLICKS) {
                Long tick = CLICKS.get(player);
                if (tick != null) {
                    long age = world.method_75260() - tick;
                    if (age >= 0 && age <= 1) return class_1269.field_5814;
                    CLICKS.remove(player);
                }
            }
            return class_1269.field_5811;
        });
    }
}
