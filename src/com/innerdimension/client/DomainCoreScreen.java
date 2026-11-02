package com.innerdimension.client;
import com.innerdimension.client.crest.DomainCrestClient;
import com.innerdimension.domain.DomainAbilityCatalog;
import com.innerdimension.network.UpdateDomainSlotPayload;
import java.awt.image.BufferedImage;
import java.util.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.*;

public final class DomainCoreScreen extends class_437 {
    private static final int W=390,H=286,ROWS=5;
    private final List<DomainAbilityCatalog.Ability> available=new ArrayList<>();
    private final LinkedHashSet<String> draft=new LinkedHashSet<>(),engraved=new LinkedHashSet<>();
    private final int capacity;private final boolean overloadUnlocked;private final Map<String,Integer> objectMasks=new HashMap<>();
    private final String previewSource;private final int previewState,slot;private final List<String> slotNames,slotLoadouts;private final boolean revealAbilities,imprintUnlocked,mentalVolumeUnlocked,strengthUnlocked;
    private final class_4185[] upper=new class_4185[ROWS],lower=new class_4185[ROWS];
    private final DomainAbilityCatalog.Ability[] upperAbility=new DomainAbilityCatalog.Ability[ROWS],lowerAbility=new DomainAbilityCatalog.Ability[ROWS];
    private int left,top,scrollA,scrollB;private class_342 search;private String query="";
    private int sortMode,dragList=-1;private boolean reversed;private double dragOffset;
    private class_4185 sortButton;
    private boolean openUnlocked;private List<Integer> floorStates=List.of();private com.innerdimension.projection.FloorPatternSettings floorPattern=com.innerdimension.projection.FloorPatternSettings.DEFAULT;
    private final com.ibm.icu.text.Collator pinyin=com.ibm.icu.text.Collator.getInstance(new com.ibm.icu.util.ULocale("zh@collation=pinyin"));
    private final Map<String,String> englishNames=loadEnglishNames();
    public DomainCoreScreen(){this(List.of(),"",0,0,List.of("","",""),List.of("","",""),false,false,false,6,false,List.of(),false);}
    public DomainCoreScreen(List<String> species,String previewSource,int previewState,int slot,List<String> names,List<String> loads,boolean revealAbilities,boolean imprintUnlocked,boolean mentalVolumeUnlocked,int capacity,boolean overloadUnlocked,List<String> objectSelections,boolean strengthUnlocked){
        super(class_2561.method_43471("screen.inner_dimension.domain_core.title"));this.capacity=capacity;this.overloadUnlocked=overloadUnlocked;for(String entry:objectSelections){String[] parts=entry.split("\\|",3);if(parts.length==3&&parts[0].equals(Integer.toString(slot)))try{objectMasks.put(parts[1],Integer.parseInt(parts[2])&63);}catch(NumberFormatException ignored){}}this.previewSource=previewSource;this.previewState=previewState;this.slot=Math.max(0,Math.min(loads.size()-1,slot));DomainCrestClient.currentDesign();com.innerdimension.client.crest.CrestDesignStore.select(this.slot);com.innerdimension.client.crest.CrestDesignStore.importNames(names);this.slotNames=java.util.stream.IntStream.range(0,loads.size()).mapToObj(com.innerdimension.client.crest.CrestDesignStore::name).toList();this.slotLoadouts=loads;this.revealAbilities=revealAbilities;this.imprintUnlocked=imprintUnlocked;this.mentalVolumeUnlocked=mentalVolumeUnlocked;this.strengthUnlocked=strengthUnlocked;
        for(String id:species){var a=DomainAbilityCatalog.forRecordedEntity(id);if(a!=null)available.add(a);}available.sort(Comparator.comparingInt((DomainAbilityCatalog.Ability a)->a.code().equals("D00")?Integer.MAX_VALUE:a.number()).thenComparing(DomainAbilityCatalog.Ability::entityId));
        if(this.slot<loads.size()&&!loads.get(this.slot).isEmpty())engraved.addAll(Arrays.asList(loads.get(this.slot).split("\u001f",-1)));draft.addAll(engraved);
    }
    public DomainCoreScreen(List<String> species,String previewSource,int previewState,int slot,List<String> names,List<String> loads,boolean revealAbilities,boolean imprintUnlocked,boolean mentalVolumeUnlocked,int capacity,boolean overloadUnlocked,List<String> objectSelections,boolean strengthUnlocked,boolean openUnlocked,List<Integer> floorStates,com.innerdimension.projection.FloorPatternSettings floorPattern) {
        this(species,previewSource,previewState,slot,names,loads,revealAbilities,imprintUnlocked,mentalVolumeUnlocked,capacity,overloadUnlocked,objectSelections,strengthUnlocked);
        this.openUnlocked=openUnlocked;this.floorStates=floorStates;this.floorPattern=floorPattern;
    }
    protected void method_25426(){left=(field_22789-W)/2;top=(field_22790-H)/2;dragList=-1;search=method_37063(new class_342(field_22793,left+166,top+27,155,18,class_2561.method_43471("screen.inner_dimension.species.search")));search.method_1880(128);search.method_47404(class_2561.method_43471("screen.inner_dimension.species.search"));search.method_1863(v->{query=v;scrollA=0;refresh();});search.method_1852(query);
        sortButton=method_37063(new Button(left+325,top+27,24,18,sortLabel(),b->{sortMode=(sortMode+1)%4;sortButton.method_25355(sortLabel());scrollA=scrollB=0;refresh();}));
        method_37063(new Button(left+353,top+27,23,18,directionLabel(),b->{reversed=!reversed;b.method_25355(directionLabel());scrollA=scrollB=0;refresh();}));
        method_37063(new Button(left+24,top+137,112,20,class_2561.method_43471("screen.inner_dimension.inject"),b->ClientPlayNetworking.send(com.innerdimension.network.SubmitDomainBottlePayload.INSTANCE)));
        // The engrave pair is the endurance chapter's payoff: without it the draft has no meaning,
        // so both buttons stay unregistered (and therefore undrawn) until the advancement is done.
        if(imprintUnlocked){
            method_37063(new Button(left+166,top+248,102,22,class_2561.method_43471("screen.inner_dimension.engrave"),b->engrave()));
            method_37063(new Button(left+274,top+248,102,22,class_2561.method_43471("screen.inner_dimension.reset_draft"),b->{draft.clear();draft.addAll(engraved);refresh();}));
        }
        if(openUnlocked&&floorStates.size()==256)method_37063(new Button(left+18,top+248,130,22,class_2561.method_43471("screen.inner_dimension.floor.entry"),b->field_22787.method_1507(new FloorPatternScreen(this,floorStates,floorPattern))));
        for(int i=0;i<ROWS;i++){final int row=i;upper[i]=method_37063(new Button(left+166,top+52+i*20,202,19,class_2561.method_43470(""),b->choose(row,false)));lower[i]=method_37063(new Button(left+166,top+158+i*17,202,16,class_2561.method_43470(""),b->choose(row,true)));}
    }
    public void method_25394(class_332 g,int mx,int my,float delta){panel(g,left,top,W,H);int bx=left+14,by=top+28,bw=136,bh=92;box(g,bx,by,bw,bh);drawDesign(g,DomainCrestClient.currentDesign(),bx+12,by+8,bw-24,bh-16);refresh();super.method_25394(g,mx,my,delta);g.method_27534(field_22793,field_22785,field_22789/2,top+8,0xffffffff);
        // Left column is only as wide as the gap before the two lists; every notice wraps there.
        int lw=142,y=top+164;
        text(g,class_2561.method_43469("screen.inner_dimension.slot.current",slot+1,slotName()),left+18,y,0xffe8dff0);y+=15;
        text(g,class_2561.method_43469(mentalVolumeUnlocked?"screen.inner_dimension.total_volume":"screen.inner_dimension.total_weight",DomainAbilityCatalog.infinite(draft)?"∞":overloadUnlocked?DomainAbilityCatalog.weight(draft)+"/"+capacity:DomainAbilityCatalog.weight(draft)),left+18,y,0xffe8dff0);
        if(strengthUnlocked){
        var strength=strength();
        var strengthText=class_2561.method_43469("screen.inner_dimension.strength",strength.format(strength.total()));
        text(g,strengthText,left+158-field_22793.method_27525(strengthText),y,0xfff0b8dc);
        }
        y+=15;
        // The "not imprinted yet" notice is part of the engrave feature; a locked player never sees it.
        if(imprintUnlocked){
            boolean engravedNow=draft.equals(engraved);
            for(String line:lines(class_2561.method_43471(engravedNow?"screen.inner_dimension.draft.engraved":"screen.inner_dimension.draft.pending").getString(),lw)){text(g,class_2561.method_43470(line),left+18,y,engravedNow?0xff8fe69b:0xffffcf72);y+=10;}
        } else {
            for(String line:lines(class_2561.method_43471("screen.inner_dimension.imprint.wait").getString(),lw)){text(g,class_2561.method_43470(line),left+18,y,0xffbcbcbc);y+=10;}
        }
        for(String key:DomainAbilityCatalog.conflicts(draft)){
            // The hard conflict talks about imprinting, so it is held back with the button; the
            // ability-interaction tips reveal nothing about the feature and stay visible.
            if(!imprintUnlocked&&key.contains(".conflict."))continue;
            int color=key.contains(".conflict.")?0xffff6b6b:0xffffcf72;
            for(String line:lines(class_2561.method_43471(key).getString(),lw)){text(g,class_2561.method_43470(line),left+18,y,color);y+=10;}
        }
        y=Math.min(y+13,top+215);
        if(previewState==-1){text(g,class_2561.method_43471("screen.inner_dimension.injection.unknown"),left+18,y,0xffff6b6b);text(g,class_2561.method_43471("screen.inner_dimension.injection.failed"),left+18,y+15,0xffff6b6b);}
        else if(previewState>0){text(g,class_2561.method_43471(entityKey(previewSource)),left+18,y,0xffffffff);text(g,class_2561.method_43471(previewState==2?"screen.inner_dimension.injected":"screen.inner_dimension.not_injected"),left+18,y+15,previewState==2?0xffb77cff:0xffbcbcbc);}
        // Collected count lives in the panel's top-right corner, right-aligned, on the title line;
        // it must never take width away from the species search field again.
        if(mentalVolumeUnlocked){
        var collected=class_2561.method_43469("screen.inner_dimension.collected",available.size());
        text(g,collected,left+W-10-field_22793.method_27525(collected),top+8,0xffe8dff0);
        }
        for(int row=0;row<ROWS;row++)if(lowerAbility[row]!=null){if(com.innerdimension.domain.DomainModes.selectable(lowerAbility[row].code())){int mode=mask(lowerAbility[row]);g.method_25294(left+280,top+158+row*17+12,left+288,top+158+row*17+15,mode==2?0xffffffff:0xff000000);continue;}if(!objectsEnabled(lowerAbility[row]))continue;int mask=mask(lowerAbility[row]);for(int n=0;n<6;n++)g.method_25294(left+258+n*7,top+158+row*17+12,left+263+n*7,top+158+row*17+15,(mask&ObjectSelectionScreen.BITS[n])!=0?ObjectSelectionScreen.COLORS[n]:0xff777777);}
        scrollbar(g,0,unselected().size(),mx,my);scrollbar(g,1,chosen().size(),mx,my);tip(g,mx,my);
    }
    /** Ability detail beside the hovered row: anchored, so it never trails the cursor over the list. */
    private void tip(class_332 g,int mx,int my){DomainAbilityCatalog.Ability a=null;int rowY=0;
        for(int i=0;i<ROWS;i++){
            if(upper[i]!=null&&upper[i].field_22763&&inside(mx,my,left+166,top+52+i*20,202,19)){a=upperAbility[i];rowY=top+52+i*20;break;}
            if(lower[i]!=null&&lower[i].field_22763&&inside(mx,my,left+166,top+158+i*17,202,16)){a=lowerAbility[i];rowY=top+158+i*17;break;}
        }
        if(a==null)return;
        int pad=5,w=142,x=left+12,body=w-pad*2;
        List<String> text=(revealAbilities||a.code().equals("D00"))?lines(a.description().getString(),body):List.of("???");
        List<String> settings=revealAbilities?lines(class_2561.method_43471(com.innerdimension.domain.DomainModes.selectable(a.code())?"screen.inner_dimension.settings.reverse":objectsEnabled(a)?"screen.inner_dimension.settings.objects":"screen.inner_dimension.objects.disabled").getString(),body):List.of();
        if(revealAbilities&&a.loadWeight()==Integer.MAX_VALUE){settings=new ArrayList<>(settings);settings.addAll(lines(class_2561.method_43469("screen.inner_dimension.settings.capacity", "∞").getString(),body));}
        int h=pad+10+2+text.size()*10+(settings.isEmpty()?0:5+settings.size()*10)+pad-3;
        int y=Math.max(top+8,Math.min(rowY,top+H-6-h));
        g.method_25294(x,y,x+w,y+h,0xff0c0a10);outline(g,x,y,w,h,0xff7d7192);
        if(revealAbilities&&com.innerdimension.domain.DomainTextPalette.pair(a.code())!=null)InfinityText.draw(g,field_22793,a.name().getString(),x+pad,y+pad,255,a.code());else text(g,(revealAbilities||a.code().equals("D00"))?a.coloredName():class_2561.method_43470("???"),x+pad,y+pad,0xffffffff);
        int ty=y+pad+12;
        int offset=0;String raw=a.description().getString();String warning=chinese()?"全体活物":"All living beings";int warningStart=raw.indexOf(warning);
        for(String line:text){if(a.code().equals("D71")){int lx=x+pad;int start=raw.indexOf(line,offset);for(int n=0;n<line.length();n++){String c=line.substring(n,n+1);int absolute=start+n;text(g,class_2561.method_43470(c),lx,ty,warningStart>=0&&absolute>=warningStart&&absolute<warningStart+warning.length()?0xffff5555:0xffd6cfe0);lx+=field_22793.method_1727(c);}offset=Math.max(offset,start+line.length());}else text(g,class_2561.method_43470(line),x+pad,ty,0xffd6cfe0);ty+=10;}
        if(!settings.isEmpty()){ty+=5;for(String line:settings){text(g,class_2561.method_43470(line),x+pad,ty,0xffa99aba);ty+=10;}}
    }
    /** Greedy wrap on the real font metrics, breaking at spaces when the line has one. */
    private List<String> lines(String text,int width){List<String> out=new ArrayList<>();StringBuilder line=new StringBuilder();
        for(int i=0;i<text.length();i++){char c=text.charAt(i);
            if(c=='\n'){out.add(line.toString());line.setLength(0);continue;}
            if(line.length()>0&&field_22793.method_1727(line.toString()+c)>width){int brk=line.lastIndexOf(" ");if(brk>0){out.add(line.substring(0,brk));line.delete(0,brk+1);}else{out.add(line.toString());line.setLength(0);}}
            line.append(c);}
        out.add(line.toString());return out;
    }
    public boolean method_25402(class_11909 e,boolean doubled){if(e.method_74228()==1)for(int row=0;row<ROWS;row++)if(lowerAbility[row]!=null&&inside(e.comp_4798(),e.comp_4799(),left+166,top+158+row*17,202,16)){var a=lowerAbility[row];if(com.innerdimension.domain.DomainModes.selectable(a.code()))field_22787.method_1507(new ModeSelectionScreen(this,a.entityId(),mask(a)));else if(objectsEnabled(a))field_22787.method_1507(new ObjectSelectionScreen(this,slot,a.entityId(),mask(a)));return true;}if(e.method_74228()==0)for(int list=0;list<2;list++){int size=list==0?unselected().size():chosen().size(),y=listY(list),h=listHeight(list);if(size>ROWS&&inside(e.comp_4798(),e.comp_4799(),left+370,y,6,h)){int thumb=thumbHeight(list,size),ty=thumbY(list,size);dragList=list;dragOffset=e.comp_4799()>=ty&&e.comp_4799()<ty+thumb?e.comp_4799()-ty:thumb/2.0;scrollTo(e.comp_4799());return true;}}if(inside(e.comp_4798(),e.comp_4799(),left+14,top+28,136,92)){if(com.innerdimension.client.DomainWheelInput.crestLocked()){field_22787.field_1724.method_7353(class_2561.method_43471("inner_dimension.void.crest_locked"),true);return true;}field_22787.method_1507(new CrestEditorScreen(this,slot,slotName(),slotNames));return true;}return super.method_25402(e,doubled);}
    public boolean method_25403(class_11909 e,double dx,double dy){if(dragList>=0){scrollTo(e.comp_4799());return true;}return super.method_25403(e,dx,dy);}
    public boolean method_25406(class_11909 e){if(dragList>=0){dragList=-1;return true;}return super.method_25406(e);}
    private int listY(int list){return top+(list==0?52:158);}private int listHeight(int list){return list==0?100:85;}
    private int thumbHeight(int list,int size){return Math.max(12,listHeight(list)*ROWS/Math.max(ROWS,size));}
    private int thumbY(int list,int size){int range=Math.max(0,size-ROWS);return listY(list)+(range==0?0:(listHeight(list)-thumbHeight(list,size))*(list==0?scrollA:scrollB)/range);}
    private void scrollbar(class_332 g,int list,int size,int mx,int my){int x=left+370,y=listY(list),h=listHeight(list),th=thumbHeight(list,size),ty=thumbY(list,size);g.method_25294(x,y,x+6,y+h,0xff100e13);g.method_25294(x+1,ty,x+5,ty+th,size<=ROWS?0xff28232e:dragList==list||inside(mx,my,x,ty,6,th)?0xff71637d:0xff4e435b);}
    private void scrollTo(double y){int list=dragList,size=list==0?unselected().size():chosen().size(),travel=listHeight(list)-thumbHeight(list,size);int value=travel<=0?0:clamp((int)Math.round((y-listY(list)-dragOffset)*(size-ROWS)/travel),size);if(list==0)scrollA=value;else scrollB=value;refresh();}
    private boolean chinese(){return field_22787.method_1526().method_4669().startsWith("zh");}
    private class_2561 directionLabel(){return class_2561.method_43471("screen.inner_dimension.sort."+(reversed?"forward":"reverse"));}
    private class_2561 sortLabel(){return class_2561.method_43471("screen.inner_dimension.sort."+switch(sortMode){case 1->mentalVolumeUnlocked?"volume":"weight";case 2->chinese()?"pinyin":"initial";case 3->"color";default->"code";});}
    private Comparator<DomainAbilityCatalog.Ability> order(){Comparator<DomainAbilityCatalog.Ability> code=Comparator.comparingInt((DomainAbilityCatalog.Ability a)->a.code().equals("D00")?Integer.MAX_VALUE:a.number()).thenComparing(DomainAbilityCatalog.Ability::entityId);Comparator<DomainAbilityCatalog.Ability> cmp=switch(sortMode){case 1->Comparator.comparingInt(DomainAbilityCatalog.Ability::loadWeight).thenComparing(code);case 2->chinese()?Comparator.comparing((DomainAbilityCatalog.Ability a)->displayName(a).getString(),pinyin).thenComparing(code):Comparator.comparing((DomainAbilityCatalog.Ability a)->englishNames.getOrDefault(entityKey(a.entityId()),a.entityId()),String.CASE_INSENSITIVE_ORDER).thenComparing(code);case 3->Comparator.comparingInt(DomainAbilityCatalog.Ability::colorWeight).thenComparing(code);default->code;};return reversed?cmp.reversed():cmp;}
    private static Map<String,String> loadEnglishNames(){try(var stream=DomainCoreScreen.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")){if(stream==null)return Map.of();var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();Map<String,String> names=new HashMap<>();json.entrySet().forEach(e->names.put(e.getKey(),e.getValue().getAsString()));return names;}catch(java.io.IOException ex){return Map.of();}}
    public boolean method_25401(double x,double y,double hx,double vy){if(inside(x,y,left+166,top+52,210,100)){scrollA=clamp(scrollA+(vy<0?1:-1),unselected().size());return true;}if(inside(x,y,left+166,top+158,210,85)){scrollB=clamp(scrollB+(vy<0?1:-1),chosen().size());return true;}return super.method_25401(x,y,hx,vy);}
    private void engrave(){if(!imprintUnlocked)return;ClientPlayNetworking.send(new UpdateDomainSlotPayload(UpdateDomainSlotPayload.ENGRAVE,slot,new com.google.gson.Gson().toJson(objectMasks),List.copyOf(draft)));}
    private void choose(int row,boolean chosen){List<DomainAbilityCatalog.Ability> list=chosen?chosen():unselected();int i=row+(chosen?scrollB:scrollA);if(i>=list.size())return;var a=list.get(i);if(chosen)draft.remove(a.entityId());else draft.add(a.entityId());refresh();}
    private void refresh(){if(upper[0]==null)return;List<DomainAbilityCatalog.Ability>a=unselected(),b=chosen();scrollA=clamp(scrollA,a.size());scrollB=clamp(scrollB,b.size());for(int row=0;row<ROWS;row++){configure(upper[row],upperAbility,row,a,row+scrollA);configure(lower[row],lowerAbility,row,b,row+scrollB);}}
    private void configure(class_4185 button,DomainAbilityCatalog.Ability[] store,int row,List<DomainAbilityCatalog.Ability> list,int index){boolean show=index<list.size();button.field_22763=show;button.field_22764=show;store[row]=show?list.get(index):null;if(show){var a=list.get(index);button.method_25355(class_2561.method_43469(mentalVolumeUnlocked?"screen.inner_dimension.trait.row.volume":"screen.inner_dimension.trait.row",a.color()<0?displayName(a):displayName(a).method_10862(class_2583.field_24360.method_36139(a.color())),a.loadWeight()==Integer.MAX_VALUE?"∞":a.loadWeight()));if(button instanceof Button custom){custom.animatedCode=com.innerdimension.domain.DomainTextPalette.pair(a.code())!=null?a.code():null;}}}
    private List<DomainAbilityCatalog.Ability>unselected(){return available.stream().filter(a->!draft.contains(a.entityId())).filter(a->query.isBlank()||displayName(a).getString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))||a.entityId().contains(query.toLowerCase(Locale.ROOT))).sorted(order()).toList();}
    private List<DomainAbilityCatalog.Ability>chosen(){return available.stream().filter(a->draft.contains(a.entityId())).sorted(order()).toList();}
    private static int clamp(int v,int size){return Math.max(0,Math.min(v,Math.max(0,size-ROWS)));}
    /** Server refreshes carry saved data; drafts live with this open page until engraving succeeds. */
    void inheritDraft(DomainCoreScreen previous){if(slot!=previous.slot)return;draft.clear();draft.addAll(previous.draft);objectMasks.putAll(previous.objectMasks);query=previous.query;sortMode=previous.sortMode;reversed=previous.reversed;scrollA=previous.scrollA;scrollB=previous.scrollB;}
    private boolean objectsEnabled(DomainAbilityCatalog.Ability a){return com.innerdimension.domain.runtime.DomainObjects.configurable(a.code())&&(!a.code().equals("D71")||available.stream().anyMatch(v->v.code().equals("D79")));}
    private int mask(DomainAbilityCatalog.Ability a){return objectMasks.getOrDefault(a.entityId(),com.innerdimension.domain.runtime.DomainObjects.defaultMask(a.code()));}
    /**
     * Strength of the page-local draft. Every input is already client-visible: capacity and the
     * draft are in this screen, energy lives in the player's DataTracker and status effects sync,
     * so the number updates as the player moves abilities around without a round trip.
     */
    private com.innerdimension.domain.DomainStrength.Breakdown strength(){
        var player=field_22787.field_1724;
        int energy=player==null?-1:com.innerdimension.domain.compat.NoNDomainBridge.currentEnergy(player);
        boolean fertile=player!=null&&com.innerdimension.domain.compat.NoNDomainBridge.fertilityAmplifier(player)>=0;
        return com.innerdimension.domain.DomainStrength.of(draft,capacity,Math.max(0,energy),fertile);
    }
    void saveObjects(String id,int mask){objectMasks.put(id,mask&63);}
    int unlockedSlots(){return slotLoadouts.size();}
    int[] abilityPalette(){Map<Integer,Integer> weights=new HashMap<>();for(String id:engraved){var a=DomainAbilityCatalog.byEntity(id);if(a!=null&&a.color()>=0)weights.merge(a.color(),a.colorWeight(),Math::max);}return weights.entrySet().stream().sorted(Map.Entry.<Integer,Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey())).limit(5).mapToInt(e->0xff000000|e.getKey()).toArray();}
    String slotName(){return slot<slotNames.size()?slotNames.get(slot):"";}
    List<String> slotNames(){return slotNames;}
    private static class_5250 displayName(DomainAbilityCatalog.Ability a){return class_2561.method_43471((a.code().equals("D39")||a.code().equals("D79"))?a.key()+".name":entityKey(a.entityId()));}
    private static String entityKey(String id){int c=id.indexOf(':');return c<0?id:"entity."+id.substring(0,c)+"."+id.substring(c+1).replace('/','.');}
    private void text(class_332 g,class_2561 s,int x,int y,int c){g.method_51439(field_22793,s,x,y,c,false);}private static boolean inside(double x,double y,int rx,int ry,int rw,int rh){return x>=rx&&y>=ry&&x<rx+rw&&y<ry+rh;}
    private static void panel(class_332 g,int x,int y,int w,int h){g.method_25294(x,y,x+w,y+h,0xff120f16);g.method_25294(x+2,y+2,x+w-2,y+h-2,0xff2b2630);}private static void box(class_332 g,int x,int y,int w,int h){g.method_25294(x,y,x+w,y+h,0xff09080a);g.method_25294(x+1,y+1,x+w-1,y+h-1,0xff514857);}private static void outline(class_332 g,int x,int y,int w,int h,int c){g.method_25294(x,y,x+w,y+1,c);g.method_25294(x,y+h-1,x+w,y+h,c);g.method_25294(x,y,x+1,y+h,c);g.method_25294(x+w-1,y,x+w,y+h,c);}
    private static void drawDesign(class_332 g,BufferedImage image,int x,int y,int w,int h){int scale=Math.max(1,Math.min(w/image.getWidth(),h/image.getHeight())),ox=x+(w-image.getWidth()*scale)/2,oy=y+(h-image.getHeight()*scale)/2;for(int py=0;py<image.getHeight();py++)for(int px=0;px<image.getWidth();px++){int c=image.getRGB(px,py);if((c>>>24)!=0)g.method_25294(ox+px*scale,oy+py*scale,ox+(px+1)*scale,oy+(py+1)*scale,c);}}
    private static final class Button extends class_4185{String animatedCode;Button(int x,int y,int w,int h,class_2561 t,class_4241 p){super(x,y,w,h,t,p,field_40754);}@Override protected void method_75752(class_332 g,int mx,int my,float d){int x=method_46426(),y=method_46427(),w=method_25368(),h=method_25364();g.method_25294(x,y,x+w,y+h,field_22764?(method_25405(mx,my)?0xff675971:0xff514857):0xff342f38);var font=class_310.method_1551().field_1772;float scale=w<=24?Math.min(1f,(w-2f)/Math.max(1,font.method_27525(method_25369()))):1f;var matrices=g.method_51448();matrices.pushMatrix();matrices.translate(x+w/2f,y+h/2f);matrices.scale(scale,scale);if(animatedCode!=null)InfinityText.draw(g,font,method_25369().getString(),-font.method_1727(method_25369().getString())/2,-4,255,animatedCode);else g.method_27534(font,method_25369(),0,-4,field_22764?0xffffffff:0xff999999);matrices.popMatrix();}}
}
