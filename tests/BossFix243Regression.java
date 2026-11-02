import com.innerdimension.domain.runtime.UnlimitedVoidControl;
import com.innerdimension.projection.OpenDomainRules;
import com.innerdimension.network.DomainHudPayload;
import net.minecraft.*;
import java.lang.reflect.*;
import java.util.*;
public final class BossFix243Regression {
    static Field position;
    static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
    static final class Probe extends class_1309 {
        class_1937 world;UUID id;double landingY=Double.NaN;
        Probe(){super(null,null);}
        public class_1306 method_6068(){return null;}
        public class_1937 method_73183(){return world;}
        public UUID method_5667(){return id;}
        public boolean method_5805(){return true;}
        public boolean method_7325(){return false;}
        public void method_6075(){}
        public boolean method_48105(class_3218 w,double x,double y,double z,Set<class_2709> flags,float yaw,float pitch,boolean reset){
            landingY=y;try{position.set(this,new class_243(x,y,z));}catch(Exception e){throw new RuntimeException(e);}return true;
        }
    }
    public static void main(String[] args)throws Exception{
        class_155.method_36208();class_2966.method_12851();run();
    }
    public static void run()throws Exception{
        var f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);var unsafe=(sun.misc.Unsafe)f.get(null);
        var e=(Probe)unsafe.allocateInstance(Probe.class);e.id=UUID.randomUUID();e.world=(class_3218)unsafe.allocateInstance(class_3218.class);
        for(var field:class_1297.class.getDeclaredFields())if(field.getType()==class_243.class&&!Modifier.isStatic(field.getModifiers())){field.setAccessible(true);field.set(e,class_243.field_1353);}
        position=class_1297.class.getDeclaredField("field_22467");position.setAccessible(true);position.set(e,new class_243(4,64,8));
        var eye=class_1297.class.getDeclaredField("field_18066");eye.setAccessible(true);eye.setFloat(e,1.6f);
        UnlimitedVoidControl.clear();UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(1);
        position.set(e,new class_243(4.5,64,8));UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(2);
        check(Math.abs(e.landingY-64)<1e-6,"Control correction must restore feet, never the original eye height");
        check(e.method_73189().field_1351==64,"A corrected mob must not move upward on repeated control ticks");
        UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(3);check(e.method_73189().field_1351==64,"Repeated control stays at original height");UnlimitedVoidControl.clear();
        check(OpenDomainRules.intersects(20,-1,-1,22,1,1,24),"Completed open domain covers distant Boss before expansion reaches it");
        check(!OpenDomainRules.intersects(20,-1,-1,22,1,1,1),"Animated radius has not reached Boss yet");
        check(!OpenDomainRules.intersects(22,22,22,23,23,23,24),"AABB corners outside the sphere do not trigger counter");
        var hud=new DomainHudPayload(false,"Boss",0,0,0,333,667,"",0,0,0,333,667,10,10,-1);
        var b=new class_9129(io.netty.buffer.Unpooled.buffer(),null);
        try{DomainHudPayload.CODEC.encode(b,hud);check(DomainHudPayload.CODEC.decode(b).equals(hud),"Top Boss announcement and two ten-tick phases survive wire codec");}finally{b.release();}
        System.out.println("PASS BossFix243Regression: feet-anchor correction, predictive sphere intersection, Boss announcement wire");
    }
}
