package com.innerdimension.domain;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.*;
/** Authoritative action denial; client presentation is only an early convenience. */
public final class DomainInteractionGuard {
    private DomainInteractionGuard(){}
    public static boolean blocked(class_1297 player){return SimpleDomain.active(player);}
    public static void register(){
        AttackEntityCallback.EVENT.register((p,w,h,e,hit)->blocked(p)?class_1269.field_5814:class_1269.field_5811);
        AttackBlockCallback.EVENT.register((p,w,h,pos,side)->blocked(p)||com.innerdimension.domain.ability.D64.locked(w,pos)?class_1269.field_5814:class_1269.field_5811);
        UseEntityCallback.EVENT.register((p,w,h,e,hit)->blocked(p)?class_1269.field_5814:class_1269.field_5811);
        UseBlockCallback.EVENT.register((p,w,h,hit)->blocked(p)?class_1269.field_5814:class_1269.field_5811);
        UseItemCallback.EVENT.register((p,w,h)->blocked(p)?class_1269.field_5814:class_1269.field_5811);
    }
}
