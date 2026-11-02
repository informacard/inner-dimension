package com.innerdimension.mixin;
import com.innerdimension.entity.*;
import com.innerdimension.progression.PistonProgress;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_1297.class)
public abstract class ProgressPistonMixin {
    @Inject(method="method_5784", at=@At("HEAD"))
    private void inner$beforeMovement(class_1313 type, class_243 delta, CallbackInfo ci) {
        if ((Object)this instanceof class_3222 p && delta.field_1351 < 0)
            com.innerdimension.progression.DomainRitual.movement(p);
    }
    @Inject(method="method_5784", at=@At("TAIL"))
    private void inner$pushed(class_1313 type, class_243 delta, CallbackInfo ci) {
        var entity = (class_1297)(Object)this;
        if (type == class_1313.field_6310 && !entity.method_73183().method_8608()
                && (entity instanceof EnderPearlDildoEntity || entity instanceof SensoryLinkDollEntity)
                && delta.method_1027() > 0) PistonProgress.record(entity);
    }
}
