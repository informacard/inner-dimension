package com.innerdimension.domain;

import java.util.*;

/**
 * Domain strength, 0-10.
 *
 * <p>Four factors, each normalised to 0..1 and weighted so that the maximum is exactly 10 and the
 * minimum exactly 0:</p>
 *
 * <pre>
 *   ability quality 5.0   desire 3.0   mental volume 1.5   fertility 0.5
 * </pre>
 *
 * <p>Ability quality itself is three parts. Concentration (how far the loadout's load weights and
 * colour weights sit above a flat distribution) is the strongest signal: a domain built around one
 * heavy trait and carried by weak ones scores high, an evenly spread one scores zero. Magnitude is
 * the strongest trait's own load weight, so an ender dragon domain outranks an axolotl domain that
 * is shaped identically. The count term is deliberately the smallest and scales with the player's
 * own capacity, so raising a capacity never punishes the player for using it.</p>
 *
 * <p>Everything here is pure arithmetic over already-known values: the caller supplies the loadout
 * ids, the capacity, the current energy and whether the fertility effect is present. No world, no
 * player, no networking, so the client screen and the server command can both call it.</p>
 */
public final class DomainStrength {
    // Each weight multiplies a 0..1 factor, and 10x the factor's weight is the points it can add.
    public static final double ABILITY_WEIGHT = 0.50;   // 5.0 points
    public static final double DESIRE_WEIGHT = 0.30;    // 3.0 points
    public static final double VOLUME_WEIGHT = 0.15;    // 1.5 points
    public static final double FERTILE_WEIGHT = 0.05;   // 0.5 points

    // Ability quality split, sums to 1.
    private static final double A_CONCENTRATION = 0.50;
    private static final double A_MAGNITUDE = 0.30;
    private static final double A_COUNT = 0.20;

    /** NoN hard-codes 200 for players ({@code PlayerEntityEnergyMixin.getMaxEnergy}). */
    public static final int ENERGY_MAX = 200;
    /** {@code DomainCapacityRules.capacity(0)} and the largest settlement count reachable today. */
    public static final int CAPACITY_BASE = 6, CAPACITY_MAX = 18;
    /** Heaviest load weight in {@link DomainAbilityCatalog}, i.e. a full-strength magnitude. */
    public static final double MAGNITUDE_FULL = 6;

    private DomainStrength() {}

    /**
     * One scored domain. The four contributions are already points, and they add up to
     * {@link #total()}, which makes a displayed breakdown self-checking.
     */
    public record Breakdown(double total, double ability, double desire, double volume, double fertility,
                            double abilityScore, int count, int energy, int capacity, boolean fertile) {
        /** {@code 5.2} style, never a locale-dependent separator. */
        public String format(double value) { return String.format(Locale.ROOT, "%.1f", value); }
    }

    /**
     * Scores a loadout of creature ids. Unknown ids (no approved D-code, e.g. D00) are ignored
     * exactly as the catalog ignores them everywhere else.
     */
    public static Breakdown of(Collection<String> entityIds, int capacity, int energy, boolean fertile) {
        List<DomainAbilityCatalog.Ability> loadout = new ArrayList<>();
        if (entityIds != null) {
            for (String id : entityIds) {
                DomainAbilityCatalog.Ability ability = DomainAbilityCatalog.byEntity(id);
                if (ability != null) loadout.add(ability);
            }
        }
        double a = abilityScore(loadout, capacity);
        double d = clamp(energy / (double) ENERGY_MAX, 0, 1);
        double v = clamp((capacity - CAPACITY_BASE) / (double) (CAPACITY_MAX - CAPACITY_BASE), 0, 1);
        double f = fertile ? 1 : 0;
        double total = 10 * (ABILITY_WEIGHT * a + DESIRE_WEIGHT * d + VOLUME_WEIGHT * v + FERTILE_WEIGHT * f);
        return new Breakdown(total, 10 * ABILITY_WEIGHT * a, 10 * DESIRE_WEIGHT * d,
                10 * VOLUME_WEIGHT * v, 10 * FERTILE_WEIGHT * f, a, loadout.size(),
                Math.max(0, energy), capacity, fertile);
    }

    /** Ability quality 0..1. Empty is zero: a domain with nothing engraved has no identity. */
    public static double abilityScore(Collection<DomainAbilityCatalog.Ability> loadout, int capacity) {
        int n = loadout.size();
        if (n == 0) return 0;
        int sumLoad = 0, sumColor = 0, maxLoad = 0, maxColor = 0;
        for (DomainAbilityCatalog.Ability ability : loadout) {
            sumLoad += ability.loadWeight();
            sumColor += ability.colorWeight();
            maxLoad = Math.max(maxLoad, ability.loadWeight());
            maxColor = Math.max(maxColor, ability.colorWeight());
        }
        double concentration = (concentration(maxLoad, sumLoad, n) + concentration(maxColor, sumColor, n)) * 0.5;
        double magnitude = clamp(maxLoad / MAGNITUDE_FULL, 0, 1);
        double count = clamp((capacity - n) / (double) Math.max(1, capacity - 1), 0, 1);
        return clamp(A_CONCENTRATION * concentration + A_MAGNITUDE * magnitude + A_COUNT * count, 0, 1);
    }

    /**
     * Share of the strongest entry, rescaled so a flat distribution scores 0 and a single entry
     * scores 1. Plain variance cannot be used here: it is 0 for a single ability, which must be the
     * strongest case, and it grows with the absolute weights so loadouts could not be compared.
     */
    private static double concentration(int max, int sum, int n) {
        if (n <= 1 || sum <= 0) return 1;
        double uniform = 1.0 / n;
        return clamp((max / (double) sum - uniform) / (1 - uniform), 0, 1);
    }

    private static double clamp(double value, double lo, double hi) {
        return Math.max(lo, Math.min(hi, value));
    }
}
