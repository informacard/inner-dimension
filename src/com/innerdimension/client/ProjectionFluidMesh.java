package com.innerdimension.client;

import com.innerdimension.projection.builtin.ShadedBlockSbbBuilder;
import com.innerdimension.projection.builtin.SuperByteBuffer;
import com.innerdimension.projection.builtin.world.VirtualRenderWorld;
import net.minecraft.*;
import java.util.*;

/** Cached vanilla liquid geometry. The virtual world supplies the full snapshot,
 * including neighbours outside the visible ring, without running world ticks. */
final class ProjectionFluidMesh {
    private final Map<class_11515, SuperByteBuffer> buffers = new EnumMap<>(class_11515.class);
    ProjectionFluidMesh(VirtualRenderWorld world, Iterable<class_2338> positions) {
        for (class_11515 layer : class_11515.values()) {
            ShadedBlockSbbBuilder builder = ShadedBlockSbbBuilder.create();
            builder.begin();
            for (class_2338 pos : positions) {
                class_2680 state = world.method_8320(pos);
                class_3610 fluid = state.method_26227();
                if (fluid.method_15769() || class_4696.method_23680(fluid) != layer) continue;
                // Vanilla emits section-local vertices; restore snapshot coordinates,
                // including negative positions and positions crossing section borders.
                builder.blockCenter(pos);
                class_4588 vertices = new Offset(builder.unwrap(false), pos.method_10263() & ~15,
                        pos.method_10264() & ~15, pos.method_10260() & ~15);
                class_310.method_1551().method_1541().method_3352(pos, world, vertices, state, fluid);
            }
            buffers.put(layer, builder.end().disableDiffuse());
        }
    }
    void append(List<com.innerdimension.projection.builtin.ContraptionEntityRenderer.ContraptionBlockLayer> layers,
                class_1937 world, org.joml.Matrix4f transform, float surfaceOffset) {
        buffers.forEach((layer, buffer) -> {
            if (!buffer.isEmpty()) layers.add(new com.innerdimension.projection.builtin.ContraptionEntityRenderer.ContraptionBlockLayer(
                    switch (layer) {
                        case field_60923 -> class_12249.method_75965();
                        case field_60925 -> class_12249.method_75972();
                        case field_60926 -> class_12249.method_75977();
                        case field_60927 -> class_12249.method_76009();
                    }, buffer, world, new org.joml.Matrix4f(transform), surfaceOffset));
        });
    }
    void release() { buffers.values().forEach(SuperByteBuffer::delete); buffers.clear(); }
    class_238 bounds() {
        class_238 result=null;
        for(var buffer:buffers.values())if(buffer.bounds()!=null)result=result==null?buffer.bounds():result.method_991(buffer.bounds());
        return result;
    }
    private record Offset(class_4588 out, int x, int y, int z) implements class_4588 {
        public class_4588 method_22912(float a,float b,float c) { out.method_22912(a+x,b+y,c+z); return this; }
        public class_4588 method_1336(int r,int g,int b,int a) { out.method_1336(r,g,b,a); return this; }
        public class_4588 method_39415(int c) { out.method_39415(c); return this; }
        public class_4588 method_22913(float u,float v) { out.method_22913(u,v); return this; }
        public class_4588 method_60796(int u,int v) { out.method_60796(u,v); return this; }
        public class_4588 method_22921(int u,int v) { out.method_22921(u,v); return this; }
        public class_4588 method_22914(float a,float b,float c) { out.method_22914(a,b,c); return this; }
        public class_4588 method_75298(float v) { out.method_75298(v); return this; }
    }
}
