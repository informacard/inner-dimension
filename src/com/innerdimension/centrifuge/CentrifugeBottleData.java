package com.innerdimension.centrifuge;

import com.nonid.potion.NonPotions;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_9279;
import net.minecraft.class_9334;

/** Compatibility data stored in vanilla custom-data, so NoN can still consume the bottles. */
public final class CentrifugeBottleData {
    public static final String SOURCES_KEY = "inner_dimension_centrifuge_sources";
    public static final String VOLUME_KEY = "inner_dimension_centrifuge_volume_ml";
    public static final int LEGACY_BOTTLE_ML = 60;

    public record Contents(Map<class_2960, Integer> sources, int totalMl, boolean provenanceKnown) {}

    private CentrifugeBottleData() {}

    public static void write(class_1799 stack, Map<class_2960, Integer> sources, int totalMl) {
        if (stack == null || stack.method_7960() || sources == null || sources.isEmpty() || totalMl <= 0) return;
        StringBuilder encoded = new StringBuilder();
        for (var entry : sources.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) continue;
            if (!encoded.isEmpty()) encoded.append(';');
            encoded.append(entry.getKey()).append('=').append(entry.getValue());
        }
        if (encoded.isEmpty()) return;
        class_9279.method_57452(class_9334.field_49628, stack, nbt -> {
            nbt.method_10582(SOURCES_KEY, encoded.toString());
            nbt.method_10569(VOLUME_KEY, totalMl);
        });
    }

    public static Contents read(class_1799 stack) {
        if(com.innerdimension.item.CreativeSemenBottle.isBottle(stack)){
            Map<class_2960,Integer> sources=new LinkedHashMap<>();
            for(var id:com.innerdimension.item.CreativeSemenBottle.sources())sources.put(id,LEGACY_BOTTLE_ML);
            return new Contents(Map.copyOf(sources),LEGACY_BOTTLE_ML,!sources.isEmpty());
        }
        if (stack == null || stack.method_7960() || !(NonPotions.isLiquidBottle(stack)||stack.method_31574(net.minecraft.class_1802.field_20417))) {
            return new Contents(Map.of(), 0, false);
        }
        class_9279 custom = (class_9279) stack.method_58694(class_9334.field_49628);
        if (custom != null && !custom.method_57458()) {
            class_2487 nbt = custom.method_57461();
            String raw = nbt.method_68564(SOURCES_KEY, "");
            int total = nbt.method_68083(VOLUME_KEY, 0);
            Map<class_2960, Integer> parsed = decode(raw);
            if (!parsed.isEmpty() && total > 0) return new Contents(Map.copyOf(parsed), total, true);
        }
        class_2960 entity = stack.method_31574(net.minecraft.class_1802.field_20417)?class_2960.method_60654("minecraft:bee"):NonPotions.getLiquidBottleEntityTypeId(stack);
        if (entity != null) {
            return new Contents(Map.of(entity, LEGACY_BOTTLE_ML), LEGACY_BOTTLE_ML, true);
        }
        return new Contents(Map.of(), 0, false);
    }

    public static class_1799 createPureBottle(class_2960 entityId) {
        try {
            Class<?> type = Class.forName("com.nonid.NonItemSystem");
            Method method = type.getDeclaredMethod("createLiquidBottleStack", class_2960.class);
            method.setAccessible(true);
            return (class_1799) method.invoke(null, entityId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create a NoN liquid bottle", e);
        }
    }

    public static class_1799 namedPureBottle(class_2960 entityId, int availableMl) {
        class_1799 stack = createPureBottle(entityId);
        stack.method_57379(class_9334.field_49631,
                class_2561.method_43469("container.inner_dimension.centrifuge.source",
                        class_2561.method_43471(entityTranslationKey(entityId)), availableMl));
        return stack;
    }

    public static String entityTranslationKey(class_2960 id) {
        return id == null ? "entity.minecraft.unknown" : "entity." + id.method_12836() + "." + id.method_12832().replace('/', '.');
    }

    public static String displayName(class_2960 id) {
        if (id == null) return "Unknown";
        String path = id.method_12832().replace('/', '_');
        StringBuilder out = new StringBuilder();
        for (String part : path.split("_")) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.isEmpty() ? id.toString() : out.toString();
    }

    private static Map<class_2960, Integer> decode(String raw) {
        return decodeSources(raw);
    }

    public record Storage(Map<class_2960, Integer> sources, int bottleMl, String status) {}

    public static void writeStorage(class_1799 state, Map<class_2960, Integer> sources, int bottleMl, String status) {
        class_9279.method_57452(class_9334.field_49628, state, nbt -> {
            nbt.method_10582(SOURCES_KEY, encodeSources(sources));
            nbt.method_10569(VOLUME_KEY, bottleMl);
            nbt.method_10582("inner_dimension_centrifuge_status", status);
        });
    }

    public static Storage readStorage(class_1799 state) {
        class_9279 custom = (class_9279)state.method_58694(class_9334.field_49628);
        if (custom == null) return new Storage(Map.of(), LEGACY_BOTTLE_ML, "");
        class_2487 nbt = custom.method_57461();
        return new Storage(Map.copyOf(decodeSources(nbt.method_68564(SOURCES_KEY, ""))),
                Math.max(1, nbt.method_68083(VOLUME_KEY, LEGACY_BOTTLE_ML)),
                nbt.method_68564("inner_dimension_centrifuge_status", ""));
    }

    public static String encodeSources(Map<class_2960, Integer> sources) {
        StringBuilder encoded = new StringBuilder();
        sources.entrySet().stream().sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(Object::toString))).forEach(entry -> {
            if (!encoded.isEmpty()) encoded.append(';');
            encoded.append(entry.getKey()).append('=').append(entry.getValue());
        });
        return encoded.toString();
    }

    public static Map<class_2960, Integer> decodeSources(String raw) {
        Map<class_2960, Integer> out = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return out;
        for (String token : raw.split(";")) {
            int split = token.lastIndexOf('=');
            if (split <= 0) continue;
            class_2960 id = class_2960.method_12829(token.substring(0, split));
            try {
                int amount = Integer.parseInt(token.substring(split + 1));
                if (id != null && amount > 0) out.merge(id, amount, Integer::sum);
            } catch (NumberFormatException ignored) {}
        }
        return out;
    }
}
