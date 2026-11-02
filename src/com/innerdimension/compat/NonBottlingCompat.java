package com.innerdimension.compat;

import com.nonid.NonAccessoryEffects;
import com.nonid.data.NonAccessoryBehavior;
import com.nonid.data.NonAccessoryItemRegistry;
import com.nonid.data.NonAccessoryItemRegistry.AccessoryItemDefinition;
import com.nonid.data.NonAccessoryItemRegistry.ActorVisualBehavior;
import com.nonid.data.NonAccessoryItemRegistry.ItemClientModel;
import com.nonid.data.NonAccessoryItemRegistry.SkinOverlays;
import com.nonid.data.NonAccessoryItemRegistry.TintMode;
import com.nonid.data.NonAccessoryItemRegistry.VisualLayer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge to the parts of NoN that are either package-private or only reachable through its
 * data-driven accessory registry.
 *
 * <p>NoN registers its data-driven accessories from pack JSON (namespace {@code needsofnature}
 * only) before dependent mods initialise, so an addon cannot ship such a JSON for its own item.
 * Its definitions map, however, is what every effect/overlay/tooltip lookup reads, so the addon
 * installs its own entry there. That is what makes the auto-bottling plug behave exactly like the
 * iron plug instead of merely imitating it.
 */
public final class NonBottlingCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("InnerDimension");

    private static final String NAMESPACE = "inner_dimension";
    private static final class_2960 IRON_PLUG = class_2960.method_60655("needsofnature", "iron_plug");

    private static Method createBottleForTank;
    private static Method createBottleForEntity;
    private static Method giveItemOrDrop;
    private static Method hasMaleGender;
    private static boolean resolved;

    private NonBottlingCompat() {}

    /** NoN's own bottle stack for whatever the tank currently holds (mixed / entity / honey / nectar). */
    public static class_1799 bottleForTank(class_1309 holder) {
        Method method = createBottleForTank();
        if (method == null || holder == null) return class_1799.field_8037;
        try {
            Object result = method.invoke(null, holder);
            return result instanceof class_1799 stack ? stack : class_1799.field_8037;
        } catch (ReflectiveOperationException failure) {
            LOGGER.warn("NoN tank bottling failed", failure);
            return class_1799.field_8037;
        }
    }

    /** NoN's own bottle stack for one species id ({@code null} means a mixed bottle). */
    public static class_1799 bottleForEntity(class_2960 entityTypeId) {
        Method method = createBottleForEntity();
        if (method == null) return class_1799.field_8037;
        try {
            Object result = method.invoke(null, entityTypeId);
            return result instanceof class_1799 stack ? stack : class_1799.field_8037;
        } catch (ReflectiveOperationException failure) {
            LOGGER.warn("NoN bottle creation failed for {}", entityTypeId, failure);
            return class_1799.field_8037;
        }
    }

    /** NoN's own "put it in the inventory, drop it if it does not fit" helper. */
    public static void giveOrDrop(class_3222 player, class_1799 stack) {
        Method method = giveOrDrop();
        if (method != null) {
            try {
                method.invoke(null, player, stack);
                return;
            } catch (ReflectiveOperationException failure) {
                LOGGER.warn("NoN bottle hand-off failed, falling back to a direct insert", failure);
            }
        }
        if (stack == null || stack.method_7960()) return;
        if (!player.method_31548().method_7394(stack)) player.method_7328(stack, false);
    }

    /** True for a creature NoN treats as male (gender mask first, then its mob gender tags). */
    public static boolean isMale(class_1297 entity) {
        Method method = hasMaleGender();
        if (method == null || entity == null) return false;
        try {
            return (Boolean) method.invoke(null, entity);
        } catch (ReflectiveOperationException failure) {
            return false;
        }
    }

    /**
     * Installs the iron-plug-identical definition for the auto-bottling plug and adds the item to
     * NoN's per-slot id index, which is what its generated Trinkets tags are built from.
     */
    public static void installAutoPlugDefinition(String slotA, String slotV) {
        class_2960 id = class_2960.method_60655(NAMESPACE, "auto_bottling_plug");
        AccessoryItemDefinition iron = definition(IRON_PLUG);
        NonAccessoryEffects effects = iron != null
                ? iron.effects()
                : new NonAccessoryEffects(0.25D, true, 3.0D, 0, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 1.0D, 0, 1.0D, 1.0D, 1.0D);
        Map<String, com.nonid.data.NonAccessoryDefinitions.TooltipColor> tooltips =
                iron != null ? iron.tooltipColors() : Map.of();
        NonAccessoryBehavior behavior = iron != null ? iron.behavior() : NonAccessoryBehavior.NONE;
        ActorVisualBehavior actor = iron != null ? iron.actorVisualBehavior() : ActorVisualBehavior.NONE;
        SkinOverlays overlays = new SkinOverlays(
                List.of(new VisualLayer(class_2960.method_60655(NAMESPACE, "item/auto_bottling_plug_v"), TintMode.NONE)),
                List.of(new VisualLayer(class_2960.method_60655(NAMESPACE, "item/auto_bottling_plug_a"), TintMode.NONE)),
                List.of());
        AccessoryItemDefinition definition = new AccessoryItemDefinition(
                id, 1, 0, false, Set.of(slotA, slotV), effects, tooltips, behavior, overlays,
                ItemClientModel.EMPTY, false, 0, actor);
        try {
            definitions().put(id, definition);
            addToSlotIndex(slotA, id);
            addToSlotIndex(slotV, id);
        } catch (ReflectiveOperationException failure) {
            LOGGER.warn("Unable to install the NoN accessory definition for {}", id, failure);
        }
    }

    private static AccessoryItemDefinition definition(class_2960 id) {
        try {
            return NonAccessoryItemRegistry.getDefinition(id);
        } catch (RuntimeException failure) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<class_2960, AccessoryItemDefinition> definitions() throws ReflectiveOperationException {
        Field field = NonAccessoryItemRegistry.class.getDeclaredField("DEFINITIONS_BY_ITEM");
        field.setAccessible(true);
        return (Map<class_2960, AccessoryItemDefinition>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private static void addToSlotIndex(String slot, class_2960 id) throws ReflectiveOperationException {
        if (slot == null || slot.isBlank()) return;
        Field field = NonAccessoryItemRegistry.class.getDeclaredField("ITEM_IDS_BY_TRINKETS_SLOT");
        field.setAccessible(true);
        Map<String, List<class_2960>> index = (Map<String, List<class_2960>>) field.get(null);
        LinkedHashMap<String, List<class_2960>> copy = new LinkedHashMap<>(index);
        for (String key : List.of(slot, slot.replace("legs/", ""))) {
            List<class_2960> ids = new ArrayList<>(copy.getOrDefault(key, List.of()));
            if (!ids.contains(id)) ids.add(id);
            copy.put(key, List.copyOf(ids));
        }
        index.clear();
        index.putAll(copy);
    }

    private static Method createBottleForTank() {
        resolve();
        return createBottleForTank;
    }

    private static Method createBottleForEntity() {
        resolve();
        return createBottleForEntity;
    }

    private static Method giveOrDrop() {
        resolve();
        return giveItemOrDrop;
    }

    private static Method hasMaleGender() {
        resolve();
        return hasMaleGender;
    }

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> itemSystem = Class.forName("com.nonid.NonItemSystem");
            Class<?> liquidHolder = Class.forName("com.nonid.LiquidHolder");
            createBottleForTank = accessible(itemSystem.getDeclaredMethod("createBottleStackForTank", liquidHolder));
            createBottleForEntity = accessible(itemSystem.getDeclaredMethod("createLiquidBottleStack", class_2960.class));
            giveItemOrDrop = accessible(itemSystem.getDeclaredMethod("giveItemOrDrop", class_3222.class, class_1799.class));
        } catch (ReflectiveOperationException | RuntimeException failure) {
            LOGGER.warn("NoN bottle helpers are unavailable", failure);
        }
        try {
            Class<?> genders = Class.forName("com.nonid.NonGenderSystem");
            hasMaleGender = accessible(genders.getDeclaredMethod("hasMaleGender", class_1297.class));
        } catch (ReflectiveOperationException | RuntimeException failure) {
            LOGGER.warn("NoN gender helper is unavailable", failure);
        }
    }

    private static Method accessible(Method method) {
        method.setAccessible(true);
        return method;
    }
}
