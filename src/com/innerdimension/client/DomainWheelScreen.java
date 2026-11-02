package com.innerdimension.client;

import com.innerdimension.client.crest.CrestDesignStore;
import com.innerdimension.projection.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.*;
import java.awt.image.BufferedImage;
import java.util.*;

/** Transparent non-pausing selection surface. Artwork is sampled once when opened. */
public final class DomainWheelScreen extends class_437 {
    private record Strip(int x,int y,int width,int slot){}
    private final boolean open;
    private final java.util.List<String> names;
    private final java.util.List<Strip> strips=new ArrayList<>();
    private final int[][] icons=new int[7][];
    private int cx,cy,r,inner;
    public DomainWheelScreen(boolean open,java.util.List<String> names){super(class_2561.method_43470(""));this.open=open;this.names=names;for(int i=0;i<names.size();i++){BufferedImage image=CrestDesignStore.slot(i);int[] icon=new int[28*19];for(int y=0;y<19;y++)for(int x=0;x<28;x++)icon[y*28+x]=image.getRGB(x*image.getWidth()/28,y*image.getHeight()/19);icons[i]=icon;}}
    protected void method_25426(){cx=field_22789/2;cy=field_22790/2;r=Math.max(24,field_22790/4);inner=Math.max(12,(int)(r*.36));strips.clear();for(int y=-r;y<=r;y++){int start=-r,previous=-1;for(int x=-r;x<=r+1;x++){int slot=x>r?-1:hit(cx+x,cy+y);if(slot!=previous){if(previous>=0)strips.add(new Strip(cx+start,cy+y,x-start,previous));start=x;previous=slot;}}}}
    private int hit(double x,double y){return DomainWheelLayout.slotAt(x-cx,y-cy,r,inner,names.size()==7);}
    public void method_25394(class_332 g,int mx,int my,float delta){int hovered=hit(mx,my);for(var strip:strips)g.method_25294(strip.x,strip.y,strip.x+strip.width,strip.y+1,strip.slot==hovered?0xbba076bf:0x88513865);for(int slot=0;slot<names.size();slot++){double angle=-Math.PI/2+(slot+.5)*Math.PI/3;int x=slot==6?cx:cx+(int)(Math.cos(angle)*(r+inner)*.5),y=slot==6?cy:cy+(int)(Math.sin(angle)*(r+inner)*.5);int size=Math.max(12,Math.min(28,(r-inner)/2));int height=Math.max(1,size*19/28);int[] pixels=icons[slot];for(int py=0;py<height;py++)for(int px=0;px<size;px++){int c=pixels[(py*19/height)*28+px*28/size];if((c>>>24)!=0)g.method_25294(x-size/2+px,y-height/2+py,x-size/2+px+1,y-height/2+py+1,c);}}if(hovered>=0){String name=CrestDesignStore.name(hovered);if(name.isEmpty())name=names.get(hovered);if(name.isEmpty())name="淫纹 "+(hovered+1);g.method_25303(field_22793,name,mx+10,my+10,0xffffffff);}}
    public boolean method_25402(class_11909 event,boolean doubled){if(event.method_74228()==0){int slot=hit(event.comp_4798(),event.comp_4799());if(slot>=0){ClientPlayNetworking.send(new DomainWheelPayload(1,open,slot));method_25419();}return true;}return false;}
    public boolean method_25421(){return false;}
    public void method_25419(){field_22787.method_1507(null);}
    public boolean method_25401(double x,double y,double horizontal,double vertical){return DomainWheelInput.scroll(vertical);}
    public boolean open(){return open;}
}
