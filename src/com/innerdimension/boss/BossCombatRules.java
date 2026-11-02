package com.innerdimension.boss;
import java.util.*;
/** Boss-specific balance and phase rules. No player/PvP changes. */
public final class BossCombatRules {
    public static final int SHRINE_INTERVAL=12;
    private BossCombatRules(){}
    public static boolean halfPhaseDue(int nodes,float health,float maximum){return (nodes&2)==0&&health<=maximum*.5f;}
    public static boolean domainSource(String type,boolean summoned){return summoned||type.startsWith("inner_dimension:domain_");}
    /** One per-target window shared by every caster and session. */
    public static final class ShrineGate {
        private final Map<UUID,Long> next=new HashMap<>();
        public boolean claim(UUID target,long now){
            if(now<next.getOrDefault(target,Long.MIN_VALUE))return false;
            next.put(target,now+SHRINE_INTERVAL);return true;
        }
        public void expire(long now){next.values().removeIf(t->t<=now);}
        public void clear(){next.clear();}
    }
}
