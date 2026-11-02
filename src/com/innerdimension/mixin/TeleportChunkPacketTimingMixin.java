package com.innerdimension.mixin;

import com.innerdimension.client.TeleportDiagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_2672;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_634.class)
public abstract class TeleportChunkPacketTimingMixin {
    @WrapMethod(method = "method_11128")
    private void inner$timeChunkPacket(class_2672 packet, Operation<Void> original) {
        // Netty invokes this handler before vanilla dispatches onto the client thread.
        boolean clientThread = net.minecraft.class_310.method_1551().method_18854();
        long start = clientThread ? TeleportDiagnostics.begin() : 0;
        try { original.call(packet); }
        finally { TeleportDiagnostics.span("client.chunk_packet", start); }
    }
}
