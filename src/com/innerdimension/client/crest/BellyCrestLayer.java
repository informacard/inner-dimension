package com.innerdimension.client.crest;

import com.innerdimension.client.DomainCrestFeature;
import com.innerdimension.projection.*;
import com.nonid.client.PlayerBellyRenderer;
import java.util.*;
import net.minecraft.*;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.*;
import software.bernie.geckolib.cache.model.cuboid.*;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.*;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

/** Samples the same posed cubes as NoN, including smoothed pregnancy and liquid growth. */
public final class BellyCrestLayer extends GeoRenderLayer<GeoAnimatable,Object,GeoRenderState> {
    private static final DataTicket<UUID> OWNER=DataTicket.create("inner_dimension_belly_owner",UUID.class);
    public BellyCrestLayer(GeoRenderer<GeoAnimatable,Object,GeoRenderState> renderer){super(renderer);}
    @Override public void addRenderData(GeoAnimatable animatable,Object context,GeoRenderState state,float partial){
        if(context instanceof PlayerBellyRenderer.RenderContext belly)state.addGeckolibData(OWNER,belly.playerUuid());
    }
    @Override public void submitRenderTask(RenderPassInfo<GeoRenderState> pass,class_11659 queue){
        UUID owner=pass.getGeckolibData(OWNER);
        var client=class_310.method_1551();
        if(!pass.willRender()||owner==null||client.field_1687==null)return;
        var projection=ProjectionIndex.active(client.field_1687,owner);
        if(projection==null||projection.phase()==ProjectionAnimation.RETRACTING)return;
        var frame=DomainCrestFeature.animation(projection,client.method_61966().method_60637(true));
        var texture=DomainCrestFeature.texture(owner);
        if(texture==null||frame.size()<=0||frame.opacity()<=0)return;
        // renderPosed installs this pass's animation snapshots and restores them afterwards.
        // Copy geometry into queued tasks now; never retain mutable bones in a callback.
        pass.renderPosed(()->{
            var surface=new ArrayList<org.joml.Vector3f>();
            for(var bone:pass.model().topLevelBones())collectSurface(pass.poseStack(),bone,surface);
            for(var bone:pass.model().topLevelBones())submitBone(pass.poseStack(),bone,queue,texture,frame,owner,surface);
        });
    }
    private static void collectSurface(class_4587 stack,GeoBone bone,List<org.joml.Vector3f> surface){
        stack.method_22903();
        try{
            RenderUtil.prepMatrixForBone(stack,bone);
            if((bone.frameSnapshot==null||!bone.frameSnapshot.isHidden())&&bone instanceof CuboidGeoBone cuboid){
                boolean outer=Arrays.stream(cuboid.cubes).anyMatch(BellyCrestLayer::jacketCube);
                for(var cube:cuboid.cubes){
                    if(outer&&!jacketCube(cube))continue;
                    stack.method_22903();
                    try{
                        cube.translateToPivotPoint(stack);cube.rotate(stack);cube.translateAwayFromPivotPoint(stack);
                        var matrix=stack.method_23760().method_23761();
                        for(var quad:cube.quads())if(quad!=null)for(var vertex:quad.vertices()){
                            surface.add(matrix.transformPosition(new org.joml.Vector3f(vertex.posX(),vertex.posY(),vertex.posZ())));
                        }
                    }finally{stack.method_22909();}
                }
            }
            if(bone.frameSnapshot==null||!bone.frameSnapshot.areChildrenHidden())for(var child:bone.children())collectSurface(stack,child,surface);
        }finally{stack.method_22909();}
    }
    private static void submitBone(class_4587 stack,GeoBone bone,class_11659 queue,class_2960 texture,CrestAnimation.Frame frame,UUID owner,List<org.joml.Vector3f> surface){
        stack.method_22903();
        try{
            RenderUtil.prepMatrixForBone(stack,bone);
            if((bone.frameSnapshot==null||!bone.frameSnapshot.isHidden())&&bone instanceof CuboidGeoBone cuboid){
                boolean outer=Arrays.stream(cuboid.cubes).anyMatch(BellyCrestLayer::jacketCube);
                for(var cube:cuboid.cubes){
                    if(outer&&!jacketCube(cube))continue;
                    stack.method_22903();
                    try{
                        cube.translateToPivotPoint(stack);cube.rotate(stack);cube.translateAwayFromPivotPoint(stack);
                        for(var quad:cube.quads())if(quad!=null)submitQuad(stack,quad,queue,texture,frame,outer,owner,surface);
                    }finally{stack.method_22909();}
                }
            }
            if(bone.frameSnapshot==null||!bone.frameSnapshot.areChildrenHidden())for(var child:bone.children())submitBone(stack,child,queue,texture,frame,owner,surface);
        }finally{stack.method_22909();}
    }
    private static boolean jacketCube(GeoCube cube){
        for(var quad:cube.quads())if(quad!=null)for(var vertex:quad.vertices()){
            float u=vertex.texU()*64,v=vertex.texV()*64;
            if(u>=20&&u<=28&&v>=36&&v<=48)return true;
        }
        return false;
    }
    private static void submitQuad(class_4587 stack,GeoQuad quad,class_11659 queue,class_2960 texture,CrestAnimation.Frame frame,boolean outer,UUID owner,List<org.joml.Vector3f> surface){
        // Use the posed lower front as the surface anchor. Extend its tangent beyond
        // its edges so expansion has geometry for every texel, including off-body ones.
        if(Math.abs(quad.normalZ())<.5f)return;
        float minV=Float.POSITIVE_INFINITY,maxV=Float.NEGATIVE_INFINITY;
        float minU=Float.POSITIVE_INFINITY,maxU=Float.NEGATIVE_INFINITY;
        for(var vertex:quad.vertices()){
            float v=vertex.texV()*64-(outer?16:0);
            minV=Math.min(minV,v);maxV=Math.max(maxV,v);
            minU=Math.min(minU,vertex.texU()*64);maxU=Math.max(maxU,vertex.texU()*64);
        }
        boolean lowerFront=Math.abs(quad.normalY())<.5f&&Math.abs(minV-27)<.001f&&Math.abs(maxV-31)<.001f;
        if(!lowerFront||Math.abs(minU-22)>.001f||Math.abs(maxU-26)>.001f)return;
        float width=frame.size()*24,height=frame.size()*16;
        float u0=24-width/2,v0=29-height/2;
        List<Point> points=new ArrayList<>();
        for(var vertex:quad.vertices()){
            float v=vertex.texV()*64-(outer?16:0);
            points.add(new Point(vertex.posX(),vertex.posY(),vertex.posZ(),vertex.texU()*64,frontV(v,lowerFront)));
        }
        Point a=points.get(0),b=points.get(1),c=points.get(2);
        // Preserve the model's winding even for mirrored UVs.
        float winding=(b.u-a.u)*(c.v-a.v)-(b.v-a.v)*(c.u-a.u);
        if(Math.abs(winding)<1e-5f)return;
        var captured=new ArrayList<Point>(List.of(pointAt(a,b,c,u0,v0),pointAt(a,b,c,u0+width,v0),
                pointAt(a,b,c,u0+width,v0+height),pointAt(a,b,c,u0,v0+height)));
        if(winding<0)Collections.reverse(captured);
        DomainCrestFeature.bellySubmitted(owner);
        var normal=quad.normalVec();
        var anchorPose=stack.method_23760();
        var outward=anchorPose.method_23762().transform(new org.joml.Vector3f(normal)).normalize();
        var plane=new ArrayList<org.joml.Vector3f>();
        for(var point:captured)plane.add(anchorPose.method_23761().transformPosition(new org.joml.Vector3f(point.x,point.y,point.z)));
        float clearance=surfaceClearance(surface,plane,outward);
        queue.method_73483(stack,class_12249.method_75984(texture,false),(pose,out)->{
            var n=pose.method_23762().transform(new org.joml.Vector3f(normal)).normalize();
            int light=class_765.method_23687(15,15);
            for(int i=1;i<captured.size()-1;i++){
                emit(pose,out,captured.get(0),n,u0,v0,width,height,light,frame.opacity(),clearance);
                emit(pose,out,captured.get(i),n,u0,v0,width,height,light,frame.opacity(),clearance);
                emit(pose,out,captured.get(i+1),n,u0,v0,width,height,light,frame.opacity(),clearance);
                emit(pose,out,captured.get(i+1),n,u0,v0,width,height,light,frame.opacity(),clearance);
            }
        });
    }
    private static float surfaceClearance(List<org.joml.Vector3f> surface,List<org.joml.Vector3f> plane,org.joml.Vector3f outward){
        float front=Float.NEGATIVE_INFINITY,anchor=Float.POSITIVE_INFINITY;
        for(var vertex:surface)front=Math.max(front,vertex.dot(outward));
        for(var vertex:plane)anchor=Math.min(anchor,vertex.dot(outward));
        // The entire posed belly must stay behind the effect's plane. A tangent of
        // the rotated lower belly can otherwise pass straight through the upper belly.
        return Math.max(0,front-anchor)+.003f;
    }
    private static Point pointAt(Point a,Point b,Point c,float u,float v){
        float bu=b.u-a.u,bv=b.v-a.v,cu=c.u-a.u,cv=c.v-a.v;
        float determinant=bu*cv-bv*cu;
        float s=((u-a.u)*cv-(v-a.v)*cu)/determinant;
        float t=(bu*(v-a.v)-bv*(u-a.u))/determinant;
        return new Point(a.x+(b.x-a.x)*s+(c.x-a.x)*t,
                a.y+(b.y-a.y)*s+(c.y-a.y)*t,a.z+(b.z-a.z)*s+(c.z-a.z)*t,u,v);
    }
    private static float frontV(float v,boolean lowerFront){
        // Preserve the upper seam at row 27; fit rows 27..32 on the visible face,
        // whose original UV ended at row 31. Geometry and normals remain unchanged.
        return lowerFront?27+(v-27)*1.25f:v;
    }
    private static void emit(class_4587.class_4665 pose,class_4588 out,Point p,org.joml.Vector3f n,float u0,float v0,float w,float h,int light,float opacity,float clearance){
        var position=pose.method_23761().transformPosition(new org.joml.Vector3f(p.x,p.y,p.z));
        // Offset in rendered space keeps the gap constant even when belly bones scale.
        position.fma(clearance,n);
        out.method_23919(position.x,position.y,position.z,((int)(opacity*255)<<24)|0xffffff,
                (p.u-u0)/w,(p.v-v0)/h,class_4608.field_21444,light,n.x,n.y,n.z);
    }
    private record Point(float x,float y,float z,float u,float v){
        Point lerp(Point b,float t){return new Point(x+(b.x-x)*t,y+(b.y-y)*t,z+(b.z-z)*t,u+(b.u-u)*t,v+(b.v-v)*t);}
    }
    private static List<Point> clip(List<Point> input,boolean uAxis,float bound,boolean lower){
        var output=new ArrayList<Point>();
        if(input.isEmpty())return output;
        Point previous=input.get(input.size()-1);
        float a=uAxis?previous.u:previous.v;
        boolean insideA=lower?a>=bound:a<=bound;
        for(Point current:input){
            float b=uAxis?current.u:current.v;
            boolean insideB=lower?b>=bound:b<=bound;
            if(insideA!=insideB)output.add(previous.lerp(current,(bound-a)/(b-a)));
            if(insideB)output.add(current);
            previous=current;a=b;insideA=insideB;
        }
        return output;
    }
}
