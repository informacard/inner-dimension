package com.innerdimension.legacyempathy;

import com.innerdimension.InnerDimension;
import com.innerdimension.entity.ModEntities;
import com.innerdimension.entity.SensoryLinkDollEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.class_5321;
import net.minecraft.class_7924;

/**
 * Registers the doll entity id this mod used before the sensory link rename.
 *
 * <p>Everything that still knows the old terminology lives in this package and nowhere else: the old
 * id text, the old-style entity class and the registration that keeps the id resolvable. The rest of
 * the mod only ever sees the neutral {@code ModEntities.LEGACY_DOLL} slot, so no runtime decision
 * outside this package can be made on the old terms.</p>
 *
 * <p>Only the entity id is kept for compatibility. The old item shape is <em>not</em> reproduced here:
 * non_player_plushie 1.5.0 migrates stored legacy dolls itself, so this mod deliberately keeps no
 * second marker or custom-name fallback.</p>
 */
public final class LegacyEmpathyEntities {
    /** The doll entity id this mod wrote before the rename. */
    private static final String OLD_DOLL_ID = "empathy_doll";

    private static boolean registered;

    private LegacyEmpathyEntities() {
    }

    /**
     * Registers the pre-rename doll entity type and its attributes.
     *
     * <p>Idempotent, so the entrypoint may call it defensively without the registry rejecting a
     * duplicate id. It uses the very same collision box, category and factory handling as the current
     * doll type, and it is the only place the old id is ever registered.</p>
     */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ModEntities.LEGACY_DOLL_KEY = class_5321.method_29179(class_7924.field_41266,
                InnerDimension.id(OLD_DOLL_ID));
        ModEntities.LEGACY_DOLL = ModEntities.<SensoryLinkDollEntity>registerDoll(
                ModEntities.LEGACY_DOLL_KEY, LegacyEmpathyDollEntity::new);
        FabricDefaultAttributeRegistry.register(ModEntities.LEGACY_DOLL,
                SensoryLinkDollEntity.createAttributes());
    }
}
