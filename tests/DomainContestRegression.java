import com.innerdimension.domain.DomainContestRules;
import com.innerdimension.domain.DomainAbilityCatalog;
import com.innerdimension.network.DomainContestPayload;
import com.innerdimension.progression.VillagerRitualState;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.class_9129;
import java.util.*;

/** Tests numeric boundaries, fractional damage accounting, wire state and saved return jobs. */
public final class DomainContestRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        net.minecraft.class_155.method_36208();net.minecraft.class_2966.method_12851();
        DebugDomainRegression.main(new String[0]);
        check(DomainContestRules.clashTicks(5,7)==120,"Average strength is seconds, not half of the average");
        check(DomainContestRules.average(6,7)==6.5,"Open/open lifetime runs at 6.5x");
        check(DomainContestRules.penalized(7)==4,"Mixed late defender loses exactly three strength");
        check(DomainContestRules.penalized(2)==0,"Strength remains within existing zero-to-ten range");
        check(DomainContestRules.clashTicks(6,DomainContestRules.penalized(7))==100,"Forced closed defender uses penalized strength for contest duration");
        check(DomainContestRules.result(6,DomainContestRules.penalized(7),.5)==1,"Forced closed defender uses penalized strength for winner");
        double mean=DomainContestRules.average(6,DomainContestRules.penalized(7));
        check(mean==5,"Mixed timing uses corrected strengths");
        check(DomainContestRules.shortenClosed(600,mean)==440,"Closed remaining loses average five seconds plus fixed three seconds");
        check(DomainContestRules.accelerateOpen(600,mean)==120,"Open remaining divides by corrected average");
        check(DomainContestRules.accelerateOpen(600,mean)*mean==600,"Open winner inverse restores lifetime");
        check(DomainContestRules.shortenClosed(600,mean)+mean*20==540,"Closed winner restores contest adjustment but retains three-second penalty");
        check(DomainContestRules.shortenClosed(40,mean)==0,"Short remaining duration clamps to zero");
        check(DomainContestRules.shortenClosed(600,DomainContestRules.average(0,0))==539,"Zero strength still pays fixed three-second penalty");
        check(Double.isFinite(DomainContestRules.accelerateOpen(600,DomainContestRules.average(0,0))),"Zero-strength pair has finite duration");
        check(DomainContestRules.clashTicks(0,10)==100,"Extreme pair averages correctly");
        check(DomainContestRules.clashTicks(.1,.2)==4,"Fractional durations round up to a tick");
        check(DomainContestRules.result(5,5.49,.5)==0,"Near strength breaks both domains");
        check(DomainContestRules.result(5,5.5,.5)==-1,"Exactly the threshold is a win, not a draw");
        check(DomainContestRules.result(5.5,5,.5)==1,"Result symmetry");
        check(DomainContestRules.result(5,5,0)==0,"Exact tie cannot produce a winner");
        check(DomainContestRules.result(5,5.2,.1)==-1,"Configurable threshold");
        check(DomainContestRules.simpleTicks(5)==550,"Villager strength starts shield at 27.5 seconds");
        check(DomainContestRules.simpleTicks(10)==500,"Maximum strength starts shield at 25 seconds");
        double left=DomainContestRules.simpleTicks(5);
        for(int i=0;i<40;i++)left-=DomainContestRules.damageTicks(.25);
        check(left==530,"Repeated fractional hits preserve 10 damage = one second");
        var a=DomainAbilityCatalog.byEntity("inner_dimension:self");
        check(a!=null&&a.loadWeight()==12&&a.colorWeight()==10&&a.color()==0xffffff,"Simple domain metadata");
        var owner=UUID.randomUUID();
        for(var payload:List.of(new DomainContestPayload(owner,20,550,100),new DomainContestPayload(null,0,0,0))){
            var buf=new class_9129(Unpooled.buffer(),null);
            try{DomainContestPayload.CODEC.encode(buf,payload);check(DomainContestPayload.CODEC.decode(buf).equals(payload),"Nullable warning and countdowns round-trip");}finally{buf.release();}
        }
        check(net.minecraft.class_7923.field_41177.method_63535(net.minecraft.class_2960.method_12829("minecraft:iron_golem"))==net.minecraft.class_1299.field_6147,"Guardian registry must use IronGolemEntity class_1439");
        // Return searches must initialize without an attached projection entity/world.
        var geometryType=Class.forName("com.innerdimension.projection.ProjectionSafety$Geometry");
        var constructor=geometryType.getDeclaredConstructor();constructor.setAccessible(true);
        var emptyGeometry=constructor.newInstance();
        var collision=geometryType.getDeclaredMethod("collides",com.innerdimension.projection.ProjectionSafetySearch.Box.class);collision.setAccessible(true);
        check(!(boolean)collision.invoke(emptyGeometry,new com.innerdimension.projection.ProjectionSafetySearch.Box(0,0,0,1,2,1)),"Real-world return geometry initializes without a bound projection and has no projected blocks");
        var state=new VillagerRitualState();
        state.put(new VillagerRitualState.Visit(owner,"minecraft:overworld",UUID.randomUUID(),12.5,64,99.5,123456L,true,123400L));
        var saved=VillagerRitualState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var restored=VillagerRitualState.CODEC.parse(JsonOps.INSTANCE,saved).getOrThrow();
        check(restored.visits.equals(state.visits),"Restart preserves origin, port, return time and original AI");
        check(restored.visits.get(owner).returnAt()==123400L,"Restart preserves half-second proximity countdown");
        var legacy=saved.deepCopy();legacy.getAsJsonArray().get(0).getAsJsonObject().remove("return_at");
        check(VillagerRitualState.CODEC.parse(JsonOps.INSTANCE,legacy).getOrThrow().visits.get(owner).returnAt()==-1L,"Existing visits without proximity countdown remain readable");
        DomainExpansionRegression.main(new String[0]);
        ProjectionSafetyRegression.main(new String[0]);
        DomainSubtitleRegression.main(new String[0]);
        DomainLifetimeRegression.main(new String[0]);
        System.out.println("PASS: contest thresholds, duration, fractional shield damage, metadata, wire state, persistent villager return");
    }
}
