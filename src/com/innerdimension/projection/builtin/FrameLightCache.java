package com.innerdimension.projection.builtin;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

/** One render-state lifetime, shared by its layers; never reused by a later frame. */
public final class FrameLightCache {
    @FunctionalInterface public interface Source { int light(int x,int y,int z); }
    private final Source source;
    private final Long2IntOpenHashMap lights=new Long2IntOpenHashMap();
    public FrameLightCache(Source source){this.source=source;lights.defaultReturnValue(-1);}
    public int queryCount(){return lights.size();}
    public int light(int x,int y,int z) {
        long key=((long)x&0x3ffffffL)<<38|((long)z&0x3ffffffL)<<12|((long)y&0xfffL);
        int value=lights.get(key);
        if(value==-1){value=source.light(x,y,z);lights.put(key,value);}
        return value;
    }
}
