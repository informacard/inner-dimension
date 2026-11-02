package com.innerdimension.item;
import net.minecraft.*;
public final class VoidNailItem extends class_1792 {
 public VoidNailItem(class_1793 settings){super(settings);}
 @Override public int method_7881(class_1799 stack,class_1309 user){return 72000;}
 @Override public class_1839 method_7853(class_1799 stack){return class_1839.field_8953;}
 @Override public class_1269 method_7836(class_1937 world,class_1657 user,class_1268 hand){
  if(user instanceof class_3222 p&&!com.innerdimension.progression.InnerAdvancements.done(p,"enlightenment"))return class_1269.field_5814;
  user.method_6019(hand);return class_1269.field_5812;
 }
 @Override public boolean method_7840(class_1799 stack,class_1937 world,class_1309 user,int remaining){
  if(user instanceof class_3222 p&&72000-remaining>=5&&com.innerdimension.progression.InnerAdvancements.done(p,"enlightenment")){
   com.innerdimension.voiddomain.VoidNails.fire(p,Math.min(1,(72000-remaining)/20.0));return true;
  }return false;
 }
}
