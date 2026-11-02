package com.innerdimension.voiddomain;
/** Full original eye cycle, or five seconds for either looping Psyklon clip. */
public final class RitualVisualSchedule {
    private RitualVisualSchedule(){}
    public static long playMillis(int variant){return variant==1?8670:9000;}
    public static int playTicks(int variant){return (int)((playMillis(variant)+49)/50);}
}
