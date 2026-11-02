package com.innerdimension.projection;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_265;

/** Retains the initial projection shapes so an expanded query can replace them. */
public final class ProjectionCollisionSeed extends ArrayList<class_265> {
    private final List<class_265> projected;
    public ProjectionCollisionSeed(List<class_265> vanilla, List<class_265> projected) {
        super(vanilla.size() + projected.size());
        this.projected = List.copyOf(projected);
        addAll(vanilla);
        addAll(projected);
    }
    public List<class_265> projected() { return projected; }

    /** Re-query each solver's swept box; preserve other mods' shapes and never stack old seeds. */
    public static List<class_265> refresh(List<class_265> original, List<class_265> projected) {
        if (!(original instanceof ProjectionCollisionSeed) && projected.isEmpty()) return original;
        List<class_265> base = original;
        if (original instanceof ProjectionCollisionSeed seed) {
            base = new ArrayList<>(original);
            base.removeAll(seed.projected());
        }
        return projected.isEmpty() ? base : new ProjectionCollisionSeed(base, projected);
    }
}
