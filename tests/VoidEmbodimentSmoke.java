package smoke;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import com.innerdimension.voiddomain.*;
import com.innerdimension.block.ModBlocks;
import com.innerdimension.item.ModItems;
import com.innerdimension.sound.ModSounds;
import com.mojang.serialization.JsonOps;
import net.minecraft.*;
import java.util.*;
public final class VoidEmbodimentSmoke implements ClientModInitializer {
 public void onInitializeClient(){try{
  var state=new VoidEmbodimentState();var id=UUID.randomUUID();
  for(int phase:new int[]{1,2,3,4}){state.put(id,phase);var json=VoidEmbodimentState.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();var saved=VoidEmbodimentState.CODEC.parse(JsonOps.INSTANCE,json).getOrThrow();if(saved.players.get(id)!=phase)throw new AssertionError("Persistent phase "+phase);}
  if(!VoidEmbodimentState.CODEC.parse(JsonOps.INSTANCE,new com.google.gson.JsonObject()).getOrThrow().players.isEmpty())throw new AssertionError("Old save");
  if(!class_7923.field_41178.method_10250(class_2960.method_60655("inner_dimension","void_nail")))throw new AssertionError("Nail registration");
  if(ModItems.VOID_NAIL.method_7853(new class_1799(ModItems.VOID_NAIL))!=class_1839.field_8953)throw new AssertionError("Bow charge pose");
  if(!ModBlocks.SHEATHED_INFECTOR.method_9564().method_28498(class_2318.field_10927))throw new AssertionError("Infector facing");
  for(String sound:List.of("pillar_drone","pillar_leak_top","pillar_leak_bottom","void_rewind")){
   if(ModSounds.event(sound)==null)throw new AssertionError(sound);
   try(var stream=java.nio.file.Files.newInputStream(net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("inner_dimension").orElseThrow().findPath("assets/inner_dimension/sounds/"+sound+".ogg").orElseThrow())){if(stream.read()<0)throw new AssertionError(sound);}
  }
  if(Arrays.stream(class_1309.class.getDeclaredMethods()).noneMatch(m->m.getName().contains("protectDirect")))throw new AssertionError("Removal protection mixin absent");
  if(Arrays.stream(class_3244.class.getDeclaredMethods()).noneMatch(m->m.getName().contains("impalementBefore")))throw new AssertionError("Impalement movement hook absent");
  if(Arrays.stream(class_309.class.getDeclaredMethods()).noneMatch(m->m.getName().contains("ritualKey")))throw new AssertionError("Ritual keyboard lock absent");
  if(Arrays.stream(class_312.class.getDeclaredMethods()).noneMatch(m->m.getName().contains("ritualLook")))throw new AssertionError("Ritual mouse look lock absent");
  for(boolean locked:new boolean[]{true,false}){var packet=new com.innerdimension.network.RitualControlPayload(locked);var buf=new class_9129(io.netty.buffer.Unpooled.buffer(),null);try{com.innerdimension.network.RitualControlPayload.CODEC.encode(buf,packet);if(!packet.equals(com.innerdimension.network.RitualControlPayload.CODEC.decode(buf)))throw new AssertionError("Ritual lock protocol");}finally{buf.release();}}
  if(Arrays.stream(class_1657.class.getDeclaredMethods()).noneMatch(m->m.getName().contains("protectCore")))throw new AssertionError("Soulbound death hook absent");
  for(String regression:List.of("ImpalementFallTraceRegression","SoulboundCoreRegression","InnerRoomLandingRegression"))Class.forName(regression).getMethod("main",String[].class).invoke(null,(Object)new String[0]);
  var pillar=com.innerdimension.projection.RoomProjectionContraption.infectionPillar();
  if(pillar.getBlocks().size()!=64||pillar.bounds.field_1320-pillar.bounds.field_1323!=2||pillar.bounds.field_1325-pillar.bounds.field_1322!=16||pillar.bounds.field_1324-pillar.bounds.field_1321!=2)throw new AssertionError("Entity pillar geometry");
  for(var entry:pillar.getBlocks().entrySet())if(entry.getValue().comp_1342().method_26220(pillar.snapshotView(),entry.getKey()).method_1110())throw new AssertionError("Entity pillar must have physical block collision");
  if(com.innerdimension.voiddomain.VoidEmbodimentRules.route(true,false,true)!=com.innerdimension.voiddomain.VoidEmbodimentRules.Input.ROOM_TRAVEL||com.innerdimension.voiddomain.VoidEmbodimentRules.route(true,true,true)!=com.innerdimension.voiddomain.VoidEmbodimentRules.Input.BLOCKED||com.innerdimension.voiddomain.VoidEmbodimentRules.route(false,false,true)!=com.innerdimension.voiddomain.VoidEmbodimentRules.Input.BLOCKED||com.innerdimension.voiddomain.VoidEmbodimentRules.route(false,true,true)!=com.innerdimension.voiddomain.VoidEmbodimentRules.Input.BLOCKED)throw new AssertionError("Void state must preserve R room travel while blocking expansion");
  if(!com.innerdimension.voiddomain.VoidEmbodimentRules.ambience(true,false,true)||com.innerdimension.voiddomain.VoidEmbodimentRules.ambience(false,false,true)||com.innerdimension.voiddomain.VoidEmbodimentRules.ambience(true,false,false)||!com.innerdimension.voiddomain.VoidEmbodimentRules.ambience(true,true,false))throw new AssertionError("Embodiment ambient lifecycle");
  System.out.println("PASS: 64 physical entity blocks, 2x2x16 bounds, R travel versus expansion routing, void ambient lifecycle");
  if(!com.innerdimension.voiddomain.VoidEmbodimentRules.baptismWorld().equals(class_1937.field_25181)||com.innerdimension.voiddomain.VoidEmbodimentRules.baptismWorld().equals(class_1937.field_25180)||!com.innerdimension.voiddomain.VoidEmbodimentRules.baptismWorld().method_29177().toString().equals("minecraft:the_end"))throw new AssertionError("Baptism destination must be the End, never the Nether");
  if(!com.innerdimension.voiddomain.VoidEmbodimentRules.pillars(1)||com.innerdimension.voiddomain.VoidEmbodimentRules.pillars(2)||com.innerdimension.voiddomain.VoidEmbodimentRules.pillars(3)||com.innerdimension.voiddomain.VoidEmbodimentRules.pillars(4))throw new AssertionError("Dead or cleansed pillars must not respawn");
  long film=com.innerdimension.voiddomain.VoidEmbodimentRules.baptismMillis()*1_000_000L;
  if(com.innerdimension.voiddomain.VoidEmbodimentRules.baptismReady(4,film-1)||!com.innerdimension.voiddomain.VoidEmbodimentRules.baptismReady(4,film)||com.innerdimension.voiddomain.VoidEmbodimentRules.baptismReady(1,film)||com.innerdimension.voiddomain.VoidEmbodimentRules.baptismReady(3,film))throw new AssertionError("Advancement waits for the entire film and only grants once");
  System.out.println("PASS: explicit End destination, post-baptism pillar removal, persisted pending-film phase, minimum film duration before advancement");
  var bottle=new class_1799(ModItems.CREATIVE_SEMEN_BOTTLE);var core=new class_1799(ModItems.DOMAIN_CORE);
  if(!com.innerdimension.item.DomainCoreBottleUse.matches(bottle,core,60,class_1268.field_5808))throw new AssertionError("Offhand core injection gesture");
  if(com.innerdimension.item.DomainCoreBottleUse.matches(bottle,core,59,class_1268.field_5808)||com.innerdimension.item.DomainCoreBottleUse.matches(core,bottle,90,class_1268.field_5808)||com.innerdimension.item.DomainCoreBottleUse.matches(bottle,core,90,class_1268.field_5810))throw new AssertionError("Injection gesture must not consume other hand/normal use");
  var silence=com.innerdimension.client.BaptismSilence.class.getDeclaredField("silenced");silence.setAccessible(true);
  var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
  var soundSystem=(class_1140)unsafe.allocateInstance(class_1140.class);
  var gain=class_1140.class.getDeclaredMethod("method_43222",float.class,class_3419.class);gain.setAccessible(true);
  try{silence.setBoolean(null,true);for(var category:class_3419.values())if((float)gain.invoke(soundSystem,.8f,category)!=0F)throw new AssertionError("Baptism sound leak: "+category);}
  finally{silence.setBoolean(null,false);}
  if(com.innerdimension.client.BaptismSilence.active())throw new AssertionError("Silence reset");
  for(int variant:new int[]{4,5}){var packet=new com.innerdimension.network.EnlightenmentVisualPayload(variant,true);var buf=new class_9129(io.netty.buffer.Unpooled.buffer(),null);try{com.innerdimension.network.EnlightenmentVisualPayload.CODEC.encode(buf,packet);if(!packet.equals(com.innerdimension.network.EnlightenmentVisualPayload.CODEC.decode(buf)))throw new AssertionError("Visual protocol");}finally{buf.release();}}
  System.out.println("VOID_EMBODIMENT_SMOKE_PASS: persistent phases, legacy save, registry, bow pose, facing, audio resources, removal mixin, visual protocol, actual sound mixer gate for every category and reset");System.exit(0);
 }catch(Throwable failure){failure.printStackTrace();System.out.println("VOID_EMBODIMENT_SMOKE_FAIL");System.exit(1);}}
}
