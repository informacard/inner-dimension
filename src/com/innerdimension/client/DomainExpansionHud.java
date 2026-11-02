package com.innerdimension.client;

import com.innerdimension.InnerDimension;
import com.innerdimension.projection.ProjectionAnimation;
import com.innerdimension.projection.ProjectionIndex;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

/** Owner announcement at the vanilla action-bar position, driven by tracked stage time. */
public final class DomainExpansionHud {
    private DomainExpansionHud() {}

    public static void register() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
                InnerDimension.id("domain_expansion_subtitle"), DomainExpansionHud::render);
    }

    private static void render(class_332 graphics, class_9779 ticks) {
        var client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1687 == null) return;
        var projection = ProjectionIndex.active(client.field_1687, client.field_1724.method_5667());
        if (projection == null || projection.phase() == ProjectionAnimation.RETRACTING) return;
        double elapsed = Math.max(0, projection.elapsed(ticks.method_60637(true)));
        boolean first = projection.phase() == ProjectionAnimation.EXPANDING;
        double releaseElapsed = projection.phase() == ProjectionAnimation.STABLE
                ? Math.max(0, projection.subtitleReleaseElapsed(ticks.method_60637(true))) : 0;
        if (!first && releaseElapsed >= DomainSubtitleAnimation.NAME_TICKS) return;
        String text = first ? class_2561.method_43471("inner_dimension.projection.opened").getString()
                : projection.domainName();
        // An unnamed domain has no name text at all, so the heart then lands on 领域展开 only.
        if (text.isBlank()) return;
        text = DomainSubtitleAnimation.heart(text,projection.heart());
        String[] units = first ? DomainSubtitleAnimation.characters(text) : DomainSubtitleAnimation.nameUnits(text);
        double gap = first ? 0 : DomainSubtitleAnimation.gap(releaseElapsed);
        double width = first ? 0 : gap * (units.length - 1);
        for (String unit : units) width += client.field_1772.method_1727(unit);
        float scale=1.25f;
        double x = (graphics.method_51421() - width*scale) / 2;
        int y = graphics.method_51443() - 80;
        for (int i = 0; i < units.length; i++) {
            double alpha = first ? DomainSubtitleAnimation.reveal(elapsed, projection.shellTicks(), i, units.length)
                    : DomainSubtitleAnimation.alpha(releaseElapsed);
            int opacity = (int)Math.round(alpha * 255);
            // Vanilla text treats very low alpha as opaque; skip those samples entirely.
            if (opacity > 3) {
                var matrices = graphics.method_51448();
                matrices.pushMatrix();
                try {
                    matrices.translate((float)x, y);
                    matrices.scale(scale,scale);
                    int color=gradient(projection.nameColor(0),projection.nameColor(1),projection.nameColor(2),units.length<=1?.5:(double)i/(units.length-1),projection.nameSplit(0),projection.nameSplit(1));
                    DomainSubtitlePainter.glyph(graphics,client.field_1772,units[i],0,0,(opacity<<24)|color,DomainSubtitlePainter.voidPalette(projection.nameColor(0),projection.nameColor(1),projection.nameColor(2)));
                } finally { matrices.popMatrix(); }
            }
            x += (client.field_1772.method_1727(units[i]) + gap)*scale;
        }
    }
    /**
     * Three-band name gradient. The bands are laid out by colour weight (see
     * {@code DomainAbilityCatalog.nameSplit}), not in equal thirds: the first band fades the
     * left colour into the main colour, the main colour then holds across its own share of the
     * text, and the last band fades the main colour into the right one. Weights 8/3/2 therefore
     * leave the main colour solid over 70% of the name instead of a single midpoint sample.
     */
    public static int gradient(int left,int center,int right,double p,int splitLeft,int splitRight){
        long millis=System.nanoTime()/1_000_000L;
        if(com.innerdimension.domain.DomainTextPalette.matches(left,center,right))return com.innerdimension.domain.DomainTextPalette.color(left,center,p,millis);
        left=animatedEndpoint(left,p,millis);center=animatedEndpoint(center,p,millis);right=animatedEndpoint(right,p,millis);
        double start=Math.max(0,Math.min(1000,splitLeft))/1000.0,end=Math.max(0,Math.min(1000,splitRight))/1000.0;
        if(end<=start)return center;
        if(p<=start)return mix(left,center,start<=0?1:p/start);
        if(p>=end)return mix(center,right,end>=1?1:(p-end)/(1-end));
        return center;
    }
    private static int mix(int from,int to,double t){
        double k=Math.max(0,Math.min(1,t));
        int r=(int)(((from>>16)&255)*(1-k)+((to>>16)&255)*k);
        int g=(int)(((from>>8)&255)*(1-k)+((to>>8)&255)*k);
        int b=(int)((from&255)*(1-k)+(to&255)*k);
        return r<<16|g<<8|b;
    }
    private static int animatedEndpoint(int color,double p,long millis){
        String code=switch(color){case 0x000000->"D71";case 0xC00020->"D76";case 0x800020->"D72";default->null;};
        if(code==null)return color;
        int[] pair=com.innerdimension.domain.DomainTextPalette.pair(code);
        return com.innerdimension.domain.DomainTextPalette.color(pair[0],pair[1],p,millis);
    }
}
