package com.innerdimension.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class InnerConfig {
    private static InnerConfig INSTANCE = new InnerConfig();

    public int openDomainInitialRadius = 24;
    public int openDomainInitialDurationSeconds = 128;
    public int imprintInitialCapacity = 6;
    public int imprintExpansionSpecies = 15;
    public int imprintExpansionPoints = 3;
    public int transformSeconds = 60;
    public boolean keepLoaded = true;
    public boolean projectionDebug = false;
    /** Projected lava always affects movement; this enables vanilla damage and ignition. */
    public boolean projectionLavaDamage = false;
    /** Server-authoritative projection timeline (game ticks). */
    public static final int projectionShellTicks = 15;
    public double domainClashDrawThreshold = 0.5;
    public static final int projectionInteriorTicks = 5;
    /** Crest animation values are in world blocks, not screen pixels. */
    public static final double projectionCrestInitialDistance = 0.0;
    public static final double projectionCrestShellDistance = 0.5;
    public static final double projectionCrestInteriorDistance = 0.3;
    public static final double projectionCrestInitialScale = 0.3;
    public static final double projectionCrestShellScale = 0.6;
    public static final double projectionCrestInteriorScale = 0.55;
    /** Client HUD settings: GUI pixels, size multiplier, and opacity percent. */
    public int crestHudOffsetX = 0;
    public int crestHudHeightFromBottom = 53;
    public double crestHudScale = 1.0;
    public int crestHudIdleOpacity = 30;

    public static InnerConfig get() {
        return INSTANCE;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve("inner_dimension/inner_dimension.properties");
        try{Files.createDirectories(file.getParent());Path legacy=configDir.resolve("inner_dimension.properties");if(!Files.exists(file)&&Files.exists(legacy))Files.copy(legacy,file);if(Files.exists(legacy)){Path backup=file.getParent().resolve("legacy-config.properties.bak");if(!Files.exists(backup))Files.copy(legacy,backup);Files.delete(legacy);}}catch(IOException e){throw new IllegalStateException("Cannot migrate Inner Dimension config",e);}
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                // ignore, use defaults
            }
        }
        INSTANCE.transformSeconds = clamp(parseInt(props.getProperty("transformSeconds"), 60), 1, Integer.MAX_VALUE / 20);
        INSTANCE.keepLoaded = Boolean.parseBoolean(props.getProperty("keepLoaded", "true"));
        INSTANCE.projectionDebug = Boolean.parseBoolean(props.getProperty("projectionDebug", "false"));
        INSTANCE.projectionLavaDamage = Boolean.parseBoolean(props.getProperty("projectionLavaDamage", "false"));
        INSTANCE.domainClashDrawThreshold = clamp(parseDouble(props.getProperty("domainClashDrawThreshold"), 0.5), 0, 10);
        INSTANCE.crestHudOffsetX = clamp(parseInt(props.getProperty("crestHudOffsetX"), 0), -1000, 1000);
        INSTANCE.crestHudHeightFromBottom = clamp(parseInt(props.getProperty("crestHudHeightFromBottom"), 53), 0, 1000);
        INSTANCE.crestHudScale = clamp(parseDouble(props.getProperty("crestHudScale"), 1.0), 0.25, 4);
        INSTANCE.crestHudIdleOpacity = clamp(parseInt(props.getProperty("crestHudIdleOpacity"), 30), 0, 100);
        INSTANCE.openDomainInitialRadius = clamp(parseInt(props.getProperty("openDomainInitialRadius"), 24), 1, Integer.MAX_VALUE);
        INSTANCE.openDomainInitialDurationSeconds = clamp(parseInt(props.getProperty("openDomainInitialDurationSeconds"), 128), 1, Integer.MAX_VALUE / 20);
        INSTANCE.imprintInitialCapacity = clamp(parseInt(props.getProperty("imprintInitialCapacity"), 6), 1, Integer.MAX_VALUE);
        INSTANCE.imprintExpansionSpecies = clamp(parseInt(props.getProperty("imprintExpansionSpecies"), 15), 1, Integer.MAX_VALUE);
        INSTANCE.imprintExpansionPoints = clamp(parseInt(props.getProperty("imprintExpansionPoints"), 3), 1, Integer.MAX_VALUE);
        save(configDir);
    }

    public static void save(Path configDir) {
        Properties props = new Properties();
        props.setProperty("openDomainInitialRadius", Integer.toString(INSTANCE.openDomainInitialRadius));
        props.setProperty("openDomainInitialDurationSeconds", Integer.toString(INSTANCE.openDomainInitialDurationSeconds));
        props.setProperty("imprintInitialCapacity", Integer.toString(INSTANCE.imprintInitialCapacity));
        props.setProperty("imprintExpansionSpecies", Integer.toString(INSTANCE.imprintExpansionSpecies));
        props.setProperty("imprintExpansionPoints", Integer.toString(INSTANCE.imprintExpansionPoints));
        props.setProperty("projectionDebug", Boolean.toString(INSTANCE.projectionDebug));
        props.setProperty("projectionLavaDamage", Boolean.toString(INSTANCE.projectionLavaDamage));
        props.setProperty("transformSeconds", Integer.toString(INSTANCE.transformSeconds));
        props.setProperty("keepLoaded", Boolean.toString(INSTANCE.keepLoaded));
        props.setProperty("domainClashDrawThreshold", Double.toString(INSTANCE.domainClashDrawThreshold));
        props.setProperty("crestHudOffsetX", Integer.toString(INSTANCE.crestHudOffsetX));
        props.setProperty("crestHudHeightFromBottom", Integer.toString(INSTANCE.crestHudHeightFromBottom));
        props.setProperty("crestHudScale", Double.toString(INSTANCE.crestHudScale));
        props.setProperty("crestHudIdleOpacity", Integer.toString(INSTANCE.crestHudIdleOpacity));
        try {
            Files.createDirectories(configDir.resolve("inner_dimension"));
            try (OutputStream out = Files.newOutputStream(configDir.resolve("inner_dimension/inner_dimension.properties"))) {
                props.store(out, "Inner Dimension config. Server gameplay: transformSeconds, keepLoaded, projectionLavaDamage, domainClashDrawThreshold, openDomainInitialRadius (blocks), openDomainInitialDurationSeconds, imprintInitialCapacity, imprintExpansionSpecies (unique species per expansion), imprintExpansionPoints. Restart server after editing. Client: crestHud*, projectionDebug.");
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private static int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
    private static double parseDouble(String s, double def) {
        if (s == null) return def;
        try {
            double value = Double.parseDouble(s.trim());
            return Double.isFinite(value) ? value : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
}
