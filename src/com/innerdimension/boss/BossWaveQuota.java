package com.innerdimension.boss;
import java.util.*;
/** Reservations can move; a successful hit remains spent for the complete wave. */
public final class BossWaveQuota {
    private final Set<UUID> reserved=new HashSet<>(),hit=new HashSet<>();
    public boolean available(UUID id){return !reserved.contains(id)&&!hit.contains(id);}
    public boolean reserve(UUID id){return available(id)&&reserved.add(id);}
    public void release(UUID id){reserved.remove(id);}
    public boolean spent(UUID id){return hit.contains(id);}
    public void hit(UUID id){hit.add(id);reserved.remove(id);}
}
