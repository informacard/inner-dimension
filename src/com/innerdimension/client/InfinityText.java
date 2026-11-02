package com.innerdimension.client;
import net.minecraft.*;
import com.innerdimension.domain.DomainTextPalette;
/** Shared two-tone glyph sweep for the four special abilities. */
public final class InfinityText {
    private InfinityText(){}
    public static int color(double position,long millis){
        int[] pair=DomainTextPalette.pair("D79");
        return DomainTextPalette.color(pair[0],pair[1],position,millis);
    }
    public static void draw(class_332 g,class_327 font,String text,int x,int y,int opacity){
        draw(g,font,text,x,y,opacity,"D79");
    }
    public static void draw(class_332 g,class_327 font,String text,int x,int y,int opacity,String code){
        int[] pair=DomainTextPalette.pair(code);if(pair==null)return;
        int width=Math.max(1,font.method_1727(text)),cursor=x;
        long millis=System.nanoTime()/1_000_000L;
        for(int cp:text.codePoints().toArray()){
            String unit=new String(Character.toChars(cp));
            DomainSubtitlePainter.glyph(g,font,unit,cursor,y,(opacity<<24)|DomainTextPalette.color(pair[0],pair[1],(cursor-x)/(double)width,millis),code.equals("D71"));
            cursor+=font.method_1727(unit);
        }
    }
}
