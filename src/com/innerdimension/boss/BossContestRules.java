package com.innerdimension.boss;

/** Pure v3 resource mathematics. Units are resource ticks, never seconds. */
public final class BossContestRules {
    private BossContestRules() {}
    public record Pressure(double bossRate, double playerRate, double closedCost) {}
    public static Pressure pressure(double player, double boss) {
        double p=Math.max(0,Math.min(10,player)), b=Math.max(0,Math.min(10,boss));
        double a=Math.max(.05,(p+b)/2), d=p-b;
        return new Pressure(a*clamp(1+.15*d),a*clamp(1-.15*d),40+10*a);
    }
    private static double clamp(double v){return Math.max(.5,Math.min(1.5,v));}
    public static double topUp(double paid,double due){return Math.max(0,due-paid);}
    public static double initialBudget(double budget,long age){return Math.max(0,budget-Math.max(0,age));}
    public static int verdict(double p,double b,double threshold){return p==b||Math.abs(p-b)<threshold?0:p>b?1:-1;}
    public record Debit(double remaining,double paid){}
    public static Debit debit(double remaining,double rate,double paid,double due){return new Debit(Math.max(0,remaining-Math.max(1,rate)-topUp(paid,due)),Math.max(paid,due));}
    public static long disabledDeadline(long now,long previous,int duration,boolean heldOrJustReleased){return heldOrJustReleased?Math.max(previous,now+duration):previous;}
    public static int waitTicks(double p,double b){return (int)Math.ceil((p+b)*10);}
    public static boolean mutual(double ax,double ay,double az,double arx,double ary,double arz,
                                 double bx,double by,double bz,double brx,double bry,double brz){
        return Math.abs(ax-bx)<=Math.min(arx,brx)&&Math.abs(ay-by)<=Math.min(ary,bry)&&Math.abs(az-bz)<=Math.min(arz,brz);
    }
    /** One independent edge; gaps and member changes preserve both progress and verdict. */
    public static final class Edge {
        public double remaining;
        public int contactTicks;
        public boolean decided;
        public Edge(double budget,long age){remaining=initialBudget(budget,age);}
        public boolean step(double rate){remaining-=rate;contactTicks++;return remaining<=0;}
    }
}
