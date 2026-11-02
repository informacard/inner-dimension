package com.innerdimension.projection;

/** Per-caster drawing choices, transmitted with each open-domain expansion. */
public record FloorPatternSettings(Mode mode, String expression, boolean preserveCenter,
                                   String foreground, String background) {
    public enum Mode {
        TILE(""), RINGS("r % 4 < 1"), BROKEN_RINGS("r % 4 < 1 && abs(sin(theta)) > 0.25"),
        SPIRAL("(r - theta * 2 / pi) % 4 < 1"), SPOKES("cos(theta * 8) > 0.85"), CUSTOM("r % 4 < 1");
        public final String expression;
        Mode(String expression) { this.expression=expression; }
    }
    public static final FloorPatternSettings DEFAULT = new FloorPatternSettings(Mode.TILE, "r % 4 < 1", true, "", "");
    public FloorPatternSettings {
        if(mode==null||expression==null||foreground==null||background==null
                ||expression.length()>512||foreground.length()>128||background.length()>128)
            throw new IllegalArgumentException("Invalid floor settings");
    }
    public String effectiveExpression() { return mode==Mode.CUSTOM ? expression : mode.expression; }
    public PolarExpression.Node compile() { return mode==Mode.TILE ? null : PolarExpression.compile(effectiveExpression()); }
    public static boolean central(int x,int z) { return x>=0&&x<16&&z>=0&&z<16; }
    public static boolean foreground(PolarExpression.Node node,int x,int z) {
        double dx=x+.5-8,dz=z+.5-8;
        return PolarExpression.draws(node,Math.hypot(dx,dz),Math.atan2(dz,dx));
    }
}
