package com.innerdimension.mixin;

import com.innerdimension.client.crest.BellyCrestLayer;
import com.nonid.client.PlayerBellyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

@Mixin(value=PlayerBellyRenderer.class,remap=false)
public abstract class NonBellyCrestMixin {
    @SuppressWarnings({"rawtypes","unchecked"})
    @Inject(method="<init>",at=@At("TAIL"))
    private void inner$attachSurfaceLayer(CallbackInfo ci){
        GeoObjectRenderer renderer=(GeoObjectRenderer)(Object)this;
        renderer.withRenderLayer(new BellyCrestLayer(renderer));
    }
}
