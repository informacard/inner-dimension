package com.innerdimension.client.crest;

import com.innerdimension.projection.CrestAnimation;
import com.innerdimension.projection.ProjectionAnimation;

/** Persistent HUD appearance, independent of whether a projection entity exists. */
public final class CrestHudAppearance {
    public record Appearance(int width, int height, int alpha) {}
    private CrestHudAppearance() {}

    /** Gentle three-second breathing, with a stronger amplitude for Fertility II. */
    public static Appearance fertile(Appearance base, double ticks, int amplifier) {
        if (amplifier < 0) return base;
        double breath = (1 - Math.cos(ticks * Math.PI * 2 / 60.0)) / 2;
        double opacityAmplitude = amplifier >= 1 ? .65 : .40;
        int alpha = Math.max(0, Math.min(255, (int)Math.round(base.alpha() * (1 + opacityAmplitude * breath))));
        return new Appearance(base.width(), base.height(), alpha);
    }

    public static float fertilitySway(double ticks, int amplifier) {
        if (amplifier < 0) return 0;
        return (float)(Math.sin(ticks * Math.PI * 2 / 60.0) * (amplifier >= 1 ? 1.5 : .8));
    }

    public static Appearance idle() { return idle(1, 30); }
    public static Appearance idle(double scale,int idleOpacity) {
        int height=Math.max(1,(int)Math.round(20*scale));
        return new Appearance(Math.round(height*1.5f),height,Math.round(255*idleOpacity/100f));
    }

    public static Appearance projected(CrestAnimation.Frame frame, float initial, float peak) {
        return projected(frame,initial,peak,1,30);
    }

    public static Appearance projected(CrestAnimation.Frame frame, float initial, float peak,double scale,int idleOpacity) {
        double growth = peak > initial
                ? ProjectionAnimation.clamp01((frame.size() - initial) / (peak - initial)) : 1;
        float intensity = (float)ProjectionAnimation.clamp01(frame.opacity());
        int height = Math.max(1,(int)Math.round((20 + 12 * growth * intensity)*scale));
        int idleAlpha=Math.round(255*idleOpacity/100f);
        return new Appearance(Math.round(height * 1.5f), height, Math.round(idleAlpha + (166-idleAlpha) * intensity));
    }
}
