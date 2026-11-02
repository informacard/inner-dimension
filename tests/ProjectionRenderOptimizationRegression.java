package com.innerdimension.projection.builtin;

import com.innerdimension.projection.*;
import net.minecraft.*;
import org.joml.Matrix4f;
import java.lang.reflect.Proxy;
import java.util.*;

public final class ProjectionRenderOptimizationRegression {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static final class Capture implements class_4588 {
        final List<Integer> lights=new ArrayList<>();int vertices;
        public class_4588 method_22912(float x,float y,float z){vertices++;return this;}
        public class_4588 method_1336(int r,int g,int b,int a){return this;}
        public class_4588 method_39415(int c){return this;}
        public class_4588 method_22913(float u,float v){return this;}
        public class_4588 method_60796(int u,int v){return this;}
        public class_4588 method_22921(int u,int v){lights.add(u|v<<16);return this;}
        public class_4588 method_22914(float x,float y,float z){return this;}
        public class_4588 method_75298(float width){return this;}
    }
    public static void main(String[] args)throws Exception {
        int[] queries={0};
        var cache=new FrameLightCache((x,y,z)->{queries[0]++;return 0;});
        for(int i=0;i<1000;i++)check(cache.light(-1,-64,0)==0,"darkness is a valid cached light");
        check(queries[0]==1,"one query per cell despite repeated vertices");
        int[][] cells={{-1,-64,1},{0,-64,0},{-1,-63,0},{30000000,319,-30000000},{-30000000,-64,30000000}};
        for(var cell:cells)cache.light(cell[0],cell[1],cell[2]);
        check(queries[0]==6,"signed coordinates do not alias");
        int[] changingLight={4};
        var first=new FrameLightCache((x,y,z)->changingLight[0]);
        check(first.light(0,0,0)==4,"initial frame light");changingLight[0]=9;
        check(first.light(0,0,0)==4,"shared layers use same frame result");
        check(new FrameLightCache((x,y,z)->changingLight[0]).light(0,0,0)==9,"new frame sees changed light");
        var otherWorld=new FrameLightCache((x,y,z)->15);
        check(otherWorld.light(0,0,0)==15,"render contexts do not mix worlds");

        float[] data=new float[4*18];
        int baked=class_765.method_23687(12,0),real=class_765.method_23687(0,15);
        for(int i=0;i<4;i++){int v=i*18;data[v]=.1f+(i%2)*.1f;data[v+1]=.1f+(i/2)*.1f;data[v+2]=.1f;data[v+3]=data[v+4]=data[v+5]=data[v+6]=1;data[v+10]=baked;data[v+12]=1;}
        var buffer=new SuperByteBuffer(data).disableDiffuse();
        var world=(class_1920)Proxy.newProxyInstance(class_1920.class.getClassLoader(),new Class[]{class_1920.class},(object,method,values)->{throw new AssertionError("Unexpected world query: "+method);});
        buffer.useLevelLight(world,new Matrix4f().translation(10.25f,0,0));
        int[] vertexQueries={0};var lights=new FrameLightCache((x,y,z)->{check(x==10&&y==0&&z==0,"light uses transformed world cell");vertexQueries[0]++;return real;});
        Capture a=new Capture(),b=new Capture();var matrices=new class_4587().method_23760();
        buffer.renderInto(matrices,a,.0005f,null,lights);buffer.renderInto(matrices,b,.0005f,null,lights);
        check(a.vertices==4&&b.vertices==4,"all vertices still submitted");
        check(a.lights.equals(b.lights)&&a.lights.stream().allMatch(value->value==SuperByteBuffer.maxLight(baked,real)),"baked and real lighting preserved across layers");
        check(vertexQueries[0]==1,"multiple layer submissions share light lookup");
        check(new SuperByteBuffer(new float[0]).bounds()==null,"empty geometry has no visible bounds");
        var frustum=new class_4604(new Matrix4f(),new Matrix4f());
        check(frustum.method_23093(buffer.bounds()),"visible mesh kept");
        check(!frustum.method_23093(buffer.bounds().method_997(new class_243(20,0,0))),"off-screen region culled");
        float[] overhang=data.clone();for(int i=0;i<4;i++)overhang[i*18]=i<2?.5f:3;
        check(frustum.method_23093(new SuperByteBuffer(overhang).bounds()),"extended model retained when its mesh enters view");
        frustum.method_23088(50,0,0);
        check(frustum.method_23093(buffer.bounds().method_997(new class_243(50,0,0))),"culling uses absolute world bounds");
        var sectionType=Class.forName("com.innerdimension.client.RoomProjectionRenderer$RingClient");
        var key=sectionType.getDeclaredMethod("sectionKey",class_2338.class);key.setAccessible(true);
        check(key.invoke(null,new class_2338(-1,-17,16)).equals(new class_2338(-1,-2,1)),"negative section boundaries use floor division");
        check(!OpenDomainRules.movementReady(ProjectionAnimation.INTERIOR,19.999),"movement remains held before one second");
        check(OpenDomainRules.movementReady(ProjectionAnimation.INTERIOR,20),"movement unlock at twenty ticks");
        check(!OpenDomainRules.movementReady(ProjectionAnimation.EXPANDING,40)&&!OpenDomainRules.movementReady(ProjectionAnimation.RETRACTING,40),"closed/retracting phases cannot early unlock");
        check(OpenDomainRules.EXPANSION_TICKS==80,"visual expansion still lasts four seconds");
        System.out.println("ProjectionRenderOptimizationRegression passed: "+checks+" checks");
    }
}
