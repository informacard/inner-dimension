package com.innerdimension.progression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.innerdimension.state.InnerState;
import net.minecraft.*;
import java.util.*;
public final class DomainState extends class_18 {
    public final Map<UUID,String> voidRooms=new HashMap<>();
    public final Set<UUID> voidGestations=new HashSet<>();
    public final Map<UUID,String> names=new HashMap<>();
    public final Set<UUID> introduced=new HashSet<>();
    public final Map<UUID,Integer> mentalChanceSteps=new HashMap<>();
    public final Map<UUID,Integer> mentalDiscoveryAttempts=new HashMap<>();
    public final Map<UUID,List<String>> mentalPregnancies=new HashMap<>();
    public final Map<UUID,List<String>> mentalRewards=new HashMap<>();
    private final Map<UUID,Set<String>> collectedSpecies=new HashMap<>();
    private final Map<UUID,Integer> capacitySettlements=new HashMap<>();
    private final Map<UUID,List<String>> objectSelections=new HashMap<>();
    private final Map<UUID,Integer> selectedSlots=new HashMap<>();
    private final Map<UUID,List<String>> slotNames=new HashMap<>();
    private final Map<UUID,List<List<String>>> engravedLoadouts=new HashMap<>();
    private final Map<UUID,List<String>> floorPatterns=new HashMap<>();
    public DomainState() { }
    private DomainState(Map<UUID,String> n,List<UUID> i,Map<UUID,List<String>> species,Map<UUID,Integer> selected,Map<UUID,List<String>> namesBySlot,Map<UUID,List<String>> encodedLoadouts,Map<UUID,Integer> chances,Map<UUID,List<String>> pregnancies,Map<UUID,Integer> discoveryAttempts,Map<UUID,Integer> settlements,Map<UUID,List<String>> objects,int objectVersion,Map<UUID,List<String>> rewards,Map<UUID,String> voidStates,List<UUID> voidPregnancies,Map<UUID,List<String>> floors) {
        floorPatterns.putAll(floors);
        voidRooms.putAll(voidStates);voidGestations.addAll(voidPregnancies);
        mentalRewards.putAll(rewards);
        settlements.forEach((id,nth)->capacitySettlements.put(id,Math.max(0,nth)));objects.forEach((owner,values)->objectSelections.put(owner,values.stream().map(v->objectVersion<2?migrateObjectMask(v):v).map(v->objectVersion<3?migrateModeMask(v):v).toList()));
        chances.forEach((id,steps)->mentalChanceSteps.put(id,Math.max(0,Math.min(10,steps))));
        discoveryAttempts.forEach((id,count)->mentalDiscoveryAttempts.put(id,Math.max(0,Math.min(50,count))));
        mentalPregnancies.putAll(pregnancies);
        names.putAll(n); introduced.addAll(i);
        species.forEach((owner, ids) -> {
            Set<String> valid=new HashSet<>();
            for(String id:ids) if(class_2960.method_60654(id)!=null) valid.add(id);
            if(!valid.isEmpty()) collectedSpecies.put(owner,valid);
        });
        selected.forEach((owner,slot)->selectedSlots.put(owner,clampSlot(slot)));
        namesBySlot.forEach((owner,names)->slotNames.put(owner,normalizeNames(names)));
        encodedLoadouts.forEach((owner,slots)->engravedLoadouts.put(owner,decodeLoadouts(slots)));
    }
    private Map<UUID,List<String>> encodedSpecies() {
        Map<UUID,List<String>> result=new TreeMap<>(Comparator.comparing(UUID::toString));
        collectedSpecies.forEach((owner,ids) -> result.put(owner,ids.stream().sorted().toList()));
        return result;
    }
    private Map<UUID,List<String>> encodedLoadouts(){Map<UUID,List<String>> out=new HashMap<>();engravedLoadouts.forEach((owner,slots)->out.put(owner,slots.stream().map(s->String.join("\u001f",s)).toList()));return out;}
    public static final Codec<DomainState> CODEC=RecordCodecBuilder.create(i -> i.group(
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING).optionalFieldOf("names",Map.of()).forGetter(s -> s.names),
        InnerState.UUID_CODEC.listOf().optionalFieldOf("introduced",List.of()).forGetter(s -> List.copyOf(s.introduced)),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("collected_species",Map.of()).forGetter(DomainState::encodedSpecies),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.INT).optionalFieldOf("selected_slots",Map.of()).forGetter(s->s.selectedSlots),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("slot_names",Map.of()).forGetter(s->s.slotNames),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("engraved_loadouts",Map.of()).forGetter(DomainState::encodedLoadouts),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.INT).optionalFieldOf("mental_chance_steps",Map.of()).forGetter(s->s.mentalChanceSteps),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("mental_pregnancies",Map.of()).forGetter(s->s.mentalPregnancies),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.INT).optionalFieldOf("mental_discovery_attempts",Map.of()).forGetter(s->s.mentalDiscoveryAttempts),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.INT).optionalFieldOf("capacity_settlements",Map.of()).forGetter(s->s.capacitySettlements),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("object_selections",Map.of()).forGetter(s->s.objectSelections),
        Codec.INT.optionalFieldOf("object_selection_version",1).forGetter(s->3),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("mental_rewards",Map.of()).forGetter(s->s.mentalRewards),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING).optionalFieldOf("void_rooms",Map.of()).forGetter(s->s.voidRooms),
        InnerState.UUID_CODEC.listOf().optionalFieldOf("void_gestations",List.of()).forGetter(s->List.copyOf(s.voidGestations)),
        Codec.unboundedMap(InnerState.UUID_CODEC,Codec.STRING.listOf()).optionalFieldOf("floor_patterns",Map.of()).forGetter(s->s.floorPatterns)
    ).apply(i,DomainState::new));
    public static final class_10741<DomainState> TYPE=new class_10741<>("inner_dimension_domains",DomainState::new,CODEC,class_4284.field_19212);
    public static DomainState get(class_3218 world) { return world.method_8503().method_30002().method_17983().method_17924(TYPE); }
    /** The domain's own name (from the naming ritual); the crest slot name is a separate value. */
    public String rawName(UUID owner){return names.getOrDefault(owner,"");}
    public class_2561 name(UUID owner) { String n=rawName(owner); return n.isBlank() ? class_2561.method_43471("inner_dimension.domain.default_name") : class_2561.method_43470(n); }
    public void introduce(UUID owner) { if(introduced.add(owner)) method_80(); }
    public void rename(UUID owner,String name) { names.put(owner,name);method_80(); }
    public boolean addSpecies(UUID owner,String id) {
        Set<String> existing=collectedSpecies.get(owner);
        if(existing!=null && existing.contains(id)) return false;
        collectedSpecies.computeIfAbsent(owner,k -> new HashSet<>()).add(id);
        method_80();
        return true;
    }
    public void removeSpecies(UUID owner,String id){var values=collectedSpecies.get(owner);if(values!=null&&values.remove(id))method_80();}
    public boolean hasSpecies(UUID owner,String id) {
        Set<String> existing=collectedSpecies.get(owner);
        return existing!=null && (existing.contains(id)||((id.equals(com.innerdimension.domain.DomainModes.SLIME_LARGE)||id.equals(com.innerdimension.domain.DomainModes.SLIME_SMALL))&&existing.contains("minecraft:slime")));
    }
    public List<String> species(UUID owner) {
        Set<String> existing=collectedSpecies.get(owner);
        if(existing==null)return List.of();
        Set<String> available=new HashSet<>(existing);
        if(existing.contains("minecraft:slime")){available.add(com.innerdimension.domain.DomainModes.SLIME_LARGE);available.add(com.innerdimension.domain.DomainModes.SLIME_SMALL);}
        return available.stream().sorted().toList();
    }
    public int selectedSlot(UUID owner){return selectedSlots.getOrDefault(owner,0);}
    public void selectSlot(UUID owner,int slot){selectedSlots.put(owner,clampSlot(slot));method_80();}
    public String slotName(UUID owner,int slot){String name=slotNames.getOrDefault(owner,Collections.nCopies(7,"")).get(clampSlot(slot));return slot==6&&name.isEmpty()?"虚空":name;}
    public void renameSlot(UUID owner,int slot,String value){List<String> names=new ArrayList<>(slotNames.getOrDefault(owner,Collections.nCopies(7,"")));names.set(clampSlot(slot),cleanName(value));slotNames.put(owner,List.copyOf(names));method_80();}
    public List<String> loadout(UUID owner,int slot){List<List<String>> slots=engravedLoadouts.get(owner);return slots==null?List.of():slots.get(clampSlot(slot));}
    public void engrave(UUID owner,int slot,Collection<String> ids){List<List<String>> slots=new ArrayList<>(engravedLoadouts.getOrDefault(owner,Collections.nCopies(7,List.of())));slots.set(clampSlot(slot),com.innerdimension.domain.DomainAbilityCatalog.sanitize(ids));engravedLoadouts.put(owner,List.copyOf(slots));method_80();}
    private static String migrateObjectMask(String value){int split=value.lastIndexOf('|');if(split<0)return value;try{int mask=Integer.parseInt(value.substring(split+1))&31;if((mask&1)!=0)mask|=32;return value.substring(0,split+1)+mask;}catch(NumberFormatException ignored){return value;}}
    private static String migrateModeMask(String value){return com.innerdimension.domain.DomainModes.migrateLegacySelection(value);}
    public int capacity(UUID owner){return DomainCapacityRules.capacity(capacitySettlements.getOrDefault(owner,0));}
    public void settleCapacity(class_3222 player){if(!InnerAdvancements.done(player,"overload")||!InnerAdvancements.done(player,"mental_seeding"))return;UUID id=player.method_5667();int earned=DomainCapacityRules.settle(capacitySettlements.getOrDefault(id,0),(int)species(id).stream().filter(v->!v.equals(com.innerdimension.domain.DomainModes.SLIME_LARGE)&&!v.equals(com.innerdimension.domain.DomainModes.SLIME_SMALL)&&!v.equals(com.innerdimension.domain.DomainModes.SUCCUBUS)&&!v.equals(com.innerdimension.domain.DomainModes.SHRINE)&&!v.equals(com.innerdimension.domain.DomainContest.SIMPLE)&&!v.equals(com.innerdimension.domain.UnlimitedVoidRules.ID)&&!v.equals(com.innerdimension.voiddomain.VoidContamination.ABILITY)).count(),true,true);if(earned>capacitySettlements.getOrDefault(id,0)){capacitySettlements.put(id,earned);InnerAdvancements.grant(player,"gaping");method_80();}}
    public List<String> objectSelections(UUID owner){return List.copyOf(objectSelections.getOrDefault(owner,List.of()));}
    public int objectMask(UUID owner,int slot,String entity){String key=clampSlot(slot)+"|"+entity+"|";for(String v:objectSelections(owner))if(v.startsWith(key))try{return Integer.parseInt(v.substring(key.length()))&63;}catch(NumberFormatException ignored){}return -1;}
    public void objectMask(UUID owner,int slot,String entity,int mask){String key=clampSlot(slot)+"|"+entity+"|";List<String> values=new ArrayList<>(objectSelections(owner));values.removeIf(v->v.startsWith(key));values.add(key+(mask&63));objectSelections.put(owner,List.copyOf(values));method_80();}
    public com.innerdimension.projection.FloorPatternSettings floorPattern(UUID owner) {
        var v=floorPatterns.get(owner);
        if(v!=null&&v.size()==5)try {
            return new com.innerdimension.projection.FloorPatternSettings(com.innerdimension.projection.FloorPatternSettings.Mode.valueOf(v.get(0)),v.get(1),Boolean.parseBoolean(v.get(2)),v.get(3),v.get(4));
        } catch(IllegalArgumentException ignored) { }
        return com.innerdimension.projection.FloorPatternSettings.DEFAULT;
    }
    public void floorPattern(UUID owner,com.innerdimension.projection.FloorPatternSettings settings) {
        floorPatterns.put(owner,List.of(settings.mode().name(),settings.expression(),Boolean.toString(settings.preserveCenter()),settings.foreground(),settings.background()));method_80();
    }
    private static int clampSlot(int slot){return Math.max(0,Math.min(6,slot));}
    private static String cleanName(String value){if(value==null)return "";String v=value.strip();return v.length()>48?v.substring(0,48):v;}
    private static List<String> normalizeNames(List<String> input){List<String> out=new ArrayList<>(Collections.nCopies(7,""));for(int i=0;i<Math.min(7,input.size());i++)out.set(i,cleanName(input.get(i)));return List.copyOf(out);}
    private static List<List<String>> decodeLoadouts(List<String> input){List<List<String>> out=new ArrayList<>(Collections.nCopies(7,List.of()));for(int i=0;i<Math.min(7,input.size());i++){String s=input.get(i);out.set(i,com.innerdimension.domain.DomainAbilityCatalog.sanitize(s.isEmpty()?List.of():Arrays.asList(s.split("\u001f",-1))));}return List.copyOf(out);}
}
