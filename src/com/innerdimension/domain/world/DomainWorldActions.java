package com.innerdimension.domain.world;

import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;

import java.util.*;

/**
 * Transactional world edits performed by abilities. Every helper here works on real
 * blocks only: a projection snapshot is never written, a position outside the session is
 * never touched, and the permission check is repeated at the moment of the write.
 */
public final class DomainWorldActions {
    /** Rotating cursors so a large candidate set is walked over several ticks. */
    private static final Map<DomainSession, Map<String, Integer>> CURSORS = new IdentityHashMap<>();

    private DomainWorldActions() {}

    /** Re-checks everything that could have moved since the candidate list was built. */
    public static boolean writable(DomainSession session, class_2338 pos) {
        if (session == null || pos == null) return false;
        if (!session.containsBlock(pos)) return false;
        if (!(session.world instanceof class_3218 world)) return false;
        if (world.method_8608()) return false;
        return true;
    }

    /** Places a state in the real world, generating no drops and no projection writes. */
    public static boolean setBlock(DomainSession session, class_2338 pos, class_2680 state, int flags) {
        if (!writable(session, pos)) return false;
        if(com.innerdimension.domain.ability.D64.locked(session.world,pos)&&session.world.method_8320(pos).method_26215()!=state.method_26215())return false;
        class_3218 world = (class_3218) session.world;
        return world.method_8652(pos, state, flags);
    }

    /** Gives back a rotating slice of the candidates for this session and key. */
    public static List<class_2338> slice(DomainSession session, String key, List<class_2338> candidates, int budget) {
        if (candidates.isEmpty() || budget <= 0) return List.of();
        Map<String, Integer> cursors = CURSORS.computeIfAbsent(session, s -> new HashMap<>());
        int start = Math.floorMod(cursors.getOrDefault(key, 0), candidates.size());
        List<class_2338> out = new ArrayList<>(budget);
        for (int i = 0; i < Math.min(budget, candidates.size()); i++) out.add(candidates.get((start + i) % candidates.size()));
        cursors.put(key, (start + budget) % candidates.size());
        return out;
    }

    public static void forget(DomainSession session) { CURSORS.remove(session); }

    public static void clear() { CURSORS.clear(); }

    /* ------------------------------------------------------------- copper */

    private static final List<String> STAGES = List.of("", "exposed_", "weathered_", "oxidized_");

    /**
     * Oxidises or de-oxidises any vanilla copper shape by identifier family, which keeps
     * every shared property, the waxed form and the block entity untouched.
     *
     * @param direction +1 towards oxidation, -1 back to unoxidised
     * @return the mapped state, or null when this is not a copper family block
     */
    public static class_2680 shiftOxidation(class_2680 state, int direction) {
        if (state == null) return null;
        class_2960 id = class_7923.field_41175.method_10221(state.method_26204());
        if (id == null || !id.method_12836().equals("minecraft")) return null;
        String path = id.method_12832();
        String waxed = "";
        if (path.startsWith("waxed_")) { waxed = "waxed_"; path = path.substring("waxed_".length()); }
        int stage = 0;
        for (int i = STAGES.size() - 1; i >= 1; i--) {
            if (path.startsWith(STAGES.get(i))) { stage = i; path = path.substring(STAGES.get(i).length()); break; }
        }
        if (!path.startsWith("copper") && !path.startsWith("cut_copper") && !path.startsWith("chiseled_copper")
                && !path.startsWith("lightning_rod")) return null;
        int next = Math.max(0, Math.min(STAGES.size() - 1, stage + direction));
        if (next == stage) return null;
        class_2960 target = class_2960.method_60655("minecraft", waxed + STAGES.get(next) + path);
        class_2248 block = class_7923.field_41175.method_10223(target).map(class_6880::comp_349).orElse(null);
        if (block == null) return null;
        return rebase(state, block);
    }

    /** Copies every property the target block also has, leaving the rest at its default. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static class_2680 rebase(class_2680 from, class_2248 to) {
        class_2680 result = to.method_9564();
        for (class_2769<?> property : from.method_28501()) {
            if (!result.method_28498(property)) continue;
            result = (class_2680) result.method_11657((class_2769) property, (Comparable) from.method_11654((class_2769) property));
        }
        return result;
    }

    /* -------------------------------------------------------------- crops */

    private static class_2248 block(String id) {
        int colon = id.indexOf(':');
        return class_7923.field_41175
                .method_10223(class_2960.method_60655(id.substring(0, colon), id.substring(colon + 1)))
                .map(class_6880::comp_349).orElse(null);
    }

    private static class_1792 item(String id) {
        int colon = id.indexOf(':');
        return class_7923.field_41178
                .method_10223(class_2960.method_60655(id.substring(0, colon), id.substring(colon + 1)))
                .map(class_6880::comp_349).orElse(null);
    }

    /** Blocks that D21 may harvest and replant. */
    public static boolean harvestable(class_2680 state) {
        class_2248 block = state.method_26204();
        return block == block("minecraft:wheat") || block == block("minecraft:carrots")
                || block == block("minecraft:potatoes") || block == block("minecraft:beetroots")
                || block == block("minecraft:nether_wart") || block == block("minecraft:cocoa");
    }

    public static boolean mature(class_2680 state) {
        class_2769<Integer> age = ageProperty(state);
        if (age == null) return true;
        Comparable<?> value = state.method_11654(age);
        int max = maxAge(state.method_26204());
        return value instanceof Integer current && current >= max;
    }

    private static int maxAge(class_2248 block) {
        if (block == block("minecraft:nether_wart")) return 3;
        if (block == block("minecraft:cocoa")) return 2;
        return 7;
    }

    @SuppressWarnings("unchecked")
    public static class_2769<Integer> ageProperty(class_2680 state) {
        for (class_2769<?> property : state.method_28501()) {
            if (property.method_11899().equals("age") && property.method_11902() == Integer.class)
                return (class_2769<Integer>) property;
        }
        return null;
    }

    /** Seed item required to replant the given mature crop, or null. */
    public static class_1792 seed(class_2680 state) {
        class_2248 block = state.method_26204();
        if (block == block("minecraft:wheat")) return item("minecraft:wheat_seeds");
        if (block == block("minecraft:carrots")) return item("minecraft:carrot");
        if (block == block("minecraft:potatoes")) return item("minecraft:potato");
        if (block == block("minecraft:beetroots")) return item("minecraft:beetroot_seeds");
        if (block == block("minecraft:nether_wart")) return item("minecraft:nether_wart");
        if (block == block("minecraft:cocoa")) return item("minecraft:cocoa_beans");
        return null;
    }
}
