package com.innerdimension.mixin;
import net.minecraft.*;
import com.innerdimension.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1542.class)
public class MentalFragmentDropMixin {
    @Inject(method="method_5773",at=@At("TAIL"))
    private void inner$drift(CallbackInfo ci){
        class_1542 item=(class_1542)(Object)this;
        if(item.method_6983().method_7909()!=ModItems.MENTAL_FRAGMENT)return;
        var velocity=item.method_18798();if(velocity.field_1351<-.035)item.method_18799(new class_243(velocity.field_1352,-.035,velocity.field_1350));
        if(item.method_73183() instanceof class_3218 world && item.field_6012%4==0)
            world.method_14199(new class_2390(0xFF88CC,1.0F),false,false,item.method_23317(),item.method_23318()+.15,item.method_23321(),2,.15,.1,.15,0);
    }
}
