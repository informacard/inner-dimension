package com.innerdimension.client;

import com.innerdimension.projection.*;
import com.innerdimension.network.SaveFloorPatternPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.*;
import java.util.*;

/** Flat material preview uses exactly the same grid and sampler as the server terrain skin. */
public final class FloorPatternScreen extends class_437 {
    private static final int W=438,H=310,GRID=64,CELL=3;
    private final DomainCoreScreen parent;
    private final class_2680[][] tile=new class_2680[16][16];
    private FloorPatternSettings.Mode mode;
    private boolean preserve;
    private String formula,fg,bg,error="";
    private int left,top;
    private float scale=1;
    private class_342 expressionBox,foregroundBox,backgroundBox;
    private class_4185 modeButton,centerButton,saveButton;
    private final int[] pixels=new int[GRID*GRID];
    private final Map<class_2680,Integer> colors=new HashMap<>();
    public FloorPatternScreen(DomainCoreScreen parent,List<Integer> states,FloorPatternSettings settings) {
        super(tr("title"));this.parent=parent;mode=settings.mode();preserve=settings.preserveCenter();
        formula=settings.expression();fg=settings.foreground();bg=settings.background();
        if(states.size()!=256)throw new IllegalArgumentException("Missing floor sample");
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)tile[x][z]=class_2248.method_9531(states.get(x*16+z));
    }
    DomainCoreScreen parent(){return parent;}
    private FloorPatternSettings settings(){return new FloorPatternSettings(mode,formula,preserve,fg,bg);}
    protected void method_25426() {
        scale=Math.min(1f,Math.min((field_22789-12f)/W,(field_22790-12f)/H));
        left=(Math.round(field_22789/scale)-W)/2;top=(Math.round(field_22790/scale)-H)/2;
        modeButton=button(left+218,top+32,208,tr("mode."+mode.name().toLowerCase(Locale.ROOT)),b->{
            var all=FloorPatternSettings.Mode.values();var old=mode;mode=all[(mode.ordinal()+1)%all.length];
            if(mode==FloorPatternSettings.Mode.CUSTOM&&old!=FloorPatternSettings.Mode.TILE)formula=old.expression;
            syncMode();refresh();
        });
        centerButton=button(left+218,top+58,208,tr(preserve?"preserve":"replace"),b->{preserve=!preserve;b.method_25355(tr(preserve?"preserve":"replace"));refresh();});
        foregroundBox=field(left+218,top+102,208,128,fg,v->{fg=v;refresh();},"foreground");
        backgroundBox=field(left+218,top+146,208,128,bg,v->{bg=v;refresh();},"background");
        expressionBox=field(left+12,top+244,W-24,512,mode==FloorPatternSettings.Mode.CUSTOM?formula:mode.expression,v->{if(mode==FloorPatternSettings.Mode.CUSTOM){formula=v;refresh();}},"expression");
        expressionBox.method_47400(class_7919.method_47407(tr("expression.help")));
        saveButton=button(left+12,top+278,100,tr("save"),b->{if(error.isEmpty()&&ClientPlayNetworking.canSend(SaveFloorPatternPayload.ID))ClientPlayNetworking.send(new SaveFloorPatternPayload(settings()));});
        button(left+118,top+278,100,class_2561.method_43471("gui.cancel"),b->method_25419());
        syncMode();refresh();
    }
    private class_4185 button(int x,int y,int width,class_2561 title,class_4185.class_4241 action) {
        return method_37063(class_4185.method_46430(title,action).method_46434(x,y,width,20).method_46431());
    }
    private class_342 field(int x,int y,int width,int max,String value,java.util.function.Consumer<String> change,String hint) {
        class_342 box=method_37063(new class_342(field_22793,x,y,width,20,tr(hint)));
        box.method_1880(max);box.method_1852(value);box.method_47404(tr(hint+".hint"));box.method_1863(change);return box;
    }
    private void syncMode() {
        modeButton.method_25355(tr("mode."+mode.name().toLowerCase(Locale.ROOT)));
        expressionBox.field_22764=mode==FloorPatternSettings.Mode.CUSTOM;
        expressionBox.method_1852(mode==FloorPatternSettings.Mode.CUSTOM?formula:mode.expression);
        centerButton.field_22764=mode!=FloorPatternSettings.Mode.TILE;
    }
    private void refresh() {
        if(saveButton==null)return;
        try {
            FloorPatternSampler sampler=new FloorPatternSampler(tile,settings());
            for(int x=0;x<GRID;x++)for(int z=0;z<GRID;z++) {
                int lx=x-24,lz=z-24;
                pixels[z*GRID+x]=OpenDomainRules.intersects(lx-8,0,lz-8,lx+1-8,1,lz+1-8,32)
                        ?color(sampler.at(lx,lz)):0xff19151e;
            }
            error="";saveButton.field_22764=true;
        } catch(IllegalArgumentException ex) {error=ex.getMessage();saveButton.field_22764=false;Arrays.fill(pixels,0xff332831);}
    }
    private int color(class_2680 state) {
        return colors.computeIfAbsent(state,s->{
            if(s.method_26215())return 0xff302a35;
            var map=s.method_26205(field_22787.field_1687,new class_2338(0,0,0));
            if(map.field_16011!=0)return 0xff000000|map.field_16011;
            String id=class_7923.field_41175.method_10221(s.method_26204()).toString();
            return 0xff000000 | (id.hashCode()&0x7f7f7f) | 0x404040;
        });
    }
    public void method_25394(class_332 g,int mx,int my,float delta) {
        var matrices=g.method_51448();matrices.pushMatrix();matrices.scale(scale,scale);
        mx=(int)(mx/scale);my=(int)(my/scale);
        g.method_25294(left,top,left+W,top+H,0xff25212a);
        g.method_27534(field_22793,field_22785,left+W/2,top+10,0xffffffff);
        int px=left+12,py=top+32;
        for(int z=0;z<GRID;z++)for(int x=0;x<GRID;x++)g.method_25294(px+x*CELL,py+z*CELL,px+(x+1)*CELL,py+(z+1)*CELL,pixels[z*GRID+x]);
        // Original 16x16 footprint; orange border does not become part of the drawn pattern.
        int a=px+24*CELL,b=py+24*CELL,n=16*CELL;
        g.method_25294(a,b,a+n,b+1,0xffffcc88);g.method_25294(a,b+n-1,a+n,b+n,0xffffcc88);
        g.method_25294(a,b,a+1,b+n,0xffffcc88);g.method_25294(a+n-1,b,a+n,b+n,0xffffcc88);
        label(g,"foreground",left+218,top+88);label(g,"background",left+218,top+132);
        wrap(g,tr("materials.help").getString(),left+218,top+176,208,0xffbcb2c9);
        wrap(g,tr("coordinates").getString(),left+218,top+199,208,0xffbcb2c9);
        label(g,"expression",left+12,top+231);
        if(!error.isEmpty())g.method_25303(field_22793,field_22793.method_27523(error,W-24),left+12,top+267,0xffff7777);
        if(mx>=px&&mx<px+192&&my>=py&&my<py+192) {
            int x=(mx-px)/CELL-24,z=(my-py)/CELL-24;
            g.method_25303(field_22793,"x="+x+" z="+z,left+238,top+284,0xffe8dff0);
        }
        super.method_25394(g,mx,my,delta);
        matrices.popMatrix();
    }
    private class_11909 scaled(class_11909 e){return new class_11909(e.comp_4798()/scale,e.comp_4799()/scale,e.comp_4800());}
    @Override public boolean method_25402(class_11909 e,boolean doubled){return super.method_25402(scaled(e),doubled);}
    @Override public boolean method_25403(class_11909 e,double dx,double dy){return super.method_25403(scaled(e),dx/scale,dy/scale);}
    @Override public boolean method_25406(class_11909 e){return super.method_25406(scaled(e));}
    private void label(class_332 g,String key,int x,int y){g.method_25303(field_22793,tr(key).getString(),x,y,0xffe8dff0);}
    private void wrap(class_332 g,String text,int x,int y,int width,int color){
        StringBuilder line=new StringBuilder();int row=0;
        for(char c:text.toCharArray()){if(field_22793.method_1727(line.toString()+c)>width){g.method_25303(field_22793,line.toString(),x,y+row++*10,color);line.setLength(0);}line.append(c);}
        g.method_25303(field_22793,line.toString(),x,y+row*10,color);
    }
    private static class_2561 tr(String key){return class_2561.method_43471("screen.inner_dimension.floor."+key);}
    @Override public void method_25419(){field_22787.method_1507(parent);}
}
