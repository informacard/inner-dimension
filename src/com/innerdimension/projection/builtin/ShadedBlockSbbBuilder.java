package com.innerdimension.projection.builtin;
import net.minecraft.*;
import java.util.Arrays;
/** Records all vanilla vertex attributes, including per-quad diffuse shading. */
public final class ShadedBlockSbbBuilder implements class_4588 {
 private float centerX,centerY,centerZ;
 public void blockCenter(class_2338 pos){centerX=pos.method_10263()+.5f;centerY=pos.method_10264()+.5f;centerZ=pos.method_10260()+.5f;}
 private float[] data; private int size,vertex=-1; private boolean shade=true;
 public static ShadedBlockSbbBuilder create(){return new ShadedBlockSbbBuilder();}
 public void begin(){data=new float[4096];size=0;vertex=-1;shade=true;}
 public SuperByteBuffer end(){return new SuperByteBuffer(Arrays.copyOf(data,size));}
 public class_4588 unwrap(boolean shade){this.shade=shade;return this;}
 public class_4588 method_22912(float x,float y,float z){if(size+18>data.length)data=Arrays.copyOf(data,data.length*2);vertex=size;size+=18;data[vertex]=x;data[vertex+1]=y;data[vertex+2]=z;data[vertex+6]=1;data[vertex+14]=shade?1:0;data[vertex+15]=centerX;data[vertex+16]=centerY;data[vertex+17]=centerZ;return this;}
 public class_4588 method_1336(int r,int g,int b,int a){data[vertex+3]=r/255f;data[vertex+4]=g/255f;data[vertex+5]=b/255f;data[vertex+6]=a/255f;return this;}
 public class_4588 method_39415(int c){return method_1336(c>>16&255,c>>8&255,c&255,c>>>24);}
 public class_4588 method_22913(float u,float v){data[vertex+7]=u;data[vertex+8]=v;return this;}
 public class_4588 method_60796(int u,int v){data[vertex+9]=u|(v<<16);return this;}
 public class_4588 method_22921(int u,int v){data[vertex+10]=u|(v<<16);return this;}
 public class_4588 method_22914(float x,float y,float z){data[vertex+11]=x;data[vertex+12]=y;data[vertex+13]=z;return this;}
 public class_4588 method_75298(float width){return this;}
 @Override public void method_22920(class_4587.class_4665 pose,class_777 quad,float[] brightness,float r,float g,float b,float a,int[] lights,int overlay){shade=quad.comp_3725();class_4588.super.method_22920(pose,quad,brightness,r,g,b,a,lights,overlay);}
 @Override public void method_22919(class_4587.class_4665 pose,class_777 quad,float r,float g,float b,float a,int light,int overlay){shade=quad.comp_3725();class_4588.super.method_22919(pose,quad,r,g,b,a,light,overlay);}
}
