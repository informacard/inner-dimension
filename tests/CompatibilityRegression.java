package com.innerdimension.tests;

import com.innerdimension.config.InnerConfig;
import com.innerdimension.core.InterfaceFactoryBridge;
import java.nio.file.Files;
import java.nio.file.Path;

/** Headless checks for the private-interface bridge and user-edited configuration. */
public final class CompatibilityRegression {
    private interface ScreenFactory {
        String create(String title);
        default String open(String title) { return "opened:" + create(title); }
        default String noArguments() { return create("default"); }
        void unsupported();
    }

    public static void main(String[] args) throws Exception {
        ScreenFactory factory = (ScreenFactory) InterfaceFactoryBridge.create(ScreenFactory.class,
                values -> "screen:" + values[0]);
        check("opened:screen:centrifuge".equals(factory.open("centrifuge")),
                "Default screen-opening method must delegate to create");
        check("screen:default".equals(factory.noArguments()), "Default method with no arguments");
        check(factory.equals(factory) && !factory.equals(new Object()), "Proxy identity equality");
        check(factory.hashCode() == System.identityHashCode(factory), "Proxy identity hash");
        check(factory.toString().contains("ScreenFactory"), "Proxy diagnostic name");
        try {
            factory.unsupported();
            throw new AssertionError("Unknown methods must fail explicitly");
        } catch (UnsupportedOperationException expected) { }

        Path configDir = Path.of(args[0]);
        Files.createDirectories(configDir);
        Path file = configDir.resolve("inner_dimension.properties");
        Files.writeString(file, "transformSeconds=2147483647\nlightLevel=99\n"
                + "projectionShellTicks=0\nprojectionInteriorTicks=99999\n"
                + "projectionCrestInitialScale=NaN\nprojectionCrestShellDistance=Infinity\n"
                + "projectionCrestInteriorDistance=-Infinity\n");
        InnerConfig.load(configDir);
        InnerConfig config = InnerConfig.get();
        check(config.transformSeconds * 20L <= Integer.MAX_VALUE, "Tick conversion must not overflow");
        check(config.lightLevel == 15, "Light must be within vanilla's range");
        check(config.projectionShellTicks == 1 && config.projectionInteriorTicks == 1200,
                "Projection durations must remain bounded");
        check(config.projectionCrestInitialScale == .3 && config.projectionCrestShellDistance == .5
                && config.projectionCrestInteriorDistance == .3, "Non-finite values must use defaults");
        Files.writeString(file, "transformSeconds=-1\nlightLevel=-99\n"
                + "projectionCrestShellScale= 0.75 \nprojectionCrestInitialDistance=bad\n");
        InnerConfig.load(configDir);
        check(config.transformSeconds == 1 && config.lightLevel == 0, "Negative settings bounded");
        check(config.projectionCrestShellScale == .75 && config.projectionCrestInitialDistance == 0,
                "Valid decimals and invalid strings handled");
        System.out.println("PASS: private factory default dispatch, object methods, configuration bounds");
    }

    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }
}
