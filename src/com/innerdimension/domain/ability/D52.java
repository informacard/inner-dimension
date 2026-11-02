package com.innerdimension.domain.ability;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.function.Predicate;
/** Lava walking is a collision surface, so falling and jumping use vanilla's movement solve. */
public final class D52 implements DomainAbility, DamageHook {
    public static final D52 INSTANCE = new D52();
    private static Predicate<class_1309> clientState = e -> false;
    private D52() {}
    @Override public String id() { return "minecraft:strider"; }
    @Override public String code() { return "D52"; }
    @Override public int priority() { return 31; }
    public static void installClientState(Predicate<class_1309> state) { clientState = state; }
    public static boolean protectedFromLava(class_1297 entity) {
        if (!(entity instanceof class_1309 living)) return false;
        if (entity.method_73183().method_8608()) return clientState.test(living);
        return DomainSessionIndex.covering(entity).stream().anyMatch(s->s.hasCode("D52")&&com.innerdimension.domain.runtime.DomainObjects.matches(s,"D52",entity));
    }
    public static boolean walksOnLava(class_1297 entity) {
        return entity != null && !entity.method_5715() && protectedFromLava(entity);
    }
    public static boolean surfaceEligible(double feet, double surface) { return feet >= surface - 0.08; }
    @Override public Boolean preDamage(DomainSession session, class_1309 victim, class_1282 source, float amount) {
        if(!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D52",victim))return null;
        return DomainDamageTypes.isLava(source) ? Boolean.TRUE : null;
    }
    @Override public void onTick(DomainSession s, long now) {
        for (class_1309 entity : com.innerdimension.domain.runtime.DomainObjects.inside(s,"D52")) {
            // Only extinguish existing lava contact. Arbitrary ordinary fire is still harmful.
            if (entity.method_5771() || com.innerdimension.domain.geometry.DomainGeometry.inLava(entity)) entity.method_5646();
        }
    }
}
