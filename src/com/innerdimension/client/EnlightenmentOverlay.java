package com.innerdimension.client;

import com.google.gson.*;
import com.innerdimension.config.InnerConfig;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.*;
import com.innerdimension.network.EnlightenmentVisualPayload;

/** Client-only full-screen ritual animation. Sheet sizes stay below 4096 pixels. */
public final class EnlightenmentOverlay {
    private record Clip(int width,int height,int columns,int capacity,int padding,int[] durations) {
        long duration(){return AnimationTimeline.duration(durations);}
        int frame(long millis){return AnimationTimeline.frame(millis,durations);}
    }
    private static final Map<String,Clip> CLIPS=new HashMap<>();
    private static final Set<class_2960> LOADED=new HashSet<>();
    private static long previewStarted,specialDuration;
    private static boolean baptismPreview;
    private static String previewVariant,ritualVariant;
    private static long ritualClosingStarted,ritualStarted;
    private EnlightenmentOverlay(){}
    private static class_2960 id(String path){return class_2960.method_60655("inner_dimension",path);}
    private static String selected(){return "dance";}
    public static void register(){
        RitualInput.register();
        BaptismSilence.register();
        ResourceManagerHelper.get(class_3264.field_14188).registerReloadListener(new SimpleSynchronousResourceReloadListener(){
            public class_2960 getFabricId(){return id("enlightenment_overlays");}
            public void method_14491(class_3300 manager){
                Map<String,Clip> next=new HashMap<>();
                for(String variant:List.of("dance"))try(var reader=new InputStreamReader(manager.open(id("textures/gui/enlightenment/"+variant+".json")),StandardCharsets.UTF_8)){
                    var data=JsonParser.parseReader(reader).getAsJsonObject();var times=data.getAsJsonArray("durations");int[] durations=new int[times.size()];
                    for(int i=0;i<durations.length;i++)durations[i]=Math.max(1,times.get(i).getAsInt());
                    if(durations.length==0)throw new IOException("Empty animation");
                    next.put(variant,new Clip(data.get("width").getAsInt(),data.get("height").getAsInt(),data.get("columns").getAsInt(),data.get("capacity").getAsInt(),data.get("padding").getAsInt(),durations));
                }catch(Exception e){org.slf4j.LoggerFactory.getLogger("inner_dimension").warn("Cannot load ritual visual {}",variant,e);}
                class_310.method_1551().execute(()->{release();CLIPS.clear();CLIPS.putAll(next);});
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->client.execute(()->{previewStarted=0;baptismPreview=false;release();}));
        ClientPlayNetworking.registerGlobalReceiver(EnlightenmentVisualPayload.ID,(payload,context)->context.client().execute(()->{
            if(baptismPreview&&payload.variant()!=5)return;
            if(payload.preview()&&(payload.variant()==4||payload.variant()==5)){prepareRitual();previewVariant=selected();previewStarted=System.nanoTime();specialDuration=payload.variant()==4?3000:playDuration(selected());baptismPreview=payload.variant()==5;if(baptismPreview)BaptismSilence.begin(specialDuration+420);return;}
            if(payload.variant()!=3)return;
            if(payload.preview())preview();
        }));
        ClientPlayConnectionEvents.JOIN.register((handler,sender,client)->syncSelection());
    }
    public static void syncSelection(){if(ClientPlayNetworking.canSend(EnlightenmentVisualPayload.ID))ClientPlayNetworking.send(new EnlightenmentVisualPayload(3,false));}
    private static void preview(){specialDuration=0;prepareRitual();previewVariant=selected();previewStarted=System.nanoTime();InnerSoundClient.personal("enlightenment",.8f);}
    private static long playDuration(String variant){return com.innerdimension.voiddomain.RitualVisualSchedule.playMillis(variant.equals("eye")?1:variant.equals("alternative")?2:3);}
    private static void release(){var manager=class_310.method_1551().method_1531();for(var texture:LOADED)manager.method_4615(texture);LOADED.clear();}
    public static void prepareRitual(){baptismPreview=false;BaptismSilence.end();previewStarted=0;ritualClosingStarted=0;ritualStarted=0;ritualVariant=selected();release();var clip=CLIPS.get(selected());if(clip!=null)for(int i=0;i<(clip.durations.length+clip.capacity-1)/clip.capacity;i++)texture(selected(),i);}
    private static class_2960 texture(String variant,int sheet){var texture=id("textures/gui/enlightenment/"+variant+"_"+sheet+".png");class_310.method_1551().method_1531().method_4619(texture);LOADED.add(texture);return texture;}
    public static void renderPreview(class_332 graphics){
        if(previewStarted==0)return;
        var clip=CLIPS.get(previewVariant);if(clip==null){previewStarted=0;BaptismSilence.end();return;}
        long elapsed=(System.nanoTime()-previewStarted)/1_000_000,duration=specialDuration>0?specialDuration:playDuration(previewVariant);
        if(specialDuration==3000){if(elapsed>=3000){previewStarted=0;return;}draw(graphics,previewVariant,elapsed*playDuration(previewVariant)/3000,1);return;}
        if(elapsed>=duration+420){previewStarted=0;BaptismSilence.end();if(baptismPreview){baptismPreview=false;if(ClientPlayNetworking.canSend(com.innerdimension.network.BaptismFinishedPayload.ID))ClientPlayNetworking.send(com.innerdimension.network.BaptismFinishedPayload.INSTANCE);}return;}
        if(elapsed<=duration+180){
            draw(graphics,previewVariant,Math.min(elapsed,duration-1)*playDuration(previewVariant)/duration,1);
            EntryTransition.drawEyelids(graphics,RitualVisualTiming.aperture(elapsed,duration));
        }else EntryTransition.drawEyelids(graphics,AnimationEnvelope.smooth((elapsed-duration-180)/240.0));
    }
    public static void renderRitual(class_332 graphics,long started){
        if(ritualVariant==null)return;
        ritualStarted=started;
        long elapsed=Math.max(0,(System.nanoTime()-started)/1_000_000);
        draw(graphics,ritualVariant,Math.min(elapsed,playDuration(ritualVariant)-1),1);
    }
    public static void finishRitual(){if(ritualStarted!=0)ritualClosingStarted=System.nanoTime();}
    public static void renderClosing(class_332 graphics){
        if(ritualClosingStarted==0||System.nanoTime()-ritualClosingStarted>=180_000_000L)return;
        draw(graphics,ritualVariant,playDuration(ritualVariant)-1,1);
    }
    public static void renderInformation(class_332 g,long elapsed){draw(g,selected(),elapsed,1);}
    private static void draw(class_332 graphics,String variant,long millis,float opacity){
        var clip=CLIPS.get(variant);if(clip==null)return;
        int frame=variant.equals("eye")&&millis>=clip.duration()?clip.durations.length-1:clip.frame(millis),sheet=frame/clip.capacity,local=frame%clip.capacity;
        int count=Math.min(clip.capacity,clip.durations.length-sheet*clip.capacity),rows=(count+clip.columns-1)/clip.columns;
        int cw=clip.width+clip.padding*2,ch=clip.height+clip.padding*2;
        int width=graphics.method_51421(),height=graphics.method_51443();
        double scale=Math.max(width/(double)clip.width,height/(double)clip.height);
        int drawWidth=(int)Math.ceil(clip.width*scale),drawHeight=(int)Math.ceil(clip.height*scale);
        graphics.method_71048();graphics.method_51448().pushMatrix();graphics.method_51448().identity();
        graphics.method_25294(0,0,width,height,0xff000000);
        if(opacity>0)graphics.method_25293(class_10799.field_56883,texture(variant,sheet),(width-drawWidth)/2,(height-drawHeight)/2,
            local%clip.columns*cw+clip.padding,local/clip.columns*ch+clip.padding,
            drawWidth,drawHeight,clip.width,clip.height,clip.columns*cw,rows*ch,((int)(255*opacity)<<24)|0xffffff);
        graphics.method_51448().popMatrix();
    }
}
