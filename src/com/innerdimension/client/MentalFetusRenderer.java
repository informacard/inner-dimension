package com.innerdimension.client;

import com.innerdimension.InnerDimension;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.entity.FetusEntity;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.*;
import org.joml.Vector3f;

/** Four authored meshes share one origin. Water/mist sample the live animated block atlas. */
public final class MentalFetusRenderer extends class_897<FetusEntity,class_10017> {
    private static final String[] PARTS={"soul","flesh","shell","core"};
    private static volatile Map<String,List<Face>> meshes=Map.of();
    private record Vertex(float x,float y,float z,float u,float v){}
    private record Face(Vector3f normal,List<Vertex> vertices,boolean inverted){}
    private final boolean voidVariant;
    public MentalFetusRenderer(class_5617.class_5618 context){this(context,false);}
    public MentalFetusRenderer(class_5617.class_5618 context,boolean voidVariant){super(context);this.voidVariant=voidVariant;}
    @Override public class_10017 method_55269(){return new class_10017();}
    @Override protected class_238 method_62358(FetusEntity e){
        return new class_238(e.method_23317()-1.3,e.method_23318()-8,e.method_23321()-1.3,e.method_23317()+1.3,e.method_23318()+8,e.method_23321()+1.3);
    }
    public static final SimpleSynchronousResourceReloadListener RELOAD=new SimpleSynchronousResourceReloadListener(){
        @Override public class_2960 getFabricId(){return InnerDimension.id("mental_fetus_meshes");}
        @Override public void method_14491(class_3300 manager){
            Map<String,List<Face>> next=new HashMap<>();
            for(String part:new String[]{"soul","flesh","shell","core","void_shell","void_core"}){
                try(var reader=new InputStreamReader(manager.open(InnerDimension.id("models/entity/"+(part.startsWith("void_")?"void_fetus_"+part.substring(5):"mental_fetus_"+part)+".mesh.json")),StandardCharsets.UTF_8)){
                    JsonObject data=JsonParser.parseReader(reader).getAsJsonObject();List<Face> faces=new ArrayList<>();
                    for(JsonElement entry:data.getAsJsonArray("faces")){
                        JsonObject face=entry.getAsJsonObject();JsonArray normal=face.getAsJsonArray("normal");List<Vertex> vertices=new ArrayList<>();
                        for(JsonElement v:face.getAsJsonArray("vertices")){JsonArray a=v.getAsJsonArray();vertices.add(new Vertex(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat(),a.get(3).getAsFloat(),a.get(4).getAsFloat()));}
                        if(vertices.size()!=4)throw new IllegalArgumentException("Fetus mesh face must be a quad");
                        faces.add(new Face(new Vector3f(normal.get(0).getAsFloat(),normal.get(1).getAsFloat(),normal.get(2).getAsFloat()),List.copyOf(vertices),face.has("inverted")&&face.get("inverted").getAsBoolean()));
                    }
                    next.put(part,List.copyOf(faces));
                }catch(Exception ex){throw new IllegalStateException("Cannot load mental fetus part "+part,ex);}
            }
            meshes=Map.copyOf(next);
            org.slf4j.LoggerFactory.getLogger("inner_dimension").info("Mental fetus meshes loaded: soul={} flesh={} shell={} core={}",next.get("soul").size(),next.get("flesh").size(),next.get("shell").size(),next.get("core").size());
        }
    };
    private static class_1058 sprite(class_2248 block){
        var model=class_310.method_1551().method_1541().method_3349(block.method_9564());
        List<class_10889> parts=new ArrayList<>();model.method_68513(class_5819.method_43053(),parts);
        return parts.get(0).comp_3752();
    }
    @Override public void method_3936(class_10017 state,class_4587 matrices,class_11659 queue,class_12075 outlines){
        class_1058 mist=sprite(voidVariant?ModBlocks.VOID_MIST:ModBlocks.SOUL_MIST),water=sprite(voidVariant?ModBlocks.VOID_WATER:ModBlocks.FLESH_WATER);
        ProjectionAnimatedSprites.mark(new HashSet<>(List.of(mist,water)));
        for(String part:PARTS){
            List<Face> faces=meshes.getOrDefault(voidVariant&&(part.equals("shell")||part.equals("core"))?"void_"+part:part,List.of());if(faces.isEmpty())continue;
            class_1058 atlasSprite=part.equals("soul")?mist:part.equals("flesh")?water:null;
            class_2960 texture=atlasSprite==null?InnerDimension.id("textures/entity/"+(voidVariant?"void_fetus_":"mental_fetus_")+part+".png"):atlasSprite.method_45852();
            boolean emissive=part.equals("core");
            class_1921 layer=emissive?class_12249.method_75994(texture):class_12249.method_75980(texture,false);
            int light=emissive?class_765.method_23687(15,15):state.field_61820;
            int color=0xFFFFFFFF;
            if(voidVariant){
                if(part.equals("core"))color=0xFF000000|VoidMaterialColors.CORE;
                else if(atlasSprite!=null)color=0xFF000000|(part.equals("soul")?VoidMaterialColors.MIST:VoidMaterialColors.WATER);
            }
            submit(matrices,queue,layer,faces,atlasSprite,light,color);
            // The reversed outer cube is an inward-facing outline, not a second opaque shell.
            if(emissive)submit(matrices,queue,class_12249.method_75984(texture,false),faces.stream().filter(f->!f.inverted).toList(),null,light,color);
        }
        super.method_3936(state,matrices,queue,outlines);
    }
    private static void submit(class_4587 matrices,class_11659 queue,class_1921 layer,List<Face> faces,class_1058 atlasSprite,int light,int color){
            queue.method_73483(matrices,layer,(pose,buffer)->{
                class_4588 out=atlasSprite==null?buffer:atlasSprite.method_24108(buffer);
                Vector3f position=new Vector3f(),normal=new Vector3f();
                for(Face face:faces){
                    pose.method_23762().transform(face.normal,normal).normalize();float nx=normal.x(),ny=normal.y(),nz=normal.z();
                    for(Vertex v:face.vertices){
                        pose.method_23761().transformPosition(v.x,v.y,v.z,position);
                        out.method_23919(position.x(),position.y(),position.z(),color,v.u,v.v,class_4608.field_21444,light,nx,ny,nz);
                    }
                }
            });
    }

}
