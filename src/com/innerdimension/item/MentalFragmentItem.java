package com.innerdimension.item;
import net.minecraft.*;
import com.innerdimension.block.ModBlocks;
public final class MentalFragmentItem extends class_1792 {
    private final boolean voidVariant;
    public MentalFragmentItem(class_1793 settings){this(settings,false);}
    public MentalFragmentItem(class_1793 settings,boolean voidVariant){super(settings);this.voidVariant=voidVariant;}
    @Override public class_1269 method_7884(class_1838 context){
        class_2350 face=context.method_8038();
        if(face!=class_2350.field_11036 && face!=class_2350.field_11033)return class_1269.field_5811;
        class_1747 placer=(class_1747)(face==class_2350.field_11036?(voidVariant?ModItems.VOID_WATER_ITEM:ModItems.FLESH_WATER_ITEM):(voidVariant?ModItems.VOID_MIST_ITEM:ModItems.SOUL_MIST_ITEM));
        return placer.method_7712(new class_1750(context));
    }
}
