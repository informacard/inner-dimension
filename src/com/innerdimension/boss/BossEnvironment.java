package com.innerdimension.boss;
import com.innerdimension.chunk.LoadedChunks;
import net.minecraft.*;
import java.util.*;
import java.util.function.BiPredicate;

/** Bounded environmental work and a fail-closed claim integration hook. */
public final class BossEnvironment {
    private static final List<BiPredicate<class_3218,class_2338>> GUARDS=new ArrayList<>();
    public static void registerProtection(BiPredicate<class_3218,class_2338> mayChange){GUARDS.add(mayChange);}
    static boolean allowed(class_3218 w,class_2338 p){
        if(!BossDomainConfig.get().environmentChanges||!LoadedChunks.contains(w,p)||!w.method_64395().method_76185(class_1928.field_19388))return false;
        if(GUARDS.isEmpty()){var loader=net.fabricmc.loader.api.FabricLoader.getInstance();for(String id:List.of("ftbchunks","flan","openpartiesandclaims","griefdefender","landclaim"))if(loader.isModLoaded(id))return false;}
        for(var guard:GUARDS)if(!guard.test(w,p))return false;
        return true;
    }
    private static final class_6862<class_2248> FLOWERS=class_6862.method_40092(class_7924.field_41254,class_2960.method_60655("minecraft","flowers"));
    static class_2248 block(String name){return class_7923.field_41175.method_10223(class_2960.method_60655("minecraft",name)).orElseThrow().comp_349();}
    static int change(BossEncounter b,class_2338 p){
        if(!b.contains(new class_243(p.method_10263()+.5,p.method_10264(),p.method_10260()+.5),false)||!allowed(b.world,p))return 0;
        var s=b.world.method_8320(p);String id=class_7923.field_41175.method_10221(s.method_26204()).method_12832();
        class_2680 next=null;
        if(id.equals("short_grass")||id.equals("tall_grass"))next=block("air").method_9564();
        else if(s.method_26164(FLOWERS)&&!id.contains("potted")&&!id.equals("wither_rose")&&!id.contains("petals")&&!id.contains("wildflower"))next=block("wither_rose").method_9564();
        if(next==null)return 0;
        return b.world.method_8652(p,next,3)?1:0;
    }
    static void spread(BossEncounter b){
        var origin=class_2338.method_49638(b.appearance);
        // Small original SculkSpreader pulse, with no catalyst/shrieker generation. Every
        // possible cell in the bounded charge neighbourhood must pass loaded/claim checks.
        for(int x=-6;x<=6;x++)for(int y=-6;y<=6;y++)for(int z=-6;z<=6;z++)if(!allowed(b.world,origin.method_10069(x,y,z)))return;
        var spreader=new class_7128(false,class_3481.field_37399,10,4,10,5);
        spreader.method_41482(origin,8);
        spreader.method_41479(b.world,origin,b.world.method_8409(),false);
        spreader.method_41494();
    }
}
