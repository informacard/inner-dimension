package com.innerdimension.domain.runtime;

import com.innerdimension.projection.ProjectionAnimation;
import com.innerdimension.projection.ProjectionIndex;
import com.innerdimension.projection.RoomProjectionEntity;
import net.minecraft.*;

import java.util.*;

/**
 * One expansion of one domain: an immutable loadout snapshot plus the fixed geometric
 * maximum bounds captured when the ability session starts. Open-domain membership follows the animated radius. Nothing here is re-read from the
 * engraving UI after construction, so switching slot / renaming / reopening the UI can
 * never change an in-flight ability or refresh one of its counters.
 */
public final class DomainSession {
    /** Fresh per expansion. The only key a once-per-expansion counter may use. */
    public final UUID sessionId = UUID.randomUUID();
    public final UUID ownerId;
    public final long startTick;
    /** Entity ids as engraved, e.g. {@code minecraft:chicken}. Immutable. */
    public final List<String> loadout;
    /** Short codes, e.g. {@code D04}. Immutable. */
    public final Set<String> codes;
    public final class_1937 world;
    public final RoomProjectionEntity shell;
    public final RoomProjectionEntity interior;

    private double minX, minY, minZ, maxX, maxY, maxZ;
    private boolean stable;
    private boolean ended;
    private long stableTick = -1;

    public final Set<UUID> activeObjects=new HashSet<>();
    private final Map<String,Integer> objectMasks=new HashMap<>();
    private final Map<String, Object> state = new HashMap<>();
    private final Map<String, Map<UUID, Long>> timers = new HashMap<>();
    private final Set<String> flags = new HashSet<>();

    public DomainSession(UUID ownerId, class_1937 world, RoomProjectionEntity shell, RoomProjectionEntity interior,
                         Collection<String> loadout) {
        this(ownerId,world,shell,interior,loadout,-1);
    }
    public DomainSession(UUID ownerId, class_1937 world, RoomProjectionEntity shell, RoomProjectionEntity interior,
                         Collection<String> loadout,int expansionSlot) {
        this.ownerId = ownerId;
        this.world = world;
        this.shell = shell;
        this.interior = interior;
        this.startTick = world.method_75260();
        this.loadout = List.copyOf(loadout);
        LinkedHashSet<String> c = new LinkedHashSet<>();
        for (String id : this.loadout) {
            var ability = com.innerdimension.domain.DomainAbilityCatalog.byEntity(id);
            if (ability != null) c.add(ability.code());
        }
        this.codes = Collections.unmodifiableSet(c);
        if(world instanceof class_3218 serverWorld){var saved=com.innerdimension.progression.DomainState.get(serverWorld);int slot=expansionSlot<0?saved.selectedSlot(ownerId):expansionSlot;for(String id:this.loadout){var ability=com.innerdimension.domain.DomainAbilityCatalog.byEntity(id);if(ability!=null){int mask=saved.objectMask(ownerId,slot,id);if(ability.code().equals("D71")&&(owner()==null||!com.innerdimension.progression.InnerAdvancements.done(owner(),"void_baptism")))mask=-1;objectMasks.put(ability.code(),mask<0?DomainObjects.defaultMask(ability.code()):mask);}}}

    }

    /* ------------------------------------------------------------------ state */

    public boolean open(){return ((com.innerdimension.projection.RoomProjectionContraption)shell.getContraption()).sourceSpec().shell()==null && !has(com.innerdimension.domain.DomainContest.SIMPLE);}

    public int objectMask(String code){return objectMasks.getOrDefault(code,DomainObjects.defaultMask(code));}
    public boolean has(String entityId) { return loadout.contains(entityId); }
    public boolean hasCode(String code) { return codes.contains(code); }

    @SuppressWarnings("unchecked")
    public <T> T state(String key, T fallback) {
        Object v = state.get(key);
        return v == null ? fallback : (T) v;
    }
    public void putState(String key, Object value) { if (value == null) state.remove(key); else state.put(key, value); }
    public Map<String, Object> states() { return state; }

    public boolean flag(String key) { return flags.contains(key); }
    public void flag(String key, boolean value) { if (value) flags.add(key); else flags.remove(key); }

    /** Simple accumulating counter, e.g. a fractional NoN delta remainder. */
    public double accumulate(String key, double delta) {
        double next = state(key, Double.valueOf(0.0)) + delta;
        putState(key, next);
        return next;
    }

    /** Remaining ticks for a per-target cooldown, or 0 when due. Never negative. */
    public long remaining(String key, UUID target, long interval, long now) {
        Map<UUID, Long> map = timers.get(key);
        if (map == null) return 0;
        Long armed = map.get(target);
        if (armed == null) return 0;
        return Math.max(0, armed + interval - now);
    }

    public boolean due(String key, UUID target, long interval, long now) {
        return remaining(key, target, interval, now) <= 0;
    }

    public void arm(String key, UUID target, long now) { timers.computeIfAbsent(key, k -> new HashMap<>()).put(target, now); }
    public void clearTimer(String key, UUID target) {
        Map<UUID, Long> map = timers.get(key);
        if (map != null) map.remove(target);
    }
    /** Shared cooldown for the whole session (the owner is the implicit target). */
    public boolean dueSelf(String key, long interval, long now) { return due(key, ownerId, interval, now); }
    public void armSelf(String key, long now) { arm(key, ownerId, now); }
    public long remainingSelf(String key, long interval, long now) { return remaining(key, ownerId, interval, now); }

    /** Once per expansion, keyed by sessionId. Independent of slot switching. */
    public boolean once(String key) { return flags.add(key); }

    /* --------------------------------------------------------------- geometry */

    /** Called exactly once at activation (expansion start for open domains). */
    public void capture(long now) {
        class_238 box = shell.method_5829();
        // The snapshot's own volume includes the one-block shell on every face and the
        // floor layer at local y=-1; the walkable interior is that volume shrunk by one.
        this.minX = box.field_1323 + 1;
        this.minY = box.field_1322 + 1;
        this.minZ = box.field_1321 + 1;
        this.maxX = box.field_1320 - 1;
        this.maxY = box.field_1325 - 1;
        this.maxZ = box.field_1324 - 1;
        if(open() || has(com.innerdimension.domain.DomainContest.SIMPLE)) {
            this.minX=box.field_1323;this.minY=box.field_1322;this.minZ=box.field_1321;
            this.maxX=box.field_1320;this.maxY=box.field_1325;this.maxZ=box.field_1324;
        }
        this.stable = true;
        this.stableTick = now;
    }

    public boolean stable() { return stable; }
    public boolean ended() { return ended; }
    public void markEnded() { ended = true; }
    public long stableTick() { return stableTick; }
    public long age(long now) { return Math.max(0, now - (stableTick < 0 ? startTick : stableTick)); }
    public long age() { return age(world.method_75260()); }

    public double minX() { return minX; }
    public double minY() { return minY; }
    public double minZ() { return minZ; }
    public double maxX() { return maxX; }
    public double maxY() { return maxY; }
    public double maxZ() { return maxZ; }
    public double centerX() { return (minX + maxX) * 0.5; }
    public double centerY() { return (minY + maxY) * 0.5; }
    public double centerZ() { return (minZ + maxZ) * 0.5; }
    public double radius() { if(open())return Math.min(maxX-minX,maxZ-minZ)*.5*interior.progress(0); return Math.min(maxX - minX, maxZ - minZ) * 0.5; }

    public class_238 bounds() { if(open()){double r=radius();return new class_238(centerX()-r,centerY()-r,centerZ()-r,centerX()+r,centerY()+r,centerZ()+r);}return new class_238(minX, minY, minZ, maxX, maxY, maxZ); }
    public class_238 bounds(double grow) { return bounds().method_1014(grow); }

    public boolean containsPoint(double x, double y, double z) {
        if(open()&&!com.innerdimension.domain.DomainContest.allowsPoint(ownerId,x,y,z))return false;
        if(open())return com.innerdimension.projection.OpenDomainRules.contains(x-centerX(),y-centerY(),z-centerZ(),radius());
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public boolean contains(class_1297 entity) {
        if (entity == null || entity.method_7325() || com.innerdimension.domain.ability.D77.shell(entity)) return false;
        if(com.innerdimension.domain.DomainContest.protectedFromOpen(ownerId,entity))return false;
        if(com.innerdimension.domain.DomainContest.suppressed(ownerId))return false;
        if(com.innerdimension.domain.SimpleDomain.active(entity)&&!ownerId.equals(entity.method_5667()))return false;
        if (entity.method_73183() != world) return false;
        if(!open()&&com.innerdimension.domain.SimpleDomain.locked(ownerId,entity))return true;
        return contains(entity.method_5829());
    }

    public boolean contains(class_238 box) {
        var contest=com.innerdimension.domain.DomainContest.entry(ownerId);
        if(open()&&contest!=null&&!contest.geometryContains(box))return false;
        if(open())return com.innerdimension.projection.OpenDomainRules.intersects(box.field_1323-centerX(),box.field_1322-centerY(),box.field_1321-centerZ(),box.field_1320-centerX(),box.field_1325-centerY(),box.field_1324-centerZ(),radius());
        return box.field_1320 > minX && box.field_1323 < maxX
                && box.field_1325 > minY && box.field_1322 < maxY
                && box.field_1324 > minZ && box.field_1321 < maxZ;
    }

    /** True when the whole block cell belongs to the interior. */
    public boolean containsBlock(class_2338 pos) {
        if(open()&&(!com.innerdimension.domain.DomainContest.allowsPoint(ownerId,pos.method_10263(),pos.method_10264(),pos.method_10260())||!com.innerdimension.domain.DomainContest.allowsPoint(ownerId,pos.method_10263()+1-1e-7,pos.method_10264()+1-1e-7,pos.method_10260()+1-1e-7)))return false;
        if(open())return com.innerdimension.projection.OpenDomainRules.farthest(pos.method_10263()-centerX(),pos.method_10264()-centerY(),pos.method_10260()-centerZ())<=radius();
        return pos.method_10263() >= Math.floor(minX) && pos.method_10263() + 1 <= Math.ceil(maxX)
                && pos.method_10264() >= Math.floor(minY) && pos.method_10264() + 1 <= Math.ceil(maxY)
                && pos.method_10260() >= Math.floor(minZ) && pos.method_10260() + 1 <= Math.ceil(maxZ);
    }

    /** True when any part of the block cell overlaps the interior. */
    public boolean intersectsBlock(class_2338 pos) {
        return contains(new class_238(pos));
    }

    public Iterable<class_2338> blocksIn() {
        List<class_2338> out = new ArrayList<>();
        class_238 area=bounds();
        for (int x = (int) Math.floor(area.field_1323); x < (int) Math.ceil(area.field_1320); x++)
            for (int y = (int) Math.floor(area.field_1322); y < (int) Math.ceil(area.field_1325); y++)
                for (int z = (int) Math.floor(area.field_1321); z < (int) Math.ceil(area.field_1324); z++) {
                    class_2338 pos=new class_2338(x,y,z);
                    if(!open()||containsBlock(pos))out.add(pos);
                }
        return out;
    }

    public boolean phaseActive() { return open()?interior.progress(0)>0||shell.phase()!=ProjectionAnimation.RETRACTING:shell.phase() != ProjectionAnimation.RETRACTING; }

    public class_1309 ownerEntity() { if(world instanceof class_3218 serverWorld&&serverWorld.method_73284(ownerId) instanceof class_1309 living&&living.method_5805())return living;return owner(); }
    public class_3222 owner() {
        var player = world.method_8503().method_3760().method_14602(ownerId);
        return player != null && player.method_5805() && player.method_73183() == world ? player : null;
    }

    public boolean ownerValid() { return owner() != null; }

    /** Entities currently inside the interior, excluding spectators and the shell itself. */
    public List<class_1309> livingInside() {
        List<class_1309> out = new ArrayList<>(world.method_8390(class_1309.class, bounds(),
                e -> e.method_5805() && !e.method_7325() && !com.innerdimension.boss.BossDomains.domainImmune(e) && contains(e)));
        // Belt and braces: the class query must never be the only way to see the owner. The
        // previous predicate used method_31747 (Entity.isPlayer, constant true for players)
        // where method_7325 (Entity.isSpectator) was meant, which filtered every player out.
        class_3222 owner = owner();
        if (owner != null && !out.contains(owner) && contains(owner)) out.add(owner);
        for(var locked:com.innerdimension.domain.SimpleDomain.lockedLiving(ownerId))if(!out.contains(locked)&&(!open()||contains(locked)))out.add(locked);
        if(hasCode("D70")&&com.innerdimension.domain.DomainModes.reverse(this,"D70"))out.removeIf(entity->!ownerId.equals(entity.method_5667()));
        return out;
    }

    /** Diagnostic view of the membership test for one entity, used by the ability probes. */
    public String membership(class_1297 entity) {
        if (entity == null) return "entity=null";
        return "alive=" + entity.method_5805() + " spectator=" + entity.method_7325()
                + " isPlayer=" + entity.method_31747()
                + " sameWorld=" + (entity.method_73183() == world)
                + " contains=" + contains(entity)
                + " box=" + entity.method_5829();
    }

    /** Stable key for logs and for cross-session arbitration. */
    public String key() { return ownerId + "#" + sessionId; }

    @Override public String toString() {
        return "DomainSession{" + key() + ", box=[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "]}";
    }

    /** True when this session's shell is still the tracked projection in the world. */
    public boolean valid() {
        return !ended && !shell.method_7325() && !interior.method_7325()
                && ProjectionIndex.active(world, ownerId) == shell;
    }
}
