package com.innerdimension.mixin;
import com.innerdimension.progression.*;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Observe accepted client movement, after vanilla's provisional move and position restoration. */
@Mixin(class_3244.class)
public abstract class RitualMovementPacketMixin {
    @Shadow public class_3222 field_14140;
    @Inject(method="method_12063",at=@At("HEAD"))
    private void inner$impalementBefore(class_2828 packet,CallbackInfo ci){com.innerdimension.voiddomain.VoidEmbodiment.beforeMovement(field_14140);}
    @Inject(method="method_12063",at=@At("TAIL"))
    private void inner$acceptedMovement(class_2828 packet,CallbackInfo ci) {
        com.innerdimension.voiddomain.VoidEmbodiment.acceptedMovement(field_14140);
        DomainRitual.packetComplete(field_14140);
    }
}
