package smoke;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.*;
import com.innerdimension.item.*;
public final class ExtractionSmoke implements ClientModInitializer {
 public void onInitializeClient(){try{
  Class.forName("net.minecraft.class_3244"); // Apply the packet-lock mixin to the actual runtime class.
  Class.forName("net.minecraft.class_757"); // Apply FOV mixin to the actual renderer.
  Class.forName("net.minecraft.class_743"); // Apply the movement-input mixin.
  if(new class_1799(ModItems.SEMEN_EXTRACTOR).method_7914()!=1)throw new AssertionError("Stack limit");
  for(String sound:new String[]{"item.bottle.fill","entity.item.break"})if(!class_7923.field_41172.method_10250(class_2960.method_60655("minecraft",sound)))throw new AssertionError(sound);
  ExtractionTimingRegressionProxy.check();
  var active=com.innerdimension.client.ExtractionClient.class.getDeclaredField("active");active.setAccessible(true);
  var ticks=com.innerdimension.client.ExtractionClient.class.getDeclaredField("ticks");ticks.setAccessible(true);
  active.setBoolean(null,true);ticks.setInt(null,20);
  if(com.innerdimension.client.ExtractionClient.scale(1)!=.75f)throw new AssertionError("Active FOV");
  var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
  var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
  var local=(class_746)unsafe.allocateInstance(class_746.class);
  // These intentionally lack worlds/collision boxes: only a HEAD cancellation can make move safe.
  local.method_18799(new class_243(1,-1,1));
  local.method_5784(class_1313.field_6308,new class_243(1,-1,1));
  if(!local.method_18798().equals(class_243.field_1353))throw new AssertionError("Client physics must stop before movement");
  var remote=(class_3222)unsafe.allocateInstance(class_3222.class);
  var position=class_1297.class.getDeclaredField("field_22467");position.setAccessible(true);position.set(remote,class_243.field_1353);
  var runningField=SemenExtraction.class.getDeclaredField("RUNNING");runningField.setAccessible(true);
  var running=(java.util.Map)runningField.get(null);running.put(remote.method_5667(),null);
  try{remote.method_18799(new class_243(1,-1,1));remote.method_5784(class_1313.field_6308,new class_243(1,-1,1));if(!remote.method_18798().equals(class_243.field_1353))throw new AssertionError("Server physics must stop before movement");}
  finally{running.remove(remote.method_5667());}
  var packet=new com.innerdimension.network.ExtractionPayload(true,1.25,70,-3.75);
  var buffer=new class_9129(io.netty.buffer.Unpooled.buffer(),null);
  try{com.innerdimension.network.ExtractionPayload.CODEC.encode(buffer,packet);if(!packet.equals(com.innerdimension.network.ExtractionPayload.CODEC.decode(buffer)))throw new AssertionError("Authoritative anchor codec");}finally{buffer.release();}
  active.setBoolean(null,false);
  if(com.innerdimension.client.ExtractionClient.scale(1)!=1)throw new AssertionError("Immediate FOV restoration");
  System.out.println("EXTRACTION_SMOKE_PASS: actual client/server movement cancellation, anchor codec, runtime mixins, stack limit, vanilla sounds, timing and instant FOV reset");System.exit(0);
 }catch(Throwable t){t.printStackTrace();System.out.println("EXTRACTION_SMOKE_FAIL");System.exit(1);}}
 static class ExtractionTimingRegressionProxy{static void check(){if(ExtractionTiming.complete(19)||!ExtractionTiming.complete(20))throw new AssertionError("Timing");}}
}
