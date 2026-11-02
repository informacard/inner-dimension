package com.innerdimension.progression.traits;
import com.innerdimension.progression.*;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import java.util.*;
public final class DomainSpeciesService {
    public record Outcome(SpeciesRecordResult result, Optional<class_2960> sourceEntityId) { }
    private DomainSpeciesService() { }
    public static Outcome previewRecord(class_3222 player,class_1799 stack) {
        if(!DomainAccess.isUnlocked(player)) return new Outcome(SpeciesRecordResult.NOT_UNLOCKED,Optional.empty());
        var resolved=NoNBottleSourceResolver.resolve(stack);
        if(resolved.result()!=SpeciesRecordResult.VALID) return new Outcome(resolved.result(),resolved.sourceEntityId());
        class_2960 id=resolved.sourceEntityId().orElseThrow();
        if(DomainState.get(player.method_51469()).hasSpecies(player.method_5667(),id.toString()))
            return new Outcome(SpeciesRecordResult.ALREADY_RECORDED,Optional.of(id));
        return new Outcome(SpeciesRecordResult.VALID,Optional.of(id));
    }
    public static Outcome recordFromBottle(class_3222 player,class_1799 stack) {
        Outcome preview=previewRecord(player,stack);
        if(preview.result()!=SpeciesRecordResult.VALID) return preview;
        class_2960 id=preview.sourceEntityId().orElseThrow();
        boolean added=DomainState.get(player.method_51469()).addSpecies(player.method_5667(),id.toString());
        return new Outcome(added?SpeciesRecordResult.RECORDED:SpeciesRecordResult.ALREADY_RECORDED,Optional.of(id));
    }
    public static List<class_2960> getCollectedSpecies(MinecraftServer server,UUID owner) {
        return DomainState.get(server.method_30002()).species(owner).stream().map(class_2960::method_60654).filter(Objects::nonNull).toList();
    }
    public static boolean hasCollected(MinecraftServer server,UUID owner,class_2960 id) {
        return DomainState.get(server.method_30002()).hasSpecies(owner,id.toString());
    }
}
