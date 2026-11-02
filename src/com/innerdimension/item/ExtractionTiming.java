package com.innerdimension.item;
/** Shared timing, independent of worlds, rendering and player settings. */
public final class ExtractionTiming {
 public static final int TICKS=20;
 public static boolean complete(long elapsed){return elapsed>=TICKS;}
 public static float fovScale(float elapsed){return 1f-.25f*Math.max(0,Math.min(1,elapsed/TICKS));}
 private ExtractionTiming(){}
}
