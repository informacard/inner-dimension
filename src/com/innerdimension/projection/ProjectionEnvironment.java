package com.innerdimension.projection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_259;
import net.minecraft.class_2680;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_3726;
import net.minecraft.class_6862;

/** Read-only union of real space and collision-enabled projection snapshots. */
public final class ProjectionEnvironment {
    private static final double EPSILON = 1.0E-7;
    private static final Map<RoomProjectionContraption, LocalSnapshot> SNAPSHOTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private ProjectionEnvironment() { }

    public static void invalidate(RoomProjectionContraption contraption) { SNAPSHOTS.remove(contraption); }

    public enum FluidKind { NONE, WATER, LAVA }

    public record FluidContact(FluidKind kind, double depth, boolean eyesIn, class_243 flow) {
        static final FluidContact NONE = new FluidContact(FluidKind.NONE, 0, false, class_243.field_1353);
    }

    private record LocalSolid(class_238 box) { }
    private record LocalFluid(class_238 box, FluidKind kind, class_243 flow) { }
    private record LocalSnapshot(List<LocalSolid> solids, List<LocalFluid> fluids,
                                 Map<Long, List<LocalSolid>> solidColumns,
                                 Map<Long, List<LocalFluid>> fluidColumns) { }

    private static LocalSnapshot snapshot(RoomProjectionContraption contraption) {
        return SNAPSHOTS.computeIfAbsent(contraption, ProjectionEnvironment::buildSnapshot);
    }

    private static LocalSnapshot buildSnapshot(RoomProjectionContraption contraption) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        List<LocalSolid> solids = new ArrayList<>();
        List<LocalFluid> fluids = new ArrayList<>();
        Map<Long, List<LocalSolid>> solidColumns = new HashMap<>();
        Map<Long, List<LocalFluid>> fluidColumns = new HashMap<>();
        class_1922 view = contraption.snapshotView();
        for (var entry : contraption.getBlocks().entrySet()) {
            class_2338 pos = entry.getKey();
            class_2680 state = entry.getValue().comp_1342();
            class_265 shape = state.method_26194(view, pos, class_3726.method_16194());
            for (class_238 box : shape.method_1090()) {
                LocalSolid solid = new LocalSolid(box.method_997(new class_243(pos.method_10263(), pos.method_10264(), pos.method_10260())));
                solids.add(solid);
                index(solidColumns, solid.box(), solid);
            }

            class_3610 fluid = state.method_26227();
            FluidKind kind = fluid.method_15767(class_3486.field_15517) ? FluidKind.WATER
                    : fluid.method_15767(class_3486.field_15518) ? FluidKind.LAVA : FluidKind.NONE;
            if (kind != FluidKind.NONE) {
                double height = fluid.method_15763(view, pos);
                if (height > 0) {
                    LocalFluid sample = new LocalFluid(new class_238(pos.method_10263(), pos.method_10264(), pos.method_10260(),
                            pos.method_10263() + 1, pos.method_10264() + height, pos.method_10260() + 1), kind,
                            fluid.method_15758(view,pos));
                    fluids.add(sample);
                    index(fluidColumns, sample.box(), sample);
                }
            }
        }
        solidColumns.replaceAll((ignored, value) -> List.copyOf(value));
        fluidColumns.replaceAll((ignored, value) -> List.copyOf(value));
        return new LocalSnapshot(List.copyOf(solids), List.copyOf(fluids), Map.copyOf(solidColumns), Map.copyOf(fluidColumns));
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.SHAPE_BUILD,auditStart); }
    }

    public static boolean hasProjection(class_1937 world, class_238 region) {
        return !ProjectionIndex.solid(world, region).isEmpty();
    }

    /** Stable enough for path invalidation: entity id, phase and exact anchor are represented. */
    public static long signature(class_1937 world, class_238 region) {
        long value = 0xcbf29ce484222325L;
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, region)) {
            value = (value ^ projection.method_5628()) * 0x100000001b3L;
            value = (value ^ projection.phase()) * 0x100000001b3L;
            value = (value ^ ((RoomProjectionContraption)projection.getContraption()).geometryVersion()) * 0x100000001b3L;
            class_243 anchor = projection.getAnchorVec();
            value = (value ^ Double.doubleToLongBits(anchor.field_1352)) * 0x100000001b3L;
            value = (value ^ Double.doubleToLongBits(anchor.field_1351)) * 0x100000001b3L;
            value = (value ^ Double.doubleToLongBits(anchor.field_1350)) * 0x100000001b3L;
        }
        return value;
    }

    public static boolean projectedCollision(class_1937 world, class_238 box) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, box.method_1014(0.02))) {
            class_243 anchor = projection.getAnchorVec();
            LocalSnapshot local = snapshot((RoomProjectionContraption) projection.getContraption());
            for (LocalSolid solid : near(local.solidColumns(), local(box, anchor))) {
                if (solid.box().method_997(anchor).method_994(box)) return true;
            }
        }
        return false;
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.COLLISION_QUERY,auditStart); }
    }

    /** World-space shapes for vanilla Entity.move's single collision/step-up pass. */
    public static List<class_265> collisionShapes(class_1937 world, class_238 sweptBox) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        List<class_265> result = new ArrayList<>();
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, sweptBox.method_1014(0.02))) {
            class_243 anchor = projection.getAnchorVec();
            LocalSnapshot local = snapshot((RoomProjectionContraption) projection.getContraption());
            for (LocalSolid solid : near(local.solidColumns(), local(sweptBox, anchor))) {
                class_238 box = solid.box().method_997(anchor);
                if (box.method_994(sweptBox)) result.add(class_259.method_1078(box));
            }
        }
        return result;
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.COLLISION_QUERY,auditStart); }
    }

    /** Thin lava tops join the same collision solve as the domain's solid blocks. */
    public static List<class_265> lavaSurfaceShapes(class_1297 entity, class_238 sweptBox) {
        if (!com.innerdimension.domain.ability.D52.walksOnLava(entity)) return List.of();
        class_1937 world = entity.method_73183();
        double feet = entity.method_5829().field_1322;
        List<class_265> result = new ArrayList<>();
        class_238 search = sweptBox.method_1014(0.1);
        for (int x=(int)Math.floor(search.field_1323); x<=Math.floor(search.field_1320); x++)
            for (int z=(int)Math.floor(search.field_1321); z<=Math.floor(search.field_1324); z++)
                for (int y=(int)Math.floor(search.field_1322)-1; y<=Math.min(Math.floor(feet),Math.floor(search.field_1325)); y++) {
                    class_2338 pos = new class_2338(x,y,z);
                    class_3610 fluid = world.method_8316(pos);
                    if (!fluid.method_15767(class_3486.field_15518)) continue;
                    double top = y + fluid.method_15763(world,pos);
                    addLavaTop(result, new class_238(x,y,z,x+1,top,z+1), feet, search);
                }
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, search)) {
            class_243 anchor = projection.getAnchorVec();
            LocalSnapshot snapshot = snapshot((RoomProjectionContraption)projection.getContraption());
            for (LocalFluid fluid : near(snapshot.fluidColumns(), local(search,anchor)))
                if (fluid.kind() == FluidKind.LAVA) addLavaTop(result, fluid.box().method_997(anchor), feet, search);
        }
        return result;
    }
    private static void addLavaTop(List<class_265> result, class_238 volume, double feet, class_238 search) {
        if (!com.innerdimension.domain.ability.D52.surfaceEligible(feet,volume.field_1325)) return;
        class_238 top = new class_238(volume.field_1323,volume.field_1325-0.025,volume.field_1321,
                volume.field_1320,volume.field_1325,volume.field_1324);
        if (top.method_994(search)) result.add(class_259.method_1078(top));
    }

    /** Highest exact projected support surface in the requested vertical interval. */
    public static double supportHeight(class_1937 world, class_238 footprint, double minY, double maxY) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        class_238 search = new class_238(footprint.field_1323, minY - 1, footprint.field_1321,
                footprint.field_1320, maxY, footprint.field_1324);
        double best = Double.NaN;
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, search)) {
            class_243 anchor = projection.getAnchorVec();
            LocalSnapshot local = snapshot((RoomProjectionContraption) projection.getContraption());
            for (LocalSolid solid : near(local.solidColumns(), local(search, anchor))) {
                class_238 box = solid.box().method_997(anchor);
                double y = box.field_1325;
                if (y + EPSILON < minY || y - EPSILON > maxY || !horizontal(box, footprint)) continue;
                if (Double.isNaN(best) || y > best) best = y;
            }
        }
        return best;
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.COLLISION_QUERY,auditStart); }
    }

    /** Classifies a ground-navigation cell using the mob's real dimensions and exact projected shapes. */
    public static Cell cell(class_1308 mob, int x, int y, int z) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        class_1937 world = mob.method_73183();
        double half = mob.method_17681() * 0.5;
        class_238 body = new class_238(x + 0.5 - half, y + 0.001, z + 0.5 - half,
                x + 0.5 + half, y + mob.method_17682() - 0.001, z + 0.5 + half);
        if (!hasProjection(world, body.method_1014(1.25))) return Cell.NONE;
        if (projectedCollision(world, body)) return Cell.BLOCKED;
        FluidContact fluid = fluid(world, body, y + mob.method_17682() * 0.85);
        if (fluid.kind() == FluidKind.WATER) return Cell.WATER;
        if (fluid.kind() == FluidKind.LAVA) return Cell.LAVA;
        double support = supportHeight(world, body, y - 1.126, y + 0.001);
        return Double.isNaN(support) ? Cell.NONE : Cell.WALKABLE;
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.PATH_QUERY,auditStart); }
    }

    public enum Cell { NONE, BLOCKED, WALKABLE, WATER, LAVA }

    public static FluidContact fluid(class_1297 entity) {
        // The carrier's large box describes scenery, not a body immersed in that scenery.
        if(entity instanceof RoomProjectionEntity)return FluidContact.NONE;
        return fluid(entity.method_73183(), entity.method_5829().method_1011(0.001), entity.method_23320());
    }

    public static FluidContact fluidAt(class_1937 world, class_243 point) {
        class_238 probe = new class_238(point.field_1352 - EPSILON, point.field_1351 - EPSILON, point.field_1350 - EPSILON,
                point.field_1352 + EPSILON, point.field_1351 + EPSILON, point.field_1350 + EPSILON);
        return fluid(world, probe, point.field_1351);
    }

    public static FluidContact fluid(class_1937 world, class_238 body, double eyeY) {
        long auditStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        try {
        FluidKind selected = FluidKind.NONE;
        double depth = 0;
        boolean eyes = false;
        class_243 flow=class_243.field_1353;
        int flowSamples=0;
        // ProjectionIndex order is stable for a session. First overlapping projection wins when
        // different projected fluid types occupy the same volume.
        for (RoomProjectionEntity projection : ProjectionIndex.solid(world, body.method_1014(0.02))) {
            class_243 anchor = projection.getAnchorVec();
            LocalSnapshot local = snapshot((RoomProjectionContraption) projection.getContraption());
            for (LocalFluid fluid : near(local.fluidColumns(), local(body, anchor))) {
                class_238 volume = fluid.box().method_997(anchor);
                if (!volume.method_994(body)) continue;
                class_238 overlap = intersection(volume, body);
                if (overlap == null || fullyOccluded(local, anchor, overlap)) continue;
                if (selected == FluidKind.NONE) selected = fluid.kind();
                if (selected != fluid.kind()) continue;
                depth = Math.max(depth, volume.field_1325 - body.field_1322);
                if(fluid.flow().method_1033()>0) { flow=flow.method_1019(fluid.flow()); flowSamples++; }
                eyes |= eyeY > volume.field_1322 + EPSILON && eyeY < volume.field_1325 - EPSILON
                        && (body.field_1323 + body.field_1320) * 0.5 > volume.field_1323
                        && (body.field_1323 + body.field_1320) * 0.5 < volume.field_1320
                        && (body.field_1321 + body.field_1324) * 0.5 > volume.field_1321
                        && (body.field_1321 + body.field_1324) * 0.5 < volume.field_1324;
            }
        }
        if(flowSamples>0) flow=flow.method_1021(1.0/flowSamples);
        return selected == FluidKind.NONE ? FluidContact.NONE : new FluidContact(selected, Math.max(0, depth), eyes, flow);
    
        } finally { com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.FLUID_QUERY,auditStart); }
    }

    public static boolean eyesIn(class_1297 entity, class_6862<class_3611> tag) {
        if(entity instanceof RoomProjectionEntity)return false;
        FluidContact contact = fluidAt(entity.method_73183(), new class_243(entity.method_23317(), entity.method_23320(), entity.method_23321()));
        return ((contact.kind() == FluidKind.WATER && tag == class_3486.field_15517)
                || (contact.kind() == FluidKind.LAVA && tag == class_3486.field_15518));
    }

    private static boolean fullyOccluded(LocalSnapshot snapshot, class_243 anchor, class_238 overlap) {
        class_243 center = overlap.method_1005();
        for (LocalSolid solid : near(snapshot.solidColumns(), local(overlap,anchor))) {
            class_238 box = solid.box().method_997(anchor);
            if (box.method_1008(center.field_1352, center.field_1351, center.field_1350)) return true;
        }
        return false;
    }

    private static class_238 intersection(class_238 a, class_238 b) {
        double minX = Math.max(a.field_1323, b.field_1323), minY = Math.max(a.field_1322, b.field_1322), minZ = Math.max(a.field_1321, b.field_1321);
        double maxX = Math.min(a.field_1320, b.field_1320), maxY = Math.min(a.field_1325, b.field_1325), maxZ = Math.min(a.field_1324, b.field_1324);
        return maxX > minX && maxY > minY && maxZ > minZ ? new class_238(minX, minY, minZ, maxX, maxY, maxZ) : null;
    }

    private static boolean horizontal(class_238 a, class_238 b) {
        return a.field_1320 > b.field_1323 + EPSILON && a.field_1323 < b.field_1320 - EPSILON
                && a.field_1324 > b.field_1321 + EPSILON && a.field_1321 < b.field_1324 - EPSILON;
    }

    private static class_238 local(class_238 world, class_243 anchor) {
        return world.method_997(anchor.method_1021(-1));
    }

    private static long key(int x, int z) { return ((long)x << 32) ^ (z & 0xffffffffL); }

    private static <T> void index(Map<Long, List<T>> columns, class_238 box, T value) {
        for (int x = (int)Math.floor(box.field_1323); x < Math.ceil(box.field_1320); x++)
            for (int z = (int)Math.floor(box.field_1321); z < Math.ceil(box.field_1324); z++)
                columns.computeIfAbsent(key(x, z), ignored -> new ArrayList<>()).add(value);
    }

    private static <T> List<T> near(Map<Long, List<T>> columns, class_238 box) {
        List<T> result = new ArrayList<>();
        for (int x = (int)Math.floor(box.field_1323); x < Math.ceil(box.field_1320); x++)
            for (int z = (int)Math.floor(box.field_1321); z < Math.ceil(box.field_1324); z++)
                result.addAll(columns.getOrDefault(key(x, z), List.of()));
        OpenDomainPerformance.count(OpenDomainPerformance.Metric.SPATIAL_CANDIDATES,result.size());
        return result;
    }
}
