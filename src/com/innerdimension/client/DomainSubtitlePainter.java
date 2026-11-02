package com.innerdimension.client;
import net.minecraft.class_332;
import net.minecraft.class_327;
/** Outlining is an explicit Void identity, never inferred from animated brightness. */
public final class DomainSubtitlePainter {
    private DomainSubtitlePainter(){}
    public static boolean dark(int argb){return (((argb>>16)&255)*299+((argb>>8)&255)*587+(argb&255)*114)<64000;}
    public static void glyph(class_332 g,class_327 font,String unit,int x,int y,int argb){
        glyph(g,font,unit,x,y,argb,false);
    }
    public static boolean voidPalette(int left,int center,int right){return left==0&&right==0&&(center==0||center==0x9174B5);}
    public static void glyph(class_332 g,class_327 font,String unit,int x,int y,int argb,boolean voidOutline){
        if(voidOutline) {
            int outline=(argb&0xff000000)|0x808080;
            for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)if(dx!=0||dy!=0)
                g.method_51433(font,unit,x+dx,y+dy,outline,false);
        }
        g.method_51433(font,unit,x,y,argb,true);
    }
}
