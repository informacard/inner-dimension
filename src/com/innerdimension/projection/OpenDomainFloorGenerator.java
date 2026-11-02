package com.innerdimension.projection;

import com.innerdimension.dimension.RoomGeometry;
import com.innerdimension.block.ModBlocks;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;

import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the first open-domain prototype without writing a single block into either world. */
final class OpenDomainFloorGenerator {
    private static final int SIZE = RoomGeometry.INTERIOR_SIZE;
    private static final int FLOOR_Y = RoomGeometry.DOMAIN_FLOOR + 1;

    record Generated(ProjectionSourceSpec spec, Map<class_2338, class_2680> blocks) { }

    private OpenDomainFloorGenerator() { }

    static Generated generate(class_3218 source, class_3218 destination, net.minecraft.class_243 feet, FloorPatternSettings settings, int RADIUS) {
        class_2680[][] tile = new class_2680[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) for (int z = 0; z < SIZE; z++)
            tile[x][z] = source.method_8320(new class_2338(x, FLOOR_Y, z));
        FloorPatternSampler pattern = new FloorPatternSampler(tile, settings);

        Map<class_2338, class_2680> blocks = new LinkedHashMap<>();
        int originX = (int)Math.round(feet.field_1352 - SIZE / 2.0);
        int originZ = (int)Math.round(feet.field_1350 - SIZE / 2.0);
        int originY = (int)Math.round(feet.field_1351) - FLOOR_Y;
        int minX = (int)Math.floor(SIZE / 2.0 - RADIUS);
        int maxX = minX + RADIUS * 2 - 1;
        int minZ = minX, maxZ = maxX;
        int minY = FLOOR_Y - RADIUS;
        int maxY = minY + RADIUS * 2 - 1;

        // Replace every full real block in the cube with the corresponding domain material.
        // Interior blocks that have six full neighbours cannot expose a face and are omitted.
        // The outermost layer is retained even when buried, so cutting the finite domain through
        // terrain never leaves its boundary faces missing.
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++)
            for (int z = minZ; z <= maxZ; z++) {
                if(!OpenDomainRules.intersects(x-SIZE/2.0,y-FLOOR_Y,z-SIZE/2.0,x+1-SIZE/2.0,y+1-FLOOR_Y,z+1-SIZE/2.0,RADIUS))continue;
                class_2338 worldPos = new class_2338(originX + x, originY + y, originZ + z);
                if (!fullCube(destination, worldPos)) continue;
                boolean boundary = x == minX || x == maxX || y == minY || y == maxY
                        || z == minZ || z == maxZ || OpenDomainRules.farthest(x-SIZE/2.0,y-FLOOR_Y,z-SIZE/2.0)>=RADIUS-1;
                if (!boundary && hidden(destination, worldPos)) continue;
                class_2680 replacement = FloorPatternSampler.floorBlock(pattern.at(x, z));
                if (!replacement.method_26215()) blocks.put(new class_2338(x, y, z), replacement);
            }

        // Buildings are independent from the terrain skin. Copy only blocks above the editable
        // floor and place them at the caster; the old flat 16x16 floor itself is never copied.
        for (int y = FLOOR_Y + 1; y < RoomGeometry.DOMAIN_CEILING; y++)
            for (int x = 0; x < SIZE; x++) for (int z = 0; z < SIZE; z++) {
                class_2338 pos = new class_2338(x, y, z);
                class_2680 state = source.method_8320(pos);
                if (OpenDomainRules.intersects(x-SIZE/2.0,y-FLOOR_Y,z-SIZE/2.0,x+1-SIZE/2.0,y+1-FLOOR_Y,z+1-SIZE/2.0,RADIUS) && !state.method_26215() && state.method_26204() != ModBlocks.SOUL_MIST)
                    blocks.put(pos, state);
            }

        // A building block directly above an occupied source-floor cell was supported by that
        // floor. If the scanned real terrain is lower, extend only that footprint downward with
        // floor-pattern blocks until it reaches the first complete real support block.
        for (int x = 0; x < SIZE; x++) for (int z = 0; z < SIZE; z++) {
            class_2680 oldFloor = source.method_8320(new class_2338(x, FLOOR_Y, z));
            class_2680 base = source.method_8320(new class_2338(x, FLOOR_Y + 1, z));
            if (oldFloor.method_26215() || base.method_26215()
                    || base.method_26204() == ModBlocks.SOUL_MIST) continue;
            int support = supportBelow(destination, originX + x, originY, originZ + z, minY);
            if (support == Integer.MIN_VALUE) continue;
            class_2680 pillar = FloorPatternSampler.floorBlock(pattern.at(x, z));
            if (pillar.method_26215()) continue;
            for (int y = support - originY + 1; y <= FLOOR_Y; y++)
                if(OpenDomainRules.intersects(x-SIZE/2.0,y-FLOOR_Y,z-SIZE/2.0,x+1-SIZE/2.0,y+1-FLOOR_Y,z+1-SIZE/2.0,RADIUS))blocks.putIfAbsent(new class_2338(x, y, z), pillar);
        }

        ProjectionSourceSpec.Volume visible = new ProjectionSourceSpec.Volume(
                minX, minY, minZ, maxX, maxY, maxZ);
        ProjectionSourceSpec.Volume mirror = new ProjectionSourceSpec.Volume(
                0, FLOOR_Y + 1, 0, SIZE - 1, RoomGeometry.DOMAIN_CEILING - 1, SIZE - 1);
        ProjectionSourceSpec spec = new ProjectionSourceSpec(
                visible, null, mirror, SIZE / 2.0, FLOOR_Y, SIZE / 2.0);
        return new Generated(spec, Map.copyOf(blocks));
    }

    private static int supportBelow(class_3218 world, int worldX, int originY, int worldZ, int minLocalY) {
        for (int localY = FLOOR_Y; localY >= minLocalY; localY--) {
            int worldY = originY + localY;
            if (fullCube(world, new class_2338(worldX, worldY, worldZ))) return worldY;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean fullCube(class_3218 world, class_2338 pos) {
        return world.method_8320(pos).method_26212(world, pos);
    }

    private static boolean hidden(class_3218 world, class_2338 pos) {
        return fullCube(world, pos.method_10084()) && fullCube(world, pos.method_10074())
                && fullCube(world, pos.method_10095()) && fullCube(world, pos.method_10072())
                && fullCube(world, pos.method_10078()) && fullCube(world, pos.method_10067());
    }

}
