package com.innerdimension.mixin;
import com.innerdimension.domain.ability.D35;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.*;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Filter after all raycast providers (including Lithium) finish populating the result. */
@Mixin(class_9892.class)
public abstract class DomainExplosionFilterMixin {
    @org.spongepowered.asm.mixin.injection.Inject(method="method_61737",at=@At("HEAD"),cancellable=true)
    private void inner$noExplosion(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> ci){var explosion=(class_1927)(Object)this;if(com.innerdimension.domain.ability.D64.locked(explosion.method_64504(),class_2338.method_49638(explosion.method_55109())))ci.setReturnValue(0);}
    @ModifyExpressionValue(method = "method_61737", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/class_9892;method_61740()Ljava/util/List;"))
    private List<class_2338> innerDimension$blastPolicy(List<class_2338> original) {
        return D35.filterBlast((class_1927) (Object) this, original);
    }
}
