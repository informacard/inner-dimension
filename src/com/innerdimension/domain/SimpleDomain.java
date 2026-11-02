package com.innerdimension.domain;

import com.innerdimension.core.ServerClock;
import com.innerdimension.domain.runtime.*;
import net.minecraft.*;
import java.util.*;

/** Damage shields, mandatory crouch and short attacker locks of the nine-cell domain. */
public final class SimpleDomain {
    public static final class Shield {
        public final class_1309 player;
        public double remaining;
        final Runnable close;
        final boolean wasSneaking;
        final Map<UUID,Long> locked=new HashMap<>();
        Shield(class_1309 player,double strength,Runnable close) {
            this.player=player; this.remaining=DomainContestRules.simpleTicks(strength);
            this.close=close; this.wasSneaking=player.method_5715();
        }
    }
    private static final Map<UUID,Shield> SHIELDS=new HashMap<>();
    private static final Map<UUID,Set<UUID>> FORCED_ENEMIES=new HashMap<>();
    private SimpleDomain() {}
    public static Shield get(UUID id) { return SHIELDS.get(id); }
    public static boolean active(class_1297 entity) {
        var shield=get(entity.method_5667());var entry=DomainContest.entry(entity.method_5667());
        return shield!=null&&entry!=null&&entry.covers(entity);
    }
    public static void start(class_3222 player,double strength,Runnable close) { start((class_1309)player,strength,close); }
    public static void start(class_1309 player,double strength,Runnable close) {
        end(player.method_5667()); SHIELDS.put(player.method_5667(),new Shield(player,strength,close));
        EffectLeaseManager.shieldEntity(player,DomainRuntime.now());
        AttributeLeaseManager.revokeEntity(player);
        if(player instanceof class_3222 serverPlayer)serverPlayer.method_7346();
        player.method_5660(true);
    }
    public static void end(UUID id) {
        Shield s=SHIELDS.remove(id); if(s!=null) s.player.method_5660(s.wasSneaking);
        FORCED_ENEMIES.remove(id);
    }
    public static void forceEnemy(UUID owner,UUID enemy) { FORCED_ENEMIES.computeIfAbsent(owner,k->new HashSet<>()).add(enemy); }
    public static boolean forcedEnemy(UUID owner,UUID enemy) { return FORCED_ENEMIES.getOrDefault(owner,Set.of()).contains(enemy); }
    public static boolean locked(UUID owner,class_1297 entity) {
        Shield s=get(owner); return s!=null&&s.player.method_73183()==entity.method_73183()
            &&s.locked.getOrDefault(entity.method_5667(),0L)>ServerClock.now();
    }
    public static List<class_1309> lockedLiving(UUID owner) {
        Shield s=get(owner); if(s==null)return List.of();
        List<class_1309> out=new ArrayList<>();
        s.locked.forEach((id,until)->{var e=((class_3218)s.player.method_73183()).method_73284(id);if(until>ServerClock.now()&&e instanceof class_1309 l&&l.method_5805())out.add(l);});
        return out;
    }
    public static boolean absorb(class_1309 victim,class_1282 source,float amount) {
        // The protected player cannot attack with melee, projectiles or delayed item effects.
        if(source!=null && source.method_5529()!=null && active(source.method_5529()) && !com.innerdimension.domain.combat.DomainDamageTypes.isDomainDamage(source)) return true;
        Shield s=get(victim.method_5667()); if(s==null)return false;
        if(!active(victim)){end(victim.method_5667());s.close.run();return false;}
        var attacker=source==null?null:source.method_5529();
        var session=DomainSessionIndex.byOwner(victim.method_5667());
        boolean reverse=session!=null&&com.innerdimension.domain.DomainModes.reverse(session,"D70");
        if(!reverse&&attacker!=null&&!attacker.method_5667().equals(victim.method_5667()))s.locked.put(attacker.method_5667(),ServerClock.now()+40);
        s.remaining-=DomainContestRules.damageTicks(amount);
        // This entire hit is paid by the shield, including the hit which exhausts it.
        if(s.remaining<=0) { end(victim.method_5667()); s.close.run(); }
        return true;
    }
    public static void tick() {
        for(var s:List.copyOf(SHIELDS.values())) {
            var entry=DomainContest.entry(s.player.method_5667());
            if(!s.player.method_5805()||entry==null||!entry.covers(s.player)) { end(s.player.method_5667());s.close.run();continue; }
            s.player.method_5660(true);
            s.locked.entrySet().removeIf(e->e.getValue()<=ServerClock.now());
            s.remaining-=com.innerdimension.boss.BossDomains.simpleRate(s.player);
            if(s.remaining<=0) { end(s.player.method_5667());s.close.run(); }
        }
    }
    public static void clear() { for(UUID id:List.copyOf(SHIELDS.keySet()))end(id);FORCED_ENEMIES.clear(); }
}
