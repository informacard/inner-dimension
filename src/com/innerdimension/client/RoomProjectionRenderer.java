package com.innerdimension.client;

import com.innerdimension.projection.RoomProjectionEntity;
import com.innerdimension.projection.RoomProjectionContraption;
import com.innerdimension.projection.ProjectionAnimation;
import com.innerdimension.projection.builtin.Contraption;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import java.util.*;
import com.innerdimension.projection.builtin.SuperByteBuffer;
import com.innerdimension.projection.builtin.ClientContraption;
import com.innerdimension.projection.builtin.ContraptionEntityRenderer;
import com.innerdimension.projection.builtin.OrientedContraptionEntityRenderer;
import net.minecraft.class_765;
import net.minecraft.class_4587;
import net.minecraft.class_5617;
import org.joml.Matrix4f;

public final class RoomProjectionRenderer extends OrientedContraptionEntityRenderer<RoomProjectionEntity,
        OrientedContraptionEntityRenderer.OrientedContraptionState> {
    private static final int MINIMUM_LIGHT = class_765.method_23687(12, 0);
    // Half a milliblock: visually imperceptible, but enough to give a coincident
    // open-domain face deterministic depth priority over the real block below it.
    private static final float OPEN_SURFACE_OFFSET = 0.0005f;
    private static final Map<RoomProjectionEntity, RingClient> CACHES = new IdentityHashMap<>();
    private net.minecraft.class_4604 currentFrustum;
    private static final class EdgeState extends OrientedContraptionState {
        float cx, cz, bottom, top, radius, alpha;
        float scale=1, pivotY;
        boolean shell,open;
        RoomProjectionEntity diagnosticEntity;
    }
    @Override public OrientedContraptionState createRenderState() { return new EdgeState(); }

    @Override public void submit(OrientedContraptionState state, class_4587 matrices,
                                 net.minecraft.class_11659 queue, net.minecraft.class_12075 camera) {
        if (state instanceof EdgeState edge && edge.diagnosticEntity != null) ProjectionDiagnostics.submitted(edge.diagnosticEntity);
        super.submit(state, matrices, queue, camera);
        if (!(state instanceof EdgeState edge) || edge.alpha <= 0 || !edge.open) return;
        // Copy values: queued submissions must not retain a mutable entity/render state.
        float x=edge.cx, z=edge.cz, y=edge.bottom, top=edge.top, r=edge.radius, a=edge.alpha;
        boolean open=edge.open;
        queue.method_73483(matrices, net.minecraft.class_12249.method_76023(), (entry, vertices) -> {
            if(open){
                for(int i=0;i<96;i++){
                    double first=i*Math.PI/48,second=(i+1)*Math.PI/48;
                    float x1=x+r*(float)Math.cos(first),z1=z+r*(float)Math.sin(first);
                    float x2=x+r*(float)Math.cos(second),z2=z+r*(float)Math.sin(second);
                    quad(entry,vertices,x1,y,z1,x2,y,z2,x2,y+.12f,z2,x1,y+.12f,z1,a);
                }
                return;
            }
            for (int side=0;side<4;side++) {
                float x1=side==0||side==2 ? x-r:x+r;
                float z1=side==0||side==3 ? z-r:z+r;
                float x2=side==0||side==3 ? x+r:x-r;
                float z2=side==0||side==2 ? z-r:z+r;
                quad(entry, vertices, x1,y,z1,x2,y,z2,x2,y+.09f,z2,x1,y+.09f,z1,a);
                quad(entry, vertices, x1,y,z1,x2,y,z2,x2,top,z2,x1,top,z1,a*.12f);
                quad(entry, vertices, x1,top-.06f,z1,x2,top-.06f,z2,x2,top,z2,x1,top,z1,a*.6f);
            }
        });
    }
    private static void quad(class_4587.class_4665 matrix, net.minecraft.class_4588 out,
                             float x1,float y1,float z1,float x2,float y2,float z2,
                             float x3,float y3,float z3,float x4,float y4,float z4,float alpha) {
        out.method_22918(matrix.method_23761(),x1,y1,z1).method_22915(217/255f,173/255f,225/255f,alpha);
        out.method_22918(matrix.method_23761(),x2,y2,z2).method_22915(217/255f,173/255f,225/255f,alpha);
        out.method_22918(matrix.method_23761(),x3,y3,z3).method_22915(217/255f,173/255f,225/255f,alpha);
        out.method_22918(matrix.method_23761(),x4,y4,z4).method_22915(217/255f,173/255f,225/255f,alpha);
    }

    /** One full virtual world for lighting/occlusion, independent cached square rings for meshes. */
    private static final class RingClient extends ClientContraption {
        final RoomProjectionEntity entity;
        final List<List<class_2338>> rings = new ArrayList<>();
        final List<Contraption> keys = new ArrayList<>();
        final Map<Integer, ProjectionFluidMesh> fluids = new HashMap<>();
        Set<net.minecraft.class_1058> animatedSprites = Set.of();
        int drawing = -1;
        boolean incremental;
        boolean expansionMesh;
        boolean neighboursReady;
        final Map<class_2338,Section> sections=new LinkedHashMap<>();
        Section drawingSection;
        boolean stableBatches;
        double visible;
        private static final class Section {
            final Set<class_2338> positions=new LinkedHashSet<>();
            final Contraption key=new RoomProjectionContraption();
            ProjectionFluidMesh fluid;
            net.minecraft.class_238 bounds;
            boolean dirty=true;
            int vertices;
        }
        static class_2338 sectionKey(class_2338 pos) {
            return new class_2338(Math.floorDiv(pos.method_10263(),16),Math.floorDiv(pos.method_10264(),16),Math.floorDiv(pos.method_10260(),16));
        }
        void rebuildSections() {
            releaseSections();sections.clear();
            for(var pos:getContraption().getBlocks().keySet())sections.computeIfAbsent(sectionKey(pos),key->new Section()).positions.add(pos);
        }
        void dirtySection(Section section) {
            for(var layer:net.minecraft.class_11515.values())com.innerdimension.projection.builtin.SuperByteBufferCache.getInstance().invalidate(
                    ContraptionEntityRenderer.CONTRAPTION,org.apache.commons.lang3.tuple.Pair.of(section.key,layer));
            if(section.fluid!=null){section.fluid.release();section.fluid=null;}
            section.bounds=null;section.dirty=true;
        }
        void releaseSections(){if(sections!=null)sections.values().forEach(this::dirtySection);}
        void updateSections(class_2338 pos,boolean occupied,Set<Section> dirty) {
            var key=sectionKey(pos);
            var section=sections.computeIfAbsent(key,ignored->new Section());
            if(occupied)section.positions.add(pos);else section.positions.remove(pos);
            // Light propagates up to fifteen cells; include neighbour faces and all three axes.
            int x=pos.method_10263(),y=pos.method_10264(),z=pos.method_10260();
            for(int sx=Math.floorDiv(x-16,16);sx<=Math.floorDiv(x+16,16);sx++)
                for(int sy=Math.floorDiv(y-16,16);sy<=Math.floorDiv(y+16,16);sy++)
                    for(int sz=Math.floorDiv(z-16,16);sz<=Math.floorDiv(z+16,16);sz++){
                        var neighbour=sections.get(new class_2338(sx,sy,sz));if(neighbour!=null)dirty.add(neighbour);
                    }
        }
        List<ContraptionEntityRenderer.ContraptionBlockLayer> sectionLayers(Section section,Matrix4f transform) {
            drawingSection=section;
            try {
                var layers=new ArrayList<ContraptionEntityRenderer.ContraptionBlockLayer>();
                var blocks=ContraptionEntityRenderer.createLayers(section.key,this,getRenderLevel(),entity.method_73183(),transform,OPEN_SURFACE_OFFSET);
                if(blocks!=null)layers.addAll(blocks);
                if(section.fluid==null)section.fluid=new ProjectionFluidMesh(getRenderLevel(),section.positions);
                section.fluid.append(layers,entity.method_73183(),transform,OPEN_SURFACE_OFFSET);
                if(section.dirty){
                    net.minecraft.class_238 bounds=null;
                    section.vertices=0;
                    for(var layer:layers)section.vertices+=layer.buffer().vertexCount();
                    for(var layer:layers)if(layer.buffer().bounds()!=null)bounds=bounds==null?layer.buffer().bounds():bounds.method_991(layer.buffer().bounds());
                    section.bounds=bounds==null?null:bounds.method_1014(OPEN_SURFACE_OFFSET+.001);
                    section.dirty=false;
                }
                return layers;
            } finally {drawingSection=null;}
        }
        void appendSections(List<ContraptionEntityRenderer.ContraptionBlockLayer> output,Matrix4f transform,net.minecraft.class_4604 frustum) {
            for(var section:sections.values()) {
                if(section.positions.isEmpty())continue;
                // Build once before culling: exact mesh bounds also cover models extending out of a cell.
                var built=section.dirty?sectionLayers(section,transform):null;
                if(section.bounds==null)continue;
                var worldBounds=section.bounds.method_997(new net.minecraft.class_243(transform.m30(),transform.m31(),transform.m32()));
                if(frustum!=null&&!frustum.method_23093(worldBounds))continue;
                output.addAll(built==null?sectionLayers(section,transform):built);
            }
        }
        void stableBatches(boolean enabled) {
            if(stableBatches==enabled)return;
            stableBatches=enabled;
            if(enabled){
                for(int i=0;i<keys.size();i++)dirtyRing(i);
            }
        }
        RingClient(Contraption source) {
            super(source);
            entity = (RoomProjectionEntity)source.entity;
            var spec=((RoomProjectionContraption)source).sourceSpec();
            int count = (int)Math.ceil(entity.spatialExtent() * 2 * (spec.shell()==null&&spec.mirror()!=null?Math.sqrt(3):1)) + 1;
            for (int i=0;i<count;i++) {
                rings.add(new ArrayList<>());
                keys.add(new RoomProjectionContraption());
            }
            for (class_2338 pos : source.getBlocks().keySet()) rings.get(ring(pos)).add(pos);
            rebuildSections();
            refreshAnimatedSprites();
            for(var be:renderedBlockEntityView) animate(be);
        }
        void refreshAnimatedSprites() {
            animatedSprites=ProjectionAnimatedSprites.collectStates(getContraption().getBlocks().values().stream()
                    .map(info -> info.comp_1342()).toList());
        }
        int ring(class_2338 pos) {
            var s = ((RoomProjectionContraption)getContraption()).sourceSpec();
            // Farthest-corner reveal, capped at the outer sphere so boundary cells never pop on phase changes.
            if(s.shell()==null&&s.mirror()!=null)return Math.min(s.source().maxX()+1-s.source().minX(),(int)Math.ceil(com.innerdimension.projection.OpenDomainRules.farthest(pos.method_10263()-s.anchorX(),pos.method_10264()-s.anchorY(),pos.method_10260()-s.anchorZ())*2));
            return ProjectionAnimation.spatialRing(pos.method_10263(),pos.method_10264(),pos.method_10260(),
                    s.anchorX(),s.anchorY(),s.anchorZ());
        }
        @Override public RenderedBlocks getRenderedBlocks() {
            var full = super.getRenderedBlocks();
            if (entity.isShell()) return full;
            if(drawingSection!=null)return new RenderedBlocks(full.lookup(),drawingSection.positions);
            return new RenderedBlocks(full.lookup(), drawing < 0 ? List.of() : rings.get(drawing));
        }
        @Override public BitSet getAndAdjustShouldRenderBlockEntities() {
            BitSet enabled = (BitSet)super.getAndAdjustShouldRenderBlockEntities().clone();
            for (int i=0;i<renderedBlockEntityView.size();i++) {
                if (!entity.isShell() && ring(renderedBlockEntityView.get(i).method_11016()) > visible) enabled.clear(i);
            }
            return enabled;
        }
        void release() {
            invalidateStructure();
            getContraption().clientContraption.set(null);
        }
        @Override public void invalidateStructure() {
            if (incremental) return;
            releaseSections();
            super.invalidateStructure();
            // Also discard ring meshes after a resource/virtual-world invalidation.
            if (fluids != null) { fluids.values().forEach(ProjectionFluidMesh::release); fluids.clear(); }
            if (keys == null) return;
            for (Contraption key : keys) for (var layer : net.minecraft.class_11515.values())
                com.innerdimension.projection.builtin.SuperByteBufferCache.getInstance().invalidate(
                        ContraptionEntityRenderer.CONTRAPTION, org.apache.commons.lang3.tuple.Pair.of(key, layer));
        }

        void dirtyRing(int ring) {
            if (ring < 0 || ring >= keys.size()) return;
            ProjectionFluidMesh fluid=fluids.remove(ring);
            if(fluid!=null) fluid.release();
            for(var layer:net.minecraft.class_11515.values())
                com.innerdimension.projection.builtin.SuperByteBufferCache.getInstance().invalidate(
                    ContraptionEntityRenderer.CONTRAPTION,org.apache.commons.lang3.tuple.Pair.of(keys.get(ring),layer));
        }
        void neighbours() {
            if(neighboursReady) return;
            // Open/simple domains have one visible snapshot and an empty bookkeeping half.
            // Closed rooms alone need a second snapshot's occlusion and emitted light.
            if(!((RoomProjectionContraption)getContraption()).sourceSpec().sharesBoundary()){
                neighboursReady=true;return;
            }
            var sibling=com.innerdimension.projection.ProjectionIndex.half(entity.method_73183(),entity.owner(),!entity.isShell());
            if(sibling==null || sibling.getContraption()==null) return;
            incremental=true;
            try {
                for(var info:sibling.getContraption().getBlocks().values())
                    getRenderLevel().method_8652(info.comp_1341(),info.comp_1342(),0);
                getRenderLevel().runLightEngine();
            } finally { incremental=false; }
            neighboursReady=true;
            invalidateStructure();
        }
        void apply(java.util.List<com.innerdimension.projection.ProjectionUpdatePayload.Entry> entries,boolean full) {
            if(full) {
                rings.forEach(List::clear);
                for(var pos:getContraption().getBlocks().keySet()) rings.get(ring(pos)).add(pos);
                rebuildSections();
                resetRenderLevel();
                neighboursReady=false; neighbours();
                refreshAnimatedSprites();
                for(var be:renderedBlockEntityView) animate(be);
                return;
            }
            var world=getRenderLevel();
            var rendered=this.renderedBlockEntities;
            java.util.Set<Integer> dirty=new java.util.HashSet<>();
            Set<Section> dirtySections=new HashSet<>();
            boolean lightDirty=false;
            incremental=true;
            try {
                for(var entry:entries) {
                    var pos=entry.pos(); var old=world.method_8320(pos);
                    var state=net.minecraft.class_2248.method_9531(entry.stateId());
                    var be=world.method_8321(pos);
                    boolean changed=old!=state;
                    if(changed) {
                        updateSections(pos,!state.method_26215(),dirtySections);
                        lightDirty=true;
                        int index=ring(pos); rings.get(index).remove(pos);
                        if(!state.method_26215()) rings.get(index).add(pos);
                        world.method_8652(pos,state,0);
                        if(be!=null) { rendered.remove(be); world.method_8544(pos); be=null; }
                        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) dirty.add(ring(pos.method_10069(dx,0,dz)));
                        if(old.method_26213()!=state.method_26213()) for(int dx=-15;dx<=15;dx++) for(int dz=-15;dz<=15;dz++)
                            dirty.add(ring(pos.method_10069(dx,0,dz)));
                    }
                    if(state.method_31709()) {
                        var info=getContraption().getBlocks().get(pos);
                        if(be==null) {
                            be=readBlockEntity(world,info,false);
                            if(be!=null) { world.method_8438(be); rendered.add(be); }
                        } else if(entry.data()!=null) {
                            be.method_58690(net.minecraft.class_11352.method_71417(net.minecraft.class_8942.field_60348,
                                world.method_30349(),entry.data()));
                            be.method_31664(state);
                        }
                        if(be!=null) animate(be);
                    }
                }
                if(lightDirty) world.runLightEngine();
            } finally { incremental=false; }
            for(int ring:dirty) dirtyRing(ring);
            dirtySections.forEach(this::dirtySection);
            if(lightDirty) refreshAnimatedSprites();
            shouldRenderBlockEntities.clear(); shouldRenderBlockEntities.set(0,rendered.size());
            scratchErroredBlockEntities.clear();
            invalidateChildren();
        }
        void animate(net.minecraft.class_2586 be) {
            var info=getContraption().getBlocks().get(be.method_11016());
            var data=info==null?null:info.comp_1343();
            if(data==null) return;
            if(be instanceof net.minecraft.class_2595 || be instanceof net.minecraft.class_2611)
                be.method_11004(1,data.method_10550("ProjectionOpeners").orElse(0));
            if(be instanceof net.minecraft.class_2627) {
                var shulker=(com.innerdimension.mixin.ProjectionShulkerAccessor)be;
                shulker.inner$progress(data.method_10583("ProjectionProgress").orElse(0f));
                shulker.inner$previousProgress(data.method_10583("ProjectionPreviousProgress").orElse(0f));
            }
            if(be instanceof net.minecraft.class_2669) {
                var piston=(com.innerdimension.mixin.ProjectionPistonAccessor)be;
                piston.inner$progress(data.method_10583("progress").orElse(0f));
                piston.inner$previousProgress(data.method_10583("ProjectionPreviousProgress").orElse(0f));
            }
        }
    }

    @Override protected ClientContraption createClientContraption(Contraption contraption) {
        RingClient client = new RingClient(contraption);
        RingClient previous=CACHES.put(client.entity, client);
        if (previous!=null) previous.release();
        return client;
    }

    public static void clearCaches() {
        CACHES.values().forEach(RingClient::release);CACHES.clear();
        com.innerdimension.projection.builtin.SuperByteBufferCache.getInstance().clear();
    }
    public static void tickEffects(class_310 minecraft) {
        CACHES.entrySet().removeIf(entry -> {
            var entity = entry.getKey();
            if (entity.method_31481() || entity.method_73183() != minecraft.field_1687) {
                entry.getValue().release();
                return true;
            }
            return false;
        });
        for(var client:CACHES.values()) {
            client.neighbours();
            ProjectionAnimatedSprites.mark(client.animatedSprites);
            if(client.entity.phase()==ProjectionAnimation.RETRACTING) continue;
            for(var be:client.renderedBlockEntityView) {
                if(be instanceof net.minecraft.class_2595 chest)
                    net.minecraft.class_2595.method_31670(client.getRenderLevel(),be.method_11016(),be.method_11010(),chest);
                else if(be instanceof net.minecraft.class_2611 chest)
                    net.minecraft.class_2611.method_31689(client.getRenderLevel(),be.method_11016(),be.method_11010(),chest);
            }
        }
    }

    public static void applyUpdates(RoomProjectionEntity entity,
            java.util.List<com.innerdimension.projection.ProjectionUpdatePayload.Entry> entries,boolean full) {
        RingClient client=CACHES.get(entity);
        if(client!=null) client.apply(entries,full);
        if(entity.isShell()) return;
        if(!((RoomProjectionContraption)entity.getContraption()).sourceSpec().sharesBoundary())return;
        // Internal boundary neighbours affect the shell's cached occlusion/light, but its blocks stay static.
        var sibling=com.innerdimension.projection.ProjectionIndex.active(entity.method_73183(),entity.owner());
        RingClient shell=CACHES.get(sibling);
        boolean dirtyShell=full;
        if(shell!=null) {
            boolean lightDirty=full;
            shell.incremental=true;
            try {
                if(full) {
                    var volume=((RoomProjectionContraption)entity.getContraption()).sourceSpec().source();
                    for(int y=volume.minY()+1;y<volume.maxY();y++) for(int x=volume.minX()+1;x<volume.maxX();x++)
                        for(int z=volume.minZ()+1;z<volume.maxZ();z++) shell.getRenderLevel().method_8652(
                            new class_2338(x,y,z),net.minecraft.class_2246.field_10124.method_9564(),0);
                }
                for(var entry:entries) {
                    var state=net.minecraft.class_2248.method_9531(entry.stateId());
                    var old=shell.getRenderLevel().method_8320(entry.pos());
                    lightDirty |= old!=state;
                    var s=((RoomProjectionContraption)entity.getContraption()).sourceSpec().source(); var p=entry.pos();
                    dirtyShell |= old!=state && (old.method_26213()!=state.method_26213()
                        || p.method_10263()==s.minX()+1 || p.method_10263()==s.maxX()-1
                        || p.method_10260()==s.minZ()+1 || p.method_10260()==s.maxZ()-1
                        || p.method_10264()==s.minY()+1 || p.method_10264()==s.maxY()-1);
                    shell.getRenderLevel().method_8652(entry.pos(),state,0);
                }
                if(lightDirty) shell.getRenderLevel().runLightEngine();
            } finally { shell.incremental=false; }
        }
        if(shell!=null && dirtyShell) shell.invalidateStructure();
    }

    public RoomProjectionRenderer(class_5617.class_5618 context) { super(context); }

    @Override public boolean shouldRender(RoomProjectionEntity entity, net.minecraft.class_4604 frustum,
                                          double cameraX, double cameraY, double cameraZ) {
        currentFrustum=frustum;
        boolean accepted = super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
        if (com.innerdimension.config.InnerConfig.get().projectionDebug)
            ProjectionDiagnostics.visibility(entity, accepted, entity.method_5727(cameraX, cameraY, cameraZ),
                    frustum.method_23093(entity.method_5829().method_1014(.5)));
        return accepted;
    }

    @Override public void extractRenderState(RoomProjectionEntity entity, OrientedContraptionState state,
                                            float partialTicks) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        if (state instanceof EdgeState edge) {
            edge.diagnosticEntity = entity;
            edge.alpha = 0;
            edge.open=false;
            edge.shell=entity.isShell();
            edge.scale=edge.shell ? (float)entity.shellScale(partialTicks):1;
            if (entity.getContraption() != null) {
                var s = ((RoomProjectionContraption)entity.getContraption()).sourceSpec();
                edge.open=s.shell()==null&&s.mirror()!=null;
                double elapsed = entity.elapsed(partialTicks);
                edge.cx=(float)s.anchorX(); edge.cz=(float)s.anchorZ();
                edge.pivotY=(float)s.anchorY();
                edge.bottom=(float)s.anchorY()+.03f;
                edge.top=edge.pivotY+((float)entity.getContraption().bounds.field_1325-edge.pivotY)*(float)entity.shellScale(partialTicks);
                edge.radius=(float)((edge.shell ? entity.shellScale(partialTicks):entity.progress(partialTicks))*(edge.open?entity.spatialExtent():entity.extent()));
                if (edge.shell && entity.phase()!=ProjectionAnimation.STABLE) {
                    edge.alpha=.7f;
                    if (entity.phase()==ProjectionAnimation.INTERIOR) edge.alpha=(float)Math.max(0,1-elapsed/ProjectionAnimation.FLASH_TICKS);
                    if (entity.phase()==ProjectionAnimation.RETRACTING) edge.alpha=(float)Math.min(.7,Math.max(0,(entity.closeTicks()-elapsed)/ProjectionAnimation.FLASH_TICKS));
                }
                if (edge.open && (entity.phase()==ProjectionAnimation.INTERIOR||entity.phase()==ProjectionAnimation.RETRACTING)) edge.alpha=.45f;
            }
        }
        ProjectionDiagnostics.extracted(entity, "extract-start");
        if (entity.getContraption() == null) {
            ProjectionDiagnostics.extracted(entity, "missing-structure");
            super.extractRenderState(entity, state, partialTicks);
            return;
        }
        ClientContraption client = getOrCreateClientContraptionLazy(entity.getContraption());
        RingClient rings = (RingClient)client;
        boolean expanding=entity.broken() || entity.isShell() && (entity.phase()==ProjectionAnimation.EXPANDING || entity.phase()==ProjectionAnimation.RETRACTING);
        if(rings.expansionMesh!=expanding){rings.expansionMesh=expanding;rings.invalidateStructure();}
        var revealSpec=((RoomProjectionContraption)entity.getContraption()).sourceSpec();
        rings.visible = entity.progress(partialTicks) * entity.spatialExtent() * 2;
        state.layers = null;
        state.blockEntity = null;
        state.actors = null;
        // Bake a block-light floor into this projection's cached mesh. Create combines
        // baked light with real-world light by taking the brighter of the two.
        client.getRenderLevel().setExternalLight(MINIMUM_LIGHT);
        super.extractRenderState(entity, state, partialTicks);
        if (state.contraption == null || state.modelEntry == null) {
            ProjectionDiagnostics.extracted(entity, "missing-render-state");
            return;
        }
        // This custom entity has no Flywheel visual factory. Render the standard mesh
        // even when Flywheel is available, so lighting also works without a shader pack.
        {
            client.getRenderLevel().setExternalLight(MINIMUM_LIGHT);
            var layers = new ArrayList<ContraptionEntityRenderer.ContraptionBlockLayer>();
            Matrix4f transform = new Matrix4f().translation((float)state.field_53325,
                    (float)state.field_53326, (float)state.field_53327);
            if (entity.isShell()) {
                transform.mul(state.modelEntry.method_23761());
                var shellLayers=ContraptionEntityRenderer.createLayers(entity.getContraption(),client,
                        client.getRenderLevel(),entity.method_73183(),transform);
                if (shellLayers!=null) layers.addAll(shellLayers);
                rings.fluids.computeIfAbsent(-1, key -> new ProjectionFluidMesh(client.getRenderLevel(), entity.getContraption().getBlocks().keySet())).append(layers, entity.method_73183(), transform, 0);
            }
            boolean batched=!entity.isShell()&&revealSpec.shell()==null&&revealSpec.mirror()!=null
                    &&entity.phase()==ProjectionAnimation.STABLE&&!entity.broken();
            boolean firstStableBatch=batched&&!rings.stableBatches;
            rings.stableBatches(batched);
            if(batched)rings.appendSections(layers,transform,currentFrustum);
            if(firstStableBatch)org.slf4j.LoggerFactory.getLogger("inner_dimension.projection").info(
                    "Open-domain render mesh: blocks={} regions={} vertices={} visibleVertices={} layers={}",
                    entity.getContraption().getBlocks().size(),rings.sections.size(),rings.sections.values().stream().mapToInt(s->s.vertices).sum(),
                    layers.stream().mapToInt(l->l.buffer().vertexCount()).sum(),layers.size());
            for (int i=0;!batched && !entity.isShell() && i<rings.rings.size() && i<=rings.visible;i++) {
                if (rings.rings.get(i).isEmpty()) continue;
                rings.drawing = i;
                var layer = ContraptionEntityRenderer.createLayers(rings.keys.get(i), client,
                        client.getRenderLevel(), entity.method_73183(), transform, OPEN_SURFACE_OFFSET);
                if (layer != null) layers.addAll(layer);
                final int ringIndex = i;
                rings.fluids.computeIfAbsent(i, key -> new ProjectionFluidMesh(client.getRenderLevel(), rings.rings.get(ringIndex))).append(layers, entity.method_73183(), transform, OPEN_SURFACE_OFFSET);
            }
            rings.drawing = -1;
            var fracture=entity.breakFrame(partialTicks);
            if(fracture!=null) {
                var animated=new ArrayList<ContraptionEntityRenderer.ContraptionBlockLayer>();
                for(var layer:layers)animated.add(new ContraptionEntityRenderer.ContraptionBlockLayer(layer.renderLayer(),layer.buffer(),layer.world(),layer.lightTransform(),layer.surfaceOffset(),fracture));
                layers=animated;
                if(state.blockEntity!=null) {
                    var be=state.blockEntity;
                    state.blockEntity=new com.innerdimension.projection.builtin.BlockEntityRenderHelper.BlockEntityListRenderState(be.dispatcher(),be.camera(),be.cameraPos(),be.states(),fracture);
                }
            }
            var lightWorld=entity.method_73183();
            var frameLights=new com.innerdimension.projection.builtin.FrameLightCache((x,y,z)->net.minecraft.class_761.method_23794(lightWorld,new class_2338(x,y,z)));
            var litLayers=new ArrayList<ContraptionEntityRenderer.ContraptionBlockLayer>(layers.size());
            for(var layer:layers)litLayers.add(new ContraptionEntityRenderer.ContraptionBlockLayer(layer.renderLayer(),layer.buffer(),layer.world(),layer.lightTransform(),layer.surfaceOffset(),layer.fracture(),frameLights));
            state.layers = litLayers;
            ProjectionDiagnostics.extracted(entity, "layers=" + layers.size() + ",visibleRing=" + rings.visible);
        }
        // Create temporarily resets virtual-world light for each block entity. Raise
        // their extracted light separately, keeping brighter real/source light intact.
        if (state.blockEntity != null) {
            for (var blockEntityState : state.blockEntity.states()) {
                blockEntityState.field_62676 = SuperByteBuffer.maxLight(
                        blockEntityState.field_62676, MINIMUM_LIGHT);
            }
        }
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.RENDER_EXTRACT,auditStart); }
    }

    @Override public void transform(OrientedContraptionState state, class_4587 matrices) {
        if (state instanceof EdgeState edge && edge.shell) {
            matrices.method_46416(edge.cx,edge.pivotY,edge.cz);
            matrices.method_22905(edge.scale,edge.scale,edge.scale);
            matrices.method_46416(-edge.cx,-edge.pivotY,-edge.cz);
        }
    }
}
