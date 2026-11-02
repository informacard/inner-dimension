package com.innerdimension.item;
import net.minecraft.*;
import net.fabricmc.fabric.api.event.player.*;
import nonplayerplushie.SensoryLinkData;
/** Looking down at the offhand core owns the use action, before drinking or block interaction. */
public final class DomainCoreBottleUse {
 public static boolean matches(class_1799 main,class_1799 off,float pitch,class_1268 hand){
  return hand==class_1268.field_5808&&pitch>=60&&off.method_7909() instanceof DomainCoreItem
   &&(CreativeSemenBottle.isBottle(main)||com.nonid.potion.NonPotions.isLiquidBottle(main)||main.method_31574(class_1802.field_20417));
 }
 private static class_1269 use(class_1657 player,class_1268 hand){
  if(!matches(player.method_6047(),player.method_6079(),player.method_36455(),hand))return class_1269.field_5811;
  if(player.method_7325()||com.innerdimension.domain.DomainInteractionGuard.blocked(player))return class_1269.field_5814;
  if(player instanceof class_3222 p){
   if(!p.method_5667().equals(SensoryLinkData.ownerUuid(SensoryLinkData.read(p.method_6079())))){
    p.method_7353(class_2561.method_43471("inner_dimension.core.not_owner"),true);return class_1269.field_5814;
   }
   var result=com.innerdimension.network.SubmitDomainBottlePayload.inject(p,p.method_6047());
   String key="inner_dimension.species."+result.result().name().toLowerCase(java.util.Locale.ROOT);
   class_2561 source=result.sourceEntityId().map(id->class_7923.field_41177.method_63535(id).method_5897()).orElse(class_2561.method_43471("item.inner_dimension.creative_semen_bottle"));
   p.method_7353(class_2561.method_43469(key,source),true);
  }
  return class_1269.field_5812;
 }
 public static void register(){
  UseItemCallback.EVENT.register((p,w,h)->use(p,h));
  UseBlockCallback.EVENT.register((p,w,h,hit)->use(p,h));
  UseEntityCallback.EVENT.register((p,w,h,e,hit)->use(p,h));
 }
 private DomainCoreBottleUse(){}
}
