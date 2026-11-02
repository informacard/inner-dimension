package com.innerdimension.domain.ability;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
/** A successful owner attack has a 50% chance to burst; damage is limited to its victim. */
public final class D65 implements DomainAbility, DamageHook {
    public static final D65 INSTANCE = new D65();
    private static final ThreadLocal<Boolean> BURST = ThreadLocal.withInitial(() -> false);
    private D65() {}
    @Override public String id() { return "minecraft:ghast"; }
    @Override public String code() { return "D65"; }
    @Override public void afterDamage(DomainSession session, class_1309 victim, class_1282 source,
            float base, float taken, boolean blocked) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D65",victim))return;
        if (BURST.get() || DomainDamagePipeline.autonomous() || taken <= 0 || blocked || source == null) return;
        class_1297 attacker = source.method_5529();
        if (attacker == null || !attacker.method_5667().equals(session.ownerId)) return;
        if (!"minecraft:player_attack".equals(DomainDamageTypes.keyOf(source))
                && !DomainDamageTypes.isProjectile(source)) return;
        if (!(session.world instanceof class_3218 world) || world.method_8409().method_43048(2) != 0) return;
        BURST.set(true);
        try {
            class_243 point = victim.method_73189().method_1031(0, victim.method_17682() * 0.5, 0);
            // Null explosion entity includes the player in knockback; the attributed source still credits them.
            class_1282 damage = DomainDamageTypes.source(world, "minecraft:player_explosion", attacker, attacker);
            victim.field_6008 = 0;
            world.method_55117(null, damage, new class_5362() {
                @Override public boolean method_55504(class_1927 explosion, class_1297 entity) { return entity == victim; }
                @Override public float method_55115(class_1927 explosion, class_1297 entity, float exposure) { return 4f; }
            }, point.field_1352, point.field_1351, point.field_1350, 1.5f, false, class_1937.class_7867.field_40888);
        } finally { BURST.set(false); }
    }
}
