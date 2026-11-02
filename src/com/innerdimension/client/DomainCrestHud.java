package com.innerdimension.client;

import com.innerdimension.InnerDimension;
import com.innerdimension.client.crest.DomainCrestClient;
import com.innerdimension.client.crest.CrestHudAppearance;
import com.innerdimension.config.InnerConfig;
import com.innerdimension.projection.ProjectionIndex;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.class_10799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

/** Small unlit, translucent owner crest above the experience bar. */
public final class DomainCrestHud {
    private DomainCrestHud() {}

    public static void register() {
        // Inherits the vanilla HUD visibility condition (including F1).
        // The experience-level render method is conditional; HOTBAR also runs without XP.
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,
                InnerDimension.id("domain_crest_hud"), DomainCrestHud::render);
    }

    private static void render(class_332 graphics, class_9779 ticks) {
        var client=class_310.method_1551();
        if(client.field_1724==null||client.field_1687==null)return;
        var owner=client.field_1724.method_5667();
        if(!DomainCrestClient.active(owner))return;
        var projection=ProjectionIndex.active(client.field_1687,owner);
        var cfg=InnerConfig.get();
        var appearance=CrestHudAppearance.idle(cfg.crestHudScale,cfg.crestHudIdleOpacity);
        if(DomainCrestClient.burnedOut(owner)){
            appearance=CrestHudAppearance.idle(cfg.crestHudScale*.9,3);
        } else if(projection!=null){
            var frame=DomainCrestFeature.animation(projection,ticks.method_60637(true));
            float peak=Math.max(projection.crestScale(0),
                    Math.max(projection.crestScale(1),projection.crestScale(2)));
            appearance=CrestHudAppearance.projected(frame,projection.crestScale(0),peak,cfg.crestHudScale,cfg.crestHudIdleOpacity);
        }
        int fertility = -1;
        for (var effect : client.field_1724.method_6026()) {
            if (effect.method_5579().comp_349() == com.nonid.effect.NonStatusEffects.FERTILITY_INCREASER)
                fertility = Math.max(fertility, effect.method_5578());
        }
        double animationTicks = client.field_1724.field_6012 + ticks.method_60637(true);
        appearance = CrestHudAppearance.fertile(appearance, animationTicks, fertility);
        var texture=DomainCrestFeature.texture(owner);
        if(texture==null)return;
        int height=appearance.height(),width=appearance.width();
        int x=(graphics.method_51421()-width)/2+cfg.crestHudOffsetX;
        int y=graphics.method_51443()-cfg.crestHudHeightFromBottom-height/2;
        // GUI_TEXTURED has alpha blending and no entity lighting; retain the same texture.
        // Designs have a fixed 3:2 aspect ratio; normalized UVs sample the whole image.
        var matrices = graphics.method_51448();
        matrices.pushMatrix();
        matrices.translate(0, CrestHudAppearance.fertilitySway(animationTicks, fertility));
        graphics.method_25293(class_10799.field_56883,texture,x,y,0,0,width,height,
                3,2,3,2,(appearance.alpha()<<24)|0xffffff);
        matrices.popMatrix();
    }
}
