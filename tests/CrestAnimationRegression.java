import com.innerdimension.projection.CrestAnimation;
import com.innerdimension.projection.ProjectionAnimation;

/** Timeline boundaries must not pop when the server changes projection phases. */
public final class CrestAnimationRegression {
    private static CrestAnimation.Frame at(int phase,double ticks){
        return CrestAnimation.sample(phase,ticks,30,10,0,.5f,.3f,.3f,.6f,.55f);
    }
    private static void near(float a,float b){
        if(Math.abs(a-b)>.00001f)throw new AssertionError(a+" != "+b);
    }
    private static void same(CrestAnimation.Frame a,CrestAnimation.Frame b){
        near(a.distance(),b.distance());near(a.size(),b.size());near(a.opacity(),b.opacity());
    }
    public static void main(String[] args){
        same(at(ProjectionAnimation.EXPANDING,30),at(ProjectionAnimation.INTERIOR,0));
        same(at(ProjectionAnimation.INTERIOR,10),at(ProjectionAnimation.STABLE,0));
        same(at(ProjectionAnimation.STABLE,0),at(ProjectionAnimation.RETRACTING,0));
        // Packet phase updates can lag behind local elapsed time: clamp overshoot.
        same(at(ProjectionAnimation.EXPANDING,300),at(ProjectionAnimation.EXPANDING,30));
        same(at(ProjectionAnimation.INTERIOR,100),at(ProjectionAnimation.STABLE,0));
        near(at(ProjectionAnimation.RETRACTING,2).opacity(),.5f);
        near(at(ProjectionAnimation.RETRACTING,4).opacity(),0);
        near(at(ProjectionAnimation.RETRACTING,100).size(),0);
        if(at(ProjectionAnimation.EXPANDING,15).size()<=at(ProjectionAnimation.EXPANDING,0).size())
            throw new AssertionError("HUD must enlarge with expansion");
        System.out.println("Crest animation regression passed");
    }
}
