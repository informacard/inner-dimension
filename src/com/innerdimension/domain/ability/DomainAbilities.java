package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import net.minecraft.*;

import java.util.*;

/** Registry of the sixty-nine D01-D69 handlers plus the per-session hook cache. */
public final class DomainAbilities {
    private static final Map<String, DomainAbility> BY_ID = new LinkedHashMap<>();
    private static final Map<String, DomainAbility> BY_CODE = new LinkedHashMap<>();
    private static final List<DomainAbility> ALL = new ArrayList<>();
    private static final Map<DomainSession, Map<Class<?>, List<?>>> CACHE = new IdentityHashMap<>();
    private static boolean registered;

    private DomainAbilities() {}

    public static synchronized void registerAll() {
        if (registered) return;
        registered = true;
        register(D01.INSTANCE);
        register(D02.INSTANCE);
        register(D03.INSTANCE);
        register(D04.INSTANCE);
        register(D05.INSTANCE);
        register(D06.INSTANCE);
        register(D07.INSTANCE);
        register(D08.INSTANCE);
        register(D09.INSTANCE);
        register(D10.INSTANCE);
        register(D11.INSTANCE);
        register(D12.INSTANCE);
        register(D13.INSTANCE);
        register(D14.INSTANCE);
        register(D15.INSTANCE);
        register(D16.INSTANCE);
        register(D17.INSTANCE);
        register(D18.INSTANCE);
        register(D19.INSTANCE);
        register(D20.INSTANCE);
        register(D21.INSTANCE);
        register(D22.INSTANCE);
        register(D23.INSTANCE);
        register(D24.INSTANCE);
        register(D25.INSTANCE);
        register(D26.INSTANCE);
        register(D27.INSTANCE);
        register(D28.INSTANCE);
        register(D29.INSTANCE);
        register(D30.INSTANCE);
        register(D31.INSTANCE);
        register(D32.INSTANCE);
        register(D33.INSTANCE);
        register(D34.INSTANCE);
        register(D35.INSTANCE);
        register(D36.INSTANCE);
        register(D37.INSTANCE);
        register(D38.INSTANCE);
        register(D39.INSTANCE);
        register(D40.INSTANCE);
        register(D41.INSTANCE);
        register(D42.INSTANCE);
        register(D43.INSTANCE);
        register(D44.INSTANCE);
        register(D45.INSTANCE);
        register(D46.INSTANCE);
        register(D47.INSTANCE);
        register(D48.INSTANCE);
        register(D49.INSTANCE);
        register(D50.INSTANCE);
        register(D51.INSTANCE);
        register(D52.INSTANCE);
        register(D53.INSTANCE);
        register(D54.INSTANCE);
        register(D55.INSTANCE);
        register(D56.INSTANCE);
        register(D57.INSTANCE);
        register(D58.INSTANCE);
        register(D59.INSTANCE);
        register(D60.INSTANCE);
        register(D61.INSTANCE);
        register(D62.INSTANCE);
        register(D63.INSTANCE);
        register(D64.INSTANCE);
        register(D65.INSTANCE);
        register(D66.INSTANCE);
        register(D67.INSTANCE);
        register(D68.INSTANCE);
        register(D69.INSTANCE);
        register(D71.INSTANCE);register(D72.INSTANCE);register(D73.INSTANCE);register(D74.INSTANCE);register(D75.INSTANCE);register(D76.INSTANCE);register(D77.INSTANCE);register(D78.INSTANCE);register(D79.INSTANCE);
    }

    private static void register(DomainAbility ability) {
        BY_ID.put(ability.id(), ability);
        BY_CODE.put(ability.code(), ability);
        ALL.add(ability);
    }

    public static DomainAbility byEntity(String id) { return BY_ID.get(id); }
    public static DomainAbility byCode(String code) { return BY_CODE.get(code); }
    public static List<DomainAbility> all() { return List.copyOf(ALL); }

    /** Abilities of one session, ordered by pipeline priority. */
    public static List<DomainAbility> of(DomainSession session) {
        if(session.ended())return session.loadout.stream().map(BY_ID::get).filter(Objects::nonNull).sorted(Comparator.comparingInt(DomainAbility::priority).thenComparing(DomainAbility::code)).toList();
        return hooks(session, DomainAbility.class);
    }

    /** Abilities of one session implementing a hook interface, ordered by priority. */
    @SuppressWarnings("unchecked")
    public static <T> List<T> hooks(DomainSession session, Class<T> type) {
        if(com.innerdimension.domain.DomainContest.suppressed(session.ownerId))return List.of();
        Map<Class<?>, List<?>> byType = CACHE.computeIfAbsent(session, s -> new HashMap<>());
        return (List<T>) byType.computeIfAbsent(type, t -> {
            List<DomainAbility> list = new ArrayList<>();
            for (String id : session.loadout) {
                DomainAbility ability = BY_ID.get(id);
                if (ability != null && t.isInstance(ability)) list.add(ability);
            }
            list.sort(Comparator.comparingInt(DomainAbility::priority).thenComparing(DomainAbility::code));
            return List.copyOf(list);
        });
    }

    /** Every ability active anywhere in one world, for world-scoped scans. */
    public static List<DomainAbility> activeIn(class_1937 world) {
        List<DomainAbility> out = new ArrayList<>();
        for (DomainSession session : DomainSessionIndex.all(world))
            for (DomainAbility ability : of(session)) if (!out.contains(ability)) out.add(ability);
        return out;
    }

    public static void forget(DomainSession session) { CACHE.remove(session); }

    public static void clear() { CACHE.clear(); }
}
