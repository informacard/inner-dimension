package com.innerdimension.client;
import com.innerdimension.network.BossDomainPayload;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.*;
import java.util.*;

public final class BossDomainHud {
    private static BossDomainPayload state;
    private static int age;
    public static boolean inBattle(){return state!=null&&!state.views().isEmpty();}
    public static boolean inside(int kind){return state!=null&&state.views().stream().anyMatch(v->v.kind()==kind&&v.inside());}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(BossDomainPayload.ID,(p,c)->c.client().execute(()->{if(state==null||p.tick()>=state.tick()){state=p;age=0;}}));
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{state=null;age=0;});
        ClientTickEvents.END_CLIENT_TICK.register(c->{age++;if(c.field_1687==null||age>40)state=null;});
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,class_2960.method_60655("inner_dimension","boss_domains"),(g,t)->{
            var c=class_310.method_1551();if(c.field_1724==null||c.field_1687==null||state==null)return;
            double remaining=state.views().stream().mapToDouble(BossDomainPayload.View::player).filter(v->v>=0).min().orElse(-1);
            if(remaining>=0)g.method_27534(c.field_1772,class_2561.method_43469("inner_dimension.boss.player_remaining",seconds(remaining)),g.method_51421()/2,35,0xffd7a8ed);
        });
    }
    private static String seconds(double ticks){return String.format(Locale.ROOT,"%.1fs",Math.max(0,ticks)/20);}
}
