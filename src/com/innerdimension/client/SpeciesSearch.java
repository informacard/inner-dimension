package com.innerdimension.client;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import net.minecraft.*;

/** Search both language resources, including translations supplied by mods and packs. */
final class SpeciesSearch {
    private final Map<String, String> names = new HashMap<>();

    SpeciesSearch(Collection<String> ids) {
        if (ids.isEmpty()) return;
        var resources = class_310.method_1551().method_1478();
        Set<String> keys = new HashSet<>();
        for (String id : ids) keys.add(key(id));
        for (String namespace : resources.method_14487()) {
            for (String language : List.of("en_us", "zh_cn")) {
                var resourceId = class_2960.method_60655(namespace, "lang/" + language + ".json");
                Map<String, String> translations = new HashMap<>();
                for (var resource : resources.method_14489(resourceId)) {
                    try (Reader reader = resource.method_43039()) {
                        var element = JsonParser.parseReader(reader);
                        if (!element.isJsonObject()) continue;
                        var json = element.getAsJsonObject();
                        for (String key : keys) if (json.has(key) && json.get(key).isJsonPrimitive())
                            translations.put(key, json.get(key).getAsString());
                    } catch (IOException | RuntimeException ex) {
                        // Some bundled packs contain empty or malformed language files;
                        // vanilla skips those too. Other namespaces still supply names.
                        org.slf4j.LoggerFactory.getLogger("inner_dimension").debug("Skipping invalid species search translations from {}", resourceId, ex);
                    }
                }
                translations.forEach((key, value) -> names.merge(key, value, (a, b) -> a + " " + b));
            }
        }
    }

    boolean matches(String id, String query) {
        String text = (id + " " + id.replace('_', ' ') + " " + class_2561.method_43471(key(id)).getString()
                + " " + names.getOrDefault(key(id), "")).toLowerCase(Locale.ROOT);
        return text.contains(query.strip().toLowerCase(Locale.ROOT));
    }
    private static String key(String id) { return "entity." + id.replace(':', '.').replace('/', '.'); }
}

