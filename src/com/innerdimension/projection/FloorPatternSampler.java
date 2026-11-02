package com.innerdimension.projection;

import net.minecraft.*;
import java.util.*;

/** Shared by the server generator and the client's flat preview. */
public final class FloorPatternSampler {
    private final class_2680[][] tile;
    private final FloorPatternSettings settings;
    private final PolarExpression.Node expression;
    private final class_2680 foreground, background;
    public FloorPatternSampler(class_2680[][] tile, FloorPatternSettings settings) {
        this.tile=tile; this.settings=settings; expression=settings.compile();
        Map<class_2248,Integer> counts=new LinkedHashMap<>();
        Map<class_2248,class_2680> states=new LinkedHashMap<>();
        // Only the outer four rows supply automatic materials; centre ornaments are excluded.
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)if(x<4||x>=12||z<4||z>=12) {
            class_2680 s=tile[x][z];counts.merge(s.method_26204(),1,Integer::sum);states.putIfAbsent(s.method_26204(),s);
        }
        var sorted=counts.entrySet().stream().sorted(Map.Entry.<class_2248,Integer>comparingByValue().reversed()).toList();
        class_2680 bg=states.get(sorted.get(0).getKey()),fg=sorted.size()>1?states.get(sorted.get(1).getKey()):bg;
        background=resolve(settings.background(),bg);foreground=resolve(settings.foreground(),fg);
    }
    public static class_2680 resolve(String id,class_2680 fallback) {
        if(id.isBlank())return fallback;
        class_2960 key=class_2960.method_60654(id.strip());
        if(key==null)throw new IllegalArgumentException("Invalid block ID: "+id);
        return class_7923.field_41175.method_17966(key).orElseThrow(()->new IllegalArgumentException("Unknown block: "+id)).method_9564();
    }
    public static void validate(FloorPatternSettings settings) {
        settings.compile();resolve(settings.foreground(),null);resolve(settings.background(),null);
    }
    public class_2680 at(int x,int z) {
        if(settings.mode()==FloorPatternSettings.Mode.TILE)
            return tile[Math.floorMod(x,16)][Math.floorMod(z,16)];
        if(settings.preserveCenter()&&FloorPatternSettings.central(x,z))return tile[x][z];
        return FloorPatternSettings.foreground(expression,x,z)?foreground:background;
    }
    public static class_2680 floorBlock(class_2680 state) {
        var block=state.method_26204();
        if(block==com.innerdimension.block.ModBlocks.FLESH_WATER)return com.innerdimension.block.ModBlocks.SOLID_FLESH_WATER.method_9564();
        if(block==com.innerdimension.block.ModBlocks.VOID_WATER)return com.innerdimension.block.ModBlocks.SOLID_VOID_WATER.method_9564();
        return state;
    }
    public class_2680 foreground() { return foreground; }
    public class_2680 background() { return background; }
}
