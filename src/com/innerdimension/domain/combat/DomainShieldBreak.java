package com.innerdimension.domain.combat;

import net.minecraft.*;

/** Vanilla shield cooldown shared by flying slashes and Shrine cuts. */
public final class DomainShieldBreak {
    public static final int DURATION = 30;
    private DomainShieldBreak() {}

    public static boolean applies(String type, float taken, boolean blocked) {
        return (taken > 0 || blocked) && (DomainDamageTypes.SHRINE_SLASH.equals(type)
                || DomainDamageTypes.FLYING_SLASH.equals(type));
    }

    public static void after(class_1309 victim, class_1282 source, float taken, boolean blocked) {
        if (!(victim instanceof class_1657 player) || source == null
                || !applies(DomainDamageTypes.keyOf(source), taken, blocked)) return;
        // Cool down the vanilla group even if the shield is equipped after this hit.
        player.method_7357().method_7906(class_2960.method_12829("minecraft:shield"), DURATION);
        for (class_1268 hand : class_1268.values()) {
            class_1799 stack = player.method_5998(hand);
            if (stack.method_58694(class_9334.field_56396) != null)
                player.method_7357().method_62835(stack, DURATION);
        }
        if (player.method_6030().method_58694(class_9334.field_56396) != null) player.method_6021();
    }
}
