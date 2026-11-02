package com.innerdimension.item;

import com.innerdimension.block.ModBlocks;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1792.class_1793;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7923;

public class ModItems {
    public static final class_5321<class_1792> VOID_NAIL_KEY=key("void_nail"),SHEATHED_INFECTOR_KEY=key("sheathed_infector");
    public static final class_1792 VOID_NAIL=register(VOID_NAIL_KEY,new VoidNailItem(settings(VOID_NAIL_KEY,1)));
    public static final class_1792 SHEATHED_INFECTOR=register(SHEATHED_INFECTOR_KEY,new class_1747(ModBlocks.SHEATHED_INFECTOR,settings(SHEATHED_INFECTOR_KEY,64)));

    public static final class_5321<class_1792> INFECTOR_KEY=key("infector");
    public static final class_1792 INFECTOR=register(INFECTOR_KEY,new InfectorItem(settings(INFECTOR_KEY,64)));
    public static final class_5321<class_1792> VOID_FRAGMENT_KEY=key("void_fragment");
    public static final class_1792 VOID_FRAGMENT=register(VOID_FRAGMENT_KEY,new MentalFragmentItem(settings(VOID_FRAGMENT_KEY,64),true));
    public static final class_5321<class_1792> VOID_WATER_ITEM_KEY=key("void_water");
    public static final class_1792 VOID_WATER_ITEM=register(VOID_WATER_ITEM_KEY,new class_1747(ModBlocks.VOID_WATER,settings(VOID_WATER_ITEM_KEY,64)));
    public static final class_5321<class_1792> VOID_MIST_ITEM_KEY=key("void_mist");
    public static final class_1792 VOID_MIST_ITEM=register(VOID_MIST_ITEM_KEY,new class_1747(ModBlocks.VOID_MIST,settings(VOID_MIST_ITEM_KEY,64)));

    public static final class_5321<class_1792> MENTAL_FRAGMENT_KEY=key("mental_fragment"), VILLAGER_SOUL_CORE_KEY=key("villager_soul_core");
    public static final class_1792 MENTAL_FRAGMENT=register(MENTAL_FRAGMENT_KEY,new MentalFragmentItem(settings(MENTAL_FRAGMENT_KEY,64)));
    public static final class_1792 VILLAGER_SOUL_CORE=register(VILLAGER_SOUL_CORE_KEY,new class_1792(settings(VILLAGER_SOUL_CORE_KEY,64)));
    public static final class_5321<class_1792> CREATIVE_SEMEN_BOTTLE_KEY=key("creative_semen_bottle");
    public static final class_1792 CREATIVE_SEMEN_BOTTLE=register(CREATIVE_SEMEN_BOTTLE_KEY,new class_1792(settings(CREATIVE_SEMEN_BOTTLE_KEY,16)));
    public static final class_5321<class_1792> AMETHYST_NAVEL_PIERCING_KEY=key("amethyst_navel_piercing");
    public static final class_1792 AMETHYST_NAVEL_PIERCING=register(AMETHYST_NAVEL_PIERCING_KEY,new NavelPiercingItem(settings(AMETHYST_NAVEL_PIERCING_KEY,1)));
    public static final class_5321<class_1792> CLIP_ON_AMETHYST_NAVEL_PIERCING_KEY=key("clip_on_amethyst_navel_piercing");
    public static final class_1792 CLIP_ON_AMETHYST_NAVEL_PIERCING=register(CLIP_ON_AMETHYST_NAVEL_PIERCING_KEY,new NavelPiercingItem(settings(CLIP_ON_AMETHYST_NAVEL_PIERCING_KEY,1)));
    public static final class_5321<class_1792> DOMAIN_CORE_KEY=key("domain_core");
    public static final class_1792 DOMAIN_CORE=register(DOMAIN_CORE_KEY,new DomainCoreItem(settings(DOMAIN_CORE_KEY,1)));
    public static final class_5321<class_1792> ENDER_PEARL_PLUG_KEY = key("ender_pearl_plug");
    public static final class_1792 ENDER_PEARL_PLUG = register(ENDER_PEARL_PLUG_KEY,
            new EnderPearlPlugItem(settings(ENDER_PEARL_PLUG_KEY, 1)));

    public static final class_5321<class_1792> STICKY_ENDER_PEARL_PLUG_KEY = key("sticky_ender_pearl_plug");
    public static final class_1792 STICKY_ENDER_PEARL_PLUG = register(STICKY_ENDER_PEARL_PLUG_KEY,
            new class_1792(settings(STICKY_ENDER_PEARL_PLUG_KEY, 1)));

    public static final class_5321<class_1792> AUTO_BOTTLING_PLUG_KEY = key("auto_bottling_plug");
    public static final class_1792 AUTO_BOTTLING_PLUG = register(AUTO_BOTTLING_PLUG_KEY,
            new AutoBottlingPlugItem(settings(AUTO_BOTTLING_PLUG_KEY, 1)));

    public static final class_5321<class_1792> SEMEN_EXTRACTOR_KEY = key("semen_extractor");
    public static final class_1792 SEMEN_EXTRACTOR = register(SEMEN_EXTRACTOR_KEY,
            new SemenExtractorItem(settings(SEMEN_EXTRACTOR_KEY, 1)));

    public static final class_5321<class_1792> ENDER_PEARL_DILDO_KEY = key("ender_pearl_dildo");
    public static final class_1792 ENDER_PEARL_DILDO = register(ENDER_PEARL_DILDO_KEY,
            new EnderPearlDildoItem(settings(ENDER_PEARL_DILDO_KEY, 16)));

    public static final class_5321<class_1792> TETHERED_ENDER_PEARL_DILDO_KEY = key("tethered_ender_pearl_dildo");
    public static final class_1792 TETHERED_ENDER_PEARL_DILDO = register(TETHERED_ENDER_PEARL_DILDO_KEY,
            new EnderPearlDildoItem(settings(TETHERED_ENDER_PEARL_DILDO_KEY, 16), true));

    // Block items so the two shell blocks can live in the creative tab.
    public static final class_5321<class_1792> PEARL_FACE_ITEM_KEY = key("pearl_face");
    public static final class_1792 PEARL_FACE_ITEM = register(PEARL_FACE_ITEM_KEY,
            new class_1747(ModBlocks.PEARL_FACE, settings(PEARL_FACE_ITEM_KEY, 1)));

    public static final class_5321<class_1792> INTESTINES_BLOCK_ITEM_KEY = key("intestines_block");
    public static final class_1792 INTESTINES_BLOCK_ITEM = register(INTESTINES_BLOCK_ITEM_KEY,
            new class_1747(ModBlocks.INTESTINES_BLOCK, settings(INTESTINES_BLOCK_ITEM_KEY, 1)));

    public static final class_5321<class_1792> ABYSS_BLOCK_ITEM_KEY = key("abyss_block");
    public static final class_1792 ABYSS_BLOCK_ITEM = register(ABYSS_BLOCK_ITEM_KEY,
            new class_1747(ModBlocks.ABYSS_BLOCK, settings(ABYSS_BLOCK_ITEM_KEY, 1)));

    public static final class_5321<class_1792> FLESH_WATER_ITEM_KEY = key("flesh_water");
    public static final class_1792 FLESH_WATER_ITEM = register(FLESH_WATER_ITEM_KEY,
            new class_1747(ModBlocks.FLESH_WATER, settings(FLESH_WATER_ITEM_KEY, 64)));

    public static final class_5321<class_1792> SOUL_MIST_ITEM_KEY = key("soul_mist");
    public static final class_1792 SOUL_MIST_ITEM = register(SOUL_MIST_ITEM_KEY,
            new class_1747(ModBlocks.SOUL_MIST, settings(SOUL_MIST_ITEM_KEY, 64)));

    public static final class_5321<class_1792> CENTRIFUGE_ITEM_KEY = key("centrifuge");
    public static final class_1792 CENTRIFUGE_ITEM = register(CENTRIFUGE_ITEM_KEY,
            new class_1747(ModBlocks.CENTRIFUGE, settings(CENTRIFUGE_ITEM_KEY, 64)));

    public static final class_5321<class_1792> SOLID_FLESH_WATER_ITEM_KEY = key("solid_flesh_water");
    public static final class_1792 SOLID_FLESH_WATER_ITEM = register(SOLID_FLESH_WATER_ITEM_KEY,
            new class_1747(ModBlocks.SOLID_FLESH_WATER, settings(SOLID_FLESH_WATER_ITEM_KEY, 64)));
    public static final class_5321<class_1792> SOLID_VOID_WATER_ITEM_KEY = key("solid_void_water");
    public static final class_1792 SOLID_VOID_WATER_ITEM = register(SOLID_VOID_WATER_ITEM_KEY,
            new class_1747(ModBlocks.SOLID_VOID_WATER, settings(SOLID_VOID_WATER_ITEM_KEY, 64)));

    private static class_1793 settings(class_5321<class_1792> key, int maxCount) {
        return new class_1793().method_63686(key).method_7889(maxCount);
    }

    private static class_5321<class_1792> key(String name) {
        return class_5321.method_29179(class_7923.field_41178.method_46765(),
                class_2960.method_60655("inner_dimension", name));
    }

    private static class_1792 register(class_5321<class_1792> key, class_1792 item) {
        return class_2378.method_10230(class_7923.field_41178, key.method_29177(), item);
    }

    public static void init() {
    }
}
