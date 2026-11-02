import com.innerdimension.client.domain.DomainOutlineRenderer;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import com.innerdimension.domain.runtime.AttributeLeaseManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

/** Runs without a world or GPU: uses the actual Minecraft line buffer and production lease merger. */
public final class DomainAbilityAuditRegression {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        VertexFormat format = class_290.field_63455;
        check(format.contains(VertexFormatElement.LINE_WIDTH), "Runtime line format requires width");
        // Reproduce the original crash, so the test proves it exercises the failing vertex format.
        try (class_9799 arena = new class_9799(4096)) {
            class_287 vertices = new class_287(arena, VertexFormat.class_5596.field_27377, format);
            vertices.method_22912(0, 0, 0).method_1336(255, 255, 255, 255).method_22914(1, 0, 0);
            try {
                vertices.method_22912(1, 0, 0);
                throw new AssertionError("Legacy line emission should fail");
            } catch (IllegalStateException expected) {
                check(expected.getMessage().contains("LineWidth"), "Reproduced LineWidth crash");
            }
        }
        Method draw = DomainOutlineRenderer.class.getDeclaredMethod("drawBox", class_4588.class, class_238.class, int.class);
        draw.setAccessible(true);
        try (class_9799 arena = new class_9799(4096)) {
            class_287 vertices = new class_287(arena, VertexFormat.class_5596.field_27377, format);
            draw.invoke(null, vertices, new class_238(-1, -2, -3, 2, 4, 6), 0x49E8FF);
            try (class_9801 mesh = vertices.method_60800()) {
                check(mesh != null, "Box produces mesh");
                ByteBuffer data = mesh.method_60818().order(ByteOrder.nativeOrder());
                int stride = format.getVertexSize();
                int normal = format.getOffset(VertexFormatElement.NORMAL);
                int width = format.getOffset(VertexFormatElement.LINE_WIDTH);
                Set<String> directions = new HashSet<>();
                for (int offset = 0; offset < data.remaining(); offset += stride) {
                    check(data.getFloat(offset + width) == 2.0f, "Every vertex has width");
                    int x = data.get(offset + normal), y = data.get(offset + normal + 1), z = data.get(offset + normal + 2);
                    check(Math.abs(x) + Math.abs(y) + Math.abs(z) == 127, "Each edge uses a unit axis direction");
                    directions.add(x != 0 ? "x" : y != 0 ? "y" : "z");
                }
                check(directions.size() == 3, "Horizontal and vertical edges have distinct directions");
            }
        }
        Method posedDraw = DomainOutlineRenderer.class.getDeclaredMethod("drawBox", class_4588.class, class_238.class, int.class, class_4587.class_4665.class);
        posedDraw.setAccessible(true);
        class_4587 pose = new class_4587();
        pose.method_22904(10, 20, 30);
        pose.method_22907(new org.joml.Quaternionf().rotationZ((float) (Math.PI / 2)));
        try (class_9799 arena = new class_9799(4096)) {
            class_287 vertices = new class_287(arena, VertexFormat.class_5596.field_27377, format);
            posedDraw.invoke(null, vertices, new class_238(0, 0, 0, 1, 2, 3), 0x49E8FF, pose.method_23760());
            try (class_9801 mesh = vertices.method_60800()) {
                ByteBuffer data = mesh.method_60818().order(ByteOrder.nativeOrder());
                int position = format.getOffset(VertexFormatElement.POSITION);
                int normal = format.getOffset(VertexFormatElement.NORMAL);
                check(data.getFloat(position) == 10 && data.getFloat(position + 4) == 20 && data.getFloat(position + 8) == 30,
                        "Queued pose transforms outline position");
                check(Math.abs(data.get(normal)) <= 1 && data.get(normal + 1) >= 126 && data.get(normal + 2) == 0,
                        "Queued pose rotates line direction with position");
            }
        }
        leases();
        attributes();
        irisPipeline();
        System.out.println("PASS: line buffer, directions, effect/attribute sources and expiry; actual installed Iris pipeline mapping");
    }

    @SuppressWarnings("unchecked")
    private static void leases() throws Exception {
        Class<?> entryType = Class.forName(EffectLeaseManager.class.getName() + "$Entry");
        Constructor<?> entryConstructor = entryType.getDeclaredConstructor();
        entryConstructor.setAccessible(true);
        Object entry = entryConstructor.newInstance();
        Field contributions = entryType.getDeclaredField("contributions");
        contributions.setAccessible(true);
        Map<Object, Object> sources = (Map<Object, Object>) contributions.get(entry);
        Class<?> sourceType = Class.forName(EffectLeaseManager.class.getName() + "$Source");
        Constructor<?> source = sourceType.getDeclaredConstructor(UUID.class, String.class);
        source.setAccessible(true);
        Class<?> contributionType = Class.forName(EffectLeaseManager.class.getName() + "$Contribution");
        Constructor<?> contribution = contributionType.getDeclaredConstructor(UUID.class, String.class, int.class, long.class);
        contribution.setAccessible(true);
        UUID session = UUID.randomUUID();
        sources.put(source.newInstance(session, "D33"), contribution.newInstance(session, "D33", 0, 300L));
        sources.put(source.newInstance(session, "D37"), contribution.newInstance(session, "D37", 1, 160L));
        check(sources.size() == 2, "Two abilities in one domain retain separate contributions");
        Method merge = EffectLeaseManager.class.getDeclaredMethod("merge", entryType, long.class);
        merge.setAccessible(true);
        Object active = merge.invoke(null, entry, 100L);
        check(read(active, "amplifier") == 1 && read(active, "duration") == 60, "Poison II does not inherit Poison I's duration");
        Object expired = merge.invoke(null, entry, 160L);
        check(read(expired, "amplifier") == 0 && read(expired, "duration") == 140, "Expired Poison II falls back to Poison I");
        check(sources.size() == 1, "Expired contribution removed");
        Field externalAmplifier = entryType.getDeclaredField("externalAmplifier");
        Field externalRemaining = entryType.getDeclaredField("externalRemaining");
        externalAmplifier.setAccessible(true);
        externalRemaining.setAccessible(true);
        externalAmplifier.setInt(entry, 3);
        externalRemaining.setLong(entry, 20L);
        Object external = merge.invoke(null, entry, 160L);
        check(read(external, "amplifier") == 3 && read(external, "duration") == 20, "Stronger external effect keeps its own duration");
        externalRemaining.setLong(entry, 0L);
        check(read(merge.invoke(null, entry, 160L), "amplifier") == 0, "Expired external effect cannot remain strongest");
        merge.invoke(null, entry, 300L);
        check(sources.isEmpty(), "All unrefreshed effects expire");
    }

    private static int read(Object record, String methodName) throws Exception {
        Method method = record.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (int) method.invoke(record);
    }

    private static void attributes() throws Exception {
        Class<?> sourceType = Class.forName(AttributeLeaseManager.class.getName() + "$Source");
        Constructor<?> source = sourceType.getDeclaredConstructor(UUID.class, String.class);
        source.setAccessible(true);
        Class<?> leaseType = Class.forName(AttributeLeaseManager.class.getName() + "$Lease");
        Constructor<?> lease = leaseType.getDeclaredConstructors()[0];
        lease.setAccessible(true);
        UUID session = UUID.randomUUID(), other = UUID.randomUUID();
        Object d47 = source.newInstance(session, "D47"), d67 = source.newInstance(session, "D67");
        Object overlap = source.newInstance(other, "D47");
        Map<Object, Object> sources = new LinkedHashMap<>();
        Object slow = lease.newInstance(session, "D47", null, null, -0.3, null);
        Object duplicate = lease.newInstance(session, "D67", null, null, -0.3, null);
        Object weaker = lease.newInstance(other, "D47", null, null, -0.1, null);
        sources.put(d47, slow);
        sources.put(d67, duplicate);
        sources.put(overlap, weaker);
        Method strongest = AttributeLeaseManager.class.getDeclaredMethod("strongest", Map.class);
        strongest.setAccessible(true);
        check(strongest.invoke(null, sources) == slow, "Shared boss slow chooses one contribution without stacking");
        sources.remove(d47);
        check(strongest.invoke(null, sources) == duplicate, "Removing D47 leaves D67's slow active");
        sources.remove(d67);
        check(strongest.invoke(null, sources) == weaker, "Ending one domain preserves an overlapping domain");
        sources.remove(overlap);
        check(strongest.invoke(null, sources) == null, "Last source removal withdraws attribute");
    }

    private static void irisPipeline() throws Exception {
        Method build = DomainOutlineRenderer.class.getDeclaredMethod("buildPipeline");
        build.setAccessible(true);
        Object pipeline = build.invoke(null);
        Class<?> iris = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines");
        for (String fieldName : List.of("coreShaderMap", "coreShaderMapShadow")) {
            Field field = iris.getDeclaredField(fieldName);
            field.setAccessible(true);
            Map<?, ?> map = (Map<?, ?>) field.get(null);
            Object vanilla = map.get(class_10799.field_56833);
            if (fieldName.equals("coreShaderMap")) check(vanilla != null, "Iris has vanilla line shader mapping");
            check(map.get(pipeline) == vanilla, "Custom lines inherit Iris mapping for " + fieldName);
        }
    }
}
