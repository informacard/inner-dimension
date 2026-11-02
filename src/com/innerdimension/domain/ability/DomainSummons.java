package com.innerdimension.domain.ability;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.domain.geometry.DomainGeometry;
import net.minecraft.*;
import java.util.*;
/** Only domain-created entities are tracked or removed; existing pets are untouched. */
public final class DomainSummons {
 public static final String SESSION="inner_dimension:summon_session:",OWNER="inner_dimension:summon_owner:";
 private DomainSummons(){}
 @SuppressWarnings("unchecked") static List<class_1308> list(DomainSession s,String code){var out=s.state(code+"_summons",(List<class_1308>)null);if(out==null){out=new ArrayList<>();s.putState(code+"_summons",out);}out.removeIf(e->e.method_7325()||!e.method_5805());return out;}
 static class_1308 spawn(DomainSession s,String code,String type){
  if(!(s.world instanceof class_3218 world))return null;
  var owner=s.owner();if(owner==null)return null;
  double x=owner.method_23317()+(world.method_8409().method_43058()-.5)*4;
  double z=owner.method_23321()+(world.method_8409().method_43058()-.5)*4;
  var entity=D47D69Support.create(world,type,owner.method_23317(),owner.method_23318(),owner.method_23321());
  if(!(entity instanceof class_1308 mob))return null;
  var p=DomainGeometry.safeSummonPoint(s,mob,x,owner.method_23318(),z);
  if(p==null)return null;
  mob.method_5814(p.field_1352,p.field_1351,p.field_1350);
  // Check the entity's final collision box, rather than a player-sized placeholder.
  if(DomainGeometry.solid(world,mob.method_5829()))return null;
  if(mob instanceof class_1321 tame){tame.method_6173(true,true);tame.method_6170(owner);}
  mob.method_5752().add(SESSION+s.sessionId);mob.method_5752().add(OWNER+s.ownerId);
  if(!world.method_8649(mob))return null;
  list(s,code).add(mob);return mob;
 }
 static void end(DomainSession s,String code){for(var mob:list(s,code))mob.method_31472();s.putState(code+"_summons",null);}
 public static void discardOrphan(class_1308 mob){if(mob.method_73183().method_8608())return;for(String tag:mob.method_5752())if(tag.startsWith(SESSION)){try{if(DomainSessionIndex.bySession(UUID.fromString(tag.substring(SESSION.length())))==null)mob.method_31472();}catch(IllegalArgumentException ignored){mob.method_31472();}return;}}
}
