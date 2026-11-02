package com.innerdimension.boss;
import com.google.gson.*;
import com.innerdimension.core.ServerClock;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Versioned world-local metadata, atomic replacement. Ordinary active casts never resume. */
final class BossPersistence {
    static final class FileData {int version=1;List<Row> bosses=new ArrayList<>();}
    static final class Row {
        UUID id;int kind,nodes,spread;String world,city;double x,y,z,sx,sy,sz,ax,ay,az,radius,vertical;
        long cast,burnout,disabled,warning;boolean registered,pending,crystalBaselineReady;List<UUID> crystals=new ArrayList<>();
    }
    private static Path path(MinecraftServer s){return s.method_27050(class_5218.field_24188).resolve("data/inner_dimension_boss_domains.json");}
    static FileData decode(String json){var data=new Gson().fromJson(json,FileData.class);if(data==null||data.version!=1||data.bosses==null)throw new IllegalStateException("Unsupported Boss domain persistence version");return data;}
    static void load(MinecraftServer s){
        Path p=path(s);if(!Files.exists(p))return;long now=ServerClock.now();
        try{
            var data=decode(Files.readString(p));
            for(var r:data.bosses){
                if(r.id==null||r.world==null||r.kind<0||r.kind>2)continue;
                var b=BossDomains.BOSSES.get(r.id);if(b==null)b=new BossEncounter(r.id,r.kind,r.world);
                b.center=new class_243(r.x,r.y,r.z);b.spawn=new class_243(r.sx,r.sy,r.sz);b.appearance=new class_243(r.ax,r.ay,r.az);b.radius=r.radius;b.vertical=r.vertical;
                b.city=r.city;b.cast=r.cast;b.nodes=r.nodes;b.spreadCount=r.spread;b.pending=r.pending;b.halfPhasePending=r.kind==1&&(r.nodes&2)!=0&&r.pending;b.crystalBaselineReady=r.crystalBaselineReady;b.crystals.clear();if(r.crystals!=null)b.crystals.addAll(r.crystals);
                b.burnoutUntil=now+Math.max(0,r.burnout);b.disabledUntil=now+Math.max(0,r.disabled);
                if(r.kind==2&&r.registered){b.registered=true;b.warningUntil=now+Math.max(0,r.warning);b.activeAt=b.warningUntil;b.nextPulse=Math.max(b.activeAt,b.disabledUntil)+BossDomainConfig.get().wardenPulseTicks;b.awake=false;}
                BossDomains.BOSSES.put(r.id,b);
            }
        }catch(IOException|JsonParseException e){throw new IllegalStateException("Cannot restore Boss domain metadata "+p,e);}
    }
    static void save(MinecraftServer s){
        var data=new FileData();long now=ServerClock.now();
        for(var b:BossDomains.BOSSES.values())if(!b.removed&&b.center!=null&&b.spawn!=null){
            var r=new Row();r.id=b.id;r.kind=b.kind;r.world=b.worldId;r.city=b.city;r.x=b.center.field_1352;r.y=b.center.field_1351;r.z=b.center.field_1350;
            r.sx=b.spawn.field_1352;r.sy=b.spawn.field_1351;r.sz=b.spawn.field_1350;r.ax=b.appearance.field_1352;r.ay=b.appearance.field_1351;r.az=b.appearance.field_1350;r.radius=b.radius;r.vertical=b.vertical;r.nodes=b.nodes;r.spread=b.spreadCount;r.cast=b.cast;r.pending=b.pending;r.crystalBaselineReady=b.crystalBaselineReady;
            r.crystals.addAll(b.crystals);r.registered=b.warden()&&b.registered;r.warning=Math.max(0,b.warningUntil-now);r.disabled=Math.max(0,b.disabledUntil-now);
            r.burnout=Math.max(0,b.burnoutUntil-now);
            if(!b.warden()&&b.registered)r.burnout=Math.max(r.burnout,b.kind==0?BossDomainConfig.get().dragonBurnout:BossDomainConfig.get().witherBurnout);
            data.bosses.add(r);
        }
        Path p=path(s),tmp=p.resolveSibling(p.getFileName()+".tmp");
        try{Files.createDirectories(p.getParent());Files.writeString(tmp,new GsonBuilder().setPrettyPrinting().create().toJson(data));
            try{Files.move(tmp,p,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,p,StandardCopyOption.REPLACE_EXISTING);}
        }catch(IOException e){throw new IllegalStateException("Cannot save Boss domain metadata "+p,e);}
    }
}
