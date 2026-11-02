package com.innerdimension.domain.geometry;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The one geometry service. Everything spatial in the ability layer goes through here so
 * that "the domain" always means the union of real space and the collision-enabled
 * projection snapshot — never real blocks alone.
 */
public final class DomainGeometry {
    private static final double EPS = 1.0E-7;

    private DomainGeometry() {}

    /* --------------------------------------------------------------- solids */

    /** World-space collision shapes of real blocks plus projected blocks in the box. */
    public static List<class_265> shapes(class_1937 world, class_238 box) {
        List<class_265> out = new ArrayList<>(world.method_20743(null, box));
        out.addAll(ProjectionEnvironment.collisionShapes(world, box));
        return out;
    }

    public static boolean solid(class_1937 world, class_238 box) {
        if (!world.method_20743(null, box).isEmpty()) return true;
        return ProjectionEnvironment.projectedCollision(world, box);
    }

    /* ------------------------------------------------------------- support */

    /**
     * Highest support surface (real or projected) under the body within {@code depth}
     * blocks, or NaN when unsupported. Callers compute the distance themselves.
     */
    public static double supportTop(class_1937 world, class_238 body, double depth) {
        double min = body.field_1322 - depth;
        double max = body.field_1322 + 0.05;
        class_238 probe = new class_238(body.field_1323, min, body.field_1321, body.field_1320, max, body.field_1324);
        double best = Double.NaN;
        for (class_265 shape : world.method_20743(null, probe)) {
            class_238 box = shape.method_1107();
            if (box.field_1325 > max + EPS || !horizontal(box, body)) continue;
            if (Double.isNaN(best) || box.field_1325 > best) best = box.field_1325;
        }
        double projected = ProjectionEnvironment.supportHeight(world, body, min, max);
        if (!Double.isNaN(projected)) best = Double.isNaN(best) ? projected : Math.max(best, projected);
        return best;
    }

    /** Distance from the body's feet down to its support; positive, or NaN when none. */
    public static double supportDistance(class_1937 world, class_238 body, double depth) {
        double top = supportTop(world, body, depth);
        return Double.isNaN(top) ? Double.NaN : body.field_1322 - top;
    }

    /** Lowest obstruction above the body within {@code height} blocks, or NaN. */
    public static double ceilingBottom(class_1937 world, class_238 body, double height) {
        double min = body.field_1325 - 0.05;
        double max = body.field_1325 + height;
        class_238 probe = new class_238(body.field_1323, min, body.field_1321, body.field_1320, max, body.field_1324);
        double best = Double.NaN;
        for (class_265 shape : shapes(world, probe)) {
            class_238 box = shape.method_1107();
            if (box.field_1322 < min - EPS || !horizontal(box, body)) continue;
            if (Double.isNaN(best) || box.field_1322 < best) best = box.field_1322;
        }
        return best;
    }

    /* ------------------------------------------------------------- sweeps */

    /** Vanilla collision resolution, extended with projected shapes. */
    public static class_243 sweep(class_1297 entity, class_243 movement) {
        if (entity == null || movement.method_1033() < EPS) return movement;
        class_1937 world = entity.method_73183();
        class_238 body = entity.method_5829();
        class_238 swept = body.method_1009(movement.field_1352, movement.field_1351, movement.field_1350).method_1014(0.02);
        return class_1297.method_20736(entity, movement, body, world, shapes(world, swept));
    }

    /**
     * Clips a movement so the entity never leaves the domain interior. The component that
     * would cross the boundary is truncated instead of teleporting the entity back.
     */
    public static class_243 clipToSession(DomainSession session, class_1297 entity, class_243 movement) {
        if (session == null || entity == null) return movement;
        class_238 box = entity.method_5829();
        double dx = movement.field_1352, dy = movement.field_1351, dz = movement.field_1350;
        if (box.field_1323 + dx < session.minX()) dx = session.minX() - box.field_1323;
        if (box.field_1320 + dx > session.maxX()) dx = session.maxX() - box.field_1320;
        if (box.field_1321 + dz < session.minZ()) dz = session.minZ() - box.field_1321;
        if (box.field_1324 + dz > session.maxZ()) dz = session.maxZ() - box.field_1324;
        if (box.field_1322 + dy < session.minY()) dy = session.minY() - box.field_1322;
        if (box.field_1325 + dy > session.maxY()) dy = session.maxY() - box.field_1325;
        return new class_243(dx, dy, dz);
    }

    /** Best-effort move that stays inside the domain and behind collision. */
    public static void moveInside(DomainSession session, class_1297 entity, class_243 movement) {
        class_243 clipped = clipToSession(session, entity, movement);
        class_243 resolved = sweep(entity, clipped);
        entity.method_18799(resolved);
        entity.field_6037 = true;
    }

    /* ------------------------------------------------------------ raycast */

    /**
     * Layered block raycast.
     *
     * @param ignoreInteriorReal  skip ordinary real blocks strictly inside the session interior
     *                            (the projection shell and everything outside still hit)
     * @param ignoreProjected     skip projected blocks entirely
     */
    public static class_3965 raycast(class_1937 world, DomainSession session, class_243 start, class_243 end,
                                     boolean ignoreInteriorReal, boolean ignoreProjected) {
        class_243 delta = end.method_1020(start);
        double length = delta.method_1033();
        if (length < EPS) return null;
        int steps = (int) Math.ceil(length / 0.25);
        class_243 step = delta.method_1021(1.0 / steps);
        Double bestT = null;
        class_2338 bestPos = null;
        class_2350 bestSide = null;
        class_2338 lastPos = null;
        for (int i = 1; i <= steps; i++) {
            class_243 point = start.method_1019(step.method_1021(i));
            class_2338 pos = class_2338.method_49638(point);
            if (pos.equals(lastPos)) continue;
            lastPos = pos;
            if (!ignoreProjected) {
                class_238 cell = new class_238(pos);
                for (class_265 shape : ProjectionEnvironment.collisionShapes(world, cell.method_1014(0.05))) {
                    class_3965 hit = shape.method_1092(start, end, class_2338.field_10980);
                    if (hit == null) continue;
                    double t = start.method_1025(hit.method_17784());
                    if (bestT == null || t < bestT) { bestT = t; bestPos = pos; bestSide = hit.method_17780(); }
                }
            }
            boolean interior = session != null && session.containsBlock(pos);
            if (!(ignoreInteriorReal && interior)) {
                class_2680 state = world.method_8320(pos);
                if (!state.method_26215()) {
                    class_265 shape = state.method_26194(world, pos, class_3726.method_16194());
                    class_3965 hit = shape.method_1092(start, end, pos);
                    if (hit != null) {
                        double t = start.method_1025(hit.method_17784());
                        if (bestT == null || t < bestT) { bestT = t; bestPos = pos; bestSide = hit.method_17780(); }
                    }
                }
            }
            // A target outside the interior is not something this ray may keep following;
            // whatever was hit up to the boundary is the answer.
            if (session != null && !session.containsPoint(point.field_1352, point.field_1351, point.field_1350)
                    && !session.contains(new class_238(point, point).method_1014(0.05))) break;
        }
        if (bestT == null) return null;
        class_243 hitPoint = start.method_1019(delta.method_1021(Math.min(1.0, bestT / length)));
        return new class_3965(hitPoint, bestSide == null ? class_2350.field_11036 : bestSide, bestPos, false);
    }

    /** True when nothing blocks the straight line between the two points (mixed geometry). */
    public static boolean lineOfSight(class_1937 world, DomainSession session, class_243 from, class_243 to) {
        return raycast(world, session, from, to, false, false) == null;
    }

    /* -------------------------------------------------------- safe points */

    /**
     * Nearest safe standing point for a humanoid inside the interior, or null. Used by the
     * domain trader and by any ability that must not place an entity inside geometry.
     */
    public static class_243 safeSummonPoint(DomainSession session, class_1297 entity,
                                          double x, double y, double z) {
        double half = entity.method_17681() * .5;
        double height = entity.method_17682();
        var body = new com.innerdimension.projection.ProjectionSafetySearch.Box(x-half,y,z-half,x+half,y+height,z+half);
        var space = new com.innerdimension.projection.ProjectionSafetySearch.Space() {
            private class_238 nativeBox(com.innerdimension.projection.ProjectionSafetySearch.Box b) {
                return new class_238(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());
            }
            public boolean clear(com.innerdimension.projection.ProjectionSafetySearch.Box b) {
                class_238 box = nativeBox(b);
                return b.minX()>=session.minX() && b.maxX()<=session.maxX()
                        && b.minY()>=session.minY() && b.maxY()<=session.maxY()
                        && b.minZ()>=session.minZ() && b.maxZ()<=session.maxZ()
                        && session.containsPoint(b.x(),b.minY(),b.z())
                        && !solid(session.world,box)
                        && session.world.method_8587(entity,box)
                        && session.world.method_8316(class_2338.method_49638(new class_243(b.x(),b.minY(),b.z()))).method_15769()
                        && ProjectionEnvironment.fluid(session.world,box,b.maxY()-.1).kind()==ProjectionEnvironment.FluidKind.NONE;
            }
            public boolean supported(com.innerdimension.projection.ProjectionSafetySearch.Box b) {
                double distance = supportDistance(session.world,nativeBox(b),.05);
                return !Double.isNaN(distance) && distance>=-EPS && distance<=.05;
            }
            public List<Double> surfaces(com.innerdimension.projection.ProjectionSafetySearch.Box b,double min,double max) {
                List<Double> tops = new ArrayList<>();
                class_238 column = new class_238(b.minX(),min-.1,b.minZ(),b.maxX(),max+.1,b.maxZ());
                for(class_265 shape:shapes(session.world,column)) for(class_238 box:shape.method_1090())
                    if(box.field_1325>=min && box.field_1325<=max && horizontal(box,nativeBox(b))) tops.add(box.field_1325);
                return tops;
            }
        };
        // Exact support heights handle slabs, stairs and the projection's fractional floor.
        // Prefer the owner's current level rather than the topmost surface in the room.
        var point = com.innerdimension.projection.ProjectionSafetySearch.nearest(body,space,3,false,
                Math.max(session.minY(),y-3),Math.min(session.maxY()-height,y+1.5));
        return point==null ? null : new class_243(point.x(),point.y(),point.z());
    }

    public static class_243 safeStandingPoint(DomainSession session, double preferredX, double preferredZ) {
        class_1937 world = session.world;
        for (int ring = 0; ring <= 8; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    double x = Math.floor(preferredX) + 0.5 + dx;
                    double z = Math.floor(preferredZ) + 0.5 + dz;
                    if (x < session.minX() + 0.5 || x > session.maxX() - 0.5) continue;
                    if (z < session.minZ() + 0.5 || z > session.maxZ() - 0.5) continue;
                    for (double y = session.maxY() - 2; y >= session.minY(); y--) {
                        class_238 feet = new class_238(x - 0.3, y, z - 0.3, x + 0.3, y + 0.02, z + 0.3);
                        double top = supportTop(world, feet, 0.4);
                        if (Double.isNaN(top) || Math.abs(top - y) > 0.4) continue;
                        class_238 body = new class_238(x - 0.3, y, z - 0.3, x + 0.3, y + 1.9, z + 0.3);
                        if (solid(world, body)) continue;
                        return new class_243(x, y, z);
                    }
                }
            }
        }
        return null;
    }

    /** Same search using only vanilla world collision; projected geometry is deliberately ignored. */
    public static class_243 safeRealityStandingPoint(DomainSession session, double preferredX, double preferredZ) {
        class_1937 world = session.world;
        for (int ring = 0; ring <= 8; ring++) for (int dx = -ring; dx <= ring; dx++)
            for (int dz = -ring; dz <= ring; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                double x = Math.floor(preferredX) + 0.5 + dx;
                double z = Math.floor(preferredZ) + 0.5 + dz;
                if (x < session.minX() + 0.5 || x > session.maxX() - 0.5
                        || z < session.minZ() + 0.5 || z > session.maxZ() - 0.5) continue;
                for (double y = session.maxY() - 2; y >= session.minY(); y--) {
                    class_238 feet = new class_238(x - 0.3, y - 0.4, z - 0.3, x + 0.3, y + 0.02, z + 0.3);
                    double support = Double.NaN;
                    for (class_265 shape : world.method_20743(null, feet)) {
                        class_238 box = shape.method_1107();
                        if (box.field_1325 <= y + 0.05 && horizontal(box,
                                new class_238(x - 0.3, y, z - 0.3, x + 0.3, y + 1.9, z + 0.3)))
                            support = Double.isNaN(support) ? box.field_1325 : Math.max(support, box.field_1325);
                    }
                    if (Double.isNaN(support) || Math.abs(support - y) > 0.4) continue;
                    class_238 body = new class_238(x - 0.3, y, z - 0.3, x + 0.3, y + 1.9, z + 0.3);
                    if (!world.method_20743(null, body).isEmpty()) continue;
                    return new class_243(x, y, z);
                }
            }
        return null;
    }

    /* -------------------------------------------------------------- fluid */

    public static ProjectionEnvironment.FluidContact fluid(class_1297 entity) {
        return ProjectionEnvironment.fluid(entity);
    }

    public static boolean eyesInWater(class_1297 entity) {
        return ProjectionEnvironment.eyesIn(entity, class_3486.field_15517);
    }

    public static boolean eyesInLava(class_1297 entity) {
        return ProjectionEnvironment.eyesIn(entity, class_3486.field_15518);
    }

    /** True when the entity's feet are in or on a real or projected water volume. */
    public static boolean inWater(class_1297 entity) {
        return ProjectionEnvironment.fluid(entity).kind() == ProjectionEnvironment.FluidKind.WATER;
    }

    public static boolean inLava(class_1297 entity) {
        return ProjectionEnvironment.fluid(entity).kind() == ProjectionEnvironment.FluidKind.LAVA;
    }

    /* ------------------------------------------------------------- maths */

    public static boolean horizontal(class_238 a, class_238 b) {
        return a.field_1320 > b.field_1323 + EPS && a.field_1323 < b.field_1320 - EPS
                && a.field_1324 > b.field_1321 + EPS && a.field_1321 < b.field_1324 - EPS;
    }

    /** Angle in degrees between the entity's look vector and the direction to the target. */
    public static double viewAngle(class_1309 viewer, class_1297 target) {
        class_243 look = viewer.method_5828(1.0f);
        class_243 to = target.method_33571().method_1020(viewer.method_33571());
        double length = to.method_1033();
        if (length < EPS) return 0;
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, look.method_1026(to.method_1021(1 / length))))));
    }

    /** Direction from a to b, normalised, or zero. */
    public static class_243 direction(class_243 a, class_243 b) {
        class_243 d = b.method_1020(a);
        double l = d.method_1033();
        return l < EPS ? class_243.field_1353 : d.method_1021(1 / l);
    }

    public static class_243 horizontalAway(class_1297 from, class_1297 target, double strength) {
        class_243 d = target.method_33571().method_1020(from.method_33571());
        d = new class_243(d.field_1352, 0, d.field_1350);
        double l = d.method_1033();
        if (l < EPS) return class_243.field_1353;
        return d.method_1021(strength / l);
    }

    /** Clamps a velocity so the entity stays inside the domain after moving. */
    public static class_243 clampInside(DomainSession session, class_1297 entity, class_243 velocity) {
        class_238 box = entity.method_5829();
        double nx = box.method_1009(velocity.field_1352, velocity.field_1351, velocity.field_1350).field_1323;
        double px = box.method_1009(velocity.field_1352, velocity.field_1351, velocity.field_1350).field_1320;
        double nz = box.method_1009(velocity.field_1352, velocity.field_1351, velocity.field_1350).field_1321;
        double pz = box.method_1009(velocity.field_1352, velocity.field_1351, velocity.field_1350).field_1324;
        double dx = velocity.field_1352, dz = velocity.field_1350;
        if (nx < session.minX() || px > session.maxX()) dx = 0;
        if (nz < session.minZ() || pz > session.maxZ()) dz = 0;
        return new class_243(dx, velocity.field_1351, dz);
    }
}
