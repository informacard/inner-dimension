import com.innerdimension.projection.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.progression.*;
import com.innerdimension.item.OpenDomainCorePayload;
import com.innerdimension.pregnancy.MentalPregnancyChance;
import com.innerdimension.projection.builtin.AbstractContraptionEntity;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
import java.util.*;

public final class Update146Regression {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static void field(Object target,Class<?> owner,String name,Object value)throws Exception{
        var f=owner.getDeclaredField(name);f.setAccessible(true);f.set(target,value);
    }
    public static void main(String[] args)throws Exception{
        class_155.method_36208();class_2966.method_12851();
        for(boolean strength:List.of(false,true)){
            var original=new OpenDomainCorePayload(List.of("inner_dimension:void"),"",0,6,List.of("虚空"),List.of("inner_dimension:void"),true,true,true,6,false,List.of("6|inner_dimension:void|32"),strength);
            var buffer=new class_9129(Unpooled.buffer(),null);
            try{
                OpenDomainCorePayload.CODEC.encode(buffer,original);
                check(OpenDomainCorePayload.CODEC.decode(buffer).equals(original),"Unlock flag survives wire round trip");
                check(original.withPreviewState(-3).strengthUnlocked()==strength,"Refresh preserves strength gate");
            }finally{buffer.release();}
        }
        check(!DomainObjects.configurable("D71"),"Void has fixed all-creature targets");
        check(DomainObjects.defaultMask("D71")==DomainObjects.ALL_CREATURES,"Void covers self and every allegiance");
        for(int steps:List.of(0,1,10))for(int amplifier:List.of(0,1,5)){
            check(MentalPregnancyChance.probability(steps,amplifier,true)==1,"Void plus either fertility level is certain");
            check(MentalPregnancyChance.probability(steps,amplifier,false)==MentalPregnancyChance.probability(steps,amplifier),"Other engravings preserve probability");
        }
        check(MentalPregnancyChance.probability(0,-1,true)==0,"Void without potion preserves normal roll");
        for(float velocity:new float[]{.21f,.42f,.52f,.62f,1.42f})for(double gravity:new double[]{.04,.08,.16}){
            double normal=VillagerJumpRules.height(velocity,gravity);
            double reduced=VillagerJumpRules.height(VillagerJumpRules.quarter(velocity,gravity),gravity);
            check(Math.abs(reduced/normal-.25)<1e-6,"Quarter apex includes Jump Boost and discrete air drag");
        }
        var state=new DomainState();UUID id=UUID.randomUUID();
        check(state.slotName(id,6).equals("虚空"),"New seventh name defaults to Void");
        state.renameSlot(id,6,"custom");check(state.slotName(id,6).equals("custom"),"Custom name survives defaults");
        state.selectSlot(id,6);state.selectSlot(id,0);check(state.selectedSlot(id)==0,"First slot persisted after ritual");
        var unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        var unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        var shell=(RoomProjectionEntity)unsafe.allocateInstance(RoomProjectionEntity.class);
        var snapshot=new RoomProjectionContraption();
        var volume=new ProjectionSourceSpec.Volume(-24,0,-24,39,63,39);
        snapshot.sourceSpec(new ProjectionSourceSpec(volume,null,new ProjectionSourceSpec.Volume(0,33,0,15,47,15),8,32,8));
        field(shell,AbstractContraptionEntity.class,"contraption",snapshot);
        shell.method_5857(new class_238(68,32,68,132,96,132));
        var session=(DomainSession)unsafe.allocateInstance(DomainSession.class);
        field(session,DomainSession.class,"shell",shell);field(session,DomainSession.class,"loadout",List.of("minecraft:chicken"));
        session.capture(1);
        check(session.open()&&session.radius()==32,"Open session captures full radius without wall inset");
        check(session.containsPoint(131.99,64,100),"Entity beyond the copied building is covered");
        check(!session.containsPoint(132.01,64,100),"Outside radius is rejected");
        check(!session.containsPoint(131,64,131),"Cube corners are outside spherical radius");
        check(session.contains(new class_238(131.5,64,99.9,132.5,65,100.1)),"Boundary-crossing body is covered");
        check(!session.contains(new class_238(132.1,64,100,133,65,101)),"Fully outside body is rejected");
        snapshot.sourceSpec(ProjectionSourceSpec.room());session.capture(2);
        check(!session.open()&&session.radius()==31,"Closed sessions retain original wall inset");
        double last=0;
        for(int age=0;age<=OpenDomainRules.EXPANSION_TICKS;age++){
            double radius=ProjectionAnimation.progress(ProjectionAnimation.INTERIOR,age,0,OpenDomainRules.EXPANSION_TICKS)*32;
            check(radius>=last&&radius-last<=.400001,"Visible wave advances smoothly over four seconds");last=radius;
        }
        System.out.println("PASS 1.4.46: screen wire flags, fixed void scope, fertility probability, quarter jump apex, slot defaults, open/closed membership, radial timeline");
    }
}
