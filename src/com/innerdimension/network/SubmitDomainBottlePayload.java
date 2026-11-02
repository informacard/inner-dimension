package com.innerdimension.network;
import com.innerdimension.progression.traits.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.*;
public record SubmitDomainBottlePayload() implements class_8710 {
    public static final SubmitDomainBottlePayload INSTANCE=new SubmitDomainBottlePayload();
    public static final class_8710.class_9154<SubmitDomainBottlePayload> ID=new class_8710.class_9154<>(class_2960.method_60655("inner_dimension","submit_domain_bottle"));
    public static final class_9139<class_9129,SubmitDomainBottlePayload> CODEC=class_9139.method_56431(INSTANCE);
    public class_8710.class_9154<SubmitDomainBottlePayload> method_56479(){return ID;}
    public static void receive(class_3222 player){
        if(!com.innerdimension.progression.DomainAccess.isUnlocked(player))return;
        class_1799 stack=player.method_6079();
        DomainSpeciesService.Outcome out=inject(player,stack);
        var response=com.innerdimension.item.OpenDomainCorePayload.forPlayer(player);
        if(out.result()!=SpeciesRecordResult.RECORDED&&out.result()!=SpeciesRecordResult.ALREADY_RECORDED)response=response.withPreviewState(-1);
        ServerPlayNetworking.send(player,response);
    }
    /** Shared authoritative transaction for the GUI and main-hand bottle shortcut. */
    public static DomainSpeciesService.Outcome inject(class_3222 player,class_1799 stack){
        if(!com.innerdimension.progression.DomainAccess.isUnlocked(player))return new DomainSpeciesService.Outcome(SpeciesRecordResult.NOT_UNLOCKED,java.util.Optional.empty());
        if(com.innerdimension.item.CreativeSemenBottle.isBottle(stack)){
            var state=com.innerdimension.progression.DomainState.get(player.method_51469());
            boolean added=false;
            for(var id:com.innerdimension.item.CreativeSemenBottle.sources())added|=state.addSpecies(player.method_5667(),id.toString());
            if(added){stack.method_7934(1);com.innerdimension.progression.InnerAdvancements.grant(player,"injection");com.innerdimension.sound.ModSounds.personal(player,"injection",.8f);}
            return new DomainSpeciesService.Outcome(added?SpeciesRecordResult.RECORDED:SpeciesRecordResult.ALREADY_RECORDED,java.util.Optional.empty());
        }
        DomainSpeciesService.Outcome out=DomainSpeciesService.previewRecord(player,stack);
        if(out.result()==SpeciesRecordResult.VALID){out=DomainSpeciesService.recordFromBottle(player,stack);if(out.result()==SpeciesRecordResult.RECORDED){stack.method_7934(1);com.innerdimension.progression.InnerAdvancements.grant(player,"injection");com.innerdimension.sound.ModSounds.personal(player,"injection",.8f);}}
        return out;
    }
}
