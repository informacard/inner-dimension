package com.innerdimension.projection;

/** Shared radial membership and reveal geometry, independent of terrain or buildings. */
public final class OpenDomainRules {
    public static final int EXPANSION_TICKS=80;
    public static final int MOVE_UNLOCK_TICKS=20;
    public static final int RETRACTION_TICKS=40;
    public static double progress(int phase,double elapsed,double from,int expansionTicks){return phase==ProjectionAnimation.RETRACTING?from*(1-ProjectionAnimation.clamp01(elapsed/RETRACTION_TICKS)):ProjectionAnimation.progress(phase,elapsed,from,expansionTicks);}
    public static int expansionTicks(double radius){return Math.max(1,(int)Math.ceil(radius*2.5));}
    public static boolean movementReady(int phase,double elapsed){return phase==ProjectionAnimation.INTERIOR&&elapsed>=MOVE_UNLOCK_TICKS;}
    private OpenDomainRules(){}
    public static boolean contains(double x,double y,double z,double radius){
        return x*x+y*y+z*z<=radius*radius;
    }
    public static boolean intersects(double minX,double minY,double minZ,double maxX,double maxY,double maxZ,double radius){
        return contains(nearest(minX,maxX),nearest(minY,maxY),nearest(minZ,maxZ),radius);
    }
    private static double nearest(double min,double max){return min>0?min:max<0?max:0;}
    public static double farthest(double x,double y,double z){
        double dx=Math.max(Math.abs(x),Math.abs(x+1)),dy=Math.max(Math.abs(y),Math.abs(y+1)),dz=Math.max(Math.abs(z),Math.abs(z+1));
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }
}
