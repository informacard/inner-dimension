package com.innerdimension.projection;

import com.innerdimension.transport.SafeEntityTeleport;
import net.minecraft.*;
import java.util.*;
import static com.innerdimension.projection.ProjectionSafetySearch.*;

/** One-time evacuation at expansion, never changes either world's blocks. */
public final class ProjectionSafety {
    public static final class CasterPlacementException extends IllegalStateException {
        public CasterPlacementException(String message) { super(message); }
    }
    private record Shape(Box box,boolean shell,int sourceX,int sourceY,int sourceZ) { }
    public record Move(class_1297 entity,Point from,Point to) { }
    private ProjectionSafety() { }
    private static boolean contestFollower(class_1297 actor,RoomProjectionEntity shell) {
        var entry=com.innerdimension.domain.DomainContest.entry(actor.method_5667());
        return entry!=null&&entry.shell==shell&&entry.follower();
    }

    /** Only newly occupied volumes trigger a bounded short relocation. Removing support never does. */
    public static void resolveDynamic(RoomProjectionEntity shell, RoomProjectionEntity interior,
                                      List<ProjectionUpdatePayload.Entry> changes) {
        class_3218 world=(class_3218)interior.method_73183();
        var snapshot=(RoomProjectionContraption)interior.getContraption();
        var view=snapshot.snapshotView();
        List<class_238> occupied=new ArrayList<>();
        for(var change:changes) {
            var state=class_2248.method_9531(change.stateId());
            for(var shape:state.method_26194(view,change.pos(),class_3726.method_16194()).method_1090())
                occupied.add(shape.method_997(interior.getAnchorVec().method_1031(change.pos().method_10263(),change.pos().method_10264(),change.pos().method_10260())));
        }
        for(var actor:world.method_8390(class_1297.class,interior.method_5829(),
                e -> !ProjectionEntityPolicy.ignores(e) && !e.method_7325() && e.method_5805())) {
            var body=actor.method_5829().method_1011(1.0E-5);
            if(occupied.stream().noneMatch(b -> b.method_994(body))) continue;
            if (snapshot.sourceSpec().shell()==null) {
                Geometry geometry=new Geometry(snapshot,interior.getAnchorVec());
                Point target=openLanding(snapshot,interior,actor,geometry,List.of());
                if (target!=null && SafeEntityTeleport.prepare(actor)) {
                    Box prepared=box(actor.method_5829());
                    target=openLanding(snapshot,interior,actor,geometry,List.of());
                    if (target!=null) evacuate(new Move(actor,
                            new Point(prepared.x(),prepared.minY(),prepared.z()),target));
                }
                // An ordinary entity with no landing remains in place; open-domain updates
                // never create a shell-escape/deferred-evacuation lifecycle.
                continue;
            }
            boolean moved=false;
            // Try lateral moves before raising anyone; stop after two blocks.
            outer: for(double distance=.25;distance<=2;distance+=.25) {
                var offsets=new ArrayList<class_243>(List.of(new class_243(distance,0,0),new class_243(-distance,0,0),
                        new class_243(0,0,distance),new class_243(0,0,-distance)));
                if(actor instanceof class_1657) offsets.add(new class_243(0,distance,0));
                for(var offset:offsets) {
                    var target=actor.method_5829().method_997(offset);
                    if(ProjectionEnvironment.projectedCollision(world,target) || !world.method_8587(actor,target)) continue;
                    if(!SafeEntityTeleport.prepare(actor)) break outer;
                    // Preparation may restore poses/positions. Revalidate the actual body.
                    target=actor.method_5829().method_997(offset);
                    if(ProjectionEnvironment.projectedCollision(world,target) || !world.method_8587(actor,target)) continue;
                    moved=SafeEntityTeleport.move(actor,world,actor.method_23317()+offset.field_1352,
                            actor.method_23318()+offset.field_1351,actor.method_23321()+offset.field_1350,
                            Set.of(),actor.method_36454(),actor.method_36455(),false);
                    if(moved) break outer;
                }
            }
            if(!moved) ProjectionEvacuation.defer(actor,shell,interior);
        }
    }

    /** Plan against the final shell AND interior before any expansion packet or collision phase. */
    public static List<Move> prepareExpansion(RoomProjectionEntity shell,RoomProjectionEntity interior,
                                               class_1309 caster,Point desired) {
        Set<UUID> prepared=new HashSet<>();
        for(int pass=0;pass<8;pass++) {
            var moves=planExpansion(shell,interior,caster,desired);
            boolean changed=false;
            for(var move:moves)if(prepared.add(move.entity.method_5667())) {
                if(!SafeEntityTeleport.prepare(move.entity))throw new CasterPlacementException("Expansion occupant preparation waits");
                changed=true;
            }
            if(!changed)return moves;
        }
        throw new CasterPlacementException("Expansion occupants did not settle");
    }
    private static List<Move> planExpansion(RoomProjectionEntity shell,RoomProjectionEntity interior,
                                            class_1309 caster,Point desired) {
        var world=(class_3218)shell.method_73183();
        var snapshot=(RoomProjectionContraption)interior.getContraption();
        var volume=snapshot.sourceSpec().source().bounds().method_997(shell.getAnchorVec());
        Geometry geometry=new Geometry((RoomProjectionContraption)shell.getContraption(),shell.getAnchorVec());
        geometry.addAt(snapshot,snapshot.getContraptionWorld(),interior.getAnchorVec());
        for(var other:ProjectionIndex.future(world,volume.method_1014(2))) {
            if(other==shell||other==interior)continue;
            geometry.addAt((RoomProjectionContraption)other.getContraption(),other.getContraption().getContraptionWorld(),other.getAnchorVec());
        }
        var actors=new ArrayList<class_1297>(world.method_8390(class_1297.class,volume.method_1009(0,2,0),
            e->!(e instanceof RoomProjectionEntity)&&!ProjectionEntityPolicy.ignores(e)&&e.method_5805()&&!e.method_7325()
                &&(e instanceof class_1309 || shell.method_30949(e))));
        actors.remove(caster);actors.add(0,caster);
        actors.sort(Comparator.comparingInt(e->e==caster?0:e instanceof class_3222?1:e instanceof class_1309?2:3));
        var reserved=new ArrayList<Box>();var affected=new ArrayList<class_1297>();
        for(var actor:actors) {
            if(actor==caster&&desired!=null||com.innerdimension.domain.SimpleDomain.active(actor)||geometry.collides(box(actor.method_5829())))affected.add(actor);
            else reserved.add(box(actor.method_5829()));
        }
        boolean open=snapshot.sourceSpec().shell()==null;
        Box bounds=geometry.interiorBounds();
        int radius=(int)Math.ceil(Math.max(volume.field_1320-volume.field_1323,volume.field_1324-volume.field_1321));
        var moves=new ArrayList<Move>();
        for(var actor:affected) {
            Box body=box(actor.method_5829());
            Space safe=space(world,actor,geometry,reserved,open?null:bounds);
            double minY=open?body.minY():bounds.minY();
            double maxY=(open?volume.field_1325:bounds.maxY())-(body.maxY()-body.minY())-CLEARANCE;
            Point target=actor==caster&&desired!=null&&safe.clear(body.at(desired))&&safe.supported(body.at(desired))?desired:null;
            if(target==null)target=ProjectionSafetySearch.occupant(body,safe,minY,maxY,radius);
            if(target==null)throw new CasterPlacementException("No safe final expansion position for "+actor.method_5667());
            moves.add(new Move(actor,new Point(actor.method_23317(),actor.method_23318(),actor.method_23321()),target));
            reserved.add(body.at(target));
        }
        return List.copyOf(moves);
    }

    public static List<Move> prepareStage(RoomProjectionEntity shell, RoomProjectionEntity interior, boolean closingShell) {
        // Animation restoration can move other participants as well. Repeat planning until all
        // actors in the current plan have been prepared; never apply a stale pre-restoration plan.
        Set<UUID> prepared=new HashSet<>();
        Set<UUID> blocked=new HashSet<>();
        boolean open=((RoomProjectionContraption)interior.getContraption()).sourceSpec().shell()==null;
        for (int pass=0;pass<8;pass++) {
            List<Move> moves=planStage(shell,interior,closingShell).stream()
                    .filter(m -> !blocked.contains(m.entity.method_5667())).toList();
            boolean changed=false;
            for (Move move:moves) if (prepared.add(move.entity.method_5667())) {
                boolean ready;
                try { ready=SafeEntityTeleport.prepare(move.entity); }
                catch (RuntimeException failure) { ready=false; }
                if (!ready) {
                    if (RoomProjectionManager.active(move.entity.method_5667())==shell && !contestFollower(move.entity,shell))
                        throw new IllegalStateException("Could not prepare domain caster; retry transition");
                    blocked.add(move.entity.method_5667());
                    if (!open) ProjectionEvacuation.defer(move.entity,shell,interior);
                }
                changed=true;
            }
            if (!changed) return moves;
        }
        List<Move> last=planStage(shell,interior,closingShell);
        for (Move move:last) {
            if (RoomProjectionManager.active(move.entity.method_5667())==shell && !contestFollower(move.entity,shell))
                throw new IllegalStateException("Domain caster did not settle; retry transition");
            if (!open) ProjectionEvacuation.defer(move.entity,shell,interior);
        }
        return List.of();
    }

    private static List<Move> planStage(RoomProjectionEntity shell, RoomProjectionEntity interior, boolean closingShell) {
        class_3218 world=(class_3218)shell.method_73183();
        RoomProjectionContraption shellSnapshot=(RoomProjectionContraption)shell.getContraption();
        RoomProjectionContraption insideSnapshot=(RoomProjectionContraption)interior.getContraption();
        if (insideSnapshot.sourceSpec().shell()==null)
            return planOpenStage(shell,interior,insideSnapshot);
        Geometry changed=new Geometry(closingShell ? shellSnapshot:insideSnapshot,shell.getAnchorVec());
        Geometry blockers=new Geometry(shellSnapshot,shell.getAnchorVec());
        blockers.addAt(insideSnapshot,insideSnapshot.getContraptionWorld(),interior.getAnchorVec());
        for(var other:ProjectionIndex.future(world,shell.method_5829().method_1014(2))) {
            if(other==shell||other==interior)continue;
            blockers.addAt((RoomProjectionContraption)other.getContraption(),other.getContraption().getContraptionWorld(),other.getAnchorVec());
        }
        List<class_1297> actors=new ArrayList<>(world.method_8390(class_1297.class,shell.method_5829(),
                e -> !ProjectionEntityPolicy.ignores(e) && e.method_5805() && (!e.method_31747() || contestFollower(e,shell))
                        && (e instanceof class_1309 ? !e.method_7325() : shell.method_30949(e))));
        List<Box> reserved=new ArrayList<>();
        List<class_1297> affected=new ArrayList<>();
        for (class_1297 actor:actors) {
            Box body=box(actor.method_5829());
            if (changed.collides(body)) affected.add(actor);
            else reserved.add(body);
        }
        // Allocate safe landings to people before movable decorative entities.
        affected.sort(Comparator.comparingInt(e -> e instanceof class_3222 ? 0 : e instanceof class_1309 ? 1 : 2));
        // Only actual overlaps are moved, including the caster. People who escaped are absent.
        List<Move> moves=new ArrayList<>();
        Box interiorBounds=blockers.interiorBounds();
        int radius=Math.max(shellSnapshot.sourceSpec().source().maxX()-shellSnapshot.sourceSpec().source().minX(),
                shellSnapshot.sourceSpec().source().maxZ()-shellSnapshot.sourceSpec().source().minZ())+2;
        for (class_1297 actor:affected) {
            try {
            Box body=box(actor.method_5829());
            boolean inFootprint=body.x()>interiorBounds.minX() && body.x()<interiorBounds.maxX()
                    && body.z()>interiorBounds.minZ() && body.z()<interiorBounds.maxZ();
            boolean within=inFootprint && body.minY()<interiorBounds.maxY() && body.maxY()>interiorBounds.minY()-.5;
            Space insideSpace=space(world,actor,blockers,reserved,interiorBounds);
            double maxY=interiorBounds.maxY()-(body.maxY()-body.minY())-CLEARANCE;
            boolean player=actor instanceof class_1657;
            var contest=com.innerdimension.domain.DomainContest.entry(actor.method_5667());
            // Arbitration never weakens player safety.
            // The projected floor is laid a hair above the caster's feet, so an actor standing on
            // that ground always overlaps it. Search a foothold vertically first: the same-height
            // lateral step cannot clear that lip, and pushing the actor out through a side is
            // exactly what must not happen to anyone inside the room. The vertical search stays
            // bounded to one body height so a collision response never becomes a ride upward.
            Point target=null;
            if(player) {
                if(within) target=ProjectionSafetySearch.vertical(body,insideSpace,Math.max(body.minY(),interiorBounds.minY()),maxY);
            } else if(inFootprint) {
                target=ProjectionSafetySearch.foothold(body,insideSpace,interiorBounds.minY(),maxY);
            }
            if(target==null && !player)
                target=ProjectionSafetySearch.horizontal(body,insideSpace,radius,within?interiorBounds:null);
            if(target==null && (player || inFootprint))
                target=inward(body,interiorBounds,insideSpace,radius,interiorBounds.minY(),
                        player ? maxY : ProjectionSafetySearch.reach(body,maxY));
            // Only an actor that is not inside the room may leave through a side. Anyone in the
            // footprint waits instead, so a failed search can never eject it out of the domain.
            if(target==null && (player || !inFootprint)) {
                Space exterior=space(world,actor,blockers,reserved,null);
                target=ProjectionSafetySearch.sideExit(body,box(shell.method_5829()),exterior,radius);
                if(target==null) target=ProjectionSafetySearch.sideAirExit(body,box(shell.method_5829()),exterior,radius);
            }
            if (target==null && (RoomProjectionManager.active(actor.method_5667())!=shell || contestFollower(actor,shell))) {
                ProjectionEvacuation.defer(actor,shell,interior);
                reserved.add(body);
                continue;
            }
            if (target==null) throw new CasterPlacementException("No safe caster position at projection collision transition"
                    + " stage=" + (closingShell ? "shell" : "interior")
                    + " entity=" + actor.method_5667() + " class=" + actor.getClass().getName()
                    + " body=" + body + " within=" + within + " grounded=" + actor.method_24828()
                    + " affected=" + affected.size() + " reserved=" + reserved.size()
                    + " domain=" + interiorBounds);
            moves.add(new Move(actor,new Point(actor.method_23317(),actor.method_23318(),actor.method_23321()),target));
            reserved.add(body.at(target));
            } catch (RuntimeException failure) {
                if (RoomProjectionManager.active(actor.method_5667())==shell && !contestFollower(actor,shell)) throw failure;
                ProjectionEvacuation.defer(actor,shell,interior);
                reserved.add(box(actor.method_5829()));
            }
        }
        return List.copyOf(moves);
    }

    /** Open domains have no inside/outside boundary: each colliding entity searches only from
     * its own feet upward, first in its current column and then horizontally. */
    private static List<Move> planOpenStage(RoomProjectionEntity shell,RoomProjectionEntity interior,
                                            RoomProjectionContraption snapshot) {
        class_3218 world=(class_3218)interior.method_73183();
        Geometry geometry=new Geometry(snapshot,interior.getAnchorVec());
        List<class_1297> actors=new ArrayList<>(world.method_8390(class_1297.class,interior.method_5829(),
                e -> !ProjectionEntityPolicy.ignores(e) && e.method_5805() && !e.method_31747()
                        && (e instanceof class_1309 ? !e.method_7325() : interior.method_30949(e))));
        List<Box> reserved=new ArrayList<>();
        List<class_1297> affected=new ArrayList<>();
        for (class_1297 actor:actors) {
            Box body=box(actor.method_5829());
            if (geometry.collides(body)) affected.add(actor);
            else reserved.add(body);
        }
        affected.sort(Comparator.comparingInt(e -> e instanceof class_3222 ? 0 : e instanceof class_1309 ? 1 : 2));
        List<Move> moves=new ArrayList<>();
        for (class_1297 actor:affected) {
            Box body=box(actor.method_5829());
            Point target;
            try { target=openLanding(snapshot,interior,actor,geometry,reserved); }
            catch (RuntimeException failure) {
                if (RoomProjectionManager.active(actor.method_5667())==shell && !contestFollower(actor,shell)) throw failure;
                reserved.add(body);
                continue;
            }
            if (target==null) {
                if (RoomProjectionManager.active(actor.method_5667())==shell && !contestFollower(actor,shell))
                    throw new CasterPlacementException("No upward or horizontal open-domain landing for caster"
                            + " entity=" + actor.method_5667() + " body=" + body);
                // Ordinary entities do not delay expansion and are never sent outside a shell.
                reserved.add(body);
                continue;
            }
            moves.add(new Move(actor,new Point(actor.method_23317(),actor.method_23318(),actor.method_23321()),target));
            reserved.add(body.at(target));
        }
        return List.copyOf(moves);
    }

    /** Only this actor waits if its animation/terrain temporarily prevents relocation. */
    public static boolean retryEvacuation(RoomProjectionEntity shell,RoomProjectionEntity interior,class_1297 actor) {
        if (ProjectionEntityPolicy.ignores(actor)) return true;
        try {
            if (!SafeEntityTeleport.prepare(actor)) return false;
            RoomProjectionContraption insideSnapshot=(RoomProjectionContraption)interior.getContraption();
            if (insideSnapshot.sourceSpec().shell()==null) return true;
            Geometry geometry=new Geometry((RoomProjectionContraption)shell.getContraption(),shell.getAnchorVec());
            geometry.addAt((RoomProjectionContraption)interior.getContraption(),interior.getContraption().getContraptionWorld(),interior.getAnchorVec());
            for(var other:ProjectionIndex.future(actor.method_73183(),shell.method_5829().method_1014(2))) {
                if(other==shell||other==interior)continue;
                geometry.addAt((RoomProjectionContraption)other.getContraption(),other.getContraption().getContraptionWorld(),other.getAnchorVec());
            }
            Box body=box(actor.method_5829());
            class_3218 world=(class_3218)actor.method_73183();
            List<Box> reserved=world.method_8390(class_1297.class,shell.method_5829().method_1014(34),
                    e -> e!=actor && !(e instanceof RoomProjectionEntity) && e.method_5805() && !e.method_7325())
                    .stream().map(e -> box(e.method_5829())).toList();
            Box bounds=geometry.interiorBounds();
            Space insideSpace=space(world,actor,geometry,reserved,bounds);
            double maxY=bounds.maxY()-(body.maxY()-body.minY())-CLEARANCE;
            boolean player=actor instanceof class_1657;
            boolean inFootprint=body.x()>bounds.minX() && body.x()<bounds.maxX()
                    && body.z()>bounds.minZ() && body.z()<bounds.maxZ();
            // Same ladder as the staged transition: a foothold inside the room comes before any
            // side exit, and an actor in the footprint keeps waiting instead of being sent out.
            Point target=null;
            if(player) target=ProjectionSafetySearch.vertical(body,insideSpace,Math.max(body.minY(),bounds.minY()),maxY);
            else if(inFootprint) target=ProjectionSafetySearch.foothold(body,insideSpace,bounds.minY(),maxY);
            if(target==null && !player) target=ProjectionSafetySearch.horizontal(body,insideSpace,20,bounds);
            if(target==null && (player || inFootprint))
                target=inward(body,bounds,insideSpace,20,bounds.minY(),
                        player ? maxY : ProjectionSafetySearch.reach(body,maxY));
            Space exterior=space(world,actor,geometry,reserved,null);
            if(target==null && (player || !inFootprint)) target=ProjectionSafetySearch.sideExit(body,box(shell.method_5829()),exterior,20);
            if(target==null && (player || !inFootprint)) target=ProjectionSafetySearch.sideAirExit(body,box(shell.method_5829()),exterior,32);
            if (target==null) return false;
            return evacuate(new Move(actor,new Point(body.x(),body.minY(),body.z()),target));
        } catch (RuntimeException failure) { return false; }
    }

    private static Point inward(Box body,Box bounds,Space space,int radius,double minY,double maxY) {
        double cx=bounds.x(),cz=bounds.z();
        double original=Math.pow(body.x()-cx,2)+Math.pow(body.z()-cz,2);
        Space towardCenter=new Space() {
            public boolean clear(Box b) { return Math.pow(b.x()-cx,2)+Math.pow(b.z()-cz,2)<=original+1e-7 && space.clear(b); }
            public boolean supported(Box b) { return space.supported(b); }
            public List<Double> surfaces(Box b,double min,double max) { return space.surfaces(b,min,max); }
        };
        Point target=ProjectionSafetySearch.nearest(body,towardCenter,radius,false,minY,maxY);
        return target!=null ? target : ProjectionSafetySearch.nearest(body,space,radius,false,minY,maxY);
    }

    private static boolean evacuate(Move move) {
        ProjectionEvacuation.protect(move.entity);
        boolean moved=SafeEntityTeleport.move(move.entity,(class_3218)move.entity.method_73183(),
                move.to.x(),move.to.y(),move.to.z(),Set.of(),move.entity.method_36454(),move.entity.method_36455(),false);
        if (moved) { move.entity.method_18799(class_243.field_1353); move.entity.field_6017=0; }
        return moved;
    }

    public static List<Move> applyStage(List<Move> moves,RoomProjectionEntity shell,RoomProjectionEntity interior) {
        List<Move> applied=new ArrayList<>();
        boolean open=((RoomProjectionContraption)interior.getContraption()).sourceSpec().shell()==null;
        for (Move move:moves) {
            boolean moved;
            try { moved=evacuate(move); } catch (RuntimeException failure) { moved=false; }
            if (moved) applied.add(move);
            else if (RoomProjectionManager.active(move.entity.method_5667())==shell && !contestFollower(move.entity,shell))
                throw new IllegalStateException("Domain caster relocation rejected; retry transition");
            else if (!open) ProjectionEvacuation.defer(move.entity,shell,interior);
        }
        RoomProjectionManager.relocated(applied);
        return List.copyOf(applied);
    }

    public static List<Move> plan(RoomProjectionEntity projection,class_1309 caster) {
        RoomProjectionContraption snapshot=(RoomProjectionContraption)projection.getContraption();
        class_3218 world=(class_3218)projection.method_73183();
        Geometry geometry=new Geometry(snapshot,projection.getAnchorVec());
        List<class_1309> actors=new ArrayList<>(world.method_8390(class_1309.class,projection.method_5829(),
                e -> !ProjectionEntityPolicy.ignores(e) && e.method_5805() && !e.method_7325() && !e.method_31747()));
        actors.remove(caster);
        actors.add(0,caster);
        List<Box> reserved=new ArrayList<>();
        List<Move> moves=new ArrayList<>();
        int radius=Math.max(snapshot.sourceSpec().source().maxX()-snapshot.sourceSpec().source().minX(),
                snapshot.sourceSpec().source().maxZ()-snapshot.sourceSpec().source().minZ())+2;
        for (class_1309 actor:actors) {
            Box body=box(actor.method_5829());
            Point preferred=geometry.highest(body,actor==caster);
            boolean centralLift=actor==caster && preferred!=null && preferred.y()>body.minY()+CLEARANCE;
            if (!centralLift && !geometry.collides(body)) { reserved.add(body); continue; }
            Space space=space(world,actor,geometry,reserved,null);
            var ceilingVolume=snapshot.sourceSpec().shell()!=null?snapshot.sourceSpec().shell():snapshot.sourceSpec().source();
            double verticalRise=actor==caster ? Math.max(0,geometry.origin.field_1351+ceilingVolume.maxY()
                    -Math.max(2,body.maxY()-body.minY())-body.minY()-CLEARANCE) : MAX_RISE;
            Point target=actor==caster
                    ? ProjectionSafetySearch.find(body,preferred,space,geometry.origin.field_1352,
                        geometry.origin.field_1350,radius,verticalRise)
                    : ProjectionSafetySearch.nearest(body,space,radius,false,body.minY()-MAX_RISE,body.minY()+.5);
            if (target==null) target=ProjectionSafetySearch.sideExit(body,box(projection.method_5829()),space,radius);
            if (target==null) throw new IllegalStateException("No grounded lateral projection evacuation position");
            moves.add(new Move(actor,new Point(actor.method_23317(),actor.method_23318(),actor.method_23321()),target));
            reserved.add(body.at(target));
        }
        return List.copyOf(moves);
    }

    /** Reuse expansion collision/support search for the caster's real-domain entry. */
    public static Point domainLanding(class_3218 world, class_1309 actor, Point desired) {
        RoomProjectionContraption snapshot=RoomProjectionContraption.snapshot(world);
        return centralLanding(snapshot,world,world,actor,class_243.field_1353);
    }

    /** Covered occupants retain their own X/Z whenever possible; only the caster uses the center. */
    public static Point occupantLanding(RoomProjectionContraption snapshot,class_3218 source,
                                        RoomProjectionEntity projection,class_1309 actor) {
        Geometry geometry=new Geometry(snapshot,projection.getAnchorVec(),source);
        Box interior=geometry.interiorBounds();
        Box body=box(actor.method_5829());
        Space safe=space((class_3218)projection.method_73183(),actor,geometry,List.of(),interior);
        double maxY=interior.maxY()-(body.maxY()-body.minY())-CLEARANCE;
        if(safe.clear(body) && safe.supported(body))return new Point(body.x(),body.minY(),body.z());
        Point target=ProjectionSafetySearch.foothold(body,safe,interior.minY(),maxY);
        int radius=(int)Math.ceil(Math.max(interior.maxX()-interior.minX(),interior.maxZ()-interior.minZ()));
        if(target==null)target=ProjectionSafetySearch.nearest(body,safe,radius,false,interior.minY(),maxY);
        if(target==null)throw new IllegalStateException("No safe interior landing for covered occupant "+actor.method_5667());
        return target;
    }

    /** A supported return point in the real world, respecting ordinary and projected blocks. */
    public static Point returnLanding(class_3218 world,class_1309 actor,double x,double y,double z) {
        Box body=box(actor.method_5829()).at(new Point(x,y,z));
        Geometry empty=new Geometry();
        Space real=space(world,actor,empty,List.of(),null);
        Space safe=new Space() {
            public boolean clear(Box b) { return real.clear(b) && !ProjectionEnvironment.projectedCollision(world,nativeBox(b)); }
            public boolean supported(Box b) { return real.supported(b); }
            public List<Double> surfaces(Box b,double min,double max) { return real.surfaces(b,min,max); }
        };
        if(safe.clear(body) && safe.supported(body))return new Point(x,y,z);
        Point landing=ProjectionSafetySearch.nearest(body,safe,4,false,
                Math.max(world.method_31607(),y-6),Math.min(world.method_31607()+world.method_31605()-(body.maxY()-body.minY()),y+6));
        if(landing==null)throw new IllegalStateException("No safe supported villager return point near "+x+","+y+","+z);
        return landing;
    }

    public static Point expansionLanding(RoomProjectionContraption snapshot,class_3218 source,
                                         RoomProjectionEntity projection,class_1309 actor) {
        return centralLanding(snapshot,source,(class_3218)projection.method_73183(),actor,projection.getAnchorVec());
    }

    /** Open domains search from the caster's current feet, upward first and then horizontally. */
    public static Point openExpansionLanding(RoomProjectionContraption snapshot,
                                             RoomProjectionEntity projection,class_1309 actor) {
        Geometry geometry=new Geometry(snapshot,projection.getAnchorVec());
        Point target=openLanding(snapshot,projection,actor,geometry,List.of());
        if(target==null) throw new CasterPlacementException(
                "No upward or horizontal standing space for open projection caster");
        return target;
    }

    private static Point openLanding(RoomProjectionContraption snapshot,RoomProjectionEntity projection,
                                     class_1297 actor,Geometry geometry,List<Box> reserved) {
        class_3218 destination=(class_3218)projection.method_73183();
        Box body=box(actor.method_5829());
        double bodyHeight=body.maxY()-body.minY();
        double scanTop=projection.getAnchorVec().field_1351+snapshot.sourceSpec().source().maxY()+1;
        double worldTop=destination.method_31607()+destination.method_31605();
        double maxY=Math.min(scanTop,worldTop)-bodyHeight-CLEARANCE;
        if (maxY<body.minY()) return null;
        Space openSpace=space(destination,actor,geometry,reserved,null);
        Point target=ProjectionSafetySearch.vertical(body,openSpace,body.minY(),maxY);
        if (target!=null) return target;
        var source=snapshot.sourceSpec().source();
        int radius=Math.max(1,Math.min(24,Math.max(source.maxX()-source.minX()+1,
                source.maxZ()-source.minZ()+1)/2));
        return ProjectionSafetySearch.nearest(body,openSpace,radius,false,body.minY(),maxY);
    }

    private static Point centralLanding(RoomProjectionContraption snapshot,class_3218 source,class_3218 destination,
                                         class_1309 actor,class_243 origin) {
        Geometry geometry=new Geometry(snapshot,origin,source);
        Box interior=geometry.interiorBounds();
        var spec=snapshot.sourceSpec();
        Box body=box(actor.method_5829()).at(new Point(origin.field_1352+spec.anchorX(),
                interior.minY(),origin.field_1350+spec.anchorZ()));
        Box standing=box(actor.method_18377(class_4050.field_18076).method_30231(body.x(),body.minY(),body.z()));
        body=new Box(Math.min(body.minX(),standing.minX()),body.minY(),Math.min(body.minZ(),standing.minZ()),
                Math.max(body.maxX(),standing.maxX()),Math.max(body.maxY(),standing.maxY()),Math.max(body.maxZ(),standing.maxZ()));
        Space real=space(destination,actor,geometry,List.of(),interior);
        Space center=new Space() {
            private Box columns(Box b) {
                return new Box(Math.min(b.minX(),origin.field_1352+spec.anchorX()-1),b.minY(),
                        Math.min(b.minZ(),origin.field_1350+spec.anchorZ()-1),
                        Math.max(b.maxX(),origin.field_1352+spec.anchorX()+1),b.maxY(),
                        Math.max(b.maxZ(),origin.field_1350+spec.anchorZ()+1));
            }
            public boolean clear(Box b) { return real.clear(b) && !geometry.collides(columns(b)); }
            public boolean supported(Box b) {
                // The support must come from the copied room, not an unrelated exterior hill.
                return geometry.collides(new Box(b.minX(),b.minY()-.02,b.minZ(),b.maxX(),b.minY(),b.maxZ()));
            }
            public List<Double> surfaces(Box b,double min,double max) {
                List<Double> heights=new ArrayList<>();
                for (Shape shape:geometry.near(columns(b)))
                    if (!geometry.isCeiling(shape) && shape.box.horizontal(columns(b))) heights.add(shape.box.maxY());
                return heights;
            }
        };
        Point target=ProjectionSafetySearch.lowestStanding(body,center,interior.minY(),
                interior.maxY()-(body.maxY()-body.minY())-CLEARANCE);
        if (target==null) throw new IllegalStateException("No standing space in the four central domain columns");
        return target;
    }

    private static Space space(class_3218 world, class_1297 actor, Geometry geometry,
                               List<Box> reserved, Box limit) {
        return new Space() {
                public boolean clear(Box b) {
                    if(com.innerdimension.domain.SimpleDomain.get(actor.method_5667())!=null) {
                        var simple=com.innerdimension.domain.DomainContest.entry(actor.method_5667());
                        var footprint=simple==null?null:simple.bounds();
                        if(footprint!=null) {
                            double dx=b.x()-actor.method_23317(),dy=b.minY()-actor.method_5829().field_1322,dz=b.z()-actor.method_23321();
                            Box whole=new Box(footprint.field_1323+dx,b.minY(),footprint.field_1321+dz,footprint.field_1320+dx,b.maxY(),footprint.field_1324+dz);
                            if(geometry.collides(whole)||!world.method_8587(actor,nativeBox(whole)))return false;
                            if(limit!=null&&(whole.minX()<limit.minX()||whole.maxX()>limit.maxX()||whole.minZ()<limit.minZ()||whole.maxZ()>limit.maxZ()))return false;
                        }
                    }
                    if (b.minY() < world.method_31607() || b.maxY() > world.method_31607()+world.method_31605()) return false;
                    class_238 nativeBox=nativeBox(b);
                    // Exterior evacuation may cross into an unloaded neighboring chunk.
                    for (int x=((int)Math.floor(b.minX()))>>4;x<=((int)Math.floor(b.maxX()))>>4;x++)
                        for (int z=((int)Math.floor(b.minZ()))>>4;z<=((int)Math.floor(b.maxZ()))>>4;z++) world.method_8497(x,z);
                    return (limit == null || (b.minX() >= limit.minX() && b.maxX() <= limit.maxX()
                            && b.minZ() >= limit.minZ() && b.maxZ() <= limit.maxZ()
                            && b.minY() >= limit.minY() && b.maxY() <= limit.maxY()))
                            && !geometry.collides(b) && world.method_8587(actor,nativeBox)
                            && reserved.stream().noneMatch(other -> other.intersects(b));
                }
                public boolean supported(Box b) {
                    Box below=new Box(b.minX(),b.minY()-0.02,b.minZ(),b.maxX(),b.minY(),b.maxZ());
                    if (geometry.collides(below)) return true;
                    for (class_265 shape:world.method_20812(actor,nativeBox(below))) if (!shape.method_1110()) return true;
                    return false;
                }
                public List<Double> surfaces(Box b,double min,double max) {
                    List<Double> ys=new ArrayList<>();
                    for (Shape shape:geometry.near(b)) {
                        double y=shape.box.maxY();
                        if (y>=min && y<=max && shape.box.horizontal(b) && !geometry.isCeiling(shape)) ys.add(y);
                    }
                    Box column=new Box(b.minX(),min-1,b.minZ(),b.maxX(),max+1,b.maxZ());
                    if (!world.method_8477(new class_2338((int)Math.floor(b.x()),(int)Math.floor(min),(int)Math.floor(b.z())))) return ys;
                    for (class_265 shape:world.method_20812(actor,nativeBox(column))) for (class_238 aabb:shape.method_1090()) {
                        Box real=box(aabb);
                        if (real.maxY()>=min && real.maxY()<=max && real.horizontal(b)) ys.add(real.maxY());
                    }
                    return ys;
                }
            };
    }

    /** Finish animations first, then recompute geometry against the restored body positions. */
    public static List<Move> prepare(RoomProjectionEntity projection,class_1309 caster) {
        List<Move> first=plan(projection,caster);
        for (Move move:first) if (!SafeEntityTeleport.prepare(move.entity))
            throw new IllegalStateException("Could not prepare projection occupant teleport");
        return plan(projection,caster);
    }

    public static void apply(List<Move> moves) {
        List<Move> applied=new ArrayList<>();
        try {
            for (Move move:moves) {
                class_1297 actor=move.entity;
                if (!SafeEntityTeleport.move(actor,(class_3218)actor.method_73183(),move.to.x(),move.to.y(),move.to.z(),
                        Set.of(),actor.method_36454(),actor.method_36455(),false))
                    throw new IllegalStateException("Projection occupant teleport rejected");
                applied.add(move);
                actor.method_18799(class_243.field_1353);
                actor.field_6017=0;
            }
        } catch (RuntimeException failure) {
            for (Move move:applied) SafeEntityTeleport.move(move.entity,(class_3218)move.entity.method_73183(),
                    move.from.x(),move.from.y(),move.from.z(),Set.of(),move.entity.method_36454(),move.entity.method_36455(),false);
            throw failure;
        }
    }

    private static Box box(class_238 b) { return new Box(b.field_1323,b.field_1322,b.field_1321,b.field_1320,b.field_1325,b.field_1324); }
    private static class_238 nativeBox(Box b) { return new class_238(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()); }

    /** Index by source-space X/Z columns, so searches don't scan the whole room per candidate. */
    private static final class Geometry {
        private final Map<Long,List<Shape>> columns=new HashMap<>();
        private final ProjectionSourceSpec spec;
        private final class_243 origin;
        /** Real-world searches need no projection snapshot or bound entity. */
        Geometry() {
            spec=ProjectionSourceSpec.room();
            origin=class_243.field_1353;
        }
        Geometry(RoomProjectionContraption snapshot,class_243 origin) {
            this(snapshot,origin,snapshot.getContraptionWorld());
        }
        Geometry(RoomProjectionContraption snapshot,class_243 origin,class_1922 collisionWorld) {
            this.origin=origin;
            spec=snapshot.sourceSpec();
            add(snapshot,collisionWorld);
        }
        void add(RoomProjectionContraption snapshot,class_1922 collisionWorld) { addAt(snapshot,collisionWorld,origin); }
        void addAt(RoomProjectionContraption snapshot,class_1922 collisionWorld,class_243 blockOrigin) {
            for (var entry:snapshot.getBlocks().entrySet()) {
                class_2338 pos=entry.getKey();
                for (class_238 bounds:entry.getValue().comp_1342().method_26194(collisionWorld,pos,
                        class_3726.method_16194()).method_1090()) {
                    Box local=box(bounds).move(pos.method_10263()+blockOrigin.field_1352-origin.field_1352,
                        pos.method_10264()+blockOrigin.field_1351-origin.field_1351,pos.method_10260()+blockOrigin.field_1350-origin.field_1350);
                    Shape shape=new Shape(local.move(origin.field_1352,origin.field_1351,origin.field_1350),
                            spec.isShell(pos),pos.method_10263(),pos.method_10264(),pos.method_10260());
                    for (int x=(int)Math.floor(local.minX());x<Math.ceil(local.maxX());x++)
                        for (int z=(int)Math.floor(local.minZ());z<Math.ceil(local.maxZ());z++)
                            columns.computeIfAbsent(key(x,z),ignored -> new ArrayList<>()).add(shape);
                }
            }
        }
        Box interiorBounds() {
            var s=spec.shell()!=null ? spec.shell():spec.source();
            return new Box(s.minX()+1,s.minY()+1,s.minZ()+1,s.maxX(),s.maxY(),s.maxZ())
                    .move(origin.field_1352,origin.field_1351,origin.field_1350);
        }
        boolean sameShellSide(Box current,Box target) {
            return sameShellSide(current,target,true);
        }
        boolean sameShellSide(Box current,Box target,boolean vertical) {
            var s=spec.shell();
            if (s==null) return true;
            for (Shape shape:near(current)) if (shape.box.intersects(current)) {
                if (shape.sourceX==s.minX() || shape.sourceX==s.maxX())
                    if (!sameSide(current.x(),target.minX(),target.maxX(),origin.field_1352+shape.sourceX+.5)) return false;
                if (shape.sourceZ==s.minZ() || shape.sourceZ==s.maxZ())
                    if (!sameSide(current.z(),target.minZ(),target.maxZ(),origin.field_1350+shape.sourceZ+.5)) return false;
                if (vertical && (shape.sourceY==s.minY() || shape.sourceY==s.maxY()))
                    if (!sameSide((current.minY()+current.maxY())/2,target.minY(),target.maxY(),origin.field_1351+shape.sourceY+.5)) return false;
            }
            return true;
        }
        private static long key(int x,int z) { return ((long)x<<32) ^ (z&0xffffffffL); }
        List<Shape> near(Box b) {
            List<Shape> out=new ArrayList<>();
            for (int x=(int)Math.floor(b.minX()-origin.field_1352);x<Math.ceil(b.maxX()-origin.field_1352);x++)
                for (int z=(int)Math.floor(b.minZ()-origin.field_1350);z<Math.ceil(b.maxZ()-origin.field_1350);z++)
                    out.addAll(columns.getOrDefault(key(x,z),List.of()));
            return out;
        }
        boolean collides(Box b) { return near(b).stream().anyMatch(shape -> shape.box.intersects(b)); }
        boolean isCeiling(Shape shape) {
            return shape.shell && spec.shell()!=null && shape.box.minY()>=origin.field_1351+spec.shell().maxY();
        }
        Point highest(Box body,boolean central) {
            List<Shape> candidates;
            if (central) {
                candidates=new ArrayList<>();
                int x=(int)Math.floor(spec.anchorX()), z=(int)Math.floor(spec.anchorZ());
                for (int dx=-1;dx<=0;dx++) for (int dz=-1;dz<=0;dz++)
                    candidates.addAll(columns.getOrDefault(key(x+dx,z+dz),List.of()));
            } else candidates=near(body);
            Shape top=candidates.stream().filter(s -> !s.shell && s.box.maxY()>body.minY()
                    && (central || s.box.horizontal(body))).max(Comparator.comparingDouble(s -> s.box.maxY())).orElse(null);
            return top==null ? null : new Point(body.x(),top.box.maxY(),body.z());
        }
    }
}
