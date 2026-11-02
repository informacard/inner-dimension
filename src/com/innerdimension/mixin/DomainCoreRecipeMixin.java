package com.innerdimension.mixin;
import com.innerdimension.item.ModItems;
import net.minecraft.*;
import nonplayerplushie.SensoryLinkData;
import nonplayerplushie.SensoryLinkItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_1867.class)
public abstract class DomainCoreRecipeMixin {
    @Inject(method="method_17729",at=@At("RETURN"),cancellable=true)
    private void inner$bind(class_9694 input,class_7225.class_7874 lookup,CallbackInfoReturnable<class_1799> ci) {
        class_1799 out=ci.getReturnValue();
        if(out.method_7909()!=ModItems.DOMAIN_CORE) return;
        for(class_1799 stack:input.method_59989()) if(stack.method_7909()==SensoryLinkItems.SENSATION_CORE) {
            var data=SensoryLinkData.read(stack);
            if(SensoryLinkData.valid(data)) { SensoryLinkData.write(out,data); return; }
        }
        ci.setReturnValue(class_1799.field_8037);
    }
}
