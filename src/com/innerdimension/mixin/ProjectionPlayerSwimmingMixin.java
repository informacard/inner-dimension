package com.innerdimension.mixin;
import com.innerdimension.projection.ProjectionEnvironment;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Players override Entity.updateSwimming; the entity-only hook cannot cover that path. */
@Mixin(class_1657.class)
public abstract class ProjectionPlayerSwimmingMixin {
    @Inject(method="method_5790",at=@At("TAIL"))
    private void inner$projectedSwimming(CallbackInfo ci) {
        class_1657 player=(class_1657)(Object)this;
        if (!player.method_31549().field_7479 && !player.method_5765() && player.method_5624()
                && ProjectionEnvironment.eyesIn(player,class_3486.field_15517)) player.method_5796(true);
    }
}
