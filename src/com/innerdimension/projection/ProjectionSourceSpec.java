package com.innerdimension.projection;

import com.innerdimension.dimension.RoomGeometry;
import net.minecraft.class_2338;
import net.minecraft.class_238;

/** Source selection, shell identification and alignment are independent of projection/safety. */
public record ProjectionSourceSpec(Volume source, Volume shell, Volume mirror,
                                   double anchorX, double anchorY, double anchorZ) {
    /** Only an actual enclosing shell contributes neighbouring geometry to the other half. */
    public boolean sharesBoundary(){return shell!=null;}
    public ProjectionSourceSpec(Volume source, Volume shell, double anchorX, double anchorY, double anchorZ) {
        this(source, shell, source, anchorX, anchorY, anchorZ);
    }
    public record Volume(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public Volume {
            if (minX > maxX || minY > maxY || minZ > maxZ) throw new IllegalArgumentException("Empty source volume");
        }
        public class_238 bounds() { return new class_238(minX,minY,minZ,maxX+1,maxY+1,maxZ+1); }
        public boolean contains(class_2338 p) {
            return p.method_10263() >= minX && p.method_10263() <= maxX
                    && p.method_10264() >= minY && p.method_10264() <= maxY
                    && p.method_10260() >= minZ && p.method_10260() <= maxZ;
        }
    }

    /** Change this selector (or supply a different spec to snapshot) for future source ranges. */
    public static ProjectionSourceSpec room() {
        int edge = RoomGeometry.INTERIOR_SIZE;
        Volume room = new Volume(-1,RoomGeometry.DOMAIN_FLOOR,-1,edge,RoomGeometry.DOMAIN_CEILING,edge);
        double centerX = (room.minX() + room.maxX() + 1) / 2.0;
        double centerZ = (room.minZ() + room.maxZ() + 1) / 2.0;
        return new ProjectionSourceSpec(room,room,room,centerX,RoomGeometry.DOMAIN_FLOOR + 1,centerZ);
    }

    public boolean isShell(class_2338 p) {
        return shell != null && shell.contains(p) && (p.method_10263() == shell.minX || p.method_10263() == shell.maxX
                || p.method_10264() == shell.minY || p.method_10264() == shell.maxY
                || p.method_10260() == shell.minZ || p.method_10260() == shell.maxZ);
    }
}
