package com.innerdimension.boss;
import com.innerdimension.chunk.LoadedChunks;
import com.innerdimension.domain.runtime.EffectLeaseManager;
import com.innerdimension.domain.geometry.DomainGeometry;
import com.innerdimension.core.ServerClock;
import net.minecraft.*;
import java.util.*;

/** Exact Boss/cast/wave ownership. The vanilla Wither's own projectiles are never tracked. */
public final class WitherDomainEffects {
    public static final String TAG="inner_dimension:boss_summon:";
    private static final Map<UUID,Shot> SHOTS=new HashMap<>();
    private final BossEncounter boss;
    private final List<Wave> waves=new ArrayList<>();
    private long environmentCursor;
    private static final class Wave {
        final int number;
        final UUID lease=UUID.randomUUID();
        final List<class_1308> skeletons=new ArrayList<>();
        final List<class_243> deaths=new ArrayList<>();
        final List<class_1295> clouds=new ArrayList<>();
        final Set<UUID> cloudHits=new HashSet<>();
        final Set<class_1309> affected=new HashSet<>();
        final BossWaveQuota quota=new BossWaveQuota();
        boolean executed,launched;
        int summonAttempts;
        long cloudUntil;
        Wave(int n){number=n;}
    }
    private static final class Shot {
        final WitherDomainEffects effect;final Wave wave;final class_1687 entity;final long expires;
        UUID target;
        Shot(WitherDomainEffects effect,Wave wave,class_1687 entity,UUID target){this.effect=effect;this.wave=wave;this.entity=entity;this.target=target;expires=ServerClock.now()+120;}
    }
    WitherDomainEffects(BossEncounter boss){this.boss=boss;}
    void tick(long now){
        long age=now-boss.activeAt;
        if(age>=20&&(age-20)%180==0&&waves.size()<4){var wave=new Wave(waves.size());waves.add(wave);}
        for(var wave:waves){
            if(wave.summonAttempts<3&&!wave.executed){wave.summonAttempts++;summon(wave);}
            if(!wave.executed&&age>=80+180L*wave.number){wave.executed=true;execute(wave,now);}
            if(!wave.launched&&age>=140+180L*wave.number){wave.launched=true;launch(wave);}
            if(now<=wave.cloudUntil)clouds(wave,now);
            if(now>wave.cloudUntil)wave.clouds.removeIf(e->{if(!e.method_31481())e.method_31472();return true;});
        }
        for(var shot:List.copyOf(SHOTS.values()))if(shot.effect==this)update(shot,now);
        // Lease revocation is by this wave only, leaving vanilla or another Boss's Wither intact.
        for(var wave:waves)for(var p:List.copyOf(wave.affected))if(p.method_73183()!=boss.world||BossDomains.protectedBy(boss,p)||!boss.contains(p.method_73189(),false))
            {EffectLeaseManager.revoke(p,EffectLeaseManager.effect("minecraft:wither"),wave.lease,now);wave.affected.remove(p);}
        environment();
    }
    private void summon(Wave wave){
        long count=waves.stream().flatMap(w->w.skeletons.stream()).filter(class_1308::method_5805).count();
        var type=class_7923.field_41177.method_10223(class_2960.method_60655("minecraft","wither_skeleton")).orElseThrow().comp_349();
        for(int i=wave.summonAttempts-1;i<wave.summonAttempts&&count<6;i++){
            var raw=type.method_5883(boss.world,class_3730.field_16462);if(!(raw instanceof class_1308 mob))continue;
            boolean found=false;
            for(int step=0;step<8;step++){
                double a=(i*8+step)*Math.PI/12;double x=boss.entity.method_23317()+Math.cos(a)*(4+step/8),z=boss.entity.method_23321()+Math.sin(a)*(4+step/8);
                int base=(int)Math.floor(boss.entity.method_23318());
                for(int dy=1;dy>=-3;dy--){
                    var pos=new class_2338((int)Math.floor(x),base+dy,(int)Math.floor(z));if(!LoadedChunks.contains(boss.world,pos))continue;
                    mob.method_5814(x,base+dy,z);
                    double support=DomainGeometry.supportDistance(boss.world,mob.method_5829(),.1);
                    if(!Double.isNaN(support)&&Math.abs(support)<.1&&!DomainGeometry.solid(boss.world,mob.method_5829())&&boss.world.method_8335(mob,mob.method_5829()).isEmpty()){found=true;break;}
                }
                if(found)break;
            }
            if(!found)continue;
            mob.method_5752().add(TAG+boss.id+":"+boss.cast+":"+wave.number);mob.method_5952(false);
            mob.method_5673(class_1304.field_6173,new class_1799(class_7923.field_41178.method_10223(class_2960.method_60655("minecraft","stone_sword")).orElseThrow().comp_349()));
            if(spawn(mob)){wave.skeletons.add(mob);count++;}
        }
    }
    private void execute(Wave wave,long now){
        for(var mob:wave.skeletons){
            if(!mob.method_5805()||mob.method_31481()||boss.world.method_73284(mob.method_5667())!=mob)continue;
            var point=mob.method_73189();mob.method_5768(boss.world);
            if(mob.method_5805())continue; // cancelled death / resurrection does not invent a corpse
            wave.deaths.add(point);
            var cloud=new class_1295(boss.world,point.field_1352,point.field_1351,point.field_1350);
            cloud.method_5595(0);cloud.method_5604(20);cloud.method_5603(2);cloud.method_5596(0);cloud.method_5609(0);cloud.method_35043(20);cloud.method_66232(1);
            cloud.method_5608(class_2398.field_11251);cloud.method_5752().add(TAG+boss.id+":"+boss.cast+":"+wave.number);
            // Visual vanilla cloud; effects are source-leased below so protection and wave
            // deduplication can be checked before every application.
            if(spawn(cloud))wave.clouds.add(cloud);
        }
        wave.cloudUntil=now+19;
        wave.deaths.sort(Comparator.comparingDouble((class_243 p)->p.field_1352).thenComparingDouble(p->p.field_1351).thenComparingDouble(p->p.field_1350));
    }
    private void clouds(Wave wave,long now){
        if(now%5!=0)return;
        for(var cloud:wave.clouds)for(var raw:boss.world.method_8335(cloud,cloud.method_5829())){
            if(!(raw instanceof class_1309 target)||!target.method_5805()||target.method_7325()||BossDomains.protectedBy(boss,target)||wave.cloudHits.contains(target.method_5667()))continue;
            double dx=target.method_23317()-cloud.method_23317(),dz=target.method_23321()-cloud.method_23321();if(dx*dx+dz*dz>4)continue;
            var effect=EffectLeaseManager.effect("minecraft:wither");
            if(effect!=null&&target.method_6049(new class_1293(effect,40,0))){wave.cloudHits.add(target.method_5667());wave.affected.add(target);EffectLeaseManager.apply(target,effect,"boss_cloud",wave.lease,40,0,now);}
        }
    }
    private boolean legal(class_3222 p,Wave wave){return p.method_5805()&&!p.method_7325()&&p.method_73183()==boss.world&&boss.contains(p.method_73189(),false)&&!BossDomains.protectedBy(boss,p)&&wave.quota.available(p.method_5667());}
    private class_3222 choose(Wave wave,class_243 point){return boss.world.method_8503().method_3760().method_14571().stream().filter(p->legal(p,wave)).min(Comparator.<class_3222>comparingDouble(p->p.method_73189().method_1025(point)).thenComparing(class_3222::method_5667)).orElse(null);}
    private void launch(Wave wave){
        for(var point:wave.deaths){
            if(SHOTS.values().stream().filter(s->s.effect==this&&!s.entity.method_31481()).count()>=6)break;
            var p=choose(wave,point);if(p==null)continue;
            var shot=new DomainSkull(boss.world,boss.entity,p.method_33571().method_1020(point).method_1029());
            shot.method_5814(point.field_1352,point.field_1351+1,point.field_1350);shot.method_5752().add(TAG+boss.id+":"+boss.cast+":"+wave.number);
            if(spawn(shot)){wave.quota.reserve(p.method_5667());SHOTS.put(shot.method_5667(),new Shot(this,wave,shot,p.method_5667()));}
        }
        wave.deaths.clear();
    }
    private void update(Shot shot,long now){
        if(shot.entity.method_31481()||now>=shot.expires){shot.entity.method_31472();shot.wave.quota.release(shot.target);SHOTS.remove(shot.entity.method_5667());return;}
        var p=boss.world.method_8503().method_3760().method_14602(shot.target);
        shot.wave.quota.release(shot.target);
        if(p==null||!legal(p,shot.wave))p=choose(shot.wave,shot.entity.method_73189());
        if(p==null){shot.entity.method_31472();SHOTS.remove(shot.entity.method_5667());return;}
        shot.target=p.method_5667();shot.wave.quota.reserve(shot.target);
        var desired=p.method_33571().method_1020(shot.entity.method_73189()).method_1029().method_1021(.55);
        var current=shot.entity.method_18798();var delta=desired.method_1020(current);double len=delta.method_1033();
        shot.entity.method_18799(current.method_1019(delta.method_1021(len<1e-8?0:Math.min(1,.08/len))));
        shot.entity.field_51893=0; // bounded turn velocity, vanilla block collision and explosion
    }
    public static boolean marked(class_1297 e){return e!=null&&e.method_5752().stream().anyMatch(t->t.startsWith(TAG));}
    private static final Set<UUID> SPAWNING=new HashSet<>();
    private boolean spawn(class_1297 e){SPAWNING.add(e.method_5667());try{return boss.world.method_8649(e);}finally{SPAWNING.remove(e.method_5667());}}
    public static void loaded(class_1297 e){if(marked(e)&&!SPAWNING.contains(e.method_5667()))e.method_31472();}
    public static boolean allowDamage(class_1309 target,class_1282 source){
        if(source==null)return true;var direct=source.method_5526();if(direct==null)return true;var shot=SHOTS.get(direct.method_5667());
        // Only this Boss's tracked skulls are filtered. Marked summons (wither skeletons, short
        // clouds) and vanilla skulls keep their own damage: the tag exists for cleanup and loot,
        // never as a blanket damage immunity.
        if(shot==null)return true;
        return shot.effect.boss.active()&&!BossDomains.protectedBy(shot.effect.boss,target)&&!shot.wave.quota.spent(target.method_5667());
    }
    public static void hit(class_1309 target,class_1282 source){if(source==null||source.method_5526()==null)return;var shot=SHOTS.get(source.method_5526().method_5667());if(shot!=null)shot.wave.quota.hit(target.method_5667());}
    void clear(){
        for(var shot:List.copyOf(SHOTS.values()))if(shot.effect==this){shot.entity.method_31472();SHOTS.remove(shot.entity.method_5667());}
        for(var wave:waves){for(var mob:wave.skeletons)if(!mob.method_31481())mob.method_31472();for(var cloud:wave.clouds)cloud.method_31472();EffectLeaseManager.revokeSession(wave.lease,ServerClock.now());wave.deaths.clear();}
        waves.clear();
    }
    private void environment(){
        var c=BossDomainConfig.get();if(!c.environmentChanges)return;
        int r=(int)Math.ceil(boss.radius),width=r*2+1,writes=0;
        // Fixed-size streaming cursor relative to the current position. No unbounded queue,
        // no stale jobs after the Boss moves; each candidate is checked again before writing.
        for(int i=0;i<Math.min(256,c.environmentChecks)&&writes<Math.min(32,c.environmentWrites);i++){
            long n=environmentCursor++%(width*width*17L);int x=(int)(n%width)-r,z=(int)(n/width%width)-r,y=(int)(n/(width*width))-8;
            var p=class_2338.method_49638(boss.center).method_10069(x,y,z);writes+=BossEnvironment.change(boss,p);
        }
    }
    private static final class DomainSkull extends class_1687 {
        DomainSkull(class_1937 world,class_1309 owner,class_243 direction){super(world,owner,direction);}
        @Override protected void method_7454(class_3966 hit){
            var shot=SHOTS.get(method_5667());
            if(shot==null||!(hit.method_17782() instanceof class_1309 target)||!allowDamage(target,method_48923().method_48806(this,shot.effect.boss.entity)))return;
            var world=shot.effect.boss.world;var owner=shot.effect.boss.entity;
            var source=method_48923().method_48806(this,owner);
            if(!target.method_64397(world,source,8))return;
            if(target.method_5805())class_1890.method_60107(world,target,source);else owner.method_6025(5);
            int duration=world.method_8407()==class_1267.field_5807?800:world.method_8407()==class_1267.field_5802?200:0;
            if(duration>0){
                EffectLeaseManager.apply(target,"minecraft:wither","boss_skull",shot.wave.lease,duration,1,ServerClock.now());
                shot.wave.affected.add(target);
            }
        }
        @Override protected boolean method_26958(class_1297 target){var s=SHOTS.get(method_5667());return super.method_26958(target)&&(s==null||!BossDomains.protectedBy(s.effect.boss,target)&&!s.wave.quota.spent(target.method_5667()));}
    }
}
