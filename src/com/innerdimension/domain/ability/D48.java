package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.runtime.DomainSessionIndex;
import com.innerdimension.domain.runtime.DomainRuntime;
import net.minecraft.*;
/** Charge while crouching motionless, then add an upward impulse to the owner's jump. */
public final class D48 implements DomainAbility {
    public static final D48 INSTANCE = new D48();
    private D48() {}
    @Override public String id() { return "minecraft:breeze"; }
    @Override public String code() { return "D48"; }
    @Override public void onTick(DomainSession session, long now) {
        class_3222 owner = session.owner();
        if (owner == null || !session.contains(owner)) { reset(session); return; }
        class_243 velocity = owner.method_18798();
        boolean still = owner.method_24828() && Math.abs(velocity.field_1352) < 0.005
                && Math.abs(velocity.field_1350) < 0.005;
        if (owner.method_5715() && still) {
            long start = session.state("d48_charge_start", now);
            session.putState("d48_charge_start", start);
            session.putState("d48_last_charge", now);
            if (now - start >= 20) session.flag("d48_ready", true);
        } else {
            session.putState("d48_charge_start", null);
            // Brief grace permits releasing Sneak and pressing Jump together.
            if (!still || now - session.state("d48_last_charge", -100L) > 4) reset(session);
        }
    }
    public static void afterJump(class_1309 entity) {
        if (!(entity instanceof class_3222 owner)) return;
        DomainSession session = DomainSessionIndex.byOwner(owner.method_5667());
        if (session == null || session.ended() || !session.phaseActive() || !session.contains(owner)
                || !session.hasCode("D48") || !session.flag("d48_ready")
                || DomainRuntime.now() - session.state("d48_last_charge", -100L) > 4) return;
        reset(session);
        class_243 velocity = owner.method_18798();
        owner.method_18800(velocity.field_1352, velocity.field_1351 + 0.8, velocity.field_1350);
        owner.field_6037 = true;
        for(var target:session.livingInside()){if(target==owner||com.innerdimension.domain.runtime.DomainRelations.isFriendly(session,target)||target.method_5858(owner)>9)continue;if(!com.innerdimension.domain.geometry.DomainGeometry.lineOfSight(session.world,session,owner.method_33571(),target.method_33571()))continue;target.method_6005(0.8,owner.method_23317()-target.method_23317(),owner.method_23321()-target.method_23321());}

    }
    private static void reset(DomainSession session) {
        session.flag("d48_ready", false);
        session.putState("d48_charge_start", null);
        session.putState("d48_last_charge", null);
    }
    @Override public void onEnd(DomainSession session) { reset(session); }
}
