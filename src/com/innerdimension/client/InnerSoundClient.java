package com.innerdimension.client;
import java.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.*;
import com.innerdimension.projection.*;
import com.innerdimension.sound.ModSounds;
import com.innerdimension.entity.FetusEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
public final class InnerSoundClient {
    private static class_638 world;
    private static boolean voidActive;
    private static Loop ambience;
    private static final Map<UUID,Loop> PULSES=new HashMap<>();
    private static final Map<UUID,Integer> PHASES=new HashMap<>();
    private InnerSoundClient(){}
    private static boolean wantsVoid(){var player=class_310.method_1551().field_1724;return player!=null&&com.innerdimension.voiddomain.VoidEmbodimentRules.ambience(player.method_5805(),voidActive,player.method_6059(class_7923.field_41174.method_47983(com.innerdimension.effect.ModStatusEffects.VOID_EMBODIMENT)));}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(com.innerdimension.network.SoundCuePayload.ID,(p,c)->c.client().execute(()->{
            if(world!=c.client().field_1687){clear();world=c.client().field_1687;}
            if(p.key().equals("void_ambience_on")){voidActive=true;return;}
            if(p.key().equals("void_ambience_off")){voidActive=false;return;}
            if(ModSounds.event(p.key())==null)return;
            personal(p.key(),Math.max(0,Math.min(1,p.volume())));
        }));
        ClientTickEvents.END_CLIENT_TICK.register(InnerSoundClient::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(InnerSoundClient::clear));
    }
    public static void personal(String key,float volume){class_310.method_1551().method_1483().method_4873(new class_1109(class_2960.method_60655("inner_dimension",key),class_3419.field_15248,volume,1,class_5819.method_43053(),false,0,class_1113.class_1114.field_5478,0,0,0,true));}
    private static void tick(class_310 client){
        if(client.field_1687!=world){clear();world=client.field_1687;}
        if(world==null||client.field_1724==null)return;
        if(wantsVoid()&&(ambience==null||ambience.done||ambience.stale())){ambience=new Loop("void_ambience",.85f,null,()->wantsVoid()&&class_310.method_1551().field_1687==world);client.method_1483().method_4873(ambience);}
        if(!wantsVoid()&&ambience!=null){stop(ambience);ambience=null;}
        Set<UUID> present=new HashSet<>(),phases=new HashSet<>();
        for(var entity:world.method_18112()){
            if(entity instanceof FetusEntity fetus&&entity.method_5805()&&entity.method_5858(client.field_1724)<32*32){
                var id=entity.method_5667();present.add(id);
                if(PULSES.containsKey(id)&&PULSES.get(id).stale()){stop(PULSES.remove(id));}
                if(!PULSES.containsKey(id)){var sourceWorld=world;var loop=new Loop("fetus_pulse",1f,fetus,()->!fetus.method_31481()&&fetus.method_73183()==class_310.method_1551().field_1687&&sourceWorld==class_310.method_1551().field_1687);PULSES.put(id,loop);client.method_1483().method_4873(loop);}
            }
            if(entity instanceof RoomProjectionEntity projection&&projection.isShell()&&projection.getContraption()!=null){
                var id=projection.method_5667();phases.add(id);Integer old=PHASES.put(id,projection.phase());
                var spec=((RoomProjectionContraption)projection.getContraption()).sourceSpec();
                String key=com.innerdimension.sound.DomainSoundRules.cue(old,projection.phase(),projection.elapsed(0),spec.shell()==null&&spec.mirror()==null,projection.broken());
                if("domain_expand".equals(key)&&spec.shell()==null&&spec.mirror()!=null){int radius=(spec.source().maxX()+1-spec.source().minX())/2;key="domain_expand_open_"+Math.max(4,Math.min(64,4*Math.round(radius/4f)));}
                if(key!=null){var center=projection.getAnchorVec().method_1031(spec.anchorX(),spec.anchorY(),spec.anchorZ());
                    client.method_1483().method_4873(new class_1109(ModSounds.event(key),class_3419.field_15254,.8f,1,class_5819.method_43053(),center.field_1352,center.field_1351,center.field_1350));}
            }
        }
        PHASES.keySet().retainAll(phases);
        PULSES.entrySet().removeIf(e->{if(present.contains(e.getKey()))return false;stop(e.getValue());return true;});
    }
    private static void stop(Loop loop){loop.done=true;class_310.method_1551().method_1483().method_4870(loop);}
    private static void clear(){if(ambience!=null)stop(ambience);ambience=null;PULSES.values().forEach(InnerSoundClient::stop);PULSES.clear();PHASES.clear();voidActive=false;world=null;}
    private static final class Loop extends class_1102 implements class_1117 {
        final class_1297 entity;final BooleanSupplier live;boolean done;int age,checks;
        Loop(String key,float volume,class_1297 entity,BooleanSupplier live){super(ModSounds.event(key),entity==null?class_3419.field_15256:class_3419.field_15254,class_5819.method_43053());this.entity=entity;this.live=live;field_5442=volume;field_5446=true;field_5451=0;if(entity==null){field_18936=true;field_5440=class_1113.class_1114.field_5478;}method_16896();}
        public boolean method_4793(){return done;}
        boolean stale(){return ++checks>40&&!class_310.method_1551().method_1483().method_4877(this);}
        public void method_16896(){age++;if(!live.getAsBoolean())done=true;if(entity!=null){field_5439=entity.method_23317();field_5450=entity.method_23318();field_5449=entity.method_23321();}}
    }
}
