package com.innerdimension.client;
/** Identical lid timing for previews and actual ritual presentation. */
public final class RitualVisualTiming {
    private RitualVisualTiming(){}
    public static double aperture(long elapsed,long duration){return elapsed<duration?AnimationEnvelope.smooth(elapsed/4000.0):1-AnimationEnvelope.smooth((elapsed-duration)/120.0);}
}
