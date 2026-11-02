package com.innerdimension.projection;
import com.mojang.serialization.Codec;
import com.innerdimension.state.InnerState;
import net.minecraft.*;
import java.util.*;
/** Saved shell choice; a live expansion captures the choice once. */
public final class ClosedDomainBinding extends class_18 {
 private final Map<UUID,Boolean> reversed=new HashMap<>();
 public ClosedDomainBinding(){}
 private ClosedDomainBinding(Map<UUID,Boolean> values){reversed.putAll(values);}
 public static final Codec<ClosedDomainBinding> CODEC=Codec.unboundedMap(InnerState.UUID_CODEC,Codec.BOOL).xmap(ClosedDomainBinding::new,s->s.reversed);
 public static final class_10741<ClosedDomainBinding> TYPE=new class_10741<>("inner_dimension_closed_bindings",ClosedDomainBinding::new,CODEC,class_4284.field_19212);
 public static ClosedDomainBinding get(class_3218 world){return world.method_8503().method_30002().method_17983().method_17924(TYPE);}
 public boolean reversed(UUID owner){return reversed.getOrDefault(owner,false);}
 /** Temporary per-cast inversion; never writes the saved choice. */
 public boolean forCast(UUID owner,boolean sneaking){return reversed(owner)^sneaking;}
 public void select(UUID owner,boolean value){reversed.put(owner,value);method_80();}
}
