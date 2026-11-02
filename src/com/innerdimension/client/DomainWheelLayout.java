package com.innerdimension.client;

/** Shared drawing and hit-test geometry: six annular sectors and an optional centre. */
public final class DomainWheelLayout {
    private DomainWheelLayout(){}
    public static int slotAt(double dx,double dy,int radius,int inner,boolean seventh){
        double distance=Math.hypot(dx,dy);
        if(distance<inner-2)return seventh?6:-1;
        if(distance<inner||distance>radius)return -1;
        double angle=(Math.atan2(dy,dx)+Math.PI/2+Math.PI*2)%(Math.PI*2);
        double sector=angle/(Math.PI/3),fraction=sector-Math.floor(sector);
        if(fraction<.015||fraction>.985)return -1;
        return (int)sector;
    }
}
