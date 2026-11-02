package com.innerdimension.domain.world;

import com.innerdimension.domain.ability.D17;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

/**
 * D17 mule endurance: while a D17 session covers the entity, hunger exhaustion, food-level
 * and saturation loss are refused. Positive gains (eating, saturation food, commands that
 * raise the values) are never touched.
 */
public final class DomainFoodGuard {
    private DomainFoodGuard() {}

    /** TRUE when a covering D17 session protects this entity from hunger loss right now. */
    public static boolean protectedFromHunger(class_1297 entity) {
        return entity instanceof class_1657 player && D17.blockHunger(player);
    }
}
