package com.innerdimension.boss;
import com.google.gson.Gson;
import java.util.*;
/** Tests metadata only. Does not instantiate or read a Minecraft world. */
public final class BossPersistenceRegression {
    public static void main(String[] args){
        var data=new BossPersistence.FileData();var row=new BossPersistence.Row();
        row.id=UUID.randomUUID();row.kind=2;row.world="minecraft:overworld";row.city="city:1:2";row.cast=9;row.nodes=15;row.spread=3;row.burnout=77;row.disabled=88;row.registered=true;row.crystalBaselineReady=true;row.crystals.add(UUID.randomUUID());data.bosses.add(row);
        var restored=BossPersistence.decode(new Gson().toJson(data));var r=restored.bosses.getFirst();
        if(!r.id.equals(row.id)||!r.city.equals(row.city)||r.cast!=9||r.nodes!=15||r.spread!=3||r.burnout!=77||r.disabled!=88||!r.registered||!r.crystals.equals(row.crystals)||!r.crystalBaselineReady)throw new AssertionError("Restart metadata lost");
        boolean rejected=false;try{BossPersistence.decode("{\"version\":99,\"bosses\":[]}");}catch(IllegalStateException expected){rejected=true;}
        if(!rejected)throw new AssertionError("Unknown persistence version accepted");
        if(BossPersistence.decode("{\"version\":1,\"bosses\":[]}").bosses.size()!=0)throw new AssertionError("Empty save");
        System.out.println("PASS BossPersistenceRegression: ownership, consumed nodes, spread cap, cooldown balances, crystal UUIDs, version guard");
    }
}
