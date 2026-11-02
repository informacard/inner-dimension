package com.innerdimension.voiddomain;
import com.mojang.serialization.Codec;
import com.innerdimension.state.InnerState;
import net.minecraft.*;
import java.util.*;
public final class VoidEmbodimentState extends class_18 {
 public final Map<UUID,Integer> players=new HashMap<>();
 public VoidEmbodimentState(){}
 private VoidEmbodimentState(Map<UUID,Integer> saved){players.putAll(saved);}
 public static final Codec<VoidEmbodimentState> CODEC=Codec.unboundedMap(InnerState.UUID_CODEC,Codec.INT).xmap(VoidEmbodimentState::new,s->s.players);
 public static final class_10741<VoidEmbodimentState> TYPE=new class_10741<>("inner_dimension_void_embodiment",VoidEmbodimentState::new,CODEC,class_4284.field_19212);
 public static VoidEmbodimentState get(class_3222 p){return p.method_51469().method_8503().method_30002().method_17983().method_17924(TYPE);}
 public static VoidEmbodimentState get(class_3218 world){return world.method_8503().method_30002().method_17983().method_17924(TYPE);}
 public void put(UUID id,int value){players.put(id,value);method_80();}
}
