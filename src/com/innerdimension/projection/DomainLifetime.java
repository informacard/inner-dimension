package com.innerdimension.projection;
/** Durations in server ticks, including expansion. */
public final class DomainLifetime {
    public static final int MAX_TICKS=600, FORCED_BURNOUT_TICKS=200, MIN_BURNOUT_TICKS=60;
    private DomainLifetime() {}
    public static boolean vulnerableSide(boolean reversed,boolean inside){return reversed==inside;}
    public static double shellDamage(double remaining,float amount){return !Float.isFinite(amount)||amount<=0?remaining:Math.max(0,remaining-amount*20);}

    public static int burnoutTicks(long elapsed) {
        return (int)Math.max(MIN_BURNOUT_TICKS, Math.min(MAX_TICKS, Math.max(0,elapsed))/3);
    }
    public static float strength(double elapsed) {
        return (float)Math.max(0,Math.min(1,1-elapsed/MAX_TICKS));
    }
}
