package com.innerdimension.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;
import net.minecraft.*;
import java.util.Arrays;

/** Bounded screen colour grading and ambient motes, independent of shader-pack pipelines. */
public final class BossDomainVisuals {
    private static final float[] strength=new float[3];
    private static class_1937 world;
    private static long clock;
    private BossDomainVisuals(){}
    private static void clear(){Arrays.fill(strength,0);world=null;clock=0;}
    public static void register(){
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->clear());
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(c.field_1687==null||c.field_1724==null){clear();return;}
            if(world!=c.field_1687){clear();world=c.field_1687;}
            clock++;
            for(int kind=0;kind<3;kind++){
                boolean inside=!c.field_1724.method_7325()&&c.field_1724.method_5805()&&BossDomainHud.inside(kind);
                strength[kind]=Math.max(0,Math.min(1,strength[kind]+(inside?.085f:-.085f)));
                if(!inside||clock%6!=0)continue;
                // One local particle per six ticks and kind; no server entities or packets.
                var random=c.field_1687.method_8409();double angle=random.method_43058()*Math.PI*2;
                double distance=3+random.method_43058()*5;
                double x=c.field_1724.method_23317()+Math.cos(angle)*distance;
                double y=c.field_1724.method_23318()+.5+random.method_43058()*3;
                double z=c.field_1724.method_23321()+Math.sin(angle)*distance;
                var particle=kind==0?class_2398.field_11207:kind==1?class_2398.field_22247:class_2398.field_38002;
                c.field_1687.method_8406(particle,x,y,z,0,kind==1?-.015:.012,0);
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS,class_2960.method_60655("inner_dimension","boss_domain_atmosphere"),(g,t)->render(g));
    }
    private static void render(class_332 g){
        var client=class_310.method_1551();if(client.field_1687==null||client.field_1724==null||client.field_1724.method_7325())return;
        int w=g.method_51421(),h=g.method_51443();
        // Normalize overlaps so multiple encounters cannot compound into an opaque screen.
        float total=Math.max(1,strength[0]+strength[1]+strength[2]);
        for(int kind=0;kind<3;kind++){
            float level=strength[kind]/total;if(level<=0)continue;
            double pulse=.92+.08*Math.sin(clock/30.0+kind*2);
            int rgb=kind==0?0x7942B0:kind==1?0x657146:0x126E79;
            int fill=(int)Math.round(6*level),edge=(int)Math.round(42*level*pulse);
            if(fill>0)g.method_25294(0,0,w,h,(fill<<24)|rgb);
            int top=Math.max(1,h/4);
            g.method_25296(0,0,w,top,(edge<<24)|rgb,rgb);
            g.method_25296(0,h-top,w,h,rgb,(edge<<24)|rgb);
            int band=Math.max(1,w/5);
            for(int i=0;i<20;i++){
                int alpha=(int)(edge*Math.pow(1-i/20.0,2));if(alpha<1)continue;
                int x0=i*band/20,x1=(i+1)*band/20;
                g.method_25294(x0,0,x1,h,(alpha<<24)|rgb);
                g.method_25294(w-x1,0,w-x0,h,(alpha<<24)|rgb);
            }
        }
    }
}
