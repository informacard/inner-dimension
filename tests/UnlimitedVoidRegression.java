import com.innerdimension.domain.*;
import com.innerdimension.domain.ability.*;
import com.innerdimension.domain.runtime.*;
import net.minecraft.*;
import java.util.*;
import java.lang.reflect.*;

public final class UnlimitedVoidRegression {
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
    static class Probe extends class_1309 {
        class_1937 world;UUID id;boolean alive=true;
        Probe(){super(null,null);}
        public class_1306 method_6068(){return null;}
        public class_1937 method_73183(){return world;}
        public UUID method_5667(){return id;}
        public boolean method_5805(){return alive;}
        public boolean method_7325(){return false;}
        public void method_6075(){}
    }
    public static void run()throws Exception {
        check(UnlimitedVoidRules.multiplier(6,0,200)==1,"baseline");
        check(UnlimitedVoidRules.multiplier(30,200,200)==.5,"maximum resistance");
        check(UnlimitedVoidRules.multiplier(Integer.MAX_VALUE,99999,200)==.5,"clamped resistance");
        check(UnlimitedVoidRules.multiplier(6,200,200)<UnlimitedVoidRules.multiplier(30,0,200),"desire matters more than capacity");
        double debt=0;for(int i=0;i<2;i++)debt=UnlimitedVoidRules.debt(debt,true,1);
        check(debt==19,"two exposed ticks = 20 budget ticks including elapsed tick");
        for(int i=0;i<19;i++)debt=UnlimitedVoidRules.debt(debt,false,1);
        check(debt==0,"residual control expires without a domain");
        var a=DomainAbilityCatalog.byEntity(UnlimitedVoidRules.ID);check(a.loadWeight()==Integer.MAX_VALUE&&a.code().equals("D79"),"ability metadata");
        boolean conflict=false;try{DomainAbilityCatalog.sanitize(List.of(UnlimitedVoidRules.ID,"minecraft:pig"));}catch(IllegalArgumentException expected){conflict=true;}check(conflict,"unlimited void is exclusive");
        check(DomainAbilityCatalog.weight(List.of(UnlimitedVoidRules.ID,"minecraft:pig"))==Integer.MAX_VALUE,"infinite draft cannot overflow");
        DomainAbilities.registerAll();check(DomainAbilities.byCode("D79")==D79.INSTANCE,"ability dispatch");
        check(DomainObjects.configurable("D71")&&DomainObjects.defaultMask("D71")==DomainObjects.ALL_CREATURES,"legacy void scope stays all, now configurable");
        check(DomainObjects.defaultMask("D79")==DomainObjects.HOSTILE,"safe default excludes caster");
        check(DomainAbilityCatalog.weight(List.of("inner_dimension:void"))==Integer.MAX_VALUE,"void still infinite");
        Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);var unsafe=(sun.misc.Unsafe)uf.get(null);
        var world=(class_3218)unsafe.allocateInstance(class_3218.class);
        Probe e=(Probe)unsafe.allocateInstance(Probe.class);e.world=world;e.id=UUID.randomUUID();e.alive=true;
        for(var f:class_1297.class.getDeclaredFields())if(f.getType()==class_243.class&&!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(e,class_243.field_1353);}
        UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(1);check(UnlimitedVoidControl.locked(e),"control starts");
        UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(2);
        for(int tick=3;tick<21;tick++){UnlimitedVoidControl.tick(tick);check(UnlimitedVoidControl.locked(e),"control survives domain closure until debt expires");}
        UnlimitedVoidControl.tick(21);check(!UnlimitedVoidControl.locked(e),"residual release");
        UnlimitedVoidControl.clear();
        for(int tick=1;tick<=300;tick++){UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(tick);check(UnlimitedVoidControl.locked(e),"mob controlled within cap");}
        UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(301);check(!UnlimitedVoidControl.locked(e),"15 second absolute cap");
        UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(302);check(!UnlimitedVoidControl.locked(e),"continuous exposure cannot restart cap");
        UnlimitedVoidControl.tick(303);UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(304);check(UnlimitedVoidControl.locked(e),"mob can be affected after leaving and reentering");
        e.alive=false;UnlimitedVoidControl.tick(305);check(!UnlimitedVoidControl.locked(e),"death clears control");
        UnlimitedVoidControl.clear();
        e.alive=true;
        var immunityField=UnlimitedVoidControl.class.getDeclaredField("IMMUNE");immunityField.setAccessible(true);
        @SuppressWarnings("unchecked") var immunity=(Map<UUID,Long>)immunityField.get(null);
        immunity.put(e.id,1201L);
        for(int tick=1;tick<1201;tick++){UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(tick);check(!UnlimitedVoidControl.locked(e),"shared immunity blocks every exposure for 60 seconds");}
        UnlimitedVoidControl.expose(e);UnlimitedVoidControl.tick(1201);check(UnlimitedVoidControl.locked(e),"immunity expiry allows a fresh episode");
        UnlimitedVoidControl.clear();
        System.out.println("UNLIMITED_VOID_REGRESSION_PASS: ratios, clamps, residual debt, mob cap, continuous exposure latch, death, dispatch and infinite void");
    }
    public static void main(String[] args)throws Exception{class_155.method_36208();class_2966.method_12851();run();}
}
