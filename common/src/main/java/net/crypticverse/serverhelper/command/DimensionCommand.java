package net.crypticverse.serverhelper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.crypticverse.serverhelper.config.dimension.DimensionConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class DimensionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("dimensions").requires(source -> source.hasPermission(1))
                .then(literal("enable").then(argument("dimension", DimensionArgument.dimension()).executes(context -> {
                    ServerLevel dimension = DimensionArgument.getDimension(context, "dimension");
                    if (dimension.dimension() == Level.NETHER) {
                        DimensionConfig.netherDisabled = false;
                        DimensionConfig.saveConfig();
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("Enabled dimension nether!"), false);
                        return SINGLE_SUCCESS;
                    } else if (dimension.dimension() == Level.END) {
                        DimensionConfig.theEndDisabled = false;
                        DimensionConfig.saveConfig();
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("Enabled dimension the_end!"), false);
                        return SINGLE_SUCCESS;
                    } else {
                        context.getSource().sendFailure(Component.nullToEmpty("Unknown dimension: " + dimension));
                        return 0;
                    }
                })))
                .then(literal("disable").then(argument("dimension", StringArgumentType.word()).suggests((context, builder) ->
                        SharedSuggestionProvider.suggest(new String[] {"nether", "the_end"}, builder)).executes(context -> {
                    ServerLevel dimension = DimensionArgument.getDimension(context, "dimension");
                    if (dimension.dimension() == Level.NETHER) {
                        DimensionConfig.netherDisabled = true;
                        DimensionConfig.saveConfig();
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("Disabled dimension nether!"), false);
                        return SINGLE_SUCCESS;
                    } else if (dimension.dimension() == Level.END) {
                        DimensionConfig.theEndDisabled = true;
                        DimensionConfig.saveConfig();
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("Disabled dimension the_end!"), false);
                        return SINGLE_SUCCESS;
                    } else {
                        context.getSource().sendFailure(Component.nullToEmpty("Unknown dimension: " + dimension));
                        return 0;
                    }
                }))));
    }
}
