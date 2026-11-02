package com.innerdimension.client;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2350;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_630;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The plushie player-head shape from non_player_plushie's built-in resource pack
 * ({@code assets/minecraft/optifine/cem/head_player.jem}), rebuilt as a vanilla model so a
 * custom entity can wear the same silhouette.
 *
 * <p>The pack is an OptiFine CEM model, which only ever applies to vanilla player heads; a
 * modded entity gets no share of it. This class therefore re-creates the very same model
 * from the same numbers, following exactly what EMF (entity_model_features) does when it
 * turns a {@code .jem} into model parts:
 *
 * <ul>
 *   <li>{@code coordinates} are {@code [x, y, z, sizeX, sizeY, sizeZ]} in the CEM's
 *       Blockbench-like space, so {@code invertAxis: "xy"} flips them into vanilla model
 *       space with {@code x = -x - sizeX} and {@code y = -y - sizeY};</li>
 *   <li>a part's {@code translate} becomes its pivot (also inverted) and its {@code rotate}
 *       becomes the part's pitch/yaw/roll in radians;</li>
 *   <li>boxes with {@code textureOffset} use the standard box UV layout, boxes with explicit
 *       per-face UVs get their six quads built by hand, because hand-scaled UV islands cannot
 *       be expressed as a texture offset.</li>
 * </ul>
 *
 * <p>The jem's own hierarchy is kept: the vanilla {@code head} part (kept for the doll's
 * facing rotation) holds a {@code plushie} part, which holds the head, hat and body boxes plus
 * four limb parts. The {@code plushie} part sits at {@code y = -8}, which is what lifts the
 * whole doll so that, exactly like a plushie head block, it stands on the bottom of its
 * block and fills it.</p>
 */
public final class SensoryLinkDollModel {
    private static final float TEX_WIDTH = 64.0F;
    private static final float TEX_HEIGHT = 64.0F;
    private static final Set<class_2350> ALL_FACES = EnumSet.allOf(class_2350.class);
    private static final float RADIANS_PER_DEGREE = (float) (Math.PI / 180.0);

    private final class_630 root;
    private final class_630 head;

    public SensoryLinkDollModel() {
        class_630 plushie = new class_630(
                List.of(
                        // head cube, standard 8x8x8 head UV layout
                        offsetBox(0, 0, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F),
                        // hat / second layer, half a pixel larger
                        offsetBox(32, 0, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F),
                        // torso, hand-scaled UV islands
                        faceBox(-3.0F, 0.0F, -1.5F, 6.0F, 7.0F, 3.0F,
                                uv(35, 17, 29, 20), uv(27, 19, 21, 16),
                                uv(21, 21, 27, 28), uv(33, 21, 39, 28),
                                uv(29, 21, 32, 28), uv(18, 21, 21, 28), false, false)),
                Map.of(
                        "bone3", limbBox(
                                -1.0F, -0.5F, -1.0F, 3.0F, 8.0F, 3.0F,
                                uv(43, 49, 40, 52), uv(39, 51, 36, 48),
                                uv(36, 52, 39, 60), uv(43, 52, 46, 60),
                                uv(38, 52, 41, 60), uv(33, 52, 36, 60), false, false,
                                3.5F, 0.5F, 0.0F, 0.0F, 5.0F, -7.5F),
                        "bone4", limbBox(
                                -2.0F, -0.5F, -1.0F, 3.0F, 8.0F, 3.0F,
                                uv(49, 17, 52, 20), uv(44, 20, 47, 17),
                                uv(54, 20, 51, 28), uv(47, 20, 44, 28),
                                uv(50, 20, 47, 28), uv(43, 20, 40, 28), true, false,
                                -3.5F, 0.5F, 0.0F, 0.0F, 0.0F, 7.5F),
                        "bone2", limbBox(
                                -1.5F, -0.5F, -3.0F, 3.0F, 8.0F, 3.0F,
                                uv(28, 49, 25, 52), uv(23, 51, 20, 48),
                                uv(17, 53, 20, 61), uv(25, 53, 28, 61),
                                uv(20, 53, 23, 61), uv(29, 53, 32, 61), false, false,
                                1.5F, 5.0F, -1.0F, -90.0F, 17.5F, 180.0F),
                        "bone5", limbBox(
                                -1.5F, -0.5F, -3.0F, 3.0F, 8.0F, 3.0F,
                                uv(8, 17, 11, 20), uv(4, 19, 7, 16),
                                uv(11, 22, 8, 30), uv(3, 21, 0, 29),
                                uv(8, 22, 5, 30), uv(16, 21, 13, 29), false, false,
                                -1.5F, 5.0F, -1.0F, -90.0F, -17.5F, 180.0F)));
        plushie.method_2851(0.0F, -8.0F, 0.0F);
        rememberPose(plushie);

        this.head = new class_630(List.of(), Map.of("plushie", plushie));
        rememberPose(this.head);
        this.root = new class_630(List.of(), Map.of("head", this.head));
        rememberPose(this.root);
    }

    public class_630 root() {
        return this.root;
    }

    /** Points the doll's head, the same way a floor player head uses its block rotation. */
    public void setHeadYaw(float degrees) {
        this.head.method_33425(0.0F, degrees * RADIANS_PER_DEGREE, 0.0F);
    }

    // ------------------------------------------------------------------ builders

    /** A box using the standard box UV layout, as vanilla's own model builder would make it. */
    private static class_630.class_628 offsetBox(int u, int v, float x, float y, float z,
                                                float sizeX, float sizeY, float sizeZ, float dilation) {
        return new class_630.class_628(u, v, x, y, z, sizeX, sizeY, sizeZ,
                dilation, dilation, dilation, false, TEX_WIDTH, TEX_HEIGHT, ALL_FACES);
    }

    /** A box with explicit per-face UV islands plus its own part transform. */
    private static class_630 limbBox(float x, float y, float z, float sizeX, float sizeY, float sizeZ,
                                     float[] uvDown, float[] uvUp, float[] uvNorth, float[] uvSouth,
                                     float[] uvWest, float[] uvEast, boolean mirrorU, boolean mirrorV,
                                     float pivotX, float pivotY, float pivotZ,
                                     float pitch, float yaw, float roll) {
        class_630 part = new class_630(List.of(
                faceBox(x, y, z, sizeX, sizeY, sizeZ, uvDown, uvUp, uvNorth, uvSouth,
                        uvWest, uvEast, mirrorU, mirrorV)),
                Map.of());
        part.method_2851(pivotX, pivotY, pivotZ);
        part.method_33425(pitch * RADIANS_PER_DEGREE, yaw * RADIANS_PER_DEGREE, roll * RADIANS_PER_DEGREE);
        rememberPose(part);
        return part;
    }

    private static class_630.class_628 faceBox(float x, float y, float z, float sizeX, float sizeY, float sizeZ,
                                               float[] uvDown, float[] uvUp, float[] uvNorth, float[] uvSouth,
                                               float[] uvWest, float[] uvEast, boolean mirrorU, boolean mirrorV) {
        return new FaceBox(x, y, z, sizeX, sizeY, sizeZ,
                uvDown, uvUp, uvNorth, uvSouth, uvWest, uvEast, mirrorU, mirrorV);
    }

    private static float[] uv(float u1, float v1, float u2, float v2) {
        return new float[]{u1, v1, u2, v2};
    }

    /** Keeps a part's reset transform in step with the pose we just gave it. */
    private static void rememberPose(class_630 part) {
        part.method_41918(part.method_32084());
    }

    /**
     * A cuboid whose six quads are given explicitly instead of being laid out from a texture
     * offset, which is how CEM's per-face UVs are expressed.
     *
     * <p>Vanilla's {@code class_628} has no entry point for that, so the vertices are built here
     * and rendered the same way {@code class_628.method_32089} renders its own; only the quads
     * differ. Face vertex order and UV corner order are copied from EMF's CEM cube so the doll
     * comes out exactly as the resource pack renders player heads.</p>
     */
    private static final class FaceBox extends class_630.class_628 {
        private final class_630.class_593[] faces;

        FaceBox(float x, float y, float z, float sizeX, float sizeY, float sizeZ,
                float[] uvDown, float[] uvUp, float[] uvNorth, float[] uvSouth,
                float[] uvWest, float[] uvEast, boolean mirrorU, boolean mirrorV) {
            super(0, 0, x, y, z, sizeX, sizeY, sizeZ, 0.0F, 0.0F, 0.0F,
                    false, TEX_WIDTH, TEX_HEIGHT, ALL_FACES);

            float x2 = x + sizeX;
            float y2 = y + sizeY;
            float z2 = z + sizeZ;
            class_630.class_618[] v = vertices(x, y, z, x2, y2, z2);
            this.faces = new class_630.class_593[]{
                    quad(mirrorV ? new class_630.class_618[]{v[7], v[6], v[2], v[3]}
                                 : new class_630.class_618[]{v[0], v[1], v[5], v[4]},
                            uvUp, mirrorU, mirrorV, mirrorV ? class_2350.field_11036 : class_2350.field_11033),
                    quad(mirrorV ? new class_630.class_618[]{v[0], v[1], v[5], v[4]}
                                 : new class_630.class_618[]{v[7], v[6], v[2], v[3]},
                            uvDown, mirrorU, mirrorV, mirrorV ? class_2350.field_11033 : class_2350.field_11036),
                    quad(mirrorU ? new class_630.class_618[]{v[0], v[4], v[7], v[3]}
                                 : new class_630.class_618[]{v[5], v[1], v[2], v[6]},
                            uvWest, mirrorU, mirrorV, mirrorU ? class_2350.field_11039 : class_2350.field_11034),
                    quad(new class_630.class_618[]{v[1], v[0], v[3], v[2]},
                            uvNorth, mirrorU, mirrorV, class_2350.field_11043),
                    quad(mirrorU ? new class_630.class_618[]{v[5], v[1], v[2], v[6]}
                                 : new class_630.class_618[]{v[0], v[4], v[7], v[3]},
                            uvEast, mirrorU, mirrorV, mirrorU ? class_2350.field_11034 : class_2350.field_11039),
                    quad(new class_630.class_618[]{v[4], v[5], v[6], v[7]},
                            uvSouth, mirrorU, mirrorV, class_2350.field_11035),
            };
        }

        private static class_630.class_618[] vertices(float x, float y, float z,
                                                      float x2, float y2, float z2) {
            return new class_630.class_618[]{
                    new class_630.class_618(x, y, z, 0.0F, 0.0F),
                    new class_630.class_618(x2, y, z, 0.0F, 8.0F),
                    new class_630.class_618(x2, y2, z, 8.0F, 8.0F),
                    new class_630.class_618(x, y2, z, 8.0F, 0.0F),
                    new class_630.class_618(x, y, z2, 0.0F, 0.0F),
                    new class_630.class_618(x2, y, z2, 0.0F, 8.0F),
                    new class_630.class_618(x2, y2, z2, 8.0F, 8.0F),
                    new class_630.class_618(x, y2, z2, 8.0F, 0.0F),
            };
        }

        /**
         * {@code class_593} maps its four UV numbers onto the four vertices as
         * {@code (C,B), (A,B), (A,D), (C,D)}, so the caller decides the corner order.
         */
        private static class_630.class_593 quad(class_630.class_618[] vertices, float[] uv,
                                                boolean mirrorU, boolean mirrorV, class_2350 facing) {
            return new class_630.class_593(vertices,
                    mirrorU ? uv[2] : uv[0],
                    mirrorV ? uv[3] : uv[1],
                    mirrorU ? uv[0] : uv[2],
                    mirrorV ? uv[1] : uv[3],
                    TEX_WIDTH, TEX_HEIGHT, false, facing);
        }

        @Override
        public void method_32089(class_4587.class_4665 pose, class_4588 vertices, int light, int overlay, int color) {
            Matrix4f matrix = pose.method_23761();
            Vector3f scratch = new Vector3f();
            for (class_630.class_593 face : this.faces) {
                Vector3f normal = pose.method_56821(face.comp_3185(), scratch);
                // Read the normal out before the buffer is reused for the positions, exactly
                // like vanilla's own class_628 renderer does; holding on to the Vector3f would
                // hand the vertex positions out as normals.
                float normalX = normal.x();
                float normalY = normal.y();
                float normalZ = normal.z();
                for (class_630.class_618 vertex : face.comp_3184()) {
                    Vector3f transformed = matrix.transformPosition(
                            vertex.method_74250(), vertex.method_74251(), vertex.method_74252(), scratch);
                    vertices.method_23919(transformed.x(), transformed.y(), transformed.z(), color,
                            vertex.comp_3187(), vertex.comp_3188(), overlay, light,
                            normalX, normalY, normalZ);
                }
            }
        }
    }
}
