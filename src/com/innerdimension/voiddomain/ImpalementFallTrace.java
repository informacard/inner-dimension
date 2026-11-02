package com.innerdimension.voiddomain;
/** Actual accepted descent, independent of vanilla's damage-only fall distance. */
public final class ImpalementFallTrace {
 private double top=Double.NaN,last=Double.NaN;
 public double accept(double from,double to,boolean grounded,boolean interrupted){
  if(interrupted||to>from+1e-5){top=to;last=to;return 0;}
  if(Double.isNaN(top)||Double.isNaN(last)||Math.abs(last-from)>.01)top=from;
  top=Math.max(top,from);last=to;
  double distance=Math.max(0,top-to);
  if(grounded){top=Double.NaN;last=Double.NaN;}
  return distance;
 }
 public static boolean highEnough(double distance){return distance>=22-1e-4;}
}
