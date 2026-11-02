package com.innerdimension.compat;

import net.minecraft.class_2487;

/** Appearance extension on the stable 1.5+ sensory-link NBT, independent of newer helpers. */
public final class PlushieDollData {
    private PlushieDollData(){}
    public static boolean destroyed(class_2487 data){return data!=null&&data.method_68566("destroyed_appearance",false);}
    public static class_2487 withDestroyed(class_2487 data,boolean value){
        var copy=data==null?new class_2487():data.method_10553();
        copy.method_10556("destroyed_appearance",value);return copy;
    }
}
