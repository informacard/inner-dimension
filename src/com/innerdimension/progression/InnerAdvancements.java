package com.innerdimension.progression;

import com.innerdimension.InnerDimension;
import net.minecraft.*;

/** Vanilla advancement progress is the sole persistent unlock state. */
public final class InnerAdvancements {
    private InnerAdvancements() { }
    public static boolean done(class_3222 player, String name) {
        var entry = player.method_51469().method_8503().method_3851().method_12896(InnerDimension.id(name));
        return entry != null && player.method_14236().method_12882(entry).method_740();
    }
    public static void grant(class_3222 player, String name) { grant(player, name, "event"); }
    public static void grant(class_3222 player, String name, String criterion) {
        var entry = player.method_51469().method_8503().method_3851().method_12896(InnerDimension.id(name));
        if (entry != null) player.method_14236().method_12878(entry, criterion);
        if(name.equals("void_baptism")&&done(player,name))DomainFeatures.grantAdvancedAbilities(player);
        if(name.equals("enlightenment"))DomainFeatures.grantOpenAbilities(player);
    }
}
