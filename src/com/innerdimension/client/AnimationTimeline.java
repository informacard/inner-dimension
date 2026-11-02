package com.innerdimension.client;
public final class AnimationTimeline {
    private AnimationTimeline(){}
    public static long duration(int[] durations){long total=0;for(int d:durations){if(d<=0)throw new IllegalArgumentException("Nonpositive frame duration");total+=d;}if(total==0)throw new IllegalArgumentException("Empty clip");return total;}
    public static int frame(long millis,int[] durations){long t=Math.floorMod(millis,duration(durations));for(int i=0;i<durations.length;i++){if(t<durations[i])return i;t-=durations[i];}return 0;}
}
