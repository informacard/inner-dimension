package com.innerdimension.mixin;

import com.innerdimension.client.TeleportDiagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_310;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(class_310.class)
public abstract class TeleportClientTimingMixin {
    @Shadow public class_638 field_1687;

    @WrapMethod(method = "method_1481")
    private void inner$timeWorldChange(class_638 world, Operation<Void> original) {
        TeleportDiagnostics.worldChange(field_1687, world);
        long start = TeleportDiagnostics.begin();
        try { original.call(world); }
        finally { TeleportDiagnostics.span("client.set_world", start); }
    }

    @WrapMethod(method = "method_76796")
    private void inner$timeWorldReset(class_638 world, Operation<Void> original) {
        long start = TeleportDiagnostics.begin();
        try { original.call(world); }
        finally { TeleportDiagnostics.span("client.reset_render_state", start); }
    }

    @WrapMethod(method = "method_1523")
    private void inner$timeFrame(boolean tick, Operation<Void> original) {
        long start = System.nanoTime();
        var performance=com.innerdimension.projection.OpenDomainPerformance.span(com.innerdimension.projection.OpenDomainPerformance.Metric.CLIENT_FRAME);
        if(performance!=null){
            class_310 client=(class_310)(Object)this;
            if(client.method_1493())com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.PAUSED_FRAMES,1);
            if(client.field_1755!=null)com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.MENU_FRAMES,1);
            com.innerdimension.projection.OpenDomainPerformance.count(com.innerdimension.projection.OpenDomainPerformance.Metric.FPS_LIMIT_SUM,client.method_61964().method_61937());
        }
        try { original.call(tick); }
        finally { TeleportDiagnostics.frame(start);com.innerdimension.projection.OpenDomainPerformance.end(performance); }
    }
}
