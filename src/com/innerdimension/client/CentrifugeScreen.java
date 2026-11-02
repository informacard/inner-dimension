package com.innerdimension.client;

import com.innerdimension.centrifuge.CentrifugeBlockEntity;
import com.innerdimension.centrifuge.CentrifugeBottleData;
import com.innerdimension.centrifuge.CentrifugeScreenHandler;
import com.innerdimension.network.CentrifugeActionPayload;
import java.util.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.*;

/** A searchable, single-column reservoir list above the normal player inventory. */
public final class CentrifugeScreen extends class_465<CentrifugeScreenHandler> {
    private static final int W = 300, H = 236, ROWS = 3, LIST_X = 108, LIST_Y = 56, LIST_W = 178, LIST_H = 60;
    private static final class_2960 BACKGROUND = class_2960.method_60655("inner_dimension", "textures/gui/centrifuge.png");
    private final LiquidRow[] rows = new LiquidRow[ROWS];
    private class_342 search;
    private class_4185 extract, discard;
    private String query = "";
    private class_2960 selected;
    private int scroll;
    private boolean draggingScrollbar;
    private CentrifugeBottleData.Storage storage = new CentrifugeBottleData.Storage(Map.of(), CentrifugeBottleData.LEGACY_BOTTLE_ML, "");
    private List<Map.Entry<class_2960, Integer>> visible = List.of();
    private Set<String> searchIds = Set.of();
    private SpeciesSearch speciesSearch;

    public CentrifugeScreen(CentrifugeScreenHandler handler, class_1661 inventory, class_2561 title) {
        super(handler, inventory, title);
        field_2792 = W; field_2779 = H; field_25269 = 69; field_25270 = 143;
    }
    @Override protected void method_25426() {
        super.method_25426();
        search = method_37063(new class_342(field_22793, field_2776 + LIST_X, field_2800 + 25, LIST_W, 18, tr("search")));
        search.method_1880(128);
        search.method_47404(tr("search"));
        search.method_1852(query);
        search.method_1863(value -> { query = value; scroll = 0; selected = null; refresh(); });
        for (int i = 0; i < ROWS; i++) {
            final int row = i;
            rows[i] = method_37063(new LiquidRow(field_2776 + LIST_X, field_2800 + LIST_Y + i * 20,
                    LIST_W, 20, b -> {
                        int index = scroll + row;
                        if (index < visible.size()) selected = visible.get(index).getKey();
                    }));
        }
        extract = method_37063(class_4185.method_46430(tr("extract"), b -> send(false))
                .method_46434(field_2776 + LIST_X, field_2800 + 122, 86, 20).method_46431());
        discard = method_37063(class_4185.method_46430(tr("discard"), b -> send(true))
                .method_46434(field_2776 + LIST_X + 90, field_2800 + 122, 88, 20).method_46431());
        extract.method_47400(class_7919.method_47407(tr("extract.help")));
        discard.method_47400(class_7919.method_47407(tr("discard.help")));
    }
    private void send(boolean discardLiquid) {
        refresh();
        if (selected == null || !storage.sources().containsKey(selected)) return;
        ClientPlayNetworking.send(new CentrifugeActionPayload(field_2797.field_7763, selected.toString(), discardLiquid));
        if (discardLiquid) selected = null;
    }
    private void refresh() {
        storage = CentrifugeBottleData.readStorage(field_2797.field_7761.get(CentrifugeBlockEntity.ENERGY).method_7677());
        Set<String> ids = new HashSet<>();
        storage.sources().keySet().forEach(id -> ids.add(id.toString()));
        if (speciesSearch == null || !ids.equals(searchIds)) { searchIds = Set.copyOf(ids); speciesSearch = new SpeciesSearch(ids); }
        visible = storage.sources().entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Object::toString)))
                .filter(e -> speciesSearch.matches(e.getKey().toString(), query)).toList();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, visible.size() - ROWS)));
        if (selected != null && !storage.sources().containsKey(selected)) selected = null;
    }
    @Override public void method_25394(class_332 g, int mx, int my, float delta) {
        refresh();
        for (int i = 0; i < ROWS; i++) {
            int index = scroll + i;
            rows[i].entry = index < visible.size() ? visible.get(index) : null;
            rows[i].field_22763 = rows[i].field_22764 = rows[i].entry != null;
            if (rows[i].entry != null) {
                var entry = rows[i].entry;
                class_2561 text = class_2561.method_43469("container.inner_dimension.centrifuge.source", name(entry.getKey()), entry.getValue());
                rows[i].method_25355(text); rows[i].method_47400(class_7919.method_47407(text));
            }
        }
        discard.field_22764 = selected != null;
        extract.field_22764 = selected != null && storage.sources().getOrDefault(selected, 0) >= storage.bottleMl()
                && !field_2797.field_7761.get(CentrifugeBlockEntity.BOTTLES).method_7677().method_7960();
        super.method_25394(g, mx, my, delta);
        method_2380(g, mx, my);
        if (inside(mx, my, field_2776 + 38, field_2800 + 105, 65, 18))
            g.method_51438(field_22793, class_2561.method_43469("container.inner_dimension.centrifuge.bottle_capacity", storage.bottleMl()), mx, my);
        if (inside(mx, my, field_2776 + 7, field_2800 + 24, 94, 31))
            g.method_51438(field_22793, storage.status().isEmpty() ? tr("input.help") : tr(storage.status()), mx, my);
    }
    @Override protected void method_2389(class_332 g, float delta, int mx, int my) {
        g.method_25290(class_10799.field_56883, BACKGROUND, field_2776, field_2800, 0, 0, W, H, W, H);
        g.method_25294(field_2776 + 9, field_2800 + 134, field_2776 + 9 + Math.round(91 * energy() / 16f), field_2800 + 140, 0xff59a83f);
        int thumbHeight = thumbHeight(), thumbY = field_2800 + LIST_Y;
        if (visible.size() > ROWS) thumbY += (LIST_H - thumbHeight) * scroll / (visible.size() - ROWS);
        g.method_25294(field_2776 + 288, thumbY, field_2776 + 294, thumbY + thumbHeight, visible.size() > ROWS ? 0xff999999 : 0xff666666);
    }
    @Override protected void method_2388(class_332 g, int mx, int my) {
        super.method_2388(g, mx, my);
        label(g, "input", 8, 25); label(g, "fuel", 8, 61); label(g, "bottles", 8, 97);
        label(g, "name", LIST_X + 4, 46); label(g, "volume", LIST_X + 119, 46);
        g.method_27535(field_22793, class_2561.method_43469("container.inner_dimension.centrifuge.volume_ml", storage.bottleMl()), 40, 106, 0xff404040);
        label(g, "per_bottle", 40, 116);
        g.method_27535(field_22793, class_2561.method_43469("container.inner_dimension.centrifuge.energy", energy(), 16), 8, 124, 0xff404040);
        if (visible.isEmpty()) g.method_27535(field_22793, tr(storage.sources().isEmpty() ? "empty" : "no_results"), LIST_X + 4, LIST_Y + 5, 0xffdddddd);
    }
    @Override public boolean method_25404(class_11908 event) {
        if (search != null && search.method_25370() && event.comp_4795() != 256) {
            search.method_25404(event); return true;
        }
        return super.method_25404(event);
    }
    @Override public boolean method_25401(double x, double y, double horizontal, double vertical) {
        if (vertical != 0 && inside(x, y, field_2776 + LIST_X, field_2800 + LIST_Y, LIST_W + 8, LIST_H)) {
            scroll = Math.max(0, Math.min(Math.max(0, visible.size() - ROWS), scroll + (vertical < 0 ? 1 : -1))); return true;
        }
        return super.method_25401(x, y, horizontal, vertical);
    }
    @Override public boolean method_25402(class_11909 event, boolean doubled) {
        if (visible.size() > ROWS && inside(event.comp_4798(), event.comp_4799(), field_2776 + 288, field_2800 + LIST_Y, 6, LIST_H)) {
            draggingScrollbar = true; scrollTo(event.comp_4799()); return true;
        }
        return super.method_25402(event, doubled);
    }
    @Override public boolean method_25403(class_11909 event, double dx, double dy) {
        if (draggingScrollbar) { scrollTo(event.comp_4799()); return true; }
        return super.method_25403(event, dx, dy);
    }
    @Override public boolean method_25406(class_11909 event) {
        if (draggingScrollbar) { draggingScrollbar = false; return true; }
        return super.method_25406(event);
    }
    private int thumbHeight() { return Math.max(10, LIST_H * ROWS / Math.max(ROWS, visible.size())); }
    private void scrollTo(double y) {
        int range = Math.max(0, visible.size() - ROWS), travel = LIST_H - thumbHeight();
        scroll = travel == 0 ? 0 : Math.max(0, Math.min(range, (int)Math.round((y - field_2800 - LIST_Y - thumbHeight() / 2.0) * range / travel)));
    }
    private int energy() {
        class_1799 state = field_2797.field_7761.get(CentrifugeBlockEntity.ENERGY).method_7677();
        return state.method_7960() ? 0 : Math.max(0, Math.min(16, state.method_7947() - 1));
    }
    private void label(class_332 g, String key, int x, int y) { g.method_27535(field_22793, tr(key), x, y, 0xff404040); }
    private static class_2561 tr(String key) { return class_2561.method_43471("container.inner_dimension.centrifuge." + key); }
    private static class_2561 name(class_2960 id) { return class_2561.method_43471(CentrifugeBottleData.entityTranslationKey(id)); }
    private static boolean inside(double x, double y, int rx, int ry, int w, int h) { return x >= rx && y >= ry && x < rx + w && y < ry + h; }

    private final class LiquidRow extends class_4185 {
        private Map.Entry<class_2960, Integer> entry;
        LiquidRow(int x, int y, int w, int h, class_4241 press) { super(x, y, w, h, class_2561.method_43470(""), press, field_40754); }
        @Override protected void method_75752(class_332 g, int mx, int my, float delta) {
            if (entry == null) return;
            int x = method_46426(), y = method_46427(), w = method_25368();
            boolean chosen = entry.getKey().equals(selected), hover = method_25405(mx, my);
            g.method_25294(x, y, x + w, y + 20, chosen ? 0xff83b66a : 0xff373737);
            g.method_25294(x + 1, y + 1, x + w - 1, y + 19, chosen ? 0xff466c35 : hover ? 0xff777777 : 0xff555555);
            String volume = class_2561.method_43469("container.inner_dimension.centrifuge.volume_ml", entry.getValue()).getString();
            int volumeWidth = field_22793.method_1727(volume), nameWidth = w - volumeWidth - 18;
            String text = name(entry.getKey()).getString();
            if (field_22793.method_1727(text) > nameWidth) text = field_22793.method_27523(text, Math.max(0, nameWidth - 7)) + "…";
            g.method_51433(field_22793, text, x + 6, y + 6, 0xffffffff, false);
            g.method_51433(field_22793, volume, x + w - volumeWidth - 6, y + 6, 0xffffffff, false);
        }
    }
}

