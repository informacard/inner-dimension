package com.innerdimension.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Borrow vanilla's post chain for the world pass; restore spectator post effects afterwards. */
@Mixin(class_757.class)
public abstract class VoidAfterimageMixin {
 @Shadow private class_2960 field_53898;
 @Shadow private boolean field_4013;
 @Unique private class_2960 inner$previous;
 @Unique private boolean inner$enabled,inner$borrowed;
 @Inject(method="method_3192",at=@At("HEAD"))
 private void inner$begin(class_9779 ticks,boolean tick,CallbackInfo ci){
  var player=class_310.method_1551().field_1724;
  inner$borrowed=player!=null&&player.method_6059(class_7923.field_41174.method_47983(com.innerdimension.effect.ModStatusEffects.VOID_EMBODIMENT));
  if(inner$borrowed){inner$previous=field_53898;inner$enabled=field_4013;field_53898=class_2960.method_60655("inner_dimension","void_afterimage");field_4013=true;}
 }
 @Inject(method="method_3192",at=@At("RETURN"))
 private void inner$end(class_9779 ticks,boolean tick,CallbackInfo ci){if(inner$borrowed){field_53898=inner$previous;field_4013=inner$enabled;inner$borrowed=false;}}
}
