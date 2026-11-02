import com.innerdimension.domain.MultiDomainBudget;
import com.innerdimension.boss.BossContestRules;

public final class MultiDomainBudgetRegression {
    static int checks;
    static void near(double a,double b,String reason){checks++;if(Math.abs(a-b)>1e-9)throw new AssertionError(reason+": "+a+" != "+b);}
    public static void main(String[] args){
        var account=new MultiDomainBudget();
        near(account.debit(1000,4,140,0,0).remaining(),856,"first contact charges once");
        near(account.debit(856,4,140,0,0).remaining(),852,"sustained contact no repeated fee");
        near(account.debit(852,6,180,0,0).remaining(),806,"stronger contact tops up difference");
        near(account.debit(806,2,100,0,0).remaining(),804,"weaker survivor does not refund or charge again");
        near(account.debit(804,6,180,0,0).remaining(),798,"recontact retains payment history");
        var fresh=new MultiDomainBudget();
        near(fresh.debit(1000,6,180,0,0).remaining(),814,"new cast receives fresh budget account");
        var concurrent=new MultiDomainBudget();
        var boss=BossContestRules.debit(1000,5,0,100);
        var player=concurrent.debit(boss.remaining(),7,180,5,boss.paid());
        near(player.remaining(),813,"boss and player share maximum rate and fee");
        near(player.rate(),2,"only rate difference charged");
        near(player.fee(),80,"only fee difference charged");
        boss=BossContestRules.debit(player.remaining(),5,concurrent.paid(),100);
        near(concurrent.debit(boss.remaining(),7,180,5,boss.paid()).remaining(),806,"next tick no duplicate fee");
        near(new MultiDomainBudget().debit(10,20,200,0,0).remaining(),0,"exhaustion clamped");
        near(new MultiDomainBudget().debit(100,1,0,0,0).remaining(),99,"reversed closed has no entry fee");
        double[] rates={2,8,4},fees={100,220,160};
        for(int first=0;first<3;first++)for(int second=0;second<3;second++)if(first!=second){
            int third=3-first-second;
            double rate=Math.max(rates[first],Math.max(rates[second],rates[third]));
            double fee=Math.max(fees[first],Math.max(fees[second],fees[third]));
            near(new MultiDomainBudget().debit(1000,rate,fee,0,0).remaining(),772,"all arrival permutations have same debit");
        }
        var partners=new java.util.HashMap<String,String>();
        partners.put("A","B");partners.put("B","A");
        near(com.innerdimension.domain.DomainContestRules.closedGroup("A","B",partners::get).size(),2,"existing pair does not collapse itself");
        near(com.innerdimension.domain.DomainContestRules.closedGroup("A","C",partners::get).size(),3,"third cast collapses all three endpoints");
        near(com.innerdimension.domain.DomainContestRules.closedGroup("C","B",partners::get).size(),3,"third cast order does not omit earlier endpoint");
        partners.put("C","D");partners.put("D","C");
        near(com.innerdimension.domain.DomainContestRules.closedGroup("A","C",partners::get).size(),4,"colliding pairs collapse all four endpoints");
        System.out.println("PASS "+checks+" multi-domain resource checks");
    }
}
