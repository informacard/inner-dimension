package com.innerdimension.projection;

import java.util.Comparator;
import java.util.List;

/** Pure geometry search; the world adapter supplies real/projected shapes and support surfaces. */
public final class ProjectionSafetySearch {
    public static final double CLEARANCE = 0.001;
    public static final double MAX_RISE = 6;
    public record Box(double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {
        public Box move(double x,double y,double z) { return new Box(minX+x,minY+y,minZ+z,maxX+x,maxY+y,maxZ+z); }
        public double x() { return (minX+maxX)/2; }
        public double z() { return (minZ+maxZ)/2; }
        public boolean horizontal(Box b) { return maxX>b.minX+1e-7 && minX<b.maxX-1e-7 && maxZ>b.minZ+1e-7 && minZ<b.maxZ-1e-7; }
        public boolean intersects(Box b) { return horizontal(b) && maxY>b.minY+1e-7 && minY<b.maxY-1e-7; }
        public Box at(Point p) { return move(p.x-x(),p.y-minY,p.z-z()); }
    }
    public record Point(double x,double y,double z) { }
    public interface Space {
        boolean clear(Box box);
        boolean supported(Box box);
        List<Double> surfaces(Box footprint,double minY,double maxY);
    }
    private ProjectionSafetySearch() { }
    /** Supported escape above an interior build, preserving the actor's exact X/Z. */
    /** Covered entities keep their original column and only move when it is unsafe. */
    public static Point occupant(Box body,Space space,double minY,double maxY,int radius) {
        if(body.minY()>=minY&&body.minY()<=maxY&&valid(body,space))
            return new Point(body.x(),body.minY(),body.z());
        Point up=vertical(body,space,Math.max(body.minY(),minY),maxY);
        return up!=null?up:nearest(body,space,radius,false,minY,maxY);
    }
    public static Point vertical(Box body,Space space,double minY,double maxY) {
        return landing(body,body.x(),body.z(),minY,maxY,space);
    }
    /**
     * Vertical reach of one body height above the feet: the most an actor can need to stop
     * overlapping a single block. Bounded on purpose, so resolving an overlap can never ride an
     * actor onto a distant high surface, while an actor left under the projected floor still
     * reaches that floor from any depth it can overlap it.
     */
    public static double reach(Box body,double ceilingY) {
        return Math.min(ceilingY,body.minY()+(body.maxY()-body.minY())+1);
    }
    /**
     * Supported foothold in the actor's own column, kept inside the room and inside that reach.
     * The lower bound is the room floor, so the floor lip that a standing actor overlaps by a
     * hair is a legal landing instead of a reason to push it out through a side.
     */
    public static Point foothold(Box body,Space space,double floorY,double ceilingY) {
        double minY=Math.max(body.minY(),floorY);
        double maxY=reach(body,ceilingY);
        return maxY<minY ? null : vertical(body,space,minY,maxY);
    }
    /** Scan supporting surfaces from bottom to top; never shift away from the center. */
    public static Point lowestStanding(Box body,Space space,double minY,double maxY) {
        return space.surfaces(body,minY,maxY).stream().distinct().sorted()
                .filter(y -> y>=minY && y+CLEARANCE<=maxY)
                .map(y -> new Point(body.x(),y+CLEARANCE,body.z()))
                .filter(p -> valid(body.at(p),space)).findFirst().orElse(null);
    }
    public static boolean inside(Box body, Box volume) {
        return body.minX>=volume.minX && body.maxX<=volume.maxX && body.minY>=volume.minY
                && body.maxY<=volume.maxY && body.minZ>=volume.minZ && body.maxZ<=volume.maxZ;
    }
    public static boolean sameSide(double center, double min, double max, double plane) {
        return center >= plane ? min >= plane : max <= plane;
    }
    /** Minimize displacement from the actual body, including its original airborne height. */
    public static Point nearest(Box body, Space space, int radius, boolean allowAir) {
        return nearest(body, space, radius, allowAir, body.minY-MAX_RISE, body.minY+MAX_RISE);
    }

    /** Interior activation must search the whole room height, including tall builds. */
    public static Point nearest(Box body, Space space, int radius, boolean allowAir, double minY, double maxY) {
        Point best=null;
        double score=Double.POSITIVE_INFINITY;
        for (int ring=0;ring<=radius*4;ring++) {
            for (int dx=-ring;dx<=ring;dx++) for (int dz=-ring;dz<=ring;dz++) {
                if (Math.max(Math.abs(dx),Math.abs(dz))!=ring) continue;
                Box footprint=body.move(dx*.25,0,dz*.25);
                java.util.ArrayList<Double> heights=new java.util.ArrayList<>(space.surfaces(footprint,minY,maxY));
                if (allowAir && body.minY>=minY && body.minY<=maxY) heights.add(body.minY-CLEARANCE);
                for (double height:heights) {
                    Point p=new Point(footprint.x(),height+CLEARANCE,footprint.z());
                    Box target=body.at(p);
                    double distance=dx*dx*.0625+dz*dz*.0625+Math.pow(p.y-body.minY,2);
                    if (distance<score && space.clear(target) && (allowAir || space.supported(target))) {
                        best=p; score=distance;
                    }
                }
            }
            if (best!=null && score <= Math.pow((ring+1)*.25,2)) break;
        }
        return best;
    }

    public static Point find(Box body, Point preferred, Space space, double gridX,double gridZ,int radius) {
        return find(body,preferred,space,gridX,gridZ,radius,MAX_RISE);
    }

    public static Point find(Box body, Point preferred, Space space, double gridX,double gridZ,int radius,double verticalRise) {
        if (preferred != null && preferred.y >= body.minY && preferred.y-body.minY <= verticalRise+1e-7) {
            Point up = new Point(body.x(),preferred.y+CLEARANCE,body.z());
            if (valid(body.at(up),space)) return up;
        }
        // Short vertical escape, without landing on the outer ceiling or jumping over tall walls.
        Point sameColumn = landing(body,body.x(),body.z(),body.minY,body.minY+verticalRise,space);
        if (sameColumn != null && (preferred == null || sameColumn.y >= preferred.y)) return sameColumn;
        // Adjust actual coordinates in quarter-block increments, preserving the
        // original sub-block alignment instead of snapping to block centres.
        double step=0.25;
        for (int r=1;r<=radius/step;r++) {
            Point best=null;
            double distance=Double.POSITIVE_INFINITY;
            for (int dx=-r;dx<=r;dx++) for (int dz=-r;dz<=r;dz++) {
                if (Math.max(Math.abs(dx),Math.abs(dz))!=r) continue;
                double x=body.x()+dx*step, z=body.z()+dz*step;
                Point candidate=landing(body,x,z,body.minY-MAX_RISE,body.minY+MAX_RISE,space);
                if (candidate==null) continue;
                double d=Math.pow(x-body.x(),2)+Math.pow(z-body.z(),2)+Math.pow(candidate.y-body.minY,2);
                if (d<distance) { best=candidate; distance=d; }
            }
            if (best!=null) return best;
        }
        return null;
    }

    private static Point landing(Box original,double x,double z,double minY,double maxY,Space space) {
        Box footprint=original.move(x-original.x(),0,z-original.z());
        return space.surfaces(footprint,minY,maxY).stream().distinct()
                .filter(y -> y>=minY && y<=maxY)
                .sorted(Comparator.comparingDouble(y -> Math.abs(y-original.minY)))
                .map(y -> new Point(x,y+CLEARANCE,z))
                .filter(p -> valid(original.at(p),space)).findFirst().orElse(null);
    }
    private static boolean valid(Box box,Space space) { return space.clear(box) && space.supported(box); }

    /** Same-height escape for non-player actors. It can never turn collision resolution into a launch. */
    public static Point horizontal(Box body,Space space,int radius,Box bounds) {
        for (int ring=1;ring<=radius*4;ring++) for (int dx=-ring;dx<=ring;dx++) for (int dz=-ring;dz<=ring;dz++) {
            if (Math.max(Math.abs(dx),Math.abs(dz))!=ring) continue;
            Point point=new Point(body.x()+dx*.25,body.minY,body.z()+dz*.25);
            Box target=body.at(point);
            if ((bounds==null || inside(target,bounds)) && space.clear(target)) return point;
        }
        return null;
    }

    /** A complete body must leave through an X/Z side, never above the roof or below the floor. */
    public static boolean outsideSide(Box body,Box domain) {
        return body.maxX<=domain.minX || body.minX>=domain.maxX
                || body.maxZ<=domain.minZ || body.minZ>=domain.maxZ;
    }

    /** Grounded lateral evacuation. A missing safe landing cancels activation instead of launching actors. */
    public static Point sideExit(Box body,Box domain,Space space,int radius) {
        double gapX=(body.maxX-body.minX)/2+CLEARANCE*2;
        double gapZ=(body.maxZ-body.minZ)/2+CLEARANCE*2;
        double[][] exits={{domain.minX-gapX,body.z()},{domain.maxX+gapX,body.z()},
                {body.x(),domain.minZ-gapZ},{body.x(),domain.maxZ+gapZ}};
        Point best=null;
        double score=Double.POSITIVE_INFINITY;
        // Search all four sides, including along-wall and outward offsets around obstructions.
        for (int side=0;side<4;side++) for (int outward=0;outward<=radius*2;outward++)
            for (int along=-radius*2;along<=radius*2;along++) {
                double x=exits[side][0],z=exits[side][1];
                if (side<2) { x+=(side==0 ? -1:1)*outward*.5; z+=along*.5; }
                else { x+=along*.5; z+=(side==2 ? -1:1)*outward*.5; }
                double horizontal=Math.pow(x-body.x(),2)+Math.pow(z-body.z(),2);
                if (horizontal>=score) continue;
                // Small terrain steps are fine; do not lift onto high ledges or a roof.
                Point candidate=landing(body,x,z,body.minY-MAX_RISE,body.minY+.5,space);
                if (candidate==null || !outsideSide(body.at(candidate),domain)) continue;
                double distance=horizontal+Math.pow(candidate.y-body.minY,2);
                if (distance<score) { best=candidate; score=distance; }
            }
        return best;
    }

    /** Exterior fallback has the same grounded side-only policy as staged activation. */
    public static Point outside(Box body,Box domain,Space space) {
        Point target=sideExit(body,domain,space,(int)Math.ceil(Math.max(domain.maxX-domain.minX,domain.maxZ-domain.minZ))+2);
        if (target==null) throw new IllegalStateException("No grounded lateral projection evacuation position");
        return target;
    }

    /** Clear horizontal fallback at the original feet height; callers protect the subsequent fall. */
    public static Point sideAirExit(Box body,Box domain,Space space,int radius) {
        double hx=(body.maxX-body.minX)/2+2*CLEARANCE,hz=(body.maxZ-body.minZ)/2+2*CLEARANCE;
        double[][] exits={{domain.minX-hx,body.z()},{domain.maxX+hx,body.z()},
                {body.x(),domain.minZ-hz},{body.x(),domain.maxZ+hz}};
        Point best=null; double score=Double.POSITIVE_INFINITY;
        for (int side=0;side<4;side++) for (int out=0;out<=radius;out++) for (int along=-radius;along<=radius;along++) {
            double x=exits[side][0],z=exits[side][1];
            if (side<2) { x+=(side==0 ? -1:1)*out; z+=along; }
            else { x+=along; z+=(side==2 ? -1:1)*out; }
            double d=Math.pow(x-body.x(),2)+Math.pow(z-body.z(),2);
            if (d>=score) continue;
            Point p=new Point(x,body.minY,z);
            if (outsideSide(body.at(p),domain) && space.clear(body.at(p))) { best=p; score=d; }
        }
        return best;
    }
}
