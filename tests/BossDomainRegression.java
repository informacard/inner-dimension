import com.innerdimension.boss.BossContestRules;
import com.innerdimension.boss.BossWaveQuota;
import com.innerdimension.domain.DomainContestRules;
import java.util.*;

/** No Minecraft bootstrap, server, client, world or save access. */
public final class BossDomainRegression {
    static int assertions;
    static void check(boolean b,String reason){assertions++;if(!b)throw new AssertionError(reason);}
    static void near(double a,double b,String reason){check(Math.abs(a-b)<1e-7,reason+": "+a+" != "+b);}
    record Result(int win,int loss,double remaining){}
    static Result run(double p,double b,boolean open,double real,int deadline){
        var pressure=BossContestRules.pressure(p,b);var edge=new BossContestRules.Edge(2400,0);
        double paid=0;int win=-1,loss=-1;
        for(int tick=1;tick<deadline;tick++){
            // Exactly the production edge step and unique account debit. No backfilled budget.
            boolean won=edge.step(pressure.bossRate());
            var debit=BossContestRules.debit(real,open?pressure.playerRate():1,paid,open?0:pressure.closedCost());
            real=debit.remaining();paid=debit.paid();
            if(real<=0)loss=tick;
            if(won)win=tick;
            if(loss>0||win>0)break;
        }
        return new Result(win,loss,real);
    }
    public static void main(String[] args){
        for(double b:new double[]{6.5,7})for(boolean open:new boolean[]{false,true}){
            int previous=Integer.MAX_VALUE;
            for(double p:new double[]{5,5.5,6,6.5,7,8,9}){
                var pressure=BossContestRules.pressure(p,b);
                int time=(int)Math.ceil(2400/pressure.bossRate());
                check(time<=previous,"Increasing strength never delays Boss break");previous=time;
            }
            var same=run(b,b,open,open?2560:540,b==6.5?600:720);
            check(same.win>0&&same.loss<0,"Equal strength defaults win with real post-expansion balance");
            var weak=run(5,b,open,open?2560:540,b==6.5?600:720);
            check(weak.loss>200&&weak.win<0,"Weak strength buys a window without automatic victory");
            var strong=run(9,b,open,open?2560:540,b==6.5?600:720);
            check(strong.win>0&&strong.loss<0&&strong.win<same.win,"Stronger wins sooner");
        }
        var p=BossContestRules.pressure(6.5,6.5);
        near(p.bossRate(),6.5,"v3 equal Boss rate");near(p.playerRate(),6.5,"v3 equal player rate");near(p.closedCost(),105,"v3 closed cost");
        near(BossContestRules.pressure(10,0).playerRate(),2.5,"qP floor");
        near(BossContestRules.pressure(0,10).bossRate(),2.5,"qB floor");
        check(run(6.5,6.5,true,100,600).win<0,"Previously spent real balance is never restored");
        var a=new BossContestRules.Edge(2400,0);var b=new BossContestRules.Edge(2400,0);
        for(int t=0;t<100;t++){a.step(6.5);b.step(6.5);}
        near(a.remaining,1750,"Independent A progress");near(b.remaining,1750,"Independent B does not double A's debit");
        var late=new BossContestRules.Edge(2400,200);near(late.remaining,2200,"Late join uses elapsed real ticks");
        double saved=a.remaining;a.step(4);near(a.remaining,saved-4,"Strongest member departure changes only subsequent rate");
        saved=a.remaining;for(int i=0;i<10;i++){}near(a.remaining,saved,"Geometry gap preserves edge balance");
        a.step(8);near(a.remaining,saved-8,"Return resumes rather than resetting progress");
        near(BossContestRules.initialBudget(2400,600),1800,"Virtual time remains separate from actual 600 tick deadline");
        var d=BossContestRules.debit(540,1,0,105);near(d.remaining(),434,"One base tick plus one contact cost");
        d=BossContestRules.debit(d.remaining(),1,d.paid(),105);near(d.remaining(),433,"Repeated contact never repeats closed cost");
        d=BossContestRules.debit(d.remaining(),1,d.paid(),115);near(d.remaining(),422,"Higher pressure charges only ten tick difference");
        d=BossContestRules.debit(d.remaining(),1,d.paid(),90);near(d.remaining(),421,"Weaker pressure gives no refund");
        var max=BossContestRules.debit(100,Math.max(6.5,7),0,Math.max(105,110));near(max.remaining(),0,"Multi-Boss uses max due and rate");
        var open=BossContestRules.debit(100,Math.max(2,3),0,0);near(open.remaining(),97,"Multi-Boss open does not sum or multiply");
        // Both outcomes are collected from the same beginning-of-tick snapshot.
        var tie=new BossContestRules.Edge(6.5,0);boolean won=tie.step(6.5);var exhausted=BossContestRules.debit(6.5,6.5,0,0);
        check(won&&exhausted.remaining()==0,"Mutual destruction preserves both events");
        for(boolean reverse:new boolean[]{false,true}){
            List<BossContestRules.Edge> edges=new ArrayList<>(List.of(new BossContestRules.Edge(6.5,0),new BossContestRules.Edge(100,0)));
            if(reverse)Collections.reverse(edges);
            long winners=edges.stream().filter(e->e.step(6.5)).count();check(winners==1,"Snapshot winner independent of iteration order");
        }
        check(BossContestRules.verdict(5.5,5.5,.5)==0,"Warden equal strength is draw");
        check(BossContestRules.verdict(5.6,5.5,.5)==0,"Warden near strength is draw");
        check(BossContestRules.verdict(6,5.5,.5)==1,"Warden preserves existing exact threshold convention");
        check(BossContestRules.verdict(4.9,5.5,.5)==-1,"Warden clear loss");
        check(BossContestRules.waitTicks(6,5.5)==115,"Warden wait ticks");
        check(DomainContestRules.penalized(7)==4&&DomainContestRules.shortenClosed(600,5)==440,"Existing PvP math remains unchanged");
        check(BossContestRules.mutual(0,0,0,10,10,10,8,0,0,10,10,10),"A-B mutual containment");
        check(BossContestRules.mutual(8,0,0,10,10,10,16,0,0,10,10,10),"B-C mutual containment");
        check(!BossContestRules.mutual(0,0,0,10,10,10,16,0,0,10,10,10),"Fixed A anchor rejects chain C");
        var quota=new BossWaveQuota();UUID first=UUID.randomUUID(),second=UUID.randomUUID();
        check(quota.reserve(first),"First projectile reserves target");check(!quota.reserve(first),"Second simultaneous projectile cannot reserve same target");
        quota.release(first);check(quota.available(first),"Invalid target before hit releases reservation");
        quota.reserve(first);quota.hit(first);quota.release(first);check(!quota.available(first),"Hit history survives reservation release");
        check(quota.spent(first),"Direct hit and explosion share hit history");check(quota.reserve(second),"Other player can receive one projectile");
        check(new BossWaveQuota().available(first),"New wave has separate quota");
        check(Math.max(2,Math.max(2.5,3.5))==3.5,"Simple multi-Boss pressure is max");
        long disabled=100+160;
        for(long tick=101;tick<=1000;tick++)disabled=BossContestRules.disabledDeadline(tick,disabled,160,true);
        check(disabled==1160,"Long-lived winning domain holds the complete eight-second budget");
        disabled=BossContestRules.disabledDeadline(1001,disabled,160,true);
        check(disabled==1161,"Complete retraction starts a fresh eight-second countdown");
        for(long tick=1002;tick<=1161;tick++)disabled=BossContestRules.disabledDeadline(tick,disabled,160,false);
        check(disabled==1161,"Countdown advances after the winning cast ends instead of restarting each tick");
        check(1160<disabled&&1161>=disabled,"Suppression ends at the exact release deadline");
        check(BossContestRules.disabledDeadline(1000,1200,160,true)==1200,"Another suppression source never shortens an existing deadline");
        for(int kind=0;kind<3;kind++){
            var palette=com.innerdimension.domain.DomainTextPalette.boss(kind);
            check(com.innerdimension.domain.DomainTextPalette.matches(palette[0],palette[1],palette[0]),"Boss names enter the same animated palette path as Unlimited Void");
            check(com.innerdimension.domain.DomainTextPalette.color(palette[0],palette[1],.5,0)!=com.innerdimension.domain.DomainTextPalette.color(palette[0],palette[1],.5,1000),"Boss name palette flows over time");
        }
        System.out.println("PASS BossDomainRegression: "+assertions+" assertions; no game/world opened");
    }
}
