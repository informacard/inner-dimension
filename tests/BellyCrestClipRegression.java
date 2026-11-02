import java.lang.reflect.*;
import java.util.*;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/** UV clipping must interpolate surface positions instead of moving them to a flat plane. */
public final class BellyCrestClipRegression {
    public static void main(String[] args)throws Exception{
        Class<?> point=Class.forName("com.innerdimension.client.crest.BellyCrestLayer$Point");
        var ctor=point.getDeclaredConstructors()[0];ctor.setAccessible(true);
        Class<?> layer=Class.forName("com.innerdimension.client.crest.BellyCrestLayer");
        var clip=layer.getDeclaredMethod("clip",List.class,boolean.class,float.class,boolean.class);clip.setAccessible(true);
        var frontV=layer.getDeclaredMethod("frontV",float.class,boolean.class);frontV.setAccessible(true);
        near((float)frontV.invoke(null,27f,true),27f);
        near((float)frontV.invoke(null,31f,true),32f);
        near((float)frontV.invoke(null,30f,true),30.75f);
        near((float)frontV.invoke(null,28f,false),28f);
        var pointAt=layer.getDeclaredMethod("pointAt",point,point,point,float.class,float.class);pointAt.setAccessible(true);
        Object a=ctor.newInstance(0f,0f,1f,22f,27f),b=ctor.newInstance(4f,0f,3f,26f,27f),c=ctor.newInstance(4f,5f,3f,26f,32f);
        // Expansion must extrapolate past every edge, keeping the same surface tangent.
        for(float u:new float[]{16.8f,22f,24f,26f,31.2f})for(float v:new float[]{24.2f,27f,29f,32f,33.8f}){
            Object p=pointAt.invoke(null,a,b,c,u,v);
            near(get(point,p,"u"),u);near(get(point,p,"v"),v);
            near(get(point,p,"x"),u-22);near(get(point,p,"y"),v-27);near(get(point,p,"z"),1+(u-22)*.5f);
            Object mirrored=pointAt.invoke(null,a,c,b,u,v);
            near(get(point,mirrored,"x"),get(point,p,"x"));near(get(point,mirrored,"z"),get(point,p,"z"));
        }
        List<?> polygon=List.of(ctor.newInstance(0f,0f,0f,20f,28f),ctor.newInstance(8f,0f,4f,28f,28f),
                ctor.newInstance(8f,4f,4f,28f,32f),ctor.newInstance(0f,4f,0f,20f,32f));
        polygon=(List<?>)clip.invoke(null,polygon,true,22f,true);
        polygon=(List<?>)clip.invoke(null,polygon,true,26f,false);
        polygon=(List<?>)clip.invoke(null,polygon,false,29f,true);
        polygon=(List<?>)clip.invoke(null,polygon,false,31f,false);
        if(polygon.size()!=4)throw new AssertionError("Expected cropped quad");
        for(Object p:polygon){
            float u=get(point,p,"u"),v=get(point,p,"v"),x=get(point,p,"x"),y=get(point,p,"y"),z=get(point,p,"z");
            if(u<22||u>26||v<29||v>31)throw new AssertionError("UV escaped cropped region");
            near(x,u-20);near(y,v-28);near(z,x*.5f);
        }
        if(!((List<?>)clip.invoke(null,polygon,true,40f,true)).isEmpty())throw new AssertionError("Other skin island survived");
        if(!((List<?>)clip.invoke(null,List.of(),true,22f,true)).isEmpty())throw new AssertionError("Empty input");
        var clearance=layer.getDeclaredMethod("surfaceClearance",List.class,List.class,Vector3f.class);clearance.setAccessible(true);
        // A growing upper belly can protrude past the lower belly's tangent.
        // All effect vertices must clear it after crouch/pitch/yaw transforms as well.
        for(float growth:new float[]{0,.2f,.8f,2f})for(float pitch:new float[]{0,.6f,1.4f}){
            var rotation=new Quaternionf().rotationXYZ(pitch,.7f,-.2f);
            var normal=new Vector3f(0,0,-1).rotate(rotation);
            var surface=new ArrayList<Vector3f>();
            var plane=new ArrayList<Vector3f>();
            for(float x:new float[]{-.25f,.25f})for(float y:new float[]{-.4f,.4f}){
                plane.add(new Vector3f(x,y,-.1f).rotate(rotation).add(4,2,-3));
                surface.add(new Vector3f(x,y,-.1f-growth).rotate(rotation).add(4,2,-3));
            }
            float shift=(float)clearance.invoke(null,surface,plane,normal);
            near(shift,growth+.003f);
            for(var p:plane)for(var s:surface){
                float gap=new Vector3f(p).fma(shift,normal).dot(normal)-s.dot(normal);
                if(gap<.00299f)throw new AssertionError("Pregnant belly occludes plane: "+gap);
            }
        }
        System.out.println("Belly surface clipping regression passed");
    }
    private static float get(Class<?> type,Object p,String name)throws Exception{var m=type.getDeclaredMethod(name);m.setAccessible(true);return (float)m.invoke(p);}
    private static void near(float a,float b){if(Math.abs(a-b)>1e-5f)throw new AssertionError(a+" != "+b);}
}
