import com.innerdimension.progression.DomainState;
import com.innerdimension.domain.DomainAbilityCatalog;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonObject;
import java.util.*;
import net.minecraft.*;
public final class MentalPregnancyRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        class_155.method_36208();class_2966.method_12851();
        check(DomainAbilityCatalog.weight(List.of("minecraft:warden","minecraft:cat"))==9,"warden + cat reward");
        check(com.innerdimension.pregnancy.MentalPregnancyChance.fragmentReward(9,false)==10,"ordinary mental pregnancy pays at least 10 fragments");
        check(com.innerdimension.pregnancy.MentalPregnancyChance.fragmentReward(9,true)==32,"void mental pregnancy pays at least 32 fragments");
        check(com.innerdimension.pregnancy.MentalPregnancyChance.fragmentReward(600,true)==600,"heavy loadouts keep their larger payout");
        check(DomainAbilityCatalog.weight(Collections.nCopies(100,"minecraft:warden"))==600,"no 64 reward cap");
        DomainState old=DomainState.CODEC.parse(JsonOps.INSTANCE,new JsonObject()).getOrThrow();
        check(old.mentalRewards.isEmpty(),"old saves default pending rewards");
        UUID owner=UUID.randomUUID();old.mentalPregnancies.put(owner,List.of("minecraft:warden","minecraft:cat"));
        old.mentalRewards.put(owner,List.of("9","600","fetus_spawned"));
        var encoded=DomainState.CODEC.encodeStart(JsonOps.INSTANCE,old).getOrThrow();
        DomainState restored=DomainState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        check(restored.mentalRewards.get(owner).equals(List.of("9","600","fetus_spawned")),"pending rewards and fetus lifecycle survive restart");
        check(restored.mentalPregnancies.get(owner).equals(old.mentalPregnancies.get(owner)),"all conceived species survive restart");
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var fetus=(com.innerdimension.entity.MentalFetusEntity)unsafe.allocateInstance(com.innerdimension.entity.MentalFetusEntity.class);
        fetus.method_5857(new class_238(0,0,0,1,1,1));
        class_1924 view=(class_1924)java.lang.reflect.Proxy.newProxyInstance(class_1924.class.getClassLoader(),new Class[]{class_1924.class},(proxy,method,values)->{
            if(method.isDefault())return java.lang.reflect.InvocationHandler.invokeDefault(proxy,method,values);
            if(method.getName().equals("method_8333"))return List.of(fetus);
            throw new AssertionError(method.toString());
        });
        var block=class_259.method_1078(new class_238(0,0,0,1,1,1));
        fetus.field_23807=true;
        check(!view.method_8611(null,block),"reproduces living entity block placement obstruction");
        fetus.method_5773();
        check(view.method_8611(null,block),"real vanilla block placement allows overlap with fetus");
        check(!fetus.method_5863()&&!fetus.method_5810(),"fetus is not targetable or pushable");
        check(!fetus.method_64397(null,null,1000000f)&&!fetus.method_31481(),"attacks cannot remove fetus");
        var dispatcher=new com.mojang.brigadier.CommandDispatcher<class_2168>();
        com.innerdimension.command.InnerDimensionCommand.register(dispatcher);
        for(String alias:List.of("innerdim","innerdimension"))check(dispatcher.getRoot().getChild(alias).getChild("debug").getChild("pregnancy").getCommand()!=null,"pregnancy command registered under "+alias);
        System.out.println("PASS fetus interaction: placement obstruction reproduced and fixed, targeting and attack removal disabled");
        System.out.println("PASS mental pregnancy: weights, uncapped totals, old save migration, pending reward persistence");
    }
}
