package com.innerdimension.client;

/** Pure subtitle timing and text segmentation; all times are game ticks. */
public final class DomainSubtitleAnimation {
    public static final int NAME_TICKS = 20;
    /** Crest heart inserted when the expanded loadout carries the chicken trait. */
    public static final String HEART = "\u2661";
    private DomainSubtitleAnimation() {}
    /**
     * Inserts the heart at the middle of a display string: {@code 领域展开} becomes
     * {@code 领域♡展开}. A spaced title is split at the whitespace run closest to the middle
     * instead, so the heart never lands inside a word: {@code Domain ♡ Expansion}, never
     * {@code Domain E♡xpansion}. The name text is only decorated when the player named the
     * domain, because an unnamed domain has no name unit to decorate at all.
     */
    public static String heart(String text,int layout){return layout==2?HEART+text+HEART:layout==1?heart(text):text;}
    public static String heart(String text) {
        if (text == null || text.isEmpty()) return text;
        int[] points = text.codePoints().toArray();
        int middle = (points.length + 1) / 2;
        int space = -1, distance = Integer.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {
            if (!Character.isWhitespace(points[i])) continue;
            int candidate = Math.abs(i + 1 - middle);
            if (candidate < distance) { distance = candidate; space = i; }
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < points.length; i++) {
            if (space >= 0 && i == space) { out.append(' ').append(HEART).append(' '); continue; }
            if (space < 0 && i == middle) out.append(HEART);
            out.appendCodePoint(points[i]);
        }
        if (space < 0 && middle >= points.length) out.append(HEART);
        return out.toString();
    }
    public static String[] characters(String text) {
        return text.codePoints().mapToObj(cp -> new String(Character.toChars(cp))).toArray(String[]::new);
    }
    public static String[] nameUnits(String name) {
        boolean cjk = name.codePoints().anyMatch(cp -> {
            var script = Character.UnicodeScript.of(cp);
            return script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL;
        });
        if (cjk) return characters("「" + name + "」");
        var words = name.strip().split("\\s+");
        String[] units = new String[words.length + 2];
        units[0] = "「";
        for (int i = 0; i < words.length; i++) units[i + 1] = (i == 0 ? "" : " ") + words[i];
        units[units.length - 1] = "」";
        return units;
    }
    public static double reveal(double tick, int duration, int index, int count) {
        double progress = tick / Math.max(1, duration);
        return clamp((progress - .30 * index / Math.max(1, count - 1)) / .15) * clamp((1-progress)/.20);
    }
    public static double gap(double tick) {
        double progress = clamp(tick / NAME_TICKS);
        // Start at rest when stage two actually finishes, then accelerate smoothly.
        return 26 * progress * progress;
    }
    public static double alpha(double tick) {
        return 1 - clamp((tick - 10) / (NAME_TICKS - 10));
    }
    private static double clamp(double value) { return Math.max(0, Math.min(1, value)); }
}
