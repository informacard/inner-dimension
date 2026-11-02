package com.innerdimension.progression;
import com.innerdimension.state.InnerState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.*;
import java.util.*;

/** Pending proximity-triggered visits survive saving/restarting the server. */
public final class VillagerRitualState extends class_18 {
    public record Visit(UUID villager,String world,UUID port,double x,double y,double z,long due,boolean noAi,long returnAt) {
        public static final Codec<Visit> CODEC=RecordCodecBuilder.create(i->i.group(
            InnerState.UUID_CODEC.fieldOf("villager").forGetter(Visit::villager),
            Codec.STRING.fieldOf("world").forGetter(Visit::world),
            InnerState.UUID_CODEC.fieldOf("port").forGetter(Visit::port),
            Codec.DOUBLE.fieldOf("x").forGetter(Visit::x),Codec.DOUBLE.fieldOf("y").forGetter(Visit::y),Codec.DOUBLE.fieldOf("z").forGetter(Visit::z),
            Codec.LONG.fieldOf("due").forGetter(Visit::due),Codec.BOOL.fieldOf("no_ai").forGetter(Visit::noAi),
            Codec.LONG.optionalFieldOf("return_at",-1L).forGetter(Visit::returnAt)
        ).apply(i,Visit::new));
    }
    public final Map<UUID,Visit> visits=new LinkedHashMap<>();
    public VillagerRitualState(){}
    private VillagerRitualState(List<Visit> saved){saved.forEach(v->visits.put(v.villager(),v));}
    public static final Codec<VillagerRitualState> CODEC=Visit.CODEC.listOf().xmap(VillagerRitualState::new,s->List.copyOf(s.visits.values()));
    public static final class_10741<VillagerRitualState> TYPE=new class_10741<>("inner_dimension_villager_visits",VillagerRitualState::new,CODEC,class_4284.field_19212);
    public static VillagerRitualState get(net.minecraft.server.MinecraftServer server){return server.method_30002().method_17983().method_17924(TYPE);}
    public void put(Visit visit){visits.put(visit.villager(),visit);method_80();}
    public void remove(UUID id){if(visits.remove(id)!=null)method_80();}
    /** MCA creates a new entity UUID; keep the original return and AI restoration data. */
    public boolean replaceVillager(UUID previous,UUID replacement){
        if(previous.equals(replacement)||visits.containsKey(replacement))return false;
        Visit visit=visits.remove(previous);
        if(visit==null)return false;
        put(new Visit(replacement,visit.world(),visit.port(),visit.x(),visit.y(),visit.z(),visit.due(),visit.noAi(),visit.returnAt()));
        return true;
    }
}
