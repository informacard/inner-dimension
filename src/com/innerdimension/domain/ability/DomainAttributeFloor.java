package com.innerdimension.domain.ability;
import net.minecraft.*;
/** Compute the missing additive contribution without stacking with vanilla enchantments. */
final class DomainAttributeFloor {
 private DomainAttributeFloor(){}
 static double missing(class_1309 entity,class_6880<class_1320> attribute,class_2960 own,double floor){
  var instance=entity.method_5996(attribute);if(instance==null)return 0;
  double base=instance.method_6201(),add=0,multiplyBase=0,multiplyTotal=1;
  for(var modifier:instance.method_6195()){
   if(modifier.comp_2447().equals(own))continue;
   if(modifier.comp_2450()==class_1322.class_1323.field_6330)add+=modifier.comp_2449();
   else if(modifier.comp_2450()==class_1322.class_1323.field_6331)multiplyBase+=modifier.comp_2449();
   else multiplyTotal*=1+modifier.comp_2449();
  }
  double scale=(1+multiplyBase)*multiplyTotal;
  return scale>0?Math.max(0,floor/scale-base-add):0;
 }
}
