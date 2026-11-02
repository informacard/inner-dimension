package com.innerdimension.projection;

import com.innerdimension.chunk.TemporaryChunkLoads;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.*;
import java.util.*;

/** Source-side simulation only. One bounded interior scan after all worlds tick. */
public final class ProjectionMirror {
    private static final Map<RoomProjectionEntity,Set<UUID>> STATIC_TRACKED=new WeakHashMap<>();
    /** Static NPC and simple-domain snapshots also retry after channel negotiation. */
    public static void tickStatic(RoomProjectionEntity projection) {
        Set<UUID> sent=STATIC_TRACKED.computeIfAbsent(projection,k->new HashSet<>());
        Set<UUID> present=new HashSet<>();
        for(var player:PlayerLookup.tracking(projection)) {
            UUID id=player.method_5667();present.add(id);
            if(!sent.contains(id)&&ServerPlayNetworking.canSend(player,ProjectionInitialPayload.ID)) {
                sendInitial(player,projection);sent.add(id);
            }
        }
        sent.retainAll(present);
    }
    public static void clearStatic() { STATIC_TRACKED.clear(); }
    private final class_3218 source;
    public class_3218 sourceWorld(){return source;}
    private final RoomProjectionEntity shell, interior;
    private final Set<UUID> tracked = new HashSet<>();
    private final Map<UUID,Long> resyncAt = new HashMap<>();
    private long ticks;
    private final Map<class_2338,ProjectionUpdatePayload.Entry> contestOverrides=new HashMap<>();
    public void contestOverrides(List<ProjectionUpdatePayload.Entry> entries) { for(var change:entries)contestOverrides.put(change.pos(),change); }
    public void clearContestOverrides(Set<class_2338> cells) { cells.forEach(contestOverrides::remove); }
    public ProjectionMirror(class_3218 source, RoomProjectionEntity shell, RoomProjectionEntity interior) {
        this.source=source; this.shell=shell; this.interior=interior;
    }
    public static void register() {
        PayloadTypeRegistry.playS2C().register(ProjectionInitialPayload.ID,ProjectionInitialPayload.CODEC);
        net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents.START_TRACKING.register((entity,player) -> {
            if(entity instanceof RoomProjectionEntity projection) sendInitial(player,projection);
        });
        PayloadTypeRegistry.playS2C().register(ProjectionUpdatePayload.ID,ProjectionUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ProjectionResyncPayload.ID,ProjectionResyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ProjectionResyncPayload.ID,(payload,context) -> context.server().execute(() -> {
            var entity=context.player().method_73183().method_8469(payload.entityId());
            if (!(entity instanceof RoomProjectionEntity projection)
                    || !projection.method_5667().equals(payload.instance())) return;
            var mirror=projection.mirror();
            if(mirror==null && projection.isShell()) {
                var sibling=ProjectionIndex.half(projection.method_73183(),projection.owner(),false);
                if(sibling!=null) mirror=sibling.mirror();
            }
            if (mirror == null || !PlayerLookup.tracking(projection).contains(context.player())) return;
            long last=mirror.resyncAt.getOrDefault(context.player().method_5667(),-100L);
            if (mirror.ticks-last < 20) return;
            mirror.resyncAt.put(context.player().method_5667(),mirror.ticks);
            sendInitial(context.player(),mirror.shell);
            sendInitial(context.player(),mirror.interior);
        }));
    }
    public void tick() {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        ticks++;
        if (interior.method_31481()) return;
        RoomProjectionContraption snapshot=(RoomProjectionContraption)interior.getContraption();
        List<ProjectionUpdatePayload.Entry> changes=new ArrayList<>();
        List<ProjectionUpdatePayload.Entry> geometryChanges=new ArrayList<>();
        long base=snapshot.version();
        if (interior.phase()!=ProjectionAnimation.RETRACTING) {
            var volume=snapshot.sourceSpec().mirror();
            if (volume == null) return;
            // Renew expiring entity-ticking tickets; no permanent force-load ownership.
            if (ticks%20==1) for(int cx=volume.minX()>>4;cx<=volume.maxX()>>4;cx++)
                for(int cz=volume.minZ()>>4;cz<=volume.maxZ()>>4;cz++) TemporaryChunkLoads.holdChunk(source,cx,cz);
            for(int y=volume.minY();y<=volume.maxY();y++) for(int x=volume.minX();x<=volume.maxX();x++)
                for(int z=volume.minZ();z<=volume.maxZ();z++) {
                    class_2338 pos=new class_2338(x,y,z);
                    if (!snapshot.validInterior(pos)) continue;
                    if(!com.innerdimension.domain.DomainContest.allowsBlock(interior.owner(),pos))continue;
                    var old=snapshot.getBlocks().get(pos);
                    class_2680 state=source.method_8320(pos);
                    var override=contestOverrides.get(pos);if(override!=null)state=class_2248.method_9531(override.stateId());
                    // Open-domain building coordinates overlap the independently scanned terrain
                    // skin. Source air means "no building here", not "delete the terrain cover".
                    if (snapshot.sourceSpec().shell()==null && (state.method_26215()
                            || state.method_26204()==com.innerdimension.block.ModBlocks.SOUL_MIST)) continue;
                    class_2680 before=old==null?class_2246.field_10124.method_9564():old.comp_1342();
                    boolean changed=state!=before;
                    class_2487 data=old==null?null:old.comp_1343();
                    // Never serialize arbitrary inventories each tick. Static display data at 5 Hz;
                    // chest lids and moving-piston progress are explicit per-tick adapters.
                    if (changed || state.method_31709() && (ticks%4==0 || animated(state)))
                        data=displayData(source,pos,state);
                    if (changed || !Objects.equals(data,old==null?null:old.comp_1343())) {
                        var change=new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(state),data);
                        changes.add(change);
                        if(changed || state.method_26204() instanceof class_2667 || source.method_8321(pos) instanceof class_2627) geometryChanges.add(change);
                    }
                }
        }
        OpenDomainPerformance.count(OpenDomainPerformance.Metric.MIRROR_CHANGES,changes.size());
        if (!changes.isEmpty()) {
            boolean geometry=snapshot.applyBatch(changes,false,base+1);
            if (geometry && interior.collisionEnabled()) ProjectionSafety.resolveDynamic(shell,interior,geometryChanges);
        }
        Set<UUID> now=new HashSet<>();
        for (class_3222 player:PlayerLookup.tracking(interior)) {
            now.add(player.method_5667());
            if (!ServerPlayNetworking.canSend(player,ProjectionUpdatePayload.ID)) continue;
            if (!tracked.contains(player.method_5667())) sendFull(player);
            else if (!changes.isEmpty()) send(player,base,snapshot.version(),false,changes);
        }
        tracked.retainAll(now); tracked.addAll(now);
        resyncAt.keySet().retainAll(now);
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.MIRROR_TICK,auditStart); }
    }
    private static boolean animated(class_2680 state) {
        String id=class_7923.field_41175.method_10221(state.method_26204()).method_12832();
        return id.equals("chest") || id.equals("trapped_chest") || id.equals("moving_piston")
                || id.equals("ender_chest") || id.endsWith("shulker_box");
    }
    public static class_2487 displayData(class_3218 source,class_2338 pos,class_2680 state) {
        class_2586 be=source.method_8321(pos);
        if(be==null) return null;
        class_2960 type=class_2591.method_11033(be.method_11017());
        if(type==null || !type.method_12836().equals("minecraft")) return null;
        class_2487 data=be.method_16887(source.method_30349()).method_10553();
        // These are never display data or an invitation to interact with a second inventory.
        for(String key:List.of("Items","items","LootTable","LootTableSeed","RecipesUsed")) data.method_10551(key);
        if(be instanceof class_2595) data.method_10569("ProjectionOpeners",class_2595.method_11048(source,pos));
        if(be instanceof class_2611) data.method_10569("ProjectionOpeners",((com.innerdimension.mixin.ProjectionEnderChestAccessor)be).inner$openers().method_31678());
        if(be instanceof class_2627 shulker) {
            data.method_10548("ProjectionProgress",shulker.method_11312(1));
            data.method_10548("ProjectionPreviousProgress",shulker.method_11312(0));
        }
        if(be instanceof class_2669 piston) {
            data.method_10548("progress",piston.method_11499(1));
            data.method_10548("ProjectionPreviousProgress",piston.method_11499(0));
        }
        return dataSize(data)>262144 ? null : data;
    }
    private static int dataSize(class_2487 data) {
        if(data==null) return 1;
        try {
            var bytes=new java.io.ByteArrayOutputStream();
            class_2507.method_52893(data,new java.io.DataOutputStream(bytes));
            return bytes.size();
        } catch(java.io.IOException failure) { throw new IllegalStateException("Cannot encode projection display data",failure); }
    }
    public static void sendInitial(class_3222 player,RoomProjectionEntity projection) {
        if(!ServerPlayNetworking.canSend(player,ProjectionInitialPayload.ID) || projection.getContraption()==null) return;
        var snapshot=(RoomProjectionContraption)projection.getContraption();
        List<ProjectionUpdatePayload.Entry> entries=new ArrayList<>();
        snapshot.getBlocks().forEach((pos,info)->entries.add(new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(info.comp_1342()),info.comp_1343())));
        var fragments=fragments(entries);
        for(int part=0;part<fragments.size();part++) ServerPlayNetworking.send(player,new ProjectionInitialPayload(projection.isShell(),snapshot.sourceSpec(),
            new ProjectionUpdatePayload(projection.method_5628(),projection.method_5667(),-1,snapshot.version(),true,part,fragments.size(),fragments.get(part))));
    }
    public static void broadcastChanges(RoomProjectionEntity entity,long base,List<ProjectionUpdatePayload.Entry> entries) {
        var parts=fragments(entries);long next=((RoomProjectionContraption)entity.getContraption()).version();
        for(var player:PlayerLookup.tracking(entity)) {
            if(!ServerPlayNetworking.canSend(player,ProjectionUpdatePayload.ID))continue;
            for(int part=0;part<parts.size();part++)ServerPlayNetworking.send(player,new ProjectionUpdatePayload(entity.method_5628(),entity.method_5667(),base,next,false,part,parts.size(),parts.get(part)));
        }
    }
    private void sendFull(class_3222 player) {
        sendInitial(player,shell); sendInitial(player,interior);
    }
    private void send(class_3222 player,long base,long next,boolean full,List<ProjectionUpdatePayload.Entry> entries) {
        var fragments=fragments(entries); int parts=fragments.size();
        for(int part=0;part<parts;part++) {
            ServerPlayNetworking.send(player,new ProjectionUpdatePayload(interior.method_5628(),interior.method_5667(),base,next,full,part,parts,fragments.get(part)));
        }
    }
    private static List<List<ProjectionUpdatePayload.Entry>> fragments(List<ProjectionUpdatePayload.Entry> entries) {
        List<List<ProjectionUpdatePayload.Entry>> fragments=new ArrayList<>();
        List<ProjectionUpdatePayload.Entry> fragment=new ArrayList<>(); int bytes=0;
        for(var entry:entries) {
            int size=16+dataSize(entry.data());
            if(!fragment.isEmpty() && (fragment.size()>=ProjectionUpdatePayload.FRAGMENT_SIZE || bytes+size>262144)) {
                fragments.add(List.copyOf(fragment)); fragment.clear(); bytes=0;
            }
            fragment.add(entry); bytes+=size;
        }
        if(!fragment.isEmpty() || fragments.isEmpty()) fragments.add(List.copyOf(fragment));
        int parts=fragments.size();
        if(parts>ProjectionUpdatePayload.MAX_PARTS) throw new IllegalStateException("Projection exceeds batch bound: entries="+entries.size()+", parts="+parts+", maximum="+ProjectionUpdatePayload.MAX_PARTS);
        return fragments;
    }
}
