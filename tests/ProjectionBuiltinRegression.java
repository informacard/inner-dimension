import com.innerdimension.projection.*;
import com.innerdimension.projection.builtin.*;
import io.netty.buffer.Unpooled;
import net.minecraft.*;
import java.util.*;
public final class ProjectionBuiltinRegression {
 static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 public static void main(String[] args){
  var spec=new ProjectionSourceSpec(new ProjectionSourceSpec.Volume(-17,-4,-2,20,12,8),new ProjectionSourceSpec.Volume(-17,-4,-2,20,12,8),2.125,-3,3.5);
  class_2338 pos=new class_2338(-17,-4,-2);class_2487 nbt=new class_2487();nbt.method_10582("text","display");
  var packet=new ProjectionInitialPayload(true,spec,new ProjectionUpdatePayload(19,UUID.randomUUID(),-1,0,true,0,1,List.of(new ProjectionUpdatePayload.Entry(pos,class_2248.method_9507(class_2246.field_10340.method_9564()),nbt))));
  var buf=new class_9129(Unpooled.buffer(),null);
  try{ProjectionInitialPayload.CODEC.encode(buf,packet);check(packet.equals(ProjectionInitialPayload.CODEC.decode(buf)),"Initial transport preserves source, shell, anchors, UUID and display NBT");}finally{buf.release();}
  var snapshot=new RoomProjectionContraption();snapshot.sourceSpec(spec);snapshot.applyBatch(packet.fragment().entries(),true,0);
  check(snapshot.bounds.equals(spec.source().bounds()),"Custom source bounds preserved");
  check(snapshot.getBlocks().containsKey(pos),"Initial shell accepted");
  check(!snapshot.validInterior(pos),"Shell cannot receive interior updates");
  var builder=ShadedBlockSbbBuilder.create();builder.begin();builder.method_22912(-16.25f,32.5f,17.75f).method_1336(13,27,99,128).method_22913(.25f,.75f).method_60796(0,10).method_22921(240,128).method_22914(0,1,0);
  var mesh=builder.end().disableDiffuse();check(!mesh.isEmpty(),"Mesh records vertex");
  var capture=new Capture();mesh.renderInto(new class_4587().method_23760(),capture);
  check(capture.x==-16.25f && capture.y==32.5f && capture.z==17.75f,"Negative and section-crossing vertex coordinates preserved");
  check(capture.r==13 && capture.g==27 && capture.b==99 && capture.a==128,"Vertex color preserved");
  check(capture.u==.25f && capture.v==.75f,"UV preserved");
  check(capture.overlay==(10<<16) && capture.light==((128<<16)|240),"Packed overlay and light preserved");
  check(capture.ny==1,"Normal preserved");mesh.delete();check(mesh.isEmpty(),"Invalidation releases mesh");
  check(!AbstractContraptionEntity.class.isAssignableFrom(class_1297.class),"Entity implementation has its own type hierarchy");
  System.out.println("ProjectionBuiltinRegression: 12 checks passed");
 }
 static final class Capture implements class_4588 {
  float x,y,z,u,v,ny;int r,g,b,a,overlay,light;
  public class_4588 method_22912(float x,float y,float z){this.x=x;this.y=y;this.z=z;return this;}
  public class_4588 method_1336(int r,int g,int b,int a){this.r=r;this.g=g;this.b=b;this.a=a;return this;}
  public class_4588 method_39415(int c){return method_1336(c>>16&255,c>>8&255,c&255,c>>>24);}
  public class_4588 method_22913(float u,float v){this.u=u;this.v=v;return this;}
  public class_4588 method_60796(int u,int v){overlay=u|(v<<16);return this;}
  public class_4588 method_22921(int u,int v){light=u|(v<<16);return this;}
  public class_4588 method_22914(float x,float y,float z){ny=y;return this;}
  public class_4588 method_75298(float width){return this;}
 }
}
