package com.innerdimension.dimension;
import com.innerdimension.projection.ProjectionSafetySearch.Box;
import com.innerdimension.projection.ProjectionSafetySearch.Point;
import java.util.*;
import java.util.function.Predicate;
/** Bounded search inside one chamber. No exterior, roof or other-dimension fallback. */
public final class InnerRoomLandingSearch {
    private InnerRoomLandingSearch(){}
    public static Point find(Box body,Point desired,boolean upper,boolean pillar,Predicate<Box> clear){
        double floor=upper?32:0,ceiling=upper?48:16,margin=.02;
        double height=body.maxY()-body.minY();
        if(!Double.isFinite(desired.x())||!Double.isFinite(desired.y())||!Double.isFinite(desired.z())||height<=0||height>ceiling-floor-2*margin)return null;
        double y=Math.max(floor+margin,Math.min(ceiling-height-margin,desired.y()));
        Box reserved=new Box(7,floor,7,9,ceiling,9);
        Predicate<Point> valid=p->{
            Box at=body.at(p);
            return at.minX()>=margin&&at.maxX()<=16-margin&&at.minZ()>=margin&&at.maxZ()<=16-margin
                &&at.minY()>=floor+margin&&at.maxY()<=ceiling-margin
                &&(!pillar||!at.intersects(reserved))&&clear.test(at);
        };
        Point preferred=new Point(desired.x(),y,desired.z());
        if(valid.test(preferred))return preferred;
        List<Point> candidates=new ArrayList<>();
        List<Double> heights=new ArrayList<>();heights.add(y);
        for(double level=floor+.05;level<=ceiling-height-margin;level+=.5)heights.add(level);
        for(double level:heights){
            candidates.add(new Point(desired.x(),level,desired.z()));
            for(int x=0;x<16;x++)for(int z=0;z<16;z++)candidates.add(new Point(x+.5,level,z+.5));
        }
        candidates.sort(Comparator.comparingDouble(p->distance(p,preferred)));
        for(Point candidate:candidates)if(valid.test(candidate))return candidate;
        return null;
    }
    private static double distance(Point a,Point b){double x=a.x()-b.x(),y=a.y()-b.y(),z=a.z()-b.z();return x*x+y*y+z*z;}
}
