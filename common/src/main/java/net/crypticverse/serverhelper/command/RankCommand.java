package net.crypticverse.serverhelper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.crypticverse.serverhelper.config.ranks.RanksConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class RankCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("rank").requires(source -> source.hasPermission(1))
                .then(literal("create").then(argument("id", StringArgumentType.word()).then(argument("name", StringArgumentType.greedyString()).executes(context -> {
                    String id = StringArgumentType.getString(context, "id");
                    String name = StringArgumentType.getString(context, "name");

                    if (RanksConfig.isRank(id)) {
                        context.getSource().sendFailure(Component.nullToEmpty("That rank already exists!"));
                        return 0;
                    }

                    RanksConfig.createRole(id, name);
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Created rank: " + StringArgumentType.getString(context, "name")), false);
                    RanksConfig.save();
                    return SINGLE_SUCCESS;
                }))))
                .then(literal("remove").then(argument("id", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(RanksConfig.ranks.keySet().stream()
                        .filter(id -> id.startsWith(builder.getRemaining()))
                        .toList(), builder))
                .executes(context -> {
                    String id = StringArgumentType.getString(context, "id");

                    if (!RanksConfig.isRank(id)) {
                        context.getSource().sendFailure(Component.nullToEmpty("That rank does not exist!"));
                        return 0;
                    }

                    RanksConfig.removeRole(id);
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Removed rank: " + StringArgumentType.getString(context, "id")), false);
                    RanksConfig.save();
                    return SINGLE_SUCCESS;
                })))
                .then(literal("assign").then(argument("player", EntityArgument.players()).then(argument("rank", StringArgumentType.word()).executes(context -> {
                    UUID playerId = EntityArgument.getPlayer(context, "player").getUUID();
                    if (RanksConfig.isPlayerInRole(playerId, StringArgumentType.getString(context, "rank"))) {
                        context.getSource().sendFailure(Component.nullToEmpty("That player is already in a rank!"));
                        return 0;
                    }

                    RanksConfig.addPlayerToRole(StringArgumentType.getString(context, "rank"), playerId);
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Added player to rank: " + StringArgumentType.getString(context, "rank")), false);
                    RanksConfig.save();
                    return SINGLE_SUCCESS;
                }))))
                .then(literal("unassign").then(argument("player", EntityArgument.players()).then(argument("rank", StringArgumentType.word()).executes(context -> {
                    UUID playerId = EntityArgument.getPlayer(context, "player").getUUID();
                    if (!RanksConfig.isPlayerInRole(playerId, StringArgumentType.getString(context, "rank"))) {
                        context.getSource().sendFailure(Component.nullToEmpty("That player is not in a rank!"));
                        return 0;
                    }

                    RanksConfig.removePlayerFromRole(StringArgumentType.getString(context, "rank"), playerId);
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Removed player from rank: " + StringArgumentType.getString(context, "player")), false);
                    RanksConfig.save();
                    return SINGLE_SUCCESS;
                }))))
                .then(literal("list").executes(context -> {
                    context.getSource().sendSystemMessage(Component.literal("All ranks (by ID):"));
                    for (String role : RanksConfig.ranks.keySet()) {
                        context.getSource().sendSystemMessage(Component.literal(role));
                    }
                    return SINGLE_SUCCESS;
                })));
    }
}
