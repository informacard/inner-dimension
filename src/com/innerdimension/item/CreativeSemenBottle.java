package com.innerdimension.item;
import java.util.*;
import net.minecraft.*;
/** Uses the same effective source list as NoN's creative bottle variants. */
public final class CreativeSemenBottle {
    private CreativeSemenBottle() {}
    public static boolean isBottle(class_1799 stack){return stack!=null&&!stack.method_7960()&&stack.method_31574(ModItems.CREATIVE_SEMEN_BOTTLE);}
    public static List<class_2960> sources(){
        try {
            var method=Class.forName("com.nonid.NonLiquidSystem").getDeclaredMethod("resolveEffectiveLiquidGainMap");
            method.setAccessible(true);
            Map<?,?> gains=(Map<?,?>)method.invoke(null);
            TreeSet<String> ids=new TreeSet<>();
            for(Object raw:gains.keySet()){
                class_2960 id=class_2960.method_12829(raw.toString());
                if(id!=null&&class_7923.field_41177.method_10250(id)&&!id.toString().equals("minecraft:player"))ids.add(id.toString());
            }
            // NoN represents the bee's source bottle as a honey bottle.
            if(class_7923.field_41177.method_10250(class_2960.method_60654("minecraft:bee")))ids.add("minecraft:bee");
            return ids.stream().map(class_2960::method_60654).toList();
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Unable to resolve NoN bottle sources",e);}
    }
}
