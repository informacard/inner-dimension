import com.innerdimension.progression.VillagerRitualState;
import com.mojang.serialization.JsonOps;
import java.util.UUID;

/** Run after Minecraft bootstrap so the real saved-state codec is available. */
public final class McaVillagerReplacementRegression {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        UUID oldId=UUID.randomUUID(),newId=UUID.randomUUID(),port=UUID.randomUUID();
        var state=new VillagerRitualState();
        var before=new VillagerRitualState.Visit(oldId,"minecraft:overworld",port,-31.5,96,18.5,1200,false,1010);
        state.put(before);
        check(state.replaceVillager(oldId,newId),"Replacement must transfer an existing visit");
        check(!state.visits.containsKey(oldId),"Old UUID must not remain queued");
        var expected=new VillagerRitualState.Visit(newId,before.world(),port,before.x(),before.y(),before.z(),before.due(),false,before.returnAt());
        check(expected.equals(state.visits.get(newId)),"Return location, deadline, proximity timer and original NoAI must survive");
        var encoded=VillagerRitualState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var restored=VillagerRitualState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        check(expected.equals(restored.visits.get(newId)),"Transferred visit must survive save/reload");
        check(!restored.replaceVillager(oldId,newId),"Repeated replacement must be harmless");
        check(!restored.replaceVillager(newId,newId),"Same UUID must retain its record");
        state.put(before);
        check(!state.replaceVillager(oldId,newId)&&before.equals(state.visits.get(oldId)),"Conflicting destination must not destroy either visit");
        check(!state.replaceVillager(UUID.randomUUID(),UUID.randomUUID()),"Unrelated MCA conversion must not create a visit");
        UUID third=UUID.randomUUID();
        state.put(new VillagerRitualState.Visit(oldId,"minecraft:the_nether",port,1,64,3,10,true,-1));
        check(state.replaceVillager(oldId,third)&&state.visits.get(third).noAi()&&state.visits.get(third).returnAt()==-1,"Originally disabled AI and unset proximity timer must survive");
        System.out.println("MCA_RITUAL_REGRESSION_PASS: UUID transfer, AI restoration, timers, return location, codec save/reload, conflict and unrelated conversion");
    }
}
