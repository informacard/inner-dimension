package com.innerdimension.client.crest;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;

/** Global client library. Existing artwork is always read before any defaults are created. */
public final class CrestDesignStore {
    public static final int SLOT_COUNT=7;
    private static final BufferedImage[] SLOTS=new BufferedImage[SLOT_COUNT];
    private static final int[] RESOLUTIONS={256,512,256,256,256,256,256};
    private static final boolean[] NAME_KNOWN=new boolean[SLOT_COUNT];
    private static final String[] NAMES=new String[SLOT_COUNT];
    private static final Path DIR=FabricLoader.getInstance().getConfigDir().resolve("inner_dimension");
    private static final Path FILE=DIR.resolve("crests.properties");
    private static int selected;private static boolean loaded;
    private CrestDesignStore(){}
    public static synchronized void load(BufferedImage defaultAtlas){
        if(loaded)return;
        Properties props=new Properties();Path legacy=DIR.getParent().resolve("inner_dimension_crest.properties");
        try{
            Path source=Files.exists(FILE)?FILE:legacy;
            if(Files.exists(source))try(InputStream in=Files.newInputStream(source)){props.load(in);}
            selected=clamp(Integer.parseInt(props.getProperty("selected","0")));
            for(int i=0;i<SLOT_COUNT;i++){
                RESOLUTIONS[i]="512".equals(props.getProperty("slot."+i+".resolution",Integer.toString(RESOLUTIONS[i])))?512:256;
                NAME_KNOWN[i]=Boolean.parseBoolean(props.getProperty("slot."+i+".name.known","false"));NAMES[i]=props.getProperty("slot."+i+".name","");Path png=DIR.resolve("crest-"+(i+1)+".png");
                BufferedImage image=null;
                if(Files.exists(png)){image=ImageIO.read(png.toFile());if(image==null)throw new IOException("Unreadable crest: "+png);}
                else if(props.containsKey("slot."+i)){image=ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(props.getProperty("slot."+i))));if(image==null)throw new IOException("Unreadable legacy crest slot "+i);}
                if(i==6){if(NAMES[i].isEmpty()){NAMES[i]="虚空";NAME_KNOWN[i]=true;}if(image==null)try(InputStream in=CrestDesignStore.class.getResourceAsStream("/assets/inner_dimension/textures/crest/default_void_crest.png")){if(in==null)throw new IOException("Missing default void crest");image=ImageIO.read(in);}}
                SLOTS[i]=image!=null?CrestImageComposer.normalizeDesign(image,RESOLUTIONS[i]):(i<2||i==6)?CrestImageComposer.normalizeDesign(defaultAtlas,RESOLUTIONS[i]):blank(RESOLUTIONS[i]);
            }
            loaded=true;persist();if(source.equals(legacy)&&Files.exists(legacy)){Path backup=DIR.resolve("legacy-crests.properties.bak");if(!Files.exists(backup))Files.copy(legacy,backup);Files.delete(legacy);}
        }catch(Exception e){throw new IllegalStateException("Crest library could not be loaded; refusing to overwrite player artwork",e);}
    }
    public static synchronized BufferedImage current(BufferedImage fallback){load(fallback);return copy(SLOTS[selected]);}
    public static synchronized BufferedImage slot(int i){return copy(SLOTS[clamp(i)]);}
    public static synchronized int resolution(int i){return RESOLUTIONS[clamp(i)];}
    public static synchronized int selected(){return selected;}
    public static synchronized String name(int i){return NAMES[clamp(i)]==null?"":NAMES[clamp(i)];}
    public static synchronized void importNames(List<String> names){for(int i=0;i<Math.min(SLOT_COUNT,names.size());i++)if(!NAME_KNOWN[i]&&!names.get(i).isEmpty()){NAMES[i]=names.get(i);NAME_KNOWN[i]=true;}if(loaded)persist();}
    public static synchronized void rename(int i,String name){NAMES[clamp(i)]=name;NAME_KNOWN[clamp(i)]=true;persistMetadata();}
    public static synchronized void select(int i){selected=clamp(i);if(loaded)persistMetadata();}
    public static synchronized void save(int i,BufferedImage image,int resolution){if(!loaded)throw new IllegalStateException("Crest library not loaded");i=clamp(i);RESOLUTIONS[i]=resolution==512?512:256;SLOTS[i]=CrestImageComposer.normalizeDesign(image,RESOLUTIONS[i]);selected=i;persist();}
    public static synchronized void setCurrent(BufferedImage image,int resolution){save(selected,image,resolution);}
    public static BufferedImage resize(BufferedImage image,int resolution){return CrestImageComposer.normalizeDesign(image,resolution);}
    private static void replace(Path tmp,Path destination)throws IOException{try{Files.move(tmp,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,destination,StandardCopyOption.REPLACE_EXISTING);}}
    private static void persist(){if(!loaded)return;try{Files.createDirectories(DIR);for(int i=0;i<SLOT_COUNT;i++){Path tmp=Files.createTempFile(DIR,"crest-",".png");ImageIO.write(SLOTS[i],"png",tmp.toFile());replace(tmp,DIR.resolve("crest-"+(i+1)+".png"));}persistMetadata();}catch(IOException e){throw new IllegalStateException("Cannot save crest library",e);}}
    private static void persistMetadata(){try{Files.createDirectories(DIR);Properties props=new Properties();props.setProperty("storage.version","3");props.setProperty("selected",Integer.toString(selected));for(int i=0;i<SLOT_COUNT;i++){props.setProperty("slot."+i+".resolution",Integer.toString(RESOLUTIONS[i]));props.setProperty("slot."+i+".name",name(i));props.setProperty("slot."+i+".name.known",Boolean.toString(NAME_KNOWN[i]));}Path tmp=Files.createTempFile(DIR,"crests-",".properties");try(OutputStream out=Files.newOutputStream(tmp)){props.store(out,"Inner Dimension global crest library");}replace(tmp,FILE);}catch(IOException e){throw new IllegalStateException("Cannot save crest metadata",e);}}
    private static BufferedImage blank(int resolution){return new BufferedImage(CrestImageComposer.designWidth(resolution),CrestImageComposer.designHeight(resolution),BufferedImage.TYPE_INT_ARGB);}
    private static BufferedImage copy(BufferedImage image){BufferedImage out=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();g.drawImage(image,0,0,null);g.dispose();return out;}
    private static int clamp(int i){return Math.max(0,Math.min(SLOT_COUNT-1,i));}
}
