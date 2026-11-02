import com.innerdimension.projection.*;
import com.innerdimension.domain.*;
import com.mojang.serialization.JsonOps;
import java.util.*;
public final class ShellUpdateRegression {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception {
  net.minecraft.class_155.method_36208();net.minecraft.class_2966.method_12851();
  check(DomainLifetime.vulnerableSide(false,false),"Normal shell accepts outside attacks");
  check(!DomainLifetime.vulnerableSide(false,true),"Normal shell rejects inside attacks");
  check(DomainLifetime.vulnerableSide(true,true),"Reversed shell accepts inside attacks");
  check(!DomainLifetime.vulnerableSide(true,false),"Reversed shell rejects outside attacks");
  check(DomainLifetime.shellDamage(600,1)==580,"One health equals one second");
  check(DomainLifetime.shellDamage(580,0.25f)==575,"Fractional damage preserves tick precision");
  check(DomainLifetime.shellDamage(10,2)==0,"Lethal shell hit clamps to zero");
  check(DomainLifetime.shellDamage(600,Float.NaN)==600,"Invalid damage cannot poison lifetime");
  var binding=new ClosedDomainBinding();UUID id=UUID.randomUUID();
  check(!binding.reversed(id),"Default is normal shell");
  check(!binding.forCast(id,false)&&binding.forCast(id,true),"Normal choice temporarily reverses only when crouched");
  check(!binding.reversed(id),"Crouched cast cannot change saved normal choice");
  binding.select(id,true);
  check(binding.forCast(id,false)&&!binding.forCast(id,true),"Reverse choice temporarily becomes normal when crouched");
  check(binding.reversed(id),"Crouched cast cannot change saved reverse choice");
  var encoded=ClosedDomainBinding.CODEC.encodeStart(JsonOps.INSTANCE,binding).getOrThrow();
  var restored=ClosedDomainBinding.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
  check(restored.reversed(id),"Reversal survives save and load");restored.select(id,false);
  check(!restored.reversed(id),"Scroll up restores normal shell");
  check(DomainModes.selectable("D70")&&DomainModes.valid("D70",2)&&!DomainModes.valid("D70",32),"Simple domain accepts only forward/reverse mode");
  SlashShrineRegression.main(args);
  System.out.println("PASS: shell health/time, fractional and lethal hits, binding persistence, simple reversal and slash mesh");
 }
}