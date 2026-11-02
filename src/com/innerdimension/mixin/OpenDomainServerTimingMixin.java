package com.innerdimension.mixin;

import com.innerdimension.projection.OpenDomainPerformance;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class OpenDomainServerTimingMixin {
    @WrapMethod(method="method_3748")
    private void inner$timeServerTick(BooleanSupplier shouldKeepTicking,Operation<Void> original){
        var span=OpenDomainPerformance.span(OpenDomainPerformance.Metric.SERVER_TICK);
        try{original.call(shouldKeepTicking);}finally{OpenDomainPerformance.end(span);}
    }
}
