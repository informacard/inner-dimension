package com.innerdimension.projection;

import java.util.*;

/** Pure transport state: incomplete batches never expose partial changes. */
public final class ProjectionBatch<T> {
    // A legal radius-64 open domain fits inside 128^3 cells. Keep packets small.
    public static final int FRAGMENT_SIZE=256, MAX_PARTS=8192;
    public enum Decision { APPLY, STALE, RESYNC }
    private final List<List<T>> fragments;
    private int received;
    public ProjectionBatch(int parts) {
        if(parts<1 || parts>MAX_PARTS) throw new IllegalArgumentException("Invalid fragment count");
        fragments=new ArrayList<>(Collections.nCopies(parts,null));
    }
    public void receive(int part,List<T> entries) {
        receiveNew(part,entries);
    }
    /** Duplicate fragments must not extend idle timeouts. */
    public boolean receiveNew(int part,List<T> entries) {
        if(part<0 || part>=fragments.size() || entries.size()>FRAGMENT_SIZE) throw new IllegalArgumentException("Invalid fragment");
        if(fragments.get(part)==null) { fragments.set(part,List.copyOf(entries)); received++; return true; }
        else if(!fragments.get(part).equals(entries)) throw new IllegalArgumentException("Conflicting fragment");
        return false;
    }
    public boolean complete() { return received==fragments.size(); }
    public List<T> entries() {
        if(!complete()) throw new IllegalStateException("Incomplete transaction");
        List<T> entries=new ArrayList<>(); fragments.forEach(entries::addAll); return List.copyOf(entries);
    }
    public static Decision decide(long current,long base,long next,boolean full) {
        return decide(current,base,next,full,false);
    }
    /** An already committed same-version full snapshot is redundant unless a repair was requested. */
    public static Decision decide(long current,long base,long next,boolean full,boolean fullAlreadyCommitted) {
        if(next<0 || !full && (base<0 || next!=base+1)) return Decision.RESYNC;
        if(next<current || !full && next==current) return Decision.STALE;
        if(full&&next==current&&fullAlreadyCommitted)return Decision.STALE;
        return full || base==current ? Decision.APPLY : Decision.RESYNC;
    }
}
