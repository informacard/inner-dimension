import com.innerdimension.domain.*;
import com.innerdimension.domain.ability.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.progression.DomainState;
import com.mojang.serialization.JsonOps;
import com.nonid.GenderHolder;
import net.minecraft.*;
import java.util.*;
import java.lang.reflect.*;
import sun.misc.Unsafe;

public final class SlimeAbilityRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static class ProbeMob extends class_1308 implements GenderHolder {
        class_1937 world;Set<String> tags;int gender;
        ProbeMob(){super(null,null);}
        public class_1937 method_73183(){return world;}
        public Set<String> method_5752(){return tags;}
        public boolean method_5780(String tag){return tags.add(tag);}
        public boolean method_5738(String tag){return tags.remove(tag);}
        public int getGenderMask(){return gender;}
        public void setGenderMask(int value){gender=value;}
    }
    public static void main(String[] args)throws Exception {
        DomainState state=new DomainState();UUID owner=UUID.randomUUID();
        state.addSpecies(owner,"minecraft:slime");
        check(new HashSet<>(state.species(owner)).equals(Set.of("minecraft:slime",DomainModes.SLIME_LARGE,DomainModes.SLIME_SMALL)),"One recorded slime supplies three abilities");
        state.engrave(owner,0,state.species(owner));
        state.objectMask(owner,0,DomainModes.SLIME_LARGE,DomainObjects.NEUTRAL);
        var json=DomainState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var restored=DomainState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();
        check(restored.species(owner).size()==3&&restored.loadout(owner,0).size()==3,"Existing slime save and engraved IDs survive codec roundtrip");
        check(restored.objectMask(owner,0,DomainModes.SLIME_LARGE)==DomainObjects.NEUTRAL,"Independent object selection persists");
        check(restored.hasSpecies(owner,DomainModes.SLIME_SMALL),"Derived ownership recognized");
        check(!DomainModes.owned(List.of("minecraft:goat"),DomainModes.SUCCUBUS)&&!DomainModes.owned(List.of("minecraft:vindicator"),DomainModes.SHRINE),"No legacy source authorization");
        check(DomainModes.owned(List.of(DomainModes.SHRINE),DomainModes.SHRINE),"Explicit independent ownership");
        check(DomainAbilityCatalog.sanitize(List.of("minecraft:goat",DomainModes.SUCCUBUS,"minecraft:vindicator",DomainModes.SHRINE)).size()==4,"Old exclusive pairs can coexist");
        check(DomainObjects.defaultMask("D77")==DomainObjects.HOSTILE&&DomainObjects.defaultMask("D78")==DomainObjects.ALL_CREATURES,"Requested target defaults");
        DomainAbilities.registerAll();check(DomainAbilities.byCode("D77")==D77.INSTANCE&&DomainAbilities.byCode("D78")==D78.INSTANCE,"Executable handler registration");
        for(String id:restored.species(owner)){var a=DomainAbilityCatalog.byEntity(id);check(a.loadWeight()<9&&a.colorWeight()<9,"Slime balance below nine");}
        Field field=Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);Unsafe unsafe=(Unsafe)field.get(null);
        ProbeMob mob=(ProbeMob)unsafe.allocateInstance(ProbeMob.class);
        mob.world=(class_3218)unsafe.allocateInstance(class_3218.class);mob.tags=new HashSet<>(Set.of("gender.male","unrelated"));mob.gender=1;
        Method acquire=D78.class.getDeclaredMethod("acquire",class_1309.class,UUID.class);acquire.setAccessible(true);
        Method release=D78.class.getDeclaredMethod("release",class_1309.class,UUID.class);release.setAccessible(true);
        UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        acquire.invoke(null,mob,a);acquire.invoke(null,mob,b);
        check(mob.gender==3&&mob.tags.containsAll(Set.of("gender.male","gender.female")),"NoN holder and both tags change together");
        D78.restoreOrphan(mob);check(mob.gender==3,"Live effect is never repaired as orphan");
        release.invoke(null,mob,a);check(mob.gender==3,"First overlapping session cannot prematurely restore");
        release.invoke(null,mob,b);check(mob.gender==1&&mob.tags.equals(Set.of("gender.male","unrelated")),"Last session restores exact original gender and tags");
        mob.gender=3;mob.tags.add("gender.female");mob.tags.add("inner_dimension:slime_gender:1:1");
        D78.restoreOrphan(mob);check(mob.gender==1&&mob.tags.equals(Set.of("gender.male","unrelated")),"Restart orphan restores saved original");
        mob.gender=0;mob.tags.clear();acquire.invoke(null,mob,a);D78.releaseAll(mob);
        check(mob.gender==0&&mob.tags.isEmpty(),"Unset gender and no tags preserved on death cleanup");
        @SuppressWarnings("unchecked") var slimeType=(class_1299<class_1621>)(class_1299<?>)class_7923.field_41177.method_10223(class_2960.method_60655("minecraft","slime")).orElseThrow().comp_349();
        class_1621 slime=new class_1621(slimeType,mob.world);slime.field_5960=true;
        Method resize=D77.class.getDeclaredMethod("resize",class_1621.class,class_1309.class);resize.setAccessible(true);
        for(var box:List.of(new class_238(10,20,30,10.6,21.8,30.6),new class_238(-2,-3,-4,2,1,4),new class_238(0,0,0,.2,.3,.2),new class_238(0,0,0,90,120,75))){
            mob.method_5857(box);resize.invoke(null,slime,mob);class_238 shell=slime.method_5829();
            check(shell.field_1323<=box.field_1323&&shell.field_1320>=box.field_1320&&shell.field_1322<=box.field_1322&&shell.field_1325>=box.field_1325&&shell.field_1321<=box.field_1321&&shell.field_1324>=box.field_1324,"Actual slime encloses full target box: "+box);
        }
        System.out.println("PASS: slime source expansion, old save codec, independent abilities, object masks, executable handlers, overlapping NoN gender leases, orphan and death restoration");
    }
}
