package com.innerdimension.projection;

import com.innerdimension.chunk.TemporaryChunkLoads;
import com.innerdimension.config.InnerConfig;

import com.innerdimension.projection.builtin.AbstractContraptionEntity;
import com.innerdimension.projection.builtin.Contraption;
import com.innerdimension.projection.builtin.StructureTransform;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3499.class_3501;

/** A read-only room snapshot: no assembly traversal, mounted storage or actors. */
public final class RoomProjectionContraption extends Contraption {
    private ProjectionSourceSpec sourceSpec = ProjectionSourceSpec.room();
    public void sourceSpec(ProjectionSourceSpec spec) { sourceSpec=spec; bounds=spec.source().bounds(); invalidateColliders(); }
    public ProjectionSourceSpec sourceSpec() { return sourceSpec; }
    private long version, geometryVersion;
    public long version() { return version; }
    public long geometryVersion() { return geometryVersion; }

    @Override public com.innerdimension.projection.builtin.ContraptionWorld getContraptionWorld() {
        if(collisionLevel==null) {
            var view=snapshotView();
            collisionLevel=new com.innerdimension.projection.builtin.ContraptionWorld(entity.method_73183(),this) {
                @Override public class_2680 method_8320(class_2338 pos) { return view.method_8320(pos); }
                @Override public net.minecraft.class_3610 method_8316(class_2338 pos) { return view.method_8316(pos); }
                @Override public class_2586 method_8321(class_2338 pos) { return view.method_8321(pos); }
            };
        }
        return collisionLevel;
    }

    /** No chunk lookups at source-local coordinates in the destination world. */
    public net.minecraft.class_1922 snapshotView() {
        return new net.minecraft.class_1922() {
            public class_2680 method_8320(class_2338 pos) {
                var info = blocks.get(pos);
                if (info == null && entity instanceof RoomProjectionEntity projection && !projection.isShell()) {
                    var shell = ProjectionIndex.active(entity.method_73183(), projection.owner());
                    if (shell != null && shell.isShell() && shell.getContraption() != null)
                        info = shell.getContraption().getBlocks().get(pos);
                }
                return info == null ? net.minecraft.class_2246.field_10124.method_9564() : info.comp_1342();
            }
            public net.minecraft.class_3610 method_8316(class_2338 pos) { return method_8320(pos).method_26227(); }
            public class_2586 method_8321(class_2338 pos) {
                var info = blocks.get(pos);
                if (info == null || !(info.comp_1342().method_26204() instanceof net.minecraft.class_2343 block)) return null;
                // Only moving-piston collision needs a display BE in the physics view.
                if (!(info.comp_1342().method_26204() instanceof net.minecraft.class_2667)
                        && !(info.comp_1342().method_26204() instanceof net.minecraft.class_2480)) return null;
                var be = block.method_10123(pos, info.comp_1342());
                if (be != null && info.comp_1343() != null) be.method_58690(net.minecraft.class_11352.method_71417(
                        net.minecraft.class_8942.field_60348, entity.method_73183().method_30349(), info.comp_1343()));
                if(be instanceof net.minecraft.class_2627 && info.comp_1343()!=null) {
                    var animation=(com.innerdimension.mixin.ProjectionShulkerAccessor)be;
                    animation.inner$progress(info.comp_1343().method_10583("ProjectionProgress").orElse(0f));
                    animation.inner$previousProgress(info.comp_1343().method_10583("ProjectionPreviousProgress").orElse(0f));
                }
                return be;
            }
            public int method_31605() { return (int)(bounds.field_1325 - bounds.field_1322); }
            public int method_31607() { return (int)bounds.field_1322; }
        };
    }

    public boolean validInterior(class_2338 pos) {
        if(sourceSpec.shell()==null&&sourceSpec.mirror()!=null){double radius=(sourceSpec.source().maxX()+1-sourceSpec.source().minX())*.5;if(!OpenDomainRules.intersects(pos.method_10263()-sourceSpec.anchorX(),pos.method_10264()-sourceSpec.anchorY(),pos.method_10260()-sourceSpec.anchorZ(),pos.method_10263()+1-sourceSpec.anchorX(),pos.method_10264()+1-sourceSpec.anchorY(),pos.method_10260()+1-sourceSpec.anchorZ(),radius))return false;}
        return sourceSpec.mirror() != null && sourceSpec.mirror().contains(pos) && !sourceSpec.isShell(pos);
    }

    /** One main-thread transaction for both server geometry and client rendering. */
    public boolean applyBatch(java.util.List<ProjectionUpdatePayload.Entry> changes, boolean full, long next) {
        boolean geometry = false;
        if (full) {
            geometry = true;
            blocks.clear(); updateTags.clear();
        }
        for (var change : changes) {
            var previous = blocks.get(change.pos());
            class_2680 state = net.minecraft.class_2248.method_9531(change.stateId());
            geometry |= previous == null ? !state.method_26215() : previous.comp_1342() != state;
            geometry |= state.method_26204() instanceof net.minecraft.class_2667;
            geometry |= state.method_26204() instanceof net.minecraft.class_2480;
            if (state.method_26215()) { blocks.remove(change.pos()); updateTags.remove(change.pos()); }
            else {
                blocks.put(change.pos(), new class_3501(change.pos(), state, change.data()));
                if (change.data() == null) updateTags.remove(change.pos());
                else updateTags.put(change.pos(), change.data());
            }
        }
        version = next;
        if (geometry) { geometryVersion++; if(entity!=null) invalidateColliders(); ProjectionEnvironment.invalidate(this); }
        return geometry;
    }

    @Override public void write(net.minecraft.class_11372 view, boolean spawn) {
        super.write(view, spawn);
        view.method_71466("ProjectionVersion", version);
        view.method_71466("ProjectionGeometryVersion", geometryVersion);
    }
    @Override public void read(class_1937 world, net.minecraft.class_11368 view, boolean spawn) {
        super.read(world, view, spawn);
        version = view.method_71425("ProjectionVersion", 0);
        geometryVersion = view.method_71425("ProjectionGeometryVersion", 0);
    }
    public RoomProjectionContraption() {
        anchor = class_2338.field_10980;
        // Create serializes storage even for render-only structures. An initialized EMPTY
        // manager satisfies that contract without mounting any source inventories.

    }
    /** Physical entity geometry only; no source or destination world block writes. */
    public static RoomProjectionContraption infectionPillar(){
        var result=new RoomProjectionContraption();
        result.sourceSpec(new ProjectionSourceSpec(new ProjectionSourceSpec.Volume(0,0,0,1,15,1),null,null,0,0,0));
        for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=0;y<16;y++){
            var pos=new class_2338(x,y,z);result.blocks.put(pos,new class_3501(pos,com.innerdimension.block.ModBlocks.ABYSS_BLOCK.method_9564(),null));
        }
        return result;
    }
    /** Independent immutable halves; neither half contains the other half's collision shapes. */
    public RoomProjectionContraption part(boolean shell) {
        RoomProjectionContraption result = new RoomProjectionContraption();
        result.sourceSpec = sourceSpec;
        result.bounds = bounds;
        for (var entry : blocks.entrySet()) if (sourceSpec.isShell(entry.getKey()) == shell) {
            result.blocks.put(entry.getKey(),entry.getValue());
            if (updateTags.containsKey(entry.getKey())) result.updateTags.put(entry.getKey(),updateTags.get(entry.getKey()));
        }
        return result;
    }

    /** Empty bookkeeping half used by an open domain, whose visible geometry is all interior. */
    public RoomProjectionContraption emptyPart() {
        RoomProjectionContraption result = new RoomProjectionContraption();
        result.sourceSpec = sourceSpec;
        result.bounds = bounds;
        return result;
    }

    /** Copies the open 16x16 domain core and synthesizes a terrain-following floor around it. */
    public static RoomProjectionContraption openSnapshot(class_3218 source, class_3218 destination,
                                                          net.minecraft.class_243 feet) {
        return openSnapshot(source, destination, feet, FloorPatternSettings.DEFAULT);
    }

    public static RoomProjectionContraption openSnapshot(class_3218 source, class_3218 destination,
            net.minecraft.class_243 feet, FloorPatternSettings settings) {
        return openSnapshot(source,destination,feet,settings,24);
    }
    public static RoomProjectionContraption openSnapshot(class_3218 source,class_3218 destination,net.minecraft.class_243 feet,FloorPatternSettings settings,int radius) {
        OpenDomainFloorGenerator.Generated generated = OpenDomainFloorGenerator.generate(source, destination, feet, settings,radius);
        RoomProjectionContraption result = new RoomProjectionContraption();
        result.sourceSpec = generated.spec();
        result.bounds = generated.spec().source().bounds();
        generated.blocks().forEach((pos,state) -> result.blocks.put(pos, new class_3501(pos,state,null)));
        return result;
    }

    public static RoomProjectionContraption snapshot(class_3218 source) {
        return snapshot(source, ProjectionSourceSpec.room());
    }

    /** A physical three-by-three water skin, held by a projection rather than world edits. */
    public static RoomProjectionContraption simpleSnapshot() {
        RoomProjectionContraption result=new RoomProjectionContraption();
        var volume=new ProjectionSourceSpec.Volume(-1,0,-1,1,3,1);
        result.sourceSpec(new ProjectionSourceSpec(volume,null,null,0.5,0,0.5));
        var state=com.innerdimension.block.ModBlocks.SIMPLE_FLESH_WATER.method_9564();
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            var pos=new class_2338(x,0,z);result.blocks.put(pos,new class_3501(pos,state,null));
        }
        return result;
    }

    public static RoomProjectionContraption snapshot(class_3218 source, ProjectionSourceSpec spec) {
        RoomProjectionContraption result = new RoomProjectionContraption();
        result.sourceSpec = spec;
        var volume = spec.source();
        // Include the floor at -1, walls at -1/edge and ceiling at edge.
        result.bounds = volume.bounds();
        for (int cx = volume.minX() >> 4; cx <= volume.maxX() >> 4; cx++) {
            for (int cz = volume.minZ() >> 4; cz <= volume.maxZ() >> 4; cz++) {
                TemporaryChunkLoads.holdChunk(source, cx, cz);
                source.method_8497(cx, cz);
            }
        }
        for (int y = volume.minY(); y <= volume.maxY(); y++) {
            for (int x = volume.minX(); x <= volume.maxX(); x++) {
                for (int z = volume.minZ(); z <= volume.maxZ(); z++) {
                    class_2338 pos = new class_2338(x, y, z);
                    class_2680 state = source.method_8320(pos);
                    if (state.method_26215()) continue;
                    // Only client-visible data; never capture inventories as mounted storage.
                    class_2487 displayData = null;
                    class_2586 be = source.method_8321(pos);
                    if (be != null) {
                        displayData = ProjectionMirror.displayData(source,pos,state);
                        if(displayData!=null) result.updateTags.put(pos, displayData);
                    }
                    result.blocks.put(pos, new class_3501(pos, state, displayData));
                }
            }
        }
        return result;
    }


    @Override public boolean assemble(class_1937 world, class_2338 pos) { return false; }
    @Override public boolean canBeStabilized(class_2350 direction, class_2338 pos) { return false; }

    @Override public void onEntityCreated(AbstractContraptionEntity entity) {
        this.entity = entity;
        invalidateColliders();
    }
    @Override public void onEntityInitialize(class_1937 world, AbstractContraptionEntity entity) { }
    @Override public void tickStorage(AbstractContraptionEntity entity) { }
    @Override public void startMoving(class_1937 world) { }
    @Override public void stop(class_1937 world) { }
    // Defence in depth: neither source nor destination can ever be written by a projection.
    @Override public void removeBlocksFromWorld(class_1937 world, class_2338 offset) { }
    @Override public void addBlocksToWorld(class_1937 world, StructureTransform transform) { }
}
