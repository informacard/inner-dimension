import com.innerdimension.client.AnimationTimeline;
import com.innerdimension.client.AnimationEnvelope;
public final class EnlightenmentVisualRegression {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        int[] durations={60,70,60,70};
        check(AnimationTimeline.duration(durations)==260,"GIF timings retained");
        for(int cycle=0;cycle<100;cycle++){
            long t=cycle*260L;
            check(AnimationTimeline.frame(t,durations)==0,"Wrap to first frame");
            check(AnimationTimeline.frame(t+59,durations)==0,"First frame boundary");
            check(AnimationTimeline.frame(t+60,durations)==1,"Second frame boundary");
            check(AnimationTimeline.frame(t+129,durations)==1,"Unequal delay honored");
            check(AnimationTimeline.frame(t+259,durations)==3,"No blank endpoint");
        }
        check(AnimationTimeline.frame(-1,durations)==3,"Negative time safe");
        check(AnimationEnvelope.opacity(0,4000)==0,"Starts black");
        check(AnimationEnvelope.opacity(2000,4000)==1,"Full screen middle");
        check(AnimationEnvelope.opacity(4000,4000)==0,"Ends black");
        for(int t=-100;t<4200;t++)check(AnimationEnvelope.opacity(t,4000)>=0&&AnimationEnvelope.opacity(t,4000)<=1,"Bounded fade");
        check(com.innerdimension.voiddomain.RitualVisualSchedule.playMillis(1)==8670,"Eye plays one complete original cycle");
        check(com.innerdimension.voiddomain.RitualVisualSchedule.playMillis(2)==9000,"Second clip lasts nine seconds");
        check(com.innerdimension.voiddomain.RitualVisualSchedule.playMillis(3)==9000,"Third clip lasts nine seconds");
        check(com.innerdimension.voiddomain.RitualVisualSchedule.playTicks(1)*50>=8670,"Server does not leave before final eye frame");
        for(long duration:new long[]{8670,9000}){
            check(com.innerdimension.client.RitualVisualTiming.aperture(0,duration)==0,"Preview starts with lids closed");
            check(com.innerdimension.client.RitualVisualTiming.aperture(2000,duration)==0.5,"Slow opening remains intact");
            check(com.innerdimension.client.RitualVisualTiming.aperture(4000,duration)==1,"Fully opened");
            check(com.innerdimension.client.RitualVisualTiming.aperture(duration,duration)==1,"Closing begins after playback");
            check(com.innerdimension.client.RitualVisualTiming.aperture(duration+120,duration)==0,"Lids fully close");
        }
        System.out.println("PASS: unequal GIF frame durations, 100 wraps, no blank endpoint, ritual fade boundaries");
    }
}
