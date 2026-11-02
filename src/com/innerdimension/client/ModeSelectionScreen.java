package com.innerdimension.client;
import com.innerdimension.domain.DomainModes;
import net.minecraft.*;

final class ModeSelectionScreen extends class_437 {
 private final DomainCoreScreen parent;
 private final String id;
 private int mode;
 ModeSelectionScreen(DomainCoreScreen parent,String id,int mode){
  super(class_2561.method_43471("screen.inner_dimension.modes.title"));
  this.parent=parent;this.id=id;
  this.mode=mode==2?2:1;
 }
 protected void method_25426(){
  int x=field_22789/2-100,y=field_22790/2-35;
  String reverseKey="screen.inner_dimension.modes.reverse";
  method_37063(class_4185.method_46430(class_2561.method_43471("screen.inner_dimension.modes.forward"),b->mode=1).method_46434(x,y,200,20).method_46431());
  method_37063(class_4185.method_46430(class_2561.method_43471(reverseKey),b->mode=2).method_46434(x,y+26,200,20).method_46431());
  method_37063(class_4185.method_46430(class_2561.method_43471("screen.inner_dimension.save"),b->{
   parent.saveObjects(id,mode);
   field_22787.method_1507(parent);
  }).method_46434(x,y+58,200,20).method_46431());
 }
 public void method_25394(class_332 g,int x,int y,float d){
  parent.method_25394(g,-1,-1,d);g.method_25294(0,0,field_22789,field_22790,0xb0000000);super.method_25394(g,x,y,d);
  g.method_27534(field_22793,field_22785,field_22789/2,field_22790/2-65,0xffffffff);
  g.method_25294(field_22789/2-113,field_22790/2-35+(mode==2?26:0),field_22789/2-105,field_22790/2-15+(mode==2?26:0),mode==2?0xffffffff:0xff000000);
 }
 public void method_25419(){field_22787.method_1507(parent);}
}
