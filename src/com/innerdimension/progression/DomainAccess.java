package com.innerdimension.progression;
import net.minecraft.class_3222;
/** Side-effect-free authority for domain unlock checks. */
public final class DomainAccess {
    private DomainAccess() { }
    public static boolean isUnlocked(class_3222 player) { return InnerAdvancements.done(player,"cursed_core"); }
    /**
     * Imprinting is taught by the endurance chapter: a player who has never let the domain burn out
     * has not learned what the crest is for, so the engrave controls stay out of the domain screen
     * until {@code endurance_training} is done. Both the screen and the server gate honour this.
     */
    public static boolean canImprint(class_3222 player) { return InnerAdvancements.done(player,"endurance_training"); }
}
