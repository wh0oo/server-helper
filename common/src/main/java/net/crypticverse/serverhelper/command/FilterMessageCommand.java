package net.crypticverse.serverhelper.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.crypticverse.serverhelper.config.filter.FilterConfig;
import net.crypticverse.serverhelper.config.filter.Offense;
import net.crypticverse.serverhelper.config.filter.PlayerUtils;
import net.crypticverse.serverhelper.config.filter.ReplacementChar;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

// See: https://github.com/Declipsonator/Chat-Control/blob/main/src/main/java/me/declipsonator/chatcontrol/command/FilterCommand.java
public class FilterMessageCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("filter").requires(source -> source.hasPermission(1))
                .then(literal("add")
                        .then(literal("word").then(argument("to_block", StringArgumentType.word()).executes(context -> {
                            if(FilterConfig.isWord(context.getArgument("to_block", String.class))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Word already blocked"));
                                return 0;
                            }
                            FilterConfig.addWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Word added to filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("explicitWord").then(argument("to_block", StringArgumentType.word()).executes(context -> {
                            if(FilterConfig.isStandAloneWord(context.getArgument("to_block", String.class))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Standalone work already blocked"));
                                return 0;
                            }
                            FilterConfig.addStandAloneWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Standalone word added to filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("phrase").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(FilterConfig.isPhrase(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Phrase already blocked"));
                                return 0;
                            }
                            FilterConfig.addPhrase(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Phrase added to filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("regex").then(argument("to_block", StringArgumentType.greedyString()).executes(context -> {
                            if(FilterConfig.isRegex(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Regex already blocked"));
                                return 0;
                            }
                            if (StringArgumentType.getString(context, "to_block").startsWith("*")) {
                                context.getSource().sendFailure(Component.nullToEmpty("Regex cannot start with *!"));
                                return 0;
                            }
                            FilterConfig.addRegex(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Regex added to filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                )
                .then(literal("remove").requires(source -> source.hasPermission(4))
                        .then(literal("word").then(argument("to_block", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(FilterConfig.getWords(), builder)).executes(context -> {
                            if(!FilterConfig.isWord(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Word not found"));
                                return 0;
                            }
                            FilterConfig.removeWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Word removed from filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("explicitWord").then(argument("to_block", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(FilterConfig.getStandAloneWords(), builder)).executes(context -> {
                            if(!FilterConfig.isStandAloneWord(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Standalone word not found"));
                                return 0;
                            }
                            FilterConfig.removeStandAloneWord(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Standalone word removed from filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("phrase").then(argument("to_block", StringArgumentType.greedyString()).suggests((context, builder) -> SharedSuggestionProvider.suggest(FilterConfig.getPhrases(), builder)).executes(context -> {
                            if(!FilterConfig.isPhrase(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Phrase not found"));
                                return 0;
                            }
                            FilterConfig.removePhrase(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Phrase removed from filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("regex").then(argument("to_block", StringArgumentType.greedyString()).suggests((context, builder) -> SharedSuggestionProvider.suggest(FilterConfig.getRegexes(), builder)).executes(context -> {
                            if(!FilterConfig.isRegex(StringArgumentType.getString(context, "to_block"))) {
                                context.getSource().sendFailure(Component.nullToEmpty("Regex not found"));
                                return 0;
                            }
                            FilterConfig.removeRegex(StringArgumentType.getString(context, "to_block"));
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Regex removed from filter"), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                )
                .then(literal("list").requires(source -> source.hasPermission(4))
                        .then(literal("words").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Words: " + FilterConfig.getWords().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("explicitWords").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Standalone Words: " + FilterConfig.getStandAloneWords().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("phrases").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Phrases: " + FilterConfig.getPhrases().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("regexes").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Regexes: " + FilterConfig.getRegexes().toString()), false);
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("all").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Words: " + FilterConfig.getWords().toString()), false);
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Standalone Words: " + FilterConfig.getStandAloneWords().toString()), false);
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Phrases: " + FilterConfig.getPhrases().toString()), false);
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Filtered Regexes: " + FilterConfig.getRegexes().toString()), false);

                            return SINGLE_SUCCESS;
                        }))
                )
                .then(literal("config").requires(source -> source.hasPermission(4))
                        .then(literal("logFiltered").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Logging filtered messages: " + FilterConfig.logFiltered), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            FilterConfig.logFiltered = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Logging filtered messages: " + FilterConfig.logFiltered), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("tellPlayer").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Telling Player: " + FilterConfig.tellPlayer), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            FilterConfig.tellPlayer = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Telling Player: " + FilterConfig.tellPlayer), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("censorAndSend").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Censoring: " + FilterConfig.censorAndSend), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            FilterConfig.censorAndSend = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Censoring: " + FilterConfig.censorAndSend), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("ignorePrivateMessages").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Ignoring Private Messages: " + FilterConfig.ignorePrivateMessages), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            FilterConfig.ignorePrivateMessages = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Ignoring Private Messages: " + FilterConfig.ignorePrivateMessages), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("reload").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Reloading Config"), true);
                            FilterConfig.loadConfig();
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("save").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Saving Config"), true);
                            FilterConfig.saveConfig();
                            return SINGLE_SUCCESS;
                        }))
                        .then(literal("caseSensitive").executes(context -> {
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Case Sensitive: " + FilterConfig.caseSensitive), false);
                            return SINGLE_SUCCESS;
                        }).then(argument("value", BoolArgumentType.bool()).executes(context -> {
                            FilterConfig.caseSensitive = BoolArgumentType.getBool(context, "value");
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Case Sensitive: " + FilterConfig.caseSensitive), false);
                            FilterConfig.saveConfig();

                            return SINGLE_SUCCESS;
                        })))
                        .then(literal("replacementLetters").executes(context -> {
                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Replacement Letters: " + FilterConfig.getReplacementChars().toString()), false);
                                    return SINGLE_SUCCESS;
                                })
                                .then(literal("add").then(argument("string", StringArgumentType.greedyString()).executes(context -> {
                                    String replacements = StringArgumentType.getString(context, "string");
                                    String[] split = replacements.split(" ");
                                    if(replacements.length() != 3 || split.length != 2) {
                                        context.getSource().sendFailure(Component.nullToEmpty("Invalid Syntax, expected \"<to_replace> <replace_with> \""));
                                        return 0;
                                    }

                                    if(FilterConfig.isReplacementChar(new ReplacementChar(split[0].charAt(0), split[1].charAt(0)))) {
                                        context.getSource().sendFailure(Component.nullToEmpty("Letter already in list"));
                                        return 0;
                                    }

                                    FilterConfig.addReplacementChar(split[0].charAt(0), split[1].charAt(0));
                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Replacement added to configAdded"), false);
                                    FilterConfig.saveConfig();

                                    return SINGLE_SUCCESS;
                                })))
                                .then(literal("remove").then(argument("string", StringArgumentType.greedyString()).suggests((context, builder) -> {
                                    List<String> suggestions = new ArrayList<>();
                                    for (ReplacementChar replacementChar : FilterConfig.getReplacementChars()) {
                                        suggestions.add(replacementChar.toReplace + " " + replacementChar.replaceWith);
                                    }
                                    return SharedSuggestionProvider.suggest(suggestions, builder);
                                }).executes(context -> {
                                    String replacements = StringArgumentType.getString(context, "string");
                                    String[] split = replacements.split(" ");
                                    if(replacements.length() != 3 || split.length != 2) {
                                        context.getSource().sendFailure(Component.nullToEmpty("Invalid Syntax, expected \"<to_replace> <replace_with> \""));
                                        return 0;
                                    }

                                    if(!FilterConfig.isReplacementChar(new ReplacementChar(split[0].charAt(0), split[1].charAt(0)))) {
                                        context.getSource().sendFailure(Component.nullToEmpty("Letter not in list"));
                                        return 0;
                                    }

                                    FilterConfig.removeReplacementChar(split[0].charAt(0), split[1].charAt(0));
                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Replacement removed from configRemoved"), false);

                                    FilterConfig.saveConfig();

                                    return SINGLE_SUCCESS;
                                }))))
                        .then(literal("ignoredPlayers").executes(context -> {
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Ignored players:" + FilterConfig.getIgnoredPlayers().toString()), false);
                                            return SINGLE_SUCCESS;
                                        })
                                        .then(literal("add").then(argument("player", GameProfileArgument.gameProfile()).executes(context -> {
                                            for(GameProfile profile : GameProfileArgument.getGameProfiles(context, "player")) {
                                                if (FilterConfig.isIgnored(profile.getId())) continue;
                                                FilterConfig.addIgnoredPlayer(profile.getId());
                                                context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" is now ignored")), false);
                                            }
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                                        .then(literal("remove").then(argument("player", GameProfileArgument.gameProfile()).suggests((context, builder) -> {
                                            List<UUID> suggestions = FilterConfig.getIgnoredPlayers();
                                            List<String> stringSuggestions = PlayerUtils.getPlayerNames(suggestions);

                                            return SharedSuggestionProvider.suggest(stringSuggestions, builder);
                                        }).executes(context -> {
                                            for(GameProfile profile : GameProfileArgument.getGameProfiles(context, "player")) {
                                                if (!FilterConfig.isIgnored(profile.getId())) continue;
                                                FilterConfig.removeIgnoredPlayer(profile.getId());
                                                context.getSource().sendSuccess(() -> Component.nullToEmpty(profile.getName()).copy().append(Component.nullToEmpty(" is no longer ignored")), false);
                                            }
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                        )
                        .then(literal("muteAfterOffense").executes(context -> {
                                            if(!FilterConfig.muteAfterOffense) {
                                                context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute after offense: " + FilterConfig.muteAfterOffense), false);
                                            } else {
                                                if(FilterConfig.muteAfterOffenseType == FilterConfig.MuteType.PERMANENT) {
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute after offense: " + FilterConfig.muteAfterOffense), false);
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Number of offenses before mute: " + FilterConfig.muteAfterOffenseNumber), false);
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Minutes before offenses expire: " + FilterConfig.offenseExpireMinutes), false);
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Permanent mute"), false);
                                                } else {
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute after offense: " + FilterConfig.muteAfterOffense), false);
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Number of offenses before mute: " + FilterConfig.muteAfterOffenseNumber), false);
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Minutes before offenses expire: " + FilterConfig.offenseExpireMinutes), false);
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Temporary mute for: " + FilterConfig.muteAfterOffenseMinutes + " minutes"), false);
                                                }

                                            }
                                            return SINGLE_SUCCESS;
                                        })
                                        .then(literal("set").then(argument("value", BoolArgumentType.bool()).executes(context -> {
                                            FilterConfig.muteAfterOffense = BoolArgumentType.getBool(context, "value");
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute after offense: " + FilterConfig.muteAfterOffense), false);
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                                        .then(literal("type").then(argument("type", StringArgumentType.word()).suggests((context, builder) -> {
                                            List<String> suggestions = new ArrayList<>();
                                            for(FilterConfig.MuteType type : FilterConfig.MuteType.values()) {
                                                suggestions.add(type.name());
                                            }
                                            return SharedSuggestionProvider.suggest(suggestions, builder);
                                        }).executes(context -> {
                                            FilterConfig.muteAfterOffenseType = FilterConfig.MuteType.valueOf(StringArgumentType.getString(context, "type").toUpperCase());
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute After Offense Type: " + FilterConfig.muteAfterOffenseType), true);
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                                        .then(literal("number").then(argument("number", IntegerArgumentType.integer(0)).executes(context -> {
                                            FilterConfig.muteAfterOffenseNumber = IntegerArgumentType.getInteger(context, "number");
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute After Offense Number: " + FilterConfig.muteAfterOffenseNumber), true);
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                                        .then(literal("length").then(argument("minutes", IntegerArgumentType.integer(0)).executes(context -> {
                                            FilterConfig.muteAfterOffenseMinutes = IntegerArgumentType.getInteger(context, "minutes");
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Mute After Offense Minutes: " + FilterConfig.muteAfterOffenseMinutes), true);
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                                        .then(literal("expireMinutes").then(argument("minutes", IntegerArgumentType.integer(0)).executes(context -> {
                                            FilterConfig.offenseExpireMinutes = IntegerArgumentType.getInteger(context, "minutes");
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Minutes before offenses expire: " + FilterConfig.offenseExpireMinutes), true);
                                            FilterConfig.saveConfig();

                                            return SINGLE_SUCCESS;
                                        })))
                                        .then(literal("currentOffenses").executes(context -> {
                                            StringBuilder offenses = new StringBuilder("[");
                                            for(Offense offense : FilterConfig.offenses) {
                                                UUID uuid = offense.uuid();
                                                offenses.append(PlayerUtils.getPlayerName(uuid.toString())).append(": ").append(FilterConfig.offenseCount(uuid)).append(" offenses,");
                                            }
                                            offenses = new StringBuilder(offenses.substring(0, offenses.length() - 1) + "]");
                                            String finalOffenses = offenses.toString();
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Offenses: " + finalOffenses), false);
                                            return SINGLE_SUCCESS;
                                        }))
                        )

                )
        );
    }
}