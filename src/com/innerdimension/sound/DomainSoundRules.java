package com.innerdimension.sound;
/** One sound per shell-handle phase, shared by closed, open and simple projections. */
public final class DomainSoundRules {
 private DomainSoundRules(){}
 public static String cue(Integer previous,int phase,double elapsed,boolean simple,boolean broken){
  if(previous==null)return elapsed<=3&&(phase==0||phase==1||simple&&phase==2)?"domain_expand":null;
  if(previous!=phase&&phase==3)return broken?"domain_fracture":"domain_retract";
  return null;
 }
}
