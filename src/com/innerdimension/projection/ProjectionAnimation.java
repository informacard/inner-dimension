package com.innerdimension.projection;

/** Pure timeline shared by server, client and verification harness. Times are game ticks. */
public final class ProjectionAnimation {
    public static final int EXPANDING = 0, INTERIOR = 1, STABLE = 2, RETRACTING = 3;
    public static final int DEFAULT_SHELL_TICKS = 15, DEFAULT_INTERIOR_TICKS = 5;
    public static final int FADE_TICKS = 4, SHRINK_TICKS = 12, CLOSE_TICKS = FADE_TICKS + SHRINK_TICKS;
    public static final int FLASH_TICKS = 4;
    public static final int BREAK_CHIPS_TICKS=4, BREAK_TICKS=10;
    public record BreakFrame(double elapsed,long seed,boolean shell) {}
    /** Coordinate hash is stable across frames and clients; about one fifth of shell blocks chip. */
    public static double breakScale(BreakFrame frame,int x,int y,int z) {
        long h=frame.seed() ^ x*341873128712L ^ y*132897987541L ^ z*42317861L;
        h^=h>>>33;h*=0xff51afd7ed558ccdL;h^=h>>>33;
        double all=1-clamp((frame.elapsed()-BREAK_CHIPS_TICKS)/(BREAK_TICKS-BREAK_CHIPS_TICKS));
        if(!frame.shell()||Math.floorMod(h,5)!=0)return all;
        double delay=Math.floorMod(h>>>8,1000)/1000.0*1.5;
        return Math.min(all,1-clamp((frame.elapsed()-delay)/2.5));
    }
    public static final double MIN_SCALE = .04;
    private ProjectionAnimation() { }
    public static double progress(int phase, double elapsed, double start, int interiorTicks) {
        if (phase == STABLE) return 1;
        if (phase == RETRACTING) return start * (1 - clamp(elapsed / FADE_TICKS));
        if (phase == INTERIOR) return clamp(elapsed / interiorTicks);
        return 0;
    }
    public static double shellScale(int phase, double elapsed, double shellFrom, double interiorFrom, int shellTicks) {
        if (phase == EXPANDING) return MIN_SCALE + (1-MIN_SCALE)*clamp(elapsed/shellTicks);
        if (phase == RETRACTING) {
            double delay = interiorFrom > 0 ? FADE_TICKS : 0;
            return Math.max(MIN_SCALE, shellFrom*(1-clamp((elapsed-delay)/SHRINK_TICKS)));
        }
        return 1;
    }
    public static int closeTicks(double interiorFrom) { return SHRINK_TICKS + (interiorFrom > 0 ? FADE_TICKS : 0); }
    public static boolean collision(int phase, boolean shell) { return phase == STABLE || shell && phase == INTERIOR; }
    public static double distance(double x, double z, double cx, double cz) {
        return Math.max(Math.abs(x - cx), Math.abs(z - cz));
    }
    public static int columnRing(int x, int z, double cx, double cz) {
        double d = Math.max(Math.max(Math.abs(x-cx), Math.abs(x+1-cx)),
                Math.max(Math.abs(z-cz), Math.abs(z+1-cz)));
        return (int)Math.ceil(d * 2);
    }
    public static int spatialRing(int x, int y, int z, double cx, double cy, double cz) {
        double d = Math.max(Math.max(Math.abs(x-cx), Math.abs(x+1-cx)),
                Math.max(Math.max(Math.abs(y-cy), Math.abs(y+1-cy)),
                        Math.max(Math.abs(z-cz), Math.abs(z+1-cz))));
        return (int)Math.ceil(d * 2);
    }
    private static double clamp(double v) { return Math.max(0, Math.min(1, v)); }
    public static double clamp01(double v) { return clamp(v); }
    public static double lerp(double from,double to,double progress) { return from+(to-from)*clamp(progress); }
}
