package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.function.Predicate;
/** Additional glideable equipment; vanilla owns takeoff, physics and durability handling. */
public final class D41 implements DomainAbility {
    public static final D41 INSTANCE = new D41();
    private D41() {}
    @Override public String id() { return "minecraft:phantom"; }
    @Override public String code() { return "D41"; }
    private static Predicate<class_1309> clientState = entity -> false;
    public static void installClientState(Predicate<class_1309> state) {
        clientState = state == null ? entity -> false : state;
    }
    /** Ephemeral BODY equipment, never saved, dropped or moved into the inventory. */
    public static class_1799 wings() {
        class_1799 stack = new class_1799(class_1802.field_8833);
        stack.method_57379(class_9334.field_54196,
                class_10192.method_64202(class_1304.field_48824).method_64203());
        return stack;
    }
    public static boolean allowsVanillaGlide(class_1309 entity) {
        if (!(entity instanceof class_1657) || !entity.method_5805() || entity.method_7325()) return false;
        if (entity.method_73183().method_8608()) return clientState.test(entity);
        return available(DomainSessionIndex.byOwner(entity.method_5667()), entity);
    }
    public static boolean available(DomainSession session, class_1309 entity) {
        return session != null && session.stable() && !session.ended() && session.phaseActive()
                && session.hasCode("D41") && com.innerdimension.domain.runtime.DomainObjects.matches(session,"D41",entity);
    }
    public static boolean isGliding(DomainSession session) {
        class_3222 owner = session == null ? null : session.owner();
        return owner != null && available(session, owner) && owner.method_6128();
    }
}
