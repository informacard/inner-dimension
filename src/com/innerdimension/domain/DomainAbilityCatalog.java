package com.innerdimension.domain;

import java.util.*;
import net.minecraft.class_2561;
import net.minecraft.class_2583;

/** Species and hidden reverse metadata. Load weight and colour weight deliberately remain independent. */
public final class DomainAbilityCatalog {
    public static final int MAX_WEIGHT=6;
    public record Ability(String code,String entityId,int loadWeight,int colorWeight,int color,String family) {
        public int number(){return Integer.parseInt(code.substring(1));}
        public String key(){return "ability.inner_dimension."+code.toLowerCase(Locale.ROOT);}
        public class_2561 name(){return class_2561.method_43471(code.equals("D00")?"ability.inner_dimension.unknown.name":key()+".name");}
        public class_2561 description(){return class_2561.method_43471(code.equals("D00")?"ability.inner_dimension.unknown.description":key()+".description");}
        public class_2561 coloredName(){return color<0?name():name().method_27661().method_10862(class_2583.field_24360.method_36139(color));}
    }
    public static final List<Ability> ALL=List.of(
        new Ability("D01","minecraft:axolotl",1,2,0xF58CB3,"pink"), new Ability("D02","minecraft:pig",1,4,0xF2A6A0,"pink"),
        new Ability("D03","minecraft:cow",2,4,0xF2E3C6,"neutral"), new Ability("D04","minecraft:chicken",2,10,0xFF69B4,"neutral"),
        new Ability("D05","minecraft:sheep",2,4,0xE9D5AD,"yellow"), new Ability("D06","minecraft:goat",2,4,0xD6BD8A,"yellow"),
        new Ability("D07","minecraft:rabbit",1,3,0xF5AD67,"orange"), new Ability("D08","minecraft:cat",3,5,0xA5D67A,"green"),
        new Ability("D09","minecraft:wolf",4,7,0xB0C4D8,"blue"), new Ability("D10","minecraft:fox",3,5,0xED9654,"orange"),
        new Ability("D11","minecraft:ocelot",2,3,0xEBC25C,"yellow"), new Ability("D12","minecraft:panda",2,4,0xA8CF92,"green"),
        new Ability("D13","minecraft:polar_bear",2,4,0xD7F1F2,"cyan"), new Ability("D14","minecraft:armadillo",3,5,0xC69286,"pink"),
        new Ability("D15","minecraft:horse",3,6,0xD6A16B,"orange"), new Ability("D16","minecraft:donkey",3,6,0xB9B1A2,"neutral"),
        new Ability("D17","minecraft:mule",2,3,0xC6A17E,"orange"), new Ability("D18","minecraft:camel",2,3,0xE4BF7C,"yellow"),
        new Ability("D19","minecraft:dolphin",1,3,0x72BCD9,"blue"), new Ability("D20","minecraft:frog",1,4,0xC7DB73,"green"),
        new Ability("D21","minecraft:sniffer",2,5,0x83BA68,"green"), new Ability("D22","minecraft:bat",1,4,0xB1A2C7,"purple"),
        new Ability("D23","minecraft:allay",2,5,0x72E0EF,"cyan"), new Ability("D24","minecraft:villager",4,6,0x59CF8C,"green"),
        new Ability("D25","minecraft:iron_golem",3,5,0xCCD6D9,"neutral"), new Ability("D26","minecraft:snow_golem",1,3,0xBCECF5,"cyan"),
        new Ability("D27","minecraft:copper_golem",1,5,0x63C7AC,"cyan"), new Ability("D28","minecraft:zombie",2,4,0x88B76B,"green"),
        new Ability("D29","minecraft:husk",2,4,0xCABB78,"yellow"), new Ability("D30","minecraft:drowned",2,4,0x4FBAAD,"cyan"),
        new Ability("D31","minecraft:skeleton",4,6,0xE5DDCA,"neutral"), new Ability("D32","minecraft:stray",2,5,0x87BFE8,"blue"),
        new Ability("D33","minecraft:bogged",4,6,0x9ABB58,"green"), new Ability("D34","minecraft:parched",3,5,0xD9AA63,"yellow"),
        new Ability("D35","minecraft:creeper",2,4,0x80DB54,"green"), new Ability("D36","minecraft:spider",2,4,0xE7797D,"red"),
        new Ability("D37","minecraft:cave_spider",3,5,0x59C1B7,"cyan"), new Ability("D38","minecraft:silverfish",1,3,0xAEBABC,"neutral"),
        new Ability("D39","minecraft:slime",1,5,0xA3E777,"green"), new Ability("D40","minecraft:enderman",3,7,0xBA88EF,"purple"),
        new Ability("D41","minecraft:phantom",2,4,0x8B9EEB,"blue"), new Ability("D42","minecraft:witch",3,6,0xCB8BDD,"purple"),
        new Ability("D43","minecraft:pillager",3,4,0x8FAAC7,"blue"), new Ability("D44","minecraft:vindicator",4,7,0xE5A078,"orange"),
        new Ability("D45","minecraft:ravager",4,8,0xBAACA2,"neutral"), new Ability("D46","minecraft:warden",6,8,0x4FD1C5,"cyan"),
        new Ability("D47","minecraft:creaking",4,7,0xF0BD65,"yellow"), new Ability("D48","minecraft:breeze",2,6,0x8AD3F3,"blue"),
        new Ability("D49","minecraft:zombie_horse",3,6,0x9BC178,"green"), new Ability("D50","minecraft:skeleton_horse",2,6,0xC6DFDC,"cyan"),
        new Ability("D51","minecraft:blaze",4,9,0xFF784F,"orange"), new Ability("D52","minecraft:strider",2,5,0xEB737F,"red"),
        new Ability("D53","minecraft:piglin",3,6,0xF3D06B,"yellow"), new Ability("D54","minecraft:piglin_brute",4,5,0xE8AF4E,"orange"),
        new Ability("D55","minecraft:zombified_piglin",3,5,0xB5C183,"green"), new Ability("D56","minecraft:hoglin",2,5,0xD5AD85,"orange"),
        new Ability("D57","minecraft:zoglin",3,7,0xD98C83,"red"), new Ability("D58","minecraft:wither_skeleton",3,6,0xA596B2,"purple"),
        new Ability("D59","minecraft:wither",5,9,0x918BA8,"purple"), new Ability("D60","minecraft:llama",3,4,0xEED6B1,"yellow"),
        new Ability("D61","minecraft:trader_llama",2,5,0x6DACE5,"blue"), new Ability("D62","minecraft:endermite",2,5,0xD18EE8,"purple"),
        new Ability("D63","minecraft:guardian",3,5,0xF1A369,"orange"), new Ability("D64","minecraft:elder_guardian",3,6,0xA3C6BC,"cyan"),
        new Ability("D65","minecraft:ghast",4,6,0xF4B1AE,"red"), new Ability("D66","minecraft:happy_ghast",2,5,0xDCF0FF,"blue"),
        new Ability("D67","minecraft:shulker",3,7,0xBE98D9,"purple"), new Ability("D68","minecraft:ender_dragon",6,9,0xDD77F2,"purple"),
        new Ability("D69","minecraft:bee",3,6,0xFFD568,"yellow"),
        new Ability("D70","inner_dimension:self",12,10,0xFFFFFF,"white"),
        new Ability("D71","inner_dimension:void",Integer.MAX_VALUE,10,0x000000,"void"),
        new Ability("D72",DomainModes.SUCCUBUS,3,10,0x800020,"burgundy"),
        new Ability("D73","minecraft:salmon",2,4,0xE77B70,"red"),
        new Ability("D74","minecraft:squid",1,4,0x6374B5,"blue"),
        new Ability("D75","minecraft:evoker",4,6,0x9CA8B8,"neutral"),
        new Ability("D76",DomainModes.SHRINE,8,10,0xC00020,"red"),
        new Ability("D77",DomainModes.SLIME_LARGE,4,7,0x65B84B,"green"),
        new Ability("D78",DomainModes.SLIME_SMALL,2,4,0xC5F2A0,"green"),
        new Ability("D79",UnlimitedVoidRules.ID,Integer.MAX_VALUE,12,0xA6F3FF,"infinity")
    );
    private static final Map<String,Ability> BY_ENTITY;
    static {Map<String,Ability> m=new LinkedHashMap<>();ALL.forEach(a->m.put(a.entityId,a));BY_ENTITY=Map.copyOf(m);}
    private DomainAbilityCatalog(){}
    public static Ability byEntity(String id){return BY_ENTITY.get(id);}
    /** Presentation template only: unsupported species never acquire an executable D-code. */
    public static Ability forRecordedEntity(String id){
        Ability known=byEntity(id);
        return known!=null?known:new Ability("D00",id,0,0,-1,"none");
    }
    public static List<String> sanitize(Collection<String> ids){
        LinkedHashSet<String> out=new LinkedHashSet<>();for(String id:ids)if(id!=null&&id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))out.add(id);
        if(out.contains("inner_dimension:void")&&out.size()>1)throw new IllegalArgumentException("domain.loadout.conflict.void");
        if(out.contains(UnlimitedVoidRules.ID)&&out.size()>1)throw new IllegalArgumentException("domain.loadout.conflict.unlimited_void");
        return List.copyOf(out);
    }
    /** Entity ids of the purely damaging abilities, derived from their D-codes at class load. */
    private static final Set<String> PURE_DAMAGE_IDS;
    static {
        Set<String> codes=Set.of("D15","D20","D33","D37","D44","D45","D46","D50","D51","D58","D59","D60","D63","D65","D68","D76");
        LinkedHashSet<String> ids=new LinkedHashSet<>();
        for(Ability a:ALL)if(codes.contains(a.code()))ids.add(a.entityId);
        PURE_DAMAGE_IDS=Set.copyOf(ids);
    }
    /**
     * Translation keys describing how a loadout's abilities interact. Purely advisory
     * feedback for the engraving UI: unlike {@link #sanitize} this never rejects a loadout.
     * The first entry is a hard conflict, the remaining ones are installable-but-cancelling
     * or installable-but-pointless tips.
     */
    public static List<String> conflicts(Collection<String> ids){
        List<String> out=new ArrayList<>();
        
        if(ids.contains("minecraft:chicken"))for(String id:ids)if(PURE_DAMAGE_IDS.contains(id)){out.add("domain.loadout.tip.no_damage");break;}
        return List.copyOf(out);
    }
    public static boolean infinite(Collection<String> ids){return ids.contains("inner_dimension:void")||ids.contains(UnlimitedVoidRules.ID);}
    public static int weight(Collection<String> ids){if(infinite(ids))return Integer.MAX_VALUE;return ids.stream().map(BY_ENTITY::get).filter(Objects::nonNull).mapToInt(Ability::loadWeight).sum();}
    public static List<Ability> colorOrder(Collection<String> ids){
        boolean chicken=ids.contains("minecraft:chicken"),skeletonHorse=ids.contains("minecraft:skeleton_horse");
        return ids.stream().map(BY_ENTITY::get).filter(Objects::nonNull)
            .filter(a->!chicken||!Set.of("minecraft:cat","minecraft:ravager","minecraft:warden","minecraft:ender_dragon").contains(a.entityId))
            
            .sorted(Comparator.comparingInt(Ability::colorWeight).reversed().thenComparing(Comparator.comparingInt(Ability::loadWeight).reversed()).thenComparingInt(Ability::number)).toList();
    }
    public static int[] nameColors(Collection<String> ids){if(ids.contains(UnlimitedVoidRules.ID))return new int[]{0x7657FF,0xA6F3FF,0x7657FF};if(DomainModes.hearts(ids)==2)return new int[]{0xFF69B4,0x800020,0xFF69B4};List<Ability> order=colorOrder(ids);if(order.isEmpty())return new int[]{0xffffff,0xffffff,0xffffff};Ability center=order.get(0);List<Ability> secondary=order.stream().skip(1).filter(a->!a.family.equals(center.family)).limit(2).toList();if(secondary.isEmpty()){int[] pair=DomainTextPalette.pair(center.code());if(pair!=null)return new int[]{pair[0],pair[1],pair[0]};return new int[]{center.color,center.color,center.color};}if(secondary.size()==1)return new int[]{secondary.get(0).color,center.color,secondary.get(0).color};return new int[]{secondary.get(0).color,center.color,secondary.get(1).color};}
    /**
     * Multiplier on the main colour's share of the domain name. {@code 1.0} means the three bands
     * are strictly proportional to their colour weights; anything above 1 widens the main colour
     * at the expense of the two flanks.
     */
    private static final double MAIN_COLOR_BONUS = 1.0;
    /**
     * Where the two colour transitions sit in the domain name, in per-mille of the whole string.
     * Each of the three colours owns a share of the length taken from its colour weight: the two
     * flanks split their combined weight evenly, and the main colour counts {@link
     * #MAIN_COLOR_BONUS} times. Colour weights 8 (main) / 3 / 2 therefore give each flank
     * (3+2)/2 = 2.5 and the main 8*1.0 = 8 out of 13, i.e. transitions at 192 and 808 per-mille.
     * Mirrors {@link #nameColors} exactly, including which entries are chosen, so the two can only
     * ever describe the same gradient.
     */
    public static int[] nameSplit(Collection<String> ids){
        if(ids.contains(UnlimitedVoidRules.ID))return new int[]{300,700};
        if(DomainModes.hearts(ids)==2)return new int[]{250,750};
        List<Ability> order=colorOrder(ids);
        if(order.isEmpty())return new int[]{333,667};
        Ability center=order.get(0);
        List<Ability> secondary=order.stream().skip(1).filter(a->!a.family.equals(center.family)).limit(2).toList();
        if(secondary.isEmpty())return new int[]{0,1000};
        double main=Math.max(0,center.colorWeight())*MAIN_COLOR_BONUS;
        double share=secondary.stream().mapToInt(Ability::colorWeight).sum()/(double)secondary.size();
        double total=share*2+main;
        if(total<=0)return new int[]{333,667};
        int left=(int)Math.round(share/total*1000),right=(int)Math.round((share+main)/total*1000);
        if(right<=left)return new int[]{333,667};
        return new int[]{left,right};
    }
}
