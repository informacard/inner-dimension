package com.innerdimension.dimension;

import com.innerdimension.block.ModBlocks;
import com.innerdimension.state.InnerState;
import net.minecraft.class_2338;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.Map;
import java.util.WeakHashMap;
import com.innerdimension.chunk.TemporaryChunkLoads;

/**
 * Lower room: interior 0..15, pearl floor -1, abyss ceiling 16.
 * Abyss passage: interior Y=17..30, abyss walls and end layers 16/31.
 * Domain: shell (-1,31,-1)..(16,48,16), interior 0..15 / 32..47 / 0..15.
 * Layout upgrades preserve room contents and replace only generated shell blocks.
 */
public class RoomShellGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");

    /** Persisted shell migration marker. */
    public static final int LAYOUT_VERSION = 7;
    private static final Map<class_3218, Integer> VERIFIED = new WeakHashMap<>();

    private RoomShellGenerator() {
    }

    public static void generateIfNeeded(class_3218 innerWorld, UUID owner) {
        // InnerState lives on the overworld.
        class_3218 overworld = innerWorld.method_8503().method_30002();
        InnerState state = InnerState.get(overworld);
        int oldLayout = state.getLayout(owner);
        if (oldLayout == LAYOUT_VERSION && VERIFIED.getOrDefault(innerWorld, 0) == LAYOUT_VERSION) {
            return;
        }

        // Outer box is -1..edge (edge = size - 2), so the hollow interior is exactly
        // 0..edge-1 -> for the default size 18 that is 0..15, matching the design doc.
        int edge = RoomGeometry.INTERIOR_SIZE;
        // Every entry point (including an already live world) must load ALL shell chunks.
        for (int cx = -1; cx <= edge >> 4; cx++) {
            for (int cz = -1; cz <= edge >> 4; cz++) {
                TemporaryChunkLoads.holdChunk(innerWorld, cx, cz);
                if (innerWorld.method_14178().method_12121(cx, cz,
                        net.minecraft.class_2806.field_12803, true) == null)
                    throw new IllegalStateException("Shell chunk unavailable: " + cx + "," + cz);
            }
        }

        class_2680 pearlState = ModBlocks.PEARL_FACE.method_9564();
        class_2680 intestinesState = ModBlocks.INTESTINES_BLOCK.method_9564();
        class_2680 airState = class_2248.method_9531(0);
        class_2680 abyssState = ModBlocks.ABYSS_BLOCK.method_9564();

        // Versions 2 and 3 already have the lower room: preserve contents on upgrade.
        if (oldLayout < 2 && !state.isShellGenerated(owner)) {
            // Wipe the whole volume first: on a re-generation the old floor/walls would
            // otherwise survive inside the new room.
            for (int y = -1; y <= edge; y++) {
                for (int x = -1; x <= edge; x++) {
                    for (int z = -1; z <= edge; z++) {
                        setBlock(innerWorld, x, y, z, airState);
                    }
                }
            }

        }

            // Reconcile the lower shell on upgrades too; never clear an existing interior.
            // Floor y=-1: pearl_face inside, intestines on the border ring.
            for (int x = -1; x <= edge; x++) {
                for (int z = -1; z <= edge; z++) {
                    boolean border = x == -1 || z == -1 || x == edge || z == edge;
                    setBlock(innerWorld, x, -1, z, border ? intestinesState : pearlState);
                }
            }

            // Walls y=0..edge-1: border ring only.
            for (int y = 0; y < edge; y++) {
                for (int x = -1; x <= edge; x++) {
                    for (int z = -1; z <= edge; z++) {
                        if (x == -1 || z == -1 || x == edge || z == edge) {
                            setBlock(innerWorld, x, y, z, intestinesState);
                        }
                    }
                }
            }

        // Version 3's roof is now inside the relocated domain. Remove its generated
        // intestinal blocks only; keep player builds in both existing interiors.
        if (oldLayout == 3) {
            int oldRoof = 2 * edge + 1;
            for (int x = 0; x < edge; x++) {
                for (int z = 0; z < edge; z++) {
                    class_2338 pos = new class_2338(x, oldRoof, z);
                    if (innerWorld.method_8320(pos).method_26204() == ModBlocks.INTESTINES_BLOCK) {
                        setBlock(innerWorld, x, oldRoof, z, airState);
                    }
                }
            }
        }

        // Convert the middle shell to abyss, including the lower room's ceiling
        // and the domain's floor. The passage is fourteen blocks tall inside.
        for (int y = RoomGeometry.INNER_CEILING; y <= RoomGeometry.DOMAIN_FLOOR; y++) {
            for (int x = -1; x <= edge; x++) {
                for (int z = -1; z <= edge; z++) {
                    if (y == RoomGeometry.INNER_CEILING || y == RoomGeometry.DOMAIN_FLOOR
                            || x == -1 || z == -1 || x == edge || z == edge) {
                        setBlock(innerWorld, x, y, z, abyssState);
                    }
                }
            }
        }

        // The entire domain shell is abyss; its floor is shared with the passage.
        for (int y = RoomGeometry.DOMAIN_FLOOR + 1; y <= RoomGeometry.DOMAIN_CEILING; y++) {
            for (int x = -1; x <= edge; x++) {
                for (int z = -1; z <= edge; z++) {
                    if (y == RoomGeometry.DOMAIN_CEILING
                            || x == -1 || z == -1 || x == edge || z == edge) {
                        setBlock(innerWorld, x, y, z, abyssState);
                    }
                }
            }
        }

        // Add skins once on migration, preserving builds and later player replacements.
        if (oldLayout < 7) {
            for (int x = 0; x < edge; x++) for (int z = 0; z < edge; z++) {
                class_2338 floor = new class_2338(x, RoomGeometry.DOMAIN_FLOOR + 1, z);
                class_2338 roof = new class_2338(x, RoomGeometry.DOMAIN_CEILING - 1, z);
                if (innerWorld.method_8320(floor).method_26215())
                    setBlock(innerWorld, x, floor.method_10264(), z, ModBlocks.FLESH_WATER.method_9564());
                if (innerWorld.method_8320(roof).method_26215())
                    setBlock(innerWorld, x, roof.method_10264(), z, ModBlocks.SOUL_MIST.method_9564());
            }
        }
        state.setLayout(owner, LAYOUT_VERSION);
        VERIFIED.put(innerWorld, LAYOUT_VERSION);
        LOGGER.info("[InnerDimension] Verified room shell for {}: layout {} -> {}", owner, oldLayout, LAYOUT_VERSION);
    }

    private static void setBlock(class_3218 world, int x, int y, int z, class_2680 state) {
        class_2338 pos = new class_2338(x, y, z);
        if (world.method_8320(pos).equals(state)) return;
        world.method_8652(pos, state, 3);
        // A false write result must not become a persisted 'migration succeeded' marker.
        if (!world.method_8320(pos).equals(state))
            throw new IllegalStateException("Shell write did not persist at " + pos);
    }

    /**
     * Safe entry point for being called from a tick handler: server side only,
     * swallows every throwable.
     */
    public static void ensureShell(class_3218 innerWorld, UUID owner) {
        if (innerWorld == null || owner == null) {
            return;
        }
        try {
            if (innerWorld.method_8503() == null || !innerWorld.method_8503().method_5384()) {
                return;
            }
            generateIfNeeded(innerWorld, owner);
        } catch (Throwable throwable) {
            LOGGER.warn("[InnerDimension] Failed to ensure room shell for {}", owner, throwable);
        }
    }
}
