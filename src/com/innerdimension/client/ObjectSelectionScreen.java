package com.innerdimension.client;
import com.innerdimension.domain.runtime.DomainObjects;
import net.minecraft.*;

/** Edits one creature's page-local draft; engraving commits it with the complete loadout. */
final class ObjectSelectionScreen extends class_437 {
    static final int[] COLORS={0xffff8fbd,0xff65db78,0xffffdb62,0xffff6868,0xff65a8ff,0xffffffff};
    static final int[] BITS={DomainObjects.SELF,DomainObjects.FRIENDLY,DomainObjects.NEUTRAL,DomainObjects.HOSTILE,DomainObjects.ACTIVE,DomainObjects.PASSIVE};
    private static final int W=360,H=252,ROW_H=24;
    private static final String[] KEYS={"self","0","1","2","all","3","4"};
    private final DomainCoreScreen parent;private final String id;private int mask,left,top;
    ObjectSelectionScreen(DomainCoreScreen parent,int slot,String id,int mask){super(class_2561.method_43471("screen.inner_dimension.objects.title"));this.parent=parent;this.id=id;this.mask=mask&allowed();}
    protected void method_25426(){left=(field_22789-W)/2;top=(field_22790-H)/2;}
    private int allowed(){var ability=com.innerdimension.domain.DomainAbilityCatalog.byEntity(id);return ability==null?63:DomainObjects.availableMask(ability.code());}
    private int rowBit(int row){return (row==4?DomainObjects.ALL_CREATURES:BITS[row>4?row-1:row])&allowed();}
    public void method_25394(class_332 g,int mx,int my,float delta){
        parent.method_25394(g,-1,-1,delta);g.method_25294(0,0,field_22789,field_22790,0xa0000000);
        g.method_25294(left,top,left+W,top+H,0xff29232f);
        g.method_27534(field_22793,field_22785,left+W/2,top+12,0xffffffff);
        for(int row=0;row<KEYS.length;row++){
            int y=top+34+row*ROW_H,bit=rowBit(row);boolean enabled=bit!=0&&(mask&bit)==bit;
            int color=row==4?0xffe6baff:COLORS[row>4?row-1:row];
            if(bit==0)color=0xff777777;
            g.method_25294(left+18,y,left+W-18,y+21,0xff514857);
            // drawText is explicitly left-aligned; drawCenteredText caused the old border overflow.
            g.method_51439(field_22793,class_2561.method_43470(enabled?"[✓]":"[ ]"),left+30,y+6,color,false);
            g.method_51439(field_22793,class_2561.method_43471("screen.inner_dimension.objects."+KEYS[row]),left+58,y+6,color,false);
            var description=class_2561.method_43471("screen.inner_dimension.objects."+KEYS[row]+".description");
            var matrices=g.method_51448();matrices.pushMatrix();matrices.translate(left+126,y+6);
            float scale=Math.min(1f,(W-150f)/Math.max(1,field_22793.method_27525(description)));matrices.scale(scale,scale);
            g.method_51439(field_22793,description,0,0,0xffded7e6,false);matrices.popMatrix();
        }
        int saveX=left+(W-88)/2;g.method_25294(saveX,top+218,saveX+88,top+240,0xff685674);
        g.method_27534(field_22793,class_2561.method_43471("screen.inner_dimension.save"),left+W/2,top+225,0xffffffff);
    }
    public boolean method_25402(class_11909 e,boolean doubled){
        if(e.method_74228()!=0)return true;double x=e.comp_4798(),y=e.comp_4799();
        for(int row=0;row<KEYS.length;row++)if(x>=left+18&&x<left+W-18&&y>=top+34+row*ROW_H&&y<top+55+row*ROW_H){int bit=rowBit(row);if(bit==0)return true;if(row==4)mask=(mask&bit)==bit?mask&~bit:mask|bit;else mask^=bit;return true;}
        int saveX=left+(W-88)/2;if(x>=saveX&&x<saveX+88&&y>=top+218&&y<top+240){parent.saveObjects(id,mask);field_22787.method_1507(parent);}return true;
    }
    public void method_25419(){field_22787.method_1507(parent);}
}
