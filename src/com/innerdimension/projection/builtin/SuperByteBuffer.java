package com.innerdimension.projection.builtin;

import net.minecraft.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.*;

/** Immutable mesh with cached static attributes and per-frame dynamic illumination. */
public final class SuperByteBuffer {
    private float[] data;
    private class_1920 world;
    private Matrix4f lightTransform;
    private boolean diffuse=true;
    private final class_238 bounds;
    private record Normal(float x,float y,float z) { }
    private int[] normalSlots,packedColors;
    private float[] normals,transformedNormals,lastShades;
    private class_2350[] normalDirections;
    private boolean[] shadedNormals;
    private int[] lightSlots=new int[0],lightCells=new int[0],cellLights=new int[0];
    private Matrix4f cachedLightTransform;

    SuperByteBuffer(float[] data) {
        this.data=data;
        int count=data.length/18;
        normalSlots=new int[count];packedColors=new int[count];
        Map<Normal,Integer> slots=new LinkedHashMap<>();
        double minX=Double.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Double.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
        for(int vertex=0;vertex<count;vertex++) {
            int i=vertex*18;
            minX=Math.min(minX,data[i]);minY=Math.min(minY,data[i+1]);minZ=Math.min(minZ,data[i+2]);
            maxX=Math.max(maxX,data[i]);maxY=Math.max(maxY,data[i+1]);maxZ=Math.max(maxZ,data[i+2]);
            Normal n=new Normal(data[i+11],data[i+12],data[i+13]);
            normalSlots[vertex]=slots.computeIfAbsent(n,ignored->slots.size());
        }
        bounds=count==0?null:new class_238(minX,minY,minZ,maxX,maxY,maxZ);
        normals=new float[slots.size()*3];transformedNormals=new float[normals.length];
        normalDirections=new class_2350[slots.size()];lastShades=new float[slots.size()];Arrays.fill(lastShades,Float.NaN);
        shadedNormals=new boolean[slots.size()];
        for(int vertex=0;vertex<count;vertex++)if(data[vertex*18+14]!=0)shadedNormals[normalSlots[vertex]]=true;
        slots.forEach((normal,slot)->{
            normals[slot*3]=normal.x;normals[slot*3+1]=normal.y;normals[slot*3+2]=normal.z;
            normalDirections[slot]=class_2350.method_10147(normal.x,normal.y,normal.z);
        });
    }
    public class_238 bounds(){return bounds;}
    public int vertexCount(){return data.length/18;}
    public boolean isEmpty(){return data.length==0;}
    public SuperByteBuffer disableDiffuse(){diffuse=false;return this;}
    public SuperByteBuffer useLevelLight(class_1920 world,Matrix4f transform){this.world=world;lightTransform=transform;return this;}
    public void delete(){
        data=new float[0];normalSlots=packedColors=lightSlots=lightCells=cellLights=new int[0];
        normals=transformedNormals=lastShades=new float[0];normalDirections=new class_2350[0];shadedNormals=new boolean[0];cachedLightTransform=null;
    }
    public static int maxLight(int a,int b){return class_765.method_23687(Math.max(class_765.method_24186(a),class_765.method_24186(b)),Math.max(class_765.method_24187(a),class_765.method_24187(b)));}
    public void renderInto(class_4587.class_4665 matrix,class_4588 out){renderInto(matrix,out,0);}
    public void renderInto(class_4587.class_4665 matrix,class_4588 out,float offset){renderInto(matrix,out,offset,null);}
    public void renderInto(class_4587.class_4665 matrix,class_4588 out,float offset,com.innerdimension.projection.ProjectionAnimation.BreakFrame fracture){renderInto(matrix,out,offset,fracture,null);}

    private void prepareColorsAndNormals(class_4587.class_4665 matrix) {
        boolean changed=false;Vector3f normal=new Vector3f();
        for(int slot=0;slot<normalDirections.length;slot++) {
            float shade=diffuse&&world!=null&&shadedNormals[slot]?world.method_24852(normalDirections[slot],true):1;
            if(shade!=lastShades[slot]){lastShades[slot]=shade;changed=true;}
            matrix.method_23762().transform(normals[slot*3],normals[slot*3+1],normals[slot*3+2],normal);
            transformedNormals[slot*3]=normal.x;transformedNormals[slot*3+1]=normal.y;transformedNormals[slot*3+2]=normal.z;
        }
        if(changed)for(int vertex=0;vertex<normalSlots.length;vertex++) {
            int i=vertex*18;float shade=data[i+14]!=0?lastShades[normalSlots[vertex]]:1;
            int r=(int)(data[i+3]*shade*255),g=(int)(data[i+4]*shade*255),b=(int)(data[i+5]*shade*255),a=(int)(data[i+6]*255);
            packedColors[vertex]=(a&255)<<24|(r&255)<<16|(g&255)<<8|b&255;
        }
    }
    private void prepareLightCells(FrameLightCache cache) {
        if(cachedLightTransform==null||!cachedLightTransform.equals(lightTransform)) {
            var slots=new Long2IntOpenHashMap();slots.defaultReturnValue(-1);
            lightSlots=new int[data.length/18];int[] cells=new int[data.length/18*3];int count=0;
            Vector4f point=new Vector4f();
            for(int vertex=0;vertex<lightSlots.length;vertex++) {
                int i=vertex*18;point.set(data[i],data[i+1],data[i+2],1);lightTransform.transform(point);
                int x=(int)Math.floor(point.x),y=(int)Math.floor(point.y),z=(int)Math.floor(point.z);
                long key=class_2338.method_10064(x,y,z);int slot=slots.get(key);
                if(slot<0){slot=count++;slots.put(key,slot);cells[slot*3]=x;cells[slot*3+1]=y;cells[slot*3+2]=z;}
                lightSlots[vertex]=slot;
            }
            lightCells=Arrays.copyOf(cells,count*3);cellLights=new int[count];cachedLightTransform=new Matrix4f(lightTransform);
        }
        // Dynamic light is refreshed each frame, but geometry-to-cell mapping stays fixed.
        for(int cell=0;cell<cellLights.length;cell++) {
            int x=lightCells[cell*3],y=lightCells[cell*3+1],z=lightCells[cell*3+2];
            cellLights[cell]=cache==null?class_761.method_23794(world,new class_2338(x,y,z)):cache.light(x,y,z);
        }
    }
    public void renderInto(class_4587.class_4665 matrix,class_4588 out,float offset,com.innerdimension.projection.ProjectionAnimation.BreakFrame fracture,FrameLightCache lights) {
        if(isEmpty())return;
        long lightStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        int beforeQueries=lights==null?0:lights.queryCount();
        prepareColorsAndNormals(matrix);if(world!=null)prepareLightCells(lights);
        com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.LIGHT_PREPARE,lightStart);
        com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.LIGHT_QUERIES,lights==null?cellLights.length:lights.queryCount()-beforeQueries);
        com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.VERTICES,normalSlots.length);
        com.innerdimension.projection.OpenDomainPerformance.consumer(out.getClass());
        long vertexStart=com.innerdimension.projection.OpenDomainPerformance.begin();
        Vector3f point=new Vector3f();
        for(int vertex=0;vertex<normalSlots.length;vertex++) {
            int i=vertex*18;float x=data[i],y=data[i+1],z=data[i+2];
            if(fracture!=null) {
                float scale=(float)com.innerdimension.projection.ProjectionAnimation.breakScale(fracture,(int)Math.floor(data[i+15]),(int)Math.floor(data[i+16]),(int)Math.floor(data[i+17]));
                if(scale<=.001f)continue;
                x=data[i+15]+(x-data[i+15])*scale;y=data[i+16]+(y-data[i+16])*scale;z=data[i+17]+(z-data[i+17])*scale;
            }
            int light=(int)data[i+10];if(world!=null)light=maxLight(light,cellLights[lightSlots[vertex]]);
            int n=normalSlots[vertex]*3;
            matrix.method_23761().transformPosition(x+data[i+11]*offset,y+data[i+12]*offset,z+data[i+13]*offset,point);
            // Vanilla's full-vertex entry uses BufferBuilder's direct packed fast path.
            // Wrapper consumers retain their normal fallback, including shader compatibility.
            out.method_23919(point.x,point.y,point.z,packedColors[vertex],data[i+7],data[i+8],(int)data[i+9],light,transformedNormals[n],transformedNormals[n+1],transformedNormals[n+2]);
        }
        com.innerdimension.projection.OpenDomainPerformance.end(com.innerdimension.projection.OpenDomainPerformance.Metric.VERTEX_WRITE,vertexStart);
    }
}
