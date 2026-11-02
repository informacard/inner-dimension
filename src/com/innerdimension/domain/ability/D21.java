package com.innerdimension.domain.ability;

import com.innerdimension.domain.runtime.DomainSession;
import com.innerdimension.domain.world.DomainWorldActions;
import net.minecraft.*;
import java.util.*;

/**
 * D21 minecraft:sniffer (green).
 *
 * <p>Auto-farming: every second a rotating cursor walks at most 16 mature crops inside the
 * domain. Each plant is re-checked for maturity and permission, the real vanilla drop list
 * is computed once, one matching seed is taken out of that harvest (or out of the owner's
 * inventory if the harvest had none) and used to replant; without a seed the crop is only
 * harvested. Everything collected goes into the owner's inventory, and only what genuinely
 * does not fit is dropped at the plant. Projection snapshots are never written and no loot
 * table is rolled twice.</p>
 */
public final class D21 implements DomainAbility {
    public static final D21 INSTANCE = new D21();
    private D21() {}
    @Override public String id() { return "minecraft:sniffer"; }
    @Override public String code() { return "D21"; }

    private static final long INTERVAL = 20;
    private static final int BUDGET = 16;
    private static final int FLAGS = 3;
    private static final String TIMER = "d21_harvest";
    private static final String CURSOR = "d21_crop_cursor";

    @Override
    public void onTick(DomainSession session, long now) {
        if (!D01D23Support.live(session)) return;
        if(com.innerdimension.domain.DomainModes.reverse(session,code())){if(!session.open()||session.interior.progress(0)>=.999)till(session,now);return;}
        if (!session.dueSelf(TIMER, INTERVAL, now)) return;
        session.armSelf(TIMER, now);
        class_3222 owner = session.owner();
        if (owner == null) return;
        if (!(session.world instanceof class_3218 world)) return;

        List<class_2338> candidates = new ArrayList<>();
        for (class_2338 pos : session.blocksIn()) {
            class_2680 state = session.world.method_8320(pos);
            if (DomainWorldActions.harvestable(state) && DomainWorldActions.mature(state)) candidates.add(pos);
        }
        for (class_2338 pos : DomainWorldActions.slice(session, CURSOR, candidates, BUDGET))
            harvest(session, world, owner, pos);
    }

    @SuppressWarnings("unchecked")
    private static void till(DomainSession s,long now){if(!s.dueSelf("d21_till",1,now))return;s.armSelf("d21_till",now);List<class_2338> positions=s.state("d21_till_positions",(List<class_2338>)null);if(positions==null){positions=new ArrayList<>();for(var p:s.blocksIn())positions.add(p);s.putState("d21_till_positions",positions);}int cursor=s.state("d21_till_index",0);for(int n=0;n<64&&cursor<positions.size();n++,cursor++){var p=positions.get(cursor);if(!DomainWorldActions.writable(s,p))continue;String id=class_7923.field_41175.method_10221(s.world.method_8320(p).method_26204()).toString();boolean rooted=id.equals("minecraft:rooted_dirt");boolean coarse=id.equals("minecraft:coarse_dirt");boolean farmland=id.equals("minecraft:grass_block")||id.equals("minecraft:dirt")||id.equals("minecraft:dirt_path");if(!rooted&&!coarse&&!farmland)continue;if(!rooted&&!s.world.method_8320(p.method_10084()).method_26215())continue;var block=D01D23Support.block(farmland?"minecraft:farmland":"minecraft:dirt");if(block!=null&&DomainWorldActions.setBlock(s,p,block.method_9564(),3)&&rooted){var roots=D01D23Support.item("minecraft:hanging_roots");if(roots!=null)class_2248.method_9577(s.world,p,new class_1799(roots));}}s.putState("d21_till_index",cursor);}

    private static void harvest(DomainSession session, class_3218 world, class_3222 owner, class_2338 pos) {
        if(D64.locked(world,pos))return;
        if (!DomainWorldActions.writable(session, pos)) return;
        class_2680 state = session.world.method_8320(pos);
        if (!DomainWorldActions.harvestable(state) || !DomainWorldActions.mature(state)) return;

        // The single real loot roll for this crop.
        List<class_1799> drops = class_2248.method_9562(state, world, pos, null);
        class_1792 seed = DomainWorldActions.seed(state);
        boolean replanted = false;
        if (seed != null) {
            for (Iterator<class_1799> iterator = drops.iterator(); iterator.hasNext(); ) {
                class_1799 stack = iterator.next();
                if (stack.method_7960() || stack.method_7909() != seed) continue;
                stack.method_7939(stack.method_7947() - 1);
                if (stack.method_7960()) iterator.remove();
                replanted = true;
                break;
            }
            if (!replanted) replanted = D01D23Support.takeOne(owner, seed);
        }

        if (replanted) {
            class_2769<Integer> age = DomainWorldActions.ageProperty(state);
            class_2680 replant = age == null ? state : state.method_11657(age, 0);
            DomainWorldActions.setBlock(session, pos, replant, FLAGS);
        } else {
            class_2248 air = D01D23Support.block("minecraft:air");
            if (air != null) DomainWorldActions.setBlock(session, pos, air.method_9564(), FLAGS);
        }

        // Harvest first, inventory second, ground only for the true remainder.
        for (class_1799 stack : drops) D01D23Support.give(owner, session, pos, stack);
    }

    @Override
    public void onEnd(DomainSession session) {
        DomainWorldActions.forget(session);
    }
}
