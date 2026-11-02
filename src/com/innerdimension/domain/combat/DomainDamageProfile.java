package com.innerdimension.domain.combat;

/** Raw damage composition; magic components use vanilla magic's armor/shield bypass tags. */
public record DomainDamageProfile(String physicalType, String magicType, float physicalFraction) {
    public static DomainDamageProfile of(String type) {
        return switch (type) {
            case DomainDamageTypes.GROUND_SHOCK -> new DomainDamageProfile(type, DomainDamageTypes.GROUND_SHOCK_MAGIC, .75f);
            case DomainDamageTypes.SPIT -> new DomainDamageProfile(type, DomainDamageTypes.SPIT_MAGIC, .5f);
            case DomainDamageTypes.REFLECTED -> new DomainDamageProfile(type, DomainDamageTypes.REFLECTED_MAGIC, .5f);
            default -> new DomainDamageProfile(type, null, 1);
        };
    }

    public boolean mixed() { return magicType != null; }
    public float physicalAmount(float total) { return total * physicalFraction; }
    public float magicAmount(float total) { return total - physicalAmount(total); }

    /** Both components must be attempted, even when the first one is blocked or cancelled. */
    public boolean apply(float total, Component hit) {
        boolean landed = hit.hurt(physicalType, physicalAmount(total));
        if (mixed()) landed = hit.hurt(magicType, magicAmount(total)) || landed;
        return landed;
    }

    @FunctionalInterface
    public interface Component { boolean hurt(String type, float amount); }
}
