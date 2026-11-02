package com.innerdimension.client;

import com.innerdimension.projection.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.*;
import java.util.*;

/** Bounded, instance-scoped atomic reassembly. Spawn, tracked metadata and structure fragments may arrive separately. */
public final class ProjectionSyncClient {
    private record Key(UUID instance,long base,long next,boolean full) { }
    private static final class Batch {
        final ProjectionUpdatePayload first;
        final ProjectionBatch<ProjectionUpdatePayload.Entry> fragments;
        long received;
        Batch(ProjectionUpdatePayload p) { first=p; received=ticks; fragments=new ProjectionBatch<>(p.parts()); }
        List<ProjectionUpdatePayload.Entry> entries() {
            return fragments.entries();
        }
    }
    private static final Map<Key,Batch> batches=new LinkedHashMap<>();
    private static final Map<Key,ProjectionInitialPayload> initials=new HashMap<>();
    private static final Map<UUID,Long> requested=new HashMap<>();
    // Key by snapshot object, not entity id/UUID: a re-tracked entity needs its initial snapshot again.
    private static final Map<RoomProjectionContraption,Boolean> committedFull=new WeakHashMap<>();
    private static long ticks;
    private ProjectionSyncClient() { }
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ProjectionInitialPayload.ID,(payload,context)->context.client().execute(()->{
            var p=payload.fragment();var key=new Key(p.instance(),p.base(),p.next(),p.full());
            if(!p.full() || p.base()!=-1) return;
            var previous=initials.get(key);
            if(previous!=null && (previous.shell()!=payload.shell() || !previous.spec().equals(payload.spec()))) { batches.remove(key); initials.remove(key); request(p); return; }
            receive(p); if(batches.containsKey(key)) initials.put(key,payload);
        }));
        ClientPlayNetworking.registerGlobalReceiver(ProjectionUpdatePayload.ID,(payload,context) ->
            context.client().execute(() -> receive(payload)));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client) -> clear());
    }
    private static void receive(ProjectionUpdatePayload p) {
        Key key=new Key(p.instance(),p.base(),p.next(),p.full());
        if(!batches.containsKey(key) && batches.size()>=256) { batches.clear(); initials.clear(); request(p); return; }
        Batch batch=batches.computeIfAbsent(key,ignored -> new Batch(p));
        if(batch.first.parts()!=p.parts() || batch.first.entityId()!=p.entityId()) { batches.remove(key); request(p); return; }
        try { if(batch.fragments.receiveNew(p.part(),p.entries()))batch.received=ticks; }
        catch(IllegalArgumentException failure) { batches.remove(key); request(p); }
    }
    public static void tick(class_310 client) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        ticks++;
        if(client.field_1687==null) { clear(); return; }
        for(var iterator=batches.entrySet().iterator();iterator.hasNext();) {
            var batchEntry=iterator.next(); Batch batch=batchEntry.getValue(); var p=batch.first;
            var initial=initials.get(batchEntry.getKey());
            if(ticks-batch.received>100) { iterator.remove(); request(p); continue; }
            var entity=client.field_1687.method_8469(p.entityId());
            if(entity==null) continue;
            if(!(entity instanceof RoomProjectionEntity projection) || !entity.method_5667().equals(p.instance())) { iterator.remove(); continue; }
            if(!batch.fragments.complete()) continue;
            if(projection.getContraption()==null && initial!=null) {
                var snapshot=new RoomProjectionContraption();snapshot.sourceSpec(initial.spec());projection.setContraption(snapshot);
            }
            if(!(projection.getContraption() instanceof RoomProjectionContraption snapshot)) continue;
            if(initial!=null && (projection.isShell()!=initial.shell() || !snapshot.sourceSpec().equals(initial.spec()))) continue;
            boolean initialized=committedFull.containsKey(snapshot)&&!requested.containsKey(p.instance());
            var decision=ProjectionBatch.decide(snapshot.version(),p.base(),p.next(),p.full(),initialized);
            if(decision==ProjectionBatch.Decision.STALE) { iterator.remove(); continue; }
            if(decision==ProjectionBatch.Decision.RESYNC) { request(p); iterator.remove(); continue; }
            var entries=batch.entries(); Set<class_2338> positions=new HashSet<>();
            boolean valid=p.next()>=0 && (p.full() || p.next()==p.base()+1);
            for(var entry:entries) valid &= (initial==null ? snapshot.validInterior(entry.pos()) : initial.spec().source().contains(entry.pos()) && initial.spec().isShell(entry.pos())==initial.shell()) && positions.add(entry.pos())
                    && entry.stateId()>=0 && class_2248.method_9507(class_2248.method_9531(entry.stateId()))==entry.stateId();
            if(valid) {
                com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.UPDATE_ENTRIES,entries.size());
                if(p.full())com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.FULL_SYNC_ENTRIES,entries.size());
                snapshot.applyBatch(entries,p.full(),p.next());
                projection.updateBounds();
                RoomProjectionRenderer.applyUpdates(projection,entries,p.full());
                if(p.full()){committedFull.put(snapshot,Boolean.TRUE);requested.remove(p.instance());}
            } else request(p);
            iterator.remove();
        }
        initials.keySet().retainAll(batches.keySet());
        requested.entrySet().removeIf(e -> ticks-e.getValue()>200);
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.APPLY_UPDATE,auditStart); }
    }
    private static void request(ProjectionUpdatePayload p) {
        if(ticks-requested.getOrDefault(p.instance(),-100L)<20 || !ClientPlayNetworking.canSend(ProjectionResyncPayload.ID)) return;
        requested.put(p.instance(),ticks);
        ClientPlayNetworking.send(new ProjectionResyncPayload(p.entityId(),p.instance()));
    }
    private static void clear() { batches.clear(); initials.clear(); requested.clear(); committedFull.clear(); }
}
