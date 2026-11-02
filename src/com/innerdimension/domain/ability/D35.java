package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;
import java.util.ArrayList;
import java.util.List;
/** Vanilla explosion behavior policy for domain blocks and shell containment. */
public final class D35 implements DomainAbility {
    public static final D35 INSTANCE = new D35();
    private D35() {}
    @Override public String id() { return "minecraft:creeper"; }
    @Override public String code() { return "D35"; }
    public static boolean protectsBlock(class_1937 world, class_2338 pos) {
        if (world == null || pos == null) return false;
        for (DomainSession session : DomainSessionIndex.all(world)) {
            if (active(session) && session.hasCode("D35") && inShell(session, pos)) return true;
        }
        return false;
    }
    /** Every domain contains its outgoing block blast, regardless of engraved abilities. */
    public static boolean blocksBlast(class_1927 explosion, class_2338 pos) {
        if (explosion == null || pos == null) return false;
        class_1937 world = explosion.method_64504();
        if(D64.locked(world,pos))return true;
        class_243 origin = explosion.method_55109();
        for (DomainSession session : DomainSessionIndex.all(world)) {
            if (!active(session)) continue;
            if (blockedBy(session, origin, pos)) return true;
        }
        return false;
    }
    /** The caller supplies the completed list, after optimization mods have appended blocks. */
    public static List<class_2338> filterBlast(class_1927 explosion, List<class_2338> original) {
        if (original.isEmpty() || DomainSessionIndex.all(explosion.method_64504()).isEmpty()) return original;
        List<class_2338> kept = new ArrayList<>(original.size());
        for (class_2338 pos : original) if (!blocksBlast(explosion, pos)) kept.add(pos);
        return kept.size() == original.size() ? original : kept;
    }
    private static boolean blockedBy(DomainSession session, class_243 origin, class_2338 pos) {
        boolean inside = inShell(session, pos);
        return (session.hasCode("D35") && inside) || (insideShell(session, origin) && !inside);
    }
    private static boolean active(DomainSession session) {
        return session.stable() && !session.ended() && session.phaseActive();
    }
    private static boolean insideShell(DomainSession session, class_243 point) {
        return point.field_1352 >= session.minX() - 1 && point.field_1352 < session.maxX() + 1
                && point.field_1351 >= session.minY() - 1 && point.field_1351 < session.maxY() + 1
                && point.field_1350 >= session.minZ() - 1 && point.field_1350 < session.maxZ() + 1;
    }
    private static boolean inShell(DomainSession session, class_2338 pos) {
        // Include the floor and boundary cells, which the walkable-interior query excludes.
        return insideShell(session, new class_243(pos.method_10263() + 0.5,
                pos.method_10264() + 0.5, pos.method_10260() + 0.5));
    }
}

