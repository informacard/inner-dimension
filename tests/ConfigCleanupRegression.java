import com.innerdimension.config.InnerConfig;
import com.innerdimension.progression.DomainCapacityRules;
import java.nio.file.*;
import java.util.Properties;
public final class ConfigCleanupRegression {
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Path dir=Path.of(args[0]);Files.createDirectories(dir.resolve("inner_dimension"));
        Path file=dir.resolve("inner_dimension/inner_dimension.properties");
        Files.writeString(file,"roomSize=55\nlightLevel=2\nenlightenmentVisual=1\nprojectionShellTicks=999\nprojectionCrestInitialScale=8\ncrestHudOffsetX=19\nopenDomainInitialRadius=40\nopenDomainInitialDurationSeconds=200\nimprintInitialCapacity=10\nimprintExpansionSpecies=4\nimprintExpansionPoints=2\n");
        InnerConfig.load(dir);var c=InnerConfig.get();
        check(c.openDomainInitialRadius==40&&c.openDomainInitialDurationSeconds==200,"Custom open domain baseline");
        check(DomainCapacityRules.capacity(0)==10&&DomainCapacityRules.capacity(3)==16,"Custom capacity and points");
        check(DomainCapacityRules.settle(0,3,true,true)==0&&DomainCapacityRules.settle(0,4,true,true)==1,"Custom species threshold");
        check(DomainCapacityRules.settle(1,4,true,true)==1,"No duplicate settlement");
        check(DomainCapacityRules.settle(0,40,false,true)==0&&DomainCapacityRules.settle(0,40,true,false)==0,"Progress gates remain");
        check(DomainCapacityRules.capacity(Integer.MAX_VALUE)==Integer.MAX_VALUE,"Capacity cannot overflow");
        Properties saved=new Properties();try(var in=Files.newInputStream(file)){saved.load(in);}
        for(String key:new String[]{"roomSize","lightLevel","enlightenmentVisual","projectionShellTicks","projectionCrestInitialScale"})check(!saved.containsKey(key),"Removed key: "+key);
        check(c.crestHudOffsetX==19&&InnerConfig.projectionShellTicks==15,"Keep HUD, fix animation defaults");
        Files.writeString(file,"imprintExpansionSpecies=0\nopenDomainInitialDurationSeconds=2147483647\nopenDomainInitialRadius=bad\n");InnerConfig.load(dir);
        check(c.imprintExpansionSpecies==1&&c.openDomainInitialDurationSeconds==Integer.MAX_VALUE/20&&c.openDomainInitialRadius==24,"Invalid input and tick overflow");
        System.out.println("ConfigCleanupRegression passed");
    }
}
