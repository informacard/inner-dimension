package smoke;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.*;
import com.innerdimension.projection.*;
import java.util.*;
import sun.misc.Unsafe;
public final class Collision248Smoke implements ClientModInitializer {
 static Unsafe unsafe;
 static int checks;
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
 static void near(double a,double b,String message){check(Math.abs(a-b)<1e-7,message+" actual="+a+" expected="+b);}
 public void onInitializeClient(){try{
  var uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);unsafe=(Unsafe)uf.get(null);
  check(Arrays.stream(class_1297.class.getDeclaredMethods()).anyMatch(m->m.getName().contains("inner$solverShapes")),"Shared solver mixin transformed");
  var bind=class_6880.class_6883.class.getDeclaredMethod("method_40235",java.util.Collection.class);bind.setAccessible(true);
  for(var fluid:class_7923.field_41173)bind.invoke(fluid.method_40178(),Set.of());
  World world=(World)unsafe.allocateInstance(World.class);
  RoomProjectionEntity p=(RoomProjectionEntity)unsafe.allocateInstance(RoomProjectionEntity.class);
  set(class_1297.class,p,"field_6002",world);set(class_1297.class,p,"field_22467",class_243.field_1353);set(class_1297.class,p,"field_6005",new class_238(-10,-10,-10,10,10,10));
  var builder=new class_2945.class_9222(p);var init=RoomProjectionEntity.class.getDeclaredMethod("method_5693",class_2945.class_9222.class);init.setAccessible(true);init.invoke(p,builder);
  var entries=class_2945.class_9222.class.getDeclaredField("field_49023");entries.setAccessible(true);
  var ctor=class_2945.class.getDeclaredConstructors()[0];ctor.setAccessible(true);var tracker=(class_2945)ctor.newInstance(p,entries.get(builder));set(class_1297.class,p,"field_6011",tracker);
  var phase=RoomProjectionEntity.class.getDeclaredField("PHASE");phase.setAccessible(true);tracker.method_12778((class_2940)phase.get(null),ProjectionAnimation.STABLE);
  RoomProjectionContraption snapshot=new RoomProjectionContraption();p.setContraption(snapshot);
  ProjectionIndex.track(p);
  snapshot.getBlocks().put(new class_2338(0,0,0),new class_3499.class_3501(new class_2338(0,0,0),class_2246.field_10340.method_9564(),null));
  var solve=class_1297.class.getDeclaredMethod("method_20736",class_1297.class,class_243.class,class_238.class,class_1937.class,List.class);solve.setAccessible(true);
  var floor=new class_238(.2,1.01,.2,.8,2.81,.8);
  class_243 down=(class_243)solve.invoke(null,null,new class_243(0,-.2,0),floor,world,List.of());
  near(down.field_1351,-.01,"Direct solver lands on projection");
  var beside=new class_238(-.8,.1,.2,-.2,1.9,.8);
  class_243 wall=(class_243)solve.invoke(null,null,new class_243(.8,0,0),beside,world,List.of());
  near(wall.field_1352,.2,"Direct solver stops at wall");near(wall.field_1351,0,"Wall contact cannot lift actor");
  var below=new class_238(.2,-2,.2,.8,-.2,.8);
  class_243 up=(class_243)solve.invoke(null,null,new class_243(0,.5,0),below,world,List.of());near(up.field_1351,.2,"Jump clips ceiling");
  var above=new class_238(-.8,1.01,.2,-.2,2.81,.8);
  class_243 step=(class_243)solve.invoke(null,null,new class_243(.8,0,0),above,world,new ProjectionCollisionSeed(List.of(),List.of(class_259.method_1078(new class_238(0,0,0,1,4,1)))));near(step.field_1352,.8,"Raised step candidate clears wall and stale seed");
  var foreignStop=class_259.method_1078(new class_238(-.1,0,0,0,3,1));
  List<class_265> otherMod=List.of(foreignStop);
  class_243 combined=(class_243)solve.invoke(null,null,new class_243(.8,0,0),beside,world,otherMod);
  near(combined.field_1352,.1,"Other mod's nearer wall is retained");check(otherMod.size()==1&&otherMod.get(0)==foreignStop,"Immutable caller list is not changed");
  var foreign=class_259.method_1078(new class_238(8,8,8,9,9,9));var old=class_259.method_1078(new class_238(0,0,0,1,1,1));var fresh=class_259.method_1078(new class_238(0,1,0,1,2,1));
  List<class_265> original=List.of(foreign);check(ProjectionCollisionSeed.refresh(original,List.of())==original,"No projection preserves original list");
  var seeded=ProjectionCollisionSeed.refresh(original,List.of(old));var refreshed=ProjectionCollisionSeed.refresh(seeded,List.of(fresh));
  check(refreshed.size()==2&&refreshed.contains(foreign)&&refreshed.contains(fresh)&&!refreshed.contains(old),"Refresh preserves foreign and replaces old geometry");
  check(ProjectionCollisionSeed.refresh(refreshed,List.of(fresh)).size()==2,"Repeated solver does not duplicate projection");
  check(ProjectionCollisionSeed.refresh(refreshed,List.of()).equals(original),"Retraction drops seed without touching foreign collisions");
  System.out.println("COLLISION_248_SMOKE_PASS checks="+checks+" lithium="+net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("lithium"));System.exit(0);
 }catch(Throwable e){e.printStackTrace();System.out.println("COLLISION_248_SMOKE_FAIL");System.exit(1);}}
 static void set(Class<?> type,Object object,String name,Object value)throws Exception{var f=type.getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
 public static class World extends class_638 {
  public World(){super(null,null,null,null,0,0,null,false,0,0);}
  @Override public class_2791 method_8402(int x,int z,class_2806 status,boolean create){return null;}
  @Override public class_1922 method_22338(int x,int z){return null;}
  @Override public List<class_265> method_20743(class_1297 e,class_238 box){return List.of();}
  @Override public Iterable<class_265> method_20812(class_1297 e,class_238 box){return List.of();}
  @Override public class_2784 method_8621(){return new class_2784();}
  @Override public int method_31607(){return -64;}
  @Override public int method_31605(){return 384;}
 }
}
