package com.innerdimension.client.domain;
import com.innerdimension.InnerDimension;
import com.innerdimension.network.DomainContestPayload;
import com.innerdimension.network.DomainHudPayload;
import com.innerdimension.client.DomainExpansionHud;
import com.innerdimension.client.DomainSubtitleAnimation;
import com.innerdimension.client.DomainSubtitlePainter;
import net.minecraft.*;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;

public final class DomainContestClient {
    private static DomainContestPayload state;
    private static DomainHudPayload announcement,contest;
    private static int age,noticeTicks,clock;
    private DomainContestClient(){}
    public static boolean simple(){return state!=null&&state.simple()>age;}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(DomainContestPayload.ID,(p,c)->c.client().execute(()->{state=p;age=0;}));
        ClientPlayNetworking.registerGlobalReceiver(DomainHudPayload.ID,(p,c)->c.client().execute(()->{
            if(p.contest())contest=p;else {if(p.heart()!=-1&&com.innerdimension.client.BossDomainHud.inBattle())return;announcement=p;noticeTicks=Math.max(1,p.titleTicks())+Math.max(1,p.nameTicks());}
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{state=null;announcement=null;contest=null;age=0;noticeTicks=0;});
        ClientTickEvents.END_CLIENT_TICK.register(c->{age++;clock++;if(noticeTicks>0)noticeTicks--;if(c.field_1687==null){state=null;announcement=null;contest=null;noticeTicks=0;}if(simple()&&c.field_1724!=null)c.field_1724.method_5660(true);});
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,InnerDimension.id("domain_contest"),(g,t)->{
            var c=class_310.method_1551();if(c.field_1687==null||c.field_1724==null)return;
            int y=g.method_51443()-110;
            if(announcement!=null&&noticeTicks>0) {
                renderAnnouncement(g,c,announcement,noticeTicks,t.method_60637(true));
            }
            int remaining=state==null?0:Math.max(0,state.simple()-age);
            if(remaining>0)g.method_27534(c.field_1772,class_2561.method_43469("inner_dimension.simple.remaining",String.format(java.util.Locale.ROOT,"%.1f",remaining/20.0)),g.method_51421()/2,y,0xffeed5e6);
            if(state!=null&&state.clash()>age&&contest!=null) {
                g.method_27534(c.field_1772,class_2561.method_43471("inner_dimension.contest.title"),g.method_51421()/2,y,0xffeee5ef);
                String first=name(contest.first()),second=name(contest.second()),vs=" vs ";
                float x=(g.method_51421()-c.field_1772.method_1727(first+vs+second))/2f;
                x=drawName(g,c,first,x,y+16,contest,false,0,t.method_60637(true));
                g.method_51433(c.field_1772,vs,(int)x,y+16,0xffeeeeee,true);x+=c.field_1772.method_1727(vs);
                drawName(g,c,second,x,y+16,contest,true,13,t.method_60637(true));
            }
        });
    }
    private static void renderAnnouncement(class_332 g,class_310 c,DomainHudPayload p,int remaining,float partial) {
        int titleTicks=Math.max(1,p.titleTicks()),nameTicks=Math.max(1,p.nameTicks());
        double elapsed=titleTicks+nameTicks-remaining+partial;
        boolean title=elapsed<titleTicks;
        String text=title?class_2561.method_43471("inner_dimension.projection.opened").getString():p.first();
        if(text.isBlank())return;
        text=DomainSubtitleAnimation.heart(text,Math.max(0,p.heart()));
        String[] units=title?DomainSubtitleAnimation.characters(text):DomainSubtitleAnimation.nameUnits(text);
        double nameAge=elapsed-titleTicks;
        double gap=title?0:4*Math.pow(Math.max(0,Math.min(1,nameAge/nameTicks)),2);
        double width=gap*(units.length-1);for(String unit:units)width+=c.field_1772.method_1727(unit);
        float scale=1.75f;double x=(g.method_51421()-width*scale)/2;int y=p.heart()==-1?55:g.method_51443()-134;
        for(int i=0;i<units.length;i++) {
            double alpha=title?DomainSubtitleAnimation.reveal(elapsed,titleTicks,i,units.length):Math.min(1,Math.max(0,(nameTicks-nameAge)/2));
            int opacity=(int)(255*alpha);if(opacity>3){
                int color=title?0xffffff:DomainExpansionHud.gradient(p.left(),p.center(),p.right(),units.length<=1?.5:(double)i/(units.length-1),p.splitLeft(),p.splitRight());
                var matrices=g.method_51448();matrices.pushMatrix();
                try{matrices.translate((float)x,y);matrices.scale(scale,scale);DomainSubtitlePainter.glyph(g,c.field_1772,units[i],0,0,(opacity<<24)|color,!title&&DomainSubtitlePainter.voidPalette(p.left(),p.center(),p.right()));}finally{matrices.popMatrix();}
            }
            x+=(c.field_1772.method_1727(units[i])+gap)*scale;
        }
    }
    private static String name(String name){return name.isBlank()?class_2561.method_43471("inner_dimension.contest.unnamed").getString():name;}
    private static float drawName(class_332 g,class_310 c,String name,float x,int y,DomainHudPayload p,boolean other,int offset,float partial) {
        String[] units=DomainSubtitleAnimation.characters(name);
        for(int i=0;i<units.length;i++) {
            int color=DomainExpansionHud.gradient(other?p.otherLeft():p.left(),other?p.otherCenter():p.center(),other?p.otherRight():p.right(),units.length<=1?.5:(double)i/(units.length-1),other?p.otherSplitLeft():p.splitLeft(),other?p.otherSplitRight():p.splitRight());
            double tick=clock+partial;
            float dx=p.contest()?(float)Math.sin(tick*1.7+(i+offset)*2.1)*.65f:0;
            float dy=p.contest()?(float)Math.cos(tick*1.3+(i+offset)*1.8)*.65f:0;
            var matrices=g.method_51448();matrices.pushMatrix();
            try{matrices.translate(x+dx,y+dy);DomainSubtitlePainter.glyph(g,c.field_1772,units[i],0,0,0xff000000|color,DomainSubtitlePainter.voidPalette(other?p.otherLeft():p.left(),other?p.otherCenter():p.center(),other?p.otherRight():p.right()));}finally{matrices.popMatrix();}
            x+=c.field_1772.method_1727(units[i]);
        }
        return x;
    }
}
