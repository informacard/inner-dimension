package com.innerdimension.projection;

import com.innerdimension.projection.builtin.OrientedContraptionEntity;
import net.minecraft.class_1268;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_11368;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import java.util.List;
import java.util.UUID;

/** Stationary, non-saving Create entity; removal only discards the snapshot. */
public final class RoomProjectionEntity extends OrientedContraptionEntity {
    private static final net.minecraft.class_2940<Boolean> REVERSED_SHELL=net.minecraft.class_2945.method_12791(RoomProjectionEntity.class,net.minecraft.class_2943.field_13323);
    public boolean reversedShell(){return field_6011.method_12789(REVERSED_SHELL);}
    public void reversedShell(boolean value){field_6011.method_12778(REVERSED_SHELL,value);}
    private static final net.minecraft.class_2940<Integer> MAXIMUM_TICKS=net.minecraft.class_2945.method_12791(RoomProjectionEntity.class,net.minecraft.class_2943.field_13327);
    public int maximumTicks(){return field_6011.method_12789(MAXIMUM_TICKS);}
    private static final net.minecraft.class_2940<Integer> EXPANSION_SLOT=net.minecraft.class_2945.method_12791(RoomProjectionEntity.class,net.minecraft.class_2943.field_13327);
    public int expansionSlot(){return Math.max(0,field_6011.method_12789(EXPANSION_SLOT));}
    public void expansionSlot(int slot){field_6011.method_12778(EXPANSION_SLOT,slot);}
    private static final net.minecraft.class_2940<Boolean> BROKEN = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13323);
    public boolean broken(){return field_6011.method_12789(BROKEN);}
    public ProjectionAnimation.BreakFrame breakFrame(float partial){return broken()?new ProjectionAnimation.BreakFrame(elapsed(partial),owner()==null?0:owner().getLeastSignificantBits(),isShell()):null;}
    public void breakApart(){field_6011.method_12778(BROKEN,true);retract();}
    private static final net.minecraft.class_2940<Boolean> CONTEST_GHOST = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13323);
    private static final net.minecraft.class_2940<Boolean> OPEN_COLLISION_READY = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13323);
    public boolean contestGhost(){return field_6011.method_12789(CONTEST_GHOST);}
    public void contestGhost(boolean value) { field_6011.method_12778(CONTEST_GHOST,value); }
    public void presentation(String name,int color) {
        field_6011.method_12778(DOMAIN_NAME,name);
        field_6011.method_12778(NAME_LEFT,color);field_6011.method_12778(NAME_CENTER,color);field_6011.method_12778(NAME_RIGHT,color);
    }
    private ProjectionMirror mirror;
    public ProjectionMirror mirror() { return mirror; }
    public void mirror(ProjectionMirror value) { mirror = value; }
    private static final net.minecraft.class_2940<Long> STABLE_START = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_39965);
    private static final net.minecraft.class_2940<String> DOMAIN_NAME = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13326);
    private static final net.minecraft.class_2940<Integer> NAME_LEFT = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Integer> NAME_CENTER = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Integer> NAME_RIGHT = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Integer> NAME_SPLIT_LEFT = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Integer> NAME_SPLIT_RIGHT = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Integer> PHASE = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Long> START = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_39965);
    private static final net.minecraft.class_2940<Float> FROM = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Float> SHELL_FROM = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Boolean> SHELL = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13323);
    private static final net.minecraft.class_2940<Integer> HEART = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<String> OWNER = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13326);
    private static final net.minecraft.class_2940<Integer> SHELL_TICKS = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Integer> INTERIOR_TICKS = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13327);
    private static final net.minecraft.class_2940<Float> CREST_D0 = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Float> CREST_D1 = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Float> CREST_D2 = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Float> CREST_S0 = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Float> CREST_S1 = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    private static final net.minecraft.class_2940<Float> CREST_S2 = net.minecraft.class_2945.method_12791(RoomProjectionEntity.class, net.minecraft.class_2943.field_13320);
    @Override protected void method_5693(net.minecraft.class_2945.class_9222 builder) {
        super.method_5693(builder);
        builder.method_56912(MAXIMUM_TICKS,DomainLifetime.MAX_TICKS);
        builder.method_56912(REVERSED_SHELL,false);
        builder.method_56912(EXPANSION_SLOT,-1);
        builder.method_56912(BROKEN,false);
        builder.method_56912(CONTEST_GHOST,false);
        builder.method_56912(OPEN_COLLISION_READY,false);
        builder.method_56912(STABLE_START, 0L);
        builder.method_56912(DOMAIN_NAME, "");
        builder.method_56912(NAME_LEFT,0xffffff);builder.method_56912(NAME_CENTER,0xffffff);builder.method_56912(NAME_RIGHT,0xffffff);
        builder.method_56912(NAME_SPLIT_LEFT,333);builder.method_56912(NAME_SPLIT_RIGHT,667);
        builder.method_56912(PHASE, ProjectionAnimation.EXPANDING);
        builder.method_56912(START, 0L);
        builder.method_56912(FROM, 1f);
        builder.method_56912(SHELL_FROM, 1f);
        builder.method_56912(SHELL, false);
        builder.method_56912(HEART, 0);
        builder.method_56912(OWNER, "");
        builder.method_56912(SHELL_TICKS, ProjectionAnimation.DEFAULT_SHELL_TICKS);
        builder.method_56912(INTERIOR_TICKS, ProjectionAnimation.DEFAULT_INTERIOR_TICKS);
        builder.method_56912(CREST_D0, 0f); builder.method_56912(CREST_D1, .5f); builder.method_56912(CREST_D2, .3f);
        builder.method_56912(CREST_S0, .3f); builder.method_56912(CREST_S1, .6f); builder.method_56912(CREST_S2, .55f);
    }
    public int phase() { return field_6011.method_12789(PHASE); }
    public String domainName() { return field_6011.method_12789(DOMAIN_NAME); }
    /** True when the loadout being expanded carries the chicken trait, i.e. the ♡ subtitle. */
    public int heart() { return field_6011.method_12789(HEART); }
    public int nameColor(int i){return field_6011.method_12789(i==0?NAME_LEFT:i==1?NAME_CENTER:NAME_RIGHT);}
    /** Per-mille position of a colour transition in the name; see {@code nameSplit}. */
    public int nameSplit(int i){return field_6011.method_12789(i==0?NAME_SPLIT_LEFT:NAME_SPLIT_RIGHT);}
    public double elapsed(float partial) { return method_73183().method_75260() - field_6011.method_12789(START) + partial; }
    public boolean open(){if(getContraption()==null)return false;var s=((RoomProjectionContraption)getContraption()).sourceSpec();return s.shell()==null&&s.mirror()!=null;}
    public double progress(float partial) { if(open())return OpenDomainRules.progress(phase(),elapsed(partial),field_6011.method_12789(FROM),interiorTicks()); return broken() && phase()==ProjectionAnimation.RETRACTING ? field_6011.method_12789(FROM) : ProjectionAnimation.progress(phase(), elapsed(partial), field_6011.method_12789(FROM), interiorTicks()); }
    public boolean isShell() { return field_6011.method_12789(SHELL); }
    public double shellScale(float partial) { return broken() && phase()==ProjectionAnimation.RETRACTING ? field_6011.method_12789(SHELL_FROM) : ProjectionAnimation.shellScale(phase(),elapsed(partial),field_6011.method_12789(SHELL_FROM),field_6011.method_12789(FROM),shellTicks()); }
    public int shellTicks() { return Math.max(1,field_6011.method_12789(SHELL_TICKS)); }
    public int interiorTicks() { return Math.max(1,field_6011.method_12789(INTERIOR_TICKS)); }
    public UUID owner() { try { return UUID.fromString(field_6011.method_12789(OWNER)); } catch (RuntimeException ignored) { return null; } }
    public float crestDistance(int stage) { return field_6011.method_12789(stage==0?CREST_D0:stage==1?CREST_D1:CREST_D2); }
    public float crestScale(int stage) { return field_6011.method_12789(stage==0?CREST_S0:stage==1?CREST_S1:CREST_S2); }
    public int closeTicks() { if(open())return OpenDomainRules.RETRACTION_TICKS; return broken()?ProjectionAnimation.BREAK_TICKS:ProjectionAnimation.closeTicks(field_6011.method_12789(FROM)); }
    public void enableOpenCollision(){field_6011.method_12778(OPEN_COLLISION_READY,true);}
    @Override public boolean collisionEnabled() {
        return (ProjectionAnimation.collision(phase(),isShell())
                || !isShell()&&phase()==ProjectionAnimation.INTERIOR&&field_6011.method_12789(OPEN_COLLISION_READY))&&!method_31747();
    }
    public void beginInterior() {
        field_6011.method_12778(START, method_73183().method_75260());
        field_6011.method_12778(PHASE, ProjectionAnimation.INTERIOR);
    }
    public double subtitleReleaseElapsed(float partial) { return method_73183().method_75260() - field_6011.method_12789(STABLE_START) + partial; }
    public void stabilize() {
        field_6011.method_12778(STABLE_START, method_73183().method_75260());
        field_6011.method_12778(PHASE, ProjectionAnimation.STABLE);
    }
    public void retract() {
        if (phase() == ProjectionAnimation.RETRACTING) return;
        field_6011.method_12778(SHELL_FROM, (float)shellScale(0));
        field_6011.method_12778(FROM, (float)progress(0));
        field_6011.method_12778(START, method_73183().method_75260());
        field_6011.method_12778(PHASE, ProjectionAnimation.RETRACTING);
        collidingEntities.clear();
    }
    public double extent() {
        var b = getContraption().bounds;
        var s = ((RoomProjectionContraption)getContraption()).sourceSpec();
        return Math.max(Math.max(Math.abs(b.field_1323-s.anchorX()), Math.abs(b.field_1320-s.anchorX())),
                Math.max(Math.abs(b.field_1321-s.anchorZ()), Math.abs(b.field_1324-s.anchorZ())));
    }
    public double spatialExtent() {
        var b = getContraption().bounds;
        var s = ((RoomProjectionContraption)getContraption()).sourceSpec();
        return Math.max(extent(), Math.max(Math.abs(b.field_1322-s.anchorY()), Math.abs(b.field_1325-s.anchorY())));
    }
    public RoomProjectionEntity(class_1299<? extends RoomProjectionEntity> type, class_1937 world) {
        super(type, world);
        // Never let vanilla or another Create contraption push the snapshot itself.
        // Create's source-side collision dispatcher still supplies our enabled block shapes.
        field_5960 = true;
        setInitialOrientation(class_2350.field_11035);
        startAtInitialYaw();
        ProjectionIndex.track(this);
    }

    public void initialize(RoomProjectionContraption snapshot, class_243 feet, boolean shell, UUID owner) {
        field_6011.method_12778(SHELL, shell);
        field_6011.method_12778(OWNER, owner.toString());
        if (method_73183() instanceof net.minecraft.class_3218 world){
            if(snapshot.sourceSpec().shell()==null&&snapshot.sourceSpec().mirror()!=null)field_6011.method_12778(MAXIMUM_TICKS,OpenDomainBinding.get(world).duration(owner));
            var state=com.innerdimension.progression.DomainState.get(world);
            field_6011.method_12778(DOMAIN_NAME,state.rawName(owner));
            if(field_6011.method_12789(EXPANSION_SLOT)<0)expansionSlot(state.selectedSlot(owner));
            List<String> loadout=state.loadout(owner,expansionSlot());
            int[] colors=com.innerdimension.domain.DomainAbilityCatalog.nameColors(loadout);
            int[] split=com.innerdimension.domain.DomainAbilityCatalog.nameSplit(loadout);
            field_6011.method_12778(NAME_LEFT,colors[0]);
            field_6011.method_12778(NAME_CENTER,colors[1]);
            field_6011.method_12778(NAME_RIGHT,colors[2]);
            field_6011.method_12778(NAME_SPLIT_LEFT,split[0]);
            field_6011.method_12778(NAME_SPLIT_RIGHT,split[1]);
            // The ♡ subtitle is a property of the expansion, decided once here on the server.
            field_6011.method_12778(HEART,com.innerdimension.domain.DomainModes.hearts(loadout));
        }
        var cfg=com.innerdimension.config.InnerConfig.get();
        field_6011.method_12778(SHELL_TICKS,cfg.projectionShellTicks);
        field_6011.method_12778(INTERIOR_TICKS,snapshot.sourceSpec().shell()==null && snapshot.sourceSpec().mirror()!=null ? OpenDomainRules.expansionTicks((snapshot.sourceSpec().source().maxX()+1-snapshot.sourceSpec().source().minX())/2.0) : cfg.projectionInteriorTicks);
        field_6011.method_12778(CREST_D0,(float)cfg.projectionCrestInitialDistance);
        field_6011.method_12778(CREST_D1,(float)cfg.projectionCrestShellDistance);
        field_6011.method_12778(CREST_D2,(float)cfg.projectionCrestInteriorDistance);
        field_6011.method_12778(CREST_S0,(float)cfg.projectionCrestInitialScale);
        field_6011.method_12778(CREST_S1,(float)cfg.projectionCrestShellScale);
        field_6011.method_12778(CREST_S2,(float)cfg.projectionCrestInteriorScale);
        setContraption(snapshot);
        field_6011.method_12778(START, method_73183().method_75260());
        // The full-cube floor occupies local y=-1..0, so its top equals anchor Y.
        // Place that top 0.01 above the player's actual support surface (feet),
        // independent of eye height, pose or scale.
        // This fractional anchor is deliberate: cell-level consumers must map world cells onto
        // the room's local cells themselves (see ProjectionBlocks).
        ProjectionSourceSpec source = snapshot.sourceSpec();
        // Open domains stay exactly on the world XYZ grid. Their overlap is resolved only
        // in the client mesh, per face, so logical positions and opposite sides remain symmetric.
        double verticalBias = source.shell() == null ? 0 : 0.01;
        method_5814(feet.field_1352 - source.anchorX(),
                feet.field_1351 - source.anchorY() + verticalBias,
                feet.field_1350 - source.anchorZ());
    }
    public void initialize(RoomProjectionContraption snapshot, class_243 feet, UUID owner) { initialize(snapshot, feet, false, owner); }

    // OrientedContraptionEntity normally subtracts half a block for minecart mounting.
    @Override public class_243 getAnchorVec() { return method_73189(); }
    @Override public class_243 getPrevAnchorVec() { return getPrevPositionVec(); }
    @Override public float getInitialYaw() { return 0; }
    @Override public float getYawOffset() { return 0; }
    @Override public class_243 applyRotation(class_243 local, float partialTicks) { return local; }
    @Override public class_243 reverseRotation(class_243 local, float partialTicks) { return local; }
    @Override protected void readAdditional(class_11368 view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);
        // The spawn packet sets position before reading the structure. Rebuild the full box now.
        method_5814(method_23317(), method_23318(), method_23321());
    }
    @Override public boolean supportsTerrainCollision() { return false; }
    @Override public boolean method_30949(net.minecraft.class_1297 actor) {
        return !ProjectionEntityPolicy.ignores(actor) && !ProjectionEvacuation.pending(actor,this)
                && super.method_30949(actor);
    }
    @Override public boolean method_5863(){return isShell()&&getContraption() instanceof RoomProjectionContraption snapshot&&snapshot.sourceSpec().shell()!=null&&phase()!=ProjectionAnimation.RETRACTING;}
    @Override public boolean method_64397(net.minecraft.class_3218 world,net.minecraft.class_1282 source,float amount){return RoomProjectionManager.damageShell(this,source,amount);}
    private long diagnosticContraptionTicks;
    public long diagnosticContraptionTicks() { return diagnosticContraptionTicks; }
    /** The full box indexes projected terrain; this stationary carrier has no fluid body.
     * Entity.baseTick dispatches here for both water and lava on client and server.
     * Keeping baseTick preserves bookkeeping without scanning the entire 64^3 domain twice.
     */
    @Override public boolean method_5692(net.minecraft.class_6862<net.minecraft.class_3611> fluidTag,double push) {
        return false;
    }
    @Override public void method_5773() {
        long started=OpenDomainPerformance.begin();
        try {super.method_5773();}
        finally {OpenDomainPerformance.end(OpenDomainPerformance.Metric.CARRIER_TICK,started);}
    }
    @Override protected void tickContraption() {
        diagnosticContraptionTicks++;
        setContraptionMotion(class_243.field_1353);
    }
    @Override public void tickActors() { }
    @Override public void method_5650(net.minecraft.class_1297.class_5529 reason) {
        mirror = null;
        if (getContraption() instanceof RoomProjectionContraption snapshot) ProjectionEnvironment.invalidate(snapshot);
        collidingEntities.clear();
        super.method_5650(reason);
    }
    @Override public void registerColliding(net.minecraft.class_1297 entity) {
        if (collisionEnabled() && !ProjectionEntityPolicy.ignores(entity)) super.registerColliding(entity);
    }
    @Override public class_243 getContactPointMotion(class_243 point) { return class_243.field_1353; }
    @Override public boolean handlePlayerInteraction(class_1657 player, class_2338 pos,
                                                     class_2350 side, class_1268 hand) { return false; }
    @Override public void disassemble() { method_31472(); }
}
