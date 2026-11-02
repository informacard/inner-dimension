import com.innerdimension.projection.*;
import com.innerdimension.network.DomainHudPayload;
import net.minecraft.*;
import io.netty.buffer.Unpooled;
import java.lang.reflect.*;
import java.util.*;

public final class DomainExpansionRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        var packet=new DomainHudPayload(false,"？？？",0,0,0,333,667,"",0xffffff,0xffffff,0xffffff,333,667);
        var clash=new DomainHudPayload(true,"无量空处𠀀",0x123456,0xffaa00,0x778899,100,900,"伏魔御厨子",0xff0000,0x220033,0x0000ff,200,800);
        var timed=new DomainHudPayload(false,"黑色领域",0,0,0,333,667,"",0,0,0,333,667,15,5,1);
        check(timed.titleTicks()==15&&timed.nameTicks()==5,"Remote title precedes five-tick name phase");
        check(com.innerdimension.client.DomainSubtitlePainter.dark(0xff000000),"Black names require a bright outline");
        check(!com.innerdimension.client.DomainSubtitlePainter.dark(0xffffffff),"White lettering keeps its original appearance");
        check(!com.innerdimension.client.DomainSubtitlePainter.dark(0xff713DA6),"Purple villager name does not need an outline");
        for(var p:List.of(packet,clash,timed)){
            var b=new class_9129(Unpooled.buffer(),null);
            try{DomainHudPayload.CODEC.encode(b,p);check(DomainHudPayload.CODEC.decode(b).equals(p),"Names and all gradient bands travel without tracked projection lookup");}finally{b.release();}
        }
        int chipped=0;
        for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++) {
            var start=new ProjectionAnimation.BreakFrame(0,12345,true);
            var chips=new ProjectionAnimation.BreakFrame(4,12345,true);
            var middle=new ProjectionAnimation.BreakFrame(7,12345,true);
            var end=new ProjectionAnimation.BreakFrame(10,12345,true);
            check(ProjectionAnimation.breakScale(start,x,0,z)==1,"Break starts at full scale");
            double scale=ProjectionAnimation.breakScale(chips,x,0,z);
            if(scale==0)chipped++;else check(scale==1,"First phase only removes selected shell blocks");
            check(ProjectionAnimation.breakScale(middle,x,0,z)<=scale,"Every block shrinks monotonically");
            check(ProjectionAnimation.breakScale(end,x,0,z)==0,"All blocks disappear at the endpoint");
            check(ProjectionAnimation.breakScale(new ProjectionAnimation.BreakFrame(4,12345,false),x,0,z)==1,"Interior survives the shell chip phase");
            check(ProjectionAnimation.breakScale(new ProjectionAnimation.BreakFrame(7,12345,false),x,0,z)==.5,"Interior shrinks in the second phase");
        }
        check(chipped>40&&chipped<150,"Random shell subset, not the whole shell");
        var snapshot=new RoomProjectionContraption();
        var pos=new class_2338(0,0,0);
        var stone=class_7923.field_41175.method_63535(class_2960.method_12829("minecraft:stone")).method_9564();
        snapshot.getBlocks().put(pos,new class_3499.class_3501(pos,stone,null));
        var type=Class.forName("com.innerdimension.projection.ProjectionSafety$Geometry");
        var constructor=type.getDeclaredConstructor(RoomProjectionContraption.class,class_243.class,class_1922.class);constructor.setAccessible(true);
        var geometry=constructor.newInstance(snapshot,new class_243(100,10,100),snapshot.snapshotView());
        var add=type.getDeclaredMethod("addAt",RoomProjectionContraption.class,class_1922.class,class_243.class);add.setAccessible(true);
        add.invoke(geometry,snapshot,snapshot.snapshotView(),new class_243(100,11,100));
        var collides=type.getDeclaredMethod("collides",ProjectionSafetySearch.Box.class);collides.setAccessible(true);
        var body=new ProjectionSafetySearch.Box(100.2,11.001,100.2,100.8,12.801,100.8);
        check((boolean)collides.invoke(geometry,body),"Second domain floor overlaps the first caster's old landing");
        var raised=body.at(new ProjectionSafetySearch.Point(100.5,12.001,100.5));
        check(!(boolean)collides.invoke(geometry,raised),"Combined future geometry accepts the raised landing");
        check((boolean)collides.invoke(geometry,new ProjectionSafetySearch.Box(100.2,12-.02,100.2,100.8,12,100.8)),"Raised position is supported by second floor");
        check(!(boolean)collides.invoke(geometry,new ProjectionSafetySearch.Box(.2,11.001,.2,.8,12.801,.8)),"Different anchors never leak into source-space collision positions");
        var physical=constructor.newInstance(snapshot,new class_243(100,10,100),snapshot.snapshotView());
        add.invoke(physical,snapshot,snapshot.snapshotView(),new class_243(100,11,100));
        var realFloor=new ProjectionSafetySearch.Box(100,12,100,101,13,101);
        var space=new ProjectionSafetySearch.Space(){
            public boolean clear(ProjectionSafetySearch.Box b){try{return !(boolean)collides.invoke(physical,b)&&!realFloor.intersects(b);}catch(Exception e){throw new RuntimeException(e);}}
            public boolean supported(ProjectionSafetySearch.Box b){try{var below=new ProjectionSafetySearch.Box(b.minX(),b.minY()-.02,b.minZ(),b.maxX(),b.minY(),b.maxZ());return (boolean)collides.invoke(physical,below)||realFloor.intersects(below);}catch(Exception e){throw new RuntimeException(e);}}
            public List<Double> surfaces(ProjectionSafetySearch.Box b,double min,double max){return List.of(11d,12d,13d);}
        };
        for(double height:List.of(1.8,1.95)) {
            var actor=new ProjectionSafetySearch.Box(body.minX(),body.minY(),body.minZ(),body.maxX(),body.minY()+height,body.maxZ());
            var landing=ProjectionSafetySearch.occupant(actor,space,11,16,4);
            check(landing!=null&&landing.x()==actor.x()&&landing.z()==actor.z()&&Math.abs(landing.y()-13.001)<1e-7,"Player and villager clear both domain floors and the real floor without moving to center");
            check(space.clear(actor.at(landing))&&space.supported(actor.at(landing)),"Final body clears all three floors and has actual support");
            var preserved=ProjectionSafetySearch.occupant(actor.at(landing),space,11,16,4);
            check(landing.equals(preserved),"Already safe covered entities remain at their original location");
        }
        System.out.println("PASS: 15/5 tick remote HUD, purple name, wire state, fracture, both domain floors plus real terrain, player/villager landings");
    }
}
