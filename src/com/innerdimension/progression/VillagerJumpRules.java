package com.innerdimension.progression;

/** Solve vanilla's discrete air motion so the apex, rather than velocity, becomes 1/4. */
public final class VillagerJumpRules {
    private VillagerJumpRules(){}
    public static double height(double velocity,double gravity){
        double height=0;
        for(int i=0;i<4096&&velocity>0;i++){
            height+=velocity;
            velocity=(velocity-gravity)*.98;
            if(Math.abs(velocity)<.003)velocity=0;
        }
        return height;
    }
    public static float quarter(float velocity,double gravity){
        if(velocity<=0)return velocity;
        if(gravity<=0)return velocity*.25f;
        double target=height(velocity,gravity)*.25,low=0,high=velocity;
        for(int i=0;i<40;i++){
            double mid=(low+high)*.5;
            if(height(mid,gravity)<target)low=mid;else high=mid;
        }
        return (float)((low+high)*.5);
    }
}
