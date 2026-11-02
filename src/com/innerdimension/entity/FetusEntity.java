package com.innerdimension.entity;
import net.minecraft.*;
/** Temporary plushie appearance; no AI, movement, collision or survival targeting. */
public class FetusEntity extends SensoryLinkDollEntity {
    public FetusEntity(class_1299<? extends SensoryLinkDollEntity> type,class_1937 world){super(type,world);method_5977(true);method_5875(true);field_23807=false;field_5960=true;}
    @Override public void method_5773(){field_23807=false;field_5960=true;method_18799(class_243.field_1353);}
    @Override public boolean method_5863(){return false;}
    @Override public boolean method_5810(){return false;}
    @Override public boolean method_30948(class_1297 other){return false;}
    @Override public class_1269 method_5688(class_1657 player,class_1268 hand){return class_1269.field_5811;}
    @Override public boolean method_64397(class_3218 world,class_1282 source,float amount){return false;}
}
