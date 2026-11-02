import com.innerdimension.domain.*;
import com.innerdimension.domain.runtime.DomainObjects;
import com.innerdimension.command.InnerDimensionCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.*;

public final class DebugDomainRegression {
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        check(DebugDomainRules.DELAY_TICKS==40,"Two-second server tick delay");
        int[] bits={DomainObjects.SELF,DomainObjects.FRIENDLY,DomainObjects.NEUTRAL,DomainObjects.HOSTILE,DomainObjects.ACTIVE,DomainObjects.PASSIVE};
        for(int n=0;n<64;n++){
            String digits=String.format("%6s",Integer.toBinaryString(n)).replace(' ','0');int expected=0;
            for(int i=0;i<6;i++)if((n&(1<<(5-i)))!=0)expected|=bits[i];
            check(DebugDomainRules.parseMask(digits)==expected,"Screen order: "+digits);
        }
        check(DebugDomainRules.parseMask("000000")==0,"Zero selects nobody");
        check(DebugDomainRules.parseMask("111111")==63,"Default selects every category including self");
        for(String invalid:new String[]{"", "00000", "0000000", "111112", "-00000", "0b1010"}){
            boolean rejected=false;try{DebugDomainRules.parseMask(invalid);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Invalid mask rejected: "+invalid);
        }
        var dispatcher=new CommandDispatcher<class_2168>();InnerDimensionCommand.register(dispatcher);
        for(String alias:new String[]{"innerdim","innerdimension"}){
            var domain=dispatcher.getRoot().getChild(alias).getChild("debug").getChild("domain");
            check(domain!=null,"Both command aliases register debug domain");
            for(String type:new String[]{"closed","open","simple"}){
                var node=domain.getChild(type);check(node!=null&&node.getCommand()!=null,"Default mask command: "+type);
                check(node.getChild("objects")!=null&&node.getChild("objects").getCommand()!=null,"Explicit six-digit mask: "+type);
            }
            check(domain.getChild("stop").getCommand()!=null,"Cleanup command");
        }
        check(SimpleDomain.Shield.class.getDeclaredConstructor(class_1309.class,double.class,Runnable.class)!=null,"Simple shield accepts NPC living owners");
        System.out.println("PASS: debug domain delay, all 64 masks, malformed masks, aliases, three types, cleanup, living-owner shield");
    }
}
