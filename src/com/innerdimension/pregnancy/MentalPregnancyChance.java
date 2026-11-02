package com.innerdimension.pregnancy;
/** Whole steps avoid cumulative floating point drift; each step is 0.2 percentage points. */
public final class MentalPregnancyChance {
    /** Guaranteed fragment payout floors, so a completed mental pregnancy always pays out. */
    public static final int FRAGMENT_FLOOR = 10;
    public static final int VOID_FRAGMENT_FLOOR = 32;
    /** Each rapid consecutive domain opening adds 20 temporary percentage points. */
    public static final double RAPID_BONUS_PER_OPEN = 0.20D;
    /** Openings closer together than this stack; anything slower is a fresh chain. */
    public static final long RAPID_WINDOW_MILLIS = 6_000L;
    /** The temporary bonus cannot grow past a certain point, so it stays a nudge. */
    public static final int RAPID_CHAIN_CAP = 10;

    private MentalPregnancyChance(){}
    public static boolean discoveryGuaranteed(int eligibleAttempts, boolean discovered) {
        return !discovered && eligibleAttempts >= 50;
    }
    public static boolean discoveryGuaranteed(int eligibleAttempts, boolean discovered, int fertilityAmplifier) {
        return !discovered && fertilityAmplifier >= 0 || discoveryGuaranteed(eligibleAttempts, discovered);
    }
    public static int next(int steps){return Math.min(10,Math.max(0,steps)+1);}
    public static double probability(int steps,int amplifier,boolean voidEngraved){
        return voidEngraved&&amplifier>=0?1.0:probability(steps,amplifier);
    }

    /** Void pregnancies keep their certainty; ordinary ones keep the engraved chance. */
    public static double probability(int steps,int amplifier,boolean voidEngraved,int rapidChain){
        return escalate(probability(steps,amplifier,voidEngraved),rapidChain);
    }

    /** Temporary rapid-opening bonus on top of any base chance, capped at certainty. */
    public static double escalate(double base,int rapidChain){
        double bonus=Math.max(0,Math.min(RAPID_CHAIN_CAP,rapidChain))*RAPID_BONUS_PER_OPEN;
        return Math.min(1.0,Math.max(0.0,base)+bonus);
    }

    /**
     * Chain length for one more opening. The first opening of a chain is 0, so the second opening
     * inside the window becomes 1 and already carries one bonus step.
     */
    public static int rapidChain(int previousChain,long millisSinceLastOpen){
        if(millisSinceLastOpen<0||millisSinceLastOpen>=RAPID_WINDOW_MILLIS)return 0;
        return Math.min(RAPID_CHAIN_CAP,Math.max(0,previousChain)+1);
    }

    public static double probability(int steps,int amplifier){
        return Math.max(0,Math.min(10,steps))*0.002*(amplifier>=1?19:amplifier==0?16:1);
    }

    /** Fragment payout for a completed mental pregnancy, with the guaranteed floor applied. */
    public static int fragmentReward(int weight,boolean voidPregnancy){
        int floor=voidPregnancy?VOID_FRAGMENT_FLOOR:FRAGMENT_FLOOR;
        if(weight==Integer.MAX_VALUE)return weight;
        return Math.max(floor,weight);
    }
}
