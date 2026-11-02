package com.innerdimension.client;

import com.innerdimension.InnerDimension;
import com.innerdimension.client.crest.DomainCrestClient;
import com.innerdimension.projection.ProjectionAnimation;
import com.innerdimension.projection.CrestAnimation;
import com.innerdimension.projection.ProjectionIndex;
import com.innerdimension.projection.RoomProjectionEntity;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_10055;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_12249;
import net.minecraft.class_2960;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_583;
import net.minecraft.class_765;
import net.minecraft.class_11659;

/** World-space crest driven by the same tracked timeline as projection collision. */
public final class DomainCrestFeature extends class_3887<class_10055,class_583<class_10055>> {
    private record Cached(int revision,class_2960 texture){}
    private static final Map<UUID,Cached> TEXTURES=new HashMap<>();
    private static final Map<UUID,SurfaceSubmission> SURFACES=new HashMap<>();
    private static final class SurfaceSubmission { boolean belly; }
    public static void bellySubmitted(UUID owner) {
        SurfaceSubmission submission=SURFACES.get(owner);
        if(submission!=null)submission.belly=true;
    }

    /** TextureManager owns the GPU resource; removing a map entry alone cannot release it. */
    public static void invalidate(UUID owner) {
        Cached cached = TEXTURES.remove(owner);
        SURFACES.remove(owner);
        if (cached != null) {
            net.minecraft.class_310.method_1551().method_1531().method_4615(cached.texture());
        }
    }

    public static void clearTextures() {
        for (UUID owner : java.util.List.copyOf(TEXTURES.keySet())) invalidate(owner);
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public DomainCrestFeature(class_3883 context){super(context);}

    @Override public void method_4199(class_4587 matrices, class_11659 queue, int light,
                                      class_10055 state, float limbAngle, float limbDistance) {
        UUID owner=((CrestPlayerRenderState)state).inner$crestPlayer();
        var client=net.minecraft.class_310.method_1551();
        if(owner==null||client.field_1687==null)return;
        RoomProjectionEntity projection=ProjectionIndex.active(client.field_1687,owner);
        if(projection==null||projection.phase()==ProjectionAnimation.RETRACTING)return;
        CrestAnimation.Frame frame=animation(projection,client.method_61966().method_60637(true));
        float size=frame.size();
        float opacity=frame.opacity();
        class_2960 texture=texture(owner);
        if(texture==null)return;
        // Downward-positive model Y; keep the raised abdominal anchor.
        float halfH=size*.5f,halfW=size*.75f,z=state.field_53544?-.144f:-.128f,y=.5625f;
        SurfaceSubmission submission=new SurfaceSubmission();
        SURFACES.put(owner,submission);
        int fullBright=class_765.method_23687(15,15);
        matrices.method_22903();
        try {
            // Features already receive the renderer's entity transform, but not model-part
            // transforms. Attach to the posed torso, including crouch translation and pitch.
            var model=method_17165();
            if(model instanceof net.minecraft.class_572<?> biped){
                model.method_63512().method_22703(matrices);
                biped.field_3391.method_22703(matrices);
            }
            queue.method_73483(matrices,class_12249.method_75984(texture,false),(entry,out)->{
            // NoN submits its belly after player features. The queued fallback is suppressed
            // only when the actual posed belly layer was submitted for this render.
            if(submission.belly)return;
            float left=-halfW,right=halfW;
            float top=y-halfH,bottom=y+halfH;
            // NativeImage upload and the queued world quad use opposite vertical origins.
            vertex(entry,out,left,top,z,(left+halfW)/(2*halfW),(top-y+halfH)/(2*halfH),fullBright,opacity);
            vertex(entry,out,right,top,z,(right+halfW)/(2*halfW),(top-y+halfH)/(2*halfH),fullBright,opacity);
            vertex(entry,out,right,bottom,z,(right+halfW)/(2*halfW),(bottom-y+halfH)/(2*halfH),fullBright,opacity);
            vertex(entry,out,left,bottom,z,(left+halfW)/(2*halfW),(bottom-y+halfH)/(2*halfH),fullBright,opacity);
            });
        } finally {
            matrices.method_22909();
        }
    }
    private static void vertex(class_4587.class_4665 pose,class_4588 out,float x,float y,float z,float u,float v,int light,float opacity){
        out.method_22918(pose.method_23761(),x,y,z).method_22915(1,1,1,opacity).method_22913(u,v)
                .method_22921(net.minecraft.class_4608.field_21444 & 65535,net.minecraft.class_4608.field_21444 >>> 16)
                .method_22922(light).method_60831(pose,0,0,-1);
    }
    public static CrestAnimation.Frame animation(RoomProjectionEntity projection,float partial){
        var frame=CrestAnimation.sample(projection.phase(),projection.elapsed(partial),
                projection.shellTicks(),projection.interiorTicks(),
                projection.crestDistance(0),projection.crestDistance(1),projection.crestDistance(2),
                projection.crestScale(0),projection.crestScale(1),projection.crestScale(2));
        float strength=DomainCrestClient.strength(projection.owner(),partial);
        return CrestAnimation.fade(frame,projection.crestScale(0),strength);
    }
    public static class_2960 texture(UUID owner){
        int revision=DomainCrestClient.revision(owner);
        Cached cached=TEXTURES.get(owner);if(cached!=null&&cached.revision()==revision)return cached.texture();
        invalidate(owner);
        BufferedImage image=DomainCrestClient.designFor(owner);if(image==null)return null;
        class_1011 nativeImage=new class_1011(image.getWidth(),image.getHeight(),true);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)nativeImage.method_61941(x,y,image.getRGB(x,y));
        class_2960 id=InnerDimension.id("dynamic/crest_"+owner.toString().replace("-","")+"_"+revision);
        net.minecraft.class_310.method_1551().method_1531().method_4616(id,new class_1043(()->"Inner Dimension crest",nativeImage));
        TEXTURES.put(owner,new Cached(revision,id));return id;
    }
}
