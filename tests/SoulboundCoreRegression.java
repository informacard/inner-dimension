import com.innerdimension.item.*;
import net.minecraft.*;
public final class SoulboundCoreRegression {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  var oldInventory=new class_1277(41);var newInventory=new class_1277(41);
  for(int slot:new int[]{0,7,40})oldInventory.method_5447(slot,new class_1799(ModItems.DOMAIN_CORE));
  oldInventory.method_5447(2,new class_1799(ModItems.MENTAL_FRAGMENT,8));
  var saved=SoulboundCores.take(oldInventory);
  check(saved.size()==3,"main hand, storage, offhand cores protected");
  check(oldInventory.method_5438(2).method_7947()==8,"ordinary items stay in vanilla death drop inventory");
  for(int slot:new int[]{0,7,40})check(oldInventory.method_5438(slot).method_7960(),"core absent during drop");
  oldInventory.method_5447(2,class_1799.field_8037);
  SoulboundCores.restore(oldInventory,saved);
  check(oldInventory.method_5438(40).method_7909()==ModItems.DOMAIN_CORE,"dead player retains core before respawn/save");
  check(SoulboundCores.copyRetained(oldInventory,newInventory).isEmpty(),"normal respawn has no conflict");
  for(int slot:new int[]{0,7,40}){check(newInventory.method_5438(slot).method_7909()==ModItems.DOMAIN_CORE,"core restored to original slot");check(oldInventory.method_5438(slot).method_7960(),"old source consumed");}
  check(SoulboundCores.copyRetained(oldInventory,newInventory).isEmpty(),"repeat callback cannot duplicate");
  oldInventory.method_5447(0,new class_1799(ModItems.DOMAIN_CORE));newInventory.method_5447(0,oldInventory.method_5438(0).method_7972());
  check(SoulboundCores.copyRetained(oldInventory,newInventory).isEmpty()&&newInventory.method_5438(0).method_7947()==1,"keepInventory and D16 do not duplicate");
  oldInventory.method_5447(3,new class_1799(ModItems.DOMAIN_CORE));newInventory.method_5447(3,new class_1799(ModItems.MENTAL_FRAGMENT,5));
  var displaced=SoulboundCores.copyRetained(oldInventory,newInventory);
  check(displaced.size()==1&&displaced.getFirst().method_7947()==5&&newInventory.method_5438(3).method_7909()==ModItems.DOMAIN_CORE,"respawn gifts cannot overwrite soulbound core");
  System.out.println("PASS: soulbound core death exclusion, ordinary drops, pre-respawn retention, main/offhand slots, keepInventory/D16 deduplication, conflicting respawn items");
 }
}
