package com.innerdimension.domain;

/** Two endpoints per special ability; sent through the existing three-colour HUD fields. */
public final class DomainTextPalette {
    private static final int[] VOID={0x000000,0x9174B5},SHRINE={0xC00020,0xFFE1BD},SUCCUBUS={0x800020,0xFF78B7},INFINITY={0x7657FF,0xA6F3FF};
    private static final int[][] BOSSES={{0x6430AF,0xE5B7FF},{0x536044,0xD6D0A1},{0x096A76,0x83F1E4}};
    public static int[] boss(int kind){return BOSSES[Math.max(0,Math.min(2,kind))].clone();}
    private DomainTextPalette(){}
    public static int[] pair(String code){return switch(code){
        case "D71"->VOID;
        case "D76"->SHRINE;
        case "D72"->SUCCUBUS;
        case "D79"->INFINITY;
        default->null;
    };}
    public static boolean matches(int left,int center,int right){
        if(left!=right)return false;
        for(var pair:BOSSES)if(left==pair[0]&&center==pair[1])return true;
        return left==VOID[0]&&center==VOID[1]||left==SHRINE[0]&&center==SHRINE[1]||left==SUCCUBUS[0]&&center==SUCCUBUS[1]||left==INFINITY[0]&&center==INFINITY[1];
    }
    public static int color(int from,int to,double position,long millis){
        // A broad, slow wave, with no random flicker or hue rotation.
        double wave=.5+.5*Math.sin(position*5-millis/950.0);
        int r=(int)(((from>>16)&255)*(1-wave)+((to>>16)&255)*wave);
        int g=(int)(((from>>8)&255)*(1-wave)+((to>>8)&255)*wave);
        int b=(int)((from&255)*(1-wave)+(to&255)*wave);
        return r<<16|g<<8|b;
    }
}
