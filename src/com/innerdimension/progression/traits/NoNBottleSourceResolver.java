package com.innerdimension.progression.traits;
import com.nonid.potion.NonPotions;
import net.minecraft.*;
import java.util.Optional;
public final class NoNBottleSourceResolver {
    public record Resolution(SpeciesRecordResult result, Optional<class_2960> sourceEntityId) { }
    private NoNBottleSourceResolver() { }
    public static Resolution resolve(class_1799 stack) {
        if(stack==null || stack.method_7960() || !(NonPotions.isLiquidBottle(stack)||stack.method_31574(class_1802.field_20417))) return result(SpeciesRecordResult.INVALID_BOTTLE,null);
        final class_2960 id;
        try {
            var contents=com.innerdimension.centrifuge.CentrifugeBottleData.read(stack);
            if(contents.provenanceKnown()){
                if(contents.sources().size()!=1)return result(SpeciesRecordResult.UNKNOWN_SOURCE,null);
                id=contents.sources().keySet().iterator().next();
            }else id=NonPotions.getLiquidBottleEntityTypeId(stack);
        }
        catch(IllegalArgumentException | IllegalStateException e) { return result(SpeciesRecordResult.INVALID_SOURCE,null); }
        if(id==null) return result(SpeciesRecordResult.UNKNOWN_SOURCE,null);
        class_2960 parsed=class_2960.method_60654(id.toString());
        if(parsed==null) return result(SpeciesRecordResult.INVALID_SOURCE,null);
        if(parsed.equals(class_2960.method_60655("minecraft","player"))) return result(SpeciesRecordResult.UNSUPPORTED_SOURCE,parsed);
        if(!class_7923.field_41177.method_10250(parsed)) return result(SpeciesRecordResult.UNAVAILABLE_SOURCE,parsed);
        return result(SpeciesRecordResult.VALID,parsed);
    }
    private static Resolution result(SpeciesRecordResult result,class_2960 id) { return new Resolution(result,Optional.ofNullable(id)); }
}
