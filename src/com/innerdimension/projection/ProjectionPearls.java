package com.innerdimension.projection;
import net.minecraft.*;
public final class ProjectionPearls {
    public static class_243 hit(class_1684 pearl) {
        class_243 start=pearl.method_73189(),end=start.method_1019(pearl.method_18798());
        class_243 nearest=null;double distance=Double.POSITIVE_INFINITY;
        for(RoomProjectionEntity projection:pearl.method_73183().method_8390(RoomProjectionEntity.class,new class_238(start,end).method_1014(1),RoomProjectionEntity::collisionEnabled)) {
            class_243 origin=projection.getAnchorVec();
            var snapshot=(RoomProjectionContraption)projection.getContraption();
            for(var entry:snapshot.getBlocks().entrySet()) {
                class_2338 pos=entry.getKey();
                if(!new class_238(pos).method_989(origin.field_1352,origin.field_1351,origin.field_1350).method_994(new class_238(start,end).method_1014(.001))) continue;
                for(class_238 local:entry.getValue().comp_1342().method_26194(snapshot.getContraptionWorld(),pos,class_3726.method_16194()).method_1090()) {
                    class_238 box=local.method_989(origin.field_1352+pos.method_10263(),origin.field_1351+pos.method_10264(),origin.field_1350+pos.method_10260());
                    class_243 point=box.method_1006(start) ? start : box.method_992(start,end).orElse(null);
                    if(point!=null && start.method_1025(point)<distance) { nearest=point;distance=start.method_1025(point); }
                }
            }
        }
        // A real block in front must retain its normal vanilla impact.
        class_239 real=pearl.method_73183().method_17742(new class_3959(start,end,class_3959.class_3960.field_17558,class_3959.class_242.field_1348,pearl));
        if(real.method_17783()!=class_239.class_240.field_1333 && start.method_1025(real.method_17784())<distance) return null;
        return nearest;
    }
}
