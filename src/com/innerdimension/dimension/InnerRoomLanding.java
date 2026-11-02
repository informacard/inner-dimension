package com.innerdimension.dimension;
import com.innerdimension.projection.*;
import com.innerdimension.projection.ProjectionSafetySearch.Box;
import com.innerdimension.projection.ProjectionSafetySearch.Point;
import com.innerdimension.voiddomain.InfectionPillars;
import net.minecraft.*;
/** Validate a full standing body against both real blocks and entity projection solids. */
public final class InnerRoomLanding {
    private InnerRoomLanding(){}
    public static Point find(class_3218 world,class_3222 actor,Point desired,boolean upper){
        var current=actor.method_5829();
        var standing=actor.method_18377(class_4050.field_18076).method_30231(actor.method_23317(),current.field_1322,actor.method_23321());
        double x=actor.method_23317(),z=actor.method_23321();
        double halfX=Math.max(Math.max(Math.abs(current.field_1323-x),Math.abs(current.field_1320-x)),Math.max(Math.abs(standing.field_1323-x),Math.abs(standing.field_1320-x)));
        double halfZ=Math.max(Math.max(Math.abs(current.field_1321-z),Math.abs(current.field_1324-z)),Math.max(Math.abs(standing.field_1321-z),Math.abs(standing.field_1324-z)));
        Box body=new Box(x-halfX,current.field_1322,z-halfZ,x+halfX,Math.max(current.field_1325,standing.field_1325),z+halfZ);
        return InnerRoomLandingSearch.find(body,desired,upper,InfectionPillars.present(world),b->{
            var box=new class_238(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());
            return world.method_8587(actor,box)&&!ProjectionEnvironment.projectedCollision(world,box);
        });
    }
}
