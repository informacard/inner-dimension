package com.innerdimension.client.domain;
import java.util.*;
/** Per-player server-authoritative movement capabilities, including guests in another domain. */
public final class DomainObjectClientState {
    private static final Map<UUID,Integer> FLAGS=new HashMap<>();
    private DomainObjectClientState(){}
    public static void accept(UUID player,int flags){FLAGS.put(player,flags);}
    public static boolean lava(UUID player){return (FLAGS.getOrDefault(player,0)&1)!=0;}
    public static void clear(){FLAGS.clear();}
}
