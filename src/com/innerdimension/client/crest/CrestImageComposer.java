package com.innerdimension.client.crest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import javax.imageio.ImageIO;
public final class CrestImageComposer {
    private static final int MAX_ENTRIES=48,MAX_BYTES=64*1024*1024;
    private static final LinkedHashMap<String,byte[]> CACHE=new LinkedHashMap<>(16,.75f,true);private static int bytes;
    private CrestImageComposer(){}
    public static int designWidth(int resolution){return resolution==512?48:24;}
    public static int designHeight(int resolution){return resolution==512?32:16;}
    public static int targetX(int resolution){return resolution*21/64;}
    public static int targetY(int resolution){return resolution*28/64;}
    public static int resolutionOf(BufferedImage design){return design!=null&&design.getWidth()>24?512:256;}
    public static synchronized byte[] compose(UUID player,byte[] original,BufferedImage overlay,String layout,int layoutVersion,int revision){
        if(original==null||overlay==null)return original;try{int resolution=resolutionOf(overlay);BufferedImage design=normalizeDesign(overlay,resolution);String key=player+":"+hex(original)+":"+hex(png(design))+":"+layout+":"+layoutVersion+":"+revision;byte[] cached=CACHE.get(key);if(cached!=null)return cached;
            BufferedImage decoded=ImageIO.read(new ByteArrayInputStream(original));if(decoded==null||decoded.getWidth()!=decoded.getHeight()||decoded.getWidth()%64!=0)return original;int sourceScale=decoded.getWidth()/64;if(sourceScale<1||sourceScale>8)return original;
            BufferedImage skin=new BufferedImage(resolution,resolution,BufferedImage.TYPE_INT_ARGB);Graphics2D g=skin.createGraphics();g.setComposite(AlphaComposite.Src);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(decoded,0,0,resolution,resolution,null);g.dispose();int tx=targetX(resolution),ty=targetY(resolution);
            for(int y=0;y<design.getHeight();y++)for(int x=0;x<design.getWidth();x++){int src=design.getRGB(x,y),dst=skin.getRGB(tx+x,ty+y);if((src>>>24)!=0&&(dst>>>24)!=0)skin.setRGB(tx+x,ty+y,blend(dst,src));}
            byte[] out=png(skin);put(key,out);return out;}catch(IOException|RuntimeException e){return original;}}
    public static BufferedImage normalizeDesign(BufferedImage source,int resolution){int w=designWidth(resolution),h=designHeight(resolution);BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);if(source==null)return out;int sx=0,sy=0,sw=source.getWidth(),sh=source.getHeight();if(sw==sh&&sw>=64){sx=sw*21/64;sy=sw*28/64;sw=sw*6/64;sh=sw*2/3;}Graphics2D g=out.createGraphics();g.setComposite(AlphaComposite.Src);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(source,0,0,w,h,sx,sy,sx+sw,sy+sh,null);g.dispose();return out;}
    public static synchronized void clear(){CACHE.clear();bytes=0;}
    private static void put(String key,byte[] value){CACHE.put(key,value);bytes+=value.length;while(CACHE.size()>MAX_ENTRIES||bytes>MAX_BYTES){var it=CACHE.entrySet().iterator();var e=it.next();bytes-=e.getValue().length;it.remove();}}
    private static int blend(int d,int s){float a=(s>>>24)/255f,da=(d>>>24)/255f,oa=a+da*(1-a);if(oa<=0)return 0;int r=(int)((((s>>16)&255)*a+((d>>16)&255)*da*(1-a))/oa),g=(int)((((s>>8)&255)*a+((d>>8)&255)*da*(1-a))/oa),b=(int)(((s&255)*a+(d&255)*da*(1-a))/oa);return((int)(oa*255)<<24)|(r<<16)|(g<<8)|b;}
    private static byte[] png(BufferedImage image)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);return out.toByteArray();}
    private static String hex(byte[] data){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));}catch(Exception e){throw new IllegalStateException(e);}}
}
