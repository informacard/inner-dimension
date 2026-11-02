package com.innerdimension.mixin;

import com.innerdimension.projection.OpenDomainPerformance;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_757.class)
public abstract class OpenDomainRenderTimingMixin {
    @WrapMethod(method="method_3192")
    private void inner$timeScene(class_9779 ticks,boolean render,Operation<Void> original){
        var span=OpenDomainPerformance.span(OpenDomainPerformance.Metric.SCENE_RENDER);
        try{original.call(ticks,render);}finally{OpenDomainPerformance.end(span);}
    }
}
