import com.innerdimension.domain.*;
import com.innerdimension.domain.ability.*;
import com.innerdimension.domain.runtime.*;
import com.innerdimension.client.domain.DomainSlashRenderer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.*;
import java.util.*;
import net.minecraft.*;

public final class SlashShrineRegression {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static class_243 v(double x,double y,double z){return new class_243(x,y,z);}
    public static void main(String[] args)throws Exception {
        check(D44.SPEED==2&&D44.DAMAGE==4,"Requested flight speed and raw damage");
        check(D44.COOLDOWN==7,"0.33-second cooldown rounds upward to seven ticks");
        check(DomainSlashMath.intersects(v(0,0,0),v(2,0,0),new class_238(1,-.2,-.2,1.3,.2,.2),.6),"Swept flight hits creatures between ticks");
        check(!DomainSlashMath.intersects(v(0,0,0),v(2,0,0),new class_238(1,3,-.2,1.3,4,.2),.6),"The flight cannot hit off-path creatures");
        check(DomainSlashMath.intersects(v(0,0,0),v(2,0,0),new class_238(-.2,-.2,-.2,.2,.2,.2),0),"Starting inside a target is not missed");
        class_243 edge=DomainSlashMath.clipToRange(v(0,0,0),v(2,0,0),p->p.field_1352<=1);
        check(edge.field_1352<=1&&edge.field_1352>.999,"Boundary is clipped before leaving the domain");
        check(DomainSlashMath.clipToRange(v(79,0,0),v(81,0,0),p->p.field_1352<=100).field_1352==81,"No artificial 32-block distance limit");
        check(D44.enemy(DomainRelations.Relation.ENEMY,false)&&D44.enemy(DomainRelations.Relation.BOSS,false),"Hostiles and bosses are eligible");
        check(D44.enemy(DomainRelations.Relation.ALLY,true)&&!D44.enemy(DomainRelations.Relation.SELF,true),"Ordinary slashes include allies and protect only owner");
        check(D44.enemy(DomainRelations.Relation.NEUTRAL,true),"Previously attacked objects remain eligible");
        check(D76.INTERVAL==4&&D76.DAMAGE==2,"Continuous Shrine frequency and damage");
        check(DomainObjects.configurable("D76")&&DomainObjects.defaultMask("D76")== (DomainObjects.ALL_CREATURES&~DomainObjects.SELF),"Shrine supports selected targets, preserving prior other-creature default");
        check((DomainObjects.availableMask("D76")&DomainObjects.SELF)==0,"Shrine self selection is unavailable");
        check(!DomainModes.owned(List.of("minecraft:vindicator"),DomainModes.SHRINE),"Vindicator no longer unlocks Shrine");
        check(!DomainModes.owned(List.of("minecraft:goat"),DomainModes.SHRINE),"Another hidden reverse cannot unlock Shrine");
        check(DomainModes.sourceSpecies(DomainModes.SHRINE).equals(DomainModes.SHRINE),"Shrine is independent of conception species");
        check(DomainAbilityCatalog.weight(List.of("minecraft:vindicator"))==4&&DomainAbilityCatalog.weight(List.of(DomainModes.SHRINE))==8,"Independent real capacity costs");
        check(DomainAbilityCatalog.sanitize(List.of("minecraft:vindicator",DomainModes.SHRINE)).size()==2,"Independent abilities can coexist");
        var shrine=DomainAbilityCatalog.byEntity(DomainModes.SHRINE);
        check(shrine.colorWeight()==10&&shrine.color()==0xC00020,"Shrine has blood-red core priority");
        check(DomainAbilityCatalog.nameColors(List.of(DomainModes.SHRINE,"minecraft:wolf"))[1]==0xC00020,"Shrine controls the central name colour");
        for(var entry:Map.of("D09",new int[]{4,7},"D26",new int[]{1,3},"D30",new int[]{2,4},"D34",new int[]{3,5},"D64",new int[]{3,6},"D73",new int[]{2,4}).entrySet()){
            var ability=DomainAbilityCatalog.ALL.stream().filter(a->a.code().equals(entry.getKey())).findFirst().orElseThrow();
            check(ability.loadWeight()==entry.getValue()[0]&&ability.colorWeight()==entry.getValue()[1],"Balance table: "+entry.getKey());
        }
        renderBuffer();
        System.out.println("PASS: swept hits, domain boundary, flight/cooldown, reverse authorization/cost, balance and slash vertex format");
    }
    private static void renderBuffer()throws Exception {
        var draw=DomainSlashRenderer.class.getDeclaredMethod("edge",class_4588.class,class_4587.class_4665.class,class_243.class,class_243.class,int.class,float.class,float.class);
        draw.setAccessible(true);
        VertexFormat format=class_290.field_63455;
        try(class_9799 arena=new class_9799(4096)){
            class_287 vertices=new class_287(arena,VertexFormat.class_5596.field_27377,format);
            draw.invoke(null,vertices,new class_4587().method_23760(),v(0,0,0),v(1,1,1),D76.COLOR,1f,6f);
            try(class_9801 mesh=vertices.method_60800()){
                check(mesh!=null,"Slash emits a real Minecraft line mesh");
                ByteBuffer data=mesh.method_60818().order(ByteOrder.nativeOrder());
                int stride=format.getVertexSize(),width=format.getOffset(VertexFormatElement.LINE_WIDTH),normal=format.getOffset(VertexFormatElement.NORMAL);
                for(int offset=0;offset<data.remaining();offset+=stride){
                    check(data.getFloat(offset+width)==6,"Every slash vertex carries its width");
                    check(data.get(offset+normal)>0&&data.get(offset+normal+1)>0&&data.get(offset+normal+2)>0,"Arbitrary 3D slash direction reaches the line shader");
                }
            }
        }
    }
}
