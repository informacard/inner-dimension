package com.innerdimension.domain;

/** Pure rules shared by the server and regression checks. Durations are ticks. */
public final class DomainContestRules {
    public static final int REACTION_TICKS = 20;
    private DomainContestRules() {}
    /** All endpoints must be detached before any collapse callback is invoked. */
    public static <T> java.util.Set<T> closedGroup(T first,T second,java.util.function.Function<T,T> partner) {
        var group=new java.util.LinkedHashSet<T>();group.add(first);group.add(second);
        for(var e:java.util.List.copyOf(group)){var other=partner.apply(e);if(other!=null)group.add(other);}
        return group;
    }
    public static double average(double first,double second) { return Math.max(.05,(first+second)/2); }
    public static double penalized(double strength) { return Math.max(0,strength-3); }
    public static double accelerateOpen(double remaining,double average) { return remaining/average; }
    public static double shortenClosed(double remaining,double average) { return Math.max(0,remaining-average*20-60); }
    public static int clashTicks(double first, double second) {
        return Math.max(1, (int)Math.ceil((first + second) * 10));
    }
    public static int result(double first, double second, double threshold) {
        return Math.abs(first-second)<threshold || first==second ? 0 : first>second ? 1 : -1;
    }
    public static double simpleTicks(double enemy) { return Math.max(0, 600 - Math.max(0, enemy) * 10); }
    public static double damageTicks(double damage) { return Math.max(0, damage) * 2; }
}
