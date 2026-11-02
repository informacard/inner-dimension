package com.innerdimension.client;
/** Map the dominant background to pure black; only brighter pattern pixels become gray. */
public final class VoidTexturePalette {
    private VoidTexturePalette() {}
    private static int luminance(int color){return (54*((color>>>16)&255)+183*((color>>>8)&255)+19*(color&255))>>>8;}
    public static void apply(int[] pixels){
        int[] histogram=new int[256];int count=0,max=0;
        for(int pixel:pixels)if((pixel>>>24)!=0){int level=luminance(pixel);histogram[level]++;count++;max=Math.max(max,level);}
        if(count==0)return;
        int threshold=0,total=0;
        for(;threshold<255;threshold++){total+=histogram[threshold];if(total*10L>=count*7L)break;}
        for(int i=0;i<pixels.length;i++){
            int level=luminance(pixels[i]);
            int gray=level<=threshold?0:24+(level-threshold)*104/Math.max(1,max-threshold);
            pixels[i]=(pixels[i]&0xFF000000)|(gray<<16)|(gray<<8)|gray;
        }
    }
}
