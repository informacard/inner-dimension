import com.innerdimension.projection.ProjectionSafetySearch;
import java.util.List;

public final class ProjectionSafetyRegression {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        var body=new ProjectionSafetySearch.Box(0,10,0,.6,11.8,.6);
        var bounds=new ProjectionSafetySearch.Box(-3,0,-3,3,20,3);
        var space=new ProjectionSafetySearch.Space() {
            public boolean clear(ProjectionSafetySearch.Box box) { return box.minX()>=.5; }
            public boolean supported(ProjectionSafetySearch.Box box) { return false; }
            public List<Double> surfaces(ProjectionSafetySearch.Box box,double min,double max) { return List.of(18d); }
        };
        var target=ProjectionSafetySearch.horizontal(body,space,4,bounds);
        check(target!=null,"Horizontal escape exists");
        check(target.y()==body.minY(),"Non-player escape never changes Y");
        check(ProjectionSafetySearch.inside(body.at(target),bounds),"Horizontal escape respects bounds");

        // Expansion lays the projected floor one hundredth above the caster's feet, so every
        // actor standing on that ground overlaps it. The same-height lateral step can never
        // clear that lip, and the actor has to be lifted onto the floor instead of pushed out.
        final double floor=10.01;
        var room=new ProjectionSafetySearch.Box(-3,floor,-3,3,30,3);
        var floored=new ProjectionSafetySearch.Space() {
            public boolean clear(ProjectionSafetySearch.Box box) {
                return box.minX()>=-3 && box.maxX()<=3 && box.minZ()>=-3 && box.maxZ()<=3
                        && box.minY()>=floor-1e-9 && box.maxY()<=30;
            }
            public boolean supported(ProjectionSafetySearch.Box box) { return Math.abs(box.minY()-floor)<.05; }
            public List<Double> surfaces(ProjectionSafetySearch.Box box,double min,double max) {
                return floor>=min && floor<=max ? List.of(floor) : List.<Double>of();
            }
        };
        var clipped=new ProjectionSafetySearch.Box(-.3,10,.0,.3,11.8,.6);
        check(ProjectionSafetySearch.horizontal(clipped,floored,4,room)==null,
                "Same-height step cannot clear the projected floor lip");
        var lifted=ProjectionSafetySearch.foothold(clipped,floored,floor,20);
        check(lifted!=null,"Vertical foothold finds the projected floor");
        check(lifted.y()>clipped.minY() && lifted.y()<=floor+.001,
                "Foothold lifts the actor onto the floor without launching it");
        check(ProjectionSafetySearch.inside(clipped.at(lifted),room),"Foothold stays inside the room");

        // A room that can only offer a distant high surface is not a landing: the reach stays
        // bounded to about one body height, so collision resolution never rides an actor up.
        var high=new ProjectionSafetySearch.Space() {
            public boolean clear(ProjectionSafetySearch.Box box) { return true; }
            public boolean supported(ProjectionSafetySearch.Box box) { return true; }
            public List<Double> surfaces(ProjectionSafetySearch.Box box,double min,double max) {
                return 18>=min && 18<=max ? List.of(18d) : List.<Double>of();
            }
        };
        check(ProjectionSafetySearch.foothold(body,high,0,20)==null,"Bounded reach rejects distant high surfaces");
        var below=new ProjectionSafetySearch.Box(.2,9.2,.2,.8,11,.8);
        var raised=ProjectionSafetySearch.foothold(below,floored,floor,20);
        check(raised!=null && raised.y()>below.minY(),
                "An actor standing below the projected floor is lifted onto it");

        var pillar=new ProjectionSafetySearch.Box(0,0,0,1,8,1);
        var returnBody=new ProjectionSafetySearch.Box(.2,1,.2,.8,2.8,.8);
        var ground=new ProjectionSafetySearch.Space() {
            public boolean clear(ProjectionSafetySearch.Box b) { return !pillar.intersects(b) && b.minY()>=1; }
            public boolean supported(ProjectionSafetySearch.Box b) { return Math.abs(b.minY()-1)<.01; }
            public List<Double> surfaces(ProjectionSafetySearch.Box b,double min,double max) { return List.of(1d); }
        };
        var returnPoint=ProjectionSafetySearch.nearest(returnBody,ground,4,false,-5,7);
        check(returnPoint!=null && !pillar.intersects(returnBody.at(returnPoint)),"Occupied return offset searches beside the ordinary pillar");
        var offCenter=new ProjectionSafetySearch.Box(2,9.2,1,2.6,11,1.6);
        var occupant=ProjectionSafetySearch.foothold(offCenter,floored,floor,20);
        check(occupant!=null && occupant.x()==offCenter.x() && occupant.z()==offCenter.z(),"Covered occupant preserves its own column instead of using caster center");
        System.out.println("ProjectionSafetyRegression: passed, including pillar return and off-center occupant");
    }
}
