package com.innerdimension.progression;

/** Heights are measured to the supporting surface, not the decorative tip. */
public final class RitualConditions {
    public enum Destination { NONE, PASSAGE, DOMAIN }
    private RitualConditions() {}
    public static double landingDistance(double fallDistance, double feetY, double supportY) {
        return Math.max(0, fallDistance) + Math.max(0, feetY - supportY);
    }
    public static boolean touches(double distance) { return distance >= 22.0 - 1.0e-4; }
    public static Destination destination(double distance, boolean gaze, boolean obsidian, boolean eye) {
        if (distance < 14.0 - 1.0e-4 || !gaze || (!obsidian && !eye)) return Destination.NONE;
        return touches(distance) && obsidian && eye ? Destination.DOMAIN : Destination.PASSAGE;
    }
}
