package com.innerdimension.mixin;
import com.innerdimension.projection.RoomProjectionEntity;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Shell melee uses surface ray picking, never the enclosing entity volume. */
@Mixin(RoomProjectionEntity.class)
public abstract class DomainShellPickingMixin {
 @Inject(method="method_5863",at=@At("HEAD"),cancellable=true)
 private void inner$outsideOnly(CallbackInfoReturnable<Boolean> ci){
  ci.setReturnValue(false);
 }
}
