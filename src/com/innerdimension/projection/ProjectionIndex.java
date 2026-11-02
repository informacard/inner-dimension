package com.innerdimension.projection;

import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.class_1937;
import net.minecraft.class_238;

/** Large contraptions span sections far beyond the section containing their entity origin. */
public final class ProjectionIndex {
    private static final Map<class_1937, List<WeakReference<RoomProjectionEntity>>> WORLDS = new WeakHashMap<>();
    /** Cheap global gate: true only while at least one projection is live (see ProjectionBlocks). */
    private static volatile boolean ANY;
    private ProjectionIndex() {}

    public static synchronized void track(RoomProjectionEntity projection) {
        WORLDS.computeIfAbsent(projection.method_73183(), ignored -> new ArrayList<>())
                .add(new WeakReference<>(projection));
        ANY = true;
    }

    /** True while any projection may answer a block query; false keeps that path free. */
    public static boolean any() {
        return ANY;
    }

    /** Clears the gate once no tracked projection is alive any more. */
    private static void refreshAny() {
        if (!ANY) return;
        for (List<WeakReference<RoomProjectionEntity>> references : WORLDS.values())
            for (WeakReference<RoomProjectionEntity> reference : references) {
                RoomProjectionEntity projection = reference.get();
                if (projection != null && !projection.method_31481()) return;
            }
        ANY = false;
    }

    public static synchronized List<RoomProjectionEntity> solid(class_1937 world, class_238 region) {
        var references = WORLDS.get(world);
        if (references == null) return List.of();
        List<RoomProjectionEntity> result = new ArrayList<>();
        references.removeIf(reference -> {
            var projection = reference.get();
            if (projection == null || projection.method_31481()) return true;
            // Spawn and custom structure payloads are independent packets. A freshly tracked
            // client entity can therefore exist for a few frames before its contraption arrives.
            // It must not participate in collision/fluid/block queries until synchronization is
            // complete; every consumer returned from this method dereferences that structure.
            if (projection.getContraption() != null && projection.collisionEnabled()
                    && projection.method_5829().method_994(region)) result.add(projection);
            return false;
        });
        refreshAny();
        return result;
    }

    /** Completed future geometry, including domains still expanding or under arbitration. */
    public static synchronized List<RoomProjectionEntity> future(class_1937 world,class_238 region) {
        var references=WORLDS.get(world);if(references==null)return List.of();
        List<RoomProjectionEntity> out=new ArrayList<>();
        for(var reference:references) {
            var p=reference.get();
            if(p!=null&&!p.method_31481()&&p.getContraption()!=null&&p.phase()!=ProjectionAnimation.RETRACTING
                &&p.method_5829().method_994(region))out.add(p);
        }
        return out;
    }

    /** Client/server lookup used by the crest renderer; prefer the shell half. */
    public static synchronized RoomProjectionEntity active(class_1937 world, UUID owner) {
        var references=WORLDS.get(world);
        if(references==null)return null;
        RoomProjectionEntity fallback=null;
        var iterator=references.iterator();
        while(iterator.hasNext()) {
            var projection=iterator.next().get();
            if(projection==null||projection.method_31481()){iterator.remove();continue;}
            if(owner.equals(projection.owner())) {
                if(projection.isShell())return projection;
                fallback=projection;
            }
        }
        return fallback;
    }

    public static synchronized RoomProjectionEntity half(class_1937 world, UUID owner, boolean shell) {
        var references=WORLDS.get(world);
        if(references==null || owner==null) return null;
        for(var reference:references) {
            var projection=reference.get();
            if(projection!=null && !projection.method_31481() && projection.isShell()==shell && owner.equals(projection.owner())) return projection;
        }
        return null;
    }
}
