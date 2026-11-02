package com.innerdimension.domain;

import com.innerdimension.projection.*;
import com.innerdimension.dimension.*;
import com.innerdimension.progression.DomainState;
import net.minecraft.*;
import java.util.*;

/** A stable per-cell coin toss changes the earlier physical projection, as the later wave arrives. */
final class OpenDomainOverlap {
    private OpenDomainOverlap() {}
    private static final Map<DomainContest.Entry,Map<DomainContest.Entry,Set<class_2338>>> PROCESSED=new WeakHashMap<>();
    private static final Map<DomainContest.Entry,FloorPatternSampler> PATTERNS=new WeakHashMap<>();
    private static final Map<DomainContest.Entry,Map<class_2338,ProjectionUpdatePayload.Entry>> ORIGINALS=new WeakHashMap<>();
    static void restore(DomainContest.Entry survivor) {
        var original=ORIGINALS.remove(survivor);if(original==null)return;
        var changes=new ArrayList<>(original.values());
        if(survivor.interior.mirror()!=null)survivor.interior.mirror().clearContestOverrides(original.keySet());
        var snapshot=(RoomProjectionContraption)survivor.interior.getContraption();
        long base=snapshot.version();snapshot.applyBatch(changes,false,base+1);ProjectionMirror.broadcastChanges(survivor.interior,base,changes);
        PROCESSED.remove(survivor);PATTERNS.remove(survivor);
    }
    private static void remember(DomainContest.Entry entry,class_2338 pos,net.minecraft.class_3499.class_3501 cell) {
        ORIGINALS.computeIfAbsent(entry,key->new HashMap<>()).putIfAbsent(pos,new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(cell.comp_1342()),cell.comp_1343()));
    }
    static void mix(DomainContest.Entry first,DomainContest.Entry second) {
        var world=(class_3218)second.shell.method_73183();
        var sampler=PATTERNS.computeIfAbsent(second,key->{
            var source=DebugDomain.source(second.owner);if(source==null)source=DynamicDimensionManager.getOrCreate(world.method_8503(),second.owner);
            class_2680[][] tile=new class_2680[16][16];
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)tile[x][z]=source.method_8320(new class_2338(x,RoomGeometry.DOMAIN_FLOOR+1,z));
            return new FloorPatternSampler(tile,DomainState.get(world).floorPattern(second.owner));
        });
        var processed=PROCESSED.computeIfAbsent(first,key->new WeakHashMap<>()).computeIfAbsent(second,key->new HashSet<>());
        var earlier=(RoomProjectionContraption)first.interior.getContraption();
        var later=(RoomProjectionContraption)second.interior.getContraption();
        var firstSource=first.open()?DebugDomain.source(first.owner):null;
        if(first.open()&&firstSource==null)firstSource=DynamicDimensionManager.getOrCreate(world.method_8503(),first.owner);
        FloorPatternSampler firstPattern=null;
        if(firstSource!=null) {
            var ownTile=new class_2680[16][16];
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)ownTile[x][z]=firstSource.method_8320(new class_2338(x,RoomGeometry.DOMAIN_FLOOR+1,z));
            firstPattern=new FloorPatternSampler(ownTile,DomainState.get(world).floorPattern(first.owner));
        }
        List<ProjectionUpdatePayload.Entry> changes=new ArrayList<>(),remove=new ArrayList<>();
        var a=first.interior.getAnchorVec();var b=second.interior.getAnchorVec();
        // Surface culling can leave a cell only in the later snapshot. Mix the union.
        var candidates=new LinkedHashMap<>(earlier.getBlocks());
        if(first.open())for(var cell:later.getBlocks().entrySet()) {
            var pos=cell.getKey();
            var local=new class_2338((int)Math.floor(b.field_1352+pos.method_10263()+.5-a.field_1352),
                (int)Math.floor(b.field_1351+pos.method_10264()+.5-a.field_1351),
                (int)Math.floor(b.field_1350+pos.method_10260()+.5-a.field_1350));
            candidates.putIfAbsent(local,new net.minecraft.class_3499.class_3501(local,cell.getValue().comp_1342(),cell.getValue().comp_1343()));
        }
        for(var cell:candidates.entrySet()) {
            var pos=cell.getKey();var point=a.method_1031(pos.method_10263()+.5,pos.method_10264()+.5,pos.method_10260()+.5);
            if(processed.contains(pos))continue;
            if(firstSource!=null&&earlier.sourceSpec().mirror().contains(pos)) {
                var building=firstSource.method_8320(pos);
                if(!building.method_26215()&&building.method_26204()!=com.innerdimension.block.ModBlocks.SOUL_MIST){processed.add(pos);continue;}
            }
            var box=new class_238(point.field_1352-.49,point.field_1351-.49,point.field_1350-.49,point.field_1352+.49,point.field_1351+.49,point.field_1350+.49);
            if(!first.geometryContains(box)||!second.geometryContains(box))continue;
            if(!terrainCube(cell.getValue().comp_1342(),earlier,pos))continue;
            processed.add(pos);
            var local=new class_2338((int)Math.floor(point.field_1352-b.field_1352),(int)Math.floor(point.field_1351-b.field_1351),(int)Math.floor(point.field_1350-b.field_1350));
            long hash=pos.method_10063()^second.owner.getLeastSignificantBits();hash^=hash>>>33;hash*=0xff51afd7ed558ccdL;hash^=hash>>>33;
            var state=!first.open()||(hash&1)==0?FloorPatternSampler.floorBlock(sampler.at(local.method_10263(),local.method_10260())):cell.getValue().comp_1342();
            if(!earlier.getBlocks().containsKey(pos)) {
                if((hash&1)!=0)state=FloorPatternSampler.floorBlock(firstPattern.at(pos.method_10263(),pos.method_10260()));
            }
            if(state!=cell.getValue().comp_1342()||!earlier.getBlocks().containsKey(pos)){remember(first,pos,earlier.getBlocks().getOrDefault(pos,new net.minecraft.class_3499.class_3501(pos,class_2246.field_10124.method_9564(),null)));changes.add(new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(state),null));}
            if(later.getBlocks().containsKey(local)){remember(second,local,later.getBlocks().get(local));remove.add(new ProjectionUpdatePayload.Entry(local,class_2248.method_9507(class_2246.field_10124.method_9564()),null));}
        }
        update(first.interior,earlier,changes);update(second.interior,later,remove);
    }
    private static boolean terrainCube(class_2680 state,RoomProjectionContraption snapshot,class_2338 pos) {
        var block=state.method_26204();
        return block==com.innerdimension.block.ModBlocks.SOLID_FLESH_WATER
            ||block==com.innerdimension.block.ModBlocks.SOLID_VOID_WATER
            ||state.method_26212(snapshot.snapshotView(),pos);
    }
    private static void update(RoomProjectionEntity entity,RoomProjectionContraption snapshot,List<ProjectionUpdatePayload.Entry> changes) {
        if(changes.isEmpty())return;
        if(entity.mirror()!=null)entity.mirror().contestOverrides(changes);
        long base=snapshot.version();snapshot.applyBatch(changes,false,base+1);
        ProjectionMirror.broadcastChanges(entity,base,changes);
    }
}
