package com.innerdimension.projection.builtin;

import java.lang.reflect.Proxy;
import java.util.*;
import net.minecraft.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Compare the complete old attribute-writing path with the fast entry point. */
public final class FastVertexRegression {
    record Vertex(float x,float y,float z,int color,float u,float v,int overlay,int light,float nx,float ny,float nz) { }
    static final class Capture implements class_4588 {
        final List<Vertex> vertices=new ArrayList<>();int completeCalls;
        float x,y,z,u,v;int color,overlay,light;
        public void method_23919(float x,float y,float z,int c,float u,float v,int o,int l,float nx,float ny,float nz){completeCalls++;class_4588.super.method_23919(x,y,z,c,u,v,o,l,nx,ny,nz);}
        public class_4588 method_22912(float x,float y,float z){this.x=x;this.y=y;this.z=z;return this;}
        public class_4588 method_1336(int r,int g,int b,int a){color=(a&255)<<24|(r&255)<<16|(g&255)<<8|b&255;return this;}
        public class_4588 method_39415(int c){color=c;return this;}
        public class_4588 method_22913(float u,float v){this.u=u;this.v=v;return this;}
        public class_4588 method_60796(int u,int v){overlay=u|v<<16;return this;}
        public class_4588 method_22921(int u,int v){light=u|v<<16;return this;}
        public class_4588 method_22914(float nx,float ny,float nz){vertices.add(new Vertex(x,y,z,color,u,v,overlay,light,nx,ny,nz));return this;}
        public class_4588 method_75298(float width){return this;}
    }
    static int source(int x,int y,int z,int epoch){return class_765.method_23687((x+z+epoch)&15,(y+z+epoch)&15);}
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    public static void main(String[] args) {
        Random random=new Random(92);float[] data=new float[2000*18];
        float[][] normals={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
        for(int i=0;i<data.length;i+=18){
            data[i]=random.nextFloat()*12-6;data[i+1]=random.nextFloat()*12-6;data[i+2]=random.nextFloat()*12-6;
            for(int n=3;n<=8;n++)data[i+n]=random.nextFloat();
            data[i+9]=321|(655<<16);data[i+10]=class_765.method_23687(random.nextInt(16),random.nextInt(16));
            var normal=normals[random.nextInt(6)];System.arraycopy(normal,0,data,i+11,3);data[i+14]=random.nextBoolean()?1:0;
        }
        int[] shades={0};
        class_1920 world=(class_1920)Proxy.newProxyInstance(class_1920.class.getClassLoader(),new Class[]{class_1920.class},(object,method,values)->{
            if(method.getName().equals("method_24852")){shades[0]++;return .4f+((class_2350)values[0]).ordinal()*.1f;}
            throw new AssertionError("Unexpected world call: "+method);
        });
        var transform=new Matrix4f().translation(-123.25f,41.2f,76.6f).rotateY(.45f);
        var matrices=new class_4587().method_23760();matrices.method_23761().translation(.75f,-2,7).rotateX(.3f);matrices.method_23762().rotationX(.3f);
        var mesh=new SuperByteBuffer(data);float offset=.0005f;
        for(int epoch=0;epoch<3;epoch++) {
            final int time=epoch;Capture fast=new Capture(),legacy=new Capture();
            mesh.useLevelLight(world,transform).renderInto(matrices,fast,offset,null,new FrameLightCache((x,y,z)->source(x,y,z,time)));
            check(fast.completeCalls==2000,"uses complete vertex writer");
            check(shades[0]<=6,"shade queries depend on normals, not vertex count");shades[0]=0;
            for(int i=0;i<data.length;i+=18){
                Vector4f p=new Vector4f(data[i],data[i+1],data[i+2],1);transform.transform(p);
                int real=source((int)java.lang.Math.floor(p.x),(int)java.lang.Math.floor(p.y),(int)java.lang.Math.floor(p.z),time);
                int light=SuperByteBuffer.maxLight((int)data[i+10],real);
                float shade=data[i+14]!=0?world.method_24852(class_2350.method_10147(data[i+11],data[i+12],data[i+13]),true):1;
                Vector3f normal=new Vector3f(data[i+11],data[i+12],data[i+13]);matrices.method_23762().transform(normal);
                legacy.method_22918(matrices.method_23761(),data[i]+data[i+11]*offset,data[i+1]+data[i+12]*offset,data[i+2]+data[i+13]*offset)
                        .method_22915(data[i+3]*shade,data[i+4]*shade,data[i+5]*shade,data[i+6])
                        .method_22913(data[i+7],data[i+8]).method_22922((int)data[i+9]).method_22921(light&65535,light>>>16).method_22914(normal.x,normal.y,normal.z);
            }
            check(fast.vertices.equals(legacy.vertices),"all 2000 vertices match old positions, colors, UVs, light, overlay and normals, epoch="+epoch);
            shades[0]=0;
            if(epoch==1)transform.translate(1,2,3);
        }
        System.out.println("FastVertexRegression passed: 6000 complete vertices match legacy output across light/anchor changes");
    }
}
