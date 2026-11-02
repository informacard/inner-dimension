package com.innerdimension.item;
import net.minecraft.*;
/** A single-use extractor whose delayed transaction is owned by the server. */
public class SemenExtractorItem extends class_1792 {
 public SemenExtractorItem(class_1793 settings){super(settings);}
 @Override public class_1269 method_7847(class_1799 stack,class_1657 user,class_1309 target,class_1268 hand){
  if(hand!=class_1268.field_5808)return class_1269.field_5811;
  if(user.method_73183().method_8608())return class_1269.field_5812;
  return user instanceof class_3222 p&&SemenExtraction.start(p,target,stack)?class_1269.field_5812:class_1269.field_5811;
 }
}
