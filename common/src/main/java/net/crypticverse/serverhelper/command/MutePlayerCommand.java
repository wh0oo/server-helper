package net.crypticverse.serverhelper.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.crypticverse.serverhelper.config.filter.FilterConfig;
import net.crypticverse.serverhelper.config.filter.MutedPlayer;
import net.crypticverse.serverhelper.config.filter.PlayerUtils;
import net.crypticverse.serverhelper.config.filter.TempMutedPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MutePlayerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!FilterConfig.muteCommand) return;
        dispatcher.register(literal("mute").requires(source -> source.hasPermission(1))
                .then(literal("add")
                        .then(literal("permanent")
                                .then(argument("target", GameProfileArgument.gameProfile()).executes(context -> {
                                            int players = GameProfileArgument.getGameProfiles(context, "target").size();
                                            for (GameProfile profile : GameProfileArgument.getGameProfiles(context, "target")) {
                                                if (FilterConfig.isMuted(profile.getId())) {
                                                    context.getSource().sendFailure(Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty("is already muted")));
                                                    if (players == 1) return 0;
                                                    else continue;
                                                }

                                                FilterConfig.addMutedPlayer(profile.getId(), "No reason provided");
                                                context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" has been permanently muted")), true);
                                            }
                                            FilterConfig.saveConfig();
                                            return SINGLE_SUCCESS;
                                        })
                                        .then(argument("reason", StringArgumentType.greedyString()).executes(context -> {
                                            int players = GameProfileArgument.getGameProfiles(context, "target").size();
                                            for (GameProfile profile : GameProfileArgument.getGameProfiles(context, "target")) {
                                                if (FilterConfig.isMuted(profile.getId())) {
                                                    context.getSource().sendFailure(Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty("is already muted")));
                                                    if (players == 1) return 0;
                                                    else continue;
                                                }

                                                FilterConfig.addMutedPlayer(profile.getId(), context.getArgument("reason", String.class));
                                                context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" has been permanently muted")), true);
                                            }
                                            FilterConfig.saveConfig();
                                            return SINGLE_SUCCESS;
                                        }))))
                        .then(literal("temporary")
                                .then(argument("target", GameProfileArgument.gameProfile())
                                        .then(argument("minutes", IntegerArgumentType.integer(0, 525960)).executes(context -> {
                                                    int players = GameProfileArgument.getGameProfiles(context, "target").size();
                                                    for (GameProfile profile : GameProfileArgument.getGameProfiles(context, "target")) {
                                                        if (FilterConfig.isMuted(profile.getId())) {
                                                            context.getSource().sendFailure(Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" is already muted")));
                                                            if (players == 1) return 0;
                                                            else continue;
                                                        }
                                                        long until = System.currentTimeMillis() + (IntegerArgumentType.getInteger(context, "minutes") * 60000L);
                                                        FilterConfig.addTempMutedPlayer(profile.getId(), until, "No reason provided");
                                                        context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" has been temporarily muted")), true);
                                                    }
                                                    FilterConfig.saveConfig();
                                                    return SINGLE_SUCCESS;
                                                })
                                                .then(argument("reason", StringArgumentType.greedyString()).executes(context -> {
                                                    int players = GameProfileArgument.getGameProfiles(context, "target").size();
                                                    for (GameProfile profile : GameProfileArgument.getGameProfiles(context, "target")) {
                                                        if (FilterConfig.isMuted(profile.getId())) {
                                                            context.getSource().sendFailure(Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty("is already muted")));
                                                            if (players == 1) return 0;
                                                            else continue;
                                                        }
                                                        long until = System.currentTimeMillis() + (IntegerArgumentType.getInteger(context, "minutes") * 60000L);
                                                        FilterConfig.addTempMutedPlayer(profile.getId(), until, context.getArgument("reason", String.class));
                                                        context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" has been temporarily muted")), true);
                                                    }
                                                    FilterConfig.saveConfig();
                                                    return SINGLE_SUCCESS;
                                                })))
                                )
                        )

                )
                .then(literal("remove").then(argument("player", GameProfileArgument.gameProfile()).suggests((context, builder) -> {
                    List<TempMutedPlayer> tempMutedPlayers = FilterConfig.getTempMutedPlayers();
                    List<MutedPlayer> mutedPlayers = FilterConfig.getMutedPlayers();
                    List<UUID> playersOnMuteList = new ArrayList<>();
                    tempMutedPlayers.forEach(player -> playersOnMuteList.add(player.uuid()));
                    mutedPlayers.forEach(player -> playersOnMuteList.add(player.uuid()));
                    return SharedSuggestionProvider.suggest(PlayerUtils.getPlayerNames(playersOnMuteList), builder);
                }).executes(context -> {
                    int players = GameProfileArgument.getGameProfiles(context, "player").size();
                    for (GameProfile profile : GameProfileArgument.getGameProfiles(context, "player")) {
                        if (!FilterConfig.isMuted(profile.getId())) {
                            context.getSource().sendFailure(Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" is not muted")));
                            if (players == 1) return 0;
                            else continue;

                        }

                        FilterConfig.removeMutedPlayer(profile.getId());
                        context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" has been unmuted")), true);

                    }
                    FilterConfig.saveConfig();
                    return SINGLE_SUCCESS;
                })))
                .then(literal("list").executes(context -> {
                    List<MutedPlayer> mutedPlayers = FilterConfig.getMutedPlayers();
                    List<TempMutedPlayer> tempMutedPlayers = FilterConfig.getTempMutedPlayers();
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Muted players: "), false);
                    mutedPlayers.forEach(player -> context.getSource().sendSuccess(() -> Component.nullToEmpty(PlayerUtils.getPlayerName(player.uuid().toString()) + " - " + player.reason()), false));
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Temporary muted players: "), false);
                    tempMutedPlayers.forEach(player -> context.getSource().sendSuccess(() -> Component.nullToEmpty(PlayerUtils.getPlayerName(player.uuid().toString()) + " - " + player.reason()), false));
                    return SINGLE_SUCCESS;
                }))
        );


    }
}
