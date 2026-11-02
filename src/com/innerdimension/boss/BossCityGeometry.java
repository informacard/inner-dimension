package com.innerdimension.boss;
import net.minecraft.*;

/** Resolves only already-loaded structure references and start chunks. Never generates chunks. */
final class BossCityGeometry {
    static void locate(BossEncounter b){
        if(b.city!=null)return;
        var p=class_2338.method_49638(b.spawn);
        var chunk=b.world.method_14178().method_21730(p.method_10263()>>4,p.method_10260()>>4);
        if(chunk!=null){
            var registry=b.world.method_30349().method_30530(class_7924.field_41246);
            for(var item:chunk.method_12179().entrySet()){
                if(!class_2960.method_60655("minecraft","ancient_city").equals(registry.method_10221(item.getKey())))continue;
                for(long ref:item.getValue()){
                    var cp=new class_1923(ref);var startChunk=b.world.method_14178().method_21730(cp.field_9181,cp.field_9180);
                    if(startChunk==null){
                        b.city="city:"+cp.field_9181+":"+cp.field_9180;
                        var existing=BossDomains.BOSSES.values().stream().filter(x->x!=b&&b.worldId.equals(x.worldId)&&b.city.equals(x.city)).findFirst().orElse(null);
                        b.center=existing==null?new class_243(cp.field_9181*16+8,b.spawn.field_1351,cp.field_9180*16+8):existing.center;
                        b.radius=128;b.vertical=48;return;
                    }
                    var start=startChunk.method_12181(item.getKey());
                    if(start==null||!start.method_16657()||!start.method_14969().method_14662(p))continue;
                    var center=start.method_14969().method_22874();
                    b.city="city:"+cp.field_9181+":"+cp.field_9180;
                    b.center=new class_243(center.method_10263()+.5,center.method_10264(),center.method_10260()+.5);b.radius=128;b.vertical=48;return;
                }
            }
        }
        // Reuse overlapping fallback regions instead of quantising across a grid boundary.
        for(var other:BossDomains.BOSSES.values())if(other!=b&&other.warden()&&other.city!=null&&other.worldId.equals(b.worldId)&&other.city.startsWith("fallback:")&&Math.abs(other.center.field_1351-b.spawn.field_1351)<=other.vertical+32&&Math.hypot(other.center.field_1352-b.spawn.field_1352,other.center.field_1350-b.spawn.field_1350)<=other.radius+64){
            b.city=other.city;b.center=other.center;b.radius=other.radius;b.vertical=other.vertical;return;
        }
        b.city="fallback:"+b.id;b.center=b.spawn;b.radius=64;b.vertical=32;
    }
}
