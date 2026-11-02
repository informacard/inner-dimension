package com.innerdimension.client;

import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;

/** Prevents movement mods from treating the tethered-device inventory as a walkable screen. */
public final class TetheredContainerMovementLock {
    private TetheredContainerMovementLock() {
    }

    public static boolean isActive() {
        class_310 client = class_310.method_1551();
        if (client == null || client.field_1755 == null) {
            return false;
        }

        String expected = class_2561.method_43471("entity.inner_dimension.tethered_ender_pearl_dildo").getString();
        return expected.equals(client.field_1755.method_25440().getString());
    }

    public static void tick(class_310 client) {
        if (!isActive()) {
            return;
        }

        class_315 options = client.field_1690;
        release(options.field_1894);
        release(options.field_1881);
        release(options.field_1913);
        release(options.field_1849);
        release(options.field_1903);
        release(options.field_1832);
        release(options.field_1867);

        if (client.field_1724 != null) {
            client.field_1724.field_3913.method_3129();
        }
    }

    private static void release(class_304 key) {
        key.method_23481(false);
    }
}
