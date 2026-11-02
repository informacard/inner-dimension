package com.innerdimension.centrifuge;

import com.nonid.LiquidHolder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class LiquidProvenanceLedger {
    private static final Map<Object, LinkedHashMap<class_2960, Integer>> LEDGERS = new WeakHashMap<>();

    private LiquidProvenanceLedger() {}

    public static synchronized void record(class_3222 player, int actualAdded, Map<class_2960, Integer> sources) {
        if (!(player instanceof LiquidHolder holder) || actualAdded <= 0 || sources == null || sources.isEmpty()) return;
        LinkedHashMap<class_2960, Integer> ledger = LEDGERS.computeIfAbsent(player, ignored -> new LinkedHashMap<>());
        reconcile(ledger, Math.max(0, holder.getLiquidStored() - actualAdded));
        int sourceTotal = sources.values().stream().filter(v -> v != null && v > 0).mapToInt(Integer::intValue).sum();
        if (sourceTotal <= 0) return;
        int assigned = 0;
        int index = 0;
        int size = sources.size();
        for (var entry : sources.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) continue;
            index++;
            int amount = index == size ? actualAdded - assigned
                    : (int)Math.floor((double)actualAdded * entry.getValue() / sourceTotal);
            if (amount > 0) {
                ledger.merge(entry.getKey(), amount, Integer::sum);
                assigned += amount;
            }
        }
    }

    public static synchronized void attachBottle(LiquidHolder holder, class_1799 bottle) {
        if (holder == null || bottle == null || bottle.method_7960()) return;
        int required = Math.max(1, (int)Math.ceil(holder.getLiquidCapacity() * 0.3F));
        LinkedHashMap<class_2960, Integer> ledger = LEDGERS.computeIfAbsent(holder, ignored -> new LinkedHashMap<>());
        // Only a plain ENTITY tank has an unambiguous legacy source. Honey/fertile
        // nectar must carry an actual ledger; never infer bee from the bottle look.
        if (ledger.isEmpty() && holder.getLiquidComposition() == LiquidHolder.LiquidComposition.ENTITY
                && holder.getLiquidEntityTypeId() != null && holder.getLiquidStored() > 0) {
            ledger.put(holder.getLiquidEntityTypeId(), holder.getLiquidStored());
        }
        reconcile(ledger, holder.getLiquidStored());
        Map<class_2960, Integer> bottleSources = proportional(ledger, Math.min(required, holder.getLiquidStored()));
        if (!bottleSources.isEmpty()) CentrifugeBottleData.write(bottle, bottleSources, required);
    }

    public static synchronized String encode(Object holder) {
        LinkedHashMap<class_2960, Integer> ledger = LEDGERS.get(holder);
        if (ledger == null || ledger.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        ledger.forEach((id, amount) -> {
            if (id == null || amount == null || amount <= 0) return;
            if (!out.isEmpty()) out.append(';');
            out.append(id).append('=').append(amount);
        });
        return out.toString();
    }

    public static synchronized void decode(Object holder, String raw) {
        LinkedHashMap<class_2960, Integer> ledger = new LinkedHashMap<>();
        if (raw != null) for (String token : raw.split(";")) {
            int split = token.lastIndexOf('=');
            if (split <= 0) continue;
            class_2960 id = class_2960.method_12829(token.substring(0, split));
            try {
                int amount = Integer.parseInt(token.substring(split + 1));
                if (id != null && amount > 0) ledger.merge(id, amount, Integer::sum);
            } catch (NumberFormatException ignored) {}
        }
        if (ledger.isEmpty()) LEDGERS.remove(holder); else LEDGERS.put(holder, ledger);
    }

    private static void reconcile(LinkedHashMap<class_2960, Integer> ledger, int target) {
        int total = ledger.values().stream().mapToInt(Integer::intValue).sum();
        if (target <= 0) { ledger.clear(); return; }
        if (total == target || total <= 0) return;
        Map<class_2960, Integer> scaled = proportional(ledger, target);
        ledger.clear();
        ledger.putAll(scaled);
    }

    private static Map<class_2960, Integer> proportional(Map<class_2960, Integer> source, int target) {
        LinkedHashMap<class_2960, Integer> out = new LinkedHashMap<>();
        int total = source.values().stream().filter(v -> v != null && v > 0).mapToInt(Integer::intValue).sum();
        if (total <= 0 || target <= 0) return out;
        int assigned = 0;
        int seen = 0;
        int valid = (int)source.entrySet().stream().filter(e -> e.getKey() != null && e.getValue() != null && e.getValue() > 0).count();
        for (var entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) continue;
            seen++;
            int amount = seen == valid ? target - assigned : (int)Math.floor((double)target * entry.getValue() / total);
            if (amount > 0) { out.put(entry.getKey(), amount); assigned += amount; }
        }
        return out;
    }
}
