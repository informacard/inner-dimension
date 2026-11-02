import com.innerdimension.dimension.InnerRoomLandingSearch;
import com.innerdimension.projection.ProjectionSafetySearch.Box;
import com.innerdimension.projection.ProjectionSafetySearch.Point;
import java.util.Random;
public final class InnerRoomLandingRegression {
    private static int checks;
    private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    private static Box body(double width,double height){return new Box(-width/2,0,-width/2,width/2,height,width/2);}
    private static void safe(Box body,Point point,boolean upper){
        check(point!=null,"A free chamber must have a landing");Box b=body.at(point);double floor=upper?32:0;
        check(b.minX()>=0&&b.maxX()<=16&&b.minZ()>=0&&b.maxZ()<=16&&b.minY()>=floor&&b.maxY()<=floor+16,"Full body stays in the intended chamber");
        check(!b.intersects(new Box(7,floor,7,9,floor+16,9)),"No body part overlaps the infection pillar");
    }
    public static void main(String[] args){
        Box normal=body(.6,1.8);Point center=new Point(8,.5,8);
        check(normal.at(center).intersects(new Box(7,0,7,9,16,9)),"Reproduce unsafe old lower-room entry");
        safe(normal,InnerRoomLandingSearch.find(normal,center,false,true,b->true),false);
        safe(normal,InnerRoomLandingSearch.find(normal,new Point(6.9,45,8),true,true,b->true),true);
        Point air=new Point(4.2,45.3,5.7);
        check(air.equals(InnerRoomLandingSearch.find(normal,air,true,true,b->true)),"Retain intended high-air landing when clear");
        Point remembered=new Point(3.2,.5,4.7);
        check(remembered.equals(InnerRoomLandingSearch.find(normal,remembered,false,true,b->true)),"Return keeps exact remembered coordinates when clear");
        Box build=new Box(0,32,0,7,48,16);
        Point aroundBuild=InnerRoomLandingSearch.find(normal,new Point(4,45,8),true,true,b->!b.intersects(build));
        safe(normal,aroundBuild,true);check(!normal.at(aroundBuild).intersects(build),"Also avoid full-height player builds");
        check(InnerRoomLandingSearch.find(normal,center,false,true,b->false)==null,"Fully blocked room refuses instead of teleporting outside");
        check(InnerRoomLandingSearch.find(body(17,1.8),center,false,true,b->true)==null,"Oversized actor cannot be squeezed through walls");
        check(InnerRoomLandingSearch.find(body(.6,17),center,false,true,b->true)==null,"Oversized height cannot cross floors");
        check(InnerRoomLandingSearch.find(normal,new Point(Double.NaN,.5,8),false,true,b->true)==null,"Reject invalid return coordinates");
        check(center.equals(InnerRoomLandingSearch.find(normal,center,false,false,b->true)),"Ordinary empty room retains center");
        safe(body(3.2,6),InnerRoomLandingSearch.find(body(3.2,6),new Point(8,46,8),true,true,b->true),true);
        safe(normal,InnerRoomLandingSearch.find(normal,new Point(8,14.15,8),false,true,b->true),false);
        Random random=new Random(250);
        for(int i=0;i<100;i++){
            boolean upper=(i&1)==0;Box actor=body(.3+random.nextDouble()*3,1+random.nextDouble()*5);
            Point wanted=new Point(-2+random.nextDouble()*20,(upper?32:0)+random.nextDouble()*16,-2+random.nextDouble()*20);
            safe(actor,InnerRoomLandingSearch.find(actor,wanted,upper,true,b->true),upper);
        }
        System.out.println("PASS InnerRoomLandingRegression: "+checks+" checks, lower entry, standing body, builds, bounded failure, return coordinates and high-air travel");
    }
}
