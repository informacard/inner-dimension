import com.innerdimension.sound.ModSounds;
import com.innerdimension.network.SoundCuePayload;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
public final class SoundIntegrationRegression {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  class_155.method_36208();class_2966.method_12851();
  // Plain-Java bootstrap freezes vanilla registries; emulate Fabric's mod-registration window.
  var frozen=class_2370.class.getDeclaredField("field_36463");frozen.setAccessible(true);frozen.setBoolean(class_7923.field_41172,false);ModSounds.init();
  for(String key:new String[]{"recharge","fetus_pulse","void_ambience","void_infect","abyss_passage","enlightenment","injection","mental_conception","lubricated_entry","domain_expand","domain_retract","domain_fracture"}){
   check(class_7923.field_41172.method_10250(class_2960.method_60655("inner_dimension",key)),"Registered event: "+key);
   var packet=new SoundCuePayload(key,.15f);var buf=new class_9129(Unpooled.buffer(),null);
   try{SoundCuePayload.CODEC.encode(buf,packet);check(SoundCuePayload.CODEC.decode(buf).equals(packet),"Sound packet roundtrip");}finally{buf.release();}
  }
  check(ModSounds.WATER.method_10594()==class_3417.field_14998,"Water material uses vanilla swim sound");
  check(ModSounds.event("domain_clash")!=null,"Clash event registered");
  for(int radius=4;radius<=64;radius+=4)check(ModSounds.event("domain_expand_open_"+radius)!=null,"Radius-matched open expansion registered");
  check(class_2498.field_11537.method_10594().comp_3319().toString().equals("minecraft:block.glass.step"),"Pearl surface maps to glass steps");
  check(com.innerdimension.sound.DomainSoundRules.cue(null,0,0,false,false).equals("domain_expand"),"Closed expansion");
  check(com.innerdimension.sound.DomainSoundRules.cue(null,1,0,false,false).equals("domain_expand"),"Open expansion");
  check(com.innerdimension.sound.DomainSoundRules.cue(null,2,0,true,false).equals("domain_expand"),"Simple expansion");
  check(com.innerdimension.sound.DomainSoundRules.cue(null,2,10,false,false)==null,"Late observers do not replay expansion");
  check(com.innerdimension.sound.DomainSoundRules.cue(2,3,0,false,false).equals("domain_retract"),"Retraction transition");
  check(com.innerdimension.sound.DomainSoundRules.cue(2,3,0,false,true).equals("domain_fracture"),"Fracture transition");
  check(com.innerdimension.sound.DomainSoundRules.cue(3,3,1,false,true)==null,"No duplicate on following tick");
  System.out.println("PASS: sound bootstrap, six retained event registry, sound packet wire values, water and glass material groups");
 }
}
