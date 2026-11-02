package com.innerdimension.progression;
/** Settlements consume lifetime collection milestones, never individual overload attempts. */
public final class DomainCapacityRules {
    private DomainCapacityRules(){}
    public static int capacity(int settled){var cfg=com.innerdimension.config.InnerConfig.get();return (int)Math.min(Integer.MAX_VALUE,(long)cfg.imprintInitialCapacity+(long)cfg.imprintExpansionPoints*Math.max(0,settled));}
    public static int settle(int settled,int collected,boolean overload,boolean seeding){return overload&&seeding?Math.max(0,settled)+(Math.max(0,collected)/com.innerdimension.config.InnerConfig.get().imprintExpansionSpecies>Math.max(0,settled)?1:0):Math.max(0,settled);}
}
