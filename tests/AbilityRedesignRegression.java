import com.innerdimension.domain.*;
import com.innerdimension.domain.runtime.DomainObjects;
import com.innerdimension.client.DomainSubtitleAnimation;
import java.util.*;
/** Regression boundaries: hidden unlock, real capacity, exclusive modes and special names. */
public final class AbilityRedesignRegression {
 private static void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 public static void main(String[] args)throws Exception{
  String succubus=DomainModes.SUCCUBUS;
  check(!DomainModes.owned(List.of("minecraft:chicken"),succubus),"Uncollected goat cannot unlock the hidden branch");
  check(DomainModes.owned(List.of("minecraft:goat"),succubus),"Collected goat permits its reverse");
  check(DomainAbilityCatalog.weight(List.of(succubus))==3,"Reverse actually changes server capacity cost");
  check(DomainAbilityCatalog.byEntity(succubus).colorWeight()==10,"Succubus has the requested colour priority");
  try{DomainAbilityCatalog.sanitize(List.of("minecraft:goat",succubus));throw new AssertionError("Both forms were accepted");}catch(IllegalArgumentException expected){}
  check(DomainAbilityCatalog.sanitize(List.of("minecraft:copper_golem","minecraft:vindicator")).size()==2,"Copper modes no longer conflict with the slash trait");
  for(String code:List.of("D27","D21","D23","D37")){check(DomainModes.valid(code,1)&&DomainModes.valid(code,2),"Both modes accepted");check(!DomainModes.valid(code,3)&&!DomainModes.valid(code,32),"Combined/old allegiance masks rejected as modes");}
  for(String code:List.of("D37","D58","D65","D44"))check(!DomainObjects.configurable(code),"Manual attacks have no allegiance picker: "+code);
  check(!DomainObjects.configurable("D72"),"Succubus follows owner animation participants without allegiance picker");
  check(DomainObjects.defaultMask("D30")== (DomainObjects.SELF|DomainObjects.FRIENDLY),"Water mining starts as support");
  check(DomainModes.hearts(List.of())==0&&DomainModes.hearts(List.of(succubus))==1&&DomainModes.hearts(List.of(succubus,"minecraft:chicken"))==2,"Three synchronized heart layouts");
  var combo=List.of(succubus,"minecraft:chicken","minecraft:blaze");
  check(Arrays.equals(DomainAbilityCatalog.nameColors(combo),new int[]{0xFF69B4,0x800020,0xFF69B4}),"Chicken/succubus override unrelated colour weights");
  check(DomainSubtitleAnimation.heart("领域",2).equals("♡领域♡"),"Two outer hearts and no center heart");
  check(DomainSubtitleAnimation.heart("领域",1).equals("领♡域"),"One center heart");
  check(DomainModes.migrateLegacySelection("2|minecraft:cave_spider|2").equals("2|minecraft:cave_spider|1"),"Legacy neutral mask never silently enables fatal reverse");
  check(DomainModes.migrateLegacySelection("2|minecraft:cow|34").equals("2|minecraft:cow|34"),"Other object selections remain intact");
  System.out.println("PASS: hidden unlock, capacity, mode exclusivity, object rules, hearts/colours and safe legacy migration");
 }
}
