package com.innerdimension.boss;

import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.IOException;

/** Boss-only tuning; has no effect on PvP or other abilities. */
public final class BossDomainConfig {
    public boolean enabled=true, environmentChanges=true;
    public double dragonStrength=6.5,witherStrength=7,wardenStrength=5.5;
    public int virtualTicks=2400,warningTicks=20,dragonTicks=600,witherTicks=720;
    public int dragonBurnout=400,witherBurnout=360,dragonExpandTicks=40,witherExpandTicks=20;
    public double dragonRadius=160,witherRadius=40;
    public double dragonSimpleRate=2,witherSimpleRate=2.5,wardenSimpleRate=3.5;
    public int dragonExposureTicks=100,wardenPulseTicks=60,wardenDisabledTicks=160;
    public float dragonDamage=10,wardenDamage=4,witherHealingMultiplier=.65f;
    public double dragonKnockback=.8,dragonUpward=.35;
    public int environmentChecks=256,environmentWrites=32;
    private static BossDomainConfig instance=new BossDomainConfig();
    public static BossDomainConfig get(){return instance;}
    public static void load(){
        var path=FabricLoader.getInstance().getConfigDir().resolve("inner_dimension_boss_domains.json");
        var gson=new GsonBuilder().setPrettyPrinting().create();
        try {
            if(Files.exists(path)) {var value=gson.fromJson(Files.readString(path),BossDomainConfig.class);if(value!=null)instance=value;}
            else Files.writeString(path,gson.toJson(instance));
        } catch(IOException|com.google.gson.JsonParseException e){throw new IllegalStateException("Cannot read Boss domain configuration: "+path,e);}
        var c=instance;
        for(var field:BossDomainConfig.class.getFields())try{Object value=field.get(c);if(value instanceof Number n&&(!Double.isFinite(n.doubleValue())||n.doubleValue()<0))throw new IllegalArgumentException("Invalid Boss parameter: "+field.getName());}catch(IllegalAccessException e){throw new IllegalStateException(e);}

        if(c.virtualTicks<1||c.warningTicks<1||c.dragonTicks<1||c.witherTicks<1||c.dragonExpandTicks<1||c.witherExpandTicks<1||c.wardenPulseTicks<1||c.dragonExposureTicks<1
            ||!finite(c.dragonStrength)||!finite(c.witherStrength)||!finite(c.wardenStrength)||c.dragonRadius<=0||c.dragonRadius>256||c.witherRadius<=0||c.witherRadius>128
            ||c.dragonSimpleRate<1||c.witherSimpleRate<1||c.wardenSimpleRate<1||c.witherHealingMultiplier<0||c.witherHealingMultiplier>1)
            throw new IllegalArgumentException("Invalid inner_dimension_boss_domains.json values");
    }
    private static boolean finite(double v){return Double.isFinite(v)&&v>=0&&v<=10;}
}
