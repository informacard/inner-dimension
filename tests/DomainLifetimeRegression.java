import com.innerdimension.projection.*;
import com.innerdimension.client.crest.CrestHudAppearance;
public final class DomainLifetimeRegression {
    static void check(boolean ok,String why) { if(!ok) throw new AssertionError(why); }
    public static void main(String[] args) {
        check(DomainLifetime.MAX_TICKS==30*20,"Maximum duration");
        check(DomainLifetime.FORCED_BURNOUT_TICKS==10*20,"Forced burnout");
        check(DomainLifetime.burnoutTicks(0)==60,"Immediate close minimum");
        check(DomainLifetime.burnoutTicks(179)==60,"Short close minimum");
        check(DomainLifetime.burnoutTicks(360)==120,"12 seconds gives 4 second burnout");
        check(DomainLifetime.burnoutTicks(599)==199,"Close just before deadline");
        var peak=new CrestAnimation.Frame(.3f,.55f,1);
        var idle=CrestHudAppearance.idle();
        float lastOpacity=2,lastSize=2;
        for(int t=0;t<=600;t++) {
            var f=CrestAnimation.fade(peak,.3f,DomainLifetime.strength(t));
            check(f.opacity()<=lastOpacity && f.size()<=lastSize,"Monotonic decay");
            lastOpacity=f.opacity();lastSize=f.size();
        }
        var end=CrestAnimation.fade(peak,.3f,DomainLifetime.strength(600));
        check(end.opacity()==0 && end.size()==.3f && end.distance()==peak.distance(),"Ordinary appearance at deadline preserves body offset");
        check(CrestHudAppearance.projected(end,.3f,.6f).equals(idle),"HUD returns to normal");
        var burnout=CrestHudAppearance.idle(.9,3);
        check(burnout.height()<idle.height() && burnout.alpha()<idle.alpha()/5,"Burnout smaller and nearly invisible");
        check(DomainLifetime.strength(999)==0,"Clamped late packet");
        System.out.println("PASS: duration, manual minimum, proportional burnout, decay, ordinary endpoint, burnout appearance");
    }
}
