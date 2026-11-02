package com.innerdimension.block;

import com.innerdimension.centrifuge.CentrifugeBlock;
import com.innerdimension.centrifuge.CentrifugeBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_2591;
import net.minecraft.class_2960;
import net.minecraft.class_3917;
import net.minecraft.class_4970.class_2251;
import net.minecraft.class_5321;
import net.minecraft.class_7923;

public class ModBlocks {
    public static final class_5321<class_2248> SHEATHED_INFECTOR_KEY=key("sheathed_infector");
    public static final class_2248 SHEATHED_INFECTOR=register("sheathed_infector",SHEATHED_INFECTOR_KEY,new InfectorBlock(class_2251.method_9637().method_63500(SHEATHED_INFECTOR_KEY).method_9634().method_9632(0F)));

    public static final class_5321<class_2248> INFECTOR_KEY=key("infector");
    public static final class_2248 INFECTOR=register("infector",INFECTOR_KEY,new InfectorBlock(class_2251.method_9637().method_63500(INFECTOR_KEY).method_9634().method_9632(0F).method_9631(state->14)));
    public static final class_5321<class_2248> VOID_WATER_KEY=key("void_water");
    public static final class_2248 VOID_WATER=register("void_water",VOID_WATER_KEY,new DomainLayerBlock(class_2251.method_9637().method_63500(VOID_WATER_KEY).method_9626(com.innerdimension.sound.ModSounds.WATER).method_9634().method_22488().method_9632(0F).method_9631(state->8),false));
    public static final class_5321<class_2248> VOID_MIST_KEY=key("void_mist");
    public static final class_2248 VOID_MIST=register("void_mist",VOID_MIST_KEY,new DomainLayerBlock(class_2251.method_9637().method_63500(VOID_MIST_KEY).method_9634().method_22488().method_9632(0F).method_9631(state->8),true));

    public static final class_5321<class_2248> PEARL_FACE_KEY = key("pearl_face");
    public static final class_2248 PEARL_FACE = register("pearl_face", PEARL_FACE_KEY,
            new PearlFaceBlock(bedrockSettings(PEARL_FACE_KEY).method_9626(net.minecraft.class_2498.field_11537)));

    public static final class_5321<class_2248> INTESTINES_BLOCK_KEY = key("intestines_block");
    public static final class_2248 INTESTINES_BLOCK = register("intestines_block", INTESTINES_BLOCK_KEY,
            new class_2248(bedrockSettings(INTESTINES_BLOCK_KEY).method_22488()));

    public static final class_5321<class_2248> ABYSS_BLOCK_KEY = key("abyss_block");
    public static final class_2248 ABYSS_BLOCK = register("abyss_block", ABYSS_BLOCK_KEY,
            new class_2248(bedrockSettings(ABYSS_BLOCK_KEY).method_22488()));

    public static final class_5321<class_2248> FLESH_WATER_KEY = key("flesh_water");
    public static final class_2248 FLESH_WATER = register("flesh_water", FLESH_WATER_KEY,
            new DomainLayerBlock(class_2251.method_9637().method_63500(FLESH_WATER_KEY).method_9626(com.innerdimension.sound.ModSounds.WATER)
                    .method_9634().method_22488().method_9632(0.0F).method_9631(state -> 8), false));

    public static final class_5321<class_2248> SOUL_MIST_KEY = key("soul_mist");
    public static final class_5321<class_2248> SOLID_FLESH_WATER_KEY = key("solid_flesh_water");
    public static final class_2248 SOLID_FLESH_WATER = register("solid_flesh_water",SOLID_FLESH_WATER_KEY,
            new class_2248(bedrockSettings(SOLID_FLESH_WATER_KEY).method_9626(com.innerdimension.sound.ModSounds.WATER).method_22488().method_9631(state->8)));
    public static final class_5321<class_2248> SIMPLE_FLESH_WATER_KEY = key("simple_flesh_water");
    public static final class_2248 SIMPLE_FLESH_WATER = register("simple_flesh_water", SIMPLE_FLESH_WATER_KEY,
            new DomainLayerBlock(bedrockSettings(SIMPLE_FLESH_WATER_KEY).method_9626(com.innerdimension.sound.ModSounds.WATER).method_22488().method_9631(state->8),false));
    public static final class_5321<class_2248> SOLID_VOID_WATER_KEY = key("solid_void_water");
    public static final class_2248 SOLID_VOID_WATER = register("solid_void_water", SOLID_VOID_WATER_KEY,
            new class_2248(bedrockSettings(SOLID_VOID_WATER_KEY).method_9626(com.innerdimension.sound.ModSounds.WATER).method_22488().method_9631(state->8)));
    public static final class_2248 SOUL_MIST = register("soul_mist", SOUL_MIST_KEY,
            new DomainLayerBlock(class_2251.method_9637().method_63500(SOUL_MIST_KEY)
                    .method_9634().method_22488().method_9632(0.0F).method_9631(state -> 8), true));

    public static final class_5321<class_2248> CENTRIFUGE_KEY = key("centrifuge");
    public static final CentrifugeBlock CENTRIFUGE = (CentrifugeBlock) register("centrifuge", CENTRIFUGE_KEY,
            new CentrifugeBlock(class_2251.method_9637().method_63500(CENTRIFUGE_KEY).method_9632(2.0F)));
    public static final class_2591<CentrifugeBlockEntity> CENTRIFUGE_BLOCK_ENTITY = class_2378.method_10230(
            class_7923.field_41181, class_2960.method_60655("inner_dimension", "centrifuge"),
            FabricBlockEntityTypeBuilder.create(CentrifugeBlockEntity::new, CENTRIFUGE).build());
    public static final class_3917<com.innerdimension.centrifuge.CentrifugeScreenHandler> CENTRIFUGE_SCREEN_HANDLER = class_2378.method_10230(
            class_7923.field_41187, class_2960.method_60655("inner_dimension", "centrifuge"),
            new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(
                    com.innerdimension.centrifuge.CentrifugeScreenHandler::new, class_2338.field_48404.method_56430()));

    /**
     * Bedrock-grade: hardness -1 (unbreakable) with a huge blast resistance and no drops.
     *
     * <p>Careful with the builder chain: {@code method_9634()} is
     * {@code Settings#noCollision()} (it clears the collidable + opaque flags), not a
     * "build" terminator, so it must not be called here.</p>
     */
    private static class_2251 bedrockSettings(class_5321<class_2248> key) {
        return class_2251.method_9637().method_63500(key)
                .method_9629(-1.0F, 3600000.0F);
    }

    private static class_5321<class_2248> key(String name) {
        return class_5321.method_29179(class_7923.field_41175.method_46765(),
                class_2960.method_60655("inner_dimension", name));
    }

    private static class_2248 register(String name, class_5321<class_2248> key, class_2248 block) {
        return class_2378.method_10230(class_7923.field_41175,
                class_2960.method_60655("inner_dimension", name), block);
    }

    public static void init() {
    }
}
