import com.innerdimension.progression.DomainState;
import com.innerdimension.domain.DomainAbilityCatalog;
import com.mojang.serialization.JsonOps;
import com.google.gson.*;
import java.util.*;
import net.minecraft.*;
public final class VoidProgressionRegression {
    private static void check(boolean pass,String message){if(!pass)throw new AssertionError(message);}
    public static void main(String[] args){
        class_155.method_36208();class_2966.method_12851();
        UUID owner=UUID.randomUUID();JsonObject legacy=new JsonObject();JsonObject loads=new JsonObject();JsonArray six=new JsonArray();
        six.add("minecraft:chicken");for(int i=1;i<6;i++)six.add("");loads.add(owner.toString(),six);legacy.add("engraved_loadouts",loads);
        DomainState migrated=DomainState.CODEC.parse(JsonOps.INSTANCE,legacy).getOrThrow();
        check(migrated.loadout(owner,0).equals(List.of("minecraft:chicken")),"preserve old engraved slot");
        check(migrated.loadout(owner,6).isEmpty(),"seventh slot defaults empty in old saves");
        check(migrated.voidRooms.isEmpty()&&migrated.voidGestations.isEmpty(),"old worlds remain uncontaminated");
        migrated.engrave(owner,6,List.of("inner_dimension:void"));migrated.selectSlot(owner,6);migrated.renameSlot(owner,6,"void design");
        migrated.voidRooms.put(owner,"pending:1:43");migrated.voidGestations.add(owner);migrated.mentalRewards.put(owner,List.of("void:12"));
        JsonElement encoded=DomainState.CODEC.encodeStart(JsonOps.INSTANCE,migrated).getOrThrow();DomainState restored=DomainState.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        check(restored.selectedSlot(owner)==6&&restored.slotName(owner,6).equals("void design"),"selected seventh slot survives reload");
        check(restored.loadout(owner,6).equals(List.of("inner_dimension:void")),"void engraving survives reload");
        check(restored.voidRooms.get(owner).equals("pending:1:43")&&restored.voidGestations.contains(owner),"wave and void gestation survive reload");
        check(restored.mentalRewards.get(owner).equals(List.of("void:12")),"void reward retains type across reload");
        check(DomainAbilityCatalog.weight(List.of("inner_dimension:void"))==Integer.MAX_VALUE,"infinite capacity metadata");
        boolean blocked=false;try{DomainAbilityCatalog.sanitize(List.of("inner_dimension:void","minecraft:chicken"));}catch(IllegalArgumentException expected){blocked=true;}
        check(blocked,"infinite ability excludes every other engraving");
        restored.engrave(owner,6,List.of());check(restored.loadout(owner,6).isEmpty(),"void ability can be removed normally");
        check(DomainAbilityCatalog.byEntity("inner_dimension:void").color()==0&&DomainAbilityCatalog.byEntity("inner_dimension:void").colorWeight()==10,"black color with weight 10");
        System.out.println("Void progression regression passed");
    }
}
