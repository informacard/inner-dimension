package com.innerdimension.item;

import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.class_1309;
import net.minecraft.class_1792;

/** Both variants unlock ability lore while equipped in chest/navel. */
public final class NavelPiercingItem extends class_1792 implements Trinket {
    public NavelPiercingItem(class_1793 settings){super(settings);}
    public static boolean isEquipped(class_1309 entity){
        return TrinketsApi.getTrinketComponent(entity).map(c->c.isEquipped(ModItems.AMETHYST_NAVEL_PIERCING)||c.isEquipped(ModItems.CLIP_ON_AMETHYST_NAVEL_PIERCING)).orElse(false);
    }
}
