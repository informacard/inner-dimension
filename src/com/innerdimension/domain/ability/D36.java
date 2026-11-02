package com.innerdimension.domain.ability;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.domain.runtime.DomainSession;
import net.minecraft.*;
/** Start with movement into a wall and jump. Sneaking holds the wall; releasing movement descends. */
public final class D36 implements DomainAbility {
    public static final D36 INSTANCE = new D36();
    private D36() {}
    @Override public String id() { return "minecraft:spider"; }
    @Override public String code() { return "D36"; }
    @Override public void onTick(DomainSession s, long now) {
        class_3222 owner = s.owner();
        if (owner == null || !owner.method_5805() || owner.method_7325()) return;
        var input = owner.method_63562();
        double forward = (input.comp_3159() ? 1 : 0) - (input.comp_3160() ? 1 : 0);
        double side = (input.comp_3161() ? 1 : 0) - (input.comp_3162() ? 1 : 0);
        double yaw = Math.toRadians(owner.method_36454());
        double x = side * Math.cos(yaw) - forward * Math.sin(yaw);
        double z = forward * Math.cos(yaw) + side * Math.sin(yaw);
        class_238 box = owner.method_5829();
        boolean contact = touches(s.world, box, 0, 0);
        boolean into = (x != 0 || z != 0) && touches(s.world, box, x, z);
        boolean climbing = contact && (s.flag("d36_climbing") || into && input.comp_3163());
        s.flag("d36_climbing", climbing);
        if (!climbing) return;
        class_243 velocity = owner.method_18798();
        double y = owner.method_5715() ? 0 : into ? 0.15 : -0.15;
        double ceiling = DomainGeometry.ceilingBottom(s.world, box, 0.5);
        if (y > 0 && (!Double.isNaN(ceiling) && ceiling - box.field_1325 < y || box.field_1325 + y > s.maxY())) y = 0;
        owner.method_18799(new class_243(velocity.field_1352, y, velocity.field_1350));
        owner.field_6017 = 0;
        owner.field_6037 = true;
    }
    private static boolean touches(class_1937 world, class_238 b, double x, double z) {
        double e = 0.08, inset = 0.02;
        double y0 = b.field_1322 + 0.05, y1 = b.field_1325 - 0.05;
        boolean any = x == 0 && z == 0;
        return (any || x < -0.01) && DomainGeometry.solid(world, new class_238(b.field_1323-e,y0,b.field_1321+inset,b.field_1323+inset,y1,b.field_1324-inset))
            || (any || x > 0.01) && DomainGeometry.solid(world, new class_238(b.field_1320-inset,y0,b.field_1321+inset,b.field_1320+e,y1,b.field_1324-inset))
            || (any || z < -0.01) && DomainGeometry.solid(world, new class_238(b.field_1323+inset,y0,b.field_1321-e,b.field_1320-inset,y1,b.field_1321+inset))
            || (any || z > 0.01) && DomainGeometry.solid(world, new class_238(b.field_1323+inset,y0,b.field_1324-inset,b.field_1320-inset,y1,b.field_1324+e));
    }
}
