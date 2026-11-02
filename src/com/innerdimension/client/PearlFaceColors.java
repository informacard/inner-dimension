package com.innerdimension.client;

import com.innerdimension.InnerDimension;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplies the per-position tint for the {@code pearl_face} floor.
 *
 * <p>The source image {@code textures/block/pearl_face_map.png} is a 16x16 picture that
 * is only used as a color lookup table. The actual block texture is plain white, and the
 * tint paints one pixel of the map onto each floor position, so the 16x16 floor shows the
 * whole picture (design doc section 6).</p>
 */
public class PearlFaceColors implements SimpleSynchronousResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("inner_dimension");
    private static final class_2960 MAP_ID = InnerDimension.id("textures/block/pearl_face_map.png");
    private static final int[] COLORS = new int[256];

    static {
        Arrays.fill(COLORS, 0xFFFFFFFF);
    }

    @Override
    public class_2960 getFabricId() {
        return InnerDimension.id("pearl_face_colors");
    }

    @Override
    public void method_14491(class_3300 manager) {
        try (InputStream in = manager.open(MAP_ID)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                LOGGER.warn("[InnerDimension] pearl_face map is not a readable image: {}", MAP_ID);
                return;
            }
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int argb = image.getRGB(
                            Math.min(x, image.getWidth() - 1),
                            Math.min(z, image.getHeight() - 1));
                    COLORS[z * 16 + x] = 0xFF000000 | (argb & 0xFFFFFF);
                }
            }
            LOGGER.info("[InnerDimension] pearl_face color map loaded ({}x{})",
                    image.getWidth(), image.getHeight());
        } catch (Exception e) {
            LOGGER.warn("[InnerDimension] failed to load pearl_face color map", e);
        }
    }

    /** Block color provider: returns the sampled pixel for this floor position. */
    public static int getColor(class_2680 state, class_1920 view, class_2338 pos, int tintIndex) {
        // Vanilla also consults block colors for the item form, where pos is null.
        if (pos == null) {
            return 0xFFFFFFFF;
        }
        int x = pos.method_10263() & 15;
        int z = pos.method_10260() & 15;
        return COLORS[z * 16 + x];
    }
}
