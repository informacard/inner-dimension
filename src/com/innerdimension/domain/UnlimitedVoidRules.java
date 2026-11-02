package com.innerdimension.domain;

/** Pure tick arithmetic. Caps apply to the complete episode, including exposure. */
public final class UnlimitedVoidRules {
    public static final String ID="inner_dimension:unlimited_void";
    public static final int PLAYER_CAP=240,ENLIGHTENED_CAP=120,MOB_CAP=300,IMMUNITY=1200;
    private UnlimitedVoidRules(){}
    public static double multiplier(int capacity,int desire,int desireCap){
        double volume=Math.max(0,Math.min(1,(capacity-6)/24.0));
        double libido=Math.max(0,Math.min(1,desire/(double)Math.max(1,desireCap)));
        return Math.max(.5,1-.15*volume-.35*libido);
    }
    public static double debt(double previous,boolean exposed,double multiplier){return Math.max(0,previous-1)+(exposed?10*multiplier:0);}
}
