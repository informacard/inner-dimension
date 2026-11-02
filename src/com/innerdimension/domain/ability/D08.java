package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainRelations;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
import java.util.*;

/**
 * D08 minecraft:cat (green) — "驱厄".
 *
 * <p>Bane-driving: a creature selected by the object mask is refused permission to target
 * the domain owner. Only the target <em>choice</em> is refused; the mob keeps its own AI,
 * movement and pathing.</p>
 *
 * <p>The periodic feline damage this ability used to deal was removed by design: D08 is now
 * pure denial, and the load weight dropped from 4 to 3 accordingly. It is therefore no
 * longer one of the purely damaging abilities, and it no longer collides with D04's
 * no-damage field.</p>
 *
 * <p>GAP: the approved text also wants the mob to pick a reachable point away from the
 * owner. {@code AiHook} exposes only denyTarget / denyMove / preferredTargets — there is no
 * injection point for "choose a flee destination" that does not amount to faking pathing
 * (which the API forbids). Only the documented fallback ("cannot flee -&gt; drop the attack
 * intent") is implemented; movement is left entirely to the mob's own AI.</p>
 */
public final class D08 implements DomainAbility, AiHook {
    public static final D08 INSTANCE = new D08();
    private D08() {}
    @Override public String id() { return "minecraft:cat"; }
    @Override public String code() { return "D08"; }

    /** Species the ability is themed around; kept for callers that classify bane prey. */
    private static final Set<String> PREY = Set.of("minecraft:creeper", "minecraft:phantom");

    /** A selected creature may not target the owner while the domain holds it. */
    @Override
    public Boolean denyTarget(DomainSession session, class_1308 mob, class_1309 target) {
        if (!com.innerdimension.domain.runtime.DomainObjects.matches(session,"D08",mob)) return null;
        if (target == null || !target.method_5667().equals(session.ownerId)) return null;
        return Boolean.TRUE;
    }

    public static boolean isPrey(class_1297 entity) {
        return PREY.contains(DomainRelations.typeId(entity));
    }
}
