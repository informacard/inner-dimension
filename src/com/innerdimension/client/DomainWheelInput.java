package com.innerdimension.client;
import com.innerdimension.projection.*;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.*;
import org.lwjgl.glfw.GLFW;

public final class DomainWheelInput {
    private static final int HOLD_TICKS=6;
    private static int ticks;
    private static int held=-1;
    private static boolean requested,consumed;
    private static double scrollRemainder;
    private static class_304 closedKey,openKey;
    private static boolean lockedClosed,lockedOpen;
    public static boolean crestLocked(){var p=class_310.method_1551().field_1724;return p!=null&&p.method_6059(class_7923.field_41174.method_47983(com.innerdimension.effect.ModStatusEffects.VOID_EMBODIMENT));}
    private static boolean lockedInput(class_310 client,boolean tappedClosed,boolean tappedOpen){
        if(!crestLocked()){lockedClosed=false;lockedOpen=false;return false;}
        boolean closed=down(client,closedKey),open=down(client,openKey);
        if(client.field_1755 instanceof DomainWheelScreen)client.method_1507(null);
        if(client.field_1755==null){
            if((closed&&!lockedClosed||tappedClosed&&!closed)&&ClientPlayNetworking.canSend(ToggleProjectionPayload.ID))ClientPlayNetworking.send(ToggleProjectionPayload.INSTANCE);
            if((open&&!lockedOpen||tappedOpen&&!open)&&ClientPlayNetworking.canSend(ToggleOpenProjectionPayload.ID))ClientPlayNetworking.send(ToggleOpenProjectionPayload.INSTANCE);
        }
        lockedClosed=closed;lockedOpen=open;held=-1;ticks=0;requested=false;consumed=true;return true;
    }
    public static void register(class_304 closed,class_304 open){closedKey=closed;openKey=open;ClientPlayNetworking.registerGlobalReceiver(DomainWheelStatePayload.ID,(payload,context)->context.client().execute(()->{var client=context.client();if(held==(payload.open()?1:0)&&requested&&!consumed&&client.field_1755==null){com.innerdimension.client.crest.CrestDesignStore.importNames(payload.names());client.method_1507(new DomainWheelScreen(payload.open(),payload.names()));}}));}
    private static boolean down(class_310 client,class_304 binding){var key=KeyBindingHelper.getBoundKeyOf(binding);long window=client.method_22683().method_4490();if(key.method_1444()<0)return false;return key.method_1442()==class_3675.class_307.field_1668?GLFW.glfwGetKey(window,key.method_1444())==GLFW.GLFW_PRESS:key.method_1442()==class_3675.class_307.field_1672&&GLFW.glfwGetMouseButton(window,key.method_1444())==GLFW.GLFW_PRESS;}
    public static void tick(class_310 client){boolean tappedClosed=false,tappedOpen=false;while(closedKey.method_1436())tappedClosed=true;while(openKey.method_1436())tappedOpen=true;if(client.field_1724==null||client.field_1687==null){held=-1;ticks=0;return;}if(RitualInput.active()){held=-1;ticks=0;requested=false;consumed=true;return;}if(lockedInput(client,tappedClosed,tappedOpen))return;if(client.field_1755!=null&&!(client.field_1755 instanceof DomainWheelScreen)){held=-1;ticks=0;return;}int next=down(client,openKey)?1:down(client,closedKey)?0:-1;if(next<0&&held<0&&client.field_1755==null){if(tappedOpen&&ClientPlayNetworking.canSend(ToggleOpenProjectionPayload.ID))ClientPlayNetworking.send(ToggleOpenProjectionPayload.INSTANCE);else if(tappedClosed&&ClientPlayNetworking.canSend(ToggleProjectionPayload.ID))ClientPlayNetworking.send(ToggleProjectionPayload.INSTANCE);}if(held>=0&&next!=held){if(client.field_1755 instanceof DomainWheelScreen)client.method_1507(null);else if(!requested&&!consumed){if(held==1&&ClientPlayNetworking.canSend(ToggleOpenProjectionPayload.ID))ClientPlayNetworking.send(ToggleOpenProjectionPayload.INSTANCE);else if(held==0&&ClientPlayNetworking.canSend(ToggleProjectionPayload.ID))ClientPlayNetworking.send(ToggleProjectionPayload.INSTANCE);}held=-1;ticks=0;requested=false;consumed=false;}if(next<0)return;if(held<0){held=next;ticks=0;requested=false;consumed=false;}if(client.field_1755 instanceof DomainWheelScreen)consumed=true;if(++ticks==HOLD_TICKS&&!consumed&&ClientPlayNetworking.canSend(DomainWheelPayload.ID)){requested=true;ClientPlayNetworking.send(new DomainWheelPayload(0,held==1,0));}}
    public static boolean scroll(double delta){
        if(RitualInput.active())return true;if(crestLocked())return false;
        var client=class_310.method_1551();
        if(delta==0||client.field_1724==null||client.field_1755!=null&&!(client.field_1755 instanceof DomainWheelScreen))return false;
        boolean closed=down(client,closedKey),open=down(client,openKey);
        if(!closed&&!open)return false;
        if(!(client.field_1724.method_6047().method_7909() instanceof com.innerdimension.item.DomainCoreItem)&&!(client.field_1724.method_6079().method_7909() instanceof com.innerdimension.item.DomainCoreItem))return false;
        if(!ClientPlayNetworking.canSend(DomainWheelPayload.ID))return false;
        held=open?1:0;ticks=HOLD_TICKS;requested=true;consumed=true;
        if(scrollRemainder*delta<0)scrollRemainder=0;
        scrollRemainder+=delta;
        int steps=(int)Math.min(64,Math.floor(Math.abs(scrollRemainder)));
        for(int i=0;i<steps;i++)ClientPlayNetworking.send(new DomainWheelPayload(2,open,delta>0?1:-1));
        scrollRemainder-=Math.copySign(steps,scrollRemainder);
        return true;
    }
}
