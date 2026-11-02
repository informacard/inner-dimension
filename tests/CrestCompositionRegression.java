package com.innerdimension.tests;

import com.innerdimension.client.crest.CrestImageComposer;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import javax.imageio.ImageIO;

/** Verify the actual saved design changes only the intended pixels of a destroyed skin. */
public final class CrestCompositionRegression {
    public static void main(String[] args) throws Exception {
        Properties slots = new Properties();
        try (var in = Files.newInputStream(Path.of(args[0]))) { slots.load(in); }
        String selected = slots.getProperty("selected", "0");
        BufferedImage design = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder()
                .decode(slots.getProperty("slot." + selected))));
        byte[] original = Files.readAllBytes(Path.of(args[1]));
        byte[] untouched = original.clone();
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(original));
        for (int resolution : new int[]{256, 512}) {
            BufferedImage normalized = CrestImageComposer.normalizeDesign(design, resolution);
            byte[] result = CrestImageComposer.compose(UUID.randomUUID(), original, normalized,
                    "standard_hd", 3, 1);
            BufferedImage derived = ImageIO.read(new ByteArrayInputStream(result));
            check(derived.getWidth() == resolution && derived.getHeight() == resolution, "Output resolution");
            BufferedImage baseline = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = baseline.createGraphics();
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.drawImage(source, 0, 0, resolution, resolution, null);
            graphics.dispose();
            int x0 = CrestImageComposer.targetX(resolution), y0 = CrestImageComposer.targetY(resolution);
            int changed = 0;
            for (int y = 0; y < resolution; y++) for (int x = 0; x < resolution; x++) {
                int before = baseline.getRGB(x, y), after = derived.getRGB(x, y);
                if (before != after) {
                    check(x >= x0 && x < x0 + normalized.getWidth()
                            && y >= y0 && y < y0 + normalized.getHeight(), "Pixels outside crest must stay unchanged");
                    check((before >>> 24) != 0, "Transparent skin pixels must remain transparent");
                    changed++;
                }
            }
            check(changed > 0, "Saved crest must be visible in derived destroyed skin");
            System.out.println("PASS: saved crest changes " + changed + " target pixels at " + resolution);
        }
        check(Arrays.equals(original, untouched), "Original skin bytes must stay unchanged");
    }

    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }
}
