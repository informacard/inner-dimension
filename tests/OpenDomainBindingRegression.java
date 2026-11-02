import com.innerdimension.projection.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
import java.util.*;

public final class OpenDomainBindingRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void close(double actual,double expected,String message){check(Math.abs(actual-expected)<1e-6,message);}
    public static void main(String[] args){
        class_155.method_36208();class_2966.method_12851();
        for(double from:new double[]{1,.5,.125})for(int elapsed=0;elapsed<=40;elapsed++){
            double progress=OpenDomainRules.progress(ProjectionAnimation.RETRACTING,elapsed,from,80);
            close(progress,from*(1-elapsed/40.0),"Retraction follows its real forty-tick duration even when partially expanded");
        }
        UUID owner=UUID.randomUUID(),other=UUID.randomUUID();OpenDomainBinding state=new OpenDomainBinding();
        check(state.radius(owner)==24&&state.duration(owner)==2560,"new defaults");
        for(int i=0;i<10;i++)check(state.adjust(owner,1),"valid upward step");
        check(state.radius(owner)==64&&state.duration(owner)==160,"upper endpoint");
        check(!state.adjust(owner,1)&&state.radius(owner)==64&&state.duration(owner)==160,"invalid step is atomic");
        for(int i=0;i<15;i++)check(state.adjust(owner,-1),"valid downward step");
        check(state.radius(owner)==4&&state.duration(owner)==3760,"lower endpoint");
        check(!state.adjust(owner,-1)&&state.radius(owner)==4&&state.duration(owner)==3760,"reject zero radius");
        check(state.radius(other)==24,"independent owners");
        var encoded=OpenDomainBinding.CODEC.encodeStart(JsonOps.INSTANCE,state).getOrThrow();
        var restored=OpenDomainBinding.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        check(restored.radius(owner)==4&&restored.duration(owner)==3760,"binding survives save/load");
        check(OpenDomainBinding.burnout(80,2560,false)==60,"minimum three seconds");
        check(OpenDomainBinding.burnout(800,2560,false)==100,"manual one eighth");
        check(OpenDomainBinding.burnout(0,2560,true)==320,"automatic one eighth of maximum");
        check(OpenDomainBinding.burnout(800,160,true)==60,"small maximum keeps minimum");
        check(OpenDomainRules.expansionTicks(32)==80&&OpenDomainRules.expansionTicks(24)==60,"legacy speed preserved");
        for(int radius:new int[]{4,24,28,64}){int duration=OpenDomainRules.expansionTicks(radius);close(radius*OpenDomainRules.progress(1,5,1,duration),2,"constant speed");close(OpenDomainRules.progress(1,duration,1,duration),1,"expansion endpoint");}
        double last=1;for(int tick=0;tick<=40;tick++){double next=OpenDomainRules.progress(3,tick,.8,60);check(next<=last,"monotonic partial retraction");last=next;}
        close(OpenDomainRules.progress(3,20,1,60),.5,"two second retraction midpoint");close(OpenDomainRules.progress(3,40,1,60),0,"retraction endpoint");
        check(OpenDomainRules.contains(10,0,0,12)&&!OpenDomainRules.contains(10,0,0,6),"shrinking membership");
        check(!OpenDomainRules.movementReady(1,19)&&OpenDomainRules.movementReady(1,20),"one second immobilization");
        for(var p:List.of(new DomainWheelPayload(0,false,0),new DomainWheelPayload(1,true,6),new DomainWheelPayload(2,true,-1))){var b=new class_9129(Unpooled.buffer(),null);try{DomainWheelPayload.CODEC.encode(b,p);check(DomainWheelPayload.CODEC.decode(b).equals(p),"wheel request wire round trip");}finally{b.release();}}
        var p=new DomainWheelStatePayload(true,List.of("一","二","三","四","五","六","虚空"));var b=new class_9129(Unpooled.buffer(),null);try{DomainWheelStatePayload.CODEC.encode(b,p);check(DomainWheelStatePayload.CODEC.decode(b).equals(p),"wheel names and seventh unlock wire round trip");}finally{b.release();}
        check(com.innerdimension.client.DomainWheelLayout.slotAt(0,0,100,36,false)==-1,"locked centre remains empty");
        check(com.innerdimension.client.DomainWheelLayout.slotAt(0,0,100,36,true)==6,"unlocked centre selects seventh");
        check(com.innerdimension.client.DomainWheelLayout.slotAt(101,0,100,36,true)==-1,"outside wheel is inert");
        for(int slot=0;slot<6;slot++){double angle=-Math.PI/2+(slot+.5)*Math.PI/3;check(com.innerdimension.client.DomainWheelLayout.slotAt(Math.cos(angle)*70,Math.sin(angle)*70,100,36,false)==slot,"sector selects corresponding slot");}
        var cfg=com.innerdimension.config.InnerConfig.get();
        cfg.openDomainInitialRadius=40;cfg.openDomainInitialDurationSeconds=300;
        var custom=new OpenDomainBinding();
        check(custom.radius(other)==40&&custom.duration(other)==6000,"server configured baseline");
        for(int i=0;i<20;i++)check(custom.adjust(other,1),"configured duration permits larger binding range");
        var customSaved=OpenDomainBinding.CODEC.encodeStart(JsonOps.INSTANCE,custom).getOrThrow();
        var customRestored=OpenDomainBinding.CODEC.parse(JsonOps.INSTANCE,customSaved).getOrThrow();
        check(customRestored.radius(other)==120&&customRestored.duration(other)==1200,"configured binding survives beyond legacy step limits");
        cfg.openDomainInitialRadius=8;cfg.openDomainInitialDurationSeconds=24;
        check(customRestored.radius(other)>0&&customRestored.duration(other)>0,"old bindings remain positive after configuration reduction");
        cfg.openDomainInitialRadius=24;cfg.openDomainInitialDurationSeconds=128;
        System.out.println("OpenDomainBindingRegression passed: "+checks+" checks");
    }
}
