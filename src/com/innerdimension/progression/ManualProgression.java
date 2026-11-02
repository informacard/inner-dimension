package com.innerdimension.progression;

import com.innerdimension.item.ModItems;
import net.minecraft.*;

/** Invisible, persistent book gates; these do not add tasks to the achievement tree. */
public final class ManualProgression {
    private ManualProgression() { }

    private static boolean done(class_3222 player, String id) {
        var entry = player.method_51469().method_8503().method_3851()
                .method_12896(class_2960.method_60654(id));
        return entry != null && player.method_14236().method_12882(entry).method_740();
    }

    public static void tick(class_3222 player) {
        if (com.innerdimension.core.ServerClock.now() % 20 != 0) return;
        boolean mental = InnerAdvancements.done(player, "mental_seeding");
        if (done(player, "minecraft:end/kill_dragon"))
            InnerAdvancements.grant(player, "manual/boss_dragon", "killed");
        if (InnerAdvancements.done(player, "enlightenment"))
            InnerAdvancements.grant(player, "manual/open_contest", "enlightenment");
        if (InnerAdvancements.done(player, "domain_contest"))
            InnerAdvancements.grant(player, "manual/open_contest", "domain_contest");
        if (done(player, "minecraft:end/root"))
            InnerAdvancements.grant(player, "manual/void_hint", "entered_end");
        if (mental) {
            InnerAdvancements.grant(player, "manual/void_hint", "mental_seeding");
            InnerAdvancements.grant(player, "manual/contest_hint", "mental_seeding");
        }
        if (InnerAdvancements.done(player, "disciple_slayer"))
            InnerAdvancements.grant(player, "manual/contest_hint", "disciple_slayer");
        if (InnerAdvancements.done(player, "manual/void_hint"))
            InnerAdvancements.grant(player, "manual/void_wait", "void_hint");
        if (InnerAdvancements.done(player, "manual/void_trial"))
            InnerAdvancements.grant(player, "manual/void_wait", "trial");
        if (InnerAdvancements.done(player, "fallen"))
            InnerAdvancements.grant(player, "manual/abyss_ritual", "fallen");
        // Remember acquisition, even when the item is later stored in a chest or consumed.
        for (int i = 0; i < player.method_31548().method_5439(); i++) {
            if (player.method_31548().method_5438(i).method_7909() == ModItems.INFECTOR) {
                InnerAdvancements.grant(player, "manual/abyss_ritual", "infector");
                break;
            }
        }
    }

    public static void noteVoidTrial(class_3222 player, class_3218 world) {
        if (!player.method_5805() || com.innerdimension.voiddomain.VoidContamination.inOwnRoom(player)) return;
        if (!"minecraft:the_end".equals(world.method_27983().method_29177().toString())) return;
        InnerAdvancements.grant(player, "manual/void_trial");
    }
}
