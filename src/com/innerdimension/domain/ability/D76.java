package com.innerdimension.domain.ability;

import com.innerdimension.domain.DomainModes;
import com.innerdimension.domain.combat.DomainDamagePipeline;
import com.innerdimension.domain.combat.DomainDamageTypes;
import com.innerdimension.domain.runtime.DomainObjects;
import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.network.DomainSlashVisualPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.*;

/** Hidden Vindicator reverse: indiscriminate periodic cuts, with ordinary exemptions. */
public final class D76 implements DomainAbility {
    public static final D76 INSTANCE = new D76();
    public static final int INTERVAL = 4;
    public static final float DAMAGE = 2;
    public static final int COLOR = 0xC00020;
    private D76() {}
    public String id() { return DomainModes.SHRINE; }
    public String code() { return "D76"; }

    public void onTick(DomainSession session, long now) {
        if (!(session.world instanceof class_3218 world) || !session.dueSelf("d76_cut", INTERVAL, now)) return;
        class_3222 owner = session.owner();
        if (owner == null||!com.innerdimension.progression.DomainFeatures.canUseShrine(owner)) return;
        session.armSelf("d76_cut", now);
        List<DomainSlashVisualPayload.Stroke> strokes = new ArrayList<>();
        // DomainObjects applies Simple Domain and contest exemptions before the damage pipeline.
        for (class_1309 victim : DomainObjects.inside(session, code())) {
            if (victim.method_5667().equals(session.ownerId)) continue;
            if(!com.innerdimension.boss.BossDomains.allowShrine(victim,now))continue;
            var random = world.method_8409();
            double azimuth = random.method_43058() * Math.PI * 2;
            double vertical = random.method_43058() * 2 - 1;
            double horizontal = Math.sqrt(1 - vertical * vertical);
            class_243 direction = new class_243(Math.cos(azimuth) * horizontal, vertical, Math.sin(azimuth) * horizontal);
            class_243 center = new class_243((victim.method_5829().field_1323+victim.method_5829().field_1320)*.5,(victim.method_5829().field_1322+victim.method_5829().field_1325)*.5,(victim.method_5829().field_1321+victim.method_5829().field_1324)*.5);
            double halfLength = Math.max(1.6, Math.max(victim.method_17681(),victim.method_17682()) * .8);
            strokes.add(new DomainSlashVisualPayload.Stroke(center.method_1020(direction.method_1021(halfLength)),
                    center.method_1019(direction.method_1021(halfLength)), COLOR, 6));
            DomainDamagePipeline.hurt(victim, owner, DomainDamageTypes.SHRINE_SLASH, DAMAGE);
        }
        DomainSlashVisualPayload.send(world, strokes);
    }
}
