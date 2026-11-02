package com.innerdimension.client.domain;

import com.innerdimension.InnerDimension;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_9779;

/**
 * Owner status HUD for the abilities whose state the client cannot derive: the D06 goat dash
 * cooldown and the D10 fox ambush state machine. Every element is drawn only when its payload
 * bit is set, so a domain without those abilities shows nothing at all.
 */
public final class DomainAbilityHud {
    private static final int LINE_HEIGHT = 10;

    private static final int COLOR_DASH = 0xFFFF8A5C;
    private static final int COLOR_AMBER = 0xFFFFD24A;
    private static final int COLOR_READY = 0xFF7CF5A0;
    private static final int COLOR_MUTED = 0xFFB9B9B9;
    private static final int COLOR_DANGER = 0xFFFF6B6B;

    private DomainAbilityHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
                InnerDimension.id("domain_ability_state"), DomainAbilityHud::render);
    }

    private static void render(class_332 graphics, class_9779 ticks) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1687 == null) return;
        var state = DomainClientState.state();
        if (state == null || state.flags() == 0) return;
        var fonts = client.field_1772;
        int right = graphics.method_51421() - 4;
        int y = graphics.method_51443() - 52;

        // D06 goat dash cooldown.
        if (state.dashCooldownShown()) {
            var text = class_2561.method_43469("inner_dimension.ability.d06.cooldown",
                    Integer.toString(state.dashCooldownTicks()));
            y = line(graphics, fonts, text, right, y, COLOR_DASH);
        }
        // D10 fox ambush state machine.
        if (state.d10Charging()) {
            y = line(graphics, fonts, class_2561.method_43471("inner_dimension.ability.d10.charging"),
                    right, y, COLOR_AMBER);
        } else if (state.d10Ready()) {
            y = line(graphics, fonts, class_2561.method_43471("inner_dimension.ability.d10.ready"),
                    right, y, COLOR_READY);
        } else if (state.d10Cooldown()) {
            y = line(graphics, fonts, class_2561.method_43471("inner_dimension.ability.d10.cooldown"),
                    right, y, COLOR_MUTED);
        }
        // D40 enderman dodge cooldown.
        if (state.dodgeCooldownShown()) {
            var text = class_2561.method_43469("inner_dimension.ability.d40.cooldown",
                    Integer.toString(state.dodgeCooldownTicks()));
            y = line(graphics, fonts, text, right, y, COLOR_DANGER);
        }
        // D49 zombie horse remaining lives.
        if (state.d49Used()) {
            var text = class_2561.method_43469("inner_dimension.ability.d49.used",
                    Integer.toString(state.remainingUses()));
            line(graphics, fonts, text, right, y, COLOR_MUTED);
        }
    }

    /** Right-aligned status line; returns the next y above it. */
    private static int line(class_332 graphics, class_327 fonts, class_2561 text, int right, int y, int color) {
        graphics.method_51439(fonts, text, right - fonts.method_1727(text.getString()), y, color, true);
        return y - LINE_HEIGHT;
    }
}
