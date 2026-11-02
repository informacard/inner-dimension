package com.innerdimension.command;

import com.innerdimension.dimension.DynamicDimensionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2186;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

/**
 * Debug/test commands.
 *
 * <pre>
 *   /innerdim enter             enter your own dimension (creating it if needed)
 *   /innerdim enter &lt;player&gt;   enter the dimension owned by another player (op level 2)
 *   /innerdim leave             go back to where you entered from
 * </pre>
 */
public final class InnerDimensionCommand {
    private InnerDimensionCommand() {
    }

    public static void register(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(class_2170.method_9247("innerdim")
                .then(class_2170.method_9247("enter")
                        .executes(ctx -> enterOwn(ctx))
                        .then(class_2170.method_9244("player", class_2186.method_9305())
                                .requires(class_2170.method_71774(class_2170.field_31839))
                                .executes(ctx -> enterOther(ctx))))
                .then(class_2170.method_9247("leave")
                        .executes(ctx -> leave(ctx)))
                .then(class_2170.method_9247("status")
                        .executes(ctx -> status(ctx)))
                .then(class_2170.method_9247("strength")
                        .executes(ctx -> strength(ctx)))
                .then(class_2170.method_9247("identity")
                        .executes(ctx -> identity(ctx)))
                .then(class_2170.method_9247("reset")
                        .executes(ctx -> reset(ctx)))
                .then(class_2170.method_9247("debug")
                        .requires(class_2170.method_71774(class_2170.field_31839))
                        .then(class_2170.method_9247("pregnancy").executes(InnerDimensionCommand::debugPregnancy))
                        .then(com.innerdimension.domain.DebugDomain.command())
                        .then(visualCommand())
                        .then(class_2170.method_9247("species")
                                .then(class_2170.method_9247("record_held").executes(InnerDimensionCommand::recordHeldSpecies))
                                .then(class_2170.method_9247("list").executes(InnerDimensionCommand::listSpecies)))));
        dispatcher.register(class_2170.method_9247("innerdimension")
                .then(class_2170.method_9247("debug")
                        .requires(class_2170.method_71774(class_2170.field_31839))
                        .then(class_2170.method_9247("pregnancy").executes(InnerDimensionCommand::debugPregnancy))
                        .then(com.innerdimension.domain.DebugDomain.command())
                        .then(visualCommand())
                        .then(class_2170.method_9247("species")
                                .then(class_2170.method_9247("record_held").executes(InnerDimensionCommand::recordHeldSpecies))
                                .then(class_2170.method_9247("list").executes(InnerDimensionCommand::listSpecies)))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<class_2168> visualCommand(){
        var command=class_2170.method_9247("visual");
        for(int i=3;i<=3;i++){final int variant=i;command.then(class_2170.method_9247(Integer.toString(i)).executes(ctx->{
            var player=ctx.getSource().method_9207();
            com.innerdimension.voiddomain.VoidRitual.selectVisual(player,variant);
            if(net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.canSend(player,com.innerdimension.network.EnlightenmentVisualPayload.ID))
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player,new com.innerdimension.network.EnlightenmentVisualPayload(variant,true));
            ctx.getSource().method_9226(()->class_2561.method_43469("inner_dimension.visual.selected",variant),false);return 1;
        }));}
        return command;
    }

    /**
     * Reports the binding, waiting and lock counters of the room the player stands in.
     *
     * <p>Purely diagnostic: it reads the same records the transfer gates use, so an in-game check can
     * quote the exact state that decided an outcome.</p>
     */
    private static int debugPregnancy(CommandContext<class_2168> ctx)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        boolean ok=com.innerdimension.pregnancy.MentalPregnancySystem.debugCurrentEngraving(ctx.getSource().method_9207());
        feedback(ctx,ok?"inner_dimension.command.pregnancy_started":"inner_dimension.command.pregnancy_failed");return ok?1:0;
    }

    private static int status(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player = ctx.getSource().method_9207();
        net.minecraft.class_3218 world = player.method_51469();
        if (!DynamicDimensionManager.isInnerWorld(world.method_27983())) {
            feedback(ctx, "inner_dimension.command.not_inside");
            return 0;
        }
        ctx.getSource().method_9226(() -> com.innerdimension.exit.ExitRouter.describe(world), false);
        return 1;
    }

    /** Shows the identities the server really compares, independently of launcher/skin names. */
    private static int identity(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player = ctx.getSource().method_9207();
        feedback(ctx, "inner_dimension.command.player", player.method_7334().name(), player.method_5667().toString());
        for (net.minecraft.class_1268 hand : net.minecraft.class_1268.values()) {
            net.minecraft.class_1799 stack = player.method_5998(hand);
            if (stack.method_7909() != nonplayerplushie.SensoryLinkItems.SENSORY_LINK_DOLL) continue;
            net.minecraft.class_2487 data = nonplayerplushie.SensoryLinkData.read(stack);
            feedback(ctx, "inner_dimension.command.doll",
                    class_2561.method_43471("inner_dimension.command." + (hand == net.minecraft.class_1268.field_5808 ? "main_hand" : "off_hand")),
                    nonplayerplushie.SensoryLinkData.ownerName(data),
                    String.valueOf(nonplayerplushie.SensoryLinkData.ownerUuid(data)),
                    class_2561.method_43471("inner_dimension.command." + nonplayerplushie.SensoryLinkData.valid(data)));
        }
        return 1;
    }

    private static int enterOwn(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player = ctx.getSource().method_9207();
        boolean ok = DynamicDimensionManager.enterAs(player, player.method_5667());
        feedback(ctx, ok ? "inner_dimension.command.entered" : "inner_dimension.command.enter_failed");
        return ok ? 1 : 0;
    }

    private static int enterOther(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 executor = ctx.getSource().method_9207();
        class_3222 target = class_2186.method_9315(ctx, "player");
        boolean ok = DynamicDimensionManager.enterAs(executor, target.method_5667());
        feedback(ctx, ok
                ? "inner_dimension.command.entered_other"
                : "inner_dimension.command.other_failed", target.method_5477());
        return ok ? 1 : 0;
    }

    private static int leave(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player = ctx.getSource().method_9207();
        boolean ok = DynamicDimensionManager.leave(player);
        feedback(ctx, ok ? "inner_dimension.command.left" : "inner_dimension.command.not_inside");
        return ok ? 1 : 0;
    }

    /** Throws away the generated room of the executing player and rebuilds it. */
    private static int reset(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player = ctx.getSource().method_9207();
        if (DynamicDimensionManager.isInnerWorld(player.method_51469().method_27983())) {
            DynamicDimensionManager.leave(player);
        }
        DynamicDimensionManager.resetOwnRoom(player);
        boolean ok = DynamicDimensionManager.enter(player, player);
        feedback(ctx, ok ? "inner_dimension.command.reset" : "inner_dimension.command.reset_failed");
        return ok ? 1 : 0;
    }

    /**
     * The authoritative domain strength of the player's currently selected engraving slot, with
     * the four contributions spelled out. The domain core screen shows the same number for its
     * unsaved draft; this one is the value the server would actually use.
     */
    private static int strength(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player = ctx.getSource().method_9207();
        if(!com.innerdimension.progression.InnerAdvancements.done(player,"domain_contest")){feedback(ctx,"inner_dimension.command.strength_locked");return 0;}
        java.util.UUID owner = player.method_5667();
        com.innerdimension.progression.DomainState state =
                com.innerdimension.progression.DomainState.get(player.method_51469());
        int capacity = state.capacity(owner);
        java.util.List<String> loadout = state.loadout(owner, state.selectedSlot(owner));
        int energy = Math.max(0, com.innerdimension.domain.compat.NoNDomainBridge.currentEnergy(player));
        boolean fertile = com.innerdimension.domain.compat.NoNDomainBridge.fertilityAmplifier(player) >= 0;
        com.innerdimension.domain.DomainStrength.Breakdown score =
                com.innerdimension.domain.DomainStrength.of(loadout, capacity, energy, fertile);
        feedback(ctx, "inner_dimension.command.strength", score.format(score.total()),
                score.format(score.ability()), score.format(score.desire()),
                score.format(score.volume()), score.format(score.fertility()));
        feedback(ctx, "inner_dimension.command.strength_detail", score.count(),
                score.format(score.abilityScore()), energy, capacity,
                // Plain text, not a Component: this arg is mixed with numbers in one format string.
                class_2561.method_43471(fertile
                        ? "inner_dimension.command.strength_fertile"
                        : "inner_dimension.command.strength_barren").getString());
        return 1;
    }

    private static void feedback(CommandContext<class_2168> ctx, String key, Object... args) {
        class_2561 text = class_2561.method_43469(key, args);
        ctx.getSource().method_9226(() -> text, false);
    }

    private static int recordHeldSpecies(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player=ctx.getSource().method_9207();
        var result=com.innerdimension.progression.traits.DomainSpeciesService.recordFromBottle(player,player.method_5998(net.minecraft.class_1268.field_5808));
        String key="inner_dimension.species."+result.result().name().toLowerCase(java.util.Locale.ROOT);
        Object[] args=result.sourceEntityId().map(id -> new Object[]{speciesName(id)}).orElseGet(() -> new Object[0]);
        feedback(ctx,key,args);
        return result.result()==com.innerdimension.progression.traits.SpeciesRecordResult.RECORDED?1:0;
    }

    private static int listSpecies(CommandContext<class_2168> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        class_3222 player=ctx.getSource().method_9207();
        var ids=com.innerdimension.progression.traits.DomainSpeciesService.getCollectedSpecies(player.method_51469().method_8503(),player.method_5667());
        if(ids.isEmpty()) { feedback(ctx,"inner_dimension.species.list_empty"); return 0; }
        feedback(ctx,"inner_dimension.species.list_header",ids.size());
        for(var id:ids) feedback(ctx,"inner_dimension.species.list_entry",speciesName(id));
        return ids.size();
    }

    private static class_2561 speciesName(net.minecraft.class_2960 id) {
        var type=net.minecraft.class_7923.field_41177.method_17966(id);
        return type.<class_2561>map(net.minecraft.class_1299::method_5897).orElseGet(() -> class_2561.method_43469("inner_dimension.species.unavailable_source",id.toString()));
    }
}
