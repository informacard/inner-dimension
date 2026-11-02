package com.innerdimension.mixin;
import com.innerdimension.client.RitualInput;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_309.class)
public abstract class RitualKeyboardMixin {
    @Inject(method="method_1466",at=@At("HEAD"),cancellable=true)
    private void inner$ritualKey(long window,int action,class_11908 key,CallbackInfo ci){
        if(action!=0&&RitualInput.active()&&class_310.method_1551().field_1755==null&&key.comp_4795()!=256)ci.cancel();
    }
}
