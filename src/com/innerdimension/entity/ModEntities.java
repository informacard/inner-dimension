package com.innerdimension.entity;

import com.innerdimension.InnerDimension;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.class_1299;
import net.minecraft.class_1299.class_1300;
import net.minecraft.class_1299.class_4049;
import net.minecraft.class_1311;
import net.minecraft.class_2378;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.class_7924;

public class ModEntities {
    public static final class_5321<class_1299<?>> VOID_FETUS_KEY=key("void_fetus");
    public static final class_1299<VoidFetusEntity> VOID_FETUS=registerDoll(VOID_FETUS_KEY,VoidFetusEntity::new);
    public static final class_5321<class_1299<?>> MENTAL_FETUS_KEY=key("mental_fetus");
    public static final class_1299<MentalFetusEntity> MENTAL_FETUS=registerDoll(MENTAL_FETUS_KEY,MentalFetusEntity::new);
    public static final class_5321<class_1299<?>> ENDER_PEARL_DILDO_KEY = key("ender_pearl_dildo");
    public static final class_1299<EnderPearlDildoEntity> ENDER_PEARL_DILDO =
            ModEntities.<EnderPearlDildoEntity>register(ENDER_PEARL_DILDO_KEY);

    public static final class_5321<class_1299<?>> TETHERED_ENDER_PEARL_DILDO_KEY = key("tethered_ender_pearl_dildo");
    public static final class_1299<EnderPearlDildoEntity> TETHERED_ENDER_PEARL_DILDO =
            ModEntities.<EnderPearlDildoEntity>register(TETHERED_ENDER_PEARL_DILDO_KEY);

    /** The one doll type this mod creates; every new interaction spawns exactly this one. */
    public static final class_5321<class_1299<?>> SENSORY_LINK_DOLL_KEY = key("sensory_link_doll");
    public static final class_1299<SensoryLinkDollEntity> SENSORY_LINK_DOLL =
            registerDoll(SENSORY_LINK_DOLL_KEY, SensoryLinkDollEntity::new);

    /**
     * The doll entity id this mod used before the sensory link rename.
     *
     * <p>The slot is filled by the isolated compatibility package, which owns the old id text and the
     * compatibility entity class; no new interaction may ever create one. Registering the old id
     * keeps the dolls of an existing save from turning into unknown entities, and the client attaches
     * the very same renderer to both types. Nothing is converted on load: such a doll keeps its uuid,
     * its piston binding, its lock and its position until a player breaks it normally and receives a
     * new sensory link doll.</p>
     *
     * <p>The field is named neutrally on purpose, so the old terminology stays confined to that one
     * package.</p>
     */
    public static class_5321<class_1299<?>> LEGACY_DOLL_KEY;
    public static class_1299<SensoryLinkDollEntity> LEGACY_DOLL;

    /**
     * Registers a doll entity type with this mod's doll collision box.
     *
     * <p>Reachable so the compatibility registrar can register the pre-rename id through the very
     * same box, category and factory handling.</p>
     */
    @SuppressWarnings("unchecked")
    public static <T extends SensoryLinkDollEntity> class_1299<T> registerDoll(
            class_5321<class_1299<?>> registryKey, class_4049<T> factory) {
        return (class_1299<T>) class_2378.method_39197(class_7923.field_41177, registryKey,
                class_1300.method_5903(factory, class_1311.field_17715)
                        // The plushie model is a whole doll filling its block: head, body and
                        // limbs, one block tall. The box matches that instead of the old head-sized one.
                        .method_17687(0.7F, 1.0F)
                        .method_5905(registryKey));
    }

    private static class_5321<class_1299<?>> key(String path) {
        return class_5321.method_29179(class_7924.field_41266, InnerDimension.id(path));
    }

    @SuppressWarnings("unchecked")
    private static <T extends EnderPearlDildoEntity> class_1299<T> register(class_5321<class_1299<?>> registryKey) {
        return (class_1299<T>) class_2378.method_39197(class_7923.field_41177, registryKey,
                class_1300.method_5903(EnderPearlDildoEntity::new, class_1311.field_17715)
                        .method_17687(0.4F, 0.9F)
                        .method_5905(registryKey));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(ENDER_PEARL_DILDO, EnderPearlDildoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(TETHERED_ENDER_PEARL_DILDO, EnderPearlDildoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(VOID_FETUS, SensoryLinkDollEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(MENTAL_FETUS, SensoryLinkDollEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SENSORY_LINK_DOLL, SensoryLinkDollEntity.createAttributes());
        // The single reference to the isolated compatibility package: it registers the pre-rename
        // entity id, fills LEGACY_DOLL_KEY/LEGACY_DOLL above and attaches the same attributes.
        com.innerdimension.legacyempathy.LegacyEmpathyEntities.register();
    }
}
