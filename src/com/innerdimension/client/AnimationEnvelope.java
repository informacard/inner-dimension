package com.innerdimension.client;
/** Presentation-only timing, independent of game world or GL state. */
public final class AnimationEnvelope {
    private AnimationEnvelope(){}
    public static float opacity(long elapsed,long duration){return (float)(smooth(elapsed/300.0)*smooth((duration-elapsed)/400.0));}
    public static double smooth(double x){double t=Math.max(0,Math.min(1,x));return t*t*(3-2*t);}
}
