package com.innerdimension.voiddomain;
public final class VoidEmbodimentRules {
 public enum Input { ROOM_TRAVEL, EXPAND, BLOCKED }
 public static Input route(boolean inside,boolean open,boolean embodied){
  if(inside)return open&&embodied?Input.BLOCKED:Input.ROOM_TRAVEL;
  return embodied?Input.BLOCKED:Input.EXPAND;
 }
 public static boolean ambience(boolean alive,boolean room,boolean embodied){return alive&&(room||embodied);}
 public static boolean pillars(int phase){return phase==1;}
 public static boolean embodied(int phase){return phase==1;}
 public static int afterDeath(int phase){return phase==1||phase==4?2:phase;}
 public static long baptismMillis(){return RitualVisualSchedule.playMillis(3)+420;}
 public static boolean baptismReady(int phase,long elapsedNanos){return phase==4&&elapsedNanos>=baptismMillis()*1_000_000L;}
 public static net.minecraft.class_5321<net.minecraft.class_1937> baptismWorld(){return net.minecraft.class_5321.method_29179(net.minecraft.class_7924.field_41223,net.minecraft.class_2960.method_60655("minecraft","the_end"));}
 private VoidEmbodimentRules(){}
}
