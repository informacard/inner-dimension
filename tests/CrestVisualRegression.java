import com.innerdimension.client.crest.CrestHudAppearance;
import com.innerdimension.projection.CrestAnimation;
import java.util.List;
import java.util.Map;
import net.minecraft.class_4587;
import net.minecraft.class_630;
import org.joml.Vector3f;

public final class CrestVisualRegression {
    private static void check(boolean condition,String message){
        if(!condition)throw new AssertionError(message);
    }
    private static void near(float a,float b){
        check(Math.abs(a-b)<.00001f,a+" != "+b);
    }
    public static void main(String[] args){
        var idle=CrestHudAppearance.idle();
        check(idle.alpha()>0&&idle.width()==30&&idle.height()==20,"No-projection HUD must stay visible");
        var start=CrestHudAppearance.projected(new CrestAnimation.Frame(0,.3f,1),.3f,.6f);
        var peak=CrestHudAppearance.projected(new CrestAnimation.Frame(.5f,.6f,1),.3f,.6f);
        check(start.height()==idle.height()&&peak.height()>start.height(),"Expand from persistent HUD size");
        check(start.alpha()>idle.alpha(),"Expansion must be more opaque");
        var closed=CrestHudAppearance.projected(new CrestAnimation.Frame(.3f,0,0),.3f,.6f);
        check(closed.equals(idle),"Retraction must return to idle, never disappear");
        var scaledIdle=CrestHudAppearance.idle(2,40);
        check(scaledIdle.width()==60&&scaledIdle.height()==40&&scaledIdle.alpha()==102,
                "Configured size and idle opacity must affect the HUD");
        check(CrestHudAppearance.projected(new CrestAnimation.Frame(0,0,0),.3f,.6f,2,40).equals(scaledIdle),
                "Retraction must return to configured appearance");
        check(CrestHudAppearance.idle(1,0).alpha()==0,"Zero idle opacity must be transparent");
        var custom=CrestHudAppearance.projected(new CrestAnimation.Frame(4,8,1),1,8);
        check(custom.width()<=48&&custom.height()<=32,"Large configured crests must stay compact");
        var flat=CrestHudAppearance.projected(new CrestAnimation.Frame(0,1,1),1,1);
        check(flat.alpha()>0&&flat.height()<=32,"Equal stage sizes must remain valid");

        // Exercise Minecraft's actual body-part transformation without launching a client.
        var body=new class_630(List.of(),Map.of());
        var matrices=new class_4587();
        Vector3f standing=matrices.method_23760().method_23761().transformPosition(new Vector3f(0,.55f,-.215f));
        matrices.method_22903();
        body.field_3656=3.2f;
        body.field_3654=.5f;
        body.method_22703(matrices);
        Vector3f crouched=matrices.method_23760().method_23761().transformPosition(new Vector3f(0,.55f,-.215f));
        near(crouched.y,.2f+(float)Math.cos(.5)*.55f+(float)Math.sin(.5)*.215f);
        near(crouched.z,(float)Math.sin(.5)*.55f-(float)Math.cos(.5)*.215f);
        check(crouched.distance(standing)>.1f,"Crest must move and tilt with the torso");
        matrices.method_22909();
        Vector3f restored=matrices.method_23760().method_23761().transformPosition(new Vector3f(0,.55f,-.215f));
        near(restored.distance(standing),0);
        System.out.println("Crest HUD and body pose regressions passed");
    }
}
