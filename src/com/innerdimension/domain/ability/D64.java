package com.innerdimension.domain.ability;
public final class D64 implements DomainAbility {
 public static final D64 INSTANCE=new D64();public String id(){return "minecraft:elder_guardian";}public String code(){return "D64";}
 public static boolean locked(net.minecraft.class_1937 world,net.minecraft.class_2338 pos){for(var s:com.innerdimension.domain.runtime.DomainSessionIndex.coveringBlock(world,pos))if(s.hasCode("D64")&&!com.innerdimension.domain.DomainContest.suppressed(s.ownerId))return true;return false;}
}
