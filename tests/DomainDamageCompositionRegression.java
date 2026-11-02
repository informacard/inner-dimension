import com.innerdimension.domain.combat.*;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.*;

/** Composition, real vanilla mitigation math, damage tags and native shield cooldowns. */
public final class DomainDamageCompositionRegression {
    static final Path RES = Path.of("dev/inner_dimension/resources");
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void near(float actual, float expected, String message) { check(Math.abs(actual-expected)<.0001, message+": "+actual); }
    static JsonObject json(Path path) throws Exception { return JsonParser.parseString(Files.readString(path)).getAsJsonObject(); }
    static boolean tagged(String tag, String type) throws Exception {
        Path local = RES.resolve("data/minecraft/tags/damage_type/"+tag+".json");
        List<JsonElement> values = new ArrayList<>();
        if (Files.exists(local)) json(local).getAsJsonArray("values").forEach(values::add);
        try (var stream = class_1282.class.getResourceAsStream("/data/minecraft/tags/damage_type/"+tag+".json")) {
            if (stream != null) JsonParser.parseString(new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("values").forEach(values::add);
        }
        for (var value : values) {
            String entry = value.getAsString();
            if (entry.equals(type) || (entry.startsWith("#minecraft:") && tagged(entry.substring(11),type))) return true;
        }
        return false;
    }
    public static void main(String[] args) throws Exception {
        class_155.method_36208(); class_2966.method_12851();
        class_1282 plain = new class_1282(class_6880.method_40223(new class_8110("test",0)));
        for (String type : List.of(DomainDamageTypes.GROUND_SHOCK,DomainDamageTypes.SPIT,DomainDamageTypes.REFLECTED)) {
            var profile = DomainDamageProfile.of(type);
            float total = type.equals(DomainDamageTypes.REFLECTED) ? 6 : 2;
            near(profile.physicalAmount(total)+profile.magicAmount(total),total,"Raw total is conserved");
            near(profile.physicalAmount(total),type.equals(DomainDamageTypes.GROUND_SHOCK)?1.5f:total/2,"Requested ratio");
            List<String> calls = new ArrayList<>();
            check(profile.apply(total,(id,amount)->{calls.add(id);return id.equals(type);}),"Physical hit succeeds");
            check(calls.equals(List.of(type,profile.magicType())),"Magic must run even after a successful physical hit");
            check(profile.apply(total,(id,amount)->id.equals(profile.magicType())),"Blocking physical cannot suppress magic");
            check(!profile.apply(total,(id,amount)->false),"Both rejected components report failure");
            check(!tagged("bypasses_armor",type)&&!tagged("bypasses_shield",type),"Physical retains armor and shield mitigation");
            check(tagged("bypasses_armor",profile.magicType())&&tagged("bypasses_shield",profile.magicType()),"Magic bypasses armor and shields");
            for (String tag:List.of("bypasses_resistance","bypasses_enchantments","bypasses_invulnerability","bypasses_effects"))
                check(!tagged(tag,profile.magicType()),"Magic must retain vanilla protections: "+tag);
            check(tagged("no_knockback",profile.magicType()),"Magic does not duplicate knockback");
            json(RES.resolve("data/inner_dimension/damage_type/"+profile.magicType().split(":")[1]+".json"));
            float[] reduced = {0};
            profile.apply(total,(id,amount)->{
                reduced[0]+=taggedUnchecked("bypasses_armor",id)?amount:class_1280.method_5496(null,amount,plain,20,8);
                return true;
            });
            check(reduced[0]<total && reduced[0]>profile.magicAmount(total),"Armor reduces only the physical share");
            check(class_1280.method_5497(reduced[0],4)<reduced[0],"Vanilla Protection still reduces the result");
        }
        check(tagged("is_projectile",DomainDamageTypes.SPIT_MAGIC),"Magical spit remains a projectile");
        check(tagged("bypasses_armor",DomainDamageTypes.DRAGON_BREATH)&&tagged("bypasses_shield",DomainDamageTypes.DRAGON_BREATH),"Dragon breath follows vanilla magic mitigation");
        for(String type:List.of(DomainDamageTypes.HOOF,DomainDamageTypes.FLYING_SLASH,DomainDamageTypes.SHRINE_SLASH,"minecraft:player_explosion")) {
            check(!DomainDamageProfile.of(type).mixed(),"Pure physical has one request");
            check(!tagged("bypasses_armor",type),"Pure physical respects armor");
        }
        check(tagged("bypasses_armor","minecraft:sonic_boom")&&tagged("bypasses_enchantments","minecraft:sonic_boom"),"Sonic boom retains its original exception");
        check(!tagged("bypasses_resistance","minecraft:sonic_boom"),"Sonic boom still respects Resistance");
        for(String type:List.of(DomainDamageTypes.FLYING_SLASH,DomainDamageTypes.SHRINE_SLASH)) {
            check(DomainShieldBreak.applies(type,0,true),"A fully shield-blocked slash breaks shields");
            check(DomainShieldBreak.applies(type,1,false),"A body hit breaks shields");
            check(!DomainShieldBreak.applies(type,0,false),"Cancelled or immune hits cannot break shields");
        }
        check(!DomainShieldBreak.applies(DomainDamageTypes.SPIT,2,false),"Spit does not break shields");
        class_1799 shield = new class_1799(class_7923.field_41178.method_10223(class_2960.method_12829("minecraft:shield")).orElseThrow());
        class_1796 cooldown = new class_1796();
        check(cooldown.method_62836(shield).equals(class_2960.method_12829("minecraft:shield")),"Vanilla shield cooldown group is correct");
        cooldown.method_7906(class_2960.method_12829("minecraft:shield"),DomainShieldBreak.DURATION);
        check(DomainShieldBreak.DURATION==30,"Both slashes use the requested 1.5 seconds");
        for(int i=0;i<DomainShieldBreak.DURATION-1;i++)cooldown.method_7903();
        check(cooldown.method_7904(shield),"Shield remains disabled before 1.5 seconds");
        cooldown.method_62835(shield,DomainShieldBreak.DURATION);
        for(int i=0;i<DomainShieldBreak.DURATION-1;i++)cooldown.method_7903();
        check(cooldown.method_7904(shield),"Another hit refreshes the duration");
        cooldown.method_7903();
        check(!cooldown.method_7904(shield),"Shield recovers at the requested duration");
        for(String lang:List.of("zh_cn","en_us")) {
            var translations=json(RES.resolve("assets/inner_dimension/lang/"+lang+".json"));
            for(String code:List.of("10","15","44","45","46","60","63","65","68","76")) {
                String desc=translations.get("ability.inner_dimension.d"+code+".description").getAsString();
                check(desc.contains(lang.equals("zh_cn")?"物理":"physical")||desc.contains(lang.equals("zh_cn")?"法术":"magic"),"Damage descriptions disclose type: "+code);
            }
        }
        System.out.println("PASS: damage composition, independent components, vanilla armor/Protection, tags, sonic exception, shield break triggers and native cooldown refresh, localized descriptions");
    }
    static boolean taggedUnchecked(String tag,String type) {
        try { return tagged(tag,type); } catch(Exception e) { throw new RuntimeException(e); }
    }
}
