import com.innerdimension.pregnancy.MentalPregnancyChance;
public final class MentalPregnancyChanceRegression {
    private static void equal(double expected,double actual){if(Math.abs(expected-actual)>1e-12)throw new AssertionError(expected+" != "+actual);}
    public static void main(String[] args){
        int steps=0;
        for(int i=0;i<30;i++){
            equal(Math.min(i,10)*.002,MentalPregnancyChance.probability(steps,-1));
            steps=MentalPregnancyChance.next(steps);
        }
        equal(0,MentalPregnancyChance.probability(0,2));
        equal(.032,MentalPregnancyChance.probability(1,0));
        equal(.32,MentalPregnancyChance.probability(10,0));
        equal(.38,MentalPregnancyChance.probability(10,1));
        equal(.38,MentalPregnancyChance.probability(100,5));
        steps=0;equal(0,MentalPregnancyChance.probability(steps,-1));
        equal(.002,MentalPregnancyChance.probability(MentalPregnancyChance.next(steps),-1));
        if(!MentalPregnancyChance.discoveryGuaranteed(1,false,0))throw new AssertionError("first fertile expansion must guarantee discovery");
        if(!MentalPregnancyChance.discoveryGuaranteed(1,false,1))throw new AssertionError("fertility II must guarantee discovery");
        if(MentalPregnancyChance.discoveryGuaranteed(1,false,-1))throw new AssertionError("no potion retains initial probability");
        if(MentalPregnancyChance.discoveryGuaranteed(1,true,0))throw new AssertionError("discovered pregnancies retain later probability");
        if(!MentalPregnancyChance.discoveryGuaranteed(50,false,-1))throw new AssertionError("existing discovery pity is preserved");
        equal(1,MentalPregnancyChance.probability(0,0,true));
        equal(.32,MentalPregnancyChance.probability(10,0,false));
        // Guaranteed fragment floors: ordinary >= 10, void >= 32, uncapped rewards keep their value.
        if(MentalPregnancyChance.fragmentReward(1,false)!=10)throw new AssertionError("ordinary floor");
        if(MentalPregnancyChance.fragmentReward(9,true)!=32)throw new AssertionError("void floor");
        if(MentalPregnancyChance.fragmentReward(23,false)!=23)throw new AssertionError("ordinary weight above floor");
        if(MentalPregnancyChance.fragmentReward(97,true)!=97)throw new AssertionError("void weight above floor");
        if(MentalPregnancyChance.fragmentReward(Integer.MAX_VALUE,true)!=Integer.MAX_VALUE)throw new AssertionError("unlimited void stays uncapped");
        // Rapid reopening: first opening 0, second inside the window 1 (+20%), slow gap resets.
        equal(0,MentalPregnancyChance.rapidChain(0,10_000L));
        equal(1,MentalPregnancyChance.rapidChain(0,5_000L));
        equal(2,MentalPregnancyChance.rapidChain(1,1L));
        equal(0,MentalPregnancyChance.rapidChain(4,6_000L));
        equal(MentalPregnancyChance.RAPID_CHAIN_CAP,MentalPregnancyChance.rapidChain(99,1L));
        equal(.2,MentalPregnancyChance.escalate(0,1));
        equal(.32,MentalPregnancyChance.escalate(.12,1));
        equal(1.0,MentalPregnancyChance.escalate(.9,3));
        equal(.52,MentalPregnancyChance.probability(10,0,false,1));
        equal(1.0,MentalPregnancyChance.probability(0,0,true,3));
        System.out.println("Mental pregnancy chance regression passed");
    }
}
