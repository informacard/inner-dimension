package com.innerdimension.network;

import net.minecraft.*;

/** The design is local; only the successful edit event crosses to the server. */
public record CrestChangedPayload() implements class_8710 {
    public static final CrestChangedPayload INSTANCE = new CrestChangedPayload();
    public static final class_8710.class_9154<CrestChangedPayload> ID = new class_8710.class_9154<>(class_2960.method_60655("inner_dimension", "crest_changed"));
    public static final class_9139<class_9129,CrestChangedPayload> CODEC = class_9139.method_56431(INSTANCE);
    public class_8710.class_9154<CrestChangedPayload> method_56479() { return ID; }
    public static void receive(class_3222 player) {
        if (com.innerdimension.progression.DomainAccess.isUnlocked(player))
            com.innerdimension.progression.InnerAdvancements.grant(player, "custom_crest");
    }
}
