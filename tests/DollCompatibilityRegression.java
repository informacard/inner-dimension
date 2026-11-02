import com.innerdimension.entity.SensoryLinkDollEntity;
import net.minecraft.*;
import nonplayerplushie.SensoryLinkData;
import java.util.*;
public class DollCompatibilityRegression {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static class Doll extends SensoryLinkDollEntity {
  public Doll(){super(null,null);}
  public void init(class_2945.class_9222 b){super.method_5693(b);}
  @Override public void method_5674(class_2940<?> key){}
 }
 static Doll doll(sun.misc.Unsafe unsafe)throws Exception{
  Doll d=(Doll)unsafe.allocateInstance(Doll.class);var b=new class_2945.class_9222(d);d.init(b);
  for(var base:class_1297.class.getDeclaredFields())if(base.getType()==class_2940.class){
   base.setAccessible(true);var key=(class_2940)base.get(null);String type=base.getGenericType().getTypeName();
   Object value=type.contains("Byte")?(byte)0:type.contains("Integer")?0:type.contains("Optional")?Optional.empty():type.contains("Boolean")?false:class_4050.values()[0];
   b.method_56912(key,value);
  }
  var f=class_1297.class.getDeclaredField("field_6011");f.setAccessible(true);f.set(d,b.method_56911());return d;
 }
 public static void main(String[] args)throws Exception {
  class_155.method_36208();class_2966.method_12851();
  var uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);var unsafe=(sun.misc.Unsafe)uf.get(null);
  var id=UUID.randomUUID();var data=SensoryLinkData.create(id,"Owner",null);
  data=com.innerdimension.compat.PlushieDollData.withDestroyed(data,true);data.method_10582("future_extension","retained");
  Doll d=doll(unsafe);d.setDollData(data);
  data.method_10582("future_extension","mutated input");
  check(d.getDollData().method_68564("future_extension","").equals("retained"),"Input data is copied");
  check(com.innerdimension.compat.PlushieDollData.destroyed(d.getDollData()),"Destroyed appearance survives entity conversion");
  check(id.equals(SensoryLinkData.ownerUuid(d.getDollData())),"Owner sync does not require a profile");
  Doll remote=doll(unsafe);remote.setDollData(class_2522.method_67315(d.getDollData().toString()));
  check(remote.getDollData().equals(d.getDollData()),"Full tracker/save representation round trips");
  d.setDollData(com.innerdimension.compat.PlushieDollData.withDestroyed(d.getDollData(),false));
  check(!com.innerdimension.compat.PlushieDollData.destroyed(d.getDollData())&&com.innerdimension.compat.PlushieDollData.destroyed(remote.getDollData()),"Appearance changes cannot mutate another doll");
  Doll legacy=doll(unsafe);legacy.setOwnerUuid(id);legacy.setOwnerName("Owner");
  check(id.equals(SensoryLinkData.ownerUuid(legacy.getDollData())),"Legacy identity migrates without a profile");
  legacy.setDollData(legacy.getDollData());
  check(SensoryLinkData.valid(legacy.getDollData()),"Migrated data uses upstream schema");
  var models=new com.innerdimension.client.PlushieAppearanceCompat();
  for(int mask=0;mask<4;mask++){check(models.root(false,mask)!=null&&models.root(true,mask)!=null,"All model state variants load");}
  try{nonplayerplushie.client.SensoryLinkDollModel.class.getMethod("root",int.class);check(models.root(false,0)!=models.root(false,3),"Queued model variants use separate roots");}catch(NoSuchMethodException absentVariant){}
  check(Doll.class.getMethod("getDollData")!=null,"Entity exposes complete appearance data");
  System.out.println("PASS: full doll data, absent profile, owner, destroyed state, extension keys, round trip, immutable data, wide/slim model variants");
 }
}