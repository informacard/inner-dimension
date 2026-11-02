package com.innerdimension.projection;

/** Shared visual sampling for the world crest and the owner's HUD. */
public final class CrestAnimation {
    public record Frame(float distance, float size, float opacity) {}
    private CrestAnimation() {}

    /**
     * Return the enhanced crest to its ordinary visibility. Only size and opacity attenuate:
     * the distance in front of the body is kept, so the crest never drifts towards the player
     * while a domain is winding down.
     */
    public static Frame fade(Frame frame,float initialSize,float strength) {
        return new Frame(frame.distance(),
                (float)ProjectionAnimation.lerp(initialSize,frame.size(),strength),frame.opacity()*strength);
    }

    public static Frame sample(int phase, double elapsed, int shellTicks, int interiorTicks,
                               float d0, float d1, float d2, float s0, float s1, float s2) {
        if (phase == ProjectionAnimation.RETRACTING) {
            float opacity = (float)(1 - ProjectionAnimation.clamp01(elapsed / ProjectionAnimation.FADE_TICKS));
            float size = (float)ProjectionAnimation.lerp(s2, 0, elapsed / ProjectionAnimation.CLOSE_TICKS);
            return new Frame(d2, size, opacity);
        }
        if (phase == ProjectionAnimation.EXPANDING) {
            double progress = elapsed / Math.max(1, shellTicks);
            return new Frame((float)ProjectionAnimation.lerp(d0, d1, progress),
                    (float)ProjectionAnimation.lerp(s0, s1, progress), 1);
        }
        if (phase == ProjectionAnimation.INTERIOR) {
            double progress = elapsed / Math.max(1, interiorTicks);
            return new Frame((float)ProjectionAnimation.lerp(d1, d2, progress),
                    (float)ProjectionAnimation.lerp(s1, s2, progress), 1);
        }
        return new Frame(d2, s2, 1);
    }
}
