package com.innerdimension.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2561;

public class InnerConfigMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            InnerConfig cfg = InnerConfig.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(class_2561.method_43471("inner_dimension.config.title"));
            ConfigEntryBuilder eb = builder.entryBuilder();
            ConfigCategory hud = builder.getOrCreateCategory(class_2561.method_43471("inner_dimension.config.category.crest_hud"));
            hud.addEntry(eb.startIntField(class_2561.method_43471("inner_dimension.config.crest_hud_offset_x"), cfg.crestHudOffsetX)
                    .setDefaultValue(0).setMin(-1000).setMax(1000).setSaveConsumer(v -> cfg.crestHudOffsetX = v).build());
            hud.addEntry(eb.startIntField(class_2561.method_43471("inner_dimension.config.crest_hud_height"), cfg.crestHudHeightFromBottom)
                    .setDefaultValue(53).setMin(0).setMax(1000).setSaveConsumer(v -> cfg.crestHudHeightFromBottom = v).build());
            hud.addEntry(eb.startDoubleField(class_2561.method_43471("inner_dimension.config.crest_hud_scale"), cfg.crestHudScale)
                    .setDefaultValue(1.0).setMin(0.25).setMax(4.0).setSaveConsumer(v -> cfg.crestHudScale = v).build());
            hud.addEntry(eb.startIntField(class_2561.method_43471("inner_dimension.config.crest_hud_idle_opacity"), cfg.crestHudIdleOpacity)
                    .setDefaultValue(30).setMin(0).setMax(100).setSaveConsumer(v -> cfg.crestHudIdleOpacity = v).build());

            builder.setSavingRunnable(() -> InnerConfig.save(FabricLoader.getInstance().getConfigDir()));
            return builder.build();
        };
    }
}
