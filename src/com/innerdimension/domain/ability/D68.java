package com.innerdimension.domain.ability;

import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D68 minecraft:ender_dragon (purple) — 龙息禁区 / dragon-breath exclusion zone.
 *
 * <p>Every enemy is on its own 20 tick timer; when it comes due it takes exactly 4 points of
 * {@code inner_dimension:domain_dragon_breath} through the autonomous damage helper (which
 * clears the vanilla invulnerability frame so two abilities can land on the same tick and
 * attributes the kill correctly). The damage can kill. No cloud is placed and no block is
 * destroyed. A D04 cancellation still consumes the interval, so the timer simply waits for the
 * next period.</p>
 *
 * <p>Server-synced particles are emitted at the victim through
 * {@code ServerWorld.spawnParticles}, so every nearby client sees the same marker.</p>
 *
 * <p>// VERIFY: the dragon-breath particle constant was resolved by positional matching of the
 * cached {@code ParticleTypes} enumeration against the intermediary particle registry
 * (index 8; {@code class_2398.field_11216}, an option type over {@code class_11978}). Re-check
 * {@code field_11216} against {@code net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH}
 * if the mapping set is ever regenerated.</p>
 */
public final class D68 implements DomainAbility {
    public static final D68 INSTANCE = new D68();
    private D68() {}
    @Override public String id() { return "minecraft:ender_dragon"; }
    @Override public String code() { return "D68"; }
    @Override public int priority() { return 100; }

    private static final long INTERVAL = 20;
    private static final float DAMAGE = 4.0f;

    @Override
    public void onTick(DomainSession s, long now) {
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D68")) {
            if (!com.innerdimension.domain.runtime.DomainObjects.matches(s,"D68",entity)) continue;
            if (!s.due("d68", entity.method_5667(), INTERVAL, now)) continue;
            s.arm("d68", entity.method_5667(), now);
            DomainDamagePipeline.hurt(entity, s.owner(), DomainDamageTypes.DRAGON_BREATH, DAMAGE);
            particles(s, entity);
        }
    }

    private static void particles(DomainSession s, class_1309 entity) {
        if (!(s.world instanceof class_3218 world)) return;
        class_243 centre = entity.method_33571();
        world.method_65096(class_11978.method_74410(class_2398.field_11216, 1.0f),
                centre.field_1352, centre.field_1351, centre.field_1350,
                6, 0.25, 0.25, 0.25, 0.01);
    }
}
